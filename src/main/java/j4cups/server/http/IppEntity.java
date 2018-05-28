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

package j4cups.server.http;

import j4cups.protocol.AbstractIpp;
import j4cups.protocol.IppRequest;
import j4cups.protocol.IppResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class IppEntity.
 *
 * @author oliver
 * @since 0.5
 */
public class IppEntity extends ByteArrayEntity {

    /**
     * Instantiates a new entity.
     *
     * @param ippRequest IPP request or response
     */
    public IppEntity(AbstractIpp ippRequest) {
        this(ippRequest.toByteArray());
    }

    /**
     * Instantiates a new entity.
     *
     * @param content IPP request or response
     */
    public IppEntity(byte[] content) {
        super(content);
        setContentType("application/ipp");
    }

    /**
     * This is a utility method to convert an {@link HttpEntityEnclosingRequest} to an
     * {@link IppRequest}.
     *
     * @param request the {@link HttpEntityEnclosingRequest}
     * @return the {@link IppRequest} inside
     */
    public static IppRequest toIppRequest(HttpEntityEnclosingRequest request) {
        HttpEntity entity = request.getEntity();
        try {
            byte[] entityContent = EntityUtils.toByteArray(entity);
            return new IppRequest(entityContent);
        } catch (IOException ioe) {
            throw new IllegalStateException("cannot read content from " + request, ioe);
        }
    }

    /**
     * This is a utility method to convert an {@link HttpResponse} to an
     * {@link IppResponse}.
     *
     * @param response the {@link HttpResponse}
     * @return the {@link IppResponse} inside
     */
    public static IppResponse toIppResponse(HttpResponse response) {
        try (InputStream istream = response.getEntity().getContent()) {
            byte[] content = IOUtils.toByteArray(istream);
            response.setEntity(new IppEntity(content));
            return new IppResponse(content);
        } catch (IOException ioe) {
            throw new IllegalStateException("cannot read content from " + response, ioe);
        }
    }

}
