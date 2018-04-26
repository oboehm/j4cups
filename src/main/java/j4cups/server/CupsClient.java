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
 * (c)reated 26.04.2018 by oboehm (ob@oasd.de)
 */
package j4cups.server;

import j4cups.protocol.IppRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The CupsClient is needed for the communication to real CUPS server which
 * is used by the {@link CupsServer} to send the request.
 *
 * @author oboehm
 * @since 0.5 (26.04.2018)
 */
public final class CupsClient {

    private static final Logger LOG = LoggerFactory.getLogger(CupsClient.class);
    private final URI forwardURI;

    /**
     * Instantiates a new Cups client.
     *
     * @param forwardURI the forward uri
     */
    public CupsClient(URI forwardURI) {
        this.forwardURI = forwardURI;
    }

    /**
     * Sends an IPP request to a CUPS server. Normally the resonse from CUPS is
     * an {@link j4cups.protocol.IppResponse}. But in some case (like the
     * get-printers operation the answer is returned as HTML page.
     *
     * @param ippRequest the ipp request
     * @return response from CUPS
     * @throws IOException the io exception
     */
    public CloseableHttpResponse send(IppRequest ippRequest) throws IOException {
        URI printerURI = getPrinterURI(ippRequest);
        LOG.info("Sending to {}: {}.", printerURI, ippRequest);
        HttpPost httpPost = new HttpPost(printerURI);
        httpPost.setEntity(new ByteArrayEntity(ippRequest.toByteArray()));
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            CloseableHttpResponse response = client.execute(httpPost);
            LOG.info("Received from {}: {}", printerURI, response);
            return response;
        }
    }

    private URI getPrinterURI(IppRequest ippRequest) {
        URI portlessURI = ippRequest.getAttribute("printer-uri").getUriValue();
        try {
            return new URI(portlessURI.getScheme(), portlessURI.getUserInfo(), portlessURI.getHost(),
                    forwardURI.getPort(), portlessURI.getPath(), portlessURI.getQuery(), portlessURI.getFragment());
        } catch (URISyntaxException ex) {
            LOG.warn("Cannot create printer-uri '{}' with port {}:", portlessURI, forwardURI.getPort(), ex);
            return portlessURI;
        }
    }

}
