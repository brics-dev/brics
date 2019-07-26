<%@include file="/common/taglibs.jsp"%>
<input type="hidden" id="inAdmin" name="inAdmin" value="${inAdmin}" />
<input type="hidden" id="public" name="public" value="${publicArea}" />
<input type="hidden" id="hostName" name="hostName" value="${pageContext.request.serverName}" />
<c:set var="hostname" value="${pageContext.request.serverName}"/>
<style>

.dataTables_scrollHead,.dataTables_scrollHeadInner,.dataTables_scrollBody {
width:100% !important;
}

.dataTables_scrollHeadInner table {
width:100% !important;
}


#resultTable {
width:100% !important;
}
</style>
<form>
	<s:textfield cssStyle="width:300px; height:30px; float:left;" name="searchTerm" id="searchTerm" aria-label="Search Form Structures" />
	<div class="submit-container">
		<input id="searchTermSubmit" type="submit" value="submit search button" class="submit submit-mag" />
	</div>
	<div style="float:left; width:150px; padding-left:8px; margin-top:10px;">
		<s:checkbox id="exactMatch" name="exactMatch" cssStyle="float:left;" /> 
		<s:label for="exactMatch" cssStyle="width:150px; font-weight:normal; font:12px/1.2 Arial;  float:none; display:block;" value="Whole Word or Phrase" />
	</div>
</form>	

<div style="clear:both;"></div>
<div style="width:20%; float:left;">

	<s:set var="dataStructureList" value="dataStructureList" />
	<s:set var="statusArray" value="statusArray" />
	<h3>Narrow your search</h3>
	<div class="button" style=" margin: 0px 0px 0px 8px;">
		<input id="clearLink" type="button" value="Clear Filters" style="display: inline; color: white; font-size: 10px; padding: 3px 5px;"/>
		<input id="resetLink" type="button" value="Restore Default" style="display: inline; color: white;  font-size: 10px; padding: 3px 5px;" />
	</div>
	<br/>
	<form id="searchForm" class="publicSearchForm" style="margin: 15px 8px;">
		
		
		<input id="ownerAllTrue" type="hidden" name="owner"  value="1" />
			<input id="ownerAllFalse" type="hidden" name="owner"  value="0" />
			
			
	<div id="dataStructureRequiredOptions" class="filter facet-form-field">
					<fieldset>
				<legend><strong><s:property value="%{orgName}" /></strong></legend>
					<ul>
					<li><input type="radio" name="selectedRequiredOptions" id="reqByAll" checked="checked"  value="" onclick="javascript:refreshSearch();" />
							<label for="reqByAll">All</label>
					</li>
					<li>
							<input type="radio" name="selectedRequiredOptions" id="reqBy<s:property value="value" />" value="<s:property value="orgNameURI" />" onclick="javascript:refreshSearch();" />
							<label for="reqBy<s:property value="value" />">Program Specific</label>
					</li>					
					
<%-- 					<s:iterator value="orgNameURIMap">
						<s:if test="%{key != '-1'}"> 

 						<li>
							<input type="radio" name="selectedRequiredOptions" id="reqBy<s:property value="value" />"  value="<s:property value="key" />" onclick="javascript:refreshSearch();" />
							<label for="reqBy<s:property value="value" />">Program Specific</label>
						</li>
 						</s:if>
					</s:iterator>
 --%>					
					</ul></fieldset>
			</div>
		<div  class="filter facet-form-field">
		<fieldset>
				<legend><strong>Form Types</strong></legend>
		<ul>
				
		<s:iterator value="@gov.nih.tbi.repository.model.SubmissionType@getMainTypes()" var="type">
		<li>
			<s:checkbox id="formType%{id}" name="selectedFormTypeOptions" fieldValue="%{#type}" value="%{type in sessionCriteria.formTypes}" 
				onclick="javascript:refreshSearch();" />
			<s:label for="formType%{id}" value="%{type}" />
			</li>
		</s:iterator>
		</ul></fieldset>
		</div>
		
		
		<div id="dataStructureStandardizationOptions" class="filter facet-form-field">
				<fieldset>
				<legend><strong>Standardization</strong></legend><br>
				<ul>
				<s:iterator value="@gov.nih.tbi.dictionary.model.FormStructureStandardization@getMainStandardizationTypesPublicSite()">
				<c:choose>
					<c:when test="${fn:contains(hostname, 'cdrns' )}">
						<s:if test="%{(name == 'Standard not NINDS' )}">
							<li><s:checkbox id="standardization%{name}" checked="checked" name="selectedStandardizationOptions" fieldValue="%{name}"  value="%{name in sessionCriteria.standardizations}" 
								onclick="javascript:refreshSearch();" />
							<s:label for="standardization%{name}" value="%{display}" title="%{name}" class="dynamicTitle" />
							</li>
						</s:if>
						<s:else>
							<li><s:checkbox id="standardization%{name}" name="selectedStandardizationOptions" fieldValue="%{name}"  value="%{name in sessionCriteria.standardizations}" 
								onclick="javascript:refreshSearch();" />
							<s:label for="standardization%{name}" value="%{display}" title="%{name}" class="dynamicTitle" />
							</li>
						</s:else>
					</c:when>
					<c:otherwise>
						<s:if test="%{(name == 'Standard NINDS' || name == 'Standard not NINDS' )}">
							<li><s:checkbox id="standardization%{name}" checked="checked" name="selectedStandardizationOptions" fieldValue="%{name}"  value="%{name in sessionCriteria.standardizations}" 
								onclick="javascript:refreshSearch();" />
							<s:label for="standardization%{name}" value="%{display}" title="%{name}" class="dynamicTitle" />
							</li>
						</s:if>
						<s:else>
							<li><s:checkbox id="standardization%{name}" name="selectedStandardizationOptions" fieldValue="%{name}"  value="%{name in sessionCriteria.standardizations}" 
								onclick="javascript:refreshSearch();" />
							<s:label for="standardization%{name}" value="%{display}" title="%{name}" class="dynamicTitle" />
							</li>
						</s:else>
					</c:otherwise>
				</c:choose>
				</s:iterator>   
				</ul></fieldset>
			</div>
		<div id="dataStructureFilterOptions" class="filter facet-form-field">
			<fieldset>
				<legend><strong>Status</strong></legend><br>
			<ul>
			<s:iterator value="@gov.nih.tbi.commons.model.StatusType@values()">
				<s:if test="%{type == 'Published' || type == 'Shared Draft' || type == 'Awaiting Publication'}">
					<li><s:checkbox id="status%{id}" name="selectedStatusOptions" checked="checked" fieldValue="%{id}" value="%{id in sessionCriteria.statuses}" 
						onclick="javascript:refreshSearch();" />
						<s:if test="%{type == 'Awaiting Publication'}">
							<s:label for="status%{id}" value="%{type}" title="This form is subject to minor changes based on user validation prior to publication."></s:label>
						</s:if>
						<s:else>
							<s:label for="status%{id}" value="%{type}" />
						</s:else>
					</li>
				</s:if>
			</s:iterator>   
			</ul></fieldset>
		</div>
		
		<div id="dataStructureCopyrightOptions" class="filter facet-form-field">
			<fieldset>
				<legend><strong>Form Copyright Status</strong></legend>
			<ul>
				<li>
					<input type="radio" name="selectedCopyRightOptions" id="selectAllCR" checked="checked"  value="all" onclick="javascript:refreshSearch();" />
					<label for="selectAllCR">All</label>
				</li>
				<li>
					<input type="radio" name="selectedCopyRightOptions" id="selectCopyright" value="true" onclick="javascript:refreshSearch();" />
					<label for="selectCopyright">Copyrighted</label>
				</li>
				<li>
					<input type="radio" name="selectedCopyRightOptions" id="selectNonCopyright" value="false" onclick="javascript:refreshSearch();" />
					<label for="selectNonCopyright">Non-Copyrighted</label>
				</li>					
			</ul>
			</fieldset>
		</div>
		
		<div  class="filter facet-form-field">
		<fieldset>
				<legend><strong>Diseases</strong></legend>
		<div id="diseaseSelections" class="diseaseSelections" style="max-height:200px;"> 
		<ul>
		<s:iterator value="majorDiseaseOptions">
			<li>
			<c:choose>
			<c:when test="${fn:contains(hostname, 'cdrns' )}">
				<s:if test="%{name == 'General (For all diseases)'}">
					<s:checkbox id="disease%{id}" name="selectedDiseaseOptions" checked="checked" fieldValue="%{id}" value="%{id in sessionCriteria.diseases}" 
						onclick="javascript:refreshSearch();" /> 
					<s:label for="disease%{id}" value="%{name}" />
				</s:if>
				<s:else>
					<s:checkbox id="disease%{id}" name="selectedDiseaseOptions" fieldValue="%{id}" value="%{id in sessionCriteria.diseases}" 
						onclick="javascript:refreshSearch();" /> 
					<s:label for="disease%{id}" value="%{name}" />
				</s:else>
			</c:when>
			<c:otherwise>
				<s:if test="%{name == 'General (For all diseases)' || name == 'Traumatic Brain Injury'}">
					<s:checkbox id="disease%{id}" name="selectedDiseaseOptions" checked="checked" fieldValue="%{id}" value="%{id in sessionCriteria.diseases}" 
						onclick="javascript:refreshSearch();" /> 
					<s:label for="disease%{id}" value="%{name}" />
				</s:if>
				<s:else>
					<s:checkbox id="disease%{id}" name="selectedDiseaseOptions" fieldValue="%{id}" value="%{id in sessionCriteria.diseases}" 
						onclick="javascript:refreshSearch();" /> 
					<s:label for="disease%{id}" value="%{name}" />
				</s:else>
			</c:otherwise>
			</c:choose>
			</li>
		</s:iterator>			
		<li>
			<div style="display: none">	
		 	<ul>	
		<s:iterator value="minorDiseaseOptions">
			<li>
				<s:checkbox id="disease%{id}" name="selectedDiseaseOptions" fieldValue="%{id}" value="%{id in sessionCriteria.diseases}" 
					onclick="javascript:refreshSearch();" /> 
				<s:label for="disease%{id}" value="%{name}" />
			</li>
		</s:iterator>
		</ul>
		 			</div> 
		
		<a href="#diseaseSelections" class="more" style="float: right; font-size: 10px;">more</a>
		</li>
		</ul>
		</div>
		</fieldset>
		</div>
	</form>
</div>

<div style="width:79%; float:left;">
	<div id="dataStructureResultsId" class="brics" style="margin-bottom:50px;">

		<table id="resultTable" cellspacing="0" cellpadding="0" width="100%">
 				<thead>
 					<tr class="tableRowHeader">
 						<th>Title</th>
 						<th>Short Name</th>
 						<%-- <th width="250">Disease</th>--%>
 						<th>Status</th>
 						<th>Modified Date</th>
 					</tr>
			</thead>
			<tbody></tbody>
		</table>
		
	</div>
</div>

<div style="clear:both;"><hr></div>



<script type="text/javascript" src="/portal/js/search/dataStructureSearch.js"></script>
<script type="text/javascript">

    
	// This loads the ALL filter to begin and sets results to page 1
	$('document').ready(function() {
		showStandardizationTooltips();

		//set scroll bar styles
		$(".diseaseSelections").mCustomScrollbar({
			theme:"inset-dark",
		    scrollButtons:{ enable: true },
		    autoHideScrollbar: true
		});
		
		$("#searchTermSubmit").click(function(e){
			e.preventDefault();
			dataStructureSearch();
			
		})
		
		$("#resetLink").click(function(e){
		    $('#searchTerm').val("");
		    $('#exactMatch').attr('checked', false);
		    
			document.forms['searchForm'].reset();
			dataStructureSearch();
		});
		
		var filterParam = getURLParameter("filter");  //?
		if (filterParam != "undefined") {
			refreshSearch();
		} else {
			
			dataStructureSearch();
		
		}
		
		$("#clearLink").click(function(e){
			$("label").each(function() {
				if($(this).text() == "All"){
					$(this).prev().prop('checked', true);
				}				
			});
			$("input[name='selectedFormTypeOptions']").each(function() {
				if($(this).is(':checked')){
					$(this).prop('checked', false);
				}
			});
			$("#dataStructureStandardizationOptions").find("input").each(function() {
				if($(this).is(':checked')){
					$(this).prop('checked', false);
				}
			});
			$("#dataStructureFilterOptions").find("input").each(function() {
				if($(this).is(':checked')){
					$(this).prop('checked', false);
				}
			});
			$(".diseaseSelections").find("input").each(function() {
				if($(this).is(':checked')){
					$(this).prop('checked', false);
				}
			});
			dataStructureSearch();
		});
	});
	
	<s:if test="!inAdmin">					
 		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"listDataStructureLink"});
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataStructuresLink"});
	</s:else>
	
	$('.more').click(function() {
	    $(this).prev('div').slideToggle();
	    if ($(this).text() == "more"){
	    	$(this).text('less');
	    } else {
	    	$(this).text('more');
	    }
	});
	
</script>