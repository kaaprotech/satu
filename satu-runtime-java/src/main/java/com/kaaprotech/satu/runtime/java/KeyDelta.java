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

public final class KeyDelta<K> extends AbstractDelta implements Comparable<KeyDelta<K>>, Serializable {

    private static final long serialVersionUID = 1L;

    private final K key_;

    public KeyDelta(final DeltaType deltaType, final K key) {
        super(deltaType);
        key_ = key;
    }

    public K getKey() {
        return key_;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof KeyDelta)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final KeyDelta rhs = (KeyDelta) obj;
        return new EqualsBuilder()
                .append(deltaType_, rhs.deltaType_)
                .append(key_, rhs.key_)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(98715, 80571)
                .append(deltaType_)
                .append(key_)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("deltaType", deltaType_)
                .append("key", key_)
                .toString();
    }

    @Override
    public int compareTo(final KeyDelta<K> rhs) {
        return new CompareToBuilder()
                .append(getKey(), rhs.getKey())
                .toComparison();
    }

    public Builder<K> toDeltaBuilder() {
        return new Builder<K>(this);
    }

    public static final class Builder<K> extends AbstractDeltaBuilder<KeyDelta<K>> implements Comparable<Builder<K>> {

        private final K key_;

        public Builder(final DeltaType deltaType, final K key) {
            super(deltaType);
            key_ = key;
        }

        public Builder(final KeyDelta<K> delta) {
            super(delta.getDeltaType());
            key_ = delta.getKey();
        }

        @Override
        public Builder<K> addDelta(final KeyDelta<K> delta) {
            if (!getKey().equals(delta.getKey())) {
                throw new RuntimeException("Keys are not equal " + getKey() + ", " + delta.getKey());
            }

            setDeltaType(delta.getDeltaType());

            if (delta.getDeltaType() != DeltaType.DELETE) {
                resetDeltaType();
            }
            return this;
        }

        @Override
        public Builder<K> setDeltaType(final DeltaType delta) {
            super.setDeltaType(delta);
            return this;
        }

        @Override
        public Builder<K> resetDeltaType() {
            super.resetDeltaType();
            return this;
        }

        public K getKey() {
            return key_;
        }

        public KeyDelta<K> buildDelta() {
            return new KeyDelta<K>(getDeltaType(), getKey());
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("initialDeltaType", getInitialDeltaType())
                    .append("deltaType", deltaType_)
                    .append("key", key_)
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
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(78125, 20379)
                    .append(getInitialDeltaType())
                    .append(deltaType_)
                    .append(key_)
                    .toHashCode();
        }

        @Override
        public int compareTo(final Builder<K> rhs) {
            return new CompareToBuilder()
                    .append(getKey(), rhs.getKey())
                    .toComparison();
        }
    }
}
