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
package org.apache.sling.feature.extension.apiregions.api.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ValidatorTest {
    
    @Test public void testValidateValueWithNull() {
        final List<String> messages = new ArrayList<>();
        Validator.validateValue(null, null, messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidateBoolean() {
        final Property prop = new Property();
        prop.setType(PropertyType.BOOLEAN);

        final List<String> messages = new ArrayList<>();

        Validator.validateBoolean(prop, Boolean.TRUE, messages);
        assertTrue(messages.isEmpty());

        Validator.validateBoolean(prop, Boolean.FALSE, messages);
        assertTrue(messages.isEmpty());

        Validator.validateBoolean(prop, "TRUE", messages);
        assertTrue(messages.isEmpty());

        Validator.validateBoolean(prop, "FALSE", messages);
        assertTrue(messages.isEmpty());

        Validator.validateBoolean(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        Validator.validateBoolean(prop, 1, messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidateByte() {
        final Property prop = new Property();
        prop.setType(PropertyType.BYTE);

        final List<String> messages = new ArrayList<>();

        Validator.validateByte(prop, (byte)1, messages);
        assertTrue(messages.isEmpty());

        Validator.validateByte(prop, "1", messages);
        assertTrue(messages.isEmpty());

        Validator.validateByte(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        Validator.validateByte(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateShort() {
        final Property prop = new Property();
        prop.setType(PropertyType.SHORT);

        final List<String> messages = new ArrayList<>();

        Validator.validateShort(prop, (short)1, messages);
        assertTrue(messages.isEmpty());

        Validator.validateShort(prop, "1", messages);
        assertTrue(messages.isEmpty());

        Validator.validateShort(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        Validator.validateShort(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateInteger() {
        final Property prop = new Property();
        prop.setType(PropertyType.INTEGER);

        final List<String> messages = new ArrayList<>();

        Validator.validateInteger(prop, 1, messages);
        assertTrue(messages.isEmpty());

        Validator.validateInteger(prop, "1", messages);
        assertTrue(messages.isEmpty());

        Validator.validateInteger(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        Validator.validateInteger(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateLong() {
        final Property prop = new Property();
        prop.setType(PropertyType.LONG);

        final List<String> messages = new ArrayList<>();

        Validator.validateLong(prop, 1L, messages);
        assertTrue(messages.isEmpty());

        Validator.validateLong(prop, "1", messages);
        assertTrue(messages.isEmpty());

        Validator.validateLong(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        Validator.validateLong(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateFloat() {
        final Property prop = new Property();
        prop.setType(PropertyType.FLOAT);

        final List<String> messages = new ArrayList<>();

        Validator.validateFloat(prop, 1.1, messages);
        assertTrue(messages.isEmpty());

        Validator.validateFloat(prop, "1.1", messages);
        assertTrue(messages.isEmpty());

        Validator.validateFloat(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        Validator.validateFloat(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateDouble() {
        final Property prop = new Property();
        prop.setType(PropertyType.DOUBLE);

        final List<String> messages = new ArrayList<>();

        Validator.validateDouble(prop, 1.1d, messages);
        assertTrue(messages.isEmpty());

        Validator.validateDouble(prop, "1.1", messages);
        assertTrue(messages.isEmpty());

        Validator.validateDouble(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();

        Validator.validateDouble(prop, 1, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateChar() {
        final Property prop = new Property();
        prop.setType(PropertyType.CHARACTER);

        final List<String> messages = new ArrayList<>();

        Validator.validateCharacter(prop, 'x', messages);
        assertTrue(messages.isEmpty());

        Validator.validateCharacter(prop, "y", messages);
        assertTrue(messages.isEmpty());

        Validator.validateCharacter(prop, "yes", messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidateUrl() {
        final List<String> messages = new ArrayList<>();

        Validator.validateURL(null, "https://sling.apache.org/documentation", messages);
        assertTrue(messages.isEmpty());

        Validator.validateURL(null, "hello world", messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidateEmail() {
        final List<String> messages = new ArrayList<>();

        Validator.validateEmail(null, "a@b.com", messages);
        assertTrue(messages.isEmpty());

        Validator.validateEmail(null, "hello world", messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidatePassword() {
        final Property prop = new Property();
        final List<String> messages = new ArrayList<>();

        Validator.validatePassword(prop, null, messages);
        assertEquals(1, messages.size());
        messages.clear();

        prop.setVariable("secret");
        Validator.validatePassword(prop, null, messages);
        assertTrue(messages.isEmpty());
    }

    @Test public void testValidateRange() {
        final List<String> messages = new ArrayList<>();
        final Property prop = new Property();

        // no range set
        Validator.validateRange(prop, 2, messages);
        assertTrue(messages.isEmpty());

        // empty range set
        prop.setRange(new Range());
        Validator.validateRange(prop, 2, messages);
        assertTrue(messages.isEmpty());

        // min set
        prop.getRange().setMin(5);
        Validator.validateRange(prop, 5, messages);
        assertTrue(messages.isEmpty());
        Validator.validateRange(prop, 6, messages);
        assertTrue(messages.isEmpty());
        Validator.validateRange(prop, 4, messages);
        assertEquals(1, messages.size());
        messages.clear();

        Validator.validateRange(prop, 5.0, messages);
        assertTrue(messages.isEmpty());
        Validator.validateRange(prop, 6.0, messages);
        assertTrue(messages.isEmpty());
        Validator.validateRange(prop, 4.0, messages);
        assertEquals(1, messages.size());
        messages.clear();

        // max set
        prop.getRange().setMax(6);
        Validator.validateRange(prop, 5, messages);
        assertTrue(messages.isEmpty());
        Validator.validateRange(prop, 6, messages);
        assertTrue(messages.isEmpty());
        Validator.validateRange(prop, 7, messages);
        assertEquals(1, messages.size());
        messages.clear();

        Validator.validateRange(prop, 5.0, messages);
        assertTrue(messages.isEmpty());
        Validator.validateRange(prop, 6.0, messages);
        assertTrue(messages.isEmpty());
        Validator.validateRange(prop, 7.0, messages);
        assertEquals(1, messages.size());
        messages.clear();
    }   
    
    @Test public void testValidateRegex() {
        final List<String> messages = new ArrayList<>();
        final Property prop = new Property();

        // no regex
        Validator.validateRegex(prop, "hello world", messages);
        Validator.validateRegex(prop, "world", messages);
        assertTrue(messages.isEmpty());

        // regex
        prop.setRegex("h(.*)");
        Validator.validateRegex(prop, "hello world", messages);
        assertTrue(messages.isEmpty());
        Validator.validateRegex(prop, "world", messages);
        assertEquals(1, messages.size());
        messages.clear();
    }

    @Test public void testValidateOptions() {
        final List<String> messages = new ArrayList<>();
        final Property prop = new Property();

        // no options
        Validator.validateOptions(prop, "foo", messages);
        Validator.validateOptions(prop, "bar", messages);
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
        Validator.validateOptions(prop, "foo", messages);
        assertTrue(messages.isEmpty());
        Validator.validateOptions(prop, "bar", messages);
        assertEquals(1, messages.size());
        messages.clear();
        Validator.validateOptions(prop, 7, messages);
        assertTrue(messages.isEmpty());
    }
    
    @Test public void testValidateList() {
        final List<String> messages = new ArrayList<>();
        final Property prop = new Property();

        final List<Object> values = new ArrayList<>();
        values.add("a");
        values.add("b");
        values.add("c");

        // default cardinality - no excludes/includes
        Validator.validateList(prop, values, messages);
        assertEquals(1, messages.size());
        messages.clear();

        // cardinality 3 - no excludes/includes
        prop.setCardinality(3);
        Validator.validateList(prop, values, messages);
        assertTrue(messages.isEmpty());

        values.add("d");
        Validator.validateList(prop, values, messages);
        assertEquals(1, messages.size());
        messages.clear();

        // excludes
        prop.setExcludes(new String[] {"d", "e"});
        Validator.validateList(prop, values, messages);
        assertEquals(2, messages.size()); // cardinality and exclude
        messages.clear();

        values.remove("d");
        Validator.validateList(prop, values, messages);
        assertTrue(messages.isEmpty());

        // includes
        prop.setIncludes(new String[] {"b"});
        Validator.validateList(prop, values, messages);
        assertTrue(messages.isEmpty());

        prop.setIncludes(new String[] {"x"});
        Validator.validateList(prop, values, messages);
        assertEquals(1, messages.size());
        messages.clear();

        values.add("x");
        values.remove("a");
        Validator.validateList(prop, values, messages);
        assertTrue(messages.isEmpty());
    }
}
