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
 * (c)reated 09.02.2018 by oboehm (boehm@javatux.de)
 */

/**
 * This package represents the which are described in 
 * <a href="https://tools.ietf.org/html/rfc2910#section-3.5">section 3.5</a>
 * of RFC-2910. There are two kinds of tags:
 * <ol>
 *     <li>
 *         delimiter tags: delimit major sections of the protocol, namely
 *         attributes and data
 *     </li>
 *     <li>
 *         value tags: specify the type of each attribute value
 *     </li>
 * </ol>
 * 
 * @since 0.0.2 (08-Feb-2018)
 */
package j4cups.protocol.tags;
