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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.annotation.Meta;
import org.apache.tapestry.annotation.Persist;
import org.apache.tapestry.integration.app1.base.BaseComponent;

@Meta("tapestry.persistence-strategy=flash")
public class FlashDemo extends BaseComponent
{
    @Persist
    private String message;

    public String getMessage()
    {
        return message;
    }

    void onAction()
    {
        message = "You clicked the link!";
    }
}