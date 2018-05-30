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
    
    /**
     * The Client did not produce a request within the time that the IPP
     * object was prepared to wait.  For example, a Client issued a
     * Create-Job operation and then, after a long period of time, issued a
     * Send-Document operation; this error status-code was returned in
     * response to the Send-Document request (see Section 4.3.1).  The IPP
     * object might have been forced to clean up resources that had been
     * held for the waiting additional Documents.  The IPP object was forced
     * to close the Job, since the Client took too long.  The Client
     * SHOULD NOT repeat the request without modifications.
     */
    CLIENT_ERROR_TIMEOUT (0x0405),

    /**
     * The IPP object has not found anything matching the request URI.  No
     * indication is given of whether the condition is temporary or
     * permanent.  For example, a Client with an old reference to a Job
     * (a URI) tries to cancel the Job; however, in the meantime the Job
     * might have been completed and all record of it at the Printer has
     * been deleted.  This status-code, 'client-error-not-found', is
     * returned indicating that the referenced Job cannot be found.  This
     * error status-code is also used when a Client supplies a URI as a
     * reference to the Document data in either a Print-URI or Send-URI
     * operation but the Document cannot be found.
     * <p>
     * In practice, an IPP application should avoid a "not found" situation
     * by first querying and presenting a list of valid Printer URIs and Job
     * URIs to the End User.
     * </p>
     */
    CLIENT_ERROR_NOT_FOUND (0x0406),

    /**
     * The requested object is no longer available, and no forwarding
     * address is known.  This condition should be considered permanent.
     * Clients with link-editing capabilities should delete references to
     * the request URI after user approval.  If the IPP object does not know
     * or has no facility to determine whether or not the condition is
     * permanent, the status-code 'client-error-not-found' should be used
     * instead.
     * <p>
     * This response is primarily intended to assist the task of maintenance
     * by notifying the recipient that the resource is intentionally
     * unavailable and that the IPP object Administrator desires that remote
     * links to that resource be removed.  It is not necessary to mark all
     * permanently unavailable resources as "gone" or to keep the mark for
     * any length of time -- that is left to the discretion of the IPP
     * object Administrator and/or Printer implementation.
     * </p>
     */
    CLIENT_ERROR_GONE (0x0407),

    /**
     * The IPP object is refusing to process a request because the request
     * entity is larger than the IPP object is willing or able to process.
     * An IPP Printer returns this status-code when it limits the size of
     * Print Jobs and it receives a Print Job that exceeds that limit or
     * when the attributes are so many that their encoding causes the
     * request entity to exceed IPP object capacity.
     */
    CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE (0x0408),

    /**
     * The IPP object is refusing to service the request because one or more
     * of the Client-supplied attributes have a variable-length value that
     * is longer than the maximum length specified for that attribute.  The
     * IPP object might not have sufficient resources (memory, buffers,
     * etc.) to process (even temporarily), interpret, and/or ignore a value
     * larger than the maximum length.  Another use of this error code is
     * when the IPP object supports the processing of a large value that is
     * less than the maximum length, but during the processing of the
     * request as a whole, the object can pass the value onto some other
     * system component that is not able to accept the large value.  For
     * more details, see the Implementor's Guides [RFC3196] [PWG5100.19].
     * <p>
     * Note: For attribute values that are URIs, this rare condition is only
     * likely to occur when a Client has improperly submitted a request with
     * long query information (e.g., an IPP application allows an End User
     * to enter an invalid URI), when the Client has descended into a URI
     * "black hole" of redirection (e.g., a redirected URI prefix that
     * points to a suffix of itself), or when the IPP object is under attack
     * by a Client attempting to exploit security holes present in some IPP
     * objects using fixed-length buffers for reading or manipulating the
     * request URI.
     * </p>
     */
    CLIENT_ERROR_REQUEST_VALUE_TOO_LONG (0x0409),

    /**
     * The IPP object is refusing to service the request because the
     * Document data is in a format, as specified in the "document-format"
     * operation attribute, that is not supported by the Printer.  This
     * error is returned independent of the Client-supplied
     * "ipp-attribute-fidelity" attribute.  The Printer MUST return this
     * status-code, even if there are other Job Template attributes that are
     * not supported as well, since this error is a bigger problem than with
     * Job Template attributes.  See Sections 4.1.6.1, 4.1.7, and 4.2.1.1.
     */
    CLIENT_ERROR_DOCUMENT_FORMAT_NOT_SUPPORTED (0x040a),
        
    /**
     * 
     * In a Job Creation request, if the Printer does not support one or
     * more attributes, attribute syntaxes, or attribute values supplied in
     * the request and the Client supplied the "ipp-attribute-fidelity"
     * operation attribute with the 'true' value, the Printer MUST return
     * this status-code.  The Printer MUST also return in the Unsupported
     * Attributes group all the attributes and/or values supplied by the
     * Client that are not supported.  See Section 4.1.7.  Examples would be
     * if the request indicates 'iso-a4' media but that media type is not
     * supported by the Printer, or if the Client supplies a Job Template
     * attribute and the attribute itself is not even supported by the
     * Printer.  If the "ipp-attribute-fidelity" attribute is 'false', the
     * Printer MUST ignore or substitute values for unsupported Job Template
     * attributes and values rather than reject the request and return this
     * status-code.
     * <p>
     * For any operation where a Client requests attributes (such as a
     * Get-Jobs, Get-Printer-Attributes, or Get-Job-Attributes operation),
     * if the IPP object does not support one or more of the requested
     * attributes, the IPP object simply ignores the unsupported requested
     * attributes and processes the request as if they had not been
     * supplied, rather than returning this status-code.  In this case,
     * the IPP object MUST return the
     * 'successful-ok-ignored-or-substituted-attributes' status-code and
     * SHOULD return the unsupported attributes as values of the
     * "requested-attributes" operation attribute in the Unsupported
     * Attributes group (see Appendix B.1.2.2).
     * </p>
     */
    CLIENT_ERROR_ATTRIBUTES_OR_VALUES_NOT_SUPPORTED (0x040b),
        
    /**
     * The scheme of the Client-supplied URI in a Print-URI or a Send-URI
     * operation is not supported.  See Sections 4.1.6.1 and 4.1.7.
     */
    CLIENT_ERROR_URI_SCHEME_NOT_SUPPORTED (0x040c),
    
    /**
     * 
     * For any operation, if the IPP Printer does not support the charset
     * supplied by the Client in the "attributes-charset" operation
     * attribute, the Printer MUST reject the operation and return this
     * status-code, and any 'text' or 'name' attributes using the 'utf-8'
     * charset (Section 4.1.4.1).  See Sections 4.1.6.1 and 4.1.7.
     */
    CLIENT_ERROR_CHARSET_NOT_SUPPORTED (0x040d),
    /**
     * The request is rejected because some attribute values conflicted with
     * the values of other attributes that this document does not permit to
     * be substituted or ignored.  The Printer MUST also return in the
     * Unsupported Attributes group the conflicting attributes supplied by
     * the Client.  See Sections 4.1.7 and 4.2.1.2.
     */
    CLIENT_ERROR_CONFLICTING_ATTRIBUTES (0x040e),
    
    /**
     * The IPP object is refusing to service the request because the
     * Document data, as specified in the "compression" operation attribute,
     * is compressed in a way that is not supported by the Printer.  This
     * error is returned independent of the Client-supplied
     * "ipp-attribute-fidelity" attribute.  The Printer MUST return this
     * status-code, even if there are other Job Template attributes that are
     * not supported as well, since this error is a bigger problem than with
     * Job Template attributes.  See Sections 4.1.6.1, 4.1.7, and 4.2.1.1.
     */
    CLIENT_ERROR_COMPRESSION_NOT_SUPPORTED (0x040f),

    /**
     * The IPP object is refusing to service the request because the
     * Document data cannot be decompressed when using the algorithm
     * specified by the "compression" operation attribute.  This error is
     * returned independent of the Client-supplied "ipp-attribute-fidelity"
     * attribute.  The Printer MUST return this status-code, even if there
     * are Job Template attributes that are not supported as well, since
     * this error is a bigger problem than with Job Template attributes.
     * See Sections 4.1.7 and 4.2.1.1.
     */
    CLIENT_ERROR_COMPRESSION_ERROR (0x0410),
   
    /**
     * The IPP object is refusing to service the request because the Printer
     * encountered an error in the Document data while interpreting it.
     * This error is returned independent of the Client-supplied
     * "ipp-attribute-fidelity" attribute.  The Printer MUST return this
     * status-code, even if there are Job Template attributes that are not
     * supported as well, since this error is a bigger problem than with Job
     * Template attributes.  See Sections 4.1.7 and 4.2.1.1.
     */
    CLIENT_ERROR_DOCUMENT_FORMAT_ERROR (0x0411),
    
    /**
     * 
     * The IPP object is refusing to service the Print-URI or Send-URI
     * request because the Printer encountered an access error while
     * attempting to validate the accessibility of, or access to, the
     * Document data specified in the "document-uri" operation attribute.
     * The Printer MAY also return a specific Document access error code
     * using the "document-access-error" operation attribute (see
     */
    CLIENT_ERROR_DOCUMENT_ACCESS_ERROR (0x0412),
    
    // B.1.5.  Server Error Status-Code Values

    /**
     * The IPP object encountered an unexpected condition that prevented it
     * from fulfilling the request.  This error status-code differs from
     * 'server-error-temporary-error' in that it implies a more permanent
     * type of internal error.  It also differs from
     * 'server-error-device-error' in that it implies an unexpected
     * condition (unlike a paper-jam or out-of-toner problem, which is
     * undesirable but expected).  This error status-code indicates that
     * intervention by a knowledgeable human is probably required.
     */
    SERVER_ERROR_INTERNAL_ERROR (0x0500),
    
    /**
     * The IPP object does not support the functionality required to fulfill
     * the request.  This is the appropriate response when the IPP object
     * does not recognize an operation or is not capable of supporting it.
     * See Sections 4.1.6.1 and 4.1.7.
     */
    SERVER_ERROR_OPERATION_NOT_SUPPORTED (0x0501),

    /**
     * The IPP object is currently unable to handle the request due to
     * temporary overloading or due to maintenance of the IPP object.  The
     * implication is that this is a temporary condition that will be
     * alleviated after some delay.  If known, the length of the delay can
     * be indicated in the message.  If no delay is given, the IPP
     * application should handle the response as it would for a
     * 'server-error-temporary-error' response.  If the condition is more
     * permanent, the 'client-error-gone' or 'client-error-not-found' error
     * status-code could be used.
     */
    SERVER_ERROR_SERVICE_UNAVAILABLE (0x0502),

    /**
     * The IPP object does not support or refuses to support the IPP version
     * that was supplied as the value of the "version-number" operation
     * parameter in the request.  The IPP object is indicating that it is
     * unable or unwilling to complete the request using the same major and
     * minor version number as supplied in the request, other than with this
     * error message.  The error response SHOULD contain a "status-message"
     * attribute (see Section 4.1.6.2) describing why that version is not
     * supported and what other versions are supported by that IPP object.
     * See Sections 4.1.6.1, 4.1.7, and 4.1.8.
     * <p>
     * The error response MUST identify in the "version-number" operation
     * parameter the closest version number that the IPP object does
     * support.  For example, if a Client supplies version '1.0' and an
     * IPP/1.1 object supports version '1.0', then it responds with
     * version '1.0' in all responses to such a request.  If the IPP/1.1
     * object does not support version '1.0', then it should accept the
     * request and respond with version '1.1' or can reject the request and
     * respond with this error code and version '1.1'.  If a Client supplies
     * version '1.2', the IPP/1.1 object should accept the request and
     * return version '1.1' or can reject the request and respond with this
     * error code and version '1.1'.  See Sections 4.1.8 and 5.3.14.
     * </p>
     */
    SERVER_ERROR_VERSION_NOT_SUPPORTED (0x0503),

    /**
     * A Printer error, such as a paper jam, occurs while the IPP object
     * processes a Print or send operation.  The response contains the true
     * Job status (the values of the "job-state" and "job-state-reasons"
     * attributes).  Additional information can be returned in the OPTIONAL
     * "job-state-message" attribute value or in the OPTIONAL status message
     * that describes the error in more detail.  This error status-code is
     * only returned in situations where the Printer is unable to accept the
     * Job Creation request because of such a device error.  For example, if
     * the Printer is unable to spool and can only accept one Job at a time,
     * the reason it might reject a Job Creation request is that the Printer
     * currently has a paper jam.  In many cases, however, where the Printer
     * can accept the request even though the Printer has some error
     * condition, the 'successful-ok' status-code will be returned.  In such
     * a case, the Client would look at the returned Job object attributes
     * or later query the Printer to determine its state and state reasons.
     */
    SERVER_ERROR_DEVICE_ERROR (0x0504),


    /**
     * A temporary error such as a buffer-full write error, a memory
     * overflow (i.e., the Document data exceeds the memory of the Printer),
     * or a disk-full condition, occurs while the IPP Printer processes an
     * operation.  The Client MAY try the unmodified request again at some
     * later point in time with an expectation that the temporary internal
     * error condition has been cleared.  Alternatively, as an
     * implementation option, a Printer MAY delay the response until the
     * temporary condition is cleared so that no error is returned.
     */
   SERVER_ERROR_TEMPORARY_ERROR (0x0505),

    /**
     * This is a temporary error indicating that the Printer is not
     * currently accepting Jobs because the Administrator has set the value
     * of the Printer's "printer-is-accepting-jobs" attribute to 'false' (by
     * means outside the scope of this IPP/1.1 document).
     */
    SERVER_ERROR_NOT_ACCEPTING_JOBS (0x0506),

    /**
     * This is a temporary error indicating that the Printer is too busy
     * processing Jobs and/or other requests.  The Client SHOULD try the
     * unmodified request again at some later point in time with an
     * expectation that the temporary busy condition will have been cleared.
     */
    SERVER_ERROR_BUSY (0x0507),

    /**
     * This is an error indicating that the Job has been canceled by an
     * Operator or the system while the Client was transmitting the data to
     * the IPP Printer.  If a "job-id" attribute and a "job-uri" attribute
     * had been created, then they are returned in the Print-Job,
     * Send-Document, or Send-URI response as usual; otherwise, no "job-id"
     * and "job-uri" attributes are returned in the response.
     */
    SERVER_ERROR_JOB_CANCELED (0x0508),

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
     * For the constants which represents an succesful state this method
     * returns true.
     * 
     * @return true for the SUCCESFUL_xxx constants, otherwise false
     * @since 0.5
     */
    public boolean isSuccessful() {
        return toString().startsWith("successful");
    }

    /**
     * This implementation generates the same representation as described in
     * section RFC-8011 (section 5.4.15).
     *
     * @return e.g. "successful-ok"
     */
    @Override
    public String toString() {
        return super.toString().toLowerCase().replaceAll("_", "-");
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
