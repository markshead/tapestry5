// Copyright 2006 The Apache Software Foundation
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

import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.InjectionProvider;

/**
 * Provider that looks for a unique service with the proper type.
 * 
 * 
 */
public class DefaultInjectionProvider implements InjectionProvider
{

    @SuppressWarnings("unchecked")
    public boolean provideInjection(String fieldName, String fieldType, ServiceLocator locator,
            ClassTransformation transformation, MutableComponentModel componentModel)
    {
        Class type = transformation.toClass(fieldType);

        // Try to find a unique service in the Registry that matches the indicated type.

        Object value = locator.getService(type);

        transformation.injectField(fieldName, value);

        // Only gets here if successful.

        return true;
    }

}