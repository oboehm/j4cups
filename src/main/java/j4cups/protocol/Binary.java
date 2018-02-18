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
 * (c)reated 18.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The Interface Binary describes objects which supports a binary presentation
 * of the object. Originally this interface was introduced to reduce duplicate
 * code.
 *
 * @author oboehm
 * @since 0.2 (18.02.2018)
 */
public interface Binary {

    /**
     * Converts the object to its binary presentation
     *
     * @return byte array
     */
    default byte[] toByteArray() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            writeBinaryTo(byteStream);
            byteStream.flush();
            return byteStream.toByteArray();
        } catch (IOException ioe) {
            throw new IllegalStateException("cannot dump attribute", ioe);
        }
    }

    /**
     * Writes the binary presentation to an output stream.
     * 
     * @param ostream an output stream
     * @throws IOException in case of I/O problems
     */
    void writeBinaryTo(OutputStream ostream) throws IOException;
    
}
