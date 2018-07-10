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
 * (c)reated 10.07.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GetPrinters}.
 */
class GetPrintersTest extends OperationTest {

    /**
     * Returns the get-printers operation for testing.
     *
     * @return the Operation for testing
     */
    @Override
    protected GetPrinters getOperation() {
        return new GetPrinters();
    }

    /**
     * This is a valid request. No Exception should be thrown.
     */
    @Test
    void testValidateRequest() {
        checkValidateRequest("Get-Printers.ipp");
    }

}
