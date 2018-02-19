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
 * (c)reated 18.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol.enums;

/**
 * The JobState describes the IPP Job Life Cycle. Normally, a Job
 * progresses from left to right.  Other value transitions are unlikely
 * but are not forbidden.  Not shown are the transitions to the
 * 'canceled' value from the 'pending', 'pending-held', and
 * 'processing-stopped' states.
 * <pre>
 * +----&gt; canceled
 * /
 * +----&gt; pending  -------&gt; processing ---------+------&gt; completed
 * |         ^                   ^               \
 * ---&gt;+     |                   |                +----&gt; aborted
 * |         v                   v               /
 * +----&gt; pending-held    processing-stopped ---+
 * </pre>
 *
 * @author oboehm
 * @since 0.2 (18.02.2018)
 */
public enum JobState {

    /**
     * The Job is a candidate to start processing    
     * but is not yet processing.
     */
    PENDING(3),

    /**
     * The Job is not a candidate for
     * processing for any number of reasons but will return to
     * the 'pending' value as soon as the reasons are no longer
     * present.  The Job's "job-value-reasons" attribute MUST
     * indicate why the Job is no longer a candidate for
     * processing.     
     */
    PENDING_HELD(4),

    /**
     * One or more of the following: (1) the Job
     * is using, or is attempting to use, one or more purely
     * software processes that are analyzing, creating, or
     * interpreting a PDL, etc.; (2) the Job is using, or is
     * attempting to use, one or more hardware devices that are
     * interpreting a PDL; making marks on a medium; and/or
     * performing finishing, such as stapling, etc.; (3) the
     * Printer has made the Job ready for printing, but the
     * Output Device is not yet printing it, either because the
     * Job hasn't reached the Output Device or because the Job
     * is queued in the Output Device or some other spooler,
     * waiting for the Output Device to print it.  When the Job
     * is in the 'processing' value, the entire Job value
     * includes the detailed status represented in the
     * Printer's "printer-value", "printer-value-reasons", and
     * "printer-value-message" attributes.  Implementations MAY
     * include additional values in the Job's "job-value-
     * reasons" attribute to indicate the progress of the Job,
     * such as adding the 'job-printing' value to indicate when
     * the Output Device is actually making marks on paper
     * and/or the 'processing-to-stop-point' value to indicate
     * that the Printer is in the process of canceling or
     * aborting the Job.
     */
    PROCESSING(5),
    
    /**
     * The Job has stopped while
     * processing for any number of reasons and will return to
     * the 'processing' value as soon as the reasons are no
     * longer present.  The Job's "job-value-reasons" attribute
     * MAY indicate why the Job has stopped processing.  For
     * example, if the Output Device is stopped, the 'printer-
     * stopped' value MAY be included in the Job's "job-value-
     * reasons" attribute.  Note: When an Output Device is
     * stopped, the device usually indicates its condition in
     * human-readable form locally at the device.  A Client can
     * obtain more complete device status remotely by querying
     * the Printer's "printer-value", "printer-value-reasons",
     * and "printer-value-message" attributes.
     */
    PROCESSING_STOPPED(6),

    /**
     * The Job has been canceled by a Cancel-Job
     * operation, and the Printer has completed canceling the
     * Job.  All Job Status attributes have reached their final
     * values for the Job.  While the Printer is canceling the
     * Job, the Job remains in its current value, but the Job's
     * "job-value-reasons" attribute SHOULD contain the
     * 'processing-to-stop-point' value and one of the
     * 'canceled-by-user', 'canceled-by-operator', or
     * 'canceled-at-device' values.  When the Job moves to the
     * 'canceled' value, the 'processing-to-stop-point' value,
     * if present, MUST be removed, but 'canceled-by-xxx', if
     * present, MUST remain.
     */
    CANCELED(7),

    /**
     * The Job has been aborted by the system,
     * usually while the Job was in the 'processing' or
     * 'processing-stopped' value, and the Printer has
     * completed aborting the Job; all Job Status attributes
     * have reached their final values for the Job.  While the
     * Printer is aborting the Job, the Job remains in its
     * current value, but the Job's "job-value-reasons"
     * attribute SHOULD contain the 'processing-to-stop-point'
     * and 'aborted-by-system' values.  When the Job moves to
     * the 'aborted' value, the 'processing-to-stop-point'
     * value, if present, MUST be removed, but the 'aborted-by-
     * system' value, if present, MUST remain.
     */
    ABORTED(8),

    /**
     * The Job has completed successfully or with 
     * warnings or errors after processing, all of the Job
     * Media Sheets have been successfully stacked in the
     * appropriate output bin(s), and all Job Status attributes
     * have reached their final values for the Job.  The Job's
     * "job-value-reasons" attribute SHOULD contain one of the
     * 'completed-successfully', 'completed-with-warnings', or
     * 'completed-with-errors' values.
     */
    COMPLETED(9);

    private final int value;

    JobState(int value) {
        this.value = value;
    }

    /**
     * Gets the represenation of the value as 32 bit integer.
     * 
     * @return number between 3 and 9
     */
    public int getValue() {
        return value;
    }

    /**
     * Allows you to map a int value to the corresponding value.
     *
     * @param value e.g. 5
     * @return operation, e.g. PROCESSING
     */
    public static JobState of(int value) {
        for (JobState js : JobState.values()) {
            if (value == js.getValue()) {
                return js;
            }
        }
        throw new IllegalArgumentException("invalid value: " + value);
    }

}
