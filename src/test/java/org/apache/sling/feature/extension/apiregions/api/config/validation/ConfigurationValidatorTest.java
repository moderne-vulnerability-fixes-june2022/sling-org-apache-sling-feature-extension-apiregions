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
package org.apache.sling.feature.extension.apiregions.api.config.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.sling.feature.Configuration;
import org.apache.sling.feature.extension.apiregions.api.config.ConfigurationDescription;
import org.apache.sling.feature.extension.apiregions.api.config.FactoryConfigurationDescription;
import org.junit.Test;

public class ConfigurationValidatorTest {

    private final ConfigurationValidator validator = new ConfigurationValidator();

    @Test public void testWrongDescriptionTypeForConfiguration() {
        final Configuration cfg = new Configuration("org.apache");
        final FactoryConfigurationDescription fcd = new FactoryConfigurationDescription();

        final ConfigurationValidationResult result = validator.validate(fcd, cfg);
        assertFalse(result.isValid());
        assertEquals(1, result.getGlobalErrors().size());
    }

    @Test public void testWrongDescriptionTypeForFactoryConfiguration() {
        final Configuration cfg = new Configuration("org.apache~foo");
        final ConfigurationDescription fcd = new ConfigurationDescription();

        final ConfigurationValidationResult result = validator.validate(fcd, cfg);
        assertFalse(result.isValid());
        assertEquals(1, result.getGlobalErrors().size());
    }
}
