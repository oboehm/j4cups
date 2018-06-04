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
 * (c)reated 08.02.2018 by oboehm (ob@oasd.de)
 */
package j4cups.protocol;


import org.apache.commons.text.WordUtils;

/**
 * The enum IppOperations represents the IPP operations which are described
 * in RFC-8011.
 * <pre>
 * +---------------+---------------------------------------------------+
 * | Value         | Operation Name                                    |
 * +---------------+---------------------------------------------------+
 * | 0x0000        | reserved, not used                                |
 * +---------------+---------------------------------------------------+
 * | 0x0001        | reserved, not used                                |
 * +---------------+---------------------------------------------------+
 * | 0x0002        | Print-Job                                         |
 * +---------------+---------------------------------------------------+
 * | 0x0003        | Print-URI                                         |
 * +---------------+---------------------------------------------------+
 * | 0x0004        | Validate-Job                                      |
 * +---------------+---------------------------------------------------+
 * | 0x0005        | Create-Job                                        |
 * +---------------+---------------------------------------------------+
 * | 0x0006        | Send-Document                                     |
 * +---------------+---------------------------------------------------+
 * | 0x0007        | Send-URI                                          |
 * +---------------+---------------------------------------------------+
 * | 0x0008        | Cancel-Job                                        |
 * +---------------+---------------------------------------------------+
 * | 0x0009        | Get-Job-Attributes                                |
 * +---------------+---------------------------------------------------+
 * | 0x000a        | Get-Jobs                                          |
 * +---------------+---------------------------------------------------+
 * | 0x000b        | Get-Printer-Attributes                            |
 * +---------------+---------------------------------------------------+
 * | 0x000c        | Hold-Job                                          |
 * +---------------+---------------------------------------------------+
 * | 0x000d        | Release-Job                                       |
 * +---------------+---------------------------------------------------+
 * | 0x000e        | Restart-Job                                       |
 * +---------------+---------------------------------------------------+
 * | 0x000f        | reserved for a future operation                   |
 * +---------------+---------------------------------------------------+
 * | 0x0010        | Pause-Printer                                     |
 * +---------------+---------------------------------------------------+
 * | 0x0011        | Resume-Printer                                    |
 * +---------------+---------------------------------------------------+
 * | 0x0012        | Purge-Jobs                                        |
 * +---------------+---------------------------------------------------+
 * | 0x0013-0x3fff | additional registered operations (see the IANA    |
 * |               | IPP registry and Section 7.8)                     |
 * +---------------+---------------------------------------------------+
 * | 0x4000-0x7fff | reserved for vendor extensions (see Section 7.8)  |
 * +---------------+---------------------------------------------------+
 * </pre>
 * aus https://tools.ietf.org/html/rfc8011#section-5.4.15  
 *
 * @author oboehm
 * @since 0.0.1 (08.02.2018)
 */
public enum IppOperations {
    
    /** Reserved, not used. */
    RESERVED_0(0x0000),
    
    /** Reserved, not used. */
    RESERVED_1(0x0001),
    
    /** Print a file. */
    PRINT_JOB(0x0002),
    
    /** Print URI. */
    PRINT_URI(0x0003),

    /** Validate job attributes. */
    VALIDATE_JOB(0x0004),
    
    /** Create a print job. */
    CREATE_JOB(0x0005),
    
    /* Send a file for a print job. */
    SEND_DOCUMENT(0x0006),
    
    /* Send URI. */
    SEND_URI(0x0007),
    
    /* Cancel a print job. */
    CANCEL_JOB(0x0008),
    
    /* Get job attributes. */
    GET_JOB_ATTRIBUTES(0x0009),
    
    /* Get all jobs. */
    GET_JOBS(0x000a),
    
    /* Get printer attributes. */
    GET_PRINTER_ATTRIBUTES(0x000b),
    
    /* Hold a job for printing. */
    HOLD_JOB(0x000c),
    
    /* Release a job for printing. */
    RELEASE_JOB(0x000b),
    
    /* Restarts a print job. */
    RESTART_JOB(0x000e),
    
    /* Pause printing on a printer. */
    PAUSE_PRINTER(0x0010),
    
    /* Resume printing on a printer. */
    RESUME_PRINTER(0x0011),
    
    /* Purge all jobs. */
    PURGE_JOBS(0x0012),
    
    /* Set attributes for a pending or held job. */
    SET_JOB_ATTRIBUTES(0x0014),
    
    /* Creates a subscription associated with a printer or the server. */
    CREATE_PRINTER_SUBSCRIPTION(0x0016),
    
    /* Creates a subscription associated with a job. */
    CREATE_JOB_SUBSCRIPTION(0x0017),
    
    /* Gets the attributes for a subscription. */
    GET_SUBSCRIPTION_ATTRIBUTES(0x0018),
    
    /* Gets the attributes for zero or more subscriptions. */
    GET_SUBSCRIPTIONS(0x0019),
    
    /* Renews a subscription. */
    RENEW_SUBSCRIPTION(0x001a),
    
    /* Cancels a subscription. */
    CANCEL_SUBSCRIPTION(0x001b),
    
    /* Get notification events for ippget subscriptions. */
    GET_NOTIFICATIONS(0x001c),
    
    /* Accepts jobs on a printer. */
    ENABLE_PRINTER(0x0022),
    
    /* Rejects jobs on a printer. */
    DISABLE_PRINTER(0x0023),
 
    /* Additional registered operations (0x0013-0x3fff, see Section 7.8 of RFC-8011). */
    ADDITIONAL_REGISTERED_OPERATIONS(0x3fff),

    /* Get the printer list from CUPS. */
    GET_PRINTERS(0x4002),
    
    /* Reserved for vendor extensions (0x4000-0x7fff, see Section 7.8 of RFC-8011).  */
    RESERVED_FOR_VENDOR_EXTENSIONS(0x7fff);

    private final short code;

    IppOperations(int value) {
        this.code = (short) value;
    }

    /**
     * Gets the operation id or op-code of the operation.
     * 
     * @return 2-byte code from 0x0000 to 0x7fff
     */
    public short getCode() {
        return code;
    }
    
    /**
     * This implementation generates the same representation as described in
     * section RFC-8011 (section 5.4.15).
     *
     * @return e.g. "Print-Job"
     */
    @Override
    public String toString() {
        String s = super.toString().toLowerCase().replaceAll("_", " ");
        return WordUtils.capitalize(s).replaceAll(" ", "-");
    }
    
    /**
     * Allows you to map an id (or op-code) to the corresponding operation.
     * 
     * @param id e.g. 0x0002
     * @return operation, e.g. PRINT_JOB
     */
    public static IppOperations of(int id) {
        for (IppOperations op : IppOperations.values()) {
            if (id == op.getCode()) {
                return op;
            }
        }
        if ((0x0013 <= id) && (id <= 0x3fff)) {
            return ADDITIONAL_REGISTERED_OPERATIONS;
        }
        if ((0x4000 <= id) && (id <= 0x7fff)) {
            return RESERVED_FOR_VENDOR_EXTENSIONS;
        }
        throw new IllegalArgumentException("invalid id: 0x" + Integer.toHexString(id));
    }

}
