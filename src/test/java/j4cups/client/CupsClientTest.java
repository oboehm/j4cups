package j4cups.client;

import j4cups.protocol.IppResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void testCreateJob() throws IOException {
        setUpHttpClient();
        URI printerURI = URI.create("http://test-printer:4711/");
        IppResponse response = cupsClient.createJob(printerURI);
        assertNotNull(response);
    }

    private static void setUpHttpClient() throws IOException  {
        CloseableHttpResponse mockedHttpResponse = mock(CloseableHttpResponse.class);
        when(mockedHttpClient.execute(any(HttpPost.class))).thenReturn(mockedHttpResponse);
        HttpEntity mockedHttpEntity = mock(HttpEntity.class);
        when(mockedHttpEntity.getContent()).thenReturn(createIppResponseInputStream());
        when(mockedHttpResponse.getEntity()).thenReturn(mockedHttpEntity);
    }

    private static InputStream createIppResponseInputStream() {
        IppResponse response = new IppResponse();
        return new ByteArrayInputStream(response.toByteArray());
    }

    @AfterAll
    static void resetHttpBuilder() {
        mockedStaticBuilder.when(HttpClientBuilder::create).thenCallRealMethod();
        LOG.info("{} is resetted.", mockedStaticBuilder);
    }

}