<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:output method="html" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />

    <xsl:template name="benchResults" match="benchResults">
        <div class="page-header">
            <h1>
                <xsl:value-of select="benchSummary/@name"/>
                <small>
                    <xsl:text> </xsl:text>
                    <xsl:text>v</xsl:text>
                    <xsl:value-of select="benchSummary/@version"/>
                    <xsl:text> </xsl:text>
                </small>
                <small>
                    <xsl:choose>
                         <xsl:when test="benchSummary/@host">
                            Partial Summary for Driver Host
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="benchSummary/@host"/>
                        </xsl:when>
                        <xsl:otherwise>Summary Report</xsl:otherwise>
                    </xsl:choose>
                </small>
            </h1>
        </div>
        <xsl:apply-templates select="*" />
    </xsl:template>

    <xsl:template name="passed">
        <xsl:param name="value" />
        <xsl:choose>
            <xsl:when test="$value='true'">
                <span class="text-success">
                    <span class="glyphicon glyphicon-ok"></span>
                    PASSED
                </span>
            </xsl:when>
            <xsl:when test="$value='false'">
                <span class="text-danger">
                    <span class="glyphicon glyphicon-remove"></span>
                    FAILED
                </span>
            </xsl:when>
            <xsl:otherwise>
                <span class=""></span>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>
    <xsl:template name="benchSummary" match="benchSummary">
        <dl class="dl-horizontal">
            <dt><xsl:value-of select="@name"/> metric:</dt>
            <dd>
                <xsl:value-of select="./metric"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="./metric/@unit"/>
            </dd>
            <dt>Benchmark Start:</dt><dd><xsl:value-of select="./startTime"/></dd>
            <dt>Benchmark End:</dt><dd><xsl:value-of select="./endTime"/></dd>
            <dt>Run ID:</dt><dd><xsl:value-of select="./runId"/></dd>
            <dt>Pass/Fail:</dt>
            <dd>
                <xsl:call-template name="passed">
                    <xsl:with-param name="value" select="./passed/text()"/>
                </xsl:call-template>
            </dd>
            <dt>Active Drivers:</dt>
            <xsl:for-each select="../driverSummary">
                <xsl:variable name="driverName" select="@name"/>
                <dd><a href="#{$driverName}"><xsl:value-of select="@name"/></a></dd>
            </xsl:for-each>
        </dl>
    </xsl:template>
    <xsl:template name="driverSummary" match="driverSummary">
        <h2>
            <xsl:variable name="driverName" select="@name"/>
            <a name="{$driverName}"><xsl:value-of select="$driverName"/></a>
        </h2>
        <dl class="dl-horizontal">
            <dt><xsl:value-of select="@name"/> <xsl:text> metric:</xsl:text></dt>
            <dd>
                <xsl:value-of select="./metric"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="./metric/@unit"/>
            </dd>
            <dt>Driver Start:</dt><dd><xsl:value-of select="./startTime"/></dd>
            <dt>Driver End:</dt><dd><xsl:value-of select="./endTime"/></dd>
            <dt>Total <xsl:value-of select="./totalOps/@unit"/>:</dt>
            <dd><xsl:value-of select="./totalOps"/></dd>
            <dt>Pass/Fail:</dt>
            <dd>
                <xsl:call-template name="passed">
                    <xsl:with-param name="value" select="./passed/text()"/>
                </xsl:call-template>
            </dd>
        </dl>
        <xsl:apply-templates select="*" />
    </xsl:template>
    
    <xsl:template match="metric|startTime|endTime|totalOps|users|rtXtps|passed"></xsl:template>
    
    <xsl:template name="mix" match="mix">
        <h3>Operation Mix</h3>
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Type</th>
                    <th>Success Count</th>
                    <th>Failure Count</th>
                    <th>Mix</th>
                    <th>Required Mix (<xsl:value-of select='format-number(@allowedDeviation, "##.##%")'/>)</th>
                    <th>Pass/Fail</th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="./operation">
                    <tr>
                        <td><xsl:value-of select="@name"/></td>
                        <td><xsl:value-of select="successes"/></td>
                        <td><xsl:value-of select="failures"/></td>
                        <td><xsl:value-of select='format-number(mix,"##.##%")'/></td>
                        <td><xsl:value-of select='format-number(requiredMix, "##.##%")'/></td>
                        <td>
                            <xsl:call-template name="passed">
                                <xsl:with-param name="value" select="./passed/text()"/>
                            </xsl:call-template>
                        </td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template name="responseTimes" match="responseTimes">
        <h3>Response Times
            <xsl:if test="responseTimes/@unit">
                (<xsl:value-of select="responseTimes/@unit"/>)
            </xsl:if>
        </h3>
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Type</th>
                    <th>Avg</th>
                    <th>Max</th>
                    <th>SD</th>
                    <xsl:if test="./operation[1]/percentile">
                        <xsl:for-each select="./operation[1]/percentile">
                            <th>
                                <xsl:value-of select="@nth"/>
                                <xsl:value-of select="@suffix"/>
                                %
                            </th>
                            <xsl:variable name="pct" select="@nth"/>
                            <xsl:if test="../../operation/percentile[@nth=$pct]/@limit">
                                <th>
                                    <xsl:value-of select="@nth"/>
                                    <xsl:value-of select="@suffix"/>
                                    % limit
                                </th>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:if>
                    <xsl:if test="./operation[1]/@r90th">
                        <th class="header">90th%</th>
                        <th class="header">Reqd. 90th%</th>
                    </xsl:if>
                    <th>Pass/Fail</th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="./operation">
                    <tr>
                        <td><xsl:value-of select="@name"/></td>
                        <td><xsl:value-of select="avg"/></td>
                        <td><xsl:value-of select="max"/></td>
                        <td><xsl:value-of select="sd"/></td>
                        <xsl:if test="@r90th">
                            <td><xsl:value-of select="p90th"/></td>
                            <td><xsl:value-of select="@r90th"/></td>
                        </xsl:if>
                        <xsl:if test="percentile">
                            <xsl:for-each select="percentile">
                                <td><xsl:value-of select="."/></td>
                            <xsl:variable name="pct" select="@nth"/>
                            <xsl:if test="../../operation/percentile[@nth=$pct]/@limit">
                                <td><xsl:value-of select="@limit"/></td>
                            </xsl:if>
                            </xsl:for-each>
                        </xsl:if>
                        <td>
                            <xsl:call-template name="passed">
                                <xsl:with-param name="value" select="./passed/text()"/>
                            </xsl:call-template>
                        </td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template name="delayTimes" match="delayTimes">
        <h3>Cycle/Think Times (seconds)</h3>
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Type</th>
                    <th>Targeted Avg</th>
                    <th>Actual Avg</th>
                    <th>Min</th>
                    <th>Max</th>
                    <th>Pass/Fail</th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="./operation">
                    <tr>
                        <td><xsl:value-of select="@name"/></td>
                        <td><xsl:value-of select="targetedAvg"/></td>
                        <td><xsl:value-of select="actualAvg"/></td>
                        <td><xsl:value-of select="min"/></td>
                        <td><xsl:value-of select="max"/></td>
                        <td>
                            <xsl:call-template name="passed">
                                <xsl:with-param name="value" select="./passed/text()"/>
                            </xsl:call-template>
                        </td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template name="miscStats" match="miscStats">
        <h3>Miscellaneous Statistics</h3>
        <xsl:call-template name="statTable"/>
    </xsl:template>
    <xsl:template name="customStats" match="customStats">
        <h3><xsl:value-of select="@name"/></h3>
        <xsl:call-template name="statTable"/>
    </xsl:template>
    <xsl:template name="statTable">
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Description</th>
                    <th>Results</th>
                    <xsl:if test="./stat/target">
                        <th>Targeted Results</th>
                    </xsl:if>
                    <xsl:if test="./stat/allowedDeviation">
                        <th>Allowed Deviation</th>
                    </xsl:if>
                    <xsl:if test="./stat/passed">
                        <th>Pass/Fail</th>
                    </xsl:if>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="stat">
                    <tr>
                        <td><xsl:value-of select="description"/></td>
                        <td><xsl:value-of select="result"/></td>
                        <xsl:if test="../stat/target">
                            <td><xsl:value-of select="target"/></td>
                        </xsl:if>
                        <xsl:if test="../stat/allowedDeviation">
                            <td><xsl:value-of select="allowedDeviation"/></td>
                        </xsl:if>
                        <xsl:if test="../stat/passed">
                            <td>
                                <xsl:call-template name="passed">
                                    <xsl:with-param name="value" select="./passed/text()"/>
                                </xsl:call-template>
                            </td>
                        </xsl:if>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template name="customTable" match="customTable">
        <h3><xsl:value-of select="@name"/></h3>
        <table class="table table-striped">
            <thead>
                <tr>
                    <xsl:for-each select="head/th">
                        <th><xsl:value-of select="."/></th>
                    </xsl:for-each>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="tr">
                    <tr>
                        <xsl:for-each select="td">
                            <td><xsl:value-of select="."/></td>
                        </xsl:for-each>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>

    <xsl:template match="/benchResults_bak">
        <div class="page-header">
            <h1>
                <xsl:value-of select="benchSummary/@name"/>
                <xsl:text> </xsl:text>
                <small>
                    <xsl:text>v</xsl:text>
                    <xsl:value-of select="benchSummary/@version"/>
                    <xsl:choose>
                         <xsl:when test="benchSummary/@host">
                            Partial Summary for Driver Host
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="benchSummary/@host"/>
                        </xsl:when>
                        <xsl:otherwise>Summary Report</xsl:otherwise>
                    </xsl:choose>

                </small>
            </h1>
        </div>
        <table>
            <tbody>
                <tr>
                    <td><xsl:value-of select="benchSummary/@name"/> metric:</td>
                    <td>
                        <xsl:value-of select="benchSummary/metric"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="benchSummary/metric/@unit"/>
                    </td>
                </tr>
                <tr>
                    <td>Benchmark Start:</td>
                    <td>
                        <xsl:value-of select="benchSummary/startTime"/></td>
                </tr>
                <tr>
                    <td>Benchmark End:</td>
                    <td><xsl:value-of select="benchSummary/endTime"/></td>
                </tr>
                <tr>
                    <td>Run ID:</td>
                    <td><xsl:value-of select="benchSummary/runId"/></td>
                </tr>
                <tr>
                    <td>Pass/Fail:</td>
                    <xsl:choose>
                        <xsl:when test="benchSummary/passed='true'">
                            <td style="color: rgb(0, 192, 0);">PASSED</td>
                        </xsl:when>
                        <xsl:otherwise>
                            <td style="color: rgb(255, 0, 0);">FAILED</td>
                        </xsl:otherwise>
                    </xsl:choose>
                </tr>
                <tr>
                    <td>Active Drivers:</td>
                    <td>
                        <xsl:for-each select="driverSummary">
                            <xsl:variable name="driverName" select="@name"/>
                            <a href="#{$driverName}"><xsl:value-of select="@name"/></a>
                            <br></br>
                        </xsl:for-each>
                    </td>
                </tr>
            </tbody>
        </table>
        <xsl:for-each select="driverSummary">
            <hr></hr>
            <h2>
                <xsl:variable name="driverName" select="@name"/>
                <a name="{$driverName}">
                    <xsl:value-of select="@name"/>
                </a>
            </h2>
            <table>
                <tbody>
                    <tr>
                        <td>
                            <xsl:value-of select="@name"/> <xsl:text> metric:</xsl:text>
                        </td>
                        <td>
                            <xsl:value-of select="metric"/>
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="metric/@unit"/>
                        </td>
                    </tr>
                    <tr>
                        <td>Driver start:</td>
                        <td><xsl:value-of select="startTime"/></td>
                    </tr>
                    <tr>
                        <td>Driver end:</td>
                        <td><xsl:value-of select="endTime"/></td>
                    </tr>
                    <tr>
                        <td>Total number of <xsl:value-of select="totalOps/@unit"/>:
                        </td>
                        <td><xsl:value-of select="totalOps"/></td>
                    </tr>
                    <tr>
                        <td>Pass/Fail:</td>
                        <xsl:choose>
                            <xsl:when test="passed='true'">
                                <td style="color: rgb(0, 192, 0);">PASSED</td>
                            </xsl:when>
                            <xsl:otherwise>
                                <td style="color: rgb(255, 0, 0);">FAILED</td>
                            </xsl:otherwise>
                        </xsl:choose>
                    </tr>
                </tbody>
            </table><br></br>
            <xsl:if test="mix">
            <h3>Operation Mix</h3>
            <table >
                <tbody>
                    <tr>
                        <th class="header">Type</th>
                        <th class="header">Success<br></br>Count</th>
                        <th class="header">Failure<br></br>Count</th>
                        <th class="header">Mix</th>
                        <th class="header">Required Mix<br></br>
                            (<xsl:value-of select='format-number(mix/@allowedDeviation, "##.##%")'/> deviation allowed)</th>
                        <th class="header">Pass/Fail</th>
                    </tr>
                    <xsl:for-each select="mix/operation">
                        <tr>
                            <xsl:choose>
                                <xsl:when test="(position() mod 2 = 1)">
                                    <xsl:attribute name="class">even</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">odd</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <td ><xsl:value-of select="@name"/></td>
                            <td ><xsl:value-of select="successes"/></td>
                            <td ><xsl:value-of select="failures"/></td>
                            <td ><xsl:value-of select='format-number(mix, "##.##%")'/></td>
                            <td ><xsl:value-of select='format-number(requiredMix, "##.##%")'/></td>
                            <xsl:choose>
                                <xsl:when test="passed='true'">
                                    <td >PASSED</td>
                                </xsl:when>
                                <xsl:when test="passed='false'">
                                    <td >FAILED</td>
                                </xsl:when>
                                <xsl:otherwise>
                                    <td ></td>
                                </xsl:otherwise>
                            </xsl:choose>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table><br></br>
            </xsl:if>
            <xsl:if test="responseTimes">
            <h3>Response Times
                <xsl:if test="responseTimes/@unit">
                    (<xsl:value-of select="responseTimes/@unit"/>)
                </xsl:if>
            </h3>
            <table>
                <tbody>
                    <tr >
                        <th class="header">Type</th>
                        <th class="header">Avg</th>
                        <th class="header">Max</th>
                        <th class="header">SD</th>
                        <xsl:if test="responseTimes/operation[1]/percentile">
                            <xsl:for-each select="responseTimes/operation[1]/percentile">
                                <th class="header"><xsl:value-of select="@nth"/><xsl:value-of select="@suffix"/>%</th>
                                <xsl:variable name="pct" select="@nth"/>
                                <xsl:if test="../../operation/percentile[@nth=$pct]/@limit">
                                    <th class="header"><xsl:value-of select="@nth"/><xsl:value-of select="@suffix"/>%<br/>limit</th>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:if>
                        <xsl:if test="responseTimes/operation[1]/@r90th">
                            <th class="header">90th%</th>
                            <th class="header">Reqd. 90th%</th>
                        </xsl:if>
                        <th class="header">Pass/Fail</th>
                    </tr>
                    <xsl:for-each select="responseTimes/operation">
                        <tr>
                            <xsl:choose>
                                <xsl:when test="(position() mod 2 = 1)">
                                    <xsl:attribute name="class">even</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">odd</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <td ><xsl:value-of select="@name"/></td>
                            <td ><xsl:value-of select="avg"/></td>
                            <td ><xsl:value-of select="max"/></td>
                            <td ><xsl:value-of select="sd"/></td>
                            <xsl:if test="@r90th">
                                <td ><xsl:value-of select="p90th"/></td>
                                <td ><xsl:value-of select="@r90th"/></td>
                            </xsl:if>
                            <xsl:if test="percentile">
                                <xsl:for-each select="percentile">
                                    <td ><xsl:value-of select="."/></td>
                                <xsl:variable name="pct" select="@nth"/>
                                <xsl:if test="../../operation/percentile[@nth=$pct]/@limit">
                                    <td ><xsl:value-of select="@limit"/></td>
                                </xsl:if>
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:choose>
                                <xsl:when test="passed='true'">
                                    <td  style="color: rgb(0, 192, 0);">PASSED</td>
                                </xsl:when>
                                <xsl:when test="passed='false'">
                                    <td  style="color: rgb(255, 0, 0);">FAILED</td>
                                </xsl:when>
                                <xsl:otherwise>
                                    <td ></td>
                                </xsl:otherwise>
                            </xsl:choose>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table><br></br>
            </xsl:if>
            <xsl:if test="delayTimes">
            <h3>Cycle/Think Times (seconds)</h3>
            <table>
                <tbody>
                    <tr >
                        <th class="header">Type</th>
                        <th class="header">Targeted Avg</th>
                        <th class="header">Actual Avg</th>
                        <th class="header">Min</th>
                        <th class="header">Max</th>
                        <th class="header">Pass/Fail</th>
                    </tr>
                    <xsl:for-each select="delayTimes/operation">
                        <tr>
                            <xsl:choose>
                                <xsl:when test="(position() mod 2 = 1)">
                                    <xsl:attribute name="class">even</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">odd</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <td ><xsl:value-of select="@name"/></td>
                            <td ><xsl:value-of select="targetedAvg"/></td>
                            <td ><xsl:value-of select="actualAvg"/></td>
                            <td ><xsl:value-of select="min"/></td>
                            <td ><xsl:value-of select="max"/></td>
                            <xsl:choose>
                                <xsl:when test="passed='true'">
                                    <td  style="color: rgb(0, 192, 0);">PASSED</td>
                                </xsl:when>
                                <xsl:when test="passed='false'">
                                    <td  style="color: rgb(255, 0, 0);">FAILED</td>
                                </xsl:when>
                                <xsl:otherwise>
                                    <td ></td>
                                </xsl:otherwise>
                            </xsl:choose>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table><br></br>
            </xsl:if>
            <xsl:if test="miscStats">
            <h3>Miscellaneous Statistics</h3>
            <table >
                <tbody>
                    <tr >
                        <th class="header">Description</th>
                        <th class="header">Results</th>
                        <xsl:if test="miscStats/stat/target">
                            <th class="header">Targeted<br/>Results</th>
                        </xsl:if>
                        <xsl:if test="miscStats/stat/allowedDeviation">
                            <th class="header">Allowed<br/>Deviation</th>
                        </xsl:if>
                        <th class="header">Pass/Fail</th>
                    </tr>
                    <xsl:for-each select="miscStats/stat">
                        <tr>
                            <xsl:choose>
                                <xsl:when test="(position() mod 2 = 1)">
                                    <xsl:attribute name="class">even</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">odd</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <td ><xsl:value-of select="description"/></td>
                            <td ><xsl:value-of select="result"/></td>
                            <xsl:if test="../stat/target">
                                <td ><xsl:value-of select="target"/></td>
                            </xsl:if>
                            <xsl:if test="../stat/allowedDeviation">
                                <td ><xsl:value-of select="allowedDeviation"/></td>
                            </xsl:if>
                            <xsl:if test="../stat/passed">
                                <xsl:choose>
                                    <xsl:when test="passed='true'">
                                        <td  style="color: rgb(0, 192, 0);">PASSED</td>
                                    </xsl:when>
                                    <xsl:when test="passed='false'">
                                        <td  style="color: rgb(255, 0, 0);">FAILED</td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td ></td>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table><br></br>
            </xsl:if>
            <xsl:for-each select="customStats">
            <h3><xsl:value-of select="@name"/></h3>
            <table>
                <tbody>
                    <tr>
                        <th class="header">Description</th>
                        <th class="header">Results</th>
                        <xsl:if test="stat/target">
                            <th class="header">Targeted<br></br>Results</th>
                        </xsl:if>
                        <xsl:if test="stat/allowedDeviation">
                            <th class="header">Allowed<br></br>Deviation</th>
                        </xsl:if>
                        <xsl:if test="stat/passed">
                            <th class="header">Pass/Fail</th>
                        </xsl:if>
                    </tr>
                    <xsl:for-each select="stat">
                        <tr>
                            <xsl:choose>
                                <xsl:when test="(position() mod 2 = 1)">
                                    <xsl:attribute name="class">even</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">odd</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <td ><xsl:value-of select="description"/></td>
                            <td ><xsl:value-of select="result"/></td>
                            <xsl:if test="../stat/target">
                                <td ><xsl:value-of select="target"/></td>
                            </xsl:if>
                            <xsl:if test="../stat/allowedDeviation">
                                <td ><xsl:value-of select="allowedDeviation"/></td>
                            </xsl:if>
                            <xsl:if test="../stat/passed">
                                <xsl:choose>
                                    <xsl:when test="passed='true'">
                                        <td  style="color: rgb(0, 192, 0);">PASSED</td>
                                    </xsl:when>
                                    <xsl:when test="passed='false'">
                                        <td  style="color: rgb(255, 0, 0);">FAILED</td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td ></td>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table><br></br>
            </xsl:for-each>
            <xsl:for-each select="customTable">
            <h3><xsl:value-of select="@name"/></h3>
            <table>
                <tbody>
                    <tr>
                        <xsl:for-each select="head/th">
                            <th class="header"><xsl:value-of select="."/></th>
                        </xsl:for-each>
                    </tr>
                    <xsl:for-each select="tr">
                        <tr>
                            <xsl:choose>
                                <xsl:when test="(position() mod 2 = 1)">
                                    <xsl:attribute name="class">even</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">odd</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:apply-templates select="node()"/>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table><br></br>
            </xsl:for-each>
            <xsl:if test="users">
                <xsl:if test="rtXtps">
                    <h3>Little's Law Verification</h3>
                    <table border="0" cellpadding="2" cellspacing="2">
                        <tbody>
                            <tr>
                                <td>Number of users</td>
                                <td>=</td>
                                <td><xsl:value-of select="users"/></td>
                            </tr>
                            <tr>
                                <td>Sum of Avg. RT * TPS for all Tx Types</td>
                                <td>=</td>
                                <td><xsl:value-of select="rtXtps"/></td>
                            </tr>
                        </tbody>
                    </table>
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
        <br></br>
        <hr></hr>
    </xsl:template>
    <xsl:template match="td|th">
        <xsl:copy>
          <xsl:attribute name="class">tablecell</xsl:attribute>
          <xsl:copy-of select="node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
