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
import org.apache.http.message.BasicHttpRequest;

import java.io.IOException;

/**
 * The HttpHandler is reponsible for handling HTTP requests. He can do it by
 * forwarding the request to a real CUPS server. Or it can handle the HTTP
 * requests itself and acts like a (more ore less) real CUPS server. This
 * may be useful if you want to do some tests with a CUPS server but do not
 * want to waste paper.
 *
 * @author oliver
 * @since 0.5 (2019-07-18)
 */
public class HttpHandler {

    public void handle(BasicHttpRequest request, HttpResponse response) throws IOException {
        throw new UnsupportedOperationException("not yet implemented");
    }

}
