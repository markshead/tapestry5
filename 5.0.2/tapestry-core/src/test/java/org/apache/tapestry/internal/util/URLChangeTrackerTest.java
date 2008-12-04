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

package org.apache.tapestry.internal.util;

import java.io.File;
import java.net.URL;

import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

public class URLChangeTrackerTest extends TapestryTestCase
{
    @Test
    public void contains_change_when_empty()
    {
        URLChangeTracker t = new URLChangeTracker();

        assertFalse(t.containsChanges());
    }

    @Test
    public void contains_changes() throws Exception
    {
        URLChangeTracker t = new URLChangeTracker();

        File f = File.createTempFile("changetracker0", ".tmp");
        URL url = f.toURL();

        t.add(url);

        assertEquals(t.trackedFileCount(), 1);

        assertFalse(t.containsChanges());

        boolean changed = false;

        // Because of clock accuracy, we need to try a couple of times
        // to ensure that the change to the file is visible in the
        // lastUpdated time stamp on the URL.

        for (int i = 0; i < 10 && !changed; i++)
        {
            Thread.sleep(100);

            touch(f);

            changed = t.containsChanges();
        }

        assertTrue(changed);

        // And, once a change has been observed ...

        assertFalse(t.containsChanges());
    }

    @Test
    public void non_file_URLs_are_ignored() throws Exception
    {
        URLChangeTracker t = new URLChangeTracker();

        URL url = new URL("ftp://breeblebrox.com");

        t.add(url);

        assertEquals(t.trackedFileCount(), 0);
    }

    @Test
    public void caching() throws Exception
    {
        URLChangeTracker t = new URLChangeTracker();

        File f = File.createTempFile("changetracker0", ".tmp");
        URL url = f.toURL();

        long initial = t.add(url);

        touch(f);

        long current = t.add(url);

        assertEquals(current, initial);

        assertTrue(t.containsChanges());

        t.clear();

        current = t.add(url);

        assertFalse(current == initial);
    }

    @Test
    public void deleted_files_show_as_changes() throws Exception
    {
        File f = File.createTempFile("changetracker0", ".tmp");
        URL url = f.toURL();

        URLChangeTracker t = new URLChangeTracker();

        long timeModified = t.add(url);

        assertTrue(timeModified > 0);

        assertEquals(t.trackedFileCount(), 1);

        assertFalse(t.containsChanges());

        assertTrue(f.delete());

        assertTrue(t.containsChanges());
    }

}