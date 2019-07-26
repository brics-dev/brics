<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:variable name="displayQids" select="/form/@displayQids" />
	<xsl:variable name="displaytop" select="/form/TOC/@display" />
	<xsl:variable name="formFontSize" select="/form/htmlAttributes/@formFontSize" />
	<xsl:param name="webroot" />
	<xsl:param name="imageroot" />
	<xsl:param name="cssstylesheet" />
	<xsl:param name="title" />
	<xsl:param name="dictionaryWsRoot" />
	<xsl:output method="html" indent="yes" />

	<xsl:template match="/">
		<style type="text/css">
			textarea, input[type="date"],
			input[type="datetime"],
			input[type="datetime-local"],
			input[type="email"],
			input[type="month"], input[type="number"],
			input[type="password"],
			input[type="search"], input[type="tel"],
			input[type="text"],
			input[type="time"], input[type="url"],
			input[type="week"] {
			width: 300px; margin-bottom: 2px;}
			select{width:
			-1; margin-bottom: 2px;}
		</style>
		<script type="text/javascript">
			function loadjs(url) {
				var fileref = document.createElement('script');
				fileref.setAttribute('type', 'text/javascript');
				fileref.setAttribute('src', url);
				document.getElementsByTagName("head")[0].appendChild(fileref);
			}

			if (typeof jQuery == "undefined") {
				loadjs("{$webroot}/common/js/jquery-1.7.2.min.js");
				loadjs("{$webroot}/common/js/jquery-ui-1.10.3.custom.min.js");
				loadjs("{$webroot}/common/common.js");
				loadjs("{$webroot}/common/js/formDisplay.js");
			}

			function displayOther(object){
				var otherName;
				var flag=true;
				var noSelect=true;
				for(var i = 0;  object.options.length > i; i++){
					if(object.options[i].value=='Other, please specify'){
						otherName=object.name;
					}
					if(object.options[i].selected){
						if(object.options[i].value=='Other, please specify'){
							$("#"+object.id+"_otherBox").show();
							flag=false;
						}else{
							$("#"+object.id+"_otherBox").hide();
						}
						noSelect=false;
					}
				}
				if(flag){
					$("#"+otherName+"_otherBox").val('');
				}
				if(noSelect){
					$("#"+otherName+"_otherBox").hide();
					$("#"+otherName+"_otherBox").val('');
				}
			}
			
			function displayRadioOther(object){
				if(object.value=='Other, please specify'){
					$("#"+object.id+"_otherBox").attr("disabled",false);
				}else{
					$("#"+object.id+"_otherBox").val('');
					$("#"+object.id+"_otherBox").attr("disabled",true);
				}
			}
			
			function displayCheckOther(name){
				var flag=true;
				if($('input[name="'+name+'"]').is(':checked')){
					$('input[name="'+name+'"]:checked').each(function(){
						if($(this).val()=='Other, please specify'){
							$("#"+name+"_otherBox").attr("disabled",false);
							flag=false;
						}else{
							$("#"+name+"_otherBox").attr("disabled",true);
						}
					});
				}else{
					$("#"+name+"_otherBox").val('');
					$("#"+name+"_otherBox").attr("disabled",true);
				}
				if(flag){
					$("#"+name+"_otherBox").val('');
				}
			}

			$(document).ready(function() {
				$('.ctdbSectionContainer').hide();
				$('div[name="Main"]').show();
				$('div[name="Form Administration"]').show();
				if($('div[name="Form Administration"]').length > 0){
					$('#mainGo').parent().closest('tr').hide();
				}
				<!-- $('#goPROMIS').click(function(){
					$('div[name="Main"]').hide();
					$('#acknowledgement').hide();
					initiateCATs();
				});
				
				$("#promis_saveLock").click(function(){
					$("input:checkbox[name='markChecked']").prop( "checked", true );
					$("#lockButtonPatient").click();
				}); -->
			});			
		</script>		
		<xsl:apply-templates select="form" />
	</xsl:template>

	<xsl:template match="form">		
	<xsl:variable name="theStyle" select="@theStyle" />
		<table width="100%"
			style="border: {htmlAttributes/formBorder}px ridge; font-size:{/form/htmlAttributes/formFontSize}pt;"
			border="0" cellpadding="{/form/htmlAttributes/cellpadding}"
			cellspacing="2">
			<tr align="center"> <!-- Form header -->
				<td style="font-family:{htmlAttributes/formFont}; color: {htmlAttributes/formColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
					<input type="hidden" id="OID" value="{catOid}"/>
					<input type="hidden" id="measurementType" value="{measurementType}"/>
					
					<xsl:value-of select="formHeader"
						disable-output-escaping="yes" />
					<xsl:choose>
						<xsl:when test="formHeader != ''">
							<hr color="lightgrey" />
						</xsl:when>
					</xsl:choose>
				</td>
			</tr>
			<tr align="left">  <!-- Form Name -->
				<td
					style="font-family: {htmlAttributes/formFont}; color: {htmlAttributes/formColor}; ">
					<h1 class="formName">
						<xsl:value-of select="name" />
					</h1>
					<span id="000" />
				</td>
			</tr>
			<xsl:apply-templates select="TOC" />

			<tr align="left"> <!-- Form description -->
				<td style="font-family: {htmlAttributes/formFont}; color: {htmlAttributes/formColor};">
					<I>
						<xsl:value-of select="description" />
					</I>
				</td>				
			</tr>
			<tr id='acknowledgement'>
				<td>
					<I>
						<b>Acknowledgement: </b>PROMIS Health Organization and Assessment Center <sup>SM</sup>: <a href="https://assessmentcenter.net/documents/Assessment%20Center%20Terms%20and%20Conditions%20v7.1.pdf" target="_blank">View full acknowledgement</a>
					</I>
				</td>
			</tr>
			<tr align="left">
				<td></td>
			</tr>
			<tr> <!-- each row (contain sections) -->
				<td>
					<xsl:apply-templates select="row" />
				</td>
			</tr>
			<tr align="center"> <!-- Form Footer -->
				<td
					style="font-family:{htmlAttributes/formFont}; color:{htmlAttributes/formColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
					<xsl:value-of select="formFooter"
						disable-output-escaping="yes" />
				</td>
			</tr>
		</table>
	</xsl:template>

	<xsl:template match="TOC">
		<xsl:choose>
			<xsl:when test="$displaytop = 'true'">
				<tr>
					<td
						style="font-family:{htmlAttributes/formFont}; color: {htmlAttributes/formColor}; font-size:8pt">
						<div style="font-size:12pt"><![CDATA[Table of Contents]]></div>
						<ul>
							<xsl:apply-templates select="TOCListing" />
						</ul>
					</td>
				</tr>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="TOCListing">
		<xsl:variable name="tocsid" select="@sectionid" />
		<xsl:variable name="tocdisp" select="@displayvalue" />
		<li>
			<a href="javascript:jumpto('{$tocsid}')">
				<xsl:value-of select="$tocdisp" />
			</a>
		</li>
	</xsl:template>

	<xsl:template match="row">
		<table width="100%" class="rowTable">
			<tr style="font-size:{htmlAttributes/formFontSize};" class="rowTR">
				<xsl:apply-templates select="formcell" />
			</tr>
		</table>
	</xsl:template>

	<xsl:template match="formcell">
		<xsl:variable name="theStyle" select="@theStyle" />
		<xsl:variable name="theRowSpan" select="@theRowSpan" />
		<xsl:variable name="theColSpan" select="@theColSpan" />
		<xsl:variable name="visible" select="@visible" />
		<xsl:variable name="visibleSection" select="@visibleSection" />
		<xsl:variable name="isRepeatable" select="@isRepeatable" />
		<xsl:variable name="parentValue" select="@parentValue" />
		<xsl:variable name="formRow" select="@formRow" />
		<xsl:variable name="secId" select="@secId" />
		<xsl:variable name="minimumValue" select="@minimumValue" />
		<xsl:variable name="maximumValue" select="@maximumValue" />


		<td style="vertical-align: top;{$theStyle}" rowspan="{$theRowSpan}"
			colspan="{$theColSpan}">   <!-- put the section into a cell, set the td size -->
			<xsl:choose>
				<xsl:when test="@isRepeatable='false'">
					<xsl:apply-templates select="section" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="@visible='true'">
							<div id="{$secId}" parent="{$parentValue}" formrow="{$formRow}"
								maxdisplay="{$maximumValue}" initdisplay="{$minimumValue}">
								<xsl:apply-templates select="section" />
							</div>
						</xsl:when>
						<xsl:otherwise>
							<div id="{$secId}" style='display:none' parent="{$parentValue}"
								formrow="{$formRow}" maxdisplay="{$maximumValue}" initdisplay="{$maximumValue}">
								<xsl:apply-templates select="section" />
							</div>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
			
			<tr style="display:none;" id='thanks'>
				<td>
					<h3></h3>
					<span><b>Thank you for taking this survey.</b></span>
					<br/>
					<span>Have a nice day!</span>
					<br/>
					<h3></h3><h3></h3>
					<input type="button" id="promis_saveLock" value="Save and Lock"/>
					<span>&#160;&#160;</span>
					<input type="button" id="promis_cancel" value="Cancel"/>
				</td>
			</tr>			
		</td>
	</xsl:template>

	<xsl:template match="section">
		<xsl:variable name="collapsable" select="@isCollapsable" />
		<xsl:variable name="gridType" select="@isGridType" />
		<xsl:variable name="tableGroupId" select="@tableGroupId" />
		<xsl:variable name="tableHeaderType" select="@tableHeaderType" />
		<xsl:variable name="sectionid" select="@id" />
		<xsl:variable name="minimumValue" select="minimumValue" />
		<xsl:variable name="maximumValue" select="maximumValue" />
		<xsl:variable name="buttonCount" select="buttonCount" />
		<xsl:variable name="isSecRepeatable" select="isSecRepeatable" />
		<xsl:variable name="isCurrentLast" select="isCurrentLast" />
		<xsl:variable name="repeatSecCount" select="repeatSecCount" />
		<xsl:variable name="parentValue" select="parentValue" />
		<xsl:variable name="formRow" select="formRow" />
		<xsl:variable name="tableHeaderClassName" select="@tableHeaderClassName" />
		<xsl:variable name="maxNoofColumnForEachRowInSection"
			select="@maxNoofColumnForEachRowInSection" />
		<div style="display:block;overflow: auto;" id="sectionContainer_{$sectionid}"
			class="ctdbSectionContainer {$tableHeaderClassName} " hideid="sectionContainer_{$sectionid}" name="{name}">
			<table class="sectionContainerTable {$tableHeaderClassName}"
				width="100%" style="height: 100%;border:{htmlAttributes/sectionBorder}px solid;"
				cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2" name="t_{name}">

				<tr class="sectionNameTr" align='left'>
					<td class="sectionNameTd"
						style="height: 19px; font-family: {htmlAttributes/sectionFont}; color: {htmlAttributes/sectionColor};font-size:{/form/htmlAttributes/formFontSize}pt;"
						colspan="{$maxNoofColumnForEachRowInSection}">
						<xsl:choose>
							<xsl:when test="@isGridType != 'true'">
								<xsl:variable name="secid" select="sectionid" />
								<span id="{$secid}" />
								<xsl:choose>
									<xsl:when test="@textDisplayed = 'true'">
										<h3 class="sectionName">
											<xsl:value-of select="name" />
										</h3>   <!-- section Name -->
										<xsl:choose>
											<xsl:when test="description != ''">
												<br />
												<I>
													<xsl:value-of select="description" />
												</I><!-- fix the description won't show bug -->
											</xsl:when>
										</xsl:choose>
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
							</xsl:when>
						</xsl:choose>
					</td>
				</tr>
				<tr>
					<td valign="top">
						<xsl:choose>
							<xsl:when test="name='Main'">
								<xsl:for-each select="sectionRows">
								<tr>
									<xsl:apply-templates select="questions/question"/>
								</tr>							
								</xsl:for-each>
								<tr>
									<td COLSPAN='2'>
										<div  style="text-align:center;">
											<h3></h3>
											<h3></h3>
											<input type="button" value="Start Measurement >>" class="goPROMIS" id="mainGo"/>
										</div>
									</td>
								</tr>
							</xsl:when>
							<xsl:when test="name='Form Administration'">
								<xsl:for-each select="sectionRows">
								<tr>
									<xsl:apply-templates select="questions/question"/>
								</tr>							
								</xsl:for-each>
								<tr>
									<td COLSPAN='2'>
										<div  style="text-align:center;">
											<h3></h3>
											<h3></h3>
											<input type="button" value="Start Measurement >>" class="goPROMIS" id="faGo"/>
										</div>
									</td>
								</tr>
							</xsl:when>
							<xsl:otherwise><!-- Final Results and PROMIS groups -->
								<xsl:for-each select="sectionRows">
									<tr style="display:none;">
										<xsl:apply-templates select="questions/question"/>
									</tr>									
								</xsl:for-each>				
							</xsl:otherwise>
						</xsl:choose>
						
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>
	<xsl:template match="question">
		<xsl:variable name="qid" select="@id" />
		<xsl:variable name="skipOperator" select="formQuestionAttributes/@skipOperator" />
		<xsl:variable name="hasSkipRule" select="formQuestionAttributes/@hasSkipRule" />
		<xsl:variable name="skipRule" select="formQuestionAttributes/@skipRule" />
		<xsl:variable name="skipRuleEquals" select="formQuestionAttributes/@skipEquals" />
		<xsl:variable name="default" select="defaultValue" />
		<xsl:variable name="questionsToSkip"
			select="formQuestionAttributes/questionsToSkip" />
		<xsl:variable name="horizontalDisplay"
			select="formQuestionAttributes/horizontalDisplay" />
		<xsl:variable name="horizDisplayBreak"
			select="formQuestionAttributes/horizontalDisplay/@horizDisplayBreak" />
		<xsl:variable name="textboxSize" select="formQuestionAttributes/@textboxSize" />
		<xsl:variable name="questionSectionNode" select="questionSectionNode" />
		<xsl:variable name="bgColor" select="bgColor" />
		<xsl:variable name="scoreStr" select="scoreStr" />
		<xsl:variable name="blankOption" select="blankOption" />
		<xsl:variable name="floorColSpanTD" select="@floorColSpanTD" />
		<xsl:variable name="showTextClassName"
			select="formQuestionAttributes/@showTextClassName" />
		<xsl:variable name="tableHeaderType"
			select="formQuestionAttributes/@tableHeaderType" />
		<xsl:variable name="widthTD" select="@widthTD" />
		<xsl:variable name="htmltext" select="formQuestionAttributes/@htmltext" />
		<xsl:variable name="parentSectionName" select="parentSectionName" />
		<xsl:variable name="catOid" select="catOid" />

		<td colspan="{$floorColSpanTD}" style="width:{$widthTD}%;"
			class="questionContainerTD {$showTextClassName} " id="{$catOid}" secId="{$questionSectionNode}" hideid="questionContainer_{questionSectionNode}_{$qid}">
			<table class="sectionquestionscellQuestionContainerTable">
				<tr bgcolor="{$bgColor}" class="questionTR" onmouseover="this.bgColor= '#ffea8a';"
					onmouseout="this.bgColor= '{$bgColor}';">
					<xsl:choose>
						<xsl:when test="@type = 'Textblock'">
		               		<td colspan="2" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}">
		               		  <span class="textblock"><xsl:value-of select="formQuestionAttributes/@htmltext" disable-output-escaping="yes"/></span>
		               		</td>
		                </xsl:when>
						<xsl:when test="$horizDisplayBreak='true'">
							<!-- if horizontal display is true it must be a radio or checkbox -->
							<td colspan="2">
								<div class="quetionContain">
									<table>
										<tr>
											<td class="questionTextContainerTd" align="{formQuestionAttributes/htmlAttributes/align}"
												valign="{formQuestionAttributes/htmlAttributes/valign}"
												width="50%"
												style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
												<img src="{$imageroot}/spacer.gif"
													width="{formQuestionAttributes/htmlAttributes/indent}"
													height="1" alt="" border="0" />
												<xsl:choose>
													<xsl:when test="formQuestionAttributes/@required = 'true'">
														<span style="color:red">*</span>
													</xsl:when>
												</xsl:choose>
												<xsl:choose>
													<xsl:when test="$displayQids='true'">
														<span>
															Id: [
															<xsl:value-of select="$qid" />
															]
															<br />
															Name: [
															<xsl:value-of select="name" />
															]
															<br />
														</span>
													</xsl:when>
												</xsl:choose>
												<xsl:choose>
													<xsl:when test="@displayText='true'">
														<xsl:choose>
															<xsl:when
																test="formQuestionAttributes/htmlAttributes/fontSize != 2">
																<xsl:choose>
																	<xsl:when test="@upDescription ='true'">
																		<span
																			style="font-family: arial; color: black;font-size:10pt">
																			<xsl:value-of select="descriptionUp"
																				disable-output-escaping="yes" />
																		</span>
																		<br />
																		<br />
																	</xsl:when>
																</xsl:choose>
																<font size="{formQuestionAttributes/htmlAttributes/fontSize}"
																	class="questionTextImmediateContainer">
																	<xsl:value-of select="text"
																		disable-output-escaping="yes" />
																</font>
																<br />
																<br />
																<xsl:choose>
																	<xsl:when test="@downDescription ='true'">
																		<span
																			style="font-family: arial; color: black;font-size:10pt">
																			<xsl:value-of select="descriptionDown"
																				disable-output-escaping="yes" />
																		</span>
																	</xsl:when>
																</xsl:choose>
															</xsl:when>
															<xsl:otherwise>
																<xsl:choose>
																	<xsl:when test="@upDescription ='true'">
																		<span
																			style="font-family: arial; color: black;font-size:10pt">
																			<xsl:value-of select="descriptionUp"
																				disable-output-escaping="yes" />
																		</span>
																		<br />
																		<br />
																	</xsl:when>
																</xsl:choose>
																<span style="font-size:{/form/htmlAttributes/formFontSize}pt"
																	class="questionTextImmediateContainer">
																	<xsl:value-of select="text"
																		disable-output-escaping="yes" />
																</span>
																<xsl:choose>
																	<xsl:when test="@downDescription ='true'">
																		<span
																			style="font-family: arial; color: black;font-size:10pt">
																			<xsl:value-of select="descriptionDown"
																				disable-output-escaping="yes" />
																		</span>
																	</xsl:when>
																</xsl:choose>
															</xsl:otherwise>
														</xsl:choose>
													</xsl:when>
												</xsl:choose>
											</td>
										</tr>
										<tr>
											<td class="questionInputTD" align="{formQuestionAttributes/htmlAttributes/align}"
												valign="{formQuestionAttributes/htmlAttributes/valign}"
												style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt;">
												<xsl:choose>
													<xsl:when test="$horizontalDisplay = 'true'">
														<br class="horizontalDisplay" style="display:none" />
													</xsl:when>
													<xsl:otherwise>
														<br class="verticalDisplay" style="display:none" />
													</xsl:otherwise>
												</xsl:choose>
												<xsl:choose>													
													<xsl:when test="@type = 'Radio'">
														<xsl:choose>
															<xsl:when test="$hasSkipRule = 'false'">
																<xsl:choose>
																	<xsl:when
																		test="formQuestionAttributes/htmlAttributes/fontSize != 2">
																		<font
																			size="{formQuestionAttributes/htmlAttributes/fontSize}">
																			<xsl:for-each select="answers/answer">
																				<xsl:choose>
																					<xsl:when test="$default = display">
																						<label>
																							<input type="radio"
																								id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																								value="{display}" checked="true" onfocus="checkButtons(this);"
																								onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" 
																								responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																							<div class="questionText">
																								<xsl:value-of select="display" />
																							</div>
																						</label>
																						<xsl:choose>
																							<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																								<input type="text"
																									id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									disabled="true" />
																							</xsl:when>
																						</xsl:choose>
																					</xsl:when>
																					<xsl:otherwise>
																						<label>
																							<input type="radio"
																								id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																								value="{display}" onfocus="checkButtons(this);"
																								onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" 
																								responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																							<div class="questionText">
																								<xsl:value-of select="display" />
																							</div>
																						</label>
																						<xsl:choose>
																							<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																								<input type="text"
																									id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									disabled="true" />
																							</xsl:when>
																						</xsl:choose>
																					</xsl:otherwise>
																				</xsl:choose>
																				<xsl:choose>
																					<xsl:when test="$horizontalDisplay!='true'">
																						<br />
																					</xsl:when>
																				</xsl:choose>
																			</xsl:for-each>
																		</font>
																	</xsl:when>
																	<xsl:otherwise>
																		<xsl:for-each select="answers/answer">
																			<xsl:choose>
																				<xsl:when test="$default = display">
																					<label>
																						<input type="radio"
																							id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																							value="{display}" checked="true" onfocus="checkButtons(this);"
																							onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" 
																							responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																						<div class="questionText">
																							<xsl:value-of select="display" />
																						</div>
																					</label>
																					<xsl:choose>
																						<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																							<input type="text"
																								id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								disabled="true" />
																						</xsl:when>
																					</xsl:choose>
																				</xsl:when>
																				<xsl:otherwise>
																					<label>
																						<input type="radio"
																							id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																							value="{display}" onfocus="checkButtons(this);"
																							onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" 
																							responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																						<div class="questionText">
																							<xsl:value-of select="display" />
																						</div>
																					</label>
																					<xsl:choose>
																						<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																							<input type="text"
																								id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								disabled="true" />
																						</xsl:when>
																					</xsl:choose>
																				</xsl:otherwise>
																			</xsl:choose>
																			<xsl:choose>
																				<xsl:when test="$horizontalDisplay!='true'">
																					<br />
																				</xsl:when>
																			</xsl:choose>
																		</xsl:for-each>
																	</xsl:otherwise>
																</xsl:choose>
															</xsl:when>
															<xsl:otherwise>
																<xsl:choose>
																	<xsl:when
																		test="formQuestionAttributes/htmlAttributes/fontSize != 2">
																		<font
																			size="{formQuestionAttributes/htmlAttributes/fontSize}">
																			<xsl:for-each select="answers/answer">
																				<xsl:choose>
																					<xsl:when test="$default = display">
																						<label>
																							<input type="radio"
																								id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																								value="{display}" onfocus="checkButtons(this);"
																								checked="true"
																								ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])"
																								onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)"
																								onMouseDown="toggleRadio(this);" 
																								responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																							<div class="questionText">
																								<xsl:value-of select="display" />
																							</div>
																						</label>
																						<xsl:choose>
																							<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																								<input type="text"
																									id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									disabled="true" />
																							</xsl:when>
																						</xsl:choose>
																					</xsl:when>
																					<xsl:otherwise>
																						<label>
																							<input type="radio"
																								id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																								value="{display}" onfocus="checkButtons(this);"
																								ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])"
																								onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)"
																								onMouseDown="toggleRadio(this);" 
																								responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																							<div class="questionText">
																								<xsl:value-of select="display" />
																							</div>
																						</label>
																						<xsl:choose>
																							<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																								<input type="text"
																									id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									disabled="true" />
																							</xsl:when>
																						</xsl:choose>
																					</xsl:otherwise>
																				</xsl:choose>
																				<xsl:choose>
																					<xsl:when test="$horizontalDisplay!='true'">
																						<br />
																					</xsl:when>
																				</xsl:choose>
																			</xsl:for-each>
																		</font>
																	</xsl:when>
																	<xsl:otherwise>
																		<xsl:for-each select="answers/answer">
																			<xsl:choose>
																				<xsl:when test="$default = display">
																					<label>
																						<input type="radio"
																							id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																							value="{display}" onfocus="checkButtons(this);"
																							checked="true"
																							ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])"
																							onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)"
																							onMouseDown="toggleRadio(this);" 
																							responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																						<div class="questionText">
																							<xsl:value-of select="display" />
																						</div>
																					</label>
																					<xsl:choose>
																						<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																							<input type="text"
																								id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								disabled="true" />
																						</xsl:when>
																					</xsl:choose>
																				</xsl:when>
																				<xsl:otherwise>
																					<label>
																						<input type="radio"
																							id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																							value="{display}" onfocus="checkButtons(this);"
																							ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])"
																							onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)"
																							onMouseDown="toggleRadio(this);" 
																							responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																						<div class="questionText">
																							<xsl:value-of select="display" />
																						</div>
																					</label>
																					<xsl:choose>
																						<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																							<input type="text"
																								id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								disabled="true" />
																						</xsl:when>
																					</xsl:choose>
																				</xsl:otherwise>
																			</xsl:choose>
																			<xsl:choose>
																				<xsl:when test="$horizontalDisplay!='true'">
																					<br />
																				</xsl:when>
																			</xsl:choose>
																		</xsl:for-each>
																	</xsl:otherwise>
																</xsl:choose>
															</xsl:otherwise>
														</xsl:choose>
														<input type="hidden"
															id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox"
															value="{scoreStr}" />
													</xsl:when>
													<xsl:when test="@type='Checkbox'">
														<xsl:choose>
															<xsl:when test="$hasSkipRule = 'false'">
																<xsl:choose>
																	<xsl:when
																		test="formQuestionAttributes/htmlAttributes/fontSize != 2">
																		<font
																			size="{formQuestionAttributes/htmlAttributes/fontSize}">
																			<xsl:for-each select="answers/answer">
																				<xsl:choose>
																					<xsl:when test="$default = display">
																						<label>
																							<input type="checkbox"
																								id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																								value="{display}" checked="true" onfocus="checkButtons(this);"
																								onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																								responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																							<div class="questionText">
																								<xsl:value-of select="display" />
																							</div>
																						</label>
																						<xsl:choose>
																							<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																								<input type="text"
																									id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									disabled="true" />
																							</xsl:when>
																						</xsl:choose>
																					</xsl:when>
																					<xsl:otherwise>
																						<label>
																							<input type="checkbox"
																								id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																								value="{display}" onfocus="checkButtons(this);"
																								onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																								responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																							<div class="questionText">
																								<xsl:value-of select="display" />
																							</div>
																						</label>
																						<xsl:choose>
																							<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																								<input type="text"
																									id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									disabled="true" />
																							</xsl:when>
																						</xsl:choose>
																					</xsl:otherwise>
																				</xsl:choose>
																			</xsl:for-each>
																		</font>
																	</xsl:when>
																	<xsl:otherwise>
																		<xsl:for-each select="answers/answer">
																			<xsl:choose>
																				<xsl:when test="$default = display">
																					<label>
																						<input type="checkbox"
																							id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																							value="{display}" checked="true" onfocus="checkButtons(this);"
																							onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																							responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																						<div class="questionText">
																							<xsl:value-of select="display" />
																						</div>
																					</label>
																					<xsl:choose>
																						<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																							<input type="text"
																								id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								disabled="true" />
																						</xsl:when>
																					</xsl:choose>
																				</xsl:when>
																				<xsl:otherwise>
																					<label>
																						<input type="checkbox"
																							id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																							value="{display}" onfocus="checkButtons(this);"
																							onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																							responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																						<div class="questionText">
																							<xsl:value-of select="display" />
																						</div>
																					</label>
																					<xsl:choose>
																						<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																							<input type="text"
																								id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								disabled="true" />
																						</xsl:when>
																					</xsl:choose>
																				</xsl:otherwise>
																			</xsl:choose>
																		</xsl:for-each>
																	</xsl:otherwise>
																</xsl:choose>
															</xsl:when>
															<xsl:otherwise>
																<xsl:choose>
																	<xsl:when
																		test="formQuestionAttributes/htmlAttributes/fontSize != 2">
																		<font
																			size="{formQuestionAttributes/htmlAttributes/fontSize}">
																			<xsl:for-each select="answers/answer">
																				<xsl:choose>
																					<xsl:when test="$default = display">
																						<label>
																							<input type="checkbox"
																								id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																								value="{display}" checked="true" onfocus="checkButtons(this);"
																								onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																								responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																							<div class="questionText">
																								<xsl:value-of select="display" />
																							</div>
																						</label>
																						<xsl:choose>
																							<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																								<input type="text"
																									id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									disabled="true" />
																							</xsl:when>
																						</xsl:choose>
																					</xsl:when>
																					<xsl:otherwise>
																						<label>
																							<input type="checkbox"
																								id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																								value="{display}" onfocus="checkButtons(this);"
																								onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																								responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																							<div class="questionText">
																								<xsl:value-of select="display" />
																							</div>
																						</label>
																						<xsl:choose>
																							<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																								<input type="text"
																									id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																									disabled="true" />
																							</xsl:when>
																						</xsl:choose>
																					</xsl:otherwise>
																				</xsl:choose>
																			</xsl:for-each>
																		</font>
																	</xsl:when>
																	<xsl:otherwise>
																		<xsl:for-each select="answers/answer">
																			<xsl:choose>
																				<xsl:when test="$default = display">
																					<label>
																						<input type="checkbox"
																							id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																							value="{display}" checked="true" onfocus="checkButtons(this);"
																							onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																							responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																						<div class="questionText">
																							<xsl:value-of select="display" />
																						</div>
																					</label>
																					<xsl:choose>
																						<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																							<input type="text"
																								id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								disabled="true" />
																						</xsl:when>
																					</xsl:choose>
																				</xsl:when>
																				<xsl:otherwise>
																					<label>
																						<input type="checkbox"
																							id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																							value="{display}" onfocus="checkButtons(this);"
																							onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																							responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																						<div class="questionText">
																							<xsl:value-of select="display" />
																						</div>
																					</label>
																					<xsl:choose>
																						<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																							<input type="text"
																								id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																								disabled="true" />
																						</xsl:when>
																					</xsl:choose>
																				</xsl:otherwise>
																			</xsl:choose>
																		</xsl:for-each>
																	</xsl:otherwise>
																</xsl:choose>
															</xsl:otherwise>
														</xsl:choose>
													</xsl:when>
												</xsl:choose>
											</td>
										</tr>
									</table>
								</div>
								<input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_default"
									value="{$default}" />

							</td>
						</xsl:when>
						<xsl:otherwise>
							<td class="questionTextContainerTd" align="{formQuestionAttributes/htmlAttributes/align}"
								valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%"
								style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
								<img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}"
									height="1" alt="" border="0" />
								<xsl:choose>
									<xsl:when test="formQuestionAttributes/@required = 'true'">
										<span style="color:red">*</span>
									</xsl:when>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="$displayQids='true'">
										<span>
											Id: [
											<xsl:value-of select="$qid" />
											]
											<br />
											Name: [
											<xsl:value-of select="name" />
											]
											<br />
										</span>
									</xsl:when>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="@displayText='true'">
										<xsl:choose>
											<xsl:when
												test="formQuestionAttributes/htmlAttributes/fontSize != 2">
												<xsl:choose>
													<xsl:when test="@upDescription ='true'">
														<span style="font-family: arial; color: black;font-size:10pt">
															<xsl:value-of select="descriptionUp"
																disable-output-escaping="yes" />
														</span>
														<br />
														<br />
													</xsl:when>
												</xsl:choose>
												<font size="{formQuestionAttributes/htmlAttributes/fontSize}"
													class="questionTextImmediateContainer">
													<xsl:value-of select="text"
														disable-output-escaping="yes" />
												</font>
												<br />
												<br />
												<xsl:choose>
													<xsl:when test="@downDescription ='true'">
														<span style="font-family: arial; color: black;font-size:10pt">
															<xsl:value-of select="descriptionDown"
																disable-output-escaping="yes" />
														</span>
													</xsl:when>
												</xsl:choose>
											</xsl:when>
											<xsl:otherwise>
												<xsl:choose>
													<xsl:when test="@upDescription ='true'">
														<span style="font-family: arial; color: black;font-size:10pt">
															<xsl:value-of select="descriptionUp"
																disable-output-escaping="yes" />
														</span>
														<br />
														<br />
													</xsl:when>
												</xsl:choose>
												<span style="font-size:{/form/htmlAttributes/formFontSize}pt"
													class="questionTextImmediateContainer">
													<xsl:value-of select="text"
														disable-output-escaping="yes" />
												</span>
												<br />
												<br />
												<xsl:choose>
													<xsl:when test="@downDescription ='true'">
														<span style="font-family: arial; color: black;font-size:10pt">
															<xsl:value-of select="descriptionDown"
																disable-output-escaping="yes" />
														</span>
													</xsl:when>
												</xsl:choose>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:when>
								</xsl:choose>
							</td>
							<td class="questionInputTD" align="{formQuestionAttributes/htmlAttributes/align}"
								valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%"
								style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt">
								<xsl:choose>
									<xsl:when test="$horizontalDisplay = 'true'">
										<br class="horizontalDisplay" style="display:none" />
									</xsl:when>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="$horizontalDisplay != 'true'">
										<br class="verticalDisplay" style="display:none" />
									</xsl:when>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="@upDescription ='true'">
										<span style="font-size:14pt">
											<br />
										</span>
									</xsl:when>
								</xsl:choose>
								<!-- DETERMINE IF QUESTION HAS SKIP RULE -->
								<xsl:choose>
									<xsl:when test="$hasSkipRule = 'false'"> <!-- QUESTION DOES NOT HAVE A SKIP RULE -->
										<xsl:variable name="answertype" select="@answertype" />
										<xsl:choose>
											<xsl:when test="@type = 'Textbox'">
												<xsl:choose>
													<xsl:when test="@answertype = 'datetime'">
														<input type="text" id="S_{$questionSectionNode}_Q_{$qid}"
															name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}"
															class="catfood dateTimeField" size="{$textboxSize}"
															onfocus="checkButtons(this);" />
													</xsl:when>
													<xsl:otherwise>
														<xsl:choose>
															<xsl:when test="@answertype = 'date'">
																<input class="dateField catfood" type="text"
																	id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																	value="{$default}" size="{$textboxSize}" onfocus="checkButtons(this);" />
															</xsl:when>
															<xsl:otherwise>
																<input type="text" id="S_{$questionSectionNode}_Q_{$qid}"
																	name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}"
																	class="catfood" size="{$textboxSize}" onfocus="checkButtons(this);" 
																	catOid="{$catOid}"/>
															</xsl:otherwise>
														</xsl:choose>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>
											<xsl:when test="@type = 'Textarea'">
												<table>
													<tr>
														<td>
															<xsl:choose>
																<xsl:when test="@answertype = 'datetime'">
																	<textarea id="S_{$questionSectionNode}_Q_{$qid}"
																		name="S_{$questionSectionNode}_Q_{$qid}" class="dateTimeField"
																		onfocus="checkButtons(this);">
																		<xsl:value-of select="$default" />
																	</textarea>
																</xsl:when>
																<xsl:otherwise>
																	<xsl:choose>
																		<xsl:when test="@answertype = 'date'">
																			<textarea id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" class="dateField"
																				onfocus="checkButtons(this);">
																				<xsl:value-of select="$default" />
																			</textarea>
																		</xsl:when>
																		<xsl:otherwise>
																			<textarea id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" class="resizeMe"
																				onfocus="checkButtons(this);" catOid="{$catOid}">
																				<xsl:value-of select="$default" />
																			</textarea>
																		</xsl:otherwise>
																	</xsl:choose>
																</xsl:otherwise>
															</xsl:choose>
														</td>
													</tr>
												</table>
											</xsl:when>
											<xsl:when test="@type = 'Select'">
												<DIV style="padding:1px">
													<select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
														onfocus="checkButtons(this);" onchange="displayOther(this);">
														<xsl:choose>
															<xsl:when test="string($default)">
																<xsl:choose>
																	<xsl:when test="blankOption='true'">
																		<option value=""></option>
																	</xsl:when>
																</xsl:choose>
																<xsl:for-each select="answers/answer">
																	<xsl:choose>
																		<xsl:when test="$default = display">
																			<option value="{display}" selected="selected" responseID="{itemResponseOid}" responseValue="{codeValue}">
																				<xsl:value-of select="display" />
																			</option>
																		</xsl:when>
																		<xsl:otherwise>
																			<option value="{display}" responseID="{itemResponseOid}" responseValue="{codeValue}">
																				<xsl:value-of select="display" />
																			</option>
																		</xsl:otherwise>
																	</xsl:choose>
																</xsl:for-each>
															</xsl:when>
															<xsl:otherwise>
																<xsl:choose>
																	<xsl:when test="blankOption='true'">
																		<option value=""></option>
																	</xsl:when>
																</xsl:choose>
																<xsl:for-each select="answers/answer">
																	<option value="{display}" responseID="{itemResponseOid}" responseValue="{codeValue}">
																		<xsl:value-of select="display" />
																	</option>
																</xsl:for-each>
															</xsl:otherwise>
														</xsl:choose>
													</select>
													<xsl:choose>
														<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
															<input type="text"
																id="S_{$questionSectionNode}_Q_{$qid}_otherBox" style="display:none"
																name="S_{$questionSectionNode}_Q_{$qid}_otherBox" />
														</xsl:when>
													</xsl:choose>
													<input type="hidden"
														id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox"
														value="{scoreStr}" />
												</DIV>
											</xsl:when>
											<xsl:when test="@type = 'Multi-Select'">
												<select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
													multiple="true" size="5" onfocus="checkButtons(this);"
													onchange="displayOther(this);">
													<xsl:for-each select="answers/answer">
														<xsl:choose>
															<xsl:when test="$default = display">
																<option value="{display}" selected="selected" responseID="{itemResponseOid}" responseValue="{codeValue}">
																	<xsl:value-of select="display" />
																</option>
															</xsl:when>
															<xsl:otherwise>
																<option value="{display}" responseID="{itemResponseOid}" responseValue="{codeValue}">
																	<xsl:value-of select="display" />
																</option>
															</xsl:otherwise>
														</xsl:choose>
													</xsl:for-each>
												</select>
												<xsl:choose>
													<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
														<input type="text"
															id="S_{$questionSectionNode}_Q_{$qid}_otherBox" style="display:none"
															name="S_{$questionSectionNode}_Q_{$qid}_otherBox" />
													</xsl:when>
												</xsl:choose>
											</xsl:when>
											<xsl:when test="@type = 'Radio'">
												<xsl:choose>
													<xsl:when
														test="formQuestionAttributes/htmlAttributes/fontSize != 2">
														<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
															<xsl:for-each select="answers/answer">
																<xsl:choose>
																	<xsl:when test="$default = display">
																		<label>
																			<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																				checked="true" onfocus="checkButtons(this);"
																				onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" 
																				responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																			<div class="questionText">
																				<xsl:value-of select="display" />
																			</div>
																		</label>
																		<xsl:choose>
																			<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																				<input type="text"
																					id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					disabled="true" />
																			</xsl:when>
																		</xsl:choose>
																	</xsl:when>
																	<xsl:otherwise>
																		<label>
																			<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																				onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);"
																				onchange="displayRadioOther(this)" 
																				responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																			<div class="questionText">
																				<xsl:value-of select="display" />
																			</div>
																		</label>
																		<xsl:choose>
																			<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																				<input type="text"
																					id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					disabled="true" />
																			</xsl:when>
																		</xsl:choose>
																	</xsl:otherwise>
																</xsl:choose>
																<xsl:choose>
																	<xsl:when test="$horizontalDisplay!='true'">
																		<br />
																	</xsl:when>
																</xsl:choose>
															</xsl:for-each>
														</font>
													</xsl:when>
													<xsl:otherwise>
														<xsl:for-each select="answers/answer">
															<xsl:choose>
																<xsl:when test="$default = display">
																	<label>
																		<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}"
																			name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																			checked="true" onfocus="checkButtons(this);"
																			onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" 
																			responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																		<div class="questionText">
																			<xsl:value-of select="display" />
																		</div>
																	</label>
																	<xsl:choose>
																		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																			<input type="text"
																				id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				disabled="true" />
																		</xsl:when>
																	</xsl:choose>
																</xsl:when>
																<xsl:otherwise>
																	<label>
																		<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}"
																			name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																			onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);"
																			onchange="displayRadioOther(this)" 
																			responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																		<div class="questionText">
																			<xsl:value-of select="display" />
																		</div>
																	</label>
																	<xsl:choose>
																		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																			<input type="text"
																				id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				disabled="true" />
																		</xsl:when>
																	</xsl:choose>
																</xsl:otherwise>
															</xsl:choose>
															<xsl:choose>
																<xsl:when test="$horizontalDisplay!='true'">
																	<br />
																</xsl:when>
															</xsl:choose>
														</xsl:for-each>
													</xsl:otherwise>
												</xsl:choose>
												<input type="hidden"
													id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox"
													value="{scoreStr}" />
											</xsl:when>
											<xsl:when test="@type = 'Checkbox'">
												<xsl:choose>
													<xsl:when
														test="formQuestionAttributes/htmlAttributes/fontSize != 2">
														<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
															<xsl:for-each select="answers/answer">
																<xsl:choose>
																	<xsl:when test="$default = display">
																		<label>
																			<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																				checked="true" onfocus="checkButtons(this);"
																				onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																				responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																			<div class="questionText">
																				<xsl:value-of select="display" />
																			</div>
																		</label>
																		<xsl:choose>
																			<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																				<input type="text"
																					id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					disabled="true" />
																			</xsl:when>
																		</xsl:choose>
																	</xsl:when>
																	<xsl:otherwise>
																		<label>
																			<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																				onfocus="checkButtons(this);"
																				onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																				responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																			<div class="questionText">
																				<xsl:value-of select="display" />
																			</div>
																		</label>
																		<xsl:choose>
																			<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																				<input type="text"
																					id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					disabled="true" />
																			</xsl:when>
																		</xsl:choose>
																	</xsl:otherwise>
																</xsl:choose>
																<xsl:choose>
																	<xsl:when test="$horizontalDisplay!='true'">
																		<br />
																	</xsl:when>
																</xsl:choose>
															</xsl:for-each>
														</font>
													</xsl:when>
													<xsl:otherwise>
														<xsl:for-each select="answers/answer">
															<xsl:choose>
																<xsl:when test="$default = display">
																	<label>
																		<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}"
																			name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																			checked="true" onfocus="checkButtons(this);"
																			onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																			responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																		<div class="questionText">
																			<xsl:value-of select="display" />
																		</div>
																	</label>
																	<xsl:choose>
																		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																			<input type="text"
																				id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				disabled="true" />
																		</xsl:when>
																	</xsl:choose>
																</xsl:when>
																<xsl:otherwise>
																	<label>
																		<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}"
																			name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																			onfocus="checkButtons(this);"
																			onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																			responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																		<div class="questionText">
																			<xsl:value-of select="display" />
																		</div>
																	</label>
																	<xsl:choose>
																		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																			<input type="text"
																				id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				disabled="true" />
																		</xsl:when>
																	</xsl:choose>
																</xsl:otherwise>
															</xsl:choose>
															<xsl:choose>
																<xsl:when test="$horizontalDisplay!='true'">
																	<br />
																</xsl:when>
															</xsl:choose>
														</xsl:for-each>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>

											<xsl:when test="@type = 'Calculated'">
												<xsl:variable name="calc" select="@calculation" />
												<xsl:variable name="answertype" select="@answertype" />
												<xsl:variable name="decimalPrecision" select="@decimalPrecision" />
												<xsl:variable name="conditionalForCalc" select="@conditionalForCalc" />
												<input type="text" id="S_{$questionSectionNode}_Q_{$qid}"
													readonly="yes" name="S_{$questionSectionNode}_Q_{$qid}"
													size="{$textboxSize}"
													onfocus="calculate(this, '{$calc}', '{$answertype}','{$decimalPrecision}','{$conditionalForCalc}')" />

											</xsl:when>
											<xsl:otherwise>
												Unknown Question Type: System Error
											</xsl:otherwise>
										</xsl:choose>
									</xsl:when>

									<xsl:otherwise>
										<!-- QUESTION HAS A SKIP RULE -->
										<xsl:variable name="answertype" select="@answertype" />
										<xsl:choose>
											<xsl:when test="@type = 'Textbox'">
												<xsl:choose>
													<xsl:when test="@answertype = 'datetime'">
														<input class="dateTimeField " type="text"
															id="S_{$questionSectionNode}_Q_{$qid}" size="{$textboxSize}"
															name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}"
															onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')"
															onfocus="checkButtons(this);" />
													</xsl:when>
													<xsl:otherwise>
														<xsl:choose>
															<xsl:when test="@answertype = 'date'">
																<input class="dateField" type="text"
																	id="S_{$questionSectionNode}_Q_{$qid}" size="{$textboxSize}"
																	name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}"
																	onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')"
																	onfocus="checkButtons(this);" />
															</xsl:when>
															<xsl:otherwise>
																<input type="text" id="S_{$questionSectionNode}_Q_{$qid}"
																	size="{$textboxSize}" name="S_{$questionSectionNode}_Q_{$qid}"
																	value="{$default}"
																	onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')"
																	onfocus="checkButtons(this);" />
															</xsl:otherwise>
														</xsl:choose>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>

											<xsl:when test="@type = 'Textarea'">
												<xsl:choose>
													<xsl:when test="@answertype = 'datetime'">
														<input id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
															class="dateTimeField" onfocus="checkButtons(this);" />
													</xsl:when>
													<xsl:otherwise>
														<xsl:choose>
															<xsl:when test="@answertype = 'date'">
																<textarea class="dateField"
																	id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
																	onfocus="checkButtons(this);">
																	<xsl:value-of select="$default" />
																</textarea>
															</xsl:when>
															<xsl:otherwise>
																<textarea id="S_{$questionSectionNode}_Q_{$qid}"
																	name="S_{$questionSectionNode}_Q_{$qid}" class="resizeMe"
																	onfocus="checkButtons(this);">
																	<xsl:value-of select="$default" />
																</textarea>
															</xsl:otherwise>
														</xsl:choose>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>
											<xsl:when test="@type = 'Select'">
												<DIV style="padding:1px">
													<select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
														onfocus="checkButtons(this);"
														onchange="displayOther(this);applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')">
														<xsl:choose>
															<xsl:when test="string($default)">
																<xsl:choose>
																	<xsl:when test="blankOption='true'">
																		<option value=""></option>
																	</xsl:when>
																</xsl:choose>
																<xsl:for-each select="answers/answer">
																	<xsl:choose>
																		<xsl:when test="$default = display">
																			<option value="{display}" selected="selected" responseID="{itemResponseOid}" responseValue="{codeValue}">
																				<xsl:value-of select="display" />
																			</option>
																		</xsl:when>
																		<xsl:otherwise>
																			<option value="{display}" responseID="{itemResponseOid}" responseValue="{codeValue}">
																				<xsl:value-of select="display" />
																			</option>
																		</xsl:otherwise>
																	</xsl:choose>
																</xsl:for-each>
															</xsl:when>
															<xsl:otherwise>
																<xsl:choose>
																	<xsl:when test="blankOption='true'">
																		<option value=""></option>
																	</xsl:when>
																</xsl:choose>
																<xsl:for-each select="answers/answer">
																	<option value="{display}" responseID="{itemResponseOid}" responseValue="{codeValue}">
																		<xsl:value-of select="display" />
																	</option>
																</xsl:for-each>
															</xsl:otherwise>
														</xsl:choose>
													</select>
													<xsl:choose>
														<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
															<input type="text"
																id="S_{$questionSectionNode}_Q_{$qid}_otherBox" name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																style="display:none" />
														</xsl:when>
													</xsl:choose>
													<input type="hidden"
														id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox"
														value="{scoreStr}" />
												</DIV>
											</xsl:when>
											<xsl:when test="@type = 'Multi-Select'">
												<select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"
													multiple="true" size="5" onfocus="checkButtons(this);"
													onchange="displayOther(this);applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')">
													<xsl:for-each select="answers/answer">
														<xsl:choose>
															<xsl:when test="$default = display">
																<option value="{display}" selected="selected" responseID="{itemResponseOid}" responseValue="{codeValue}">
																	<xsl:value-of select="display" />
																</option>
															</xsl:when>
															<xsl:otherwise>
																<option value="{display}" responseID="{itemResponseOid}" responseValue="{codeValue}">
																	<xsl:value-of select="display" />
																</option>
															</xsl:otherwise>
														</xsl:choose>
													</xsl:for-each>
												</select>
												<xsl:choose>
													<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
														<input type="text"
															id="S_{$questionSectionNode}_Q_{$qid}_otherBox" name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
															style="display:none" />
													</xsl:when>
												</xsl:choose>
											</xsl:when>
											<xsl:when test="@type = 'Radio'">
												<xsl:choose>
													<xsl:when
														test="formQuestionAttributes/htmlAttributes/fontSize != 2">
														<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
															<xsl:for-each select="answers/answer">
																<xsl:choose>
																	<xsl:when test="$default = display">
																		<label>
																			<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																				onfocus="checkButtons(this);" checked="true"
																				ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])"
																				onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)"
																				onMouseDown="toggleRadio(this);" 
																				responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																			<div class="questionText">
																				<xsl:value-of select="display" />
																			</div>
																		</label>
																		<xsl:choose>
																			<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																				<input type="text"
																					id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					disabled="true" />
																			</xsl:when>
																		</xsl:choose>
																	</xsl:when>
																	<xsl:otherwise>
																		<label>
																			<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																				onfocus="checkButtons(this);"
																				ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])"
																				onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)"
																				onMouseDown="toggleRadio(this);" 
																				responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																			<div class="questionText">
																				<xsl:value-of select="display" />
																			</div>
																		</label>
																		<xsl:choose>
																			<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																				<input type="text"
																					id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					disabled="true" />
																			</xsl:when>
																		</xsl:choose>
																	</xsl:otherwise>
																</xsl:choose>
																<xsl:choose>
																	<xsl:when test="$horizontalDisplay!='true'">
																		<br />
																	</xsl:when>
																</xsl:choose>
															</xsl:for-each>
														</font>
													</xsl:when>
													<xsl:otherwise>
														<xsl:for-each select="answers/answer">
															<xsl:choose>
																<xsl:when test="$default = display">
																	<label>
																		<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}"
																			name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																			onfocus="checkButtons(this);" checked="true"
																			ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])"
																			onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)"
																			onMouseDown="toggleRadio(this);" 
																			responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																		<div class="questionText">
																			<xsl:value-of select="display" />
																		</div>
																	</label>
																	<xsl:choose>
																		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																			<input type="text"
																				id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				disabled="true" />
																		</xsl:when>
																	</xsl:choose>
																</xsl:when>
																<xsl:otherwise>
																	<label>
																		<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}"
																			name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																			onfocus="checkButtons(this);"
																			ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])"
																			onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)"
																			onMouseDown="toggleRadio(this);" 
																			responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																		<div class="questionText">
																			<xsl:value-of select="display" />
																		</div>
																	</label>
																	<xsl:choose>
																		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																			<input type="text"
																				id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				disabled="true" />
																		</xsl:when>
																	</xsl:choose>
																</xsl:otherwise>
															</xsl:choose>
															<xsl:choose>
																<xsl:when test="$horizontalDisplay!='true'">
																	<br />
																</xsl:when>
															</xsl:choose>
														</xsl:for-each>
													</xsl:otherwise>
												</xsl:choose>
												<input type="hidden"
													id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox"
													value="{scoreStr}" />
											</xsl:when>

											<xsl:when test="@type = 'Checkbox'">
												<xsl:choose>
													<xsl:when
														test="formQuestionAttributes/htmlAttributes/fontSize != 2">
														<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
															<xsl:for-each select="answers/answer">
																<xsl:choose>
																	<xsl:when test="$default = display">
																		<label>
																			<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																				checked="true" onfocus="checkButtons(this);"
																				onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																				responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																			<div class="questionText">
																				<xsl:value-of select="display" />
																			</div>
																		</label>
																		<xsl:choose>
																			<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																				<input type="text"
																					id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					disabled="true" />
																			</xsl:when>
																		</xsl:choose>
																	</xsl:when>
																	<xsl:otherwise>
																		<label>
																			<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}"
																				name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																				onfocus="checkButtons(this);"
																				onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')"
																				responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																			<div class="questionText">
																				<xsl:value-of select="display" />
																			</div>
																		</label>
																		<xsl:choose>
																			<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																				<input type="text"
																					id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																					disabled="true" />
																			</xsl:when>
																		</xsl:choose>
																	</xsl:otherwise>
																</xsl:choose>
																<xsl:choose>
																	<xsl:when test="$horizontalDisplay!='true'">
																		<br />
																	</xsl:when>
																</xsl:choose>
															</xsl:for-each>
														</font>
													</xsl:when>
													<xsl:otherwise>
														<xsl:for-each select="answers/answer">
															<xsl:choose>
																<xsl:when test="$default = display">
																	<label>
																		<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}"
																			name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																			checked="true" onfocus="checkButtons(this);"
																			onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																			responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																		<div class="questionText">
																			<xsl:value-of select="display" />
																		</div>
																	</label>
																	<xsl:choose>
																		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																			<input type="text"
																				id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				disabled="true" />
																		</xsl:when>
																	</xsl:choose>
																</xsl:when>
																<xsl:otherwise>
																	<label>
																		<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}"
																			name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"
																			onfocus="checkButtons(this);"
																			onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" 
																			responseID="{itemResponseOid}" responseValue="{codeValue}"/>
																		<div class="questionText">
																			<xsl:value-of select="display" />
																		</div>
																	</label>
																	<xsl:choose>
																		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
																			<input type="text"
																				id="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				name="S_{$questionSectionNode}_Q_{$qid}_otherBox"
																				disabled="true" />
																		</xsl:when>
																	</xsl:choose>
																</xsl:otherwise>
															</xsl:choose>
															<xsl:choose>
																<xsl:when test="$horizontalDisplay!='true'">
																	<br />
																</xsl:when>
															</xsl:choose>
														</xsl:for-each>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>
											<xsl:otherwise>
												Unknown Question Type: System Error
											</xsl:otherwise>
										</xsl:choose>
									</xsl:otherwise>
								</xsl:choose>
								<input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_default"
									value="{$default}" />
								<xsl:choose>
									<xsl:when test="@downDescription ='true'">
										<span style="font-size:14pt">
											<br />
										</span>
									</xsl:when>
								</xsl:choose>
							</td>
						</xsl:otherwise>
					</xsl:choose>
				</tr> <!-- show question end -->
				<xsl:choose>
					<xsl:when test="parentSectionName != 'Main' and parentSectionName != 'Form Administration'">
						<tr>
							<td COLSPAN='2'>
								<div  style="text-align:center;">
									<h3></h3><h3></h3>
									<input type="button" value="Next >>" id="S_{$questionSectionNode}_Q_{$qid}" onclick="getNext(S_{$questionSectionNode}_Q_{$qid})"/>
								</div>
							</td>
						</tr>
					</xsl:when>
				</xsl:choose>
				<tr align='left'>
					<td colspan="2" id="S_{$questionSectionNode}_Q_{$qid}_link">
						<xsl:for-each select="images/filename">
							<img class="imgThumb"
								src="{$dictionaryWsRoot}portal/ws/ddt/dictionary/eforms/question/{$qid}/document/{.}"
								alt="Question Image" border="0" style="cursor: pointer" />
							&#160;&#160;
						</xsl:for-each>
					</td>
				</tr>
			</table>
		</td>
	</xsl:template>
</xsl:stylesheet>
