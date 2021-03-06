// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.assets;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

public class SRSCompressingInterceptor implements StreamableResourceSource
{
    private final int compressionCutoff;

    private final StreamableResourceSource delegate;

    public SRSCompressingInterceptor(int compressionCutoff, StreamableResourceSource delegate)
    {
        this.compressionCutoff = compressionCutoff;
        this.delegate = delegate;
    }

    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing)
            throws IOException
    {
        StreamableResource streamable = delegate.getStreamableResource(baseResource, processing);

        if (processing == StreamableResourceProcessing.COMPRESSION_ENABLED) { return compress(streamable); }

        return streamable;
    }

    private StreamableResource compress(StreamableResource uncompressed) throws IOException
    {
        if (uncompressed.getCompression() != CompressionStatus.COMPRESSABLE)
            return uncompressed;

        int size = uncompressed.getSize();

        // Because of GZIP overhead, streams below a certain point actually get larger when compressed so
        // we don't even try.

        if (size < compressionCutoff)
            return uncompressed;

        ByteArrayOutputStream bos = new ByteArrayOutputStream(size);

        GZIPOutputStream gos = new GZIPOutputStream(bos);
        BufferedOutputStream buffered = new BufferedOutputStream(gos);

        uncompressed.streamTo(buffered);

        buffered.close();

        BytestreamCache cache = new BytestreamCache(bos);

        return new StreamableResourceImpl(uncompressed.getContentType(), CompressionStatus.COMPRESSED,
                uncompressed.getLastModified(), cache);
    }
}
