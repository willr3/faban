<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:chiba="http://chiba.sourceforge.net/xforms"
    exclude-result-prefixes="chiba xforms xlink xsl">

    <xsl:include href="bootstrap-form-controls.xsl"/>
    <xsl:output method="html" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />

    <xsl:param name="action-url" select="''"/>
    <xsl:param name="form-id" select="'chiba-form'"/>
    <xsl:param name="debug-enabled" select="'false'"/>

    <xsl:template match="/">
            <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="head">
    </xsl:template>

    <xsl:template match="body">
            <form class="form-horizontal" role="form" action="{$action-url}" method="post" >
                <xsl:apply-templates/>
            </form>
    </xsl:template>

    <xsl:template match="xforms:model"/>

    <xsl:template match="xforms:group[@id='group-tabsheet']">
        <xsl:variable name="selected-case" select="xforms:switch/xforms:case[@xforms:selected='true']"/>
        
            <ul class="nav nav-tabs">
                <xsl:for-each select="xforms:trigger">
                    <xsl:choose>
                        <xsl:when test="xforms:action/xforms:toggle/@xforms:case=$selected-case/@id">
                            <li class="active">
                                <a >
                                    <xsl:apply-templates select="xforms:label"/>
                                </a>
                            </li>
                        </xsl:when>
                        <xsl:otherwise>
                            <li class="">
<!--                                <input type="submit" name="{chiba:data/@chiba:name}" value="{xforms:label}" class="inactive-tab-button"/>-->
                                <input type="submit" class="btn btn-link" name="{concat('t_',@id)}" value="{xforms:label}" />
                            </li>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </ul>
            <xsl:variable name="tab-count" select="count(xforms:trigger) + 1"/>
            <xsl:for-each select="$selected-case/*">
                <tr>
                    <td colspan="{$tab-count}">
                        <xsl:apply-templates select="."/>
                    </td>
                </tr>
            </xsl:for-each>
    </xsl:template>

    <xsl:template match="xforms:group[@id='group-buttons']">
        <div class="form-group">
            <div class="col-lg-offset-3 col-lg-6">
                <xsl:for-each select="xforms:trigger">
    <!--                <input type="submit" name="{chiba:data/@chiba:name}" value="{xforms:label}" class="action-button"/>-->
                    <button type="submit" name="{concat('t_',@id)}" value="{xforms:label}" class="btn btn-default">
                        <xsl:apply-templates select="xforms:label"/>
                    </button>
                </xsl:for-each>
            </div>
        </div>
    </xsl:template>

    <xsl:template match="xforms:group[xforms:label]">
        <section class="doc-section">
            <span class="section-title">
                <xsl:apply-templates select="xforms:label"/>
            </span>
            <xsl:for-each select="xforms:input|xforms:select1|xforms:textarea">
                <div class="form-group">
                    <label class="col-lg-3 control-label" for="TODO">
                        <xsl:apply-templates select="xforms:label"/>
                    </label>
                    <div class="col-lg-6">
                        <xsl:apply-templates select="."/>
                    </div>
                </div>
            </xsl:for-each>
        </section>
    </xsl:template>

    <xsl:template match="xforms:group">
        <section class="doc-section">
            <xsl:for-each select="xforms:input|xforms:select1|xforms:textarea">
                <div class="form-group">
                    <label class="col-lg-3 control-label" for="TODO">
                        <xsl:apply-templates select="xforms:label"/>
                    </label>
                    <div class="col-lg-6">
                        <xsl:apply-templates select="."/>
                    </div>
                </div>
            </xsl:for-each>
        </section>
    </xsl:template>

    <xsl:template match="xforms:select1">
        <xsl:call-template name="select1">
<!--            <xsl:with-param name="name" select="chiba:data/@chiba:name"/>-->
            <xsl:with-param name="name" select="concat('d_',@id)"/>
            <xsl:with-param name="value" select="chiba:data"/>
            <xsl:with-param name="appearance" select="@xforms:appearance"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="xforms:input">
        <xsl:call-template name="input">
<!--            <xsl:with-param name="name" select="chiba:data/@chiba:name"/>-->
            <xsl:with-param name="name" select="concat('d_',@id)"/>
            <xsl:with-param name="value" select="chiba:data"/>
            <xsl:with-param name="size" select="40"/>
        </xsl:call-template>
        <xsl:apply-templates select="xforms:alert"/>
    </xsl:template>

    <xsl:template match="xforms:textarea">
        <xsl:call-template name="textarea">
            <xsl:with-param name="name" select="concat('d_',@id)"/>
            <xsl:with-param name="value" select="chiba:data"/>
            <xsl:with-param name="rows" select="10"/>
            <xsl:with-param name="cols" select="40"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="xforms:label">
        <xsl:copy-of select="*|text()"/>
    </xsl:template>

    <xsl:template match="xforms:alert[../chiba:data/@chiba:valid='false']">
        <div class="alert">
            <xsl:copy-of select="*|text()"/>
        </div>
    </xsl:template>

    <xsl:template match="xforms:alert"/>

</xsl:stylesheet>
