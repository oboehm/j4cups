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

import j4cups.protocol.AbstractIppTest;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import j4cups.protocol.attr.Attribute;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
 * use the environment variable "cupsURI":
 * </p>
 * <pre>
 *  ... -DcupsURI=http://localhost:631
 * </pre>
 *
 * @author oliver (boehm@javatux.de)
 */
class IppHandlerTest extends AbstractServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(IppHandlerTest.class);
    private static URI cupsURI;
    private static IppHandler ippHandler;
    private static URI testPrinterUri =
            URI.create(System.getProperty("printerURI", "http://localhost:631/printers/Brother_MFC_J5910DW_2"));

    @BeforeAll
    static void setUpCupsURI() {
        cupsURI = URI.create(System.getProperty("cupsURI", "http://localhost:" + cupsServer.getPort()));
        ippHandler = new IppHandler(cupsURI);
        LOG.info("{} is used for testing.", ippHandler);
    }

    /**
     * Unit test for {@link IppHandler#send(IppRequest)}.
     *
     * @throws IOException the io exception
     */
    @Test
    void testSendGetPrinters() throws IOException {
        IppRequest getPrintersRequest = readIppRequest("Get-Printers.bin");
        CloseableHttpResponse response = ippHandler.send(getPrintersRequest);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    /**
     * Another unit test for {@link IppHandler#send(IppRequest)}.
     *
     * @throws IOException the io exception
     */
    @Test
    void testSendGetJobs() throws IOException {
        IppRequest jobsRequest = readIppRequest("Get-Jobs.bin", testPrinterUri);
        CloseableHttpResponse response = ippHandler.send(jobsRequest);
        assertEquals(200, response.getStatusLine().getStatusCode());
        HttpEntity httpEntity = response.getEntity();
        byte[] content = IOUtils.toByteArray(httpEntity.getContent());
        IppResponse ippResponse = new IppResponse(content);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
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
        checkIppResponse(ippResponse, "Create-Jobs.bin");
    }

    private static void assumeCupsAndPrinterAreOnline() {
        assumeTrue(isOnline(cupsURI), cupsURI + " is not available");
        assumeTrue(isOnline(testPrinterUri), testPrinterUri + " is not available");
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
        checkIppResponse(ippResponse, "Print-Job.bin");
    }

    private static void checkIppResponse(IppResponse ippResponse, String refResource) {
        IppResponse reference = AbstractIppTest.readIppResponse("response", refResource);
        for (Attribute attr : reference.getAttributes()) {
            assertThat("missing attribute: " + attr, ippResponse.hasAttribute(attr.getName()), is(true));
        }
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
     * Test method for {@link IppHandler#sendDocument(URI, Path, int, boolean)}.
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

    private static Path readTestFile() {
        assumeTrue(isOnline(cupsURI), cupsURI + " is not available");
        Path testFile = Paths.get("src", "test", "resources", "j4cups", "test.txt");
        assertTrue(Files.exists(testFile));
        return testFile;
    }

    private static void cancelJob(IppResponse ippResponse) {
        int jobId = ippResponse.getJobId();
        LOG.info("Cancelling job {}...", jobId);
        assertThat(jobId, greaterThan(0));
        IppResponse cancelResponse = ippHandler.cancelJob(testPrinterUri, jobId);
        assertThat(cancelResponse.getStatusCode(),
                anyOf(equalTo(StatusCode.SUCCESSFUL_OK), equalTo(StatusCode.CLIENT_ERROR_NOT_POSSIBLE)));
    }

    @AfterAll
    static void closeClient() throws IOException {
        ippHandler.close();
    }

}
