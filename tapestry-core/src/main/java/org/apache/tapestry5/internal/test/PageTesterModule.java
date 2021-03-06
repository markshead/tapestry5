// Copyright 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.internal.services.CookieSink;
import org.apache.tapestry5.internal.services.CookieSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.apache.tapestry5.services.MarkupRendererFilter;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.test.PageTester;

/**
 * Used in conjunction with {@link PageTester} to mock up and/or stub out portions of Tapestry that
 * need to be handled differently when testing.
 */
@SuppressWarnings("rawtypes")
public class PageTesterModule
{
    public static final String TEST_MODE = "test";

    public static void bind(ServiceBinder binder)
    {
        binder.bind(TestableRequest.class, TestableRequestImpl.class);
        binder.bind(TestableResponse.class, TestableResponseImpl.class);
    }

    @Contribute(ServiceOverride.class)
    public static void setupTestableOverrides(MappedConfiguration<Class, Object> configuration, @Local
    TestableRequest request, @Local
    TestableResponse response)
    {
        configuration.add(Request.class, request);
        configuration.add(Response.class, response);

        TestableCookieSinkSource cookies = new TestableCookieSinkSource();

        configuration.add(CookieSink.class, cookies);
        configuration.add(CookieSource.class, cookies);
    }

    public static void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration)
    {
        configuration.addInstance("EndOfRequestCleanup", EndOfRequestCleanupFilter.class, "before:StaticFiles");
    }

    public static void contributeMarkupRenderer(OrderedConfiguration<MarkupRendererFilter> configuration)
    {
        configuration.addInstance("CaptureRenderedDocument", CaptureRenderedDocument.class, "before:DocumentLinker");
    }
}
