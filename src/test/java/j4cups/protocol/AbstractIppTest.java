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
 * (c)reated 15.02.2018 by Oli B. (boehm@javatux.de)
 */
package j4cups.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The class AbstractIppTest provides some prepared IPP requests for testing.
 *
 * @author oboehm
 * @since 0.1 (15.02.2018)
 */
public abstract class AbstractIppTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIppTest.class);

    public final static IppRequest REQUEST_PRINT_JOB = readRequest("Print-Job.bin");
    public final static IppRequest REQUEST_GET_JOBS = readRequest("Get-Jobs.bin");
    public final static IppRequest REQUEST_GET_PRINTER_ATTRIBUTES = readRequest("Get-Printer-Attributes.bin");

    private AbstractIpp ippPackage;
    
    private static IppRequest readRequest(String name) {
        Path recordedPrintJob = Paths.get("src", "test", "resources", "j4cups", "request", name);
        try {
            byte[] requestData = Files.readAllBytes(recordedPrintJob);
            return new IppRequest(requestData);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("no file '" + recordedPrintJob + "' found", ioe);
        }
    }

    /**
     * Should return an IPP request or response for testing.
     * 
     * @return IPP request or response
     */
    protected abstract AbstractIpp getIppPackage();
    
    @BeforeEach
    public void setUpIppPackage() {
        ippPackage = getIppPackage();
    }

    @Test
    public void testToString() {
        String s = ippPackage.toString();
        LOG.info("s = \"{}\"", s);
        assertThat(s, containsString(ippPackage.getOpCodeAsString()));
    }

    @Test
    public void testToLongString() {
        String longString = ippPackage.toLongString();
        LOG.info("longString = {}", longString);
        assertThat(longString.length(), greaterThan(ippPackage.toString().length()));
    }

    @Test
    public void testSetData() {
        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        ippPackage.setData(bytes);
        assertEquals("hello", new String(ippPackage.getData(), StandardCharsets.UTF_8));
    }

    @Test
    public void testSetAttribute() {
        byte[] world = "world".getBytes(StandardCharsets.UTF_8);
        ippPackage.setAttribute("hello", world);
        assertEquals("world", ippPackage.getAttribute("hello").getStringValue());
    }

}
