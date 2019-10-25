/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.feature.extension.apiregions.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

public class TestApiRegions {

    private String readJSON(final String name) throws IOException {
        try (final Reader reader = new InputStreamReader(
                TestApiRegions.class.getResourceAsStream("/json/" + name + ".json"),
                "UTF-8"); final Writer writer = new StringWriter()) {
            int l;
            char[] buf = new char[2048];
            while ((l = reader.read(buf)) > -1) {
                writer.write(buf, 0, l);
            }

            return writer.toString();
        }
    }

    @Test
    public void testParsing() throws Exception {
        final String json = readJSON("apis");

        final ApiRegions regions = ApiRegions.parse(json);
        assertNotNull(regions);

        assertEquals(2, regions.listRegions().size());

        final ApiRegion global = regions.listRegions().get(0);
        assertEquals("global", global.getName());

        assertEquals(2, global.getExports().size());

        final ApiRegion internal = regions.listRegions().get(1);
        assertEquals("internal", internal.getName());

        assertEquals(1, internal.getExports().size());
    }

    @Test
    public void testOrdering() throws Exception {
        final ApiRegions regions = new ApiRegions();
        final ApiRegion one = new ApiRegion("one");
        final ApiRegion two = new ApiRegion("two");
        final ApiRegion three = new ApiRegion("three");

        final ApiRegion duplicate = new ApiRegion("two");
        final ApiRegion other = new ApiRegion("other");

        assertTrue(regions.addUniqueRegion(one));
        assertTrue(regions.addUniqueRegion(two));
        assertTrue(regions.addUniqueRegion(three));

        assertFalse(regions.addUniqueRegion(duplicate));

        assertEquals(3, regions.listRegions().size());

        assertNull(one.getParent());
        assertEquals(two, one.getChild());
        assertEquals(one, two.getParent());
        assertEquals(three, two.getChild());
        assertEquals(two, three.getParent());
        assertNull(three.getChild());

        assertFalse(regions.remove(other));
        assertTrue(regions.remove(two));

        assertEquals(2, regions.listRegions().size());
        assertNull(one.getParent());
        assertEquals(three, one.getChild());
        assertEquals(one, three.getParent());
        assertNull(three.getChild());
    }

    @Test
    public void testExports() throws Exception {
        final ApiRegions regions = new ApiRegions();

        final ApiRegion one = new ApiRegion("one");
        one.getExports().add(new ApiExport("a"));

        final ApiRegion two = new ApiRegion("two");
        two.getExports().add(new ApiExport("b"));

        final ApiRegion three = new ApiRegion("three");
        three.getExports().add(new ApiExport("c"));

        assertTrue(regions.addUniqueRegion(one));
        assertTrue(regions.addUniqueRegion(two));
        assertTrue(regions.addUniqueRegion(three));

        assertEquals(1, one.getAllExports().size());
        assertTrue(one.getAllExports().contains(new ApiExport("a")));
        assertEquals(1, one.getExports().size());
        assertTrue(one.getExports().contains(new ApiExport("a")));

        assertEquals(2, two.getAllExports().size());
        assertTrue(two.getAllExports().contains(new ApiExport("a")));
        assertTrue(two.getAllExports().contains(new ApiExport("b")));
        assertEquals(1, two.getExports().size());
        assertTrue(two.getExports().contains(new ApiExport("b")));

        assertEquals(3, three.getAllExports().size());
        assertTrue(three.getAllExports().contains(new ApiExport("a")));
        assertTrue(three.getAllExports().contains(new ApiExport("b")));
        assertTrue(three.getAllExports().contains(new ApiExport("c")));
        assertEquals(1, three.getExports().size());
        assertTrue(three.getExports().contains(new ApiExport("c")));
    }
}
