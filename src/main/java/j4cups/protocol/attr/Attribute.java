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

import j4cups.protocol.Binary;
import j4cups.protocol.tags.ValueTags;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
public final class Attribute implements Binary {
    
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
        this(new AttributeWithOneValue(bytes), readAdditionalValues(bytes));
    }
    
    private Attribute(AttributeWithOneValue value) {
        this(value, new ArrayList<AdditionalValue>());
    }

    private Attribute(AttributeWithOneValue value, List<AdditionalValue> additionalValues) {
        this.value = value;
        this.additionalValues = additionalValues;
    }

    private static List<AdditionalValue> readAdditionalValues(ByteBuffer buffer) {
        List<AdditionalValue> values = new ArrayList<>();
        while (buffer.remaining() > 4) {
            int pos = buffer.position();
            if (!ValueTags.isValid(buffer.get(pos)) || (buffer.getShort(pos+1) != 0)) {
                break;
            }
            values.add(new AdditionalValue(buffer));
        }
        return values;
    }

    /**
     * Creates a singe-value attribute for integer values.
     *
     * @param name e.g. "job-id"
     * @param value e.g. 42
     * @return the attribute
     */
    public static Attribute of(String name, int value) {
        return of(ValueTags.INTEGER, name, value);
    }

    /**
     * Creates a singe-value attribute for URI values.
     *
     * @param name e.g. "job-uri"
     * @param value an URI
     * @return the attribute
     */
    public static Attribute of(String name, URI value) {
        return of(ValueTags.URI, name, value.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a singe-value attribute for charset values.
     *
     * @param name e.g. "attributes-charset"
     * @param value e.g. {@link StandardCharsets#UTF_8}
     * @return the attribute
     */
    public static Attribute of(String name, Charset value) {
        return of(ValueTags.CHARSET, name, value.name().toLowerCase());
    }

    /**
     * Creates a singe-value attribute for the given language.
     *
     * @param name e.g. "attributes-natural-language"
     * @param value e.g. the English {@link Locale}
     * @return the attribute
     */
    public static Attribute of(String name, Locale value) {
        return of(ValueTags.NATURAL_LANGUAGE, name, value.getLanguage());
    }

    /**
     * Creates a singe-value attribute for integer based values.
     *
     * @param tag   the value-tag
     * @param name  the name of the attribute
     * @param value the string value of the attribute
     * @return the attribute
     */
    public static Attribute of(ValueTags tag, String name, int value) {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putInt(value);
        return of(ValueTags.INTEGER, name, bytes);
    }

    /**
     * Creates a singe-value attribute for character-string values.
     *
     * @param tag   the value-tag
     * @param name  the name of the attribute
     * @param value the string value of the attribute
     * @return the attribute
     */
    public static Attribute of(ValueTags tag, String name, String value) {
        return of(ValueTags.CHARSET, name, value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a singe-value attribute.
     *
     * @param tag   the value-tag
     * @param name  the name of the attribute
     * @param value the binary value of the attribute
     * @return the attribute
     */
    public static Attribute of(ValueTags tag, String name, byte[] value) {
        AttributeWithOneValue attr = new AttributeWithOneValue(tag, name, value);
        return new Attribute(attr);
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
     * The "value" field contains the value of a charset attribute.
     *
     * @return e.g. "one-sided"
     */
    public String getStringValue() {
        return new String(getValue(), StandardCharsets.UTF_8);
    }

    /**
     * The "value" field contains the value of a URI attribute.
     *
     * @return an URI
     */
    public URI getUriValue() {
        return URI.create(getStringValue());
    }

    /**
     * When the attribte value is multi-valued this method returns true.
     * 
     * @return true or false
     */
    public boolean isMultiValue() {
        return !this.additionalValues.isEmpty();
    }

    /**
     * This method shows a short representation of an attribute. The
     * difference to {@link #toLongString()} is, that in case of a multi-value
     * attribute only the first value is shown.
     * 
     * @return e.g. "requested-attributes=copies-supported,..."
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(getName());
        buffer.append("=");
        if (getValueTag().isCharacterStringValue()) {
            buffer.append(getStringValue());
        } else {
            buffer.append(DatatypeConverter.printHexBinary(getValue()));
        }
        if (!additionalValues.isEmpty()) {
            buffer.append(",...");
        }
        return buffer.toString();
    }

    /**
     * This method shows a short representation of an attribute. I.e. in case
     * of a multi-value attribute all values are shown.
     *
     * @return string with all values
     */
    public String toLongString() {
        StringBuilder buffer = new StringBuilder(getName());
        buffer.append("=");
        if (getValueTag().isCharacterStringValue()) {
            buffer.append(getStringValue());
        } else {
            buffer.append(new BigInteger(getValue()).toString(16));
        }
        for (AdditionalValue addValue : additionalValues) {
            buffer.append(',');
            if (getValueTag().isCharacterStringValue()) {
                buffer.append(addValue.getStringValue());
            } else {
                buffer.append(DatatypeConverter.printHexBinary(addValue.getValue()));
            }
        }
        return buffer.toString();
    }

    /**
     * Converts the attribute to a byte array as described in RFC-2910 and
     * writes it to the given output-stream.
     *
     * @param ostream an output stream
     * @throws IOException in case of I/O problems
     */
    @Override
    public void writeBinaryTo(OutputStream ostream) throws IOException {
        DataOutputStream dos = new DataOutputStream(ostream);
        dos.write(this.value.toByteArray());
        for (AdditionalValue av : additionalValues) {
            dos.write(av.toByteArray());
        }
    }



    /**
     * An "attribute-with-one-value" field is encoded with five subfields.
     * <pre>
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

        /**
         * Instantiates a new attribute-with-one-value.
         * 
         * @param valueTag the value-tag
         * @param name     name of the attribute
         * @param value    binary value of the attribute
         */
        public AttributeWithOneValue(ValueTags valueTag, String name, byte[] value) {
            this.valueTag = valueTag;
            this.name = name;
            this.value = value;
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

        /**
         * Converts an attribute-with-one-value to a byte array.
         * <pre>
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
         * 
         * @return byte array
         */
        public byte[] toByteArray() {
            try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
                writeTo(byteStream);
                byteStream.flush();
                return byteStream.toByteArray();
            } catch (IOException ioe) {
                throw new IllegalStateException("cannot dump attribute", ioe);
            }
        }

        private void writeTo(OutputStream ostream) throws IOException {
            DataOutputStream dos = new DataOutputStream(ostream);
            dos.writeByte(getValueTag().getValue());
            dos.writeShort(getName().length());
            dos.write(getName().getBytes(StandardCharsets.UTF_8));
            dos.writeShort(getValue().length);
            dos.write(getValue());
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
