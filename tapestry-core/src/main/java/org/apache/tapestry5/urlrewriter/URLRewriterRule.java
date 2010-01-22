// Copyright 2009 The Apache Software Foundation
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
package org.apache.tapestry5.urlrewriter;

import org.apache.tapestry5.services.Request;

/**
 * Defines an URL rewriting rule. It is executed before any Tapestry request processing is done.
 */
public interface URLRewriterRule
{

    /**
     * Processes a {@linkplain org.apache.tapestry5.services.Request}. Implementations of this
     * method must check if they need to rewrite this request. If no, it must return the received
     * request unchanged. This method cannot return null.
     * 
     * @param request
     *            a {@link org.apache.tapestry5.services.Request}.
     * @return request a {@link org.apache.tapestry5.services.Request}.
     */
    Request process(Request request,URLRewriteContext context);

    RewriteRuleApplicability applicability();
}