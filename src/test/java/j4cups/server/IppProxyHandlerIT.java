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
 * (c)reated 01.06.2018 by oboehm (ob@oasd.de)
 */
package j4cups.server;

/**
 * Integration tests for {@link IppProxyHandler}. For tests with a real I used
 * http://localhost:631/printers/Brother_MFC_J5910DW_2 in my local home
 * network.
 * <p>
 * To set a real CUPS server for {@link IppProxyHandlerIT} as forward URI you can
 * use the environment variable "forwardURI":
 * </p>
 * <pre>
 *  ... -DcupsURI=http://localhost:631
 * </pre>
 *
 * @author oboehm
 */
public class IppProxyHandlerIT extends IppHandlerTest {

    @Override
    protected IppHandler getIppHandler() {
        return new IppProxyHandler(forwardURI);
    }

}
