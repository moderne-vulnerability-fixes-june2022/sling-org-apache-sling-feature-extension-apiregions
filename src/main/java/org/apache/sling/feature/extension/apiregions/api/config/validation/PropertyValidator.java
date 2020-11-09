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

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.sling.feature.extension.apiregions.api.config.Option;
import org.apache.sling.feature.extension.apiregions.api.config.PropertyDescription;

/**
 * Validate values
 */
public class PropertyValidator {
    
	/**
	 * Validate the value against the property definition
	 * @return A property validation result
	 */
	public PropertyValidationResult validate(final PropertyDescription prop, final Object value) {
		final PropertyValidationResult result = new PropertyValidationResult();
		if ( value == null ) {
            if ( prop.isRequired() ) {
                result.getErrors().add("No value provided");
            }
		} else {
			final List<Object> values;
			if ( value.getClass().isArray() ) {
				// array
				values = new ArrayList<>();
                for(int i=0;i<Array.getLength(value);i++) {
					values.add(Array.get(value, i));
				}
			} else if ( value instanceof Collection ) { 
				// collection
				values = new ArrayList<>();
				final Collection<?> c = (Collection<?>)value;
				for(final Object o : c) {
					values.add(o);
				}
			} else {
				// single value
				values = null;
				validateValue(prop, value, result.getErrors());
			}

			if ( values != null ) {
                // array or collection
                for(final Object val : values) {
                    validateValue(prop, val, result.getErrors());
                }
                validateList(prop, values, result.getErrors());
			}
		}
		return result;
	}

    void validateList(final PropertyDescription prop, final List<Object> values, final List<String> messages) {
        if ( prop.getCardinality() > 0 && values.size() > prop.getCardinality() ) {
            messages.add("Array/collection contains too many elements, only " + prop.getCardinality() + " allowed");
        }
        if ( prop.getIncludes() != null ) {
            for(final String inc : prop.getIncludes()) {
                boolean found = false;
                for(final Object val : values) {
                    if ( inc.equals(val.toString())) {
                        found = true;
                        break;
                    }
                }
                if ( !found ) {
                    messages.add("Required included value " + inc + " not found");
                }
            }
        }
        if ( prop.getExcludes() != null ) {
            for(final String exc : prop.getExcludes()) {
                boolean found = false;
                for(final Object val : values) {
                    if ( exc.equals(val.toString())) {
                        found = true;
                        break;
                    }
                }
                if ( found ) {
                    messages.add("Required excluded value " + exc + " found");
                }
            }
        }
    }

	void validateValue(final PropertyDescription prop, final Object value, final List<String> messages) {
		if ( value != null ) {
			switch ( prop.getType() ) {
				case BOOLEAN : validateBoolean(prop, value, messages);
							   break;
				case BYTE : validateByte(prop, value, messages);
							break;
				case CHARACTER : validateCharacter(prop, value, messages);
							break;
				case DOUBLE : validateDouble(prop, value, messages); 
							break;
				case FLOAT : validateFloat(prop, value, messages); 
							break;
				case INTEGER : validateInteger(prop, value, messages);
							break;
				case LONG : validateLong(prop, value, messages);
							break;
				case SHORT : validateShort(prop, value, messages);
							break;
				case STRING : // no special validation for string
							break;
				case EMAIL : validateEmail(prop, value, messages); 
							break;
				case PASSWORD : validatePassword(prop, value, messages);
							break;
				case URL : validateURL(prop, value, messages);
							break;
				default : messages.add("Unable to validate value - unknown property type : " + prop.getType());
            }
            validateRegex(prop, value, messages);
            validateOptions(prop, value, messages);
		} else {
			messages.add("Null value provided for validation");
		}
	}
	
	void validateBoolean(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( ! (value instanceof Boolean) ) {
			if ( value instanceof String ) {
				final String v = (String)value;
				if ( ! v.equalsIgnoreCase("true") && !v.equalsIgnoreCase("false") ) {
					messages.add("Boolean value must either be true or false, but not " + value);
				}
			} else {
				messages.add("Boolean value must either be of type Boolean or String : " + value);
			}
		}
	}

	void validateByte(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( ! (value instanceof Byte) ) {
			if ( value instanceof String ) {
				final String v = (String)value;
				try {
					validateRange(prop, Byte.valueOf(v), messages);
				} catch ( final NumberFormatException nfe ) {
                    messages.add("Value is not a valid Byte : " + value);
                }
            } else if ( value instanceof Number ) {
                validateRange(prop, ((Number)value).byteValue(), messages);            
			} else {
				messages.add("Byte value must either be of type Byte or String : " + value);
			}
		} else {
			validateRange(prop, (Byte)value, messages);
		}
	}

	void validateShort(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( ! (value instanceof Short) ) {
			if ( value instanceof String ) {
				final String v = (String)value;
				try {
					validateRange(prop, Short.valueOf(v), messages);
				} catch ( final NumberFormatException nfe ) {
                    messages.add("Value is not a valid Short : " + value);
				}
            } else if ( value instanceof Number ) {
                validateRange(prop, ((Number)value).shortValue(), messages);            
			} else {
				messages.add("Short value must either be of type Short or String : " + value);
			}
		} else {
			validateRange(prop, (Short)value, messages);
		}
	}

	void validateInteger(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( ! (value instanceof Integer) ) {
			if ( value instanceof String ) {
				final String v = (String)value;
				try {
					validateRange(prop, Integer.valueOf(v), messages);
				} catch ( final NumberFormatException nfe ) {
                    messages.add("Value is not a valid Integer : " + value);
				}
            } else if ( value instanceof Number ) {
                validateRange(prop, ((Number)value).intValue(), messages);            
			} else {
				messages.add("Integer value must either be of type Integer or String : " + value);
			}
		} else {
			validateRange(prop, (Integer)value, messages);
		}
	}

	void validateLong(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( ! (value instanceof Long) ) {
			if ( value instanceof String ) {
				final String v = (String)value;
				try {
					validateRange(prop, Long.valueOf(v), messages);
				} catch ( final NumberFormatException nfe ) {
                    messages.add("Value is not a valid Long : " + value);
				}
            } else if ( value instanceof Number ) {
                validateRange(prop, ((Number)value).longValue(), messages);            
			} else {
				messages.add("Long value must either be of type Long or String : " + value);
			}
		} else {
			validateRange(prop, (Long)value, messages);
		}
	}

	void validateFloat(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( ! (value instanceof Float) ) {
			if ( value instanceof String ) {
				final String v = (String)value;
				try {
					validateRange(prop, Float.valueOf(v), messages);
				} catch ( final NumberFormatException nfe ) {
                    messages.add("Value is not a valid Float : " + value);
				}
            } else if ( value instanceof Number ) {
                validateRange(prop, ((Number)value).floatValue(), messages);            
			} else {
				messages.add("Float value must either be of type Float or String : " + value);
			}
		} else {
			validateRange(prop, (Float)value, messages);
		}
	}

	void validateDouble(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( ! (value instanceof Double) ) {
			if ( value instanceof String ) {
				final String v = (String)value;
				try {
					validateRange(prop, Double.valueOf(v), messages);
				} catch ( final NumberFormatException nfe ) {
                    messages.add("Value is not a valid Double : " + value);
				}
            } else if ( value instanceof Number ) {
                validateRange(prop, ((Number)value).doubleValue(), messages);            
			} else {
				messages.add("Double value must either be of type Double or String : " + value);
			}
		} else {
			validateRange(prop, (Double)value, messages);
		}
	}

	void validateCharacter(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( ! (value instanceof Character) ) {
			if ( value instanceof String ) {
				final String v = (String)value;
				if ( v.length() > 1 ) {
                    messages.add("Value is not a valid Character : " + value);
				}
			} else {
				messages.add("Character value must either be of type Character or String : " + value);
			}
		}
	}

	void validateURL(final PropertyDescription prop, final Object value, final List<String> messages) {
		final String val = value.toString();
		try {
			new URL(val);
		} catch ( final MalformedURLException mue) {
			messages.add("Value is not a valid URL : " + val);
		}
	}

	void validateEmail(final PropertyDescription prop, final Object value, final List<String> messages) {
		final String val = value.toString();
		// poor man's validation (should probably use InternetAddress)
		if ( !val.contains("@") ) {
			messages.add("Not a valid email address " + val);
		}
	}

	void validatePassword(final PropertyDescription prop, final Object value, final List<String> messages) {
		if ( prop.getVariable() == null ) {
			messages.add("Value for a password must use a variable");
		}
	}

	void validateRange(final PropertyDescription prop, final Number value, final List<String> messages) {
	    if ( prop.getRange() != null ) {
            if ( prop.getRange().getMin() != null ) {
                if ( value instanceof Float || value instanceof Double ) {
                    final double min = prop.getRange().getMin().doubleValue();
                    if ( value.doubleValue() < min ) {
                            messages.add("Value " + value + " is too low; should not be lower than " + prop.getRange().getMin());
                    }    
                } else {
                    final long min = prop.getRange().getMin().longValue();
                    if ( value.longValue() < min ) {
                            messages.add("Value " + value + " is too low; should not be lower than " + prop.getRange().getMin());
                    }    
                }
            }
            if ( prop.getRange().getMax() != null ) {
                if ( value instanceof Float || value instanceof Double ) {
                    final double max = prop.getRange().getMax().doubleValue();
                    if ( value.doubleValue() > max ) {
                        messages.add("Value " + value + " is too high; should not be higher than " + prop.getRange().getMax());
                    }    
                } else {
                    final long max = prop.getRange().getMax().longValue();
                    if ( value.longValue() > max ) {
                        messages.add("Value " + value + " is too high; should not be higher than " + prop.getRange().getMax());
                    }    
                }
            }
		}
	}

    void validateRegex(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( prop.getRegexPattern() != null ) {
            if ( !prop.getRegexPattern().matcher(value.toString()).matches() ) {
                messages.add("Value " + value + " does not match regex " + prop.getRegex());
            }
        }
    }

    void validateOptions(final PropertyDescription prop, final Object value, final List<String> messages) {
        if ( prop.getOptions() != null ) {
            boolean found = false;
            for(final Option opt : prop.getOptions()) {
                if ( opt.getValue().equals(value.toString() ) ) {
                    found = true; 
                }
            }
            if ( !found ) {
                messages.add("Value " + value + " does not match provided options");
            }
        }
    }
}