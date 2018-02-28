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
 * (c)reated 10.02.2018 by Oli B. (boehm@javatux.de)
 */
package j4cups.protocol.tags;

/**
 * The ValueTags specify the type of each attribute value. There are 4 kindes
 * of value tags:
 * <ul>
 *     <li>0x10 - 0x1f: for out-of-bank values</li>
 *     <li>0x20 - 0x2f: for integer values</li>
 *     <li>0x30 - 0x3f: for octet-string values</li>
 *     <li>0x40 - 0x5f: for character-string values</li>
 *     <li>0x60 - 0xff: reserved for future type definitions</li>
 * </ul>
 * <p>
 * For further information see RFC-2910 <a 
 * href="https://tools.ietf.org/html/rfc2910#section-3.5.2">section 3.5.2</a>.
 * </p>
 *
 * @author oboehm
 * @since 0.0.2 (10.02.2018)
 */
public enum ValueTags {
    
    /** unsupported. */
    UNSUPPORTED(0x10),

    /** reserved for 'default' for definition in a future IETF document. */
    RESERVED_FOR_DEFAULT(0x11),

    /** unknown. */
    UNKNOWN(0x12),

    /** no-value. */
    NO_VALUE(0x13),

    /** 0x14-0x1F is reserved for future out-of-band" values. */
    RESERVED_FOR_OUT_OF_BAND_VALUES(0x1f),

    /** 0x20 is reserved for "generic integer" if it should ever be needed. */
    GENERIC_INTEGER(0x20),

    /** integer. */
    INTEGER(0x21),

    /** boolean. */
    BOOLEAN(0x22),

    /** enum. */
    ENUM(0x23),

    /** 0x24-0x2F is reserved for future integer type definitions. */
    RESERVED_FOR_INTEGER_TYPE(0x2f),

    /** octetString with an unspecified format. */
    UNSPECIFIED_OCTET_STRING(0x30),

    /** dateTime. */
    DATE_TIME(0x31),

    /** resolution. */
    RESOLUTION(0x32),

    /** rangeOfInteger. */
    RANGE_OF_INTEGER(0x33),

    /** reserved for future definition. */
    RESERVED_34(0x34),

    /** textWithLanguage. */
    TEXT_WITH_LANGUAGE(0x35),

    /** nameWithLanguage. */
    NAME_WITH_LANGUAGE(0x36),

    /** 0x37-0x3F is reserved for future octetString type definitions. */
    RESERVED_FOR_OCTET_STRING_TYPE(0x5f),

    /** reserved for future definition. */
    RESERVED_40(0x40),

    /** textWithoutLanguage. */
    TEXT_WITHOUT_LANGUAGE(0x41),

    /** nameWithoutLanguage. */
    NAME_WITHOUT_LANGUAGE(0x42),

    /** reserved for future definition. */
    RESERVED_43(0x43),

    /** keyword. */
    KEYWORD(0x44),

    /** uri. */
    URI(0x45),

    /** uriScheme. */
    URI_SCHEME(0x46),

    /** charset. */
    CHARSET(0x47),

    /** naturalLanguage. */
    NATURAL_LANGUAGE(0x48),
    
    /** mimeMediaType. */
    MIME_MEDIA_TYPE(0x49),
    
    /** 0x4A-0x5F is reserved for future character string type definitions. */
    RESERVED_FOR_CHARACTER_STRING_TYPE(0x5f),
    
    /** The values 0x60-0xFF are reserved for future type definitions. */
    RESERVED_FOR_FUTURE_TYPE(0xff);

    private final byte value;

    ValueTags(int value) {
        this.value = (byte) value;
    }

    /**
     * Gets the byte value of the tag.
     *
     * @return byte code from 0x00 to 0xff
     */
    public byte getValue() {
        return value;
    }

    /**
     * One kind of value tags are integer values, which are the
     * values from 0x20 - 0x2f.
     *
     * @return true or false
     */
    public boolean isIntegerValue() {
        return 0x20 <= value && value <= 0x2f;
    }

    /**
     * One kind of value tags are character-string values, which are the
     * values from 0x40 - 0x4f.
     *
     * @return true or false
     */
    public boolean isCharacterStringValue() {
        return 0x40 <= value && value <= 0x4f;
    }

    /**
     * Allows you to map a byte value to the corresponding tag.
     *
     * @param id e.g. 0x04
     * @return operation, e.g. PRINTER_ATTRIBUTES_TAG
     */
    public static ValueTags of(int id) {
        for (ValueTags tag : ValueTags.values()) {
            if (id == tag.getValue()) {
                return tag;
            }
        }
        throw new IllegalArgumentException("invalid id: " + id);
    }

    /**
     * The range for a value-tag is between 0x10 and 0x0f.
     *
     * @param id byte value
     * @return true or false
     */
    public static boolean isValid(int id) {
        return 0x10 <= id && id <= 0xff;
    }

}
