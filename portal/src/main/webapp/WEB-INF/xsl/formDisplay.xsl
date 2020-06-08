<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:variable name="displayQids" select="/form/@displayQids"/>
    <xsl:variable name="displaytop" select="/form/TOC/@display"/>
    <xsl:variable name="formFontSize" select="/form/htmlAttributes/@formFontSize"/>
    <xsl:param name="webroot"/>
    <xsl:param name="imageroot"/>
    <xsl:param name="cssstylesheet"/>
    <xsl:param name="title"/>
    <xsl:output method="html" indent="yes"/>
	
    <xsl:template match="/">
    	<style type="text/css">
    		textarea, input[type="date"], input[type="datetime"], input[type="datetime-local"], input[type="email"], input[type="month"], input[type="number"], input[type="password"], input[type="search"], input[type="tel"], input[type="text"], input[type="time"], input[type="url"], input[type="week"] {
    			   width: 300px; margin-bottom: 2px;}
    		select{width: -1;    margin-bottom: 2px;}
    	</style>    
		<script type="text/javascript">
    		function loadjs(url) {
    			var fileref = document.createElement('script');
    			fileref.setAttribute('type', 'text/javascript');
    			fileref.setAttribute('src', url);
    			document.getElementsByTagName("head")[0].appendChild(fileref);
    		}
    	
    		if (typeof jQuery == "undefined") {
    			loadjs("/portal/formbuilder/js/common/jquery-1.7.2.min.js");
    			loadjs("/portal/formbuilder/js/common/jquery-ui-1.10.3.custom.min.js");
    			loadjs("/portal/formbuilder/js/common/common.js");
    			loadjs("/portal/formbuilder/js/common/formDisplay.js");
    		}
			
			// deal with very large images
			$(document).ready(function() {
   				initPage();
   			});
   			$(window).resize(function() {
   				initPage();
   			});	
   		
    		function initPage() {
    			var $thumb = $(".imgThumb");
    			$thumb.each(function() {
    			   	var $basisObj = ($(this).parents(".ctdbSectionContainer").length > 0) ? $(this).parents(".ctdbSectionContainer") : $(window);
    				$(this).width(150);
    				$(this).css("max-width", $basisObj.width() - 20 + "px");
    				$(this).css("width", "auto");
    			});
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
			
			function goImgWin(myImage,myWidth,myHeight,origLeft,origTop) {
				   myHeight += 24;
				   myWidth += 24;
				   TheImgWin = openPopup(myImage,'image','height=' +
				                                myHeight + ',width=' + myWidth +
				                                ',toolbar=no,directories=no,status=no,' +
				                                'menubar=no,scrollbars=no,resizable=yes');
				   TheImgWin.moveTo(origLeft,origTop);
				   TheImgWin.focus();
			}
    	</script>
        <xsl:apply-templates select="form"/>
    </xsl:template>

    <xsl:template match="form">
        <!-- <table width="100%" style="border: {htmlAttributes/formBorder}px ridge; font-size:{/form/htmlAttributes/formFontSize}pt;" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2"> -->
		<table width="100%" style="border: {htmlAttributes/formBorder}px ridge; font-size:{/form/htmlAttributes/formFontSize}pt;" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">
            <tr align="center"> <!-- Form header -->
                <td style="font-family:{htmlAttributes/formFont}; color: {htmlAttributes/formColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    <xsl:value-of select="formHeader" disable-output-escaping="yes"/>
                    <xsl:choose>
                      <xsl:when test="formHeader != ''">
                      	<hr color="lightgrey"/>
                      </xsl:when>
                    </xsl:choose>
                </td>
            </tr>
            <tr align="left">  <!-- Form Name -->
                <td style="font-family: {htmlAttributes/formFont}; color: {htmlAttributes/formColor}; ">
                        <h1 class="formName"><xsl:value-of select="name"/></h1>
                    <span id="000"/>
                </td>
            </tr>
            <xsl:apply-templates select="TOC"/>

          <tr align="left"> <!-- Form description -->
              <td style="font-family: {htmlAttributes/formFont}; color: {htmlAttributes/formColor};">
              	<I><xsl:value-of select="description"/></I>
              </td>
          </tr>
          <tr align="left">
             <td>
             	<!-- <img src="{/formbuilder/images/spacer.gif" width="1" height="10" alt="" border="0"/> -->
             </td>
          </tr>
           <tr> <!-- each row (contain sections) -->
                <td>
                    <xsl:apply-templates select="row"/>
                </td>
            </tr>
            <tr align="center"> <!-- Form Footer -->
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
        <table width="100%" class="rowTable">
            <tr style="font-size:{htmlAttributes/formFontSize};" class="rowTR">
                <xsl:apply-templates select="formcell"/>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="formcell">
        <xsl:variable name="theStyle" select="@theStyle"/>
        <xsl:variable name="theRowSpan" select="@theRowSpan"/>
        <xsl:variable name="theColSpan" select="@theColSpan"/>
        <xsl:variable name="visible" select="@visible"/>
        <xsl:variable name="visibleSection" select="@visibleSection"/> 
        <xsl:variable name="isRepeatable" select="@isRepeatable"/> 
         <xsl:variable name="parentValue" select="@parentValue"/>
        <xsl:variable name="formRow" select="@formRow"/>
         <xsl:variable name="secId" select="@secId"/>
        
        
        <td style="vertical-align: top;{$theStyle}" rowspan="{$theRowSpan}" colspan="{$theColSpan}">   <!-- put the section into a cell, set the td size -->
	      <xsl:choose>
           	<xsl:when test="@isRepeatable='false'">
           		<xsl:apply-templates select="section"/>
           	</xsl:when>
		    <xsl:otherwise>   
	           <xsl:choose>
	           		<xsl:when test="@visible='true'">
			           <div id="{$secId}" parent="{$parentValue}" formrow="{$formRow}">
			            <xsl:apply-templates select="section"/>          
			           </div>
			        </xsl:when>
			        <xsl:otherwise>
			             <div id="{$secId}" style='display:none' parent="{$parentValue}" formrow="{$formRow}">
			             <xsl:apply-templates select="section"/>
			            
			             </div>
			        </xsl:otherwise>
	             </xsl:choose>
	      </xsl:otherwise>
	    </xsl:choose>
        </td>
    </xsl:template>

    <xsl:template match="section">
        <xsl:variable name="collapsable" select="@isCollapsable"/>
        <xsl:variable name="gridType" select="@isGridType"/>
        <xsl:variable name="tableGroupId" select="@tableGroupId"/>
        <xsl:variable name="tableHeaderType" select="@tableHeaderType"/>
        <xsl:variable name="sectionid" select="@sectionId"/>
        <xsl:variable name="minimumValue" select="minimumValue"/>
        <xsl:variable name="maximumValue" select="maximumValue"/>
        <xsl:variable name="buttonCount" select="buttonCount"/>
        <xsl:variable name="isSecRepeatable" select="isSecRepeatable"/>
        <xsl:variable name="isCurrentLast" select="isCurrentLast"/>
        <xsl:variable name="repeatSecCount" select="repeatSecCount"/>
        <xsl:variable name="parentValue" select="parentValue"/>
        <xsl:variable name="formRow" select="formRow"/>
        <xsl:variable name="tableHeaderClassName" select="@tableHeaderClassName"/>
        <xsl:variable name="maxNoofColumnForEachRowInSection" select="@maxNoofColumnForEachRowInSection"/>
            <xsl:choose>
                <xsl:when test="@isCollapsable = 'true'">
                    <table width="100%" cellspacing="0" cellpadding="0" border="0">
                    	<tr width="100%">
                    		<td  width="100%" align="right" valign="top" >
                        		<DIV style="z-index:99;position:relative; top:17px; padding-left:10px; height:18px;" align="left"><xsl:value-of select="name"/></DIV>
                        		<img style="position:relative; z-index:0;" src="../images/sectionCollapseBkgd.gif" height="18" width="100%" BORDER="0"/>

 							</td>
 							<td valign="bottom">
 								<a href="javascript:;" style="display:block;" onclick="toggleVisibility('sectionContainer_{$sectionid}', 'sectionImg_{$sectionid}');">
 									<img src="../images/ctdbCollapse.gif" alt="expand / collapse" id="sectionImg_{$sectionid}" border="0" style="border:none;"/>
 								</a>
							     <script language="Javascript">
							         setTimeout ( "toggleVisibility('sectionContainer_<xsl:value-of select="$sectionid"/>', 'sectionImg_<xsl:value-of select="$sectionid"/>');", 900);
							     </script>
                        	</td>
                         </tr>
                    </table>
                  </xsl:when>
        	</xsl:choose>
           <div style="display:block;" id="sectionContainer_{$sectionid}" class="ctdbSectionContainer {$tableHeaderClassName}">
        	<table class="sectionContainerTable {$tableHeaderClassName}" width="100%" style="height: 100%;border:{htmlAttributes/sectionBorder}px solid;"  cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">
	            
            	<tr class="sectionNameTr" align='left'>
	                <td class="sectionNameTd" style="height: 19px; font-family: {htmlAttributes/sectionFont}; color: {htmlAttributes/sectionColor};font-size:{/form/htmlAttributes/formFontSize}pt;" colspan="{$maxNoofColumnForEachRowInSection}">
	                	<xsl:choose>
           					<xsl:when test="@isGridType != 'true'">
			                    <xsl:variable name="secid" select="sectionid"/>
			                    <span id="{$secid}"/>
			                    <xsl:choose>
			                        <xsl:when test="@textDisplayed = 'true'">
			                            <h3 class="sectionName"><xsl:value-of select="name"/></h3>   <!-- section Name -->
			                            <xsl:choose>
				                            <xsl:when test="@description != ''">
				                            	<br/>
				                            	<I><xsl:value-of select="description"/></I><!-- fix the description won't show bug -->
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
     					<xsl:for-each select="sectionRows">
     						<tr>
     							<xsl:apply-templates select="questions/question"/>
     						</tr>
     					</xsl:for-each>	
                	 </td> 
                </tr> 
        </table>
      </div>
         <xsl:choose>
              <xsl:when test="isSecRepeatable = 'true'">
              	<xsl:choose>
              		<xsl:when test="isCurrentLast = 'true'">
		        	<input type="button" class="repeatButton" value="Repeat" id="{$sectionid}_{$formRow}" style="float:right" onclick='showSection({$sectionid},{minimumValue},{$maximumValue},{$buttonCount},{$repeatSecCount},{$parentValue},{$formRow})'/>
		         </xsl:when>
		         <xsl:otherwise>
		         	<input type="button" class="repeatButton" value="Repeat" id="{$sectionid}_{$formRow}" style="float:right; display:none" onclick='showSection({$sectionid},{minimumValue},{$maximumValue},{$buttonCount},{$repeatSecCount},{$parentValue},{$formRow})'/>
		         </xsl:otherwise>
		    </xsl:choose>
       </xsl:when>
   </xsl:choose>
    </xsl:template>
    
    <xsl:template match="question">
    	<xsl:variable name="qid" select="@questionId"/>
        <xsl:variable name="skipOperator" select="formQuestionAttributes/@skipOperator"/>
        <xsl:variable name="hasSkipRule" select="formQuestionAttributes/@hasSkipRule"/>
        <xsl:variable name="skipRule" select="formQuestionAttributes/@skipRule"/>
        <xsl:variable name="skipRuleEquals" select="formQuestionAttributes/@skipEquals"/>
        <xsl:variable name="default" select="defaultValue"/>
        <xsl:variable name="questionsToSkip" select="formQuestionAttributes/questionsToSkip"/>
        <xsl:variable name="horizontalDisplay" select="formQuestionAttributes/horizontalDisplay"/>
        <xsl:variable name="horizDisplayBreak" select="formQuestionAttributes/horizontalDisplay/@horizDisplayBreak"/>
        <xsl:variable name="textboxSize" select="formQuestionAttributes/@textboxSize"/>
        <xsl:variable name="questionSectionNode" select="questionSectionNode"/>
		<xsl:variable name="bgColor" select="bgColor"/>
		<xsl:variable name="scoreStr" select="scoreStr"/>
		<xsl:variable name="blankOption" select="blankOption"/>	
		<xsl:variable name="floorColSpanTD" select="@floorColSpanTD"/>
		<xsl:variable name="showTextClassName" select="formQuestionAttributes/@showTextClassName"/>
		<xsl:variable name="tableHeaderType" select="formQuestionAttributes/@tableHeaderType"/>
		<xsl:variable name="widthTD" select="@widthTD"/>	
			
    	
    	<td  colspan="{$floorColSpanTD}" style="width:{$widthTD}%;" class="questionContainerTD {$showTextClassName}">
	    	<table class="sectionquestionscellQuestionContainerTable"  >
	        <tr  class="questionTR" >
	            <xsl:choose>
	                <xsl:when test="@type= 'Visual Scale'">    <!-- THIS IS THE VISUAL SCALE PORTION -->
	                     <xsl:variable name="rightText" select="@rightText"/>
	                     <xsl:variable name="leftText" select="@leftText"/>
	                     <xsl:variable name="width" select="@width"/>
	                     <xsl:variable name="scaleMin" select="@scaleMin"/>
	                     <xsl:variable name="scaleMax" select="@scaleMax"/>
	                    <xsl:variable name="centerText" select="@centerText"/>
	                    <xsl:variable name="showHandle" select="@showHandle"/>
	
	                    <td colspan="2" class="questionInputTD" align="left" >
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
	                        <div class="quetionContain">
		                        <table align="left" cellspacing="10">
		                            <tr align="center">   <!-- THIS ROW IS FOR THE QUESTION TEXT -->
		                                <td class="questionTextContainerTd" colspan='3' align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
		                                      <xsl:choose>
		                                        <xsl:when test="@displayText='true'">
		                                            <xsl:choose>
		                                                <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
		                                                	<xsl:choose>
		                                                		<xsl:when test="@upDescription ='true'">
						                 	                        <span style="font-family: arial; color: black;font-size:10pt">
							                                        	<xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
							                                        </span><br/><br/>
							                                     </xsl:when>
					                                        </xsl:choose>
		                                                    <font size="{formQuestionAttributes/htmlAttributes/fontSize}" class="questionTextImmediateContainer">
		                                                        <xsl:value-of select="text" disable-output-escaping="yes"/>
		                                                    </font><br/><br/>
		                                                    <xsl:choose>
		                                                    	<xsl:when test="@downDescription ='true'">
				 			                                        <span style="font-family: arial; color: black;font-size:10pt">
							                                        	<xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
							                                        </span>
							                                     </xsl:when> 
					                                        </xsl:choose>
		                                                </xsl:when>
		                                                <xsl:otherwise>
		                                                	<xsl:choose>
		                                                		<xsl:when test="@upDescription ='true'">
																	<span style="font-family: arial; color: black;font-size:10pt">
							                                        	<xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
							                                        </span><br/><br/>
							                                     </xsl:when>
					                                        </xsl:choose>
		                                                    <span style="font-size:{/form/htmlAttributes/formFontSize}pt" class="questionTextImmediateContainer">
			                                               		 <xsl:value-of select="text" disable-output-escaping="yes"/><br/><br/>
			                                                </span>
			                                                <xsl:choose>
			                                                	<xsl:when test="@downDescription ='true'">
				 			                                        <span style="font-family: arial; color: black;font-size:10pt">
							                                        	<xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
							                                        </span>
						                                        </xsl:when>
					                                        </xsl:choose>
		                                                </xsl:otherwise>
		                                            </xsl:choose>
		                                        </xsl:when>
		                                    </xsl:choose>
		                                    <div align="center">
		                                    	{<span id="S_{$questionSectionNode}_Q_{$qid}_amount" style="border:0; color:#f6931f; font-weight:bold;"><xsl:value-of select="$scaleMin"/>/<xsl:value-of select="$scaleMax"/></span>}<!-- show the index number -->
		                                	</div>
		                                </td>
		                                <td width="1%">
		                                    <span id="slider_S_{$questionSectionNode}_Q_{$qid}" class="_sliderControl" style="display:none;">
		                                        <input type="text" size="3"  id="slider_S_{$questionSectionNode}_Q_{$qid}_valueSet" onkeyup="_sl{$qid}.setValue(parseInt(this.value))"/>
		                                    </span>
		                                </td>
		                                <td></td>
		                            </tr>
		                            <tr align="center"> <!-- THIS ROW IS THE SCALE AND RIGHT AND LEFT TEXT -->
		                                <td align="right" valign="{formQuestionAttributes/htmlAttributes/valign}" width="10%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt;">
		                                     <xsl:value-of select="$leftText" disable-output-escaping="yes"/>
		                                </td>
		                                <td align="center" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt ">
		
		                                    <div class="slider" id="S_{$questionSectionNode}_Q_{$qid}_slider" style="width:{$width}mm;">
		                                        <input class="slider-input"  id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}" style="display: none" />
		                                    </div>
		                                    <script language="javascript">
												$("#S_<xsl:value-of select="$questionSectionNode"/>_Q_<xsl:value-of select="$qid"/>").css("display", "none");
												$("#S_<xsl:value-of select="$questionSectionNode"/>_Q_<xsl:value-of select="$qid"/>_slider").slider({
													min: <xsl:value-of select="$scaleMin"/>,
													max: <xsl:value-of select="$scaleMax"/>,
													change : function(event, ui) {
														$("#S_<xsl:value-of select="$questionSectionNode"/>_Q_<xsl:value-of select="$qid"/>").attr("value", ui.value);
													},
													value : "<xsl:value-of select="$default"/>",
													slide: function( event, ui ) { 
														$( "#S_<xsl:value-of select="$questionSectionNode"/>_Q_<xsl:value-of select="$qid"/>_amount" ).html(ui.value+"/"+ <xsl:value-of select="$scaleMax"/>);
													}
												});
		                                     try {
		                                        <xsl:choose>
		                                            <xsl:when test="$default!=''">
		                                                    _slS_<xsl:value-of select="$questionSectionNode"/> _Q_<xsl:value-of select="$qid"/>.setValue(<xsl:value-of select="$default"/>);
		                                            </xsl:when>
		                                        </xsl:choose>
		                                        } catch (err) {}
		                                        
		                                       	function resetSlider(){
		                                        	$("#S_<xsl:value-of select="$questionSectionNode"/>_Q_<xsl:value-of select="$qid"/>_amount" ).html(<xsl:value-of select="$scaleMin"/>+"/"+ <xsl:value-of select="$scaleMax"/>);
		                                        	$("#S_<xsl:value-of select="$questionSectionNode"/>_Q_<xsl:value-of select="$qid"/>").attr("value","");
		                                        	$("#S_<xsl:value-of select="$questionSectionNode"/>_Q_<xsl:value-of select="$qid"/>_slider").slider({
		                                        		value : "<xsl:value-of select="$default"/>",
		                                        	});
		                                        }
		                                    </script>
		                                    <br/><xsl:value-of select="$centerText" disable-output-escaping="yes"/>
		
		                                </td>
		                                <td align="left" valign="{formQuestionAttributes/htmlAttributes/valign}" width="10%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt">
		                                     <xsl:value-of select="$rightText" disable-output-escaping="yes"/>
		                                </td>
		                            </tr>
		                        </table>
	                        </div>
	                    </td>
	                </xsl:when>
	                <!-- Check to see if it is a patient calendar -->
	                <xsl:when test="@type = 'Image Map'">
	                    <xsl:variable name="imageHeight" select="@imageHeight"/>
	                    <xsl:variable name="imageWidth" select="@imageWidth"/>
	                    <xsl:variable name="imageMapFileName" select="@imageMapFileName"/>
	                    <xsl:variable name="gridFileName" select="@gridFileName"/>
	                    <td colspan="2" class="questionInputTD">
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
	                    <div class="quetionContain">
	                        <table width="100%">
	                            <tr>
	                                <td class="questionTextContainerTd" colspan="1" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
	
	                                    <img src="/portal/formbuilder/images/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
	                                    <xsl:choose>
	                                        <xsl:when test="formQuestionAttributes/@required = 'true'">
	                                            <span style="color:red">*</span>
	                                        </xsl:when>
	                                    </xsl:choose>
	                                    <xsl:choose>
	                                        <xsl:when test="$displayQids='true'">
	                                             <span>Id: [
	                                                <xsl:value-of select="$qid"/>]<br/>
	                                                Name: [
	                                                    <xsl:value-of select="name"/>]<br/>
	                                            </span>
	                                        </xsl:when>
	                                    </xsl:choose>
	                                    <xsl:choose>
	                                        <xsl:when test="@displayText='true'">
	                                            <xsl:choose>
	                                                <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
	                                                	<xsl:choose>
	                                                		<xsl:when test="@upDescription ='true'">
					                 	                        <span style="font-family: arial; color: black;font-size:10pt">
						                                        	<xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
						                                        </span><br/><br/>
					                                        </xsl:when>
					                                    </xsl:choose>
		                                                <font size="{formQuestionAttributes/htmlAttributes/fontSize}" class="questionTextImmediateContainer">
		                                                    <xsl:value-of select="text" disable-output-escaping="yes"/>
		                                                </font><br/><br/>
		                                                <xsl:choose>
		                                                	<xsl:when test="@downDescription ='true'">
			 			                                        <span style="font-family: arial; color: black;font-size:10pt">
						                                        <xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
						                                        </span>
					                                        </xsl:when> 
					                                    </xsl:choose>
	                                                </xsl:when>
	                                                <xsl:otherwise>
	                                                	<xsl:choose>
	                                                		<xsl:when test="@upDescription ='true'">
																<span style="font-family: arial; color: black;font-size:10pt">
						                                        <xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
						                                        </span><br/><br/>
					                                        </xsl:when>
					                                    </xsl:choose>
		                                                <span style="font-size:{/form/htmlAttributes/formFontSize}pt" class="questionTextImmediateContainer">
			                                            	 <xsl:value-of select="text" disable-output-escaping="yes"/><br/><br/>
			                                            </span>
			                                            <xsl:choose>
			                                            	<xsl:when test="@downDescription ='true'">
			 			                                        <span style="font-family: arial; color: black;font-size:10pt">
						                                        	<xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
						                                        </span>
					                                        </xsl:when>
					                                    </xsl:choose>
	                                                </xsl:otherwise>
	                                            </xsl:choose>
	                                        </xsl:when>
	                                    </xsl:choose>
	                                    <br/>
		                                <!-- Image map options -->
		                                <select id="imageMap_S_{$questionSectionNode}_Q_{$qid}" size="5" name="S_{$questionSectionNode}_Q_{$qid}" class="ctdbImageMapAnswers" multiple="true" style="width:170px">
		                                    <!-- <option value="{$default}" selected="selected"> -->
		                                    <option value="{$default}">
		                                	    <xsl:value-of select="$default"/>
		                                 	</option>
		                                </select>
		                                <!-- Image map remove option button -->
		                                <img src="{$imageroot}/buttonRemove.png" alt="Delete selected option" title="Delete" border="0" width="23" height="23" tabindex="0" 
		                                     onclick="removeItemMacOpt (document.getElementById('imageMap_S_{$questionSectionNode}_Q_{$qid}'), 'NO');"
		                                     onkeypress="removeItemMacOpt (document.getElementById('imageMap_S_{$questionSectionNode}_Q_{$qid}'), 'NO');"
		                                     />
	                                </td>
	                            </tr>
	                            <tr>
	                                <td colspan="3" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" 
	                                    width="10%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
	                                    <!-- one line space -->
	                                    <img src="/portal/formbuilder/images/spacer.gif" border="0" height="1" width="1" id="imageMapRef_S_{$questionSectionNode}_Q_{$qid}" style="z-index:-100; position:absolute"/>
	                                    <xsl:choose>
	                                        <xsl:when test="@displayGrid = 'true'">
	                                            <!-- hidded by Ching heng  -->
	                                            <div style="float:left">
	                                             	<!-- make image at right position -->
		                                            <img src="/portal/formbuilder/images/spacer.gif" border="0" height="{$imageHeight}" width="1"
		                                                id="imageMapSpacer_S_{$questionSectionNode}_Q_{$qid}" style="z-index:-100"/>
		                                            <!-- image map -->                         
		                                            <img src="{$imageroot}/questionimages/{$imageMapFileName}" border="0"
		                                                height="{$imageHeight}" width="{$imageWidth}"
		                                                style="z-index:100; position:absolute" id="imageMapImage_S_{$questionSectionNode}_Q_{$qid}"/> 
		                                            <!-- image map grid -->
		                                            <img src="{$imageroot}/{$gridFileName}" border="0"
		                                                height="{$imageHeight}" width="{$imageWidth}"
		                                                style="z-index:500; position:absolute" id="imageMapGrid_S_{$questionSectionNode}_Q_{$qid}"
		                                                useMap="#theMap_S_{$questionSectionNode}_Q_{$qid}"/>                                           
		                                            <div style="display:none"><xsl:value-of select="mapHtml" disable-output-escaping="yes"/></div>
		                                        </div>
	                                            <script language="javascript">
	                                                setTimeout ('updateGridLocation (<xsl:value-of select="$questionSectionNode"/>,<xsl:value-of select="$qid"/>)', 800);
	                                            </script>
	                                        </xsl:when>
	                                        <xsl:otherwise>
	                                        	<div style="float:left">
	                                        		<!-- make image at right position -->
		                                            <img src="/portal/formbuilder/images/spacer.gif" border="0" height="{$imageHeight}" width="1"
		                                                id="imageMapSpacer_S_{$questionSectionNode}_Q_{$qid}" style="z-index:-100"/>
		                                            <!-- image map -->
		                                            <img src="{$imageroot}/questionimages/{$imageMapFileName}" border="0" 
		                                                style="z-index:100; position:absolute" height="{$imageHeight}" width="{$imageWidth}"
		                                                useMap="#theMap_S_{$questionSectionNode}_Q_{$qid}"/>                                             
		                                            <div style="display:none"><xsl:value-of select="mapHtml" disable-output-escaping="yes"/></div>
		                                         </div>
	                                        </xsl:otherwise>
	                                    </xsl:choose>
	                                </td>                              
	                            </tr>
	                        </table>
	                        </div>
	                        <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_default" value="{$default}"/>
	                    </td>
	                </xsl:when>
	                <!-- added by Ching Heng for File question Type -->
	                <xsl:when test="@type = 'File'">
	                	<td class="questionTextContainerTd" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
	                        <img src="/portal/formbuilder/images/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
	                        <xsl:choose>
	                            <xsl:when test="formQuestionAttributes/@required = 'true'">
	                                <span style="color:red">*</span>
	                            </xsl:when>
	                        </xsl:choose>
	                        <xsl:choose>
	                            <xsl:when test="$displayQids='true'">
	                                 <span>Id: [
	                                    <xsl:value-of select="$qid"/>]<br/>
	                                    Name: [
	                                        <xsl:value-of select="name"/>]<br/>
	                                </span>
	                            </xsl:when>
	                        </xsl:choose>
	                        <xsl:choose>
	                            <xsl:when test="@displayText='true'">
	                                <xsl:choose>
	                                    <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
		                                     <xsl:choose>	
		                                    	<xsl:when test="@upDescription ='true'">
						                 	        <span style="font-family: arial; color: black;font-size:10pt">
							                            <xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
							                        </span><br/><br/>
						                        </xsl:when>
						                      </xsl:choose>
		                                    <font size="{formQuestionAttributes/htmlAttributes/fontSize}" class="questionTextImmediateContainer">
		                                        <xsl:value-of select="text" disable-output-escaping="yes"/>
		                                    </font><br/><br/>
		                                    <xsl:choose>
		                                    	<xsl:when test="@downDescription ='true'">
				 			                        <span style="font-family: arial; color: black;font-size:10pt">
							                            <xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
							                        </span>
						                        </xsl:when>
					                        </xsl:choose> 
	                                    </xsl:when>
	                                    <xsl:otherwise>
	    									<xsl:choose>	
		                                    	<xsl:when test="@upDescription ='true'">
						                 	        <span style="font-family: arial; color: black;font-size:10pt">
							                            <xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
							                        </span><br/><br/>
						                        </xsl:when>
						                     </xsl:choose>
		                                    <span style="font-size:{/form/htmlAttributes/formFontSize}pt" class="questionTextImmediateContainer">
			                                    <xsl:value-of select="text" disable-output-escaping="yes"/><br/><br/>
			                                </span>
		 			                        <xsl:choose>
		                                    	<xsl:when test="@downDescription ='true'">
				 			                        <span style="font-family: arial; color: black;font-size:10pt">
							                            <xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
							                        </span>
						                        </xsl:when>
					                        </xsl:choose> 
	                                    </xsl:otherwise>
	                                </xsl:choose>
	                            </xsl:when>
	                        </xsl:choose>
	                    </td>
	                    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt">
		                	<input type="file" id="S_{$questionSectionNode}_Q_{$qid}" key="S_{$questionSectionNode}_Q_{$qid}" name="fileUpload" class="fileInput"/>
		                	<input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_fileKey" name="fileUploadKey" value=""/>
		                </td>
	                </xsl:when>
	                
	                <xsl:when test="@type = 'Textblock'">
	               		   <xsl:variable name="htmltext" select="@htmltext"/>
	               		  <td colspan="2" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}">
	               		  	<span class="textblock"><xsl:value-of select="htmltext" disable-output-escaping="yes"/></span>
	               		  </td>               
	                </xsl:when>
					
	                <xsl:when test="$horizDisplayBreak='true'">
	                <!-- if horizontal display is true it must be a radio or checkbox -->
	
	                <td colspan="2">
	                <div class="quetionContain">
	                <table>
	                    <tr>
	                        <td class="questionTextContainerTd" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
	                                <img src="/portal/formbuilder/images/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
	                                <xsl:choose>
	                                    <xsl:when test="formQuestionAttributes/@required = 'true'">
	                                        <span style="color:red">*</span>
	                                    </xsl:when>
	                                </xsl:choose>
	                                <xsl:choose>
	                                    <xsl:when test="$displayQids='true'">
	                                         <span >Id: [
	                                            <xsl:value-of select="$qid"/>]<br/>
	                                            Name: [
	                                                <xsl:value-of select="name"/>]<br/>
	                                        </span>
	                                    </xsl:when>
	                                </xsl:choose>
	                                <xsl:choose>
	                                    <xsl:when test="@displayText='true'">
	                                        <xsl:choose>
	                                            <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
	                                            	<xsl:choose>
	                                            		<xsl:when test="@upDescription ='true'">
				                 	                        <span style="font-family: arial; color: black;font-size:10pt">
					                                     	   <xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
					                                        </span><br/><br/>
					                                     </xsl:when>
					                                 </xsl:choose>
		                                             <font size="{formQuestionAttributes/htmlAttributes/fontSize}" class="questionTextImmediateContainer">
		                                                 <xsl:value-of select="text" disable-output-escaping="yes"/>
		                                             </font><br/><br/>
		                                             <xsl:choose>
		                                             	<xsl:when test="@downDescription ='true'">
		 			                                        <span style="font-family: arial; color: black;font-size:10pt">
					                                        	<xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
					                                        </span>
					                                     </xsl:when>
					                                 </xsl:choose> 
	                                            </xsl:when>
	                                            <xsl:otherwise>
	                                            	<xsl:choose>
	                                            		<xsl:when test="@upDescription ='true'">
															<span style="font-family: arial; color: black;font-size:10pt">
					                                        <xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
					                                        </span><br/><br/>
					                                     </xsl:when>
					                                 </xsl:choose>
		                                             <span style="font-size:{/form/htmlAttributes/formFontSize}pt" class="questionTextImmediateContainer">
		                                                 <xsl:value-of select="text" disable-output-escaping="yes"/>
			                                         </span>
			                                         <xsl:choose>
			                                         	<xsl:when test="@downDescription ='true'">
		 			                                        <span style="font-family: arial; color: black;font-size:10pt">
					                                    	    <xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
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
	                    <td class="questionInputTD" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt;">
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
						                       <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
						                       		<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
							                       		<xsl:for-each select="answers/answer">
					                                        <xsl:choose>
					                                            <xsl:when test="$default = display">
					                                           		<label>
					                                                	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" />
					                                                	<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>
					                                                </label>
					                                                <xsl:choose>
				                                                		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                			<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                                		</xsl:when>
				                                                	</xsl:choose>
					                                            </xsl:when>
					                                            <xsl:otherwise>
					                                           		<label>
					                                                	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" />
					                                                	<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>	
					                                                </label>			                                                                       
					                                                <xsl:choose>
				                                                		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                			<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox" name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true" />
				                                                		</xsl:when>
				                                                	</xsl:choose>
					                                            </xsl:otherwise>
					                                        </xsl:choose>
					                                         <xsl:choose>
					                                                 <xsl:when test="$horizontalDisplay!='true'">
					                                                     <br/>
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
				                                                	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                                	</xsl:when>
				                                                </xsl:choose>
				                                            </xsl:when>
				                                            <xsl:otherwise>
				                                        	    <label>
				                                               		<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" />
				                                               		<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                                	</xsl:when>
				                                                </xsl:choose>
				                                            </xsl:otherwise>
				                                        </xsl:choose>
				                                         <xsl:choose>
				                                                 <xsl:when test="$horizontalDisplay!='true'">
				                                                     <br/>
				                                                 </xsl:when>
				                                            </xsl:choose>
				                                    </xsl:for-each>
				                                 </xsl:otherwise>
				                            </xsl:choose>
	                                </xsl:when>
	                                <xsl:otherwise>
	                                	<xsl:choose>
						                    <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
						                    	<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
						                    		<xsl:for-each select="answers/answer">
				                                       <xsl:choose>
				                                            <xsl:when test="$default = display">
				                                                 <label>
				                                                 	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"  onfocus="checkButtons(this);" checked="true" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);" />
				                                                 	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                 </label>	                                                
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox"  disabled="true"/>
				                                                	</xsl:when>
				                                                </xsl:choose>
				                                             </xsl:when>
				                                             <xsl:otherwise>
				                                                <label>
				                                                	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox"  disabled="true"/>
				                                                	</xsl:when>
				                                                </xsl:choose>
				                                             </xsl:otherwise>
				                                         </xsl:choose>
				                                         <xsl:choose>
				                                                 <xsl:when test="$horizontalDisplay!='true'">
				                                                     <br/>
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
				                                                	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"  onfocus="checkButtons(this);" checked="true" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>     
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox"  disabled="true"/>
				                                                	</xsl:when>
				                                                </xsl:choose>
				                                             </xsl:when>
				                                             <xsl:otherwise>
				                                                <label>
				                                                	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                                	</xsl:when>
				                                                </xsl:choose>
				                                             </xsl:otherwise>
				                                         </xsl:choose>
				                                         <xsl:choose>
				                                                 <xsl:when test="$horizontalDisplay!='true'">
				                                                     <br/>
				                                                 </xsl:when>
				                                         </xsl:choose>
				                                     </xsl:for-each>
				                             </xsl:otherwise>
				                          </xsl:choose>
	                                </xsl:otherwise>
	                            </xsl:choose>
	                        	<input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>
	                        </xsl:when>
	                        <xsl:when test="@type='Checkbox'">
	                            <xsl:choose>
	                                <xsl:when test="$hasSkipRule = 'false'">
	                                	<xsl:choose>                                	
		                                	<xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
		                                		<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
				                                    <xsl:for-each select="answers/answer">
				                                        <xsl:choose>
				                                            <xsl:when test="$default = display">
				                                                <label>
				                                                	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                                	</xsl:when>
				                                               	</xsl:choose>
				                                            </xsl:when>
				                                            <xsl:otherwise>
				                                                <label>
				                                                	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                               			<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
			                                                	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
			                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
			                                                </label>
			                                                <xsl:choose>
				                                            	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                            		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                           		</xsl:when>
				                                           	</xsl:choose>
			                                            </xsl:when>
			                                            <xsl:otherwise>
			                                                <label>
			                                                	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
			                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
			                                                </label>
			                                                <xsl:choose>
				                                             	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                             		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
		                                	<xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
		                                		<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
		                                			<xsl:for-each select="answers/answer">
				                                        <xsl:choose>
				                                            <xsl:when test="$default = display">
				                                                <label>
				                                                	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                               		</xsl:when>
				                                               	</xsl:choose>
				                                            </xsl:when>
				                                            <xsl:otherwise>
				                                                <label>
				                                                	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
				                                                	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                               		</xsl:when>
				                                               	</xsl:choose>
				                                            </xsl:when>
				                                            <xsl:otherwise>
				                                                <label>
				                                                	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
				                                                </label>
				                                                <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
	                <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_default" value="{$default}"/>
	                
	                </td>
	                </xsl:when>
	
	
	                <xsl:otherwise>
	                    <td class="questionTextContainerTd" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
	                        <img src="/portal/formbuilder/images/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
	                        <xsl:choose>
	                            <xsl:when test="formQuestionAttributes/@required = 'true'">
	                                <span style="color:red">*</span>
	                            </xsl:when>
	                        </xsl:choose>
	                        <xsl:choose>
	                            <xsl:when test="$displayQids='true'">
	                                 <span>Id: [
	                                    <xsl:value-of select="$qid"/>]<br/>
	                                    Name: [
	                                        <xsl:value-of select="name"/>]<br/>
	                                </span>
	                            </xsl:when>
	                        </xsl:choose>
	                        <xsl:choose>
	                            <xsl:when test="@displayText='true'">
	                                <xsl:choose>
	                                    <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
	                                    	<xsl:choose>
	                                    		<xsl:when test="@upDescription ='true'">
						                 	        <span style="font-family: arial; color: black;font-size:10pt">
			                                        	<xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
			                                        </span><br/><br/>
		                                        </xsl:when>
	                                        </xsl:choose>
	                                        <font size="{formQuestionAttributes/htmlAttributes/fontSize}" class="questionTextImmediateContainer">
	                                           <xsl:value-of select="text" disable-output-escaping="yes"/>
	                                        </font><br/><br/>
	                                        <xsl:choose>
	                                        	<xsl:when test="@downDescription ='true'">
			                                        <span style="font-family: arial; color: black;font-size:10pt">
		                                        		<xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
		                                        	</span>
	                                        	</xsl:when> 
	                                        </xsl:choose>
	                                    </xsl:when>
	                                    <xsl:otherwise>
	                                    	<xsl:choose>
	                                    		<xsl:when test="@upDescription ='true'">
													<span style="font-family: arial; color: black;font-size:10pt">
			                                        	<xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
			                                        </span><br/><br/>
		                                        </xsl:when>
		                                    </xsl:choose>
	                                        <span style="font-size:{/form/htmlAttributes/formFontSize}pt" class="questionTextImmediateContainer">
	                                        	 <xsl:value-of select="text" disable-output-escaping="yes"/>
	                                        </span><br/><br/>
	                                        <xsl:choose>
	                                        	<xsl:when test="@downDescription ='true'">
			                                        <span style="font-family: arial; color: black;font-size:10pt">
			                                        	<xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
			                                        </span>
			                                     </xsl:when>
			                                </xsl:choose>
	                                    </xsl:otherwise>
	                                </xsl:choose>
	                            </xsl:when>
	                        </xsl:choose>
	                    </td>
	   					<td class="questionInputTD" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt">		   					<xsl:choose>
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
		                                    	<br/>
		                                    	</span>
		                                    	</xsl:when>
		                                  	  </xsl:choose>                             
	                     
	                        <!-- DETERMINE IF QUESTION HAS SKIP RULE -->              
	                        <xsl:choose>
	                            <xsl:when test="$hasSkipRule = 'false'"> <!-- QUESTION DOES NOT HAVE A SKIP RULE -->
	                            <xsl:variable name="answertype" select="@answertype"/>
	                                <xsl:choose>
	                                    <xsl:when test="@type = 'Textbox'">
	                                    	<xsl:choose>
	                                    		<xsl:when test="@answertype = 'datetime'">
	                                    			<input  type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}" class="catfood dateTimeField" size="{$textboxSize}" onfocus="checkButtons(this);"/>
	                                    		</xsl:when>
	                                    		  <xsl:otherwise>
	                                    		  	<xsl:choose>
	                                    		  		<xsl:when test="@answertype = 'date'">
	                                        				<input class="dateField catfood" type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}"  size="{$textboxSize}" onfocus="checkButtons(this);"/>
	                                        			</xsl:when>
	                                        			<xsl:otherwise>	
	                                        				<input type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}" class="catfood" size="{$textboxSize}" onfocus="checkButtons(this);"/>
	                                        			</xsl:otherwise>
	                                        		</xsl:choose>
	                                        	  </xsl:otherwise>
	                                        </xsl:choose>
	                                    </xsl:when>
	                                    <xsl:when test="@type = 'Textarea'">
	                                        <table>
	                                            <!-- <tr><td nowrap="true">
	                                                <span style="float:right" class="smallText"><a href="Javascript:doNothing();" onClick="toggleOverflow ('Q_{$qid}');">
	                                                    <img src="{$imageroot}/textareaExpand.gif" height="18" width="30" border="0"/>
	                                                </a></span>
	
	                                            </td></tr> -->
	                                            <tr><td>
	                                            <xsl:choose>
	                                            	<xsl:when test="@answertype = 'datetime'">
	                                           			  <textarea id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="dateTimeField"   onfocus="checkButtons(this);">
		                                                    <xsl:value-of select="$default"/>
		                                                </textarea>
	                                                </xsl:when>
	                                                 <xsl:otherwise>
	                                                 	<xsl:choose>
	                                                 		<xsl:when test="@answertype = 'date'">
				                                                <textarea id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="dateField"   onfocus="checkButtons(this);">
				                                                    <xsl:value-of select="$default"/>
				                                                </textarea>
		                                                	</xsl:when>
		                                                	<xsl:otherwise>
				                                                <textarea id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="resizeMe"  onfocus="checkButtons(this);">
				                                                    <xsl:value-of select="$default"/>
				                                                </textarea>
				                                            </xsl:otherwise>
		                                                </xsl:choose>
	                                                </xsl:otherwise>
	                                             </xsl:choose>
	                                            </td></tr>
	                                           <!--  <tr><td>
	                                                <span style="float:right" class="smallText">
	                                                    <a href="Javascript:doNothing();" onClick="growTextarea('S_{$questionSectionNode}_Q_{$qid}');"><img src="{$imageroot}/textareaGrow.gif" height="16" width="16" border="0"/></a>
	                                                    <img src="{/formbuilder/images/spacer.gif" height="18" width="3" border="0"/>
	                                                    <a href="Javascript:doNothing();" onClick="shrinkTextarea('S_{$questionSectionNode}_Q_{$qid}');"><img src="{$imageroot}/textareaShrink.gif" height="16" width="16" border="0"/></a>
	                                                </span>
	                                            </td></tr> -->
	                                        </table>
	
	                                    </xsl:when>
	                                <xsl:when test="@type = 'Select'">
	                                    <DIV style="padding:1px">
	                                        <select class="ySelect" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"  onfocus="checkButtons(this);" onchange="displayOther(this);">
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
	                                                                <option value="{display}" selected="selected" title="{display/@displayToolTip}">
	                                                                    <xsl:value-of select="display"/>
	                                                                </option>
	                                                            </xsl:when>
	                                                            <xsl:otherwise>
	                                                                <option value="{display}" title="{display/@displayToolTip}">
	                                                                    <xsl:value-of select="display"/>
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
	                                                        <option value="{display}" title="{display/@displayToolTip}">
	                                                            <xsl:value-of select="display"/>
	                                                        </option>
	                                                    </xsl:for-each>
	                                                </xsl:otherwise>
	                                            </xsl:choose>
	                                        </select>
	                                        <xsl:choose>
				                                <xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                 	<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox" style="display:none"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" />
				                                </xsl:when>
				                            </xsl:choose>
				                            <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>
	                                    </DIV>
	                                    </xsl:when>
	                                    <xsl:when test="@type = 'Multi-Select'">
	                                        <select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" multiple="true" size="5" onfocus="checkButtons(this);" onchange="displayOther(this);">
	                                            <xsl:for-each select="answers/answer">
	                                                <xsl:choose>
	                                                    <xsl:when test="$default = display">
	                                                        <option value="{display}" selected="selected" title="{display/@displayToolTip}">
	                                                            <xsl:value-of select="display"/>
	                                                        </option>
	                                                    </xsl:when>
	                                                    <xsl:otherwise>
	                                                        <option value="{display}" title="{display/@displayToolTip}">
	                                                            <xsl:value-of select="display"/>
	                                                        </option>
	                                                    </xsl:otherwise>
	                                                </xsl:choose>
	                                            </xsl:for-each>
	                                        </select>
	                                        <xsl:choose>
				                                <xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                 	<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox" style="display:none"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" />
				                                </xsl:when>
				                            </xsl:choose>
	                                    </xsl:when>
	                                    <xsl:when test="@type = 'Radio'">
	                                    	<xsl:choose>
						                       <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
						                       			<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
						                                 <xsl:for-each select="answers/answer">
				                                            <xsl:choose>
				                                                <xsl:when test="$default = display">
				                                                    <label>
				                                                    	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" />
				                                                    	<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>
				                                                    </label>
				                                                    <xsl:choose>
					                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
					                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
					                                               		</xsl:when>
					                                               	</xsl:choose>
				                                                </xsl:when>
				                                                <xsl:otherwise>
				                                                    <label>
				                                                    	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" />
				                                                    	<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>
				                                                    </label>
				                                                    <xsl:choose>
					                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
					                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
					                                               		</xsl:when>
					                                               	</xsl:choose>
				                                                </xsl:otherwise>
				                                            </xsl:choose>
				                                             <xsl:choose>
				                                                <xsl:when test="$horizontalDisplay!='true'">
				                                                     <br/>
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
				                                                   		<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" />
				                                                   		<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>
				                                                    </label>
				                                                    <xsl:choose>
					                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
					                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
					                                               		</xsl:when>
					                                               	</xsl:choose>
				                                                </xsl:when>
				                                                <xsl:otherwise>
				                                                	<label>
				                                                   		<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)" />
				                                                   		<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>
				                                                    </label>
				                                                    <xsl:choose>
				                                                		<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                			<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                                		</xsl:when>
				                                                	</xsl:choose>
				                                                </xsl:otherwise>
				                                            </xsl:choose>
				                                             <xsl:choose>
				                                                <xsl:when test="$horizontalDisplay!='true'">
				                                                     <br/>
				                                                </xsl:when>
				                                            </xsl:choose>
				                                        </xsl:for-each>
						                        </xsl:otherwise>
						                     </xsl:choose>
						                     <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>                                        
	                                    </xsl:when>
	                                    <xsl:when test="@type = 'Checkbox'">
	                                    	<xsl:choose>
						                       <xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
						                       		<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
				                                        <xsl:for-each select="answers/answer">
				                                                    <xsl:choose>
				                                                        <xsl:when test="$default = display">
				                                                            <label>
				                                                            	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                            	<div class="questionText" title="{display/@displayToolTip}">
							                                                		<xsl:value-of select="display"/>
							                                                	</div>
				                                                            </label>
				                                                            <xsl:choose>
							                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
							                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
							                                               		</xsl:when>
							                                               	</xsl:choose>
				                                                        </xsl:when>
				                                                        <xsl:otherwise>
				                                                            <label>
				                                                            	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                            	<div class="questionText" title="{display/@displayToolTip}">
							                                                		<xsl:value-of select="display"/>
							                                                	</div>
				                                                            </label>
				                                                            <xsl:choose>
							                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
							                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
							                                               		</xsl:when>
							                                               	</xsl:choose>
				                                                        </xsl:otherwise>
				                                                    </xsl:choose>
				                                              <xsl:choose>
				                                                 <xsl:when test="$horizontalDisplay!='true'">
				                                                     <br/>
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
				                                                            	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                            	<div class="questionText" title="{display/@displayToolTip}">
							                                                		<xsl:value-of select="display"/>
							                                                	</div>
				                                                            </label>
				                                                            <xsl:choose>
							                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
							                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
							                                               		</xsl:when>
							                                               	</xsl:choose>
				                                                        </xsl:when>
				                                                        <xsl:otherwise>
				                                                            <label>
				                                                            	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                            	<div class="questionText" title="{display/@displayToolTip}">
							                                                		<xsl:value-of select="display"/>
							                                                	</div>
				                                                            </label>
				                                                            <xsl:choose>
							                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
							                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
							                                               		</xsl:when>
							                                               	</xsl:choose>
				                                                        </xsl:otherwise>
				                                                    </xsl:choose>
				                                              <xsl:choose>
				                                                 <xsl:when test="$horizontalDisplay!='true'">
				                                                     <br/>
				                                                 </xsl:when>
				                                            </xsl:choose>
														</xsl:for-each>
												</xsl:otherwise>
											</xsl:choose>
	                                    </xsl:when>
	
	                                    <xsl:when test="@type = 'Calculated'">
	                                        <xsl:variable name="calc" select="@calculation"/>
	                                        <xsl:variable name="answertype" select="@answertype"/>
	                                        <xsl:variable name="decimalPrecision" select="@decimalPrecision"/>
	                                        <input type="text" id="S_{$questionSectionNode}_Q_{$qid}" readonly="yes" name="S_{$questionSectionNode}_Q_{$qid}" size="{$textboxSize}"  onfocus="calculate(this, '{$calc}', '{$answertype}','{$decimalPrecision}')"/>
	                                    	
	                                    </xsl:when>
	                                    <xsl:otherwise>
	                                        Unknown Question Type: System Error
	                                    </xsl:otherwise>
	                                </xsl:choose>
	                            </xsl:when>
	
	                            <xsl:otherwise>
	                                <!-- QUESTION HAS A SKIP RULE -->
	                                <xsl:variable name="answertype" select="@answertype"/>
	                                <xsl:choose>	
	                                    <xsl:when test="@type = 'Textbox'">
	                                      <xsl:choose>
	                                      	<xsl:when test="@answertype = 'datetime'">
	                                      		  <input class="dateTimeField " type="text" id="S_{$questionSectionNode}_Q_{$qid}" size="{$textboxSize}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')" onfocus="checkButtons(this);"/>
	                                    	</xsl:when>
	                                        <xsl:otherwise>
	                                   			<xsl:choose>
	                                      			<xsl:when test="@answertype = 'date'">
	                                      		  		<input class="dateField" type="text" id="S_{$questionSectionNode}_Q_{$qid}" size="{$textboxSize}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')" onfocus="checkButtons(this);"/>
	                                    			</xsl:when>
	                                    			<xsl:otherwise>
	                                    				<input type="text" id="S_{$questionSectionNode}_Q_{$qid}" size="{$textboxSize}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')" onfocus="checkButtons(this);"/>
	                                    			</xsl:otherwise>
		   									 	</xsl:choose>
	                                   		</xsl:otherwise>
	                                   	</xsl:choose>
	                                    </xsl:when>
	                                    
	                                    <xsl:when test="@type = 'Textarea'">
	                                       <!--  <table style="border-spacing:2px 0px"> -->
	                                           <!--  <tr><td align="left">
	                                                <span class="smallText"><a href="Javascript:doNothing();" onClick="toggleOverflow ('Q_{$qid}');">expand/collapse</a></span>
	                                            </td></tr> -->
	                                           <!--  <tr><td> -->
	                                            <xsl:choose>
	                                            	<xsl:when test="@answertype = 'datetime'">
	                                            	<input id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="dateTimeField"   onfocus="checkButtons(this);"/>
	                                            	</xsl:when>
	                                            	 <xsl:otherwise>
	                                            	 	<xsl:choose>
	                                            	 		<xsl:when test="@answertype = 'date'">
				                                                <textarea class="dateField" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"    onfocus="checkButtons(this);">
				                                                    <xsl:value-of select="$default"/>
				                                                </textarea>
				                                              </xsl:when>
				                                              <xsl:otherwise>
				                                                <textarea id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="resizeMe"  onfocus="checkButtons(this);">
				                                                    <xsl:value-of select="$default"/>
				                                                </textarea>
			                                                </xsl:otherwise>
	                                               		 </xsl:choose>
	                                                </xsl:otherwise>
	                                                </xsl:choose>
	                                          <!--   </td></tr> -->
	                                           <!--  <tr><td>
	                                                <span style="float:right" class="smallText">
	                                                    <a href="Javascript:doNothing();" onClick="growTextarea('S_{$questionSectionNode}_Q_{$qid}');"><img src="{$imageroot}/textareaGrow.gif" height="16" width="16" border="0"/></a>
	                                                    <img src="{/formbuilder/images/spacer.gif" height="18" width="3" border="0"/>
	                                                    <a href="Javascript:doNothing();" onClick="shrinkTextarea('S_{$questionSectionNode}_Q_{$qid}');"><img src="{$imageroot}/textareaShrink.gif" height="16" width="16" border="0"/></a>
	                                                </span>
	                                            </td></tr> -->
	                                      <!--   </table> -->
	                                    </xsl:when>
	                                    <xsl:when test="@type = 'Select'">
	                                        <DIV style="padding:1px">
	                                        <select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"  onfocus="checkButtons(this);" onchange="displayOther(this);applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')">
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
	                                                                <option value="{display}" selected="selected" title="{display/@displayToolTip}">
	                                                                    <xsl:value-of select="display"/>
	                                                                </option>
	                                                            </xsl:when>
	                                                            <xsl:otherwise>
	                                                                <option value="{display}" title="{display/@displayToolTip}">
	                                                                    <xsl:value-of select="display"/>
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
	                                                        <option value="{display}" title="{display/@displayToolTip}">
	                                                            <xsl:value-of select="display"/>
	                                                        </option>
	                                                    </xsl:for-each>
	                                                </xsl:otherwise>
	                                            </xsl:choose>
	                                        </select>
	                                        <xsl:choose>
				                                <xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                 	<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" style="display:none"/>
				                                </xsl:when>
				                            </xsl:choose>
				                            <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>
	                                        </DIV>
	                                    </xsl:when>
	                                    <xsl:when test="@type = 'Multi-Select'">
	                                        <select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" multiple="true" size="5"  onfocus="checkButtons(this);" onchange="displayOther(this);applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')">
	                                            <xsl:for-each select="answers/answer">
	                                                <xsl:choose>
	                                                    <xsl:when test="$default = display">
	                                                        <option value="{display}" selected="selected" title="{display/@displayToolTip}">
	                                                            <xsl:value-of select="display"/>
	                                                        </option>
	                                                    </xsl:when>
	                                                    <xsl:otherwise>
	                                                        <option value="{display}" title="{display/@displayToolTip}">
	                                                            <xsl:value-of select="display"/>
	                                                        </option>
	                                                    </xsl:otherwise>
	                                                </xsl:choose>
	                                            </xsl:for-each>
	                                        </select>
	                                        <xsl:choose>
				                                <xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                 	<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" style="display:none"/>
				                                </xsl:when>
				                            </xsl:choose>
	                                    </xsl:when>
	                                    <xsl:when test="@type = 'Radio'">
	                                    	<xsl:choose>
												<xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
				                                        <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
					                                        <xsl:for-each select="answers/answer">
					                                            <xsl:choose>
					                                                <xsl:when test="$default = display">
					                                                	<label>
						                                                    <input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"  onfocus="checkButtons(this);" checked="true" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);" />
						                                                    <div class="questionText" title="{display/@displayToolTip}">
						                                                		<xsl:value-of select="display"/>
						                                                	</div>
					                                                    </label>
					                                                    <xsl:choose>
						                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
						                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
						                                               		</xsl:when>
						                                               	</xsl:choose>
					                                                </xsl:when>
					                                                <xsl:otherwise>
					                                                    <label>
					                                                    	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);" />
					                                                    	<div class="questionText" title="{display/@displayToolTip}">
						                                                		<xsl:value-of select="display"/>
						                                                	</div>
					                                                    </label>
					                                                    <xsl:choose>
						                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
						                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
						                                               		</xsl:when>
						                                               	</xsl:choose>
					                                                </xsl:otherwise>
					                                            </xsl:choose>
					                                            <xsl:choose>
					                                                 <xsl:when test="$horizontalDisplay!='true'">
					                                                     <br/>
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
				                                                    	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}"  onfocus="checkButtons(this);" checked="true" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);" />
				                                                    	<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>
				                                                    </label>
				                                                    <xsl:choose>
					                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
					                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
					                                               		</xsl:when>
					                                               	</xsl:choose>
				                                                </xsl:when>
				                                                <xsl:otherwise>
				                                                	<label>
				                                                    	<input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);" />
				                                                    	<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>
				                                                    </label>
				                                                    <xsl:choose>
					                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
					                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
					                                               		</xsl:when>
					                                               	</xsl:choose>
				                                                </xsl:otherwise>
				                                            </xsl:choose>
				                                            <xsl:choose>
				                                                 <xsl:when test="$horizontalDisplay!='true'">
				                                                     <br/>
				                                                 </xsl:when>
				                                            </xsl:choose>
				                                        </xsl:for-each>
				                                  </xsl:otherwise>
				                             </xsl:choose>
				                             <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>
	                                    </xsl:when>
	                                    
	                                    <xsl:when test="@type = 'Checkbox'">
	                                    	<xsl:choose>
												<xsl:when test="formQuestionAttributes/htmlAttributes/fontSize != 2">
													<font size="{formQuestionAttributes/htmlAttributes/fontSize}">
				                                        <xsl:for-each select="answers/answer">
				                                            <xsl:choose>
				                                                <xsl:when test="$default = display">
				                                                    <label>
				                                                   		<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                   		<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>
				                                                    </label>
				                                                    <xsl:choose>
					                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
					                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
					                                               		</xsl:when>
					                                               	</xsl:choose>
				                                                </xsl:when>
				                                                <xsl:otherwise>
				                                                    <label>
				                                                    	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
				                                                    	<div class="questionText" title="{display/@displayToolTip}">
					                                                		<xsl:value-of select="display"/>
					                                                	</div>
				                                                    </label>
				                                                    <xsl:choose>
					                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
					                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
					                                               		</xsl:when>
					                                               	</xsl:choose>
				                                                </xsl:otherwise>
				                                            </xsl:choose>
				                                            <xsl:choose>
				                                                 <xsl:when test="$horizontalDisplay!='true'">
				                                                     <br/>
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
			                                                    	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
			                                                    	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
			                                                    </label>
			                                                    <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                               		</xsl:when>
				                                               	</xsl:choose>
			                                                </xsl:when>
			                                                <xsl:otherwise>
			                                                    <label>
			                                                    	<input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" />
			                                                    	<div class="questionText" title="{display/@displayToolTip}">
				                                                		<xsl:value-of select="display"/>
				                                                	</div>
			                                                    </label>
			                                                    <xsl:choose>
				                                                	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                                		<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
				                                               		</xsl:when>
				                                               	</xsl:choose>
			                                                </xsl:otherwise>
			                                            </xsl:choose>
			                                            <xsl:choose>
			                                                 <xsl:when test="$horizontalDisplay!='true'">
			                                                     <br/>
			                                                 </xsl:when>
			                                            </xsl:choose>
			                                        </xsl:for-each>
		                                        </xsl:otherwise>
											</xsl:choose>
	                                    </xsl:when>
	                                    <xsl:when test="@type = 'Calculated'">
	                                        <xsl:variable name="calc" select="@calculation"/>
	                                        <xsl:variable name="answertype" select="@answertype"/>
	                                         <xsl:variable name="decimalPrecision" select="@decimalPrecision"/>
	                                        <input type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" readonly="yes" size="{$textboxSize}"  onfocus="calculate(this,'{$calc}', '{@answertype}','{$decimalPrecision}')" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')"/>
	                                    </xsl:when>
	                                    <xsl:otherwise>
	                                        Unknown Question Type: System Error
	                                    </xsl:otherwise>
	                                </xsl:choose>
	
	                            </xsl:otherwise>
	                        </xsl:choose>
	                        <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_default" value="{$default}"/>
	   	                                   	 <xsl:choose>
		                                    	<xsl:when test="@downDescription ='true'">
		                                    	<span style="font-size:14pt">
		                                    	<br/>
		                                    	</span>
		                                    	</xsl:when>
		                                  	  </xsl:choose>                         
	                    </td>
	                </xsl:otherwise>
	            </xsl:choose>
	        </tr> <!-- show question end -->
			<!--Question  graphics  -->
			
			<tr align='left'>
	            <td colspan="2" id="S_{$questionSectionNode}_Q_{$qid}_link">
	                <xsl:for-each select="images/filename">
	                  	<img class="imgThumb" src="" imageName="{.}" questionId="{$qid}" alt="Question Image" border="0"  style="cursor: pointer"/>&#160;&#160;
	                </xsl:for-each>
	                <xsl:for-each select="files/filename">
	                	<xsl:variable name="fileLink" select="@fileLink"/>
	                	<xsl:variable name="fileLink" select="@fileLink"/>
	                  	<a href="{@fileLink}" fileName="{.}" questionId="{$qid}" alt="Question File"><xsl:value-of select="current()"/></a>&#160;&#160;
	                </xsl:for-each>
	            </td> 
	        </tr> 
	        </table>
        </td>
    </xsl:template>

</xsl:stylesheet>
