<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />
	<xsl:template match="record">
		<xsl:variable name="seq" select="sequence" />

			<xsl:choose>
				<xsl:when test="exception">
						
					<div id="fold-${seq}" class="open-fold">
						<span class="fold-marker"><xsl:text>Exception</xsl:text></span>
						<xsl:call-template name="single-line" />
						<xsl:apply-templates select="exception"/>
					</div>
					
					<div id="close-${seq}" class="close-fold"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="single-line" />
				</xsl:otherwise>
			</xsl:choose>
		
	</xsl:template>

	<xsl:template name="exception-body" match="exception">
		<p>
			<a/>
			<span class="e-message"><xsl:value-of select="message"/></span>
		</p>
		<xsl:for-each select="frame">
			<xsl:variable name="id" select="position()"/>
			<p>
				<a href="#${seq}-${id}"/>
				<span>
					<span class="e-class"><xsl:value-of select="class"/></span>
					<span class="e-method"><xsl:value-of select="method"/></span>
					<span class="e-line"><xsl:value-of select="line"/></span>
				</span>
			</p>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="single-line">
		<p>
			<a href="#${seq}"/>
			<span>
				<span class="date"><xsl:value-of select="date"/></span>
				<span class="seq"><xsl:value-of select="sequence"/></span>
				<span class="host"><xsl:value-of select="host"/></span>
				<span class="level"><xsl:value-of select="level"/></span>
				<span class="class"><xsl:value-of select="class"/></span>
				<span class="method"><xsl:value-of select="method"/></span>
				<span class="thread"><xsl:value-of select="thread"/></span>
				<span class="message"><xsl:value-of select="message"/></span>
			</span>
		</p>
        
    </xsl:template>

</xsl:stylesheet>