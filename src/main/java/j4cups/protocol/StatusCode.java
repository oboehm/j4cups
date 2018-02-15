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
 * (c)reated 14.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol;

/**
 * The status-code values range from 0x0000 to 0x7fff. The value ranges
 * for each status-code class are as follows (see RFC-8011
 * <a href="https://tools.ietf.org/html/rfc8011#appendix-B">Appendix B</a>):
 * <ul>
 *     <li>"successful" - 0x0000 to 0x00ff</li>
 *     <li>"informational" - 0x0100 to 0x01ff</li>
 *     <li>"redirection" - 0x0300 to 0x03ff</li>
 *     <li>client-error" - 0x0400 to 0x04ff</li>
 *     <li>server-error" - 0x0500 to 0x05ff</li>
 * </ul>
 * <p>
 * The top half (128 values) of each range (0x0n80 to 0x0nff, for n = 0
 * to 5) is reserved for vendor use within each status-code class.
 * Values 0x0600 to 0x7fff are reserved for future assignment by
 * Standards Track documents and MUST NOT be used.
 * </p>
 * 
 * @author oboehm
 * @since 0.2 (14.02.2018)
 */
public enum StatusCode {

    // B.1.2.  Successful Status-Code Values
    
    /**
     * The request has succeeded, and no request attributes were substituted
     * or ignored.  In the case of a response to a Job Creation request, the
     * 'successful-ok' status-code indicates that the request was
     * successfully received and validated, and that the Job object has been
     * created; it does not indicate that the Job has been processed.  The
     * transition of the Job object into the 'completed' state is the only
     * indicator that the Job has been printed.
     */
    SUCCESSFUL_OK (0x0000),
    
    /**
     * The request has succeeded, but some supplied (1) attributes were
     * ignored or (2) unsupported values were substituted with supported
     * values or were ignored in order to perform the operation without
     * rejecting it.  Unsupported attributes, attribute syntaxes, or values
     * MUST be returned in the Unsupported Attributes group of the response
     * for all operations.  There is an exception to this rule for the query
     * operations Get-Printer-Attributes, Get-Jobs, and Get-Job-Attributes
     * for the "requested-attributes" operation attribute only.  When the
     * supplied values of the "requested-attributes" operation attribute are
     * requesting attributes that are not supported, the IPP object SHOULD
     * return the "requested-attributes" operation attribute in the
     * Unsupported Attributes group of the response (with the unsupported
     * values only).
     */
    SUCCESSFUL_OK_IGNORED_OR_SUBSTITUTED_ATTRIBUTES (0x0001),

    /**
     * The request has succeeded, but some supplied attribute values
     * conflicted with the values of other supplied attributes.  Either
     * (1) these conflicting values were substituted with (supported) values
     * or (2) the attributes were removed in order to process the Job
     * without rejecting it.  Attributes or values that conflict with other
     * attributes and have been substituted or ignored MUST be returned in
     * the Unsupported Attributes group of the response for all operations
     * as supplied by the Client.
     */
    SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES (0x0002),

    // TODO: define code B.1.3 and more...

    /**
     * The IPP object does not support multiple Documents per Job, and a
     * Client attempted to supply Document data with a second Send-Document
     * or Send-URI operation.
     */
    SERVER_ERROR_MULTIPLE_DOCUMENT_JOBS_NOT_SUPPORTED (0x0509);

    private final short code;

    StatusCode(int value) {
        this.code = (short) value;
    }

    /**
     * Gets the value of the statuscode.
     *
     * @return 2-byte code from 0x0000 to 0x7fff
     */
    public short getCode() {
        return code;
    }

    /**
     * Allows you to map a value to the corresponding statuscode
     *
     * @param id e.g. 0x0000
     * @return status code, e.g. SUCCESSFUL_OK
     */
    public static StatusCode of(int id) {
        for (StatusCode op : StatusCode.values()) {
            if (id == op.getCode()) {
                return op;
            }
        }
        throw new IllegalArgumentException("invalid id: " + id);
    }
    
}
