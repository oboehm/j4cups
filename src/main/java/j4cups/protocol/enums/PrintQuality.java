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
 * (c)reated 09.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol.enums;

/**
 * This RECOMMENDED attribute specifies the print quality that the
 * Printer uses for the Job.
 * <pre>
 * +-------+---------------------------------------------------------+
 * | Value | Symbolic Name and Description                           |
 * +-------+---------------------------------------------------------+
 * | '3'   | 'draft': lowest quality available on the Printer        |
 * +-------+---------------------------------------------------------+
 * | '4'   | 'normal': normal or intermediate quality on the Printer |
 * +-------+---------------------------------------------------------+
 * | '5'   | 'high': highest quality available on the Printer        |
 * +-------+---------------------------------------------------------+
 * </pre>
 * 
 * @since 0.5
 */
public enum PrintQuality {
    
    /** Lowest quality available on the Printer. */
    DRAFT(3),
    
    /** Normal or intermediate quality on the Printer. */
    NORMAL(4),
    
    /** Highest quality available on the Printer. */
    HIGH(5);

    private final byte value;

    PrintQuality(int value) {
        this.value = (byte) value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public byte getValue() {
        return value;
    }

}
