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

package org.apache.tapestry.integration.app1.components;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.Parameter;

/**
 * Echos out its value parameter. Uused to test parameter overrides between component annoation and
 * template. Also, used to test parameter defaulter methods.
 */
public class Echo
{
    @Parameter("componentResources.completeId")
    private String _value;

    @BeginRender
    void render(MarkupWriter writer)
    {
        writer.write(_value);
    }
}