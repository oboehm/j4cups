/*
 * Copyright (c) 2019 by Oliver Boehm
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
 * (c)reated 2019-07-18 by oliver (ob@oasd.de)
 */

package j4cups.server;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpRequest;

import java.io.IOException;
import java.net.URI;

/**
 * The class HttpProxyHandler forwards HTTP requests to a real CUPS server and
 * acts like a proxy. It can be used as template for additional tasks like
 * filtering, accounting, logging or other things which are not part
 * of a normal CUPS server.
 *
 * @author oliver
 * @since 0.5 (2019-07-18)
 */
public class HttpProxyHandler extends HttpHandler {

    private final URI forwardURI;

    public HttpProxyHandler(URI forwardURI) {
        this.forwardURI = forwardURI;
    }

    public void handle(BasicHttpRequest request, HttpResponse response) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(forwardURI + request.getRequestLine().getUri());
        CloseableHttpResponse cupsResponse = httpclient.execute(httpGet);
        response.setEntity(cupsResponse.getEntity());
    }

}
