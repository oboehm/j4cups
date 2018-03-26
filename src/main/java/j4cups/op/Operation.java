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
 * This is the common super class of all IPP operations.
 *
 * @author oboehm
 * @since 0.5 (26.03.2018)
 */
public class Operation {
    
    private final IppOperations id;

    /**
     * Instantiates an operation with the given id.
     * 
     * @param id a positiv number between 0x0000 and 0x0031
     */
    public Operation(IppOperations id) {
        this.id = id;
    }

    /**
     * Looks if the given bytes represents a valid request. If not an
     * {@link ValidationException} will be thrown
     * 
     * @param bytes which represents an request
     */
    public void validateRequest(byte[] bytes) {
        validateRequest(new IppRequest(bytes));
    }

    /**
     * Looks if the given request is valid. If not an
     * {@link ValidationException} will be thrown
     *
     * @param request IPP reqeust
     */
    public void validateRequest(IppRequest request) {
        if (request.getOperation() != id) {
            throw new ValidationException("not a " + id + " request:" + request);
        }
    }

}
