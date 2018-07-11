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
 * (c)reated 11.07.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GetDefault}.
 */
class GetDefaultTest extends OperationTest {
    
    private final GetDefault op = getOperation();

    /**
     * Returns the get-default operation for testing.
     *
     * @return the Operation for testing
     */
    @Override
    protected GetDefault getOperation() {
        return new GetDefault();
    }
    
    @Test
    void testGetRequest() {
        checkIppRequest(op.getIppRequest(), "Get-Default.ipp");
    }

}
