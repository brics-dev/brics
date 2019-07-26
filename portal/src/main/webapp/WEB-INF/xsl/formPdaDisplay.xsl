<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:variable name="displayQids" select="/form/@displayQids"/>
    <xsl:variable name="displaytop" select="/form/TOC/@display"/>
    <xsl:variable name="formFontSize" select="/form/htmlAttributes/@formFontSize"/>
    <xsl:param name="webroot"/>
    <xsl:param name="imageroot"/>
    <xsl:param name="cssstylesheet"/>
    <xsl:param name="title"/>
    <xsl:variable name="fontReduction" select="'smaller'"/>
    <xsl:output method="html" indent="yes"/>

    <xsl:template match="/">
        <script language="JavaScript" type="text/javascript" src="/formbuilder/js/common/common.js"></script>
        <xsl:apply-templates select="form"/>
    </xsl:template>

    <xsl:template match="form">

        <table width="100%" style="border: {htmlAttributes/formBorder}px ridge; font-size:{/form/htmlAttributes/formFontSize}pt;" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">

            <tr>
                <td style="font-family: {htmlAttributes/formFont}; color: {htmlAttributes/formColor}; ">

                    <xsl:value-of select="name"/>
                    <span id="000"/>
                </td>
            </tr>
            <tr>
                <td style="font-family:{htmlAttributes/formFont}; color: {htmlAttributes/formColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    <xsl:value-of select="formHeader"  disable-output-escaping="yes"/>
                </td>
            </tr>
            <xsl:apply-templates select="TOC"/>

            <!--
          <tr>
              <td><xsl:value-of select="description"/></td>
          </tr>
          <tr>
              <tr><td><img src="{/formbuilder/images/spacer.gif" width="1" height="10" alt="" border="0"/></td></tr>
          </tr>
          -->
           <!-- <tr>
                <td>-->
                    <xsl:apply-templates select="row"/>
<!--                </td>
            </tr>-->
            <tr>
                <td style="font-family:{htmlAttributes/formFont}; color:{htmlAttributes/formColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    <xsl:value-of select="formFooter"  disable-output-escaping="yes"/>
                </td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="TOC">
        <xsl:choose>
            <xsl:when test="$displaytop = 'true'">
                <tr>
                    <td style="font-family:{htmlAttributes/formFont}; color: {htmlAttributes/formColor}; font-size:8pt">
                        <div style="font-size:12pt"><![CDATA[Table of Contents]]></div>
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
        <li>
            <a href="javascript:jumpto('{$tocsid}')">
                <xsl:value-of select="$tocdisp"/>
            </a>
        </li>
    </xsl:template>


    <xsl:template match="row">
        <!--<table width="100%">-->
            <tr style="font-size:{htmlAttributes/formFontSize};">
                <xsl:apply-templates select="formcell"/>
            </tr>
        <!--</table>                   -->
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
        <xsl:variable name="collapsable" select="@isCollapsable"/>
        <xsl:variable name="sectionid" select="@id"/>
            <xsl:choose>
                <xsl:when test="@isCollapsable = 'true'">
                    <table width="100%" cellspacing="0" cellpadding="0" border="0"><tr width="100%"><td  width="100%" align="right" valign="top" >
                        <DIV style="z-index:99;position:relative; top:17px; padding-left:10px; height:18px;" align="left"><xsl:value-of select="name"/></DIV>
                        <img style="position:relative; z-index:0;" src="{$imageroot}/sectionCollapseBkgd.gif" height="18" width="100%" BORDER="0"/>

 </td><td valign="bottom">
 <a href="javascript:;" onclick="toggleVisibility('sectionContainer_{$sectionid}', 'sectionImg_{$sectionid}');">
 	<img src="{$imageroot}/ctdbCollapse.gif" alt="expand / collapse" id="sectionImg_{$sectionid}" border="0" style="border:none" />
 </a>
     <script language="Javascript">
         setTimeout ( "toggleVisibility('sectionContainer_<xsl:value-of select="$sectionid"/>', 'sectionImg_<xsl:value-of select="$sectionid"/>');", 500);
     </script>
                        </td>
                         </tr>
                    </table>
                  </xsl:when>
        </xsl:choose>

                <div style="display:block;" id="sectionContainer_{$sectionid}" class="ctdbSectionContainer">


        <table width="100%" style="border: {htmlAttributes/sectionBorder}px solid;" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">
            <tr>
                <td style="font-family: {htmlAttributes/sectionFont}; color: {htmlAttributes/sectionColor};font-size:{/form/htmlAttributes/formFontSize}pt;">
                    <xsl:variable name="secid" select="sectionid"/>
                    <span id="{$secid}"/>
                    <xsl:choose>
                        <xsl:when test="@textDisplayed = 'true'">
                            <span style="font-size:{$fontReduction}">
                                <xsl:value-of select="name"/>
                            </span>
                        </xsl:when>
                    </xsl:choose>
                    <xsl:choose>
                        <xsl:when test="$displaytop = 'true'">
                            <xsl:choose>
                                <xsl:when test="@inTableOfContents = 'true'">
                                    <span>&#160;&#160;</span>
                                    <a href="javascript:jumpto('000')">
                                        <font color="{htmlAttributes/sectionColor}">top</font>
                                    </a>
                                </xsl:when>
                            </xsl:choose>
                        </xsl:when>
                    </xsl:choose>
                </td>
            </tr>
            <tr>
                <td style="font-family:{htmlAttributes/sectionFont}; color: {htmlAttributes/sectionColor}; font-size:{/form/htmlAttributes/formFontSize}pt;">
                    <span style="font-size:{$fontReduction}">
                        <xsl:value-of select="instructionalText" disable-output-escaping="yes"/>
                    </span>
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
        </div>
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
        <xsl:variable name="label" select="formQuestionAttributes/@label"/>



        <tr>
            <td colspan="1" id="Q_{$qid}_link">
                <hr style="height:1px; width:100%;" color="gray"/>
                <xsl:for-each select="images/filename">
                    <img src="{$imageroot}/questionimages/{.}" alt="Question Image" border="0"/>&#160;&#160;
                </xsl:for-each>
            </td>
        </tr>
        <xsl:if test="$label != ''">
        <tr>
            <td colspan="2" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:8pt">
                <img src="{/formbuilder/images/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/><xsl:value-of select="formQuestionAttributes/label"/>
            </td>
        </tr>
        </xsl:if>
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
                                        <input class="slider-input"  id="Q_{$qid}" name="Q_{$qid}" style="display: none" />
                                    </div>
                                    <script language="javascript">
                                        $("#Q_<xsl:value-of select="$qid"/>_slider").slider({
                                    		min: <xsl:value-of select="$scaleMin"/>,
                                    		max: <xsl:value-of select="$scaleMax"/>
                                    	});
                                        /*var _sl<xsl:value-of select="$qid"/> = new Slider(document.getElementById("Q_<xsl:value-of select="$qid"/>_slider"), document.getElementById("Q_<xsl:value-of select="$qid"/>"), 'horizontal', '<xsl:value-of select="$showHandle"/>' );
                                        _sl<xsl:value-of select="$qid"/>.setMinimum(<xsl:value-of select="$scaleMin"/>);
                                        _sl<xsl:value-of select="$qid"/>.setMaximum(<xsl:value-of select="$scaleMax"/>);
                                         try {
                                        _sl<xsl:value-of select="$qid"/>.setValue(<xsl:value-of select="$default"/>);
                                        } catch (err) {}*/                                    
                                        </script>
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
            <!-- Check to see if it is a patient calendar -->
                <xsl:when test="@type = 'Image Map'">
                    <xsl:variable name="imageHeight" select="@imageHeight"/>
                    <xsl:variable name="imageWidth" select="@imageWidth"/>
                    <xsl:variable name="imageMapFileName" select="@imageMapFileName"/>
                    <xsl:variable name="gridFileName" select="@gridFileName"/>
                    <td colspan="2">
                        <table width="100%">
                            <tr>
                                <td colspan="1" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">

                                    <img src="{/formbuilder/images/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
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
                                                        <span style="font-size:{$fontReduction}">
                                                        <xsl:value-of select="text" disable-output-escaping="yes"/>
                                                        </span>
                                                    </font>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                                    <span style="font-size:{$fontReduction}">
                                                         <xsl:value-of select="text" disable-output-escaping="yes"/>
                                                        </span>
                                                </span>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                    </xsl:choose>
                                </td>
                                <td>
                                    <img src="{$imageroot}/buttonRemove.png" alt="Delete" title="Delete"
                                        border="0" width="23" height="23"
                                        onclick="removeItemMacOpt (document.getElementById('imageMap_{$qid}'), 'NO');"/>
                                </td>
                                <td>
                                    <select id="imageMap_{$qid}" size="2" name="Q_{$qid}" class="ctdbImageMapAnswers" multiple="true">
                                        <option value="{$default}" selected="selected">
                                            <xsl:value-of select="$default"/>
                                        </option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="3" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="40%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">


                                    <xsl:choose>
                                        <xsl:when test="@displayGrid = 'true'">
                                            <img src="{/formbuilder/images/spacer.gif" border="0"
                                                height="1" width="1"
                                                id="imageMapRef_{$qid}" style="z-index:-100; position:absolute"/>
                                            <img src="{/formbuilder/images/spacer.gif" border="0"
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

                <xsl:otherwise>
                    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                        <img src="{/formbuilder/images/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
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
                                        <span style="font-size:{$fontReduction}">
                                            <xsl:value-of select="text" disable-output-escaping="yes"/>
                                        </span>
                                        </font>
                                    </xsl:when>
                                    <xsl:otherwise>
                                         <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                        <span style="font-size:{$fontReduction}">
                                                <xsl:value-of select="text" disable-output-escaping="yes"/>
                                            </span>
                                                </span>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <br/> <!-- all question objects on line below questiontext -->
                            </xsl:when>
                        </xsl:choose>
                   <!-- </td>
                    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="40%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                    -->
                        <!-- DETERMINE IF QUESTION HAS SKIP RULE -->
                        <xsl:choose>
                            <xsl:when test="$hasSkipRule = 'false'">
                                <!-- QUESTION DOES NOT HAVE A SKIP RULE -->
                                <xsl:choose>

                                    <xsl:when test="@type = 'Textbox'">
                                        <input type="text" id="Q_{$qid}" name="Q_{$qid}" value="{$default}" class="catfood" size="{$textboxSize}" onfocus="checkButtons(this);"/>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Textarea'">
                                          <textarea id="Q_{$qid}" name="Q_{$qid}" class="resizeMe" style="width:90%;" rows="2"  onfocus="checkButtons(this);">
                                                    <xsl:value-of select="$default"/>
                                                </textarea>
                                    </xsl:when>
                                <xsl:when test="@type = 'Select'">
                                    <span style="font-size:{$fontReduction}">
                                      <xsl:for-each select="answers/answer">

                                            <xsl:choose>
                                                <xsl:when test="$default = display">
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" checked="true" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:for-each>
                                    </span>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Multi-Select'">
                                        <span style="font-size:{$fontReduction}">
                                        <xsl:for-each select="answers/answer">
                                                    <xsl:choose>
                                                        <xsl:when test="$default = display">
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
                                        </span>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Radio'">
                                        <span style="font-size:{$fontReduction}">

                                        <xsl:for-each select="answers/answer">

                                            <xsl:choose>
                                                <xsl:when test="$default = display">
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:for-each>
                                        </span>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Checkbox'">
                                        <span style="font-size:{$fontReduction}">

                                          <xsl:for-each select="answers/answer">
                                                    <xsl:choose>
                                                        <xsl:when test="$default = display">
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
                                        </span>
                                    </xsl:when>

                                    <xsl:when test="@type = 'Calculated'">
                                        <xsl:variable name="calc" select="@calculation"/>
                                        <xsl:variable name="answertype" select="@answertype"/>
                                        <input type="text" id="Q_{$qid}" readonly="yes" size="{$textboxSize}"  name="Q_{$qid}" onfocus="calculate(this, '{$calc}', '{$answertype}')"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        Unknown Question Type: System Error
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>

                            <xsl:otherwise>
                                <!-- QUESTION HAS A SKIP RULE -->
                                <xsl:choose>

                                    <xsl:when test="@type = 'Textbox'">
                                        <input type="text" id="Q_{$qid}" size="{/formQuestionAttributes/textboxSize}" name="Q_{$qid}" value="{$default}" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')" onfocus="checkButtons(this);"/>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Textarea'">
                                        <table>
                                            <tr><td align="left">
                                                <span class="smallText"><a href="Javascript:doNothing();" onClick="toggleOverflow ('Q_{$qid}');">expand/collapse</a></span>
                                            </td></tr>
                                            <tr><td>
                                                <textarea id="Q_{$qid}" name="Q_{$qid}" class="resizeMe" style="height:{formQuestionAttributes/textareaHeight}px; width:{formQuestionAttributes/textareaWidth}px;"  onfocus="checkButtons(this);">
                                                    <xsl:value-of select="$default"/>
                                                </textarea>
                                            </td></tr>
                                        </table>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Select'">
                                           <span style="font-size:{$fontReduction}">
                                      <xsl:for-each select="answers/answer">

                                            <xsl:choose>
                                                <xsl:when test="$default = display">
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);"  onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')" onMouseDown="toggleRadio(this);">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);"  onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')" onMouseDown="toggleRadio(this);">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:for-each>
                                    </span>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Multi-Select'">
                                        <span style="font-size:{$fontReduction}">

                                        <xsl:for-each select="answers/answer">
                                            <xsl:choose>
                                                <xsl:when test="$default = display">
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
                                        </span>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Radio'">
                                        <span style="font-size:{$fontReduction}">

                                        <xsl:for-each select="answers/answer">

                                            <xsl:choose>
                                                <xsl:when test="$default = display">
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" checked="true" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')" onMouseDown="toggleRadio(this);">
                                                        <xsl:value-of select="display"/>
                                                    </input>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <input type="radio" id="Q_{$qid}" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')" onMouseDown="toggleRadio(this);">
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
                                         </span>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Checkbox'">
                                        <span style="font-size:{$fontReduction}">

                                        <xsl:for-each select="answers/answer">
                                            <xsl:choose>
                                                <xsl:when test="$default = display">
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
                                            <xsl:choose>
                                                 <xsl:when test="$horizontalDisplay!='true'">
                                                     <br/>
                                                 </xsl:when>
                                            </xsl:choose>
                                        </xsl:for-each>
                                        </span>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Calculated'">
                                        <xsl:variable name="calc" select="@calculation"/>
                                        <xsl:variable name="answertype" select="@answertype"/>
                                        <input type="text" id="Q_{$qid}" name="Q_{$qid}" readonly="yes" size="{$textboxSize}"  onfocus="calculate(this,'{$calc}', '{@answertype}')" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        Unknown Question Type: System Error
                                    </xsl:otherwise>
                                </xsl:choose>

                            </xsl:otherwise>
                        </xsl:choose>
                        <input type="hidden" id="Q_{$qid}_default" value="{$default}"/>
                    </td>
                </xsl:otherwise>
            </xsl:choose>
        </tr>

    </xsl:template>


</xsl:stylesheet>
