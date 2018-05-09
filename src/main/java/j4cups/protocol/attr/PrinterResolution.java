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
package j4cups.protocol.attr;

import j4cups.protocol.enums.PrintQuality;

import java.nio.ByteBuffer;

/**
 * This RECOMMENDED attribute identifies the output resolution that the
 * Printer uses for the Job.
 *
 * @since 0.5
 */
public class PrinterResolution {
    
    private final int crossFeedDirection;
    private final int feedDirection;
    private final PrintQuality printQuality;

    /**
     * Instantiates a new Printer resolution.
     *
     * @param crossFeedDirection the cross feed direction
     * @param feedDirection      the feed direction
     * @param printQuality       the print quality
     */
    public PrinterResolution(int crossFeedDirection, int feedDirection, PrintQuality printQuality) {
        this.crossFeedDirection = crossFeedDirection;
        this.feedDirection = feedDirection;
        this.printQuality = printQuality;
    }

    /**
     * Returns a {@link PrinterResolution} with the given value
     *
     * @param crossFeed the cross feed direction
     * @param feed      the feed direction
     * @param quality   the printer quality
     * @return the printer resolution
     */
    public static PrinterResolution of(int crossFeed, int feed, PrintQuality quality) {
        return new PrinterResolution(crossFeed, feed, quality);
    }

    /**
     * Gets cross feed direction.
     *
     * @return the cross feed direction
     */
    public int getCrossFeedDirection() {
        return crossFeedDirection;
    }

    /**
     * Gets feed direction.
     *
     * @return the feed direction
     */
    public int getFeedDirection() {
        return feedDirection;
    }

    /**
     * Gets print quality.
     *
     * @return the print quality
     */
    public PrintQuality getPrintQuality() {
        return printQuality;
    }

    /**
     * Creates a byte array of 9 bytes with the resolutions and quality as
     * described in RFC-2910 (chapter 3.9). It contains of nine octets of  2
     * SIGNED-INTEGERs followed by a SIGNED-BYTE. The
     * first SIGNED-INTEGER contains the value of
     * cross feed direction resolution. The second
     * SIGNED-INTEGER contains the value of feed
     * direction resolution. The SIGNED-BYTE contains
     * the units.
     *
     * @return the byte [ ]
     */
    public byte[] toByteArray() {
        byte[] octets = new byte[9];
        ByteBuffer buf = ByteBuffer.wrap(octets);
        buf.putInt(getCrossFeedDirection());
        buf.putInt(getFeedDirection());
        buf.put(getPrintQuality().getValue());
        return octets;
    }

    @Override
    public String toString() {
        return getCrossFeedDirection() + "x" + getFeedDirection() + " (" + getPrintQuality() + ")";
    }

}
