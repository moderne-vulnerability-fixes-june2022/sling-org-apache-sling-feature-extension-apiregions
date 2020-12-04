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

import java.util.Objects;

/**
 * Deprecation info for a package or member.
 * This class is not thread safe.
 * @since 1.1
 */
public class DeprecationInfo {

    private final String message;

    private String since;

    /**
     * Create a new info
     * @param msg The msg
     * @throws IllegalArgumentException if msg is {@code null}
     */
    public DeprecationInfo(final String msg) {
        if ( msg == null ) {
            throw new IllegalArgumentException();
        }
        this.message = msg;
    }

    /**
     * Get the message
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the optional since information
     * @return The since information or {@code null}
     */
    public String getSince() {
        return since;
    }

    /**
     * Set the since information
     * @param since The new info
     */
    public void setSince(final String since) {
        this.since = since;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, since);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeprecationInfo other = (DeprecationInfo) obj;
        return Objects.equals(message, other.message) && Objects.equals(since, other.since);
    }
}
