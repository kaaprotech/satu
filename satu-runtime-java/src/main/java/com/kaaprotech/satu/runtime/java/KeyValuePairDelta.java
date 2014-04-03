/*
 * Copyright 2014 Kaaprotech Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaaprotech.satu.runtime.java;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public final class KeyValuePairDelta<K, V> extends AbstractDelta implements Comparable<KeyValuePairDelta<K, V>>, Serializable {

    private static final long serialVersionUID = 1L;

    private final K key_;

    private final V value_;

    public KeyValuePairDelta(final DeltaType deltaType, final K key, final V value) {
        super(deltaType);
        key_ = key;
        value_ = value;
    }

    public K getKey() {
        return key_;
    }

    public V getValue() {
        return value_;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("deltaType", deltaType_)
                .append("key", key_)
                .append("value", value_)
                .toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof KeyValuePairDelta)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final KeyValuePairDelta rhs = (KeyValuePairDelta) obj;
        return new EqualsBuilder()
                .append(deltaType_, rhs.deltaType_)
                .append(key_, rhs.key_)
                .append(value_, rhs.value_)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(14389, 78143)
                .append(deltaType_).append(key_)
                .append(value_)
                .toHashCode();
    }

    @Override
    public int compareTo(final KeyValuePairDelta<K, V> rhs) {
        return new CompareToBuilder().append(getKey(), rhs.getKey()).toComparison();
    }

    public Builder<K, V> toDeltaBuilder() {
        return new Builder<K, V>(this);
    }

    public static final class Builder<K, V> extends AbstractDeltaBuilder<KeyValuePairDelta<K, V>> implements Comparable<Builder<K, V>> {

        private final K key_;

        private V value_;

        public Builder(final DeltaType deltaType, final K key) {
            super(deltaType);
            key_ = key;
        }

        public Builder(final KeyValuePairDelta<K, V> delta) {
            super(delta.getDeltaType());
            key_ = delta.getKey();
            value_ = delta.getValue();
        }

        @Override
        public Builder<K, V> addDelta(final KeyValuePairDelta<K, V> delta) {
            if (!getKey().equals(delta.getKey())) {
                throw new RuntimeException("Keys are not equal " + getKey() + ", " + delta.getKey());
            }

            setDeltaType(delta.getDeltaType());
            setValue(delta.getValue());

            if (delta.getDeltaType() != DeltaType.DELETE) {
                resetDeltaType();
            }

            return this;
        }

        @Override
        public Builder<K, V> setDeltaType(final DeltaType deltaType) {
            super.setDeltaType(deltaType);
            return this;
        }

        @Override
        public Builder<K, V> resetDeltaType() {
            super.resetDeltaType();
            return this;
        }

        public V getValue() {
            return value_;
        }

        public Builder<K, V> setValue(final V value) {
            value_ = value;
            return this;
        }

        public K getKey() {
            return key_;
        }

        public KeyValuePairDelta<K, V> buildDelta() {
            return new KeyValuePairDelta<K, V>(getDeltaType(), getKey(), getValue());
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("initialDeltaType", getInitialDeltaType())
                    .append("deltaType", deltaType_)
                    .append("key", key_)
                    .append("value", value_)
                    .toString();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Builder)) {
                return false;
            }
            @SuppressWarnings("rawtypes")
            final Builder rhs = (Builder) obj;
            return new EqualsBuilder()
                    .append(getInitialDeltaType(), rhs.getInitialDeltaType())
                    .append(deltaType_, rhs.deltaType_)
                    .append(key_, rhs.key_)
                    .append(value_, rhs.value_)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(45197, 940141)
                    .append(getInitialDeltaType())
                    .append(deltaType_)
                    .append(key_)
                    .append(value_)
                    .toHashCode();
        }

        @Override
        public int compareTo(final Builder<K, V> rhs) {
            return new CompareToBuilder()
                    .append(getKey(), rhs.getKey())
                    .toComparison();
        }
    }
}
