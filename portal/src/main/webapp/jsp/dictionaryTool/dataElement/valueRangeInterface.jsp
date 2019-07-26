<%@include file="/common/taglibs.jsp"%>
<title>Define the Data</title>

<!-- This header is required to get the autocompleter to work -->
<%-- <sx:head debug="false" cache="false" compressed="true" /> --%>
<s:set var="currentDataElement" value="currentDataElement" />
<s:set var="valueRangeForm" value="valueRangeForm" />
<s:set var="types" value="types" />
<s:set var="dataType" value="dataType" />
<s:set var="inValueRangeForm" value="true" />



<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<!-- begin #left-sidebar -->
	<s:if test="%{formType == 'edit'}">
			<div id="breadcrumb">
				<s:if test="inAdmin"><s:a action="searchDataElementAction" method="list" namespace="/dictionaryAdmin">Manage Data Elements</s:a></s:if>
				<s:else><s:a action="searchDataElementAction" method="list" namespace="/dictionary">Search Data Elements</s:a></s:else>
				&gt;
				<s:url action="dataElementAction" method="view" var="viewTag">
					<s:param name="dataElementId"><s:property value="currentDataElement.id" /></s:param>
				</s:url>
				<a href="<s:property value="#viewTag" />"><s:property value="currentDataElement.title" /></a> &gt; Edit Data Element
			</div>
		</s:if>
		<div style="clear:both;"></div>
	<jsp:include page="../../navigation/dataDictionaryNavigation.jsp" />
	<h1 class="float-left">
		<s:if test="inAdmin">(Admin)&nbsp;</s:if>
		Define the Data:&nbsp;<s:property value="currentDataElement.title" />
	</h1>
		<div style="clear:both;"></div>
	<!--begin #center-content -->
	<div id="main-content">
		

		<s:form id="theForm" action="dataElementAction" method="post" accept-charset="UTF-8">
<s:token />
			<s:if test="dataType == 'dataElement'">
				<ndar:dataElementChevron action="valueRangedataElementValidationAction" chevron="Define the Data" />
			</s:if>
			<s:if test="dataType == 'mapElement'">
				<ndar:dataElementChevron action="valueRangemapElementValidationAction" chevron="Define the Data" />
			</s:if>

			<s:if test="hasActionErrors()">
				<div class="form-error clear-both">
					<s:actionerror />
				</div>
			</s:if>
			<p>
					Fields marked with a <span class="required">* </span>are required.
				</p>
			<p>Describe the data that will be entered for this Data Element.</p>
			
			
			
			<div class="clear-float">
			
				
				<div class="clear-right"></div>
		
				<div id="dataRestrictionsHeader">
						<h3>Data Restrictions</h3>
					</div>
				<div class="form-field form-field-vert">
					<label class="required">Is there a defined list of permissible values? <span class="required">*</span>:</label>

					<ul id="definedOptions" class="checkboxgroup-horizontal">
						<li><input type="radio" name="valueRangeForm.defined" id="definedYes" class="radio" value="true"
							<s:if test="valueRangeForm.defined">checked="checked"</s:if> /> <label for="definedYes">Yes</label></li>
						<li><input type="radio" name="valueRangeForm.defined" id="definedNo" class="radio" value="false"
							<s:if test="!valueRangeForm.defined">checked="checked"</s:if> /> <label for="definedNo">No</label></li>
					</ul>
					<s:fielderror fieldName="valueRangeForm.defined" />
				
				</div>
					<div style="clear:both;"></div>
				<div class="form-field form-field-vert">
					<label class="required">What type of data will be stored? <span class="required">* </span>:</label>
					
					<s:select id="type" name="valueRangeForm.type" list="types" listKey="id" listValue="specialInstructions != '' ? value+' ('+specialInstructions+')' : value" 
							value="valueRangeForm.type.id" headerKey="" headerValue="- Select One -" />
							<s:fielderror fieldName="valueRangeForm.type" />
					
				</div>
				
		
	
		
		
		<div id="measuringUnitFields">
				
						

					
				
				
				<div id="defineValuesFields">
				
					
					<div id="valueRangeTable">
					
					<div class="form-field form-field-vert">
	<s:fielderror fieldName="valueRangeForm.multiple" />
	<div class="clear-both"></div>
	</div>
					<div class="clear-both"></div>
					</div>
					
						
					<div class="form-field form-field-vert">
						<label class="required">Can more than one value be selected? <span class="required">* </span>:
						</label>

						<ul class="checkboxgroup-horizontal">
							<li><input type="radio" name="valueRangeForm.multiple" id="multipleYes" class="radio" value="true"
								<s:if test="valueRangeForm.multiple">checked="checked"</s:if> /> <label for="multipleYes">Yes</label></li>
							<li><input type="radio" name="valueRangeForm.multiple" id="multipleNo" class="radio" value="false"
								<s:if test="!valueRangeForm.multiple">checked="checked"</s:if> /> <label for="multipleNo">No</label></li>
						</ul>
						<div style="clear:both;"></div>
					</div>
				</div>
				<hr class="underline">	
				<div id="measuringUnit"><jsp:include page="measuring-unit-autocomplete.jsp" /></div>
					
					<div class="clear-both"></div>
				</div>
				
				<div id="freeFormValuesFields">
					<div id="freeFormAlpha">
						<div class="form-field form-field-vert">
							<label for="size" class="required">Enter Maximum Character Quantity <span class="required">* </span>:
							</label>
							<s:textfield label="size" id="sizeId" maxlength="100" name="valueRangeForm.size" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
							<s:fielderror fieldName="valueRangeForm.size" />
							<div class="special-instruction">Maximum number of characters</div>
						</div>
					</div>

					<div id="freeFormNumeric">
						<div class="form-field form-field-vert">
							<label for="valueRangeForm.minimumValue">Enter minimum acceptable value:</label>
							<s:textfield id="valueRangeForm.minimumValue" name="valueRangeForm.minimumValue" maxlength="200"
								cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
							<s:fielderror fieldName="valueRangeForm.minimumValue" />
						</div>

						<div class="form-field form-field-vert">
							<label for="valueRangeForm.maximumValue">Enter maximum acceptable value:</label>
							<s:textfield id="valueRangeForm.maximumValue" name="valueRangeForm.maximumValue" maxlength="200"
								cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
							<s:fielderror fieldName="valueRangeForm.maximumValue" />
						</div>
					</div>
				</div>
	<hr class="underline">		
		<h3>Guidelines</h3>
	<div class="form-field form-field-vert">
					<label for="valueRangeForm.population" class="required">Population <span class="required">* </span>:
					</label>
					<s:select id="valueRangeForm.population" list="populationList" name="valueRangeForm.population" listKey="name"
						listValue="name" value="valueRangeForm.population.name" headerKey="" headerValue="- Select One -" />
					<s:fielderror fieldName="valueRangeForm.population" />
				</div>
				
	<div class="form-field form-field-vert">
					<label for="guidelines">Collection Guidelines &amp; Instructions:</label>
					<s:textarea cssClass="textfield" label="guidelines" cols="60" rows="4" name="valueRangeForm.guidelines" escapeHtml="true" escapeJavaScript="true" />
					<div class="special-instruction">E.g. "Choose the highest level attained by the participant/subject"</div>
					<s:fielderror fieldName="valueRangeForm.guidelines" />
				</div>

				<div class="form-field form-field-vert">
					<label for="suggestedQuestion">Preferred Question Text:</label>
					<s:textarea cssClass="textfield" label="suggestedQuestion" cols="60" rows="4"
						name="valueRangeForm.suggestedQuestion" escapeHtml="true" escapeJavaScript="true" />
					<div class="special-instruction">E.g. "What is the participant/subject level of education?"</div>
					<s:fielderror fieldName="valueRangeForm.suggestedQuestion" />
				</div>
		
		<hr class="underline">
			<h3 id="classificationHeader">Category Groups and Classification</h3>
				<input type="hidden" value="" name="valueRangeForm.domainList" >
				<input type="hidden" value="" name="valueRangeForm.subdomainList" >
				<input type="hidden" value="" name="valueRangeForm.subDomainElementList" >

				<!-- Search box for Diseases -->
				<div id="createTreeFilterContainer">
					
					<p>Use the diseases listed below to refine your search for domains and sub-domains</p>
					<hr>
					<!-- Disease List -->
					<div id="columnOne" class="treeColumn">
						
						
						<div class="facet-form-field">
							<b>Disease/Classification: <span class="required">*</span></b>
							<br>
							<br>
							<ul id="diseaseSelectionsValue" class="tree diseaseSelections">
								<s:iterator status="status" value="diseaseList">
								<s:set var="currentView" value="currentView" />
								<li name="selected_<s:property value="name" />" >
									
										<s:property value="name" />
										
										
									</li>
									<li id="classificationsLeafValueRange" class="classificationsLeaf closeLeaf">
									</li>
									
									
								</s:iterator>
							</ul>
						</div>
						
					
					</div>
				
					<!-- Domains -->
					<div id="columnTwo" class="treeColumn"></div>
				
				
					<!-- Sub-Domains -->
					<div id="columnThree" class="treeColumn"></div>
					<div style="clear:both;"></div>
				</div>
								
								
				
						<s:iterator var="var" value="valueRangeForm.classificationElementList">
						
							<s:set var="clist" value="#clist+#var.disease.name+'.'+#var.subgroup.subgroupName+'.'+#var.classification.name+','" />
						</s:iterator>
					
				<%-- <s:set var="clist" value="#valueRangeForm.classificationElementList" />--%>
				<s:hidden name="valueRangeForm.classificationElementList" value="%{clist}" escapeHtml="true" escapeJavaScript="true" />
				<s:fielderror fieldName="valueRangeForm.classification" />
				
				<div style="clear:both;"></div>
	<hr class="underline">	
			<%--- 
				<div class="form-field">
							<label for="disease" class="required">Disease <span class="required">* </span>:
							</label> <s:select id="diseaseSelect" name="dataElementForm.diseaseList" list="diseaseList" listKey="id" listValue="name" value="disease.id"
								disabled="%{isPublished || !#editableDiseases}" headerKey="" headerValue="- Select One -" />
							<s:fielderror fieldName="dataElementForm.diseaseList" />
							<!-- <div class="special-instruction">Hold "Ctrl" to select multiple values</div> -->
						</div>
						
				
					
	
					<div class="form-field">
						<label for="dataElementForm.domain" class="required">Domain <span class="required">* </span>:
						</label>
						<s:select id="domainSelect" list="domainList" listKey="id" listValue="name" name="dataElementForm.domain"
							value="dataElementForm.domain.id" headerKey="" headerValue="- Select One -" disabled="%{isPublished || !#editableDiseases}" />
						<s:fielderror fieldName="dataElementForm.domain" />
					</div>
					
					<div class="form-field">
						<label for="dataElementForm.subdomain" class="required">Sub-Domain <span class="required">* </span>:
						</label>
						<s:select id="dataElementForm.subdomain" list="subdomainList" listKey="id" listValue="name"
							name="dataElementForm.subdomain" value="dataElementForm.subdomain.id" headerKey="" headerValue="- Select One -"
							disabled="%{isPublished || !#editableDiseases}" />
						<s:fielderror fieldName="dataElementForm.subdomain" />
					</div>
					--%>	
					
					
				

				

				<div class="form-field clear-left">
					<div class="button">
						<input type="button" value="Continue"
							onClick="javascript:if(!verifyDomainPairing()){
				return;
			}; if(!confirmDomainPairSelection()){ return; };  submitForm('valueRange${dataType}ValidationAction!editKeywords.action')" />
					</div>
					<s:if test="%{formType=='edit'}">
						<a class="form-link" href="javascript:void();" onClick="javascript:if(!verifyDomainPairing()){ return; }; if(!confirmDomainPairSelection()){ return; }; submitForm('valueRange${dataType}ValidationAction!review.action');">Review</a>
					</s:if>
					<a class="form-link" href="javascript:cancel()">Cancel</a>
				</div>
		</s:form>
	</div>
</div>
<!-- end of #main-content -->
</div>
<!-- end of .border-wrapper -->

<script type="text/javascript">
	<s:if test="!inAdmin">
		<s:if test="%{formType == 'create'}">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"dataElementLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"searchDataElementLink"});
		</s:else>
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataElementsLink"});
	</s:else>
	
	var disease = "";
	var treeSelectedDisease = "";
	var classificationsArray = new Array();
	var diseaseArray = new Array();
	var domainArray = new Array();
	var subDomainArray = new Array();
	var chosenDiseases = new Array();
	
	
	<%-- There is probably a better way to do this. but for now this will do. r.s.--%>
	<s:if test="valueRangeForm.subDomainElementList.size > 0">
	
	<s:iterator var="i" value="valueRangeForm.subDomainElementList">
		diseaseArray.push("<s:property value='disease.name' />");
		domainArray.push("<s:property value='disease.name' />.<s:property value='domain.name' />");
		subDomainArray.push("<s:property value='disease.name' />.<s:property value='domain.name' />.<s:property value='subDomain.name' />");	
	</s:iterator>
	//after selection assign value to hidden form fields
	subDomainArrayList = subDomainArray.join(';');
    $('[name="valueRangeForm.subdomainList"]').val(subDomainArrayList); //domains
	</s:if>
	
	
	
	
	function confirmDomainPairSelection(){
		if(subDomainArray.length < 1) {
			alert('A disease, domain and subdomain pairing selection is required.');
			$('html, body').animate({
		 	    scrollTop:$( "#columnOne").offset().top - 10
		 	}, 500);
			return false;
		}
		return true;
	}
	
	function verifyDomainPairing() {
		//I need to verify that the subdomains have a domain pair
	
		
		//if there are values in the subDomain Array, let's make sure they have a domain in the domain array.
		domainWithSubdomain = new Array();
		diseaseWithSubdomain = new Array();
		
		
		if(subDomainArray.length > 0) {
			for (var i = 0; i < subDomainArray.length; i++) {
				
				splitSubDomain = subDomainArray[i].split(".");
				theDomain = splitSubDomain[0]+'.'+splitSubDomain[1];
				theDisease = splitSubDomain[0];
				
				domainWithSubdomain.push(theDomain);
				diseaseWithSubdomain.push(theDisease);
				
				//make sure there is a domain for the subDomain
				if($.inArray(theDomain,domainArray) <= -1) {
					
				 	alert("There is not a domain pairing for subdomain: "+subDomainArray[i].split('.')[2]+".  Please select domain:"+subDomainArray[i].split('.')[1]);
				 	scrollToSubdomain = subDomainArray[i].replace(/'/g,"\\'");
				 	
				 	$('html, body').animate({
				 	    scrollTop:$( "input[id='"+scrollToSubdomain+"']").offset().top - 10
				 	}, 500);
				 	return false;
				}
				
				
				///make sure there is a classification for the disease related to the chosen subdomain
			
				//assume a classification hasn't been chosen
				classificationExists = false;
				
				if(classificationsArray.length > 0) {
					for (var j = 0; j < classificationsArray.length; j++) {
						splitClassificationArray = classificationsArray[j].split(".");
						
						 if(splitClassificationArray[0] === splitSubDomain[0]) {
							classificationExists = true;
						} 
			
					}
					
					
					
					//we need a special check for tbi, right now this is hard coded hopefully this changes
					if(splitSubDomain[0] == "Traumatic Brain Injury" && classificationExists == true) {
						
						
						tbiClassificationsArray = ["Traumatic Brain Injury.Acute Hospitalized", "Traumatic Brain Injury.Epidemiology", "Traumatic Brain Injury.Moderate/Severe TBI: Rehabilitation", "Traumatic Brain Injury.Concussion/Mild TBI"] ;
						
						for (var j = 0; j < tbiClassificationsArray.length; j++) {
							
							var checkedClassification = false;
							
							for (var k = 0; k < classificationsArray.length; k++) {
								splitClassificationArray = classificationsArray[k].split(".");
								
								 if(splitClassificationArray[0]+'.'+splitClassificationArray[1] === tbiClassificationsArray[j]) {
									 checkedClassification = true;
								} 
					
							}
							
							
							
							
							if(!checkedClassification) {
								classificationExists = checkedClassification;
								break;
							}
							
						}
						
						
					} 
					
					
					if(!classificationExists) {
						if(splitSubDomain[0] == "Traumatic Brain Injury") {
							alert("Classifications have not been selected for disease:  "+splitSubDomain[0]+". Please select a classification.");
						} else {
							alert("A classification has not been selected for disease:  "+splitSubDomain[0]+". Please select a classification.");
							
						}
						scrollToDisease = splitSubDomain[0].replace(/'/g,"\\'");
						diseaseObj = $( "li[name='selected_"+scrollToDisease+"']");
						$('html, body').animate({
					 	    scrollTop:diseaseObj.offset().top - 10
					 	}, 500);
						
						openDisease(diseaseObj);
						return false;
					}
				} else {
					if(splitSubDomain[0] == "Traumatic Brain Injury") {
						alert("Classifications have not been selected for disease:  "+splitSubDomain[0]+". Please select a classification.");
					} else {
						alert("A classification has not been selected for disease:  "+splitSubDomain[0]+". Please select a classification.");
						
					}
					scrollToDisease = splitSubDomain[0].replace(/'/g,"\\'");
					diseaseObj = $( "li[name='selected_"+scrollToDisease+"']");
					$('html, body').animate({
				 	    scrollTop:diseaseObj.offset().top - 10
				 	}, 500);
					
					openDisease(diseaseObj);
					return false;
					
				}
				
				
				
				
				
			}
		} else if(classificationsArray.length > 0) {
			
			disease = classificationsArray[0].split(".")[0];
			alert("You have selected a classification for disease "+disease+" . But, there is not a subdomain and domain pairing");
			scrollToDisease = disease.replace(/'/g,"\\'");
			diseaseObj = $( "li[name='selected_"+scrollToDisease+"']");
			$('html, body').animate({
		 	    scrollTop:diseaseObj.offset().top - 10
		 	}, 500);
			
			openDisease(diseaseObj);
			return false;
			
		}
		
		
		
		
		
		//if there are values in the Domain Array, let's make sure they have a subdomain in the subdomain array.
		if(domainArray.length > 0) {
			for (var i = 0; i < domainArray.length; i++) {
				
				if($.inArray(domainArray[i],domainWithSubdomain) <= -1) {
					alert("There is not a subdomain pairing for the domain:  "+domainArray[i].split('.')[1]);
					
					$('html, body').animate({
				 	    scrollTop:$( "input[id='"+domainArray[i].replace(/'/g,"\\'")+"']").offset().top - 10
				 	}, 500);
					return false;
				}
				
			}
		} 
		
		//if there are values in the classifications Array, let's make sure they have a subdomain in the subdomain array.
		if(classificationsArray.length > 0) {
			for (var i = 0; i < classificationsArray.length; i++) {
				classificationSplit = classificationsArray[i].split('.');
				if($.inArray(classificationSplit[0],diseaseWithSubdomain) <= -1) {
					alert("There is not a subdomain pairing for the classification:  "+classificationSplit[1]+' '+classificationSplit[2]);
					
					diseaseObj = $( "li[name='selected_"+classificationSplit[0].replace(/'/g,"\\'")+"']");
					
					$('html, body').animate({
				 	    scrollTop:diseaseObj.offset().top - 10
				 	}, 500);
					
					openDisease(diseaseObj);
					return false;
				}
				
			}
		} 
		
		return true;
		
	}
///end verification functions 	
	$('document').ready( function() { 
		displayTable();
		updateInterface();
		$("input:radio").change( function(){
			updateInterface();
		 });
		
		$("#type").change( function(){
			updateInterface();
		 });
		
		
		//check to see if we have classifications in session, if so add em 
		if($('[name="valueRangeForm.classificationElementList"]').val().replace("null","").split(",").length > 0) {
			
			classificationsArray = $('[name="valueRangeForm.classificationElementList"]').val().replace("null","").split(",");
			classificationsArray = $.grep(classificationsArray,function(n){ return(n) });
			
			$('[name="valueRangeForm.classificationElementList"]').val(classificationsArray);
		}
		
		
		///this well set the selected class for diseases the user has chosen for this data Element
		if(diseaseArray.length > 0) {
			$('[name^="selected_"]').each(
					
					
					function(){
						
						
						name = $(this).attr('name');
						 nameArray = name.split("_");
						 diseaseVar = nameArray[1].toString();
						 
						
						 
						if($.inArray(diseaseVar,diseaseArray) > -1) {
							
							 if(!$(this).hasClass("selected")) {
									$(this).addClass("selected");
							}
						} 
					});
			
		}
		
		
		
		$('[name^="selected_"]').click(function(){
			
			
			 
			if(!verifyDomainPairing()){
				
				//if no disease is currently being viewed open up else don't move on.
			
				
					return;
				
			};
			
			
			
			 
			 
			 //empty domain and sub-domain columns
			 $('#columnTwo , #columnThree').html('');
			 
			 
			 openDisease($(this));
	    	
			
		});
		
		
		
		
	});
	
	
	
	function openDisease($this) {
		
		
		
		name = $this.attr('name');
		 nameArray = name.split("_");
		 treeSelectedDisease = nameArray[1].toString();
		
		 currentViewName = "";
		 if($('.currentView').attr('name') !== undefined && $('.currentView').attr('name') != '') {
			 currentViewName = $('.currentView').attr('name');
			 currentViewName = currentViewName.split("_")[1];
		}
		 
			if(treeSelectedDisease != currentViewName) {
			 	$('.closeLeaf').hide("slow");
			 	$('.closeLeaf').html('');
			}
		//remove selected class from previously selected disease
		 $('#columnOne li').each(function(){$(this).removeClass("currentView");});
		 
		 
		 //add current view class to selected disease
		$this.addClass("currentView");
		
			//populate domain list
   		$.ajax("createDataElementAction!updateDomain.ajax", {
   			"type": 	"POST",
   			"async": 	false,
   			"data": 	{"selectedDiseases" : treeSelectedDisease},
   			"success": 	function(data) {
   				
   							$("#columnTwo").html(data);	
   							
   							//let's check to see if the user has made any selection changes for this disease
   							
   								if(domainArray.length > 0) {
   									
   									$('#columnTwo [name="selectedDomains"]').each(
   											function(){
   												
   												if($.inArray($(this).val(),domainArray) > -1) {
   													
   													$(this).prop('checked', true);
   												} else {
   													$(this).prop('checked', false);
   												}
   											});
   								
   								}
   							
   								if($("#columnTwo input:checked").length < 1) {
       								$('#columnTwo [name="selectedAllDomains"]').prop("checked",false);
       							}
   								
   								
   								
   								

   							
   						
   						}
   		});
		
		var domainSelectArray = $('#columnTwo input[name="selectedDomains"]:checked').map(function() {
   		return this.value;
		}).get();
		
		var domainSelect = (domainSelectArray.length > 0) ? domainSelectArray.toString() : "all";
		
		
		
		 if(domainSelectArray.length > 0) {
		    		//populate sub-domain list
		    		$.ajax("createDataElementAction!updateSubDomain.ajax", {
		    			"type": 	"POST",
		    			"async": 	false,
		    			"data": 	{"selectedDiseases" : treeSelectedDisease,
		    						 "selectedDomains" : domainSelect},
		    			"success": 	function(data) {
		    				
		    							$("#columnThree").html(data);	
		    							
		    							///has the user made selections in this session.
		    							
		    								if(subDomainArray.length > 0) {
		    									
		    									$('#columnThree [name="selectedSubDomains"]').each(
		    											function(){
		    												
		    												if($.inArray($(this).val(),subDomainArray) > -1) {
		    													
		    													$(this).prop('checked', true);
		    												} else {
		    													$(this).prop('checked', false);
		    												}
		    											});
		    								
		    								}
		    							
		    								if($("#columnThree input:checked").length < 1) {
		        								$('#columnThree [name="allSubDomains"]').prop("checked",false);
		        							}
		    								
		    								
		    								///this will add the selected disease to the chosenDisease array if the user picks this 
		    								$('#columnThree input[type="checkbox"]').change(function(event) {
		    									
			    								
		    								      // State has changed to checked/unchecked.
		    								      if($(this).prop('checked')) {
		    								    	  if($.inArray( treeSelectedDisease , chosenDiseases ) == -1) {
		    								    		  chosenDiseases.push(treeSelectedDisease);
		    											}
		    								    	  
		    								    	//add select  class to disease list object
		  	  	    								if($("#columnTwo input[type=checkbox]:checked").length > 0) {
		  	  	    									
		  	  	    									if(!$('[name="selected_'+treeSelectedDisease+'"]').hasClass("selected")) {
		  	  	    										$('[name="selected_'+treeSelectedDisease+'"]').addClass("selected");
		  	  	    										
		  	  	    									}
		  	  	    						        	
		  	  	    						        }
		    								      } else { ///ok now check to see if any other domains or subdomains are checked, if not remove disase from list in necessary
		    								    	  
		    								    	  if(!domainCheckBoxes.is(":checked") && !subDomainCheckBoxes.is(":checked")) {
		    								    		  //remove disease from array if there are no domains and subdomains selected
		    								    		  if($.inArray( treeSelectedDisease,chosenDiseases ) > -1) {
																index = $.inArray(treeSelectedDisease, chosenDiseases ) ;
								
																chosenDiseases.splice(index, 1);
															}
		    								    	  }
		    								      
		    								    	//remove select  class to disease list object
		    											if($("#columnTwo input[type=checkbox]:checked").length <= 0) {
		    												
		    												if($('[name="selected_'+treeSelectedDisease+'"]').hasClass("selected")) {
		    													$('[name="selected_'+treeSelectedDisease+'"]').removeClass("selected");
		    													
		    												}
		    									        	
		    									        }
		    								      
		    								      }
		    								    
		    								});
		    							
		    								
		    							
		    							
		    						}
		    		}); 
   		
   		
		 }
   		
			
		
   		
   		
   	////open classifications 
   		$.ajax("createDataElementAction!updateClassifications.ajax", {
				"type": 	"POST",
				"async": 	false,
				"data": 	{"activeDisease" : treeSelectedDisease},
				"success": 	function(data) {
					
							$('.currentView').next('li').html(data);	
					
					
					
								//let's check to see if the user has made any selection changes for this disease
								
								if(classificationsArray.length > 0) {
									
									$('.classificationItem').each(
											function(){
												
												
												if($(this).attr('disabled') != 'disabled') {
													if($.inArray($(this).val(),classificationsArray) > -1) {
														
														$(this).prop('checked', true);
														$(this).attr('previousValue', 'checked');
													} else {
														$(this).prop('checked', false);
													}
												}
											});
								
								}
								
								
							}
			});
   		
		if(!$('.currentView').next('li').is(":visible")) {
   			$('.currentView').next('li').show("slow",function(){});
		}
		
	}
	
	function updateInterface(){
		var definedOpt = $('#definedOptions input:radio:checked').val();
		var typeOpt = $('#type').val();
		
		if(definedOpt == "true")
		{
			$("#type select option[value*='1'], option[value*='2'], option[value*='3'], option[value*='4'], option[value*='5'], option[value*='6'], option[value*='7']").prop('disabled',true);
			if(typeOpt != "0" && typeOpt != "1")
			{
				$('#type').val("");
			}
			//$("select option[value*='2'], option[value*='3'], option[value*='4'], option[value*='5'], option[value*='6']").hide();
			//$("#type2, #type3, #type4, #type5, #type6").prop("disabled", true);
			//$("#type2, #type3, #type4, #type5, #type6").prop("checked", false);
			$("#measuringUnitFields").show();
			$("#freeFormValuesFields").hide();
			$("#defineValuesFields").show();
		}else{
			//$("#type2, #type3, #type4, #type5, #type6").prop("disabled", false);
			$("#type select  option[value*='1'], option[value*='2'], option[value*='3'], option[value*='4'], option[value*='5'], option[value*='6'], option[value*='7']").prop('disabled',false);
			//$("select option[value*='2'], option[value*='3'], option[value*='4'], option[value*='5'], option[value*='6']").show();
			$("#defineValuesFields").hide();
			$("#freeFormValuesFields").show();
			
			if(typeOpt == "0")
			{
				$("#freeFormNumeric").hide();
				document.getElementById("valueRangeForm.minimumValue").value = "";
				document.getElementById("valueRangeForm.maximumValue").value = "";
				$("#freeFormAlpha").show();
				$("#measuringUnitFields").show();
				//$("#dataRestrictionsHeader").show();
			}else if(typeOpt == "1") {
				$("#freeFormAlpha").hide();
				document.getElementById("sizeId").value = "";
				$("#freeFormNumeric").show();
				$("#measuringUnitFields").show();
				//$("#dataRestrictionsHeader").show();
				
			}else{
				$("#freeFormAlpha").hide();
				$("#freeFormNumeric").hide();
				$("#measuringUnitFields").hide();
				//$("#dataRestrictionsHeader").hide();
				document.getElementById("valueRangeForm.minimumValue").value = "";
				document.getElementById("valueRangeForm.maximumValue").value = "";
				document.getElementById("sizeId").value = "";
			}
		}
	}
		
	function displayTable() {
		$.post(	"valueRangeAction!display.ajax", 
			{ }, 
				function (data) { 
					document.getElementById("valueRangeTable").innerHTML =  data + document.getElementById("valueRangeTable").innerHTML;
				}
		);
	}
	
	function createValueRange() {
		var permissibleValue = $('#permissibleField').val();
		var valueDescription = $('#descriptionField').val();
		var outputCode = $('#outputCodeField').val();
		var isNumeric = $('#type').val() == "1";	
					
		// This is a hack! We don't want any comma in the output code. But struts2 type converter removes 
		// any comma in the integer value before validation. I here replace ',' with another special charater 
		// so that it can fail the struts validator and display the error message.
		if (outputCode.indexOf(',') != -1) {
			outputCode = outputCode.replace(",", ":");
			$('#outputCodeField').val(outputCode);
		}
		
		$.post("permissibleValueAction!create.ajax",
			{ permissibleValue : permissibleValue, 
			  valueDescription : valueDescription, 
			  outputCode : outputCode,
			  numeric : isNumeric},			  
			function(data) {
				$("#valueRangeTable").html(data);
				$('#descriptionField').val('');
				$('#outputCodeField').val('');
				$('#permissibleField').val('').focus();
			}
		);
	}
	
	function removePermissibleValueRange(permissibleValue, permissibleValueDesc, outputCode) {
		
		if(permissibleValue.length>0) {
			$.post(	"valueRangeAction!remove.ajax", 
				{ permissibleValue:permissibleValue }, 
					function (data) { 
						document.getElementById("valueRangeTable").innerHTML = data;
						$('#descriptionField').val(permissibleValueDesc);
						$('#outputCodeField').val(outputCode);
						$('#permissibleField').val(permissibleValue);
					}
			);
		}
	}
	
	//calls clear session to clear the data in session upon cancel
	function cancel() {
		var dataType = '<s:property value="dataType"/>';
		if(dataType=="mapElement") { 
			window.location = "dataStructureElementAction!moveToElements.action";
		} else if(dataType=="dataElement") {
			<s:if test="%{formType == 'create'}">
				window.location = "searchDataElementAction!list.action";
			</s:if>
			<s:else>
				window.location = "dataElementAction!view.action?dataElementName=<s:property value='currentDataElement.name' />";
			</s:else>
		}		
	}
</script>