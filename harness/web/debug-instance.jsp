<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<!--
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
 * $Id: debug-instance.jsp,v 1.1 2006/06/29 18:51:45 akara Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */
-->
<%@ page import="java.io.*,
                 org.w3c.dom.Node" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="org.w3c.dom.Document" %>
<%@ page import="org.chiba.xml.util.*" %>
<%@ page import="org.chiba.xml.xforms.*" %>
<%@ page import="org.chiba.util.DateUtil" %>

<%@ page session="true" %>
<%@ page errorPage="error.jsp" %>


<html>
    <head>
        <title>Instance Data</title>
        <link rel="icon" type="image/gif" href="img/faban.gif">
     </head>
<body bgcolor="#aabbdd">

<h3>Instance Data:</h3>
<div class="xml">
<font face="sans-serif" size="+1">
<pre>
<%
        Reader reader = request.getReader();
//    System.out.println("content-length: " + request.getContentLength());
        char[] buf = new char[request.getContentLength()];
        reader.read( buf );
        String s = new String(buf);
// how wierd , if you put only &lt; then &gt; is put in automatically in ie at least !
        while (true){        
            int index = s.indexOf('<');
            if (index == -1) break;
            s = s.substring(0 , index )+"&lt;"+s.substring(index+1);
        }
        out.write( s );
%></pre>
</font>
</div>
</body>
</html>
