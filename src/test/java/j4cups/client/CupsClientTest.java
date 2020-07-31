package j4cups.client;

import j4cups.protocol.IppResponse;
import j4cups.protocol.StatusCode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * Copyright (c) 2020 by Oliver Boehm
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
 * (c)reated 21.07.2020 by oboehm
 */
class CupsClientTest {

    private static Logger LOG = LoggerFactory.getLogger(CupsClientTest.class);
    private static URI PRINTER_URI = URI.create("http://test-printer:4711/");
    private static MockedStatic<HttpClientBuilder> mockedStaticBuilder = Mockito.mockStatic(HttpClientBuilder.class);
    private static CloseableHttpClient mockedHttpClient = mock(CloseableHttpClient.class);
    private final CupsClient cupsClient = new CupsClient();

    @BeforeAll
    static void setUpHttpBuilder() {
        HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
        when(mockedBuilder.build()).thenReturn(mockedHttpClient);
        mockedStaticBuilder.when(HttpClientBuilder::create).thenReturn(mockedBuilder);
        LOG.info("{}.create() is set up and will return {}.", mockedStaticBuilder, mockedBuilder);
    }

    @BeforeEach
    void setUpHttpClient() throws IOException {
        setUpHttpClientWith(new IppResponse());
    }

    private static void setUpHttpClientWith(IppResponse response) throws IOException {
        CloseableHttpResponse mockedHttpResponse = mock(CloseableHttpResponse.class);
        when(mockedHttpClient.execute(any(HttpPost.class))).thenReturn(mockedHttpResponse);
        HttpEntity mockedHttpEntity = mock(HttpEntity.class);
        when(mockedHttpEntity.getContent()).thenReturn(new ReopenableByteStream(response.toByteArray()));
        when(mockedHttpResponse.getEntity()).thenReturn(mockedHttpEntity);
    }

    @Test
    void testCreateJob() {
        IppResponse response = cupsClient.createJob(PRINTER_URI);
        assertNotNull(response);
    }

    @Test
    void testCancelJob() {
        IppResponse response = cupsClient.cancelJob(4711, PRINTER_URI);
        assertNotNull(response);
    }

    @Test
    void testPrintJob() {
        Path file = Paths.get("src/test/resources/j4cups/test.txt");
        IppResponse ippResponse = cupsClient.print(PRINTER_URI, file);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
    }

    @Test
    void testPrintDocuments() throws IOException {
        IppResponse response = new IppResponse();
        response.setJobId(4711);
        setUpHttpClientWith(response);
        Path file = Paths.get("src/test/resources/j4cups/test.txt");
        IppResponse ippResponse = cupsClient.print(URI.create("http://testprinter:777"), file, file);
        assertEquals(StatusCode.SUCCESSFUL_OK, ippResponse.getStatusCode());
    }

    /**
     * This is a replay of 2 documents which were successful sent to a printer
     * using the send-document operation.
     *
     * @throws IOException the io exception
     */
    @Test
    void testReplay() throws IOException {
        checkReplay("send-document");
    }

    /**
     * This is a replay of 2 documents which were not successful sent to a
     * printer using the send-document operation. Nevertheless the replay
     * should not break but should log the problematic requests.
     *
     * @throws IOException the io exception
     */
    @Test
    void testReplay400() throws IOException {
        checkReplay("send-document-400");
    }

    private void checkReplay(String filename) throws IOException {
        Path dir = Paths.get("src", "test", "resources", "j4cups", "recorded", filename);
        assertTrue(Files.isDirectory(dir), dir + " is not a directory");
        cupsClient.replay(dir);
    }

    @Test
    void testToString() {
        String s = cupsClient.toString();
        assertThat(s, not(containsString("@")));
        LOG.info("s = \"{}\"", s);
    }

    @AfterAll
    static void resetHttpBuilder() {
        mockedStaticBuilder.when(HttpClientBuilder::create).thenCallRealMethod();
        LOG.info("{} is resetted.", mockedStaticBuilder);
    }



    static class ReopenableByteStream extends ByteArrayInputStream {

        public ReopenableByteStream(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() {
            reset();
        }

    }

}
