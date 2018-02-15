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
    
    // B.1.4.  Client Error Status-Code Values

    /**
     * The request could not be understood by the IPP object due to
     * malformed syntax (such as the value of a fixed-length attribute whose
     * length does not match the prescribed length for that attribute -- see
     * the Implementor's Guides [RFC3196] [PWG5100.19]).  The IPP
     * application SHOULD NOT repeat the request without modifications.
     */
    CLIENT_ERROR_BAD_REQUEST (0X0400),

    /**
     * The IPP object understood the request but is refusing to fulfill it.
     * Additional authentication information or authorization credentials
     * will not help, and the request SHOULD NOT be repeated.  This
     * status-code is commonly used when the IPP object does not wish to
     * reveal exactly why the request has been refused or when no other
     * response is applicable.
     */
    CLIENT_ERROR_FORBIDDEN (0X0401),

    /**
     * The request requires user authentication.  The IPP Client can repeat
     * the request with suitable authentication information.  If the request
     * already included authentication information, then this status-code
     * indicates that authorization has been refused for those credentials.
     * If this response contains the same challenge as the prior response
     * and the user agent has already attempted authentication at least
     * once, then the response message can contain relevant diagnostic
     * information.  This status-code reveals more information than
     * 'client-error-forbidden'.
     */
    CLIENT_ERROR_NOT_AUTHENTICATED (0X0402),

    /**
     * The requester is not authorized to perform the request.  Additional
     * authentication information or authorization credentials will not
     * help, and the request SHOULD NOT be repeated.  This status-code is
     * used when the IPP object wishes to reveal that the authentication
     * information is understandable; however, the requester is explicitly
     * not authorized to perform the request.  This status-code reveals more
     * information than 'client-error-forbidden' and
     * 'client-error-not-authenticated'.
     */
    CLIENT_ERROR_NOT_AUTHORIZED (0X0403),

    /**
     * This status-code is used when the request is for something that
     * cannot happen.  For example, there might be a request to cancel a Job
     * that has already been canceled or aborted by the system.  The IPP
     * Client SHOULD NOT repeat the request.
     */
    CLIENT_ERROR_NOT_POSSIBLE (0X0404),

    // TODO: define code B.1.4.6 and more...

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
