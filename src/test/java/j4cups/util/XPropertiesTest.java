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

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link XProperties}.
 */
class XPropertiesTest {

    private final XProperties xprops = new XProperties();

    @Test
    void testVarSubstitutionOfSystemProperty() {
        xprops.setProperty("tmpdir", "{java.io.tmpdir}");
        assertEquals(SystemUtils.JAVA_IO_TMPDIR, xprops.getProperty("tmpdir"));
    }

    @Test
    void testVarSubstitution() {
        xprops.setProperty("hi", "Hello");
        xprops.setProperty("greet", "{hi} World!");
        assertEquals("Hello World!", xprops.getProperty("greet"));
    }

}
