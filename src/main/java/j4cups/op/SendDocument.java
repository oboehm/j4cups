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
 * (c)reated 26.03.2018 by oboehm (ob@oasd.de)
 */
package j4cups.op;

import j4cups.protocol.IppOperations;
import j4cups.protocol.IppRequest;

import javax.validation.ValidationException;

/**
 * Class SendDocument represents the Send-Document operation if IPP.
 *
 * @author oboehm
 * @since 0.5 (26.03.2018)
 */
public class SendDocument extends Operation {

    /**
     * Instantiates an operation for 'send-document'.
     */
    public SendDocument() {
        super(IppOperations.SEND_DOCUMENT);
    }

    /**
     * Looks if the given request is valid. If not an
     * {@link ValidationException} will be thrown
     *
     * @param request IPP reqeust
     */
    @Override
    public void validateRequest(IppRequest request) {
        super.validateRequest(request);
        if (!hasTarget(request)) {
            throw new ValidationException(
                    "neither 'printer-uri' & 'job-id' nor 'job-uri' is given in " + request.toShortString());
        }
    }

    private static boolean hasTarget(IppRequest request) {
        return (request.hasAttribute("printer-uri") && request.hasAttribute("job-id"))
                || request.hasAttribute("job-uri");
    }

}
