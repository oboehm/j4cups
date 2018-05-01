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

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class LogRequestInterceptor.
 *
 * @author oliver
 * @since 0.5
 */
public final class LogResponseInterceptor implements HttpResponseInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(LogResponseInterceptor.class);
    private final String prefix;

    /**
     * Instantiates a new interceptor.
     *
     * @param prefix for the log output
     */
    public LogResponseInterceptor(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Processes a response.
     * On the server side, this step is performed before the response is
     * sent to the client. On the client side, this step is performed
     * on incoming messages before the message body is evaluated.
     *
     * @param response the response to postprocess
     * @param context  the context for the request
     */
    @Override
    public void process(HttpResponse response, HttpContext context) {
        LOG.info("{} => {}", prefix, response);
        LOG.debug("{} => {}", prefix, context);
    }

}
