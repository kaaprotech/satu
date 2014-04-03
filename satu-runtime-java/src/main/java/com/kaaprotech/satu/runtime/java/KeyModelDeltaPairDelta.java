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

/**
 * Note the key doesn't need to be the model class key
 */
public final class KeyModelDeltaPairDelta<K, K2, D extends ModelDelta<K2, ? extends ModelBuilder<K2, ?, D>, DB>, DB extends ModelDeltaBuilder<K2, D>> extends AbstractDelta implements
        Comparable<KeyModelDeltaPairDelta<K, K2, D, DB>>, Serializable {

    private static final long serialVersionUID = 1L;

    private final K key_;

    private final D value_;

    public KeyModelDeltaPairDelta(final DeltaType deltaType, final K key, final D value) {
        super(deltaType);
        key_ = key;
        value_ = value;
    }

    public K getKey() {
        return key_;
    }

    public D getValue() {
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
        if (!(obj instanceof KeyModelDeltaPairDelta)) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final KeyModelDeltaPairDelta rhs = (KeyModelDeltaPairDelta) obj;
        return new EqualsBuilder()
                .append(deltaType_, rhs.deltaType_)
                .append(key_, rhs.key_)
                .append(value_, rhs.value_)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(72451, 89307)
                .append(deltaType_)
                .append(key_)
                .append(value_)
                .toHashCode();
    }

    @Override
    public int compareTo(final KeyModelDeltaPairDelta<K, K2, D, DB> rhs) {
        return new CompareToBuilder()
                .append(getKey(), rhs.getKey())
                .toComparison();
    }

    public Builder<K, K2, D, DB> toDeltaBuilder() {
        return new Builder<K, K2, D, DB>(this);
    }

    public static final class Builder<K, K2, D extends ModelDelta<K2, ? extends ModelBuilder<K2, ?, D>, DB>, DB extends ModelDeltaBuilder<K2, D>> extends
            AbstractDeltaBuilder<KeyModelDeltaPairDelta<K, K2, D, DB>> implements Comparable<Builder<K, K2, D, DB>> {

        private final K key_;

        private DB value_;

        public Builder(final KeyModelDeltaPairDelta<K, K2, D, DB> delta) {
            super(delta.getDeltaType());
            key_ = delta.getKey();
            value_ = delta.getValue().toDeltaBuilder();
        }

        @Override
        public Builder<K, K2, D, DB> addDelta(final KeyModelDeltaPairDelta<K, K2, D, DB> delta) {
            if (!getKey().equals(delta.getKey())) {
                throw new RuntimeException("Keys are not equal " + getKey() + ", " + delta.getKey());
            }

            setDeltaType(delta.getDeltaType());

            if (delta.getDeltaType() != DeltaType.DELETE) {
                resetDeltaType();
            }

            if (delta.getDeltaType() == DeltaType.DELETE || !value_.getKey().equals(delta.getValue().getKey())) {
                value_ = delta.getValue().toDeltaBuilder();
            }
            else {
                value_.addDelta(delta.getValue());
            }

            return this;
        }

        @Override
        public Builder<K, K2, D, DB> setDeltaType(final DeltaType deltaType) {
            super.setDeltaType(deltaType);
            return this;
        }

        @Override
        public Builder<K, K2, D, DB> resetDeltaType() {
            super.resetDeltaType();
            return this;
        }

        public DB getValue() {
            return value_;
        }

        public K getKey() {
            return key_;
        }

        @SuppressWarnings("cast")
        public KeyModelDeltaPairDelta<K, K2, D, DB> buildDelta() {
            D delta = (D) (value_ == null ? null : value_.buildDelta());
            return new KeyModelDeltaPairDelta<K, K2, D, DB>(getDeltaType(), key_, delta);
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
            return new HashCodeBuilder(12169, 87027)
                    .append(getInitialDeltaType())
                    .append(deltaType_)
                    .append(key_)
                    .append(value_)
                    .toHashCode();
        }

        @Override
        public int compareTo(final Builder<K, K2, D, DB> rhs) {
            return new CompareToBuilder()
                    .append(getKey(), rhs.getKey())
                    .toComparison();
        }
    }
}
