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
import static org.junit.Assert.assertNotNull;

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

        assertEquals(2, regions.getRegions().size());

        final ApiRegion global = regions.getRegions().get(0);
        assertEquals("global", global.getName());

        assertEquals(2, global.getExports().size());

        final ApiRegion internal = regions.getRegions().get(1);
        assertEquals("internal", internal.getName());

        assertEquals(1, internal.getExports().size());
    }
}
