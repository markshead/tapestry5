// Copyright 2006, 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.internal.services;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.ioc.internal.util.ConcurrentBarrier;
import org.apache.tapestry.ioc.internal.util.Invokable;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestFilter;
import org.apache.tapestry.services.RequestHandler;
import org.apache.tapestry.services.Response;

/**
 * Implements a barrier that periodically asks the
 * {@link org.apache.tapestry.internal.services.UpdateListenerHub} to check for updates to files.
 * The UpdateListenerHub is invoked from a write method, meaning that when it is called, all other
 * threads will be blocked.
 */
public class CheckForUpdatesFilter implements RequestFilter
{
    private long _lastCheck = 0;

    private final long _checkInterval;

    private final long _updateTimeout;

    private final UpdateListenerHub _updateListenerHub;

    private final ConcurrentBarrier _barrier = new ConcurrentBarrier();

    private final Runnable _checker = new Runnable()
    {
        public void run()
        {
            // On a race condition, multiple threads may hit this method briefly. If we've
            // already done a check, don't run it again.

            if (System.currentTimeMillis() - _lastCheck >= _checkInterval)
            {

                // Fire the update event which will force a number of checks and then
                // corresponding invalidation events.

                _updateListenerHub.fireUpdateEvent();

                _lastCheck = System.currentTimeMillis();
            }
        }
    };

    /**
     * @param updateListenerHub
     *            invoked, at intervals, to spur the process of detecting changes
     * @param checkInterval
     *            interval, in milliseconds, between checks
     * @param updateTimeout
     *            time, in  milliseconds, to wait to obtain update lock.
     */
    public CheckForUpdatesFilter(UpdateListenerHub updateListenerHub, long checkInterval, long updateTimeout)
    {
        _updateListenerHub = updateListenerHub;
        _checkInterval = checkInterval;
        _updateTimeout = updateTimeout;
    }

    public boolean service(final Request request, final Response response,
            final RequestHandler handler) throws IOException
    {
        final Holder<IOException> exceptionHolder = new Holder<IOException>();

        Invokable<Boolean> invokable = new Invokable<Boolean>()
        {
            public Boolean invoke()
            {
                if (System.currentTimeMillis() - _lastCheck >= _checkInterval)
                    _barrier.tryWithWrite(_checker, _updateTimeout, TimeUnit.MILLISECONDS);

                // And, now, back to code within the read lock.
                
                try
                {
                    return handler.service(request, response);
                }
                catch (IOException ex)
                {
                    exceptionHolder.put(ex);
                    return false;
                }
            }
        };

        // Obtain a read lock while handling the request. This will not impair parallel operations, except when a file check
        // is needed (the exclusive write lock will block threads attempting to get a read lock).
        
        boolean result = _barrier.withRead(invokable);

        IOException ex = exceptionHolder.get();

        if (ex != null)
            throw ex;

        return result;
    }

}