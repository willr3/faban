/* The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html or
 * install_dir/legal/LICENSE
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at faban/src/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: CookieHandler.java,v 1.1 2006/06/29 18:51:34 akara Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.driver.transport.http;

import java.net.URI;
import java.util.Map;

/**
 * Faban's implementation of the cookie handler.
 *
 * @author Akara Sucharitakul
 */
public class CookieHandler extends java.net.CookieHandler {

    public Map<String, java.util.List<String>> get(
            URI uri, Map<String, java.util.List<String>> map) {
        return ThreadCookieHandler.getInstance().get(uri, map);
    }

    public void put(URI uri, Map<String, java.util.List<String>> map) {
        ThreadCookieHandler.getInstance().put(uri, map);
    }
}
