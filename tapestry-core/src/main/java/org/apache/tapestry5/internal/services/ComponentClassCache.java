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

package org.apache.tapestry.internal.services;

/**
 * A cache for converting between class names and component (or other) classes.  For component classes, ensures that the
 * class is the transformed class.
 */
public interface ComponentClassCache
{
    /**
     * Gets the Class instance for the given fully-qualified class name.
     *
     * @param className fully qualified class name, or a primitive type name, or an array name (in source format)
     * @return the class instance
     */
    Class forName(String className);
}