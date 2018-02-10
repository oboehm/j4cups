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
 * (c)reated 10.02.2018 by oboehm (boehm@javatux.de)
 */
package j4cups.protocol;

import j4cups.protocol.tags.DelimiterTags;

import java.nio.ByteBuffer;

/**
 * Each "attribute-group" field represents a single group of
 * attributes, such as an Operation Attributes group or a Job Attributes
 * group (see the Model document). The IPP model document specifies the
 * required attribute groups and their order for each operation request
 * and response.
 * <p>
 * Each "attribute-group" field is encoded as follows:
 * </p>
 * <pre>
 * -----------------------------------------------
 * |           begin-attribute-group-tag         |  1 byte
 * ----------------------------------------------------------
 * |                   attribute                 |  p bytes |- 0 or more
 * ----------------------------------------------------------
 * </pre>
 *
 * @author oboehm
 * @since 0.0.2 (10.02.2018)
 */
public class AttributeGroup {
    
    private final ByteBuffer bytes;

    /**
     * Instantiates a new attribute group from the given bytes.
     *
     * @param bytes the bytes of attribute group
     */
    public AttributeGroup(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    /**
     * Instantiates a new attribute group from the given bytes.
     *
     * @param bytes the bytes of attribute group
     */
    public AttributeGroup(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    /**
     * The "begin-attribute-group-tag" field marks the beginning of an
     * "attribute-group" field and its value identifies the type of
     * attribute group, e.g. Operations Attributes group versus a Job
     * Attributes group.
     * 
     * @return e.g. {@link DelimiterTags#OPERATIONS_ATTRIBUTES_TAG}
     */
    public DelimiterTags getBeginTag() {
        return DelimiterTags.of(bytes.get(0));
    }
    
}
