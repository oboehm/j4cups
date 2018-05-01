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
 * (c)reated 01.05.18 by oliver (ob@oasd.de)
 */
package j4cups.server.http;

import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;

/**
 * Class AbstractIppRequestHandler.
 *
 * @author oliver
 * @since 0.5
 */
public abstract class AbstractIppRequestHandler implements HttpRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIppRequestHandler.class);

    /**
     * Handles the incomming HTTP request and converts it to a
     * {@link HttpEntityEnclosingRequest} (if possible).
     *
     * @param request incoming request
     * @param response outgoing response
     * @param context context
     * @throws HttpException in case of HTTP problems
     * @throws IOException e.g. network problems
     */
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        if (request instanceof HttpEntityEnclosingRequest) {
            handle((HttpEntityEnclosingRequest) request, response);
        } else {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
            StringEntity entity = new StringEntity(
                    "<html><body><h1>" + request +
                            " not supported</h1></body></html>",
                    ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);
            LOG.warn("{} is not supported.", request);
        }
    }

    /**
     * Handles the incomming HTTP request.
     *
     * @param request incoming request
     * @param response outgoing response
     * @throws IOException e.g. network problems
     */
    protected abstract void handle(HttpEntityEnclosingRequest request, HttpResponse response) throws IOException;

}
