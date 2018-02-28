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
 * (c)reated 09.02.2018 by oboehm (boehm@javatux.de)
 */
package j4cups.protocol.tags;

/**
 * The DelimiterTags delimit major sections of the protocol, namely attributes
 * and data. This is described in RFC-2910 in 
 * <a href="https://tools.ietf.org/html/rfc2910#section-3.5.1">section 3.5.1</a>.
 *
 * @author oboehm
 * @since 0.0.2 (09.02.2018)
 */
public enum DelimiterTags {

    /** Reserved for definition in a future IETF standards track document. */
    RESERVED(0x00),
    
    /** operation-attributes-tag. */
    OPERATION_ATTRIBUTES_TAG(0x01),
    
    /** job-attributes-tag. */
    JOB_ATTRIBUTES_TAG(0x02),
    
    /** end-of-attributes-tag. */
    END_OF_ATTRIBUTES_TAG(0x03),
    
    /** printer-attributes-tag. */
    PRINTER_ATTRIBUTES_TAG(0x04),
    
    /** unsupported-attributes-tag. */
    UNSUPPORTED_ATTRIBUTES_TAG(0x05),
    
    /** Reserved for future delimiters in IETF standards track documents (0x06-0x0f). */
    RESERVED_FOR_FUTURE(0x0f);
    
    private final byte value;
    
    DelimiterTags(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Gets the byte value of the tag.
     *
     * @return byte code from 0x00 to 0x0f
     */
    public byte getValue() {
        return value;
    }

    /**
     * This implementation generates the same representation as in the
     * corresponding RFCs.
     * 
     * @return e.g. "job-attributes-tag"
     */
    @Override
    public String toString() {
        return super.toString().toLowerCase().replaceAll("_", "-");
    }

    /**
     * Allows you to map a byte value to the corresponding tag.
     *
     * @param id e.g. 0x04
     * @return operation, e.g. PRINTER_ATTRIBUTES_TAG
     */
    public static DelimiterTags of(int id) {
        for (DelimiterTags tag : DelimiterTags.values()) {
            if (id == tag.getValue()) {
                return tag;
            }
        }
        throw new IllegalArgumentException("invalid id: " + id);
    }

    /**
     * The range for a delimiter-tag is between 0 and 0x0f.
     * 
     * @param id byte value
     * @return true or false
     */
    public static boolean isValid(int id) {
        return 0 <= id && id <= 0x0f;
    }

}
