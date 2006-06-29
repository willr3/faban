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
 * $Id: index.jsp,v 1.1 2006/06/29 18:51:45 akara Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */
-->
<%@ page language="java" import="com.sun.faban.harness.common.BenchmarkDescription"%>
<html>
    <head>
        <title><%=BenchmarkDescription.getBannerName()%> Administration</title>
        <link rel="icon" type="image/gif" href="img/faban.gif">
    </head>
    <frameset rows="80,*">
        <frame name="banner" src="banner.jsp" scrolling="no" noresize="noresize" frameborder="0"/>
        <frameset cols="15%,*">
            <frame name="menu" src="menu.html" frameborder="0"/>
            <frame name="main" src="welcome.jsp" frameborder="0"/>
        </frameset>
        <noframes>
            <p>This page requires frames, but your browser does not support them.</p>
        </noframes>
    </frameset>
</html>
