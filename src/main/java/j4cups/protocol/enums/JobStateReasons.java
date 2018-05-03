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
 * (c)reated 03.05.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This REQUIRED attribute provides additional information about the
 * Job's current state. I.e., information that augments the value of the
 * Job's "job-state" attribute.
 * <p></p>
 * These values MAY be used with any Job state or states for which the
 * reason makes sense.  Some of these value definitions indicate
 * conformance requirements; the rest are OPTIONAL.  Furthermore, when
 * implemented, the Printer MUST return these values when the reason
 * applies and MUST NOT return them when the reason no longer applies,
 * whether the value of the Job's "job-state" attribute changed or not.
 * When the Job does not have any reasons for being in its current
 * state, the value of the Job's "job-state-reasons" attribute MUST be
 * 'none'.
 * 
 * @since 0.5
 */
public enum JobStateReasons {

    /**
     * There are no reasons for the Job's current state.  This
     * state reason is semantically equivalent to "job-state-reasons"
     * without any value and MUST be used when there is no other value,
     * since the '1setOf' attribute syntax requires at least one value.
     */
    NONE,

    /**
     * Either (1) the Printer has accepted the Create-Job
     * operation and is expecting additional Send-Document and/or
     * Send-URI operations or (2) the Printer is retrieving/accepting
     * Document data as a result of a Print-Job, Print-URI,
     * Send-Document, or Send-URI operation.
     */
    JOB_INCOMING;

    private static final Logger LOG = LoggerFactory.getLogger(JobStateReasons.class);

    /**
     * This implementation generates the same representation as described in
     * section RFC-8011 (section 5.3.8).
     *
     * @return e.g. "job-incoming"
     */
    @Override
    public String toString() {
        return super.toString().toLowerCase().replaceAll("_", "-");
    }

    /**
     * Maps the given string to the corresponding element. If it cannot be
     * mapped NONE will be returned.
     * 
     * @param value e.g. "job-incoming"
     * @return e.g. JOB_INCOMING
     */
    public static JobStateReasons of(String value) {
        for (JobStateReasons jsr : JobStateReasons.values()) {
            if (value.equalsIgnoreCase(jsr.toString())) {
                return jsr;
            }
        }
        LOG.warn("'{}' is unknown and will be mapped to 'NONE'", value);
        return NONE;
    }

}
