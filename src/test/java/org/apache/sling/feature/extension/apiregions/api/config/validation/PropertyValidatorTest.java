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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.feature.extension.apiregions.api.config.Option;
import org.apache.sling.feature.extension.apiregions.api.config.PropertyDescription;
import org.apache.sling.feature.extension.apiregions.api.config.PropertyType;
import org.apache.sling.feature.extension.apiregions.api.config.Range;
import org.junit.Test;

public class PropertyValidatorTest {
    
    private final PropertyValidator validator = new PropertyValidator();
    
    @Test public void testValidateWithNull() {
        final PropertyDescription prop = new PropertyDescription();

        // prop not required - no error
        assertTrue(validator.validate(prop, null).getErrors().isEmpty());
        assertTrue(validator.validate(prop, null).isValid());

        // prop required - error
        prop.setRequired(true);
        assertEquals(1, validator.validate(prop, null).getErrors().size());
        assertFalse(validator.validate(prop, null).isValid());
    }

    @Test public void testValidateBoolean() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.BOOLEAN);

        final List<String> messages = new ArrayList<>();

        validator.validateBoolean(prop, Boolean.TRUE, messages);
        assertTrue(messages.isEmpty());

        validator.validateBoolean(prop, Boolean.FALSE, messages);
        assertTrue(messages.isEmpty());

        validator.validateBoolean(prop, "TRUE", messages);
        assertTrue(messages.isEmpty());

        validator.validateBoolean(prop, "FALSE", messages);
        assertTrue(messages.isEmpty());

        validator.validateBoolean(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        validator.validateBoolean(prop, 1, messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidateByte() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.BYTE);

        final List<String> messages = new ArrayList<>();

        validator.validateByte(prop, (byte)1, messages);
        assertTrue(messages.isEmpty());

        validator.validateByte(prop, "1", messages);
        assertTrue(messages.isEmpty());

        validator.validateByte(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        validator.validateByte(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateShort() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.SHORT);

        final List<String> messages = new ArrayList<>();

        validator.validateShort(prop, (short)1, messages);
        assertTrue(messages.isEmpty());

        validator.validateShort(prop, "1", messages);
        assertTrue(messages.isEmpty());

        validator.validateShort(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        validator.validateShort(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateInteger() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.INTEGER);

        final List<String> messages = new ArrayList<>();

        validator.validateInteger(prop, 1, messages);
        assertTrue(messages.isEmpty());

        validator.validateInteger(prop, "1", messages);
        assertTrue(messages.isEmpty());

        validator.validateInteger(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        validator.validateInteger(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateLong() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.LONG);

        final List<String> messages = new ArrayList<>();

        validator.validateLong(prop, 1L, messages);
        assertTrue(messages.isEmpty());

        validator.validateLong(prop, "1", messages);
        assertTrue(messages.isEmpty());

        validator.validateLong(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        validator.validateLong(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateFloat() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.FLOAT);

        final List<String> messages = new ArrayList<>();

        validator.validateFloat(prop, 1.1, messages);
        assertTrue(messages.isEmpty());

        validator.validateFloat(prop, "1.1", messages);
        assertTrue(messages.isEmpty());

        validator.validateFloat(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        validator.validateFloat(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateDouble() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.DOUBLE);

        final List<String> messages = new ArrayList<>();

        validator.validateDouble(prop, 1.1d, messages);
        assertTrue(messages.isEmpty());

        validator.validateDouble(prop, "1.1", messages);
        assertTrue(messages.isEmpty());

        validator.validateDouble(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        validator.validateDouble(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateChar() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.CHARACTER);

        final List<String> messages = new ArrayList<>();

        validator.validateCharacter(prop, 'x', messages);
        assertTrue(messages.isEmpty());

        validator.validateCharacter(prop, "y", messages);
        assertTrue(messages.isEmpty());

        validator.validateCharacter(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidateUrl() {
        final List<String> messages = new ArrayList<>();

        validator.validateURL(null, "https://sling.apache.org/documentation", messages);
        assertTrue(messages.isEmpty());

        validator.validateURL(null, "hello world", messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidateEmail() {
        final List<String> messages = new ArrayList<>();

        validator.validateEmail(null, "a@b.com", messages);
        assertTrue(messages.isEmpty());

        validator.validateEmail(null, "hello world", messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidatePassword() {
        final PropertyDescription prop = new PropertyDescription();
        final List<String> messages = new ArrayList<>();

        validator.validatePassword(prop, null, messages);
        assertEquals(1, messages.size());
        messages.clear();

        prop.setVariable("secret");
        validator.validatePassword(prop, null, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateRange() {
        final List<String> messages = new ArrayList<>();
        final PropertyDescription prop = new PropertyDescription();

        // no range set
        validator.validateRange(prop, 2, messages);
        assertTrue(messages.isEmpty());

        // empty range set
        prop.setRange(new Range());
        validator.validateRange(prop, 2, messages);
        assertTrue(messages.isEmpty());

        // min set
        prop.getRange().setMin(5);
        validator.validateRange(prop, 5, messages);
        assertTrue(messages.isEmpty());
        validator.validateRange(prop, 6, messages);
        assertTrue(messages.isEmpty());
        validator.validateRange(prop, 4, messages);
        assertEquals(1, messages.size());
        messages.clear();

        validator.validateRange(prop, 5.0, messages);
        assertTrue(messages.isEmpty());
        validator.validateRange(prop, 6.0, messages);
        assertTrue(messages.isEmpty());
        validator.validateRange(prop, 4.0, messages);
        assertEquals(1, messages.size());
        messages.clear();

        // max set
        prop.getRange().setMax(6);
        validator.validateRange(prop, 5, messages);
        assertTrue(messages.isEmpty());
        validator.validateRange(prop, 6, messages);
        assertTrue(messages.isEmpty());
        validator.validateRange(prop, 7, messages);
        assertEquals(1, messages.size());
        messages.clear();

        validator.validateRange(prop, 5.0, messages);
        assertTrue(messages.isEmpty());
        validator.validateRange(prop, 6.0, messages);
        assertTrue(messages.isEmpty());
        validator.validateRange(prop, 7.0, messages);
        assertEquals(1, messages.size());
        messages.clear();
    }   
    
    @Test public void testValidateRegex() {
        final List<String> messages = new ArrayList<>();
        final PropertyDescription prop = new PropertyDescription();

        // no regex
        validator.validateRegex(prop, "hello world", messages);
        validator.validateRegex(prop, "world", messages);
        assertTrue(messages.isEmpty());

        // regex
        prop.setRegex("h(.*)");
        validator.validateRegex(prop, "hello world", messages);
        assertTrue(messages.isEmpty());
        validator.validateRegex(prop, "world", messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidateOptions() {
        final List<String> messages = new ArrayList<>();
        final PropertyDescription prop = new PropertyDescription();

        // no options
        validator.validateOptions(prop, "foo", messages);
        validator.validateOptions(prop, "bar", messages);
        assertTrue(messages.isEmpty());

        // options
        final List<Option> options = new ArrayList<>();
        final Option o1 = new Option();
        o1.setValue("foo");
        final Option o2 = new Option();
        o2.setValue("7");
        options.add(o1);
        options.add(o2);
        prop.setOptions(options);
        validator.validateOptions(prop, "foo", messages);
        assertTrue(messages.isEmpty());
        validator.validateOptions(prop, "bar", messages);
        assertEquals(1, messages.size());
        messages.clear();
        validator.validateOptions(prop, 7, messages);
        assertTrue(messages.isEmpty());
    }
    
    @Test public void testValidateList() {
        final List<String> messages = new ArrayList<>();
        final PropertyDescription prop = new PropertyDescription();

        final List<Object> values = new ArrayList<>();
        values.add("a");
        values.add("b");
        values.add("c");

        // default cardinality - no excludes/includes
        validator.validateList(prop, values, messages);
        assertEquals(1, messages.size());
        messages.clear();

        // cardinality 3 - no excludes/includes
        prop.setCardinality(3);
        validator.validateList(prop, values, messages);
        assertTrue(messages.isEmpty());

        values.add("d");
        validator.validateList(prop, values, messages);
        assertEquals(1, messages.size());
        messages.clear();

        // excludes
        prop.setExcludes(new String[] {"d", "e"});
        validator.validateList(prop, values, messages);
        assertEquals(2, messages.size()); // cardinality and exclude
        messages.clear();

        values.remove("d");
        validator.validateList(prop, values, messages);
        assertTrue(messages.isEmpty());

        // includes
        prop.setIncludes(new String[] {"b"});
        validator.validateList(prop, values, messages);
        assertTrue(messages.isEmpty());

        prop.setIncludes(new String[] {"x"});
        validator.validateList(prop, values, messages);
        assertEquals(1, messages.size());
        messages.clear();

        values.add("x");
        values.remove("a");
        validator.validateList(prop, values, messages);
        assertTrue(messages.isEmpty());
    }
}
