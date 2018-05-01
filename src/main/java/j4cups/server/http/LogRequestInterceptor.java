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
 * (c)reated 27.04.18 by oliver (ob@oasd.de)
 */

package j4cups.server.http;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class LogRequestInterceptor.
 *
 * @author oliver
 * @since 0.5
 */
public final class LogRequestInterceptor implements HttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(LogRequestInterceptor.class);
    private final String prefix;

    /**
     * Instantiates a new interceptor.
     *
     * @param prefix for the log output
     */
    public LogRequestInterceptor(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Processes a request.
     * On the client side, this step is performed before the request is
     * sent to the server. On the server side, this step is performed
     * on incoming messages before the message body is evaluated.
     *
     * @param request the request to preprocess
     * @param context the context for the request
     */
    @Override
    public void process(HttpRequest request, HttpContext context) {
        LOG.info("{} <= {}", prefix, request);
        LOG.debug("{} <= {}", prefix, context);
    }

}
