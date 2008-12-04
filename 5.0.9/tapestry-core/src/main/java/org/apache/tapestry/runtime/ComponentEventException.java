// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.runtime;

import org.apache.tapestry.ioc.internal.util.TapestryException;

/**
 * A wrapper exception around any exception thrown when invoking a component event handler. In some cases, the
 * underlying exception may have been a declared exception, and will be wrapped in a RuntimeException.
 *
 * @see org.apache.tapestry.ioc.util.ExceptionUtils#findCause(Throwable, Class)
 */
public class ComponentEventException extends TapestryException
{
    private final String _eventType;
    private final Object[] _context;

    /**
     * @param message   exception message
     * @param eventType type of event that triggered the exception
     * @param context   context passed with the failed event
     * @param location  location of the component while failed (may be null)
     * @param cause     underlying exception
     */
    public ComponentEventException(String message, String eventType, Object[] context, Object location, Throwable cause)
    {
        super(message, location, cause);

        _eventType = eventType;
        _context = context;
    }

    public String getEventType()
    {
        return _eventType;
    }

    public Object[] getContext()
    {
        return _context;
    }
}