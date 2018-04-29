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

import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for class {@link CupsClient}. For tests with a real I used
 * http://localhost:631/printers/Brother_MFC_J5910DW_2 in my local home
 * network.
 *
 * @author oliver (boehm@javatux.de)
 */
class CupsClientTest extends AbstractServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(CupsClientTest.class);
    private static final URI CUPS_URI = URI.create("http://localhost:631");
    private static final CupsClient CLIENT = new CupsClient(CUPS_URI);
    public static final URI TEST_PRINTER_URI = URI.create("http://localhost:631/printers/Brother_MFC_J5910DW_2");

    /**
     * Unit test for {@link CupsClient#send(IppRequest)}.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testSendGetPrinters() throws IOException {
        IppRequest getPrintersRequest = readIppRequest("Get-Printers.bin");
        CloseableHttpResponse response = CLIENT.send(getPrintersRequest);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    /**
     * Another unit test for {@link CupsClient#send(IppRequest)}.
     *
     * @throws IOException the io exception
     */
    @Test
    public void testSendGetJobs() throws IOException {
        IppRequest jobsRequest = readIppRequest("Get-Jobs.bin", TEST_PRINTER_URI);
        CloseableHttpResponse response = CLIENT.send(jobsRequest);
        assertEquals(200, response.getStatusLine().getStatusCode());
        HttpEntity httpEntity = response.getEntity();
        byte[] content = IOUtils.toByteArray(httpEntity.getContent());
        IppResponse ippResponse = new IppResponse(content);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
    }

    /**
     * Test method for {@link CupsClient#getJobs(URI)}.
     */
    @Test
    public void testGetJobs() {
        assumeTrue(isOnline(CUPS_URI), CUPS_URI + " is not available");
        IppResponse ippResponse = CLIENT.getJobs(TEST_PRINTER_URI);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
    }

    /**
     * Test method for {@link CupsClient#createJob(URI)} and
     * {@link CupsClient#cancelJob(URI, int)}.
     */
    @Test
    public void testCreateAndCancelJob() {
        assumeTrue(isOnline(CUPS_URI), CUPS_URI + " is not available");
        IppResponse ippResponse = CLIENT.createJob(TEST_PRINTER_URI);
        int jobId = ippResponse.getJobId();
        LOG.info("Job {} created.", jobId);
        assertThat(jobId, greaterThan(0));
        ippResponse = CLIENT.cancelJob(TEST_PRINTER_URI, jobId);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
    }

    @AfterAll
    public static void closeClient() throws IOException {
        CLIENT.close();
    }

}
