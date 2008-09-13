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

import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.ClasspathResource;

import java.io.IOException;

/**
 * Responsible for streaming the contents of a resource to the client. The {@link Resource} to stream is almost always a
 * {@link ClasspathResource}.
 */
public interface ResourceStreamer
{
    /**
     * Streams the content of the resource to the client.
     */
    void streamResource(Resource resource) throws IOException;
}