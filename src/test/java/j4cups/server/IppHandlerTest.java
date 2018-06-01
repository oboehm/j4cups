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
 * (c)reated 26.04.18 by oliver (ob@oasd.de)
 */

package j4cups.server;

import j4cups.op.OperationTest;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for class {@link IppHandler}. For tests with a real I used
 * http://localhost:631/printers/Brother_MFC_J5910DW_2 in my local home
 * network.
 * <p>
 * To set a real CUPS server for {@link IppHandlerTest} as forward URI you can
 * use the environment variable "forwardURI":
 * </p>
 * <pre>
 *  ... -DcupsURI=http://localhost:631
 * </pre>
 *
 * @author oliver (boehm@javatux.de)
 */
public class IppHandlerTest extends AbstractServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(IppHandlerTest.class);
    protected static URI forwardURI;
    protected static URI testPrinterUri;
    private IppHandler ippHandler;

    @BeforeAll
    static void setUpCupsURI() {
        Path spoolDir = Paths.get("target");
        forwardURI = spoolDir.toUri();
        String cupsProp = System.getProperty("forwardURI");
        if (cupsProp != null) {
            forwardURI = URI.create(cupsProp);
        }
        testPrinterUri = URI.create(System.getProperty("printerURI", "http://localhost:631/printers/text"));
    }
    
    @BeforeEach
    void setUpIppHandler() {
        ippHandler = getIppHandler();
        LOG.info("{} is used for testing.", ippHandler);
    }
    
    protected IppHandler getIppHandler() {
        return new IppHandler(forwardURI);
    }

    /**
     * Unit test for {@link IppHandler#send(IppRequest)}.
     */
    @Test
    void testSendGetPrinters() {
        IppRequest getPrintersRequest = readIppRequest("Get-Printers.bin");
        IppResponse response = ippHandler.send(getPrintersRequest);
        assertEquals(StatusCode.SUCCESSFUL_OK, response.getStatusCode());
    }

    /**
     * Test method for {@link IppHandler#getJobs(URI)}.
     */
    @Test
    void testGetJobs() {
        assumeCupsAndPrinterAreOnline();
        IppResponse ippResponse = ippHandler.getJobs(testPrinterUri);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
    }

    /**
     * Test method for {@link IppHandler#createJob(URI)} and
     * {@link IppHandler#cancelJob(URI, int)}.
     */
    @Test
    void testCreateAndCancelJob() {
        assumeCupsAndPrinterAreOnline();
        IppResponse ippResponse = ippHandler.createJob(testPrinterUri);
        cancelJob(ippResponse);
        OperationTest.checkIppResponse(ippResponse, "Create-Jobs.bin");
    }
    /**
     * Test method for {@link IppHandler#printJob(URI, Path)}.
     */
    @Test
    void testPrintJob() {
        assumeCupsAndPrinterAreOnline();
        Path readme = readTestFile();
        IppResponse ippResponse = ippHandler.printJob(testPrinterUri, readme);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
        cancelJob(ippResponse);
        OperationTest.checkIppResponse(ippResponse, "Print-Job.bin");
    }

    @Test
    void testGetPrinterAttributes() {
        assumeCupsAndPrinterAreOnline();
        IppResponse ippResponse = ippHandler.getPrinterAttributes(testPrinterUri);
        OperationTest.checkIppResponse(ippResponse, "Get-Printer-Attributes.bin");
    }

    /**
     * Test method for {@link IppHandler#sendDocument(URI, Path, int, boolean)}.
     */
    @Test
    void testSendDocument() {
        assumeCupsAndPrinterAreOnline();
        Path testFile = readTestFile();
        int jobId = ippHandler.createJob(testPrinterUri).getJobId();
        try {
            IppResponse ippResponse = ippHandler.sendDocument(testPrinterUri, testFile, jobId, true);
            assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
        } finally {
            ippHandler.cancelJob(testPrinterUri, jobId);
        }
    }

    /**
     * Test method for {@link IppHandler#sendDocument(URI, Path, int, boolean)},
     * where two documents are sent and cancelled immediately afterwards. So if
     * you want to see the documents on the printer set a breakpoint before the
     * cancel statement and wait.
     */
    @Test
    void testSendTwoDocuments() {
        assumeCupsAndPrinterAreOnline();
        Path testFile = readTestFile();
        int jobId = ippHandler.createJob(testPrinterUri).getJobId();
        try {
            ippHandler.sendDocument(testPrinterUri, testFile, jobId, false);
            IppResponse ippResponse = ippHandler.sendDocument(testPrinterUri, testFile, jobId, true);
            assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
        } finally {
            ippHandler.cancelJob(testPrinterUri, jobId);
        }
    }

    private static void assumeCupsAndPrinterAreOnline() {
        assumeTrue(isOnline(forwardURI), forwardURI + " is not available");
        assumeTrue(isOnline(testPrinterUri), testPrinterUri + " is not available");
    }
    
    private static Path readTestFile() {
        assumeTrue(isOnline(forwardURI), forwardURI + " is not available");
        Path testFile = Paths.get("src", "test", "resources", "j4cups", "test.txt");
        assertTrue(Files.exists(testFile));
        return testFile;
    }

    public void cancelJob(IppResponse ippResponse) {
        int jobId = ippResponse.getJobId();
        LOG.info("Cancelling job {}...", jobId);
        assertThat(jobId, greaterThan(0));
        IppResponse cancelResponse = ippHandler.cancelJob(testPrinterUri, jobId);
        assertThat(cancelResponse.getStatusCode(),
                anyOf(equalTo(StatusCode.SUCCESSFUL_OK), equalTo(StatusCode.CLIENT_ERROR_NOT_POSSIBLE)));
    }

}
