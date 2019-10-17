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

import org.apache.sling.feature.ArtifactId;

/**
 * Describes an exported package.
 */
public class ApiExport {

    private volatile String name;

    private volatile String toggle;

    private volatile ArtifactId previous;

    public ApiExport() {
        // default
    }

    public ApiExport(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToggle() {
        return toggle;
    }

    public void setToggle(String toggle) {
        this.toggle = toggle;
    }

    public ArtifactId getPrevious() {
        return previous;
    }

    public void setPrevious(ArtifactId previous) {
        this.previous = previous;
    }

    @Override
    public String toString() {
        return "ApiExport [name=" + name + ", toggle=" + toggle + ", previous=" + previous + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((previous == null) ? 0 : previous.hashCode());
        result = prime * result + ((toggle == null) ? 0 : toggle.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApiExport other = (ApiExport) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (previous == null) {
            if (other.previous != null)
                return false;
        } else if (!previous.equals(other.previous))
            return false;
        if (toggle == null) {
            if (other.toggle != null)
                return false;
        } else if (!toggle.equals(other.toggle))
            return false;
        return true;
    }
}
