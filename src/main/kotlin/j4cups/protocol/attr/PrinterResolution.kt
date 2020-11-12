/*
 * Copyright (c) 2018-2020 by Oliver Boehm
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
package j4cups.protocol.attr

import j4cups.protocol.enums.PrintQuality
import java.nio.ByteBuffer

/**
 * This RECOMMENDED attribute identifies the output resolution that the
 * Printer uses for the Job.
 *
 * @since 0.5
 */
class PrinterResolution(
        val crossFeedDirection: Int,
        val feedDirection: Int,
        val printQuality: PrintQuality) {

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
    fun toByteArray(): ByteArray {
        val octets = ByteArray(9)
        val buf = ByteBuffer.wrap(octets)
        buf.putInt(crossFeedDirection)
        buf.putInt(feedDirection)
        buf.put(printQuality.value)
        return octets
    }

    override fun toString(): String {
        return crossFeedDirection.toString() + "x" + feedDirection + " (" + printQuality + ")"
    }



    companion object {

        /**
         * Returns a [PrinterResolution] with the given value
         *
         * @param crossFeed the cross feed direction
         * @param feed      the feed direction
         * @param quality   the printer quality
         * @return the printer resolution
         */
        @JvmStatic
        fun of(crossFeed: Int, feed: Int, quality: PrintQuality): PrinterResolution {
            return PrinterResolution(crossFeed, feed, quality)
        }

    }

}