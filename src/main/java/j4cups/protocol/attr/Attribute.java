/*
 * Copyright (c) 2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 11.02.2018 by Oli B. (boehm@javatux.de)
 */
package j4cups.protocol.attr;

import j4cups.protocol.tags.ValueTags;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * An "attribute" field is encoded as attribute-with-one-value and (optional)
 * several addtional-values.
 * <pre>
 * -----------------------------------------------
 * |          attribute-with-one-value           |  q bytes
 * ----------------------------------------------------------
 * |             additional-value                |  r bytes |- 0 or more
 * ----------------------------------------------------------
 * </pre>
 * <p>
 * When an attribute is single valued (e.g. "copies" with value of 10)
 * or multi-valued with one value (e.g. "sides-supported" with just the
 * value 'one-sided') it is encoded with just an "attribute-with-one-
 * value" field. When an attribute is multi-valued with n values (e.g.
 * "sides-supported" with the values 'one-sided' and 'two-sided-long-
 * edge'), it is encoded with an "attribute-with-one-value" field
 * followed by n-1 "additional-value" fields.
 * </p>
 *
 * @author Oli B.
 * @since 0.0.2 (11.02.2018)
 */
public class Attribute {
    
    private final AttributeWithOneValue value;
    private final List<AdditionalValue> additionalValues;

    /**
     * Instantiates a new (single valued or multi-valued) attribute from the
     * given bytes. The given {@link ByteBuffer} must be positioned at the 
     * beginning of the attribute.
     *
     * @param bytes ByteBuffer positioned at the beginning
     */
    public Attribute(ByteBuffer bytes) {
        value = new AttributeWithOneValue(bytes);
        this.additionalValues = readAdditionalValues(bytes);
    }

    /**
     * The "value-tag" field specifies the attribute syntax, e.g. 0x44
     * for the attribute syntax 'keyword'.
     *
     * @return e.g. {@link ValueTags#KEYWORD}
     */
    public ValueTags getValueTag() {
        return value.getValueTag();
    }

    /**
     * The "name" field contains the textual name of the attribute.
     *
     * @return e.g. "slides-supported"
     */
    public String getName() {
        return value.getName();
    }

    /**
     * The "value" field contains the value of the attribute.
     * If the attribute is a multi-value attribute this returns only the
     * fist value.
     *
     * @return e.g. the textual value 'one-sided' as bytes
     */
    public byte[] getValue() {
        return value.getValue();
    }

    /**
     * The "value" field contains the value of the attribute.
     *
     * @return e.g. "one-sided"
     */
    public String getStringValue() {
        return new String(getValue(), StandardCharsets.UTF_8);
    }

    private static List<AdditionalValue> readAdditionalValues(ByteBuffer buffer) {
        List<AdditionalValue> values = new ArrayList<>();
        while (buffer.remaining() > 4) {
            int pos = buffer.position();
            ValueTags valueTag = ValueTags.of(buffer.get());
            short len = buffer.getShort();
            if (len != 0) {
                break;
            }
            buffer.position(pos);
            AdditionalValue val = new AdditionalValue(buffer);
            values.add(new AdditionalValue(buffer));
        }
        return values;
    }



    /**
     * An "attribute-with-one-value" field is encoded with five subfields.
     * <pre></pre>
     * -----------------------------------------------
     * |                   value-tag                 |   1 byte
     * -----------------------------------------------
     * |               name-length  (value is u)     |   2 bytes
     * -----------------------------------------------
     * |                     name                    |   u bytes
     * -----------------------------------------------
     * |              value-length  (value is v)     |   2 bytes
     * -----------------------------------------------
     * |                     value                   |   v bytes
     * -----------------------------------------------
     * </pre>
     * <p>
     * The "value-tag" field specifies the attribute syntax, e.g. 0x44
     * for the attribute syntax 'keyword'.
     * </p>
     * <p>
     * The "name-length" field specifies the length of the "name" field
     * in bytes, e.g. u in the above diagram or 15 for the name "sides-
     * supported".
     * </p>
     * <p>
     * The "name" field contains the textual name of the attribute, e.g.
     * "sides-supported".
     * </p>
     * <p>
     * The "value-length" field specifies the length of the "value" field
     * in bytes, e.g. v in the above diagram or 9 for the (keyword) value
     * 'one-sided'.
     * </p>
     * <p>
     * The "value" field contains the value of the attribute, e.g. the
     * textual value 'one-sided'.
     * </p>
     */
    public static class AttributeWithOneValue {

        private final ValueTags valueTag;
        private final String name;
        private final byte[] value;

        /**
         * Instantiates a new attribute-with-one-value from the given bytes.
         * The given {@link ByteBuffer} must be positioned at the beginning of the
         * attribute-group.
         *
         * @param bytes ByteBuffer positioned at the beginning
         */
        public AttributeWithOneValue(ByteBuffer bytes) {
            this.valueTag = ValueTags.of(bytes.get());
            short nameLength = bytes.getShort();
            this.name = readString(bytes, nameLength);
            short valueLength = bytes.getShort();
            this.value = readBytes(bytes, valueLength);
        }

        private static String readString(ByteBuffer buffer, short length) {
            byte[] bytes = readBytes(buffer, length);
            return new String(bytes, StandardCharsets.UTF_8);
        }

        private static byte[] readBytes(ByteBuffer buffer, short length) {
            byte[] value = new byte[length];
            for (int i = 0; i < length; i++) {
                value[i] = buffer.get();
            }
            return value;
        }

        /**
         * The "value-tag" field specifies the attribute syntax, e.g. 0x44
         * for the attribute syntax 'keyword'.
         *
         * @return e.g. {@link ValueTags#KEYWORD}
         */
        public ValueTags getValueTag() {
            return valueTag;
        }

        /**
         * The "name" field contains the textual name of the attribute.
         *
         * @return e.g. "slides-supported"
         */
        public String getName() {
            return name;
        }

        /**
         * The "value" field contains the value of the attribute.
         *
         * @return e.g. the textual value 'one-sided' as bytes
         */
        public byte[] getValue() {
            return value;
        }

        /**
         * The "value" field contains the value of the attribute.
         *
         * @return e.g. "one-sided"
         */
        public String getStringValue() {
            return new String(getValue(), StandardCharsets.UTF_8);
        }

    }



    /**
     * An "additional-value" has four entries.
     * <pre>
     * -----------------------------------------------
     *  |                   value-tag                 |   1 byte
     *  -----------------------------------------------
     *  |            name-length  (value is 0x0000)   |   2 bytes
     *  -----------------------------------------------
     *  |              value-length (value is w)      |   2 bytes
     *  -----------------------------------------------
     *  |                     value                   |   w bytes
     *  -----------------------------------------------
     * </pre>
     */
    public static class AdditionalValue extends AttributeWithOneValue {
        
        public AdditionalValue(ByteBuffer bytes) {
            super(bytes);
        }
        
    }

}
