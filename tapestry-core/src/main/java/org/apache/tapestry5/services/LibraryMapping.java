// Copyright 2006, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * Used to configure the {@link ComponentClassResolver}, to allow it to map prefixes to library root packages (the
 * application namespace is a special case of this). In each case, a prefix on the path is mapped to a package. Starting
 * with Tapestry 5.2, the path prefix may not contain a slash character.
 * <p/>
 * The root package name should have a number of sub-packages:
 * <dl>
 * <dt>pages</dt>
 * <dd>contains named pages</dd>
 * <dt>components</dt>
 * <dd>contains components</dd>
 * <dt>mixins</dt>
 * <dd>contains component mixins</dd>
 * <dt>base</dt>
 * <dd>contains base classes</dd>
 * </dl>
 * 
 * @see org.apache.tapestry5.services.TapestryModule#contributeComponentClassResolver(org.apache.tapestry5.ioc.Configuration)
 */
public final class LibraryMapping
{
    private final String virtualFolderName, rootPackage;

    /**
     * Maps a virtual folder to a package that contains sub-packages for components, pages, etc. The special pathPrefix
     * "" (the empty string) identifies the application. Tapestry defines a special pathPrefix, "core", for the core
     * components.
     * <p>
     * Note that it <em>is</em> allowed to contribute mutiple LibraryMappings with the same prefix to the
     * {@link ComponentClassResolver}, and the results are merged (though conflicts, where the same simple name appears
     * under multiple root packages, is not currently checked for).
     * 
     * @param virtualFolderName
     *            identifies the virtual folder "containing" the pages and components of the library. Prior to Tapestry
     *            5.2, the name could include a slash, but this is now expressly forbidden.
     * @param rootPackage
     *            The root package to search.
     */
    public LibraryMapping(String virtualFolderName, String rootPackage)
    {
        assert InternalUtils.isNonBlank(rootPackage);
        
        if (virtualFolderName.contains("/"))
            throw new RuntimeException(
                    "LibraryMapping path prefixes may no longer contain slashes (as of Tapestry 5.2).");

        this.virtualFolderName = virtualFolderName;
        this.rootPackage = rootPackage;
    }

    /**
     * Returns the virtual folder name (the odd name of this method reflects the evolution of the framework).
     */
    public String getPathPrefix()
    {
        return virtualFolderName;
    }

    public String getRootPackage()
    {
        return rootPackage;
    }

    @Override
    public String toString()
    {
        return String.format("LibraryMapping[%s, %s]", virtualFolderName, rootPackage);
    }
}
