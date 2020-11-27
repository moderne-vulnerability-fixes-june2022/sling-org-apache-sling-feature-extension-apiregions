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
        assertTrue(validator.validate(null, prop).getErrors().isEmpty());
        assertTrue(validator.validate(null, prop).isValid());

        // prop required - error
        prop.setRequired(true);
        assertEquals(1, validator.validate(null, prop).getErrors().size());
        assertFalse(validator.validate(null, prop).isValid());
    }

    @Test public void testValidateBoolean() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.BOOLEAN);

        PropertyValidationResult result;
        result = validator.validate(Boolean.TRUE, prop);
        assertTrue(result.isValid());

        result = validator.validate(Boolean.FALSE, prop);
        assertTrue(result.isValid());

        result = validator.validate("TRUE", prop);
        assertTrue(result.isValid());

        result = validator.validate("FALSE", prop);
        assertTrue(result.isValid());

        result = validator.validate("yes", prop);
        assertEquals(1, result.getErrors().size());

        result = validator.validate(1, prop);
        assertEquals(1, result.getErrors().size());
    }

    @Test public void testValidateByte() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.BYTE);

        PropertyValidationResult result;

        result = validator.validate((byte)1, prop);
        assertTrue(result.isValid());

        result = validator.validate("1", prop);
        assertTrue(result.isValid());

        result = validator.validate("yes", prop);
        assertEquals(1, result.getErrors().size());

        result = validator.validate(1, prop);
        assertTrue(result.isValid());
    }

    @Test public void testValidateShort() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.SHORT);

        PropertyValidationResult result;

        result = validator.validate((short)1, prop);
        assertTrue(result.isValid());

        result = validator.validate("1", prop);
        assertTrue(result.isValid());

        result = validator.validate("yes", prop);
        assertEquals(1, result.getErrors().size());

        result = validator.validate(1, prop);
        assertTrue(result.isValid());
    }

    @Test public void testValidateInteger() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.INTEGER);

        PropertyValidationResult result;

        result = validator.validate(1, prop);
        assertTrue(result.isValid());

        result = validator.validate("1", prop);
        assertTrue(result.isValid());

        result = validator.validate("yes", prop);
        assertEquals(1, result.getErrors().size());

        result = validator.validate(1, prop);
        assertTrue(result.isValid());
    }

    @Test public void testValidateLong() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.LONG);

        PropertyValidationResult result;

        result = validator.validate(1L, prop);
        assertTrue(result.isValid());

        result = validator.validate("1", prop);
        assertTrue(result.isValid());

        result = validator.validate("yes", prop);
        assertEquals(1, result.getErrors().size());

        result = validator.validate(1, prop);
        assertTrue(result.isValid());
    }

    @Test public void testValidateFloat() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.FLOAT);

        PropertyValidationResult result;

        result = validator.validate(1.1, prop);
        assertTrue(result.isValid());

        result = validator.validate("1.1", prop);
        assertTrue(result.isValid());

        result = validator.validate("yes", prop);
        assertEquals(1, result.getErrors().size());

        result = validator.validate(1, prop);
        assertTrue(result.isValid());
    }

    @Test public void testValidateDouble() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.DOUBLE);

        PropertyValidationResult result;

        result = validator.validate(1.1d, prop);
        assertTrue(result.isValid());

        result = validator.validate("1.1", prop);
        assertTrue(result.isValid());

        result = validator.validate("yes", prop);
        assertEquals(1, result.getErrors().size());

        result = validator.validate(1, prop);
        assertTrue(result.isValid());
    }

    @Test public void testValidateChar() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.CHARACTER);

        PropertyValidationResult result;

        result = validator.validate('x', prop);
        assertTrue(result.isValid());

        result = validator.validate("y", prop);
        assertTrue(result.isValid());

        result = validator.validate("yes", prop);
        assertEquals(1, result.getErrors().size());
    }

    @Test public void testValidateUrl() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.URL);

        PropertyValidationResult result;

        result = validator.validate("https://sling.apache.org/documentation", prop);
        assertTrue(result.isValid());

        result = validator.validate("hello world", prop);
        assertEquals(1, result.getErrors().size());
    }

    @Test public void testValidateEmail() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.EMAIL);

        PropertyValidationResult result;

        result = validator.validate("a@b.com", prop);
        assertTrue(result.isValid());

        result = validator.validate("hello world", prop);
        assertEquals(1, result.getErrors().size());
    }

    @Test public void testValidatePassword() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.PASSWORD);

        PropertyValidationResult result;

        result = validator.validate(null, prop);
        assertTrue(result.isValid());

        prop.setVariable("secret");
        result = validator.validate(null, prop);
        assertTrue(result.isValid());
    }

    @Test public void testValidatePath() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setType(PropertyType.PATH);

        PropertyValidationResult result;

        result = validator.validate("/a/b/c", prop);
        assertTrue(result.isValid());

        result = validator.validate("hello world", prop);
        assertEquals(1, result.getErrors().size());
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
        final PropertyDescription prop = new PropertyDescription();

        final List<Object> values = new ArrayList<>();
        values.add("a");
        values.add("b");
        values.add("c");

        // default cardinality - no excludes/includes
        PropertyValidationResult result;
        result = validator.validate(values, prop);
        assertEquals(1, result.getErrors().size());

        // cardinality 3 - no excludes/includes
        prop.setCardinality(3);
        result = validator.validate(values, prop);
        assertTrue(result.getErrors().isEmpty());

        values.add("d");
        result = validator.validate(values, prop);
        assertEquals(1, result.getErrors().size());

        // excludes
        prop.setExcludes(new String[] {"d", "e"});
        result = validator.validate(values, prop);
        assertEquals(2, result.getErrors().size()); // cardinality and exclude

        values.remove("d");
        result = validator.validate(values, prop);
        assertTrue(result.getErrors().isEmpty());

        // includes
        prop.setIncludes(new String[] {"b"});
        result = validator.validate(values, prop);
        assertTrue(result.getErrors().isEmpty());

        prop.setIncludes(new String[] {"x"});
        result = validator.validate(values, prop);
        assertEquals(1, result.getErrors().size());

        values.add("x");
        values.remove("a");
        result = validator.validate(values, prop);
        assertTrue(result.getErrors().isEmpty());
    }

    @Test public void testValidateArray() {
        final PropertyDescription prop = new PropertyDescription();

        String[] values = new String[] {"a", "b", "c"};

        // default cardinality - no excludes/includes
        PropertyValidationResult result;
        result = validator.validate(values, prop);
        assertEquals(1, result.getErrors().size());

        // cardinality 3 - no excludes/includes
        prop.setCardinality(3);
        result = validator.validate(values, prop);
        assertTrue(result.getErrors().isEmpty());

        values = new String[] {"a", "b", "c", "d"};
        result = validator.validate(values, prop);
        assertEquals(1, result.getErrors().size());

        // excludes
        prop.setExcludes(new String[] {"d", "e"});
        result = validator.validate(values, prop);
        assertEquals(2, result.getErrors().size()); // cardinality and exclude

        values = new String[] {"a", "b", "c"};
        result = validator.validate(values, prop);
        assertTrue(result.getErrors().isEmpty());

        // includes
        prop.setIncludes(new String[] {"b"});
        result = validator.validate(values, prop);
        assertTrue(result.getErrors().isEmpty());

        prop.setIncludes(new String[] {"x"});
        result = validator.validate(values, prop);
        assertEquals(1, result.getErrors().size());

        values = new String[] {"b", "c", "x"};
        result = validator.validate(values, prop);
        assertTrue(result.getErrors().isEmpty());
    }

    @Test public void testDeprecation() {
        final PropertyDescription prop = new PropertyDescription();
        prop.setDeprecated("This is deprecated");

        final PropertyValidationResult result = validator.validate("foo", prop);
        assertTrue(result.isValid());
        assertEquals(1, result.getWarnings().size());
        assertEquals("This is deprecated", result.getWarnings().get(0));
    }
}
