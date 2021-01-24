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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class CaseInsensitiveMap<T> implements Map<String, T> {
    
    private final Map<CaseInsensitiveKey, T> map = new LinkedHashMap<>();

	@Override
	public void clear() {
		this.map.clear();		
	}

	@Override
	public boolean containsKey(final Object key) {
		return this.map.containsKey(new CaseInsensitiveKey(key.toString()));
	}

	@Override
	public boolean containsValue(final Object value) {
		return this.map.containsValue(value);
	}

	@Override
	public Set<Entry<String, T>> entrySet() {
		return new EntrySet();
	}

	@Override
	public T get(final Object key) {
        final CaseInsensitiveKey k = new CaseInsensitiveKey(key.toString());
        return this.map.get(k);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return new KeySet();
	}

	@Override
	public T put(final String key, final T value) {
        final CaseInsensitiveKey k = new CaseInsensitiveKey(key.toString());
        final T old = this.map.remove(k);
        this.map.put(k, value);
        return old;
	}

	@Override
	public void putAll(final Map<? extends String, ? extends T> m) {
		for(final Map.Entry<? extends String, ? extends T> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }		
	}

	@Override
	public T remove(final Object key) {
        final CaseInsensitiveKey k = new CaseInsensitiveKey(key.toString());
        return this.map.remove(k);
	}

	@Override
	public int size() {
		return this.map.size();
	}

	@Override
	public Collection<T> values() {
		return this.map.values();
    } 
    
    private static final class CaseInsensitiveKey {

        private final String value;

        private final int hashCode;

        public CaseInsensitiveKey(final String v) {
            this.value = v;
            this.hashCode = v.toUpperCase().hashCode();
        }

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */		
		@Override
		public int hashCode() {
			return this.hashCode;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
                return true;
            }
			if (!(obj instanceof CaseInsensitiveKey)) {
				return false;
            }
            final CaseInsensitiveKey other = (CaseInsensitiveKey) obj;
            if ( value == null ) {
                if ( other.value == null ) {
                    return true;
                }
                return false;
            }
            if ( other.value == null ) {
                return false;
            }
            return value.equalsIgnoreCase(other.value);
		}
    }

    private final class KeySet extends AbstractSet<String> {

		@Override
		public int size() {
			return CaseInsensitiveMap.this.size();
		}

		@Override
		public boolean isEmpty() {
			return CaseInsensitiveMap.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return CaseInsensitiveMap.this.containsKey(o);
		}

		@Override
		public Iterator<String> iterator() {
			return new KeyIterator(CaseInsensitiveMap.this.map.keySet());
		}

		@Override
		public boolean remove(Object o) {
			return CaseInsensitiveMap.this.remove(o) != null;
		}

		@Override
		public void clear() {
			CaseInsensitiveMap.this.clear();
		}
	}

	private static final class KeyIterator implements Iterator<String> {
		private final Iterator<CaseInsensitiveKey> i;

		KeyIterator(final Collection<CaseInsensitiveKey> c) {
			this.i = c.iterator();
		}

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		@Override
		public String next() {
			final CaseInsensitiveKey k = i.next();
			return k.value;
		}

		@Override
		public void remove() {
			i.remove();
		}
	}

	private final class EntrySet extends AbstractSet<Entry<String, T>> {

		@Override
		public int size() {
			return CaseInsensitiveMap.this.size();
		}

		@Override
		public boolean isEmpty() {
			return CaseInsensitiveMap.this.isEmpty();
		}

		@Override
		public Iterator<Entry<String, T>> iterator() {
			return new EntryIterator<>(CaseInsensitiveMap.this.map.entrySet());
		}

		@Override
		public void clear() {
			CaseInsensitiveMap.this.clear();
		}
	}

	private static final class EntryIterator<T> implements Iterator<Entry<String, T>> {
		private final Iterator<Entry<CaseInsensitiveKey, T>> i;

		EntryIterator(final Collection<Entry<CaseInsensitiveKey, T>> c) {
			this.i = c.iterator();
		}

		@Override
		public boolean hasNext() {
			return i.hasNext();
		}

		@Override
		public Entry<String, T> next() {
			return new CaseInsentiveEntry<>(i.next());
		}

		@Override
		public void remove() {
			i.remove();
		}
	}

	private static final class CaseInsentiveEntry<T> implements Entry<String, T> {
		private final Entry<CaseInsensitiveKey, T> entry;

		CaseInsentiveEntry(final Entry<CaseInsensitiveKey, T> entry) {
			this.entry = entry;
		}

		@Override
		public String getKey() {
			return entry.getKey().value;
		}

		@Override
		public T getValue() {
			return entry.getValue();
		}

		@Override
		public T setValue(final T value) {
			return entry.setValue(value);
		}

		@Override
		public int hashCode() {
            return entry.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof CaseInsentiveEntry) {
                final CaseInsentiveEntry<?> other = (CaseInsentiveEntry<?>) obj;
                return Objects.equals(other.entry.getKey(), this.entry.getKey()) && Objects.equals(other.entry.getValue(), this.entry.getValue());
			}
			return false;
		}
	}
}
