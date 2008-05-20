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

package org.apache.tapestry.internal;

import org.apache.tapestry.ioc.*;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.services.SymbolProvider;

/**
 * Makes a contribution to the SymbolSource service configuration.
 */
public class SyntheticSymbolSourceContributionDef implements ContributionDef
{
    private final String contributionName;

    private final SymbolProvider provider;

    private final String[] constraints;

    public SyntheticSymbolSourceContributionDef(String contributionName, SymbolProvider provider,
                                                String... constraints)
    {
        this.contributionName = contributionName;
        this.provider = provider;
        this.constraints = constraints;
    }

    public void contribute(ModuleBuilderSource moduleBuilderSource, ObjectLocator locator,
                           Configuration configuration)
    {
    }

    @SuppressWarnings("unchecked")
    public void contribute(ModuleBuilderSource moduleBuilderSource, ObjectLocator locator,
                           OrderedConfiguration configuration)
    {
        configuration.add(contributionName, provider, constraints);
    }

    public void contribute(ModuleBuilderSource moduleBuilderSource, ObjectLocator locator,
                           MappedConfiguration configuration)
    {
    }

    /**
     * Returns "SymbolSource".
     */
    public String getServiceId()
    {
        return "SymbolSource";
    }

}