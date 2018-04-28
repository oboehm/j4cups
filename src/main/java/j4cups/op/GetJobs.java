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
 * (c)reated 28.04.18 by oliver (ob@oasd.de)
 */

package j4cups.op;

import j4cups.protocol.IppOperations;
import j4cups.protocol.IppRequest;
import j4cups.protocol.attr.Attribute;
import j4cups.protocol.tags.ValueTags;

/**
 * Class GetJobs.
 *
 * @author oliver
 * @since 28.04.18
 */
public class GetJobs extends Operation {

    /**
     * Instantiates an operation for 'get-jobs'.
     */
    public GetJobs() {
        super(IppOperations.GET_JOBS, createIppGetJobsRequest());
    }

    private static IppRequest createIppGetJobsRequest() {
        IppRequest request = createIppRequest(IppOperations.GET_JOBS);
        Attribute requestedAttributes = Attribute.of(ValueTags.KEYWORD, "requested-attributes", "job-id",
                "job-impressions-completed", "job-media-sheets-completed", "job-name", "job-originating-user-name",
                "job-state", "job-state-reasons");
        request.setOperationAttribute(requestedAttributes);
        return request;
    }

}
