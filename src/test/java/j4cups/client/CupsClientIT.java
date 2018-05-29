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
 * (c)reated 29.05.18 by oliver (ob@oasd.de)
 */
package j4cups.client;

import j4cups.op.OperationTest;
import j4cups.protocol.IppResponse;
import j4cups.server.AbstractServerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link CupsClient}.
 */
final class CupsClientIT {

    private CupsClient cupsClient;

    @BeforeEach
    public void setUpCupsClient() {
        cupsClient = getCupsClient();
    }

    /**
     * Creates a job for a printer. To specify the printer use
     * <pre>
     *     -DprinterURI=http://localhost:631/printers/Brother_MFC_J5910DW_2
     * </pre>
     * <p>
     * to set it. Otherwise the tests would be skipped.
     * </p>
     */
    @Test
    void createJob() {
        String printer = System.getProperty("printerURI");
        assumeTrue(printer != null, "specify printer with '-DprinterURI=...'");
        URI printerURI = URI.create(printer);
        IppResponse ippResponse = cupsClient.createJob(printerURI);
        cupsClient.cancelJob(ippResponse.getJobId());
        OperationTest.checkIppResponse(ippResponse, "Create-Jobs.bin");
    }

    /**
     * If you have no CUPS running on your local machine you must set the
     * envrionment variable 'cupsURI' to your CUPS server in the
     * network. Otherwise the test fails.
     *
     * @return your CupsClient for testing
     */
    public static CupsClient getCupsClient() {
        URI cupsURI = URI.create(System.getProperty("cupsURI", "http://localhost:631"));
        assumeTrue(AbstractServerTest.isOnline(cupsURI),
                cupsURI + " is offline - use '-DcupsURI=...' to use another CUPS");
        return new CupsClient(cupsURI);
    }

}