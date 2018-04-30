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
 * (c)reated 29.04.18 by oliver (ob@oasd.de)
 */

package j4cups.op;

import j4cups.protocol.IppOperations;

/**
 * Class CancelJob represents the cancel-job operation.
 *
 * @author oliver
 * @since 0.5
 */
public class CancelJob extends Operation {

    /**
     * Instantiates an operation for 'cancel-job'.
     */
    public CancelJob() {
        super(IppOperations.CANCEL_JOB);
    }

}
