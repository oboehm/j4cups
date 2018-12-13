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
 * (c)reated 13.12.2018 by oboehm (ob@oasd.de)
 */
package j4cups.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * The eXtended Properties (or XProperties) is a extension of the normal
 * properties and allows variable substitution. After an idea of
 * https://stackoverflow.com/questions/8383753/reference-other-variables-in-java-properties-file
 *
 * @author oboehm
 * @since 0.6 (13.12.2018)
 */
public final class XProperties extends Properties {

    private static final Logger LOG = LoggerFactory.getLogger(XProperties.class);

    /**
     * If the property is unset the value of the system property will be taken
     * as default.
     *
     * @param key the key
     * @return the property
     */
    @Override
    public String getProperty(String key) {
        String value = super.getProperty(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }

    /**
     * If there is a reference to another value the value will replaced
     * accordingly.
     *
     * @param key the key
     * @param value the value
     * @return the object
     */
    @Override
    public Object put(Object key, Object value) {
        if (value == null) {
            LOG.info("Put of '{}=null' is ignored.", key);
            return null;
        } else {
            return super.put(key, substitute((String) value));
        }
    }

    /**
     * Variables like '{java.io.tmpdir}' will be substituted.
     *
     * @param s the value
     * @return the string
     */
    private String substitute(String s) {
        if (StringUtils.contains(s, "{")) {
            String[] tokens = s.split("[{,}]");
            return substitute(tokens);
        } else {
            return s;
        }
    }

    private String substitute(String[] tokens) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < tokens.length; i += 2) {
            buf.append(tokens[i]);
            if (i+1 < tokens.length) {
                buf.append(this.getProperty(tokens[i+1]));
            }
        }
        return buf.toString();
    }

}
