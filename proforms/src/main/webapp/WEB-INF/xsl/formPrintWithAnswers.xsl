<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="webroot"/>
    <xsl:variable name="displaytop" select="/form/TOC/@display"/>
    <xsl:param name="imageroot"/>
    <xsl:param name="cssstylesheet"/>
    <xsl:param name="title"/>
    <xsl:output method="html" indent="yes"/>
    <xsl:param name="dictionaryWsRoot"/>
    <xsl:template match="/">
        <script language="JavaScript" type="text/javascript" src="{$webroot}/common/common.js"></script>
        <xsl:apply-templates select="form"/>
    </xsl:template>

    <xsl:template match="form">
        <table width="100%" style="border: {htmlAttributes/formBorder}px ridge;" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">
            <!--
            <tr>
                <td style="font-family: {htmlAttributes/formFont}; color: {htmlAttributes/formColor}"><xsl:value-of select="name"/></td>
            </tr>
            <tr>
                <td><xsl:value-of select="description"/></td>
            </tr>
            <tr>
                <tr><td><img src="{$imageroot}/spacer.gif" width="1" height="10" alt="" border="0"/></td></tr>
            </tr>
            -->

           <!-- <xsl:apply-templates select="TOC"/>-->

           <!--<tr>
                <td>  -->
                    <xsl:apply-templates select="row"/>
               <!--  </td>
            </tr> -->
        </table>
    </xsl:template>
  <!--
    <xsl:template match="TOC">
        <xsl:choose>
            <xsl:when test="$displaytop = 'true'">
            <tr>
                <td style="font-family:{htmlAttributes/formFont}; color: {htmlAttributes/formColor}; font-size:8pt">
                    <span id="000" /><div style="font-size:12pt"><![CDATA[Table of Contents]]></div>
                    <ul>
                        <xsl:apply-templates select="TOCListing"/>
                    </ul>
                </td>
            </tr>
        </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="TOCListing">
        <xsl:variable name="tocsid" select="@sectionid"/>
        <xsl:variable name="tocdisp" select="@displayvalue"/>
        <li><a href="javascript:jumpto('{$tocsid}')"><xsl:value-of select="$tocdisp"/></a></li>
    </xsl:template>
    -->


    <xsl:template match="row">
       <!-- <table width="100%"> -->
            <tr>
                <xsl:apply-templates select="formcell"/>
            </tr>
        <!-- </table> -->
    </xsl:template>

    <xsl:template match="formcell">
        <xsl:variable name="theStyle" select="@theStyle"/>
        <xsl:variable name="theRowSpan" select="@theRowSpan"/>
        <xsl:variable name="theColSpan" select="@theColSpan"/>
        <td style="{$theStyle}" rowspan="{$theRowSpan}" colspan="{$theColSpan}" >
            <xsl:apply-templates select="section"/>
        </td>
    </xsl:template>

    <xsl:template match="section">
        <xsl:variable name="sectionid" select="@id"/>
        <table width="100%" style="border: {htmlAttributes/sectionBorder}px solid;" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">
            <tr>
                <td style="font-family: {htmlAttributes/sectionFont}; color: {htmlAttributes/sectionColor}; font-size:{/form/htmlAttributes/formFontSize}pt;">
                <xsl:variable name="secid" select="sectionid" />
                    <span id="{$secid}" />
                    <xsl:choose>
                        <xsl:when test="@textDisplayed = 'true'">
                            <xsl:value-of select="name"/>
                        </xsl:when>
                    </xsl:choose>
               <!--     <xsl:choose>
                        <xsl:when test="$displaytop = 'true'">
                            <xsl:choose>
                                <xsl:when test="@inTableOfContents = 'true'">
                                    <span>&#160;&#160;</span><a href="javascript:jumpto('000')"><font color="{htmlAttributes/sectionColor}">top</font></a>
                                </xsl:when>
                            </xsl:choose>
                        </xsl:when>
                    </xsl:choose>
                    -->
                </td>
            </tr>
            <tr>
                <td style="font-family:{htmlAttributes/sectionFont}; color: {htmlAttributes/sectionColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    <xsl:value-of select="instructionalText" disable-output-escaping="yes"/>
                </td>
            </tr>
            
            <tr>
            	<td valign="top">
            		<table width="100%" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">
            			<xsl:apply-templates select="questions/question"/>
            		</table>
            	</td>
            </tr>

        </table>
    </xsl:template>

    <xsl:template match="question">
        <xsl:variable name="qid" select="@id"/>
        <xsl:variable name="skipOperator" select="formQuestionAttributes/@skipOperator"/>
        <xsl:variable name="hasSkipRule" select="formQuestionAttributes/@hasSkipRule"/>
        <xsl:variable name="skipRule" select="formQuestionAttributes/@skipRule"/>
        <xsl:variable name="skipRuleEquals" select="formQuestionAttributes/@skipEquals"/>
        <xsl:variable name="default" select="defaultValue"/>
        <xsl:variable name="questionsToSkip" select="formQuestionAttributes/questionsToSkip"/>
        <xsl:variable name="horizontalDisplay" select="formQuestionAttributes/horizontalDisplay"/>
        <xsl:variable name="horizDisplayBreak" select="formQuestionAttributes/horizontalDisplay/@horizDisplayBreak"/>
        <xsl:variable name="textboxSize" select="formQuestionAttributes/@textboxSize"/>
        


        <tr>
            <td colspan="2" id="Q_{$qid}_link">

                <xsl:for-each select="images/filename">
                    <img src="{$imageroot}/questionimages/{.}" alt="Question Image" border="0"/>&#160;&#160;
                </xsl:for-each>
            </td>
        </tr>
        <tr>
            <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt">
                <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/><xsl:value-of select="formQuestionAttributes/label"/>
            </td>
        </tr>
        <tr>
            <xsl:choose>

                <xsl:when test="@type= 'Visual Scale'">    <!-- THIS IS THE VISUAL SCALE PORTION -->
                     <xsl:variable name="rightText" select="@rightText"/>
                     <xsl:variable name="leftText" select="@leftText"/>
                     <xsl:variable name="width" select="@width"/>
                     <xsl:variable name="scaleMin" select="@scaleMin"/>
                     <xsl:variable name="scaleMax" select="@scaleMax"/>
                    <xsl:variable name="centerText" select="@centerText"/>
                    <xsl:variable name="showHandle" select="@showHandle"/>



                    <td colspan="2" align="center">
                        <table align="center" width="580px">
                            <tr align="center">   <!-- THIS ROW IS FOR THE QUESTION TEXT -->
                                <td width="1%">
                                    <span id="slider_Q_{$qid}" class="_sliderControl" style="display:none;">
                                        <input type="text" size="3"  id="slider_Q_{$qid}_valueSet" onkeyup="_sl{$qid}.setValue(parseInt(this.value))"/>
                                    </span>
                                </td>
                                <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                                      <xsl:choose>
                                        <xsl:when test="@displayText='true'">
                                            <xsl:choose>
                                                <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 0">
                                                    <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
                                                        <xsl:value-of select="text" disable-output-escaping="yes"/>
                                                    </font>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                                <xsl:value-of select="text" disable-output-escaping="yes"/>
                                                </span>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                    </xsl:choose>
                                </td>
                                <td></td>
                            </tr>
                            <tr align="center"> <!-- THIS ROW IS THE SCALE AND RIGHT AND LEFT TEXT -->
                                <td align="right" valign="{formQuestionAttributes/htmlAttributes/valign}" width="10%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt;">
                                     <xsl:value-of select="$leftText" disable-output-escaping="yes"/>
                                </td>
                                <td align="center" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt ">

                                    <div class="slider" id="Q_{$qid}_slider" style="width:{$width}mm;">
                                        <input class="slider-input"  id="Q_{$qid}" name="Q_{$qid}" />
                                    </div>
                                    <script language="javascript">
                                        var _sl<xsl:value-of select="$qid"/> = new Slider(document.getElementById("Q_<xsl:value-of select="$qid"/>_slider"), document.getElementById("Q_<xsl:value-of select="$qid"/>"), 'horizontal', '<xsl:value-of select="$showHandle"/>' );
                                        _sl<xsl:value-of select="$qid"/>.setMinimum(<xsl:value-of select="$scaleMin"/>);
                                        _sl<xsl:value-of select="$qid"/>.setMaximum(<xsl:value-of select="$scaleMax"/>);
                                         try {
                                        _sl<xsl:value-of select="$qid"/>.setValue(<xsl:value-of select="$default"/>);
                                        } catch (err) {}                                    </script>
                                    <br/><xsl:value-of select="$centerText" disable-output-escaping="yes"/>

                                </td>
                                <td align="left" valign="{formQuestionAttributes/htmlAttributes/valign}" width="10%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt">
                                     <xsl:value-of select="$rightText" disable-output-escaping="yes"/>
                                </td>
                            </tr>
                        </table>

                    </td>
                </xsl:when>
                <!-- Check to see if it is a patient calendar -->

                <xsl:when test="@type = 'Patient Calendar'">
                    <xsl:variable name="colcount" select="columncount"/>
                    <xsl:variable name="colwidth" select="columnwidth"/>
                    <xsl:variable name="winwidth" select="windowwidth"/>
                    <td colspan="2">
                        <DIV STYLE="overflow: auto; width: {$winwidth}px; height: 390px;
                                    border-left: 1px gray solid; border-bottom: 1px gray solid;
                                    border-top: 1px gray solid; border-right: 1px gray solid;
                                    padding:0px; margin: 0px">
                            <xsl:choose>
                                <xsl:when test="@displayText='true'">
                                    <xsl:value-of select="text" disable-output-escaping="yes"/>
                                </xsl:when>
                            </xsl:choose>
                            <table cellpadding="0">
                                <tr>
                                    <td>
                                        <xsl:value-of select="calendarheader/rowtitle"/>
                                    </td>
                                    <xsl:for-each select="calendarheader/rowcells/rowcell">
                                        <td align="center">
                                            <xsl:value-of select="cellidentifier"/>
                                        </td>
                                    </xsl:for-each>
                                </tr>
                                <xsl:for-each select="calendarrows/calendarrow">
                                    <tr>
                                        <td>
                                            <xsl:value-of select="rowtitle" disable-output-escaping="yes"/>
                                        </td>
                                        <xsl:for-each select="rowcells/rowcell">
                                            <xsl:variable name="cid" select="cellidentifier"/>
                                            <xsl:variable name="cellvalue" select="celldata"/>
                                            <td width="{$colwidth}">
                                                <input type="text" name="{$cid}" size="{$colwidth}" value="{$cellvalue}"/>
                                            </td>
                                        </xsl:for-each>
                                    </tr>
                                </xsl:for-each>
                            </table>
                        </DIV>
                    </td>

                </xsl:when>
                <xsl:when test="@type = 'Image Map'">
                    <xsl:variable name="imageHeight" select="@imageHeight"/>
                    <xsl:variable name="imageWidth" select="@imageWidth"/>
                    <xsl:variable name="imageMapFileName" select="@imageMapFileName"/>
                    <xsl:variable name="gridFileName" select="@gridFileName"/>
                    <td colspan="2">
                        <table width="100%" border="0">
                            <tr>
                                <td width="95%" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">

                                    <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
                                    <xsl:choose>
                                        <xsl:when test="formQuestionAttributes/@required = 'true'">
                                            <span class="requiredIndicator">*</span>
                                        </xsl:when>
                                    </xsl:choose>

                                    <xsl:choose>
                                        <xsl:when test="@displayText='true'">
                                            <xsl:choose>
                                                <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 0">
                                                    <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
                                                        <xsl:value-of select="text" disable-output-escaping="yes"/>
                                                    </font>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                                <xsl:value-of select="text" disable-output-escaping="yes"/>
                                                </span>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                    </xsl:choose>
                                </td>
                                <td width="5%">
                                    <img src="{$imageroot}/buttonRemove.png" alt="Delete" title="Delete"
                                        border="0" width="23" height="23"
                                        onclick="removeItemMacOpt (document.getElementById('imageMap_{$qid}'), 'NO');"/>
                                </td>
                                <td width="5%">
                                    <select id="imageMap_{$qid}" size="5" name="Q_{$qid}" class="ctdbImageMapAnswers" multiple="true">
                                        <xsl:for-each select="answers/option">
                                            <xsl:choose>
                                                <xsl:when test="@selected = 'true'">
                                                    <option value="{display}" selected="@selected" title="{display/@displayToolTip}">
                                                        <xsl:value-of select="display"/>
                                                    </option>
                                                </xsl:when>
                                            </xsl:choose>
                                        </xsl:for-each>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="3" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="40%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">


                                    <xsl:choose>
                                        <xsl:when test="@displayGrid = 'true'">
                                            <img src="{$imageroot}/spacer.gif" border="0"
                                                height="1" width="1"
                                                id="imageMapRef_{$qid}" style="z-index:-100; position:absolute"/>
                                            <img src="{$imageroot}/spacer.gif" border="0"
                                                height="{$imageHeight}" width="{$imageWidth}"
                                                id="imageMapSpacer_{$qid}" style="z-index:-100"/>
                                            <img src="{$imageroot}/questionimages/{$imageMapFileName}" border="0"
                                                height="{$imageHeight}" width="{$imageWidth}"
                                                style="z-index:100; position:absolute" id="imageMapImage_{$qid}"/>


                                            <img src="{$imageroot}/{$gridFileName}" border="0"
                                                height="{$imageHeight}" width="{$imageWidth}"
                                                style="z-index:500; position:absolute" id="imageMapGrid_{$qid}"
                                                useMap="#theMap{$qid}"/>
                                            <xsl:value-of select="mapHtml" disable-output-escaping="yes"/>
                                            <script language="javascript">
                                                setTimeout ('updateGridLocation (<xsl:value-of select="$qid"/>)', 1000);
                                            </script>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <img src="{$imageroot}/questionimages/{$imageMapFileName}" border="0"
                                                height="{$imageHeight}" width="{$imageWidth}"
                                                useMap="#theMap{$qid}"/>
                                            <xsl:value-of select="mapHtml" disable-output-escaping="yes"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
                        </table>
                        <input type="hidden" id="Q_{$qid}_default" value="{$default}"/>

                    </td>
                </xsl:when>

                <xsl:when test="$horizontalDisplay = 'true' and $horizDisplayBreak='true'">
                               <!-- if horizontal display is true it must be a radio or checkbox -->

                               <td colspan="2">
                               <table>
                                   <tr>
                                       <td  align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                                               <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
                                               <xsl:choose>
                                                   <xsl:when test="formQuestionAttributes/@required = 'true'">
                                                       <span class="requiredIndicator">*</span>
                                                   </xsl:when>
                                               </xsl:choose>

                                               <xsl:choose>
                                                   <xsl:when test="@displayText='true'">
                                                       <xsl:choose>
                                                           <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 0">
                                                               <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
                                                                   <xsl:value-of select="text" disable-output-escaping="yes"/>
                                                               </font>
                                                           </xsl:when>
                                                           <xsl:otherwise>
                                                               <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                                <xsl:value-of select="text" disable-output-escaping="yes"/>
                                                </span>
                                                           </xsl:otherwise>
                                                       </xsl:choose>
                                                   </xsl:when>
                                               </xsl:choose>
                                       </td>
                                   </tr>
                                   <tr>
                                   <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                                   <xsl:choose>
                                       <xsl:when test="@type = 'Radio'">
                                           <xsl:choose>
                                               <xsl:when test="$hasSkipRule = 'false'">
                                                   <xsl:for-each select="answers/answer">
                                                       <xsl:choose>
                                                           <xsl:when test="$default = display">
                                                               <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);">
                                                                   <xsl:value-of select="display"/>
                                                               </input>
                                                           </xsl:when>
                                                           <xsl:otherwise>
                                                               <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);">
                                                                   <xsl:value-of select="display"/>
                                                               </input>
                                                           </xsl:otherwise>
                                                       </xsl:choose>
                                                   </xsl:for-each>
                                               </xsl:when>
                                               <xsl:otherwise>
                                                   <xsl:for-each select="answers/answer">
                                                           <xsl:choose>
                                                               <xsl:when test="$default = display">
                                                                   <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}"  onfocus="checkButtons(this);" checked="true" title="{display/@displayToolTip}" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')">
                                                                       <xsl:value-of select="display"/>
                                                                   </input>
                                                               </xsl:when>
                                                               <xsl:otherwise>
                                                                   <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')">
                                                                       <xsl:value-of select="display"/>
                                                                   </input>
                                                               </xsl:otherwise>
                                                           </xsl:choose>
                                                       </xsl:for-each>
                                               </xsl:otherwise>
                                           </xsl:choose>
                                       </xsl:when>
                                       <xsl:when test="@type='Checkbox'">
                                           <xsl:choose>
                                               <xsl:when test="selected = 'true'">
                                                   <xsl:for-each select="answers/answer">
                                                       <xsl:choose>
                                                           <xsl:when test="selected = 'true'">
                                                               <input type="checkbox" id="Q_{$qid}" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);">
                                                                   <xsl:value-of select="display"/>
                                                               </input>
                                                           </xsl:when>
                                                           <xsl:otherwise>
                                                               <input type="checkbox" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);">
                                                                   <xsl:value-of select="display"/>
                                                               </input>
                                                           </xsl:otherwise>
                                                       </xsl:choose>
                                                   </xsl:for-each>
                                               </xsl:when>
                                               <xsl:otherwise>
                                                   <xsl:for-each select="answers/answer">
                                                       <xsl:choose>
                                                           <xsl:when test="selected = 'true'">
                                                               <input type="checkbox" id="Q_{$qid}" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')">
                                                                   <xsl:value-of select="display"/>
                                                               </input>
                                                           </xsl:when>
                                                           <xsl:otherwise>
                                                               <input type="checkbox" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')">
                                                                   <xsl:value-of select="display"/>
                                                               </input>
                                                           </xsl:otherwise>
                                                       </xsl:choose>
                                                   </xsl:for-each>


                                               </xsl:otherwise>
                                           </xsl:choose>
                                       </xsl:when>


                                   </xsl:choose>

                                   </td>
                                   </tr>

                               </table>
                               </td>

                               </xsl:when>

                <xsl:otherwise>
                    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                        <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
                        <xsl:choose>
                            <xsl:when test="formQuestionAttributes/@required = 'true'">
                                <span class="requiredIndicator">*</span>
                            </xsl:when>
                        </xsl:choose>
                        <xsl:choose>
                            <xsl:when test="@displayText='true'">
                                <xsl:choose>
                                    <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 0">
                                        <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
                                            <xsl:value-of select="text" disable-output-escaping="yes"/>
                                        </font>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                                <xsl:value-of select="text" disable-output-escaping="yes"/>
                                                </span>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                        </xsl:choose>
                    </td>
                    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="40%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                        <!-- DETERMINE IF QUESTION HAS SKIP RULE -->

                                <!-- QUESTION DOES NOT HAVE A SKIP RULE -->
                                <xsl:choose>

                                    <xsl:when test="@type = 'Textbox'">
                                        <input type="text" id="Q_{$qid}" size="{/formQuestionAttributes/textboxSize}" name="Q_{$qid}" value="{$default}" style="overflow:visible"/>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Textarea'" >
                                        <textarea id="Q_{$qid}" name="Q_{$qid}" style="overflow:visible">
                                            <xsl:value-of select="$default"/>
                                        </textarea>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Select'">

                                        <xsl:for-each select="answers/answer">
                                            <xsl:choose>
                                                <xsl:when test="$default = display">
                                                    <input type="radio" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <input type="radio" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <br/>
                                        </xsl:for-each>

                                    </xsl:when>
                                    <xsl:when test="@type = 'Multi-Select'">
                                            <xsl:for-each select="answers/answer">
                                                <xsl:choose>
                                                    <xsl:when test="selected = 'true'">
                                                        <input type="checkbox" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}">
                                                            <xsl:value-of select="display"/>
                                                        </input>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <input type="checkbox" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}">
                                                            <xsl:value-of select="display"/>
                                                        </input>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:for-each>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Radio'">



                                        <xsl:for-each select="answers/answer">
                                            <xsl:choose>
                                                <xsl:when test="$default = display">
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <xsl:choose>
                                                <xsl:when test="$horizontalDisplay!='true'">
                                                     <br/>
                                                </xsl:when>
                                            </xsl:choose>
                                        </xsl:for-each>


                                    </xsl:when>
                                    <xsl:when test="@type = 'Checkbox'">

                                        <xsl:for-each select="answers/answer">
                                            <xsl:choose>
                                                <xsl:when test="selected = 'true'">
                                                    <input type="checkbox" id="Q_{$qid}" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <input type="checkbox" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <xsl:choose>
                                                <xsl:when test="$horizontalDisplay!='true'">
                                                     <br/>
                                                </xsl:when>
                                            </xsl:choose>
                                        </xsl:for-each>

                                     </xsl:when>
                                    <xsl:when test="@type = 'Calculated'">
                                        <xsl:variable name="calc" select="@calculation"/>
                                        <xsl:variable name="answertype" select="@answertype"/>
                                        <input type="text" size="{$textboxSize}"  id="Q_{$qid}" value="{$default}" readonly="yes" name="Q_{$qid}" onfocus="calculate(this,'{$calc}', '{$answertype}')"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        Unknown Question Type: System Error
                                    </xsl:otherwise>
                                </xsl:choose>

                        <input type="hidden" id="Q_{$qid}_default" value="{$default}"/>
                    </td>
                </xsl:otherwise>
            </xsl:choose>
        </tr>
    </xsl:template>
</xsl:stylesheet>