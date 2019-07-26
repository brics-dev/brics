<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="webroot"/>
    <xsl:param name="imageroot"/>
    <xsl:param name="cssstylesheet"/>
    <xsl:param name="title"/>
    <xsl:param name="dictionaryWsRoot"/>
    <xsl:variable name="fontReduction" select="'smaller'"/>
    
    <xsl:output method="html" indent="yes"/>
    <xsl:template match="/">
        <xsl:apply-templates select="form"/>
    </xsl:template>

    <xsl:template match="form">
        <script language="Javascript">
            function updateGridLocation (qid) {
            id = 'imageMapImage_'+qid;
            img = document.getElementById(id);
            id = 'imageMapGrid_'+qid;
            grid = document.getElementById(id);
            spacerId = "imageMapRef_"+qid;
            spacer = document.getElementById (spacerId);
            img.style.left = spacer.offsetLeft;
            grid.style.left = spacer.offsetLeft;
            }
        </script>
        <table width="100%" style="border: {htmlAttributes/formBorder}px ridge;" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">
            <tr>
                <td style="font-family:{htmlAttributes/formFont}; color:{htmlAttributes/formColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    <span style="font-size:{$fontReduction}">
                    <xsl:value-of select="formHeader"  disable-output-escaping="yes"/>
                    </span>
                </td>
            </tr>
            <!-- <tr>
                <td> -->

                    <xsl:apply-templates select="row"/>
              <!--  </td>
            </tr> -->
            <tr>
                <td style="font-family:{htmlAttributes/formFont}; color:{htmlAttributes/formColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    <span style="font-size:{$fontReduction}">
                        <xsl:value-of select="formFooter"  disable-output-escaping="yes"/>
                    </span>
                </td>
            </tr>
        </table>
    </xsl:template>
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
        <table width="100%" style="border: {htmlAttributes/sectionBorder}px solid;" border="0" cellpadding="0" cellspacing="1">
            <tr>
                <td style="font-family: {htmlAttributes/sectionFont}; color: {htmlAttributes/sectionColor}; font-size:{/form/htmlAttributes/formFontSize}pt;">
                    <xsl:choose>
                        <xsl:when test="@textDisplayed = 'true'">
                            <span style="font-size:{$fontReduction}">
                                <xsl:value-of select="name"/>
                            </span>
                        </xsl:when>
                    </xsl:choose>
                </td>
            </tr>
            <tr>
                <td style="font-family:{htmlAttributes/sectionFont}; color: {htmlAttributes/sectionColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    <span style="font-size:{$fontReduction}">
                        <xsl:value-of select="instructionalText" disable-output-escaping="yes"/>
                    </span>
                </td>
            </tr>
            <tr>
                <td valign="top">
                    <table width="100%" border="0" cellpadding="0" cellspacing="1">
                        <xsl:apply-templates select="questions/question"/>
                    </table>
                </td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="question">
        <xsl:variable name="qid" select="@id"/>
        <xsl:variable name="default" select="defaultValue"/>
        <xsl:variable name="horizontalDisplay" select="formQuestionAttributes/horizontalDisplay"/>
        <xsl:variable name="horizDisplayBreak" select="formQuestionAttributes/horizontalDisplay/@horizDisplayBreak"/>
        <xsl:variable name="textboxSize" select="formQuestionAttributes/@textboxSize"/>
        

        <tr>
            <td colspan="2">
                <xsl:for-each select="images/filename">
                    <img src="{$imageroot}/questionimages/{.}" alt="Question Image" border="0"/>&#160;&#160;
                </xsl:for-each>
            </td>
        </tr>
        <tr>

            <tr>
                <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
                      <span style="font-size:{$fontReduction}">
                          <xsl:value-of select="formQuestionAttributes/label"/>
                    </span>
                </td>
            </tr>

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

                <xsl:when test="@type = 'Image Map'">
                    <xsl:variable name="imageHeight" select="@imageHeight"/>
                    <xsl:variable name="imageWidth" select="@imageWidth"/>
                    <xsl:variable name="imageMapFileName" select="@imageMapFileName"/>
                    <xsl:variable name="gridFileName" select="@gridFileName"/>
                    <td colspan="2">
                        <table width="100%">
                            <tr>
                                <td colspan="1" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">

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
                                <td colspan="1" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="40%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">


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
                                                setTimeout ('updateGridLocation (
                                                <xsl:value-of select="$qid"/>)', 1000);
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
                    </td>
                </xsl:when>

<xsl:otherwise>
    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="60%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
        <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
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
        <xsl:choose>
            <xsl:when test="formQuestionAttributes/@required = 'true'">
                <span style="font-size:{$fontReduction}">
                                (required)
                </span>
            </xsl:when>
        </xsl:choose>
    </td>
    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="40%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
        <xsl:choose>

            <xsl:when test="@type = 'Textbox'">
                <input type="text" name="Q_{$qid}" size="{/formQuestionAttributes/textboxSize}" value="{$default}"/>
            </xsl:when>
            <xsl:when test="@type = 'Textarea'">
                <textarea name="Q_{$qid}" style="height:{formQuestionAttributes/textareaHeight}px; width:{formQuestionAttributes/textareaWidth}px;" >
                    <xsl:value-of select="$default"/>
                </textarea>
            </xsl:when>
            <xsl:when test="(@type = 'Select')or (@type = 'Radio')">
                <xsl:for-each select="answers/answer">
                    <xsl:choose>
                        <xsl:when test="$default = display">
                            <input type="radio" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}">
                                <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                <span style="font-size:{$fontReduction}">
                                <xsl:value-of select="display"/>
                                    </span>
                                </span>
                            </input>
                        </xsl:when>
                        <xsl:otherwise>
                            <input type="radio" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}">
                                <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                <span style="font-size:{$fontReduction}">
                                <xsl:value-of select="display"/>
                                    </span>
                                </span>
                            </input>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="(@type = 'Multi-Select')  or (@type = 'Checkbox')">
                <xsl:for-each select="answers/answer">
                    <xsl:choose>
                        <xsl:when test="$default = display">
                            <input type="checkbox" name="Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}">
                                <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                <span style="font-size:{$fontReduction}">
                                <xsl:value-of select="display"/></span>
                                </span>
                            </input>
                        </xsl:when>
                        <xsl:otherwise>
                            <input type="checkbox" name="Q_{$qid}" value="{display}" title="{display/@displayToolTip}">
                                <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
                                <span style="font-size:{$fontReduction}">
                                <xsl:value-of select="display"/> </span>
                                </span>
                            </input>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>
<!--            <xsl:when test="@type = 'Radio'">
                        <xsl:for-each select="answers/answer">
                            <xsl:choose>
                                <xsl:when test="$default = display">
                                    <input type="radio" name="Q_{$qid}" value="{display}" checked="true">
                                        <xsl:value-of select="display"/>
                                    </input>
                                </xsl:when>
                                <xsl:otherwise>
                                    <input type="radio" name="Q_{$qid}" value="{display}">
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
                                <xsl:when test="$default = display">
                                    <input type="checkbox" name="Q_{$qid}" value="{display}" checked="true">
                                        <xsl:value-of select="display"/>
                                    </input>
                                </xsl:when>
                                <xsl:otherwise>
                                    <input type="checkbox" name="Q_{$qid}" value="{display}">
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
            -->
            <xsl:when test="@type = 'Calculated'">
                <input type="text" name="Q_{$qid}" size="{$textboxSize}"  disabled="true"/>
            </xsl:when>
            <xsl:otherwise>
                                Unknown Question Type: System Error
            </xsl:otherwise>
        </xsl:choose>
    </td>
</xsl:otherwise>
</xsl:choose>
</tr>
</xsl:template>
</xsl:stylesheet>
