<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />
	<xsl:template match="record">
		<xsl:variable name="seq" select="sequence" />

		<xsl:call-template name="record-row" />
		
	</xsl:template>
	<xsl:template name="exception-row">
		<div id="fold-${seq}" class="open-fold">
			<span class="fold-marker"><xsl:text>Exception</xsl:text></span>
			<xsl:call-template name="record-row" />
			<xsl:apply-templates select="exception"/>
		</div>
		
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
	<xsl:template name="record-row">
		<tr>
			<td><xsl:value-of select="date"/></td>
			<td><xsl:value-of select="host"/></td>
			<td><xsl:value-of select="level"/></td>
			<td><xsl:value-of select="message"/></td>
		</tr>        
    </xsl:template>

</xsl:stylesheet>