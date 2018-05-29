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
 * (c)reated 29.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.client;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

/**
 * This is a very basic client to access CUPS. It was introduced to simplify
 * the internal tests with a CUPS server.
 * 
 * @since 0.5
 */
public class CupsClient {
    
    private final URI cupsURI;

    /**
     * Generates a client for the access to a local CUPS on port 631.
     */
    public CupsClient() {
        this(URI.create("http://localhost:631"));
    }

    /**
     * Generates a client for the access to the given URI to CUPS.
     * 
     * @param cupsURI normally "http://localhost:631" on Linux and Mac
     */
    public CupsClient(URI cupsURI) {
        this.cupsURI = cupsURI;
    }

    /**
     * Sends a list of files as one job to the given printer.
     * 
     * @param printerURI where to send the files
     * @param files the files to be printed
     */
    public void printTo(URI printerURI, List<Path> files) {
        throw new UnsupportedOperationException("not yet implemented");
    }
    
}
