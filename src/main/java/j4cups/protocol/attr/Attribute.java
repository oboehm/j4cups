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
import java.util.*;

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
    
    private final List<AdditionalValue> additionalValues = new ArrayList<>();

    /**
     * Instantiates a new (single valued or multi-valued) attribute from the
     * given bytes. The given {@link ByteBuffer} must be positioned at the 
     * beginning of the attribute.
     *
     * @param bytes ByteBuffer positioned at the beginning
     */
    public Attribute(ByteBuffer bytes) {
        this(readAdditionalValues(bytes));
    }
    
    private Attribute(AttributeWithOneValue value) {
        this(Collections.singletonList(new AdditionalValue(value)));
    }

    private Attribute(List<AdditionalValue> additionalValues) {
        this.additionalValues.addAll(additionalValues);
    }

    private static List<AdditionalValue> readAdditionalValues(ByteBuffer buffer) {
        List<AdditionalValue> values = new ArrayList<>();
        while (buffer.remaining() > 4) {
            values.add(new AdditionalValue(buffer));
            int pos = buffer.position();
            if ((buffer.remaining() <= 4) || (!ValueTags.isValid(buffer.get(pos)) || (buffer.getShort(pos+1) != 0))) {
                break;
            }
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
     * Creates a singe-value attribute for a range of integer values.
     *
     * @param name e.g. "copies-supported"
     * @param min e.g. 1
     * @param max e.g. 9999
     * @return the attribute
     */
    public static Attribute of(String name, int min, int max) {
        byte[] data = new byte[8];
        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.putInt(min);
        buf.putInt(max);
        return of(ValueTags.RANGE_OF_INTEGER, name, data);
    }

    /**
     * Creates a singe-value attribute for boolean values.
     *
     * @param name e.g. "last-document"
     * @param value true or falce
     * @return the attribute
     */
    public static Attribute of(String name, boolean value) {
        byte[] booleanByte = new byte[1];
        booleanByte[0] = (byte) (value ? 0x01 : 0x00);
        return of(ValueTags.BOOLEAN, name, booleanByte);
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
        return of(tag, name, bytes);
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
     * Creates a multi-value attribute for the given value-tag.
     *
     * @param tag   the value-tag
     * @param name  the name of the attribute
     * @return the attribute
     */
    public static Attribute of(ValueTags tag, String name, byte[]... additionalValues) {
        Attribute attr = new Attribute(new AttributeWithOneValue(tag, name, new byte[0]));
        for (byte[] bytes : additionalValues) {
            attr.add(Attribute.of(tag, "", bytes));
        }
        return attr;
    }

    /**
     * Creates a multi-value attribute for character-string values.
     *
     * @param tag              the value-tag
     * @param name             the name of the attribute
     * @param additionalValues additional values
     * @return the attribute
     */
    public static Attribute of(ValueTags tag, String name, String... additionalValues) {
        Attribute attr = new Attribute(new AttributeWithOneValue(tag, name, new byte[0]));
        for (String s : additionalValues) {
            attr.add(Attribute.of(tag, "", s.getBytes(StandardCharsets.UTF_8)));
        }
        return attr;
    }

    /**
     * Creates a single-value attribute for character-string values.
     *
     * @param tag   the value-tag
     * @param name  the name of the attribute
     * @return the attribute
     */
    public static Attribute of(ValueTags tag, String name, String value) {
        return new Attribute(new AttributeWithOneValue(tag, name, value.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Creates a multi-value attribute with the different printer resolutions.
     *
     * @param name        the name
     * @param resolutions the different printer resolutions
     * @return the attribute
     */
    public static Attribute of(String name, PrinterResolution... resolutions) {
        Attribute attr = new Attribute(new AttributeWithOneValue(ValueTags.RESOLUTION, name, new byte[0]));
        for (PrinterResolution pr : resolutions) {
            attr.add(Attribute.of(ValueTags.RESOLUTION, "", pr.toByteArray()));
        }
        return attr;
    }

    /**
     * The "value-tag" field specifies the attribute syntax, e.g. 0x44
     * for the attribute syntax 'keyword'.
     *
     * @return e.g. {@link ValueTags#KEYWORD}
     */
    public ValueTags getValueTag() {
        return additionalValues.get(0).getValueTag();
    }

    /**
     * The "name" field contains the textual name of the attribute.
     *
     * @return e.g. "slides-supported"
     */
    public String getName() {
        return additionalValues.get(0).getName();
    }

    /**
     * Sets the value of the attribute.
     * 
     * @param value value as byte array
     */
    public void setValue(byte[] value) {
        this.additionalValues.get(0).setValue(value);
    }

    /**
     * The "value" field contains the value of the attribute.
     * If the attribute is a multi-value attribute this returns only the
     * fist value.
     *
     * @return e.g. the textual value 'one-sided' as bytes
     */
    public byte[] getValue() {
        return additionalValues.get(0).getValue();
    }

    /**
     * Gets the different values for a multi-value attribute.
     *
     * @return the values
     */
    public List<AdditionalValue> getAdditionalValues() {
        return additionalValues;
    }

    /**
     * The "value" field contains the value of a integer attribute.
     *
     * @return a 32-bit number
     */
    public int getIntValue() {
        return new BigInteger(getValue()).intValue();
    }

    /**
     * The "value" field contains the value of a boolean attribute. The boolean
     * value is stored as byte, whereas 0x00 is 'false' and 0x01 is 'true'.
     *
     * @return true (0x01) or false (0x00)
     */
    public boolean getBooleanValue() {
        byte booleanByte = getValue()[0];
        switch (booleanByte) {
            case 0x00:  return false;
            case 0x01:  return true;
            default:    throw new IllegalStateException("invalid boolean value stored: 0x" + Integer.toHexString(booleanByte));
        }
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
        return this.additionalValues.size() > 1;
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
        getValueAsString(buffer);
        if (isMultiValue()) {
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
        for (AdditionalValue addValue : additionalValues) {
            if (getValueTag().isCharacterStringValue()) {
                buffer.append(addValue.getStringValue());
            } else {
                buffer.append(DatatypeConverter.printHexBinary(addValue.getValue()));
            }
            buffer.append(',');
        }
        buffer.deleteCharAt(buffer.length()-1);
        return buffer.toString();
    }

    private void getValueAsString(StringBuilder buffer) {
        ValueTags tag = getValueTag();
        if (tag.isIntegerValue()) {
            buffer.append(getIntValue());
        } else if (tag.isCharacterStringValue()) {
            buffer.append(getStringValue());
        } else {
            buffer.append(DatatypeConverter.printHexBinary(getValue()));
        }
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
        for (AdditionalValue av : additionalValues) {
            dos.write(av.toByteArray());
        }
    }

    /**
     * For multi-value attributes you can use this method to add the single
     * values to the attribute.
     *
     * @param value the value
     */
    public void add(Attribute value) {
        additionalValues.add(new AdditionalValue(value));
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
        private byte[] value;

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
         * Sets the value of the attribute.
         * 
         * @param value as byte array
         */
        public void setValue(byte[] value) {
            this.value = value;
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

        public AdditionalValue(Attribute attr) {
            this(attr.toByteArray());
        }

        public AdditionalValue(AttributeWithOneValue attr) {
            this(attr.toByteArray());
        }
        
        public AdditionalValue(byte[] bytes) {
            this(ByteBuffer.wrap(bytes));
        }

        public AdditionalValue(ByteBuffer bytes) {
            super(bytes);
        }

    }

}
