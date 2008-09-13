// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.base;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.internal.services.ComponentInvocation;
import org.apache.tapestry.internal.services.ComponentInvocationMap;
import org.apache.tapestry.internal.services.MarkupWriterImpl;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.internal.test.PageTesterComponentInvocationMap;
import org.testng.annotations.Test;

public class AbstractLinkTest extends InternalBaseTestCase
{
    private final static String LINK_URI = "/foo/bar.baz";

    private final AbstractLink _linkFixture = new AbstractLink()
    {
    };

    @Test
    public void no_anchor()
    {
        Link link = mockLink();
        ComponentResources resources = mockComponentResources();
        ComponentInvocationMap map = new PageTesterComponentInvocationMap();
        MarkupWriter writer = new MarkupWriterImpl();
        ComponentInvocation invocation = mockComponentInvocation();

        map.store(link, invocation);

        train_toURI(link, LINK_URI);

        resources.renderInformalParameters(writer);

        replay();

        _linkFixture.inject(null, map, resources);

        _linkFixture.writeLink(writer, "fred", link);

        verify();

        Element e = writer.getElement();

        writer.write("link text");
        writer.end();

        assertEquals(writer.toString(), "<a href=\"/foo/bar.baz\" id=\"fred\">link text</a>");
        assertSame(map.get(e), invocation);
    }

    @Test
    public void with_anchor()
    {
        Link link = mockLink();
        ComponentResources resources = mockComponentResources();
        ComponentInvocationMap map = new PageTesterComponentInvocationMap();
        MarkupWriter writer = new MarkupWriterImpl();
        ComponentInvocation invocation = mockComponentInvocation();

        map.store(link, invocation);

        train_toURI(link, LINK_URI);

        resources.renderInformalParameters(writer);

        replay();

        _linkFixture.inject("wilma", map, resources);

        _linkFixture.writeLink(writer, "barney", link);

        verify();

        Element e = writer.getElement();

        writer.write("link text");
        writer.end();

        assertEquals(writer.toString(), "<a href=\"/foo/bar.baz#wilma\" id=\"barney\">link text</a>");
        assertSame(map.get(e), invocation);
    }

    protected final void train_toURI(Link link, String URI)
    {
        expect(link.toURI()).andReturn(URI);
    }

    protected final ComponentInvocation mockComponentInvocation()
    {
        return newMock(ComponentInvocation.class);
    }
}