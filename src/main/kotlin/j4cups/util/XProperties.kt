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
package j4cups.util

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.util.*

/**
 * The eXtended Properties (or XProperties) is a extension of the normal
 * properties and allows variable substitution. After an idea of
 * https://stackoverflow.com/questions/8383753/reference-other-variables-in-java-properties-file
 *
 * @author oboehm
 * @since 0.6 (13.12.2018)
 */
class XProperties : Properties() {

    /**
     * If the property is unset the value of the system property will be taken
     * as default.
     *
     * @param key the key
     * @return the property
     */
    override fun getProperty(key: String): String? {
        var value = super.getProperty(key)
        if (value == null) {
            value = System.getProperty(key)
        }
        return value
    }

    /**
     * If there is a reference to another value the value will replaced
     * accordingly. Values like '{java.io.tmpdir}' will be substituted.
     *
     * @param key the key
     * @param value the value
     * @return the object
     */
    @Synchronized
    override fun put(key: Any, value: Any): Any? {
        return super.put(key, substitute(value as String))
    }

    private fun substitute(s: String): String {
        return if (StringUtils.contains(s, "{")) {
            val tokens = s.split("[{,}]".toRegex()).toTypedArray()
            substitute(tokens)
        } else {
            s
        }
    }

    private fun substitute(tokens: Array<String>): String {
        val buf = StringBuilder()
        var i = 0
        while (i < tokens.size) {
            buf.append(tokens[i])
            if (i + 1 < tokens.size) {
                buf.append(this.getProperty(tokens[i + 1]))
            }
            i += 2
        }
        return buf.toString()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(XProperties::class.java)
    }

}