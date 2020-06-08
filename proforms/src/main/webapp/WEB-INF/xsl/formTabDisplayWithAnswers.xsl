<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="webroot"/>
    <xsl:variable name="displaytop" select="/form/TOC/@display"/>
    <xsl:variable name="hasBtrisMappingQuestion" select="/form/@hasBtrisMappingQuestion"/>
    <xsl:param name="imageroot"/>
    <xsl:param name="cssstylesheet"/>
    <xsl:param name="title"/>
    <xsl:param name="dictionaryWsRoot"/>

    <xsl:output method="html" indent="yes"/>
    <xsl:template match="/">
       <style type="text/css">
    		textarea, input[type="date"], input[type="datetime"], input[type="datetime-local"], input[type="email"], input[type="month"], input[type="number"], input[type="password"], input[type="search"], input[type="tel"], input[type="text"], input[type="time"], input[type="url"], input[type="week"] {
    			   width: 300px; margin-bottom: 2px;}
    		select{width: -1;    margin-bottom: 2px;}
    	</style>
        <script language="JavaScript" type="text/javascript" src="{$webroot}/common/common.js"></script>
        <script language="JavaScript" type="text/javascript" src="{$webroot}/common/tabs.js"></script>
        <script type="text/javascript">
        	$(document).ready(function(){
    			$('select').each(function(){
    				if($(this).val()!=null){
	    				if($(this).val().indexOf('Other, please specify')>-1){
	    					$("#"+$(this).attr('id')+"_otherBox").show();
	    				}else{
	    					$("#"+$(this).attr('id')+"_otherBox").val('');
							$("#"+$(this).attr('id')+"_otherBox").hide();
	    				}
	    			}
    			});
    		});
    		
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
		<table width="100%" style="border-spacing:2px 0px; border: {htmlAttributes/formBorder}px ridge; font-size:{/form/htmlAttributes/formFontSize}pt;" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">
			<tr align="center"> <!-- Form header -->
                <td style="font-family:{htmlAttributes/formFont}; color: {htmlAttributes/formColor}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    <xsl:value-of select="formHeader" disable-output-escaping="yes"/>
                    <hr/>
                </td>
            </tr>
            <tr align="left">    <!-- Form name -->
                <td style="font-family: {htmlAttributes/formFont}; color: {htmlAttributes/formColor}">
                	<b><xsl:value-of select="name"/></b>
                </td>
            </tr>
	        <tr align="left"> <!-- Form description -->
	            <td style="font-family: {htmlAttributes/formFont}; color: {htmlAttributes/formColor}">
	            	<I><xsl:value-of select="description"/></I>
	            </td>
	        </tr>
	        <tr align="left"><!-- BTRIS Get All Button -->
	          <td>
		           <xsl:choose>
	                     <xsl:when test="$hasBtrisMappingQuestion = 'true'">
	                     	<input type="button" id="getAllBtrisData" value="Get All BTRIS Data" class="allBtrisDataBtn" 
	  									title="Get All BTRIS Data for All Mapped Questions"
	  									onclick="openMappedBtrisQuestionsDlg();" 
	  									style="float: right; display: none;"/>
	   					</xsl:when>
	   			    </xsl:choose>
	          </td>
	        </tr>
            <xsl:apply-templates select="TOC"/>
        </table>
        <table style="border-spacing:2px 0px;width:100%">
                <!-- PROP OPEN TABLE TO DISPLAY SITE PROPERLY WITH TABS -->
		  <tr> 
		     <td>
                 <img id="prop" src="{$imageroot}/spacer.gif" width="1" height="1" alt="" border="0"/>  <!-- to hidde the other tabs content -->
             </td>
          </tr>
          <tr>
             <td>
                <div id="tabs" align="left">  </div>
                <xsl:apply-templates select="row"/>
                <div id="controlContainer"><div id="controlContent" class="control"></div></div>
             	<script language="Javascript">window.setTimeout("tabInit();", 500);</script>
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
                <td style="font-family:{htmlAttributes/formFont}; color: {htmlAttributes/formColor}; font-size:{/form/htmlAttributes/formFontSize}pt;">
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

    <xsl:template match="row">
      <xsl:variable name="rowNum" select="@rowNum"/>
        <xsl:variable name="tabLabel" select="@tabLabel"/>
        <div id="content{$rowNum}" class="tabContent">
	        <table width="100%" style="border-spacing:2px 0px">
	            <tr style="font-size:{htmlAttributes/formFontSize};">	
	                <script language="javascript">
	                    document.getElementById('tabs').innerHTML += '<span id="tab{$rowNum}" class="tab"><span class="tabLeft"></span><span class="tabRight">'+"<xsl:value-of select="$tabLabel"/>"+'</span></span>';	                   
	                </script>
	                <xsl:apply-templates select="formcell"/>
	           </tr>
	        </table>
        </div>
    </xsl:template>

   <xsl:template match="formcell">
        <xsl:variable name="theStyle" select="@theStyle"/>
        <xsl:variable name="theRowSpan" select="@theRowSpan"/>
        <xsl:variable name="theColSpan" select="@theColSpan"/>
        <td style="vertical-align: top;{$theStyle}" rowspan="{$theRowSpan}" colspan="{$theColSpan}" >
            <xsl:apply-templates select="section"/>
        </td>
    </xsl:template>

    <xsl:template match="section">
        <xsl:variable name="collapsable" select="@isCollapsable"/>
        <xsl:variable name="sectionid" select="@id"/>
        <xsl:variable name="maxNoofColumnForEachRowInSection" select="@maxNoofColumnForEachRowInSection"/>
        	<xsl:choose>
                <xsl:when test="@isCollapsable = 'true'">
                    <table width="100%" style="border-spacing:2px 0px" cellspacing="0" cellpadding="0" border="0">
                    	<tr width="100%">
                    		<td  width="100%" align="right" valign="top" >
                        		<DIV style="z-index:99;position:relative; top:17px; padding-left:10px; height:18px;" align="left"><xsl:value-of select="name"/></DIV>
                        		<img style="position:relative; z-index:0;" src="{$imageroot}/sectionCollapseBkgd.gif" height="18" width="100%" BORDER="0"/>

 							</td>
 							<td valign="bottom">
 								<a href="javscript:;" onclick="toggleVisibility('sectionContainer_{$sectionid}', 'sectionImg_{$sectionid}');">
 									<img src="{$imageroot}/ctdbCollapse.gif" alt="expand / collapse" id="sectionImg_{$sectionid}" border="0" style="border: none;" />
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
        		<table width="100%" style="border-spacing:2px 0px; border: {htmlAttributes/sectionBorder}px solid; " border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="2">
		            <tr align='left' bgcolor= "#EFEFEF">
		                <td style="font-family: {htmlAttributes/sectionFont}; color: {htmlAttributes/sectionColor}; font-size:{/form/htmlAttributes/formFontSize}pt;" colspan="{$maxNoofColumnForEachRowInSection}">
		                <xsl:variable name="secid" select="sectionid" />
		                    <span id="{$secid}" />
		                    <xsl:choose>
		                        <xsl:when test="@textDisplayed = 'true'">
		                            <b><xsl:value-of select="name"/></b> <!-- section Name -->
		                            <br/>
		                            <I><xsl:value-of select="description"/></I><!-- fix the description won't show bug -->
		                            <hr/>
		                        </xsl:when>
		                    </xsl:choose>
		                    <xsl:choose>
		                        <xsl:when test="$displaytop = 'true'">
		                            <xsl:choose>
		                                <xsl:when test="@inTableOfContents = 'true'">
		                                    <span>&#160;&#160;</span><a href="javascript:jumpto('000')"><font color="{htmlAttributes/sectionColor}">top</font></a>
		                                </xsl:when>
		                            </xsl:choose>
		                        </xsl:when>
		                    </xsl:choose>
		                </td>
		            </tr>
		            <tr>
		            	<td valign="top">
		            		<!-- <table width="100%" style="border-spacing:2px 0px" border="0" cellpadding="{/form/htmlAttributes/cellpadding}" cellspacing="1">
		            			<xsl:apply-templates select="questions/question"/> Questions
		            		</table> -->
		            		<xsl:for-each select="sectionRows">
	     						<tr>
	     							<xsl:apply-templates select="questions/question"/>
	     						</tr>
     						</xsl:for-each>
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
        <xsl:variable name="bgColor" select="bgColor"/>
		<!-- added by Ching Heng for File question -->
        <xsl:variable name="attachmentId" select="attachmentId"/>
        <xsl:variable name="questionSectionNode" select="questionSectionNode"/>
        <!-- added by Ching Heng for other option text box -->
        <xsl:variable name="otherBox" select="otherBox"/>
        <xsl:variable name="scoreStr" select="scoreStr"/>
        	<xsl:variable name="floorColSpanTD" select="@floorColSpanTD"/>	
		<xsl:variable name="widthTD" select="@widthTD"/>	
			
    	
    	<td  colspan="{$floorColSpanTD}" style="width:{$widthTD}%;">
       
        <table>
        
        
       
        <tr  bgcolor="{$bgColor}"   class="questionTR"  onmouseover= "this.bgColor= '#ffea8a';"   onmouseout= "this.bgColor= '{$bgColor}';">
            <xsl:choose>
                <xsl:when test="@type= 'Visual Scale'">    <!-- THIS IS THE VISUAL SCALE PORTION -->
                     <xsl:variable name="rightText" select="@rightText"/>
                     <xsl:variable name="leftText" select="@leftText"/>
                     <xsl:variable name="width" select="@width"/>
                     <xsl:variable name="scaleMin" select="@scaleMin"/>
                     <xsl:variable name="scaleMax" select="@scaleMax"/>
                    <xsl:variable name="centerText" select="@centerText"/>
                    <xsl:variable name="showHandle" select="@showHandle"/>

                    <td colspan="2" align="left">
                        <table style="border-spacing:2px 0px" align="left" cellspacing="10">
                            <tr align="center">   <!-- THIS ROW IS FOR THE QUESTION TEXT -->
                                <td colspan='3' align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
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
	                                                    <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
	                                                        <xsl:value-of select="text" disable-output-escaping="yes"/>
	                                                    </font><br/><br/>
	 			                                        <span style="font-family: arial; color: black;font-size:10pt">
				                                        <xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
				                                        </span>  
                                                </xsl:when>
                                                <xsl:otherwise>
                                                	<xsl:choose>
                                                		<xsl:when test="@upDescription ='true'">
	   														<span style="font-family: arial; color: black;font-size:10pt">
					                                        	<xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
					                                        </span><br/><br/>
				                                        </xsl:when>
				                                     </xsl:choose>
	                                                 <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
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
                                    	{<span id="S_{$questionSectionNode}_Q_{$qid}_amount" style="border:0; color:#f6931f; font-weight:bold;"><xsl:value-of select="$default"/>/<xsl:value-of select="$scaleMax"/></span>}<!-- show the index number -->
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
                                        <input class="slider-input"  id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" />
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
                                                    _sl<xsl:value-of select="$qid"/>.setValue(<xsl:value-of select="$default"/>);
                                            </xsl:when>
                                        </xsl:choose>
                                        } catch (err) {}
                                      
                                      function resetSlider(){
	                                        $( "#S_<xsl:value-of select="$questionSectionNode"/>_Q_<xsl:value-of select="$qid"/>_amount" ).html(<xsl:value-of select="$scaleMin"/>+"/"+ <xsl:value-of select="$scaleMax"/>);
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

                    </td>
                </xsl:when>
                <!-- Check to see if it is a patient calendar -->
                
 <!--                <xsl:when test="@type = 'Patient Calendar'">
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
                        			<span style="font-family: arial; color: black;font-size:10pt">
                                    <xsl:value-of select="descriptionUp" disable-output-escaping="yes"/>
                                    </span><br/><br/>
                                    <xsl:value-of select="text" disable-output-escaping="yes"/>
                                    <span style="font-family: arial; color: black;font-size:10pt">
                                    <xsl:value-of select="descriptionDown" disable-output-escaping="yes"/>
                                    </span> 
                                </xsl:when>
                            </xsl:choose>
                            <table style="border-spacing:2px 0px" cellpadding="0">
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

                </xsl:when> -->
                <xsl:when test="@type = 'Image Map'">
                    <xsl:variable name="imageHeight" select="@imageHeight"/>
                    <xsl:variable name="imageWidth" select="@imageWidth"/>
                    <xsl:variable name="imageMapFileName" select="@imageMapFileName"/>
                    <xsl:variable name="gridFileName" select="@gridFileName"/>
                    <td colspan="2">
                        <table width="100%" style="border-spacing:2px 0px" border="0">
                            <tr>
                                 <td colspan="1" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">

                                    <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
                                    <xsl:choose>
                                        <xsl:when test="formQuestionAttributes/@required = 'true'">
                                            <span style="color:red">*</span>
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
	                                                <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
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
	                                                <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
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
	                                <!-- Image map remove option button -->
	                                <img src="{$imageroot}/buttonRemove.png" alt="Delete selected option" title="Delete" tabindex="0" 
	                                     border="0" width="23" height="23"
	                                     onkeypress="removeItemMacOpt (document.getElementById('imageMap_S_{$questionSectionNode}_Q_{$qid}'), 'NO');onBlurElem(document.getElementById('imageMap_S_{$questionSectionNode}_Q_{$qid}')" 
	                                     onclick="removeItemMacOpt (document.getElementById('imageMap_S_{$questionSectionNode}_Q_{$qid}'), 'NO');onBlurElem(document.getElementById('imageMap_S_{$questionSectionNode}_Q_{$qid}')"/>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="3" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                                    <!-- one line space -->
                                    <img src="{$imageroot}/spacer.gif" border="0" height="1" width="1" id="imageMapRef_S_{$questionSectionNode}_Q_{$qid}" style="z-index:-100; position:absolute"/>
                                    <xsl:choose>
                                        <xsl:when test="@displayGrid = 'true'">
                                            <!-- <img src="{$imageroot}/spacer.gif" border="0"
                                                height="{$imageHeight}" width="{$imageWidth}"
                                                id="imageMapSpacer_{$qid}" style="z-index:-100"/> --> <!--  as big as Image map space -->
                                            <!-- hidded by Ching heng  -->
                                            <div style="float:left;position:relative">
                                            	<!-- make image at right position -->
	                                            <img src="{$imageroot}/spacer.gif" border="0" height="{$imageHeight}" width="1"
	                                                id="imageMapSpacer_S_{$questionSectionNode}_Q_{$qid}" style="z-index:-100"/>
                                            	<!-- image map -->
	                                            <img src="{$imageroot}/questionimages/{$imageMapFileName}" border="0"
	                                                height="{$imageHeight}" width="{$imageWidth}"
	                                                style="z-index:100; position:absolute" id="imageMapImage_S_{$questionSectionNode}_Q_{$qid}"/>
	                                            <!-- image map grid --> 
	                                            <img src="{$imageroot}/{$gridFileName}" border="0"
	                                                height="{$imageHeight}" width="{$imageWidth}"
	                                                style="z-index:500; position:absolute" id="imageMapGrid_{$qid}"
	                                                useMap="#theMap_S_{$questionSectionNode}_Q_{$qid}"/>
	                                            <div style="display:none"><xsl:value-of select="mapHtml" disable-output-escaping="yes"/></div>
	                                        </div>
                                            <script language="javascript">
                                                setTimeout ('updateGridLocation (<xsl:value-of select="$questionSectionNode"/>,<xsl:value-of select="$qid"/>)', 1000);
                                            </script>
                                        </xsl:when>
                                        <xsl:otherwise>
                                        	<div style="float:left;">
                                        		<!-- make image at right position -->
	                                           <!--  <img src="{$imageroot}/spacer.gif" border="0" height="{$imageHeight}" width="1"
	                                                id="imageMapSpacer_{$qid}" style="z-index:-100"/> -->
	                                            <!-- image map -->
	                                            <img src="{$imageroot}/questionimages/{$imageMapFileName}" border="0"
	                                                height="{$imageHeight}" width="{$imageWidth}"
	                                                useMap="#theMap_S_{$questionSectionNode}_Q_{$qid}"/>
                                            	<div style="display:none"><xsl:value-of select="mapHtml" disable-output-escaping="yes"/></div>
                                            </div>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
                        </table>
                        <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_default" value="{$default}"/>

                    </td>
                </xsl:when>
                <!-- added by Ching Heng for File question Type -->
                <xsl:when test="@type = 'File'">
                	<td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                        <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
                        <xsl:choose>
                            <xsl:when test="formQuestionAttributes/@required = 'true'">
                                <span style="color:red">*</span>
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
	                                    <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
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
                                         <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
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
	                	<a herf="javascript:" style="cursor:pointer"  assoc_questionId="S_{$questionSectionNode}_Q_{$qid}" class="fileDownloadLink" filename="{$default}" attachmentId="{$attachmentId}" onclick="downloadQuestionFile('{$attachmentId}','{/form/formId}');"><xsl:value-of select="$default"/></a>
	                </td>
	                <td id="replaceFileField">
	                	Replace File:<input type="file" id="S_{$questionSectionNode}_Q_{$qid}" name="value(S_{$questionSectionNode}_Q_{$qid})" class="fileInput"/>
	                	<input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_fileKey" name="fileUploadKey" value=""/>
	                </td>
                </xsl:when>
                
                <xsl:when test="@type = 'Textblock'">
               		   <xsl:variable name="htmltext" select="@htmltext"/>
               		  <td colspan="2" align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}">
               		  	<span><xsl:value-of select="htmltext" disable-output-escaping="yes"/></span>
               		  </td>               
                </xsl:when>
                
				<xsl:when test="$horizDisplayBreak='true'">
                <!-- if horizontal display is true it must be a radio or checkbox -->

                <td colspan="2">
                <table style="border-spacing:2px 0px">
                    <tr>
                        <td  align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};">
                                <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
                                <xsl:choose>
                                    <xsl:when test="formQuestionAttributes/@required = 'true'">
                                        <span style="color:red">*</span>
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
	                                            <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
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
	                                             <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
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
                    </tr>
                    <tr>
                    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt;">

                    <xsl:choose>
                        <xsl:when test="@type = 'Radio'">
                            <xsl:choose>
                                <xsl:when test="$hasSkipRule = 'false'">
                                	<font size="{formQuestionAttributes/htmlAttributes/fontSize}">   <!-- add font size by Ching Heng -->
	                                    <xsl:for-each select="answers/answer">
	                                        <xsl:choose>
	                                            <xsl:when test="$default = display">
	                                                <input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)">
	                                                    &#xa0;<xsl:value-of select="display"/>
	                                                </input>
	                                                <xsl:choose>
				                                       <xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                       		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
				                                       </xsl:when>
				                                    </xsl:choose>	 
	                                            </xsl:when>
	                                            <xsl:otherwise>
	                                                <input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)">
	                                                    &#xa0;<xsl:value-of select="display"/>
	                                                </input>
	                                                <xsl:choose>
				                                        <xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           	&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
                                	<font size="{formQuestionAttributes/htmlAttributes/fontSize}">   <!-- add font size by Ching Heng -->
                                   		 <xsl:for-each select="answers/answer">
                                            <xsl:choose>
                                                <xsl:when test="$default = display">
                                                    <input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" checked="true" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);">
                                                        &#xa0;<xsl:value-of select="display"/>
                                                    </input>
                                                    <xsl:choose>
				                                       <xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                       		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
				                                       </xsl:when>
				                                    </xsl:choose>	
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onMouseDown="toggleRadio(this);">
                                                        &#xa0;<xsl:value-of select="display"/>
                                                    </input>
                                                    <xsl:choose>
				                                        <xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           	&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
                                </xsl:otherwise>
                            </xsl:choose>
                            <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>
                        </xsl:when>
                        <xsl:when test="@type='Checkbox'">
                            <xsl:choose>
                                <xsl:when test="$hasSkipRule = 'false'">
                                	<font size="{formQuestionAttributes/htmlAttributes/fontSize}">   <!-- add font size by Ching Heng -->
	                                    <xsl:for-each select="answers/answer">
	                                        <xsl:choose>
	                                            <xsl:when test="selected = 'true'">
	                                                <input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')">
	                                                    &#xa0;<xsl:value-of select="display"/>
	                                                </input>
	                                                <xsl:choose>
				                                       	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                       		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
				                                       	</xsl:when>
				                                    </xsl:choose>
	                                            </xsl:when>
	                                            <xsl:otherwise>
	                                                <input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')">
	                                                    &#xa0;<xsl:value-of select="display"/>
	                                                </input>
	                                                 <xsl:choose>
				                                    	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                        	&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
                                	<font size="{formQuestionAttributes/htmlAttributes/fontSize}">   <!-- add font size by Ching Heng -->
	                                    <xsl:for-each select="answers/answer">
	                                        <xsl:choose>
	                                            <xsl:when test="$default = display">
	                                                <input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')">
	                                                    &#xa0;<xsl:value-of select="display"/>
	                                                </input>
	                                                <xsl:choose>
				                                       	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                       		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
				                                       	</xsl:when>
				                                    </xsl:choose>
	                                            </xsl:when>
	                                            <xsl:otherwise>
	                                                <input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')">
	                                                    &#xa0;<xsl:value-of select="display"/>
	                                                </input>
	                                                <xsl:choose>
				                                        <xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           	&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                       </xsl:choose>
                        <!-- Btris Mapping -->
                        <xsl:choose>
                             <xsl:when test="@hasBtrisMapping = 'true'">
                            		<xsl:for-each select="btrisMapping">
                            		<br/><img src="{$imageroot}/icons/information.png" class="btrisMappingInfo" alt="Info Image" title="Has Btris Mapping" border="0" style="float: right; display: none;"/>
           						</xsl:for-each>
           					</xsl:when>
           			   </xsl:choose>
                      </td>
                   </tr>
                </table>
                <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_default" value="{$default}"/>
                </td>
                </xsl:when>
                <xsl:otherwise>
                    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color};" id='qTextTD'>
                        <img src="{$imageroot}/spacer.gif" width="{formQuestionAttributes/htmlAttributes/indent}" height="1" alt="" border="0"/>
                        <xsl:choose>
                            <xsl:when test="formQuestionAttributes/@required = 'true'">
                                <span style="color:red">*</span>
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
	                                    <font size="{formQuestionAttributes/htmlAttributes/fontSize}">
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
	                                    <span style="font-size:{/form/htmlAttributes/formFontSize}pt">
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
                    <td align="{formQuestionAttributes/htmlAttributes/align}" valign="{formQuestionAttributes/htmlAttributes/valign}" width="50%" style="font-family: {formQuestionAttributes/htmlAttributes/fontFace}; color: {formQuestionAttributes/htmlAttributes/color}; font-size:{/form/htmlAttributes/formFontSize}pt">
                    	<div style="display: table-cell; float: left;">
  	                                   	 <xsl:choose>
	                                    	<xsl:when test="@upDescription ='true'">
	                                    	<span style="font-size:14pt">
	                                    	<br/>
	                                    	</span>
	                                    	</xsl:when>
	                                  	  </xsl:choose>                      
                        <!-- DETERMINE IF QUESTION HAS SKIP RULE -->
                        <xsl:choose>
                            <xsl:when test="$hasSkipRule = 'false'">
                                <!-- QUESTION DOES NOT HAVE A SKIP RULE -->
                                <xsl:variable name="answertype" select="@answertype"/>
                                <xsl:choose>

                                    <xsl:when test="@type = 'Textbox'">
                                    	<xsl:choose>
                                    		<xsl:when test="@answertype = 'datetime'">
                                        		<input class="dateTimeField" type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"  size="{/formQuestionAttributes/textboxSize}" onfocus="checkButtons(this);" value="{$default}"/>
                                        		</xsl:when>
                                        		<xsl:otherwise>
                                        			<xsl:choose>
	                                        			<xsl:when test="@answertype = 'date'">
	                                        				<input class="dateField"  type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"  size="{/formQuestionAttributes/textboxSize}" onfocus="checkButtons(this);" value="{$default}"/>
	                                        			</xsl:when>
	                                        			<xsl:otherwise>
	                                        				<input type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}"  size="{/formQuestionAttributes/textboxSize}" onfocus="checkButtons(this);" value="{$default}"/>
	                                        			</xsl:otherwise>
                                        			</xsl:choose>
                                        		</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Textarea'" >
                                            <xsl:choose>
                                             <xsl:when test="@answertype = 'datetime'">
                                                 <textarea id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="dateTimeField" onfocus="checkButtons(this);"  style="overflow:auto; word-wrap:break-word"  >
                                                   	 <xsl:value-of select="$default"/>
                                               		</textarea>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                	<xsl:choose>
                                                		<xsl:when test="@answertype = 'date'">
		                                                   	<textarea id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="dateField" onfocus="checkButtons(this);"  style="overflow:auto; word-wrap:break-word" >
		                                                   	 <xsl:value-of select="$default"/>
		                                               		</textarea>
		                                               	</xsl:when>
		                                               	<xsl:otherwise>
	                                               		  <textarea id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="resizeMe" onfocus="checkButtons(this);"  style="overflow:auto; word-wrap:break-word" >
	                                                   	 	<xsl:value-of select="$default"/>
	                                               		  </textarea>
	                                               		  </xsl:otherwise>
                                               		</xsl:choose>
                                                </xsl:otherwise>
                                                </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Select'">
                                        <DIV style="padding:1px">
                                        <select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" onfocus="checkButtons(this);" onchange="displayOther(this);">
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
			                                 	&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox" name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
			                                </xsl:when>
			                            </xsl:choose>
			                            <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>
                                        </DIV>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Multi-Select'">
                                        <select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" multiple="true" size="5" onfocus="checkButtons(this);" onchange="displayOther(this);">
                                            <xsl:for-each select="answers/answer">
                                                <xsl:choose>
                                                    <xsl:when test="selected = 'true'">
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
			                                 	&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox" name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
			                                </xsl:when>
			                            </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Radio'">
										<font size="{formQuestionAttributes/htmlAttributes/fontSize}">   <!-- add font size by Ching Heng -->
	                                        <xsl:for-each select="answers/answer">
	                                            <xsl:choose>
	                                                <xsl:when test="$default = display">
	                                                    <input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)">
	                                                        &#xa0;<xsl:value-of select="display"/>
	                                                    </input>
	                                                     <xsl:choose>
				                                          	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
				                                        	</xsl:when>
				                                        </xsl:choose>
	                                                </xsl:when>
	                                                <xsl:otherwise>
	                                                    <input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);" onchange="displayRadioOther(this)">
	                                                        &#xa0;<xsl:value-of select="display"/>
	                                                    </input>
	                                                    <xsl:choose>
				                                          	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
										<input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Checkbox'">
										<font size="{formQuestionAttributes/htmlAttributes/fontSize}">   <!-- add font size by Ching Heng -->
	                                        <xsl:for-each select="answers/answer">
	                                            <xsl:choose>
	                                                <xsl:when test="selected = 'true'">
	                                                    <input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')">
	                                                        &#xa0;<xsl:value-of select="display"/>
	                                                    </input>
	                                                    <xsl:choose>
				                                          	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
				                                        	</xsl:when>
				                                        </xsl:choose>
	                                                </xsl:when>
	                                                <xsl:otherwise>
	                                                    <input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onfocus="checkButtons(this);" onchange="displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')">
	                                                        &#xa0;<xsl:value-of select="display"/>
	                                                    </input>
	                                                    <xsl:choose>
				                                          	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
                                    <xsl:when test="@type = 'Calculated'">
                                        <xsl:variable name="calc" select="@calculation"/>
                                        <xsl:variable name="answertype" select="@answertype"/>
                                        <xsl:variable name="decimalPrecision" select="@decimalPrecision"/>
                                        <input type="text" id="S_{$questionSectionNode}_Q_{$qid}" size="{$textboxSize}"  value="{$default}" readonly="yes" name="S_{$questionSectionNode}_Q_{$qid}" onfocus="calculate(this,'{$calc}', '{$answertype}','{$decimalPrecision}')"/>
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
                                        	<input class="dateTimeField" type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}" size="{/formQuestionAttributes/textboxSize}"  onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                        	<xsl:choose>
                                        	<xsl:when test="@answertype = 'date'">
                                        	 	<input class="dateField" type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}" size="{/formQuestionAttributes/textboxSize}"  onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')"/>
                                        	 </xsl:when>
                                        	 <xsl:otherwise>
                                        	 	<input type="text" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{$default}" size="{/formQuestionAttributes/textboxSize}"  onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')"/>
                                        	 </xsl:otherwise>
                                        	 </xsl:choose>
                                     	</xsl:otherwise>
                                    </xsl:choose>
                                   
                                    
                                    </xsl:when>
                                    <xsl:when test="@type = 'Textarea'">
                                        <table style="border-spacing:2px 0px">
                                            <tr><td align="left">
                                                <span class="smallText"><a href="Javascript:doNothing();" onClick="toggleOverflow ('Q_{$qid}');">expand/collapse</a></span>
                                            </td></tr>
                                            <tr><td>
                                            <xsl:choose>
                                             <xsl:when test="@answertype = 'datetime'">
                                                 <textarea id="Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="dateTimeField"  onfocus="checkButtons(this);">
                                                    <xsl:value-of select="$default"/>
                                                </textarea>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                	<xsl:choose>
	                                                	<xsl:when test="@answertype = 'date'">
		                                                 	 <textarea id="Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="dateField"   onfocus="checkButtons(this);">
		                                                    	<xsl:value-of select="$default"/>
		                                                	 </textarea>
	                                                	 </xsl:when>
	                                                	 <xsl:otherwise>
		                                                	 <textarea id="Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" class="resizeMe"   onfocus="checkButtons(this);">
		                                                    	<xsl:value-of select="$default"/>
		                                                	 </textarea>
	                                                	 </xsl:otherwise>
                                                	 </xsl:choose>
                                                </xsl:otherwise>
                                                </xsl:choose>
                                            </td></tr>
                                        </table>

                                    </xsl:when>
                                    <xsl:when test="@type = 'Select'">
                                        <DIV STYLE="padding:1px;">
                                        <select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" onfocus="checkButtons(this);" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayOther(this);">
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
			                                 	&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox" name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
			                                </xsl:when>
			                            </xsl:choose>
			                            <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>
                                        </DIV>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Multi-Select'">
                                        <select id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" multiple="true" size="5" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayOther(this);" onfocus="checkButtons(this);">
                                            <xsl:for-each select="answers/answer">
                                                <xsl:choose>
                                                    <xsl:when test="selected = 'true'">
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
			                                 	&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox" name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
			                                </xsl:when>
			                            </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Radio'">
										<font size="{formQuestionAttributes/htmlAttributes/fontSize}">   <!-- add font size by Ching Heng -->
	                                        <xsl:for-each select="answers/answer">
	                                            <xsl:choose>
	                                                <xsl:when test="$default = display">
	                                                    <input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);">
	                                                        &#xa0;<xsl:value-of select="display"/>
	                                                    </input>
	                                                    <xsl:choose>
				                                          	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
				                                        	</xsl:when>
				                                        </xsl:choose>
	                                                </xsl:when>
	                                                <xsl:otherwise>
	                                                    <input type="radio" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" title="{display/@displayToolTip}" ondblclick="clearSkipRuleForDoubleClick('{$skipRule}',[{$questionsToSkip}])" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayRadioOther(this)" onfocus="checkButtons(this);" onMouseDown="toggleRadio(this);">
	                                                        &#xa0;<xsl:value-of select="display"/>
	                                                    </input>
	                                                    <xsl:choose>
				                                          	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
	                                    <input type="hidden" id="S_{$questionSectionNode}_Q_{$qid}_scoreBox" name="S_{$questionSectionNode}_Q_{$qid}_scoreBox" value="{scoreStr}"/>
										</font>
                                    </xsl:when>
                                    <xsl:when test="@type = 'Checkbox'">
										<font size="{formQuestionAttributes/htmlAttributes/fontSize}">   <!-- add font size by Ching Heng -->
	                                        <xsl:for-each select="answers/answer">
	                                            <xsl:choose>
	                                                <xsl:when test="selected = 'true'">
	                                                    <input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" checked="true" title="{display/@displayToolTip}" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" onfocus="checkButtons(this);">
	                                                        &#xa0;<xsl:value-of select="display"/>
	                                                    </input>
	                                                    <xsl:choose>
				                                          	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" value="{$otherBox}"/>
				                                        	</xsl:when>
				                                        </xsl:choose>
	                                                </xsl:when>
	                                                <xsl:otherwise>
	                                                    <input type="checkbox" id="S_{$questionSectionNode}_Q_{$qid}" name="S_{$questionSectionNode}_Q_{$qid}" value="{display}" title="{display/@displayToolTip}" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}');displayCheckOther('S_{$questionSectionNode}_Q_{$qid}')" onfocus="checkButtons(this);">
	                                                        &#xa0;<xsl:value-of select="display"/>
	                                                    </input>
	                                                    <xsl:choose>
				                                          	<xsl:when test="includeOther = 'true'"> <!-- include Other option -->
				                                           		&#xa0;<input type="text" id="S_{$questionSectionNode}_Q_{$qid}_otherBox"  name="S_{$questionSectionNode}_Q_{$qid}_otherBox" disabled="true"/>
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
                                    <xsl:when test="@type = 'Calculated'">
                                        <xsl:variable name="calc" select="@calculation"/>
                                        <xsl:variable name="answertype" select="@answertype"/>
                                        <xsl:variable name="decimalPrecision" select="@decimalPrecision"/>
                                        <input type="text" id="S_{$questionSectionNode}_Q_{$qid}" size="{$textboxSize}"  value="{$default}" name="S_{$questionSectionNode}_Q_{$qid}" readonly="yes" onfocus="calculate(this, '{$calc}', '{$answertype}','{$decimalPrecision}')" onchange="applyskiprule(this, [{$questionsToSkip}], '{$skipOperator}','{$skipRule}','{$skipRuleEquals}')"/>
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

           			   </div>
           			   <div style="display: table-cell; float: right;">
           			   	 <!-- Btris Mapping -->
                         <xsl:choose>
                             <xsl:when test="@hasBtrisMapping = 'true'">
                            		<xsl:for-each select="btrisMapping">
                            		<br/><img src="{$imageroot}/icons/information.png" class="btrisMappingInfo" alt="Info Image" title="Has Btris Mapping" border="0" style="float: right; display: none;"/>
           						</xsl:for-each>
           					</xsl:when>
           			     </xsl:choose>
           			   </div>
	                   <div width="5%" class="enterAuditComment" style="display: none; float: right; margin-right: 10%;" id="enterAuditComment_S_{$questionSectionNode}_Q_{$qid}"   >
	                     	<img  id="imgAuditComment_S_{$questionSectionNode}_Q_{$qid}" src="{$webroot}/images/icons/comment.png" alt="Enter Comments" title="Enter comments" width="20" height="20"  />
	                   </div>                             
                    </td>
                </xsl:otherwise>
            </xsl:choose>
        </tr><!-- show question end -->
	
        <!--Question documents -->
        <tr align='left'>
            <td colspan="2" id="S_{$questionSectionNode}_Q_{$qid}_link">
               	<xsl:for-each select="images/filename">
                	<img class="imgThumb" src="{$dictionaryWsRoot}portal/ws/public/eforms/question/{$qid}/document/{.}" alt="Question Image" border="0"  style="cursor: pointer"/>&#160;&#160;
                </xsl:for-each>
                <xsl:for-each select="files/filename">
                	<xsl:variable name="fileLink" select="@fileLink"/>
                  	<a href="{@fileLink}" fileName="{.}" questionId="{$qid}" alt="Question File"><xsl:value-of select="current()"/></a>&#160;&#160;
                </xsl:for-each>
            </td>
        </tr>
        </table>
        </td>
    </xsl:template>
</xsl:stylesheet>