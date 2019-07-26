<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="webroot"/>
    <xsl:param name="imageroot"/>
    <xsl:param name="cssstylesheet"/>
    <xsl:param name="title"/>
    <xsl:output method="html" indent="yes" />
    <xsl:template match="/">
        <script language="JavaScript" type="text/javascript" src="{$webroot}/common/common.js"></script>
        <xsl:apply-templates select="response"/>
    </xsl:template>
    
    <xsl:template match="response">
        <xsl:apply-templates select="question"/>        
    </xsl:template>
    
    <xsl:template match="question">
        <xsl:variable name="qid" select="@id"/>
        <xsl:variable name="calculationType" select="@calculateType"/>
        <xsl:variable name="skipOperator" select="@skipOperator"/>
        <xsl:variable name="hasSkipRule" select="@hasSkipRule"/>
        <xsl:variable name="skipRule" select="@skipRule"/>
        <xsl:variable name="skipRuleEquals" select="@skipEquals"/>
        <xsl:variable name="default" select="defaultValue"/>
        <xsl:variable name="questionsToSkip" select="questionsToSkip"/>
        <xsl:variable name="adminid" select="../@adminformid" />

    <xsl:choose>
            <xsl:when test="@type = 'Patient Calendar'">
            <xsl:choose>
                <xsl:when test="@editType = 'draftEdit'">
                    <xsl:variable name="colcount" select="columncount" />
                    <xsl:variable name="colwidth" select="columnwidth" />
                    <xsl:variable name="winwidth" select="windowwidth" />
                    <DIV STYLE="overflow: auto; width: {$winwidth}px; height: 390px;
                                border-left: 1px gray solid; border-bottom: 1px gray solid; 
                                border-top: 1px gray solid; border-right: 1px gray solid;
                                padding:0px; margin: 0px">
                    <xsl:value-of select="text" />
                    <table cellpadding="2" border="1">
                    <tr valign="middle">
                    <td>
                    <xsl:value-of select="calendarheader/rowtitle" />
                    </td>
                    <xsl:for-each select="calendarheader/rowcells/rowcell">
                        <td align="center">
                        <xsl:value-of select="cellidentifier" />
                        </td>
                    </xsl:for-each>
                    </tr>
                    <xsl:for-each select="calendarrows/calendarrow">
                        <tr>
                        <td><xsl:value-of select="rowtitle" disable-output-escaping="yes"/></td>
                        <xsl:for-each select="rowcells/rowcell">
                            <xsl:variable name="responseid" select="responseid" />
                            <xsl:variable name="displaytext" select="displaytext" />
                            <xsl:variable name="ident" select="cellidentifier" />
                            <td>
                            <xsl:choose>
                                <xsl:when test="celldata/@discrepancy = 'yes'">                        
                                    <a href="javascript:popupWindow('{$webroot}/response/editCalendarCellDiscrepancy.do?identifier={$ident}&amp;responseid={$responseid}&amp;adminid={$adminid}')">
                                    <xsl:value-of select="$displaytext"/>
                                    </a>
                                </xsl:when>
                                <xsl:when test="celldata/@discrepancy = 'none'">
                                    <xsl:value-of select="$displaytext"/>
                                </xsl:when>
                            </xsl:choose>
                            </td>
                        </xsl:for-each>
                        </tr>
                    </xsl:for-each>
                    </table>
                    </DIV>         
                </xsl:when>
                <xsl:when test="@editType = 'finalEdit'">
                    <xsl:variable name="colcount" select="columncount" />
                    <xsl:variable name="colwidth" select="columnwidth" />
                    <xsl:variable name="winwidth" select="windowwidth" />
                    <DIV STYLE="overflow: auto; width: {$winwidth}px; height: 390px;
                                border-left: 1px gray solid; border-bottom: 1px gray solid; 
                                border-top: 1px gray solid; border-right: 1px gray solid;
                                padding:0px; margin: 0px">
                    <xsl:value-of select="text" />
                    <table cellpadding="2" border="1">
                    <tr valign="middle">
                    <td>
                    <xsl:value-of select="calendarheader/rowtitle" />
                    </td>
                    <xsl:for-each select="calendarheader/rowcells/rowcell">
                        <td align="center">
                        <xsl:value-of select="cellidentifier" />
                        </td>
                    </xsl:for-each>
                    </tr>
                    <xsl:for-each select="calendarrows/calendarrow">
                        <tr>
                        <td><xsl:value-of select="rowtitle" disable-output-escaping="yes"/></td>
                        <xsl:for-each select="rowcells/rowcell">
                            <xsl:variable name="responseid" select="responseid" />
                            <xsl:variable name="displaytext" select="displaytext" />
                            <xsl:variable name="ident" select="cellidentifier" />
                            <td>
                                <a href="javascript:popupWindow('{$webroot}/response/editCalendarCell.do?identifier={$ident}&amp;responseid={$responseid}')">
                                <xsl:value-of select="$displaytext"/>
                                </a>
                            </td>
                        </xsl:for-each>
                        </tr>
                    </xsl:for-each>
                    </table>
                    </DIV>         
                </xsl:when>
                </xsl:choose>
            </xsl:when>
    </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
