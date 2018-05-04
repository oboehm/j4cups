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
 * (c)reated 04.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import j4cups.protocol.IppOperations;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static patterntesting.runtime.junit.ObjectTester.assertEquals;

/**
 * Unit tests for {@link Operation}.
 */
class OperationTest {
    
    private final Operation operation = new Operation(IppOperations.GET_JOBS);

    @Test
    void testSetJobId() {
        operation.setCupsURI(URI.create("http://localhost:4711"));
        operation.setJobId(42);
        assertEquals(URI.create("ipp://localhost:4711/jobs/42"), operation.getIppResponse().getJobURI());
    }

}
