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
 * (c)reated 07.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

/**
 * Unit tests for {@link GetPrinterAttributes}.
 */
class GetPrinterAttributesTest extends OperationTest { 
    
    private final GetPrinterAttributes op = new GetPrinterAttributes();
    
    @BeforeEach
    public void setUpOperation() {
        URI testPrinter = URI.create("http://localhost:631/printers/test");
        op.setPrinterURI(testPrinter);
    }
    
    @Test
    void testGetIppRequest() {
        checkIppRequest(op.getIppRequest(), "Get-Printer-Attributes.bin");
    }
    
    @Test
    void testGetIppResponse() {
        checkIppResponse(op.getIppResponse(), "Get-Printer-Attributes.bin");
    }

}
