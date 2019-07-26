<%@include file="/common/taglibs.jsp"%>

<form id="searchForm" method="post" class="validate" name="searchForm">

<%-- <s:hidden name="filterId" value=""/> --%>
<s:hidden id="publicArea" name="publicArea" />
<s:hidden name="searchLocations" value=""/>
<s:hidden id="filterDataElementLocations" name="dataElementLocations" value=""/>
<s:hidden id="filterSearchKey" name="searchKey" value=""/>
<s:hidden id="filterExactMatch" name="exactMatch" value=""/>

<s:hidden id="filterPopulationSelection" name="populationSelection" value=""/>
<s:hidden id="filterSelectedDomains" name="selectedDomains" value=""/>
<s:hidden id="filterSelectedSubdomains" name="selectedSubdomains" value=""/>
<s:hidden id="filterSelectedClassifications" name="selectedClassifications" value=""/>
<s:hidden id="filterSelectedStatuses" name="selectedStatuses" value=""/>
<s:hidden id="filterSelectedElementTypes" name="selectedElementTypes" value=""/>
<s:hidden id="filterSelectedDiseases" name="selectedDiseases" value=""/>

<s:if test="publicArea != true">
	<!-- ownership filter -->
	<div id="dataElementOwnerShipOptions" class="filter facet-form-field">
		<b>Ownership</b>
		<ul id="ownerSelections">
		<li>
				<input id="ownerAll" type="radio" name="ownerId" checked="checked"  value="0" onclick="javascript:refreshSearch();" />
				<label for="ownerAll">All</label>
			</li>
			<li>
				<input id="ownerMine" type="radio" name="ownerId" value="1" onclick="javascript:refreshSearch();" />
				<label for="ownerMine">Mine</label>
			</li>
		</ul>
	</div>
</s:if>
<s:else>
<input id="ownerAll" type="hidden" name="ownerId"  value="0" />
</s:else>
	
	<!-- Status Filter -->
	
		
	<div id="dataElementStatusOptions" class="filter facet-form-field">
			<fieldset>
				<legend><b>Status</b></legend>
		<ul id="statusSelections">
	
			<c:set var="hostname" value="${pageContext.request.serverName}"/>
			
			<s:iterator value="@gov.nih.tbi.commons.model.DataElementStatus@values()"  >
			
			<s:if test="(publicArea == true && name != 'Draft') || publicArea != true">
			
				<!-- Hide Deprecated and Retired status checkboxes for pdbp -->
				<s:if test="publicArea == true && (name == 'Deprecated' || name == 'Retired')">
					<c:if test="${!(fn:contains(hostname, 'pdbp'))}"> 
					<s:if test="%{(name == 'Draft' || name == 'Awaiting Publication' || name == 'Published') && !(isDictionaryAdmin)}"> 
						<li>
						<s:checkbox cssClass="dataElementSelectedFilter" id="status%{id}" name="selectedStatusOptions" fieldValue="%{name}" 
								value="%{name in sessionCriteria.statuses}" onclick="javascript:refreshSearch();" checked="checked" /> 
						<s:label for="status%{id}" value="%{name}" title="%{name}" class="dynamicTitle"></s:label>
						</li>
					</s:if>
					</c:if> 
				</s:if>
				
				<s:else>

					<li>
					<s:checkbox cssClass="dataElementSelectedFilter" id="status%{id}" name="selectedStatusOptions" fieldValue="%{name}" 
							value="%{name in sessionCriteria.statuses}" onclick="javascript:refreshSearch();" checked="checked" /> 
					<s:label for="status%{id}" value="%{name}" title="%{name}" class="dynamicTitle"></s:label>
					</li>
					
					
				
				</s:else>
			</s:if>
			
			</s:iterator>
		
		
		</ul>
		</fieldset>
	</div>
	

	<!--  modified date -->
	<div class="filter facet-form-field">
		<label for="modifiedDateList" style="margin-left:0px;"><b>Modified Date</b></label><br/>
		<ul style="margin-top:5px !important;"><li>
			<s:select cssStyle="width:auto;" id="modifiedDateList" name="modifiedDate" headerKey="" headerValue="------" 
				 onchange="javascript:refreshSearch();"  
				list="#{0:'Today', 7:'Within 7 days', 14:'Within 14 days', 30:'Last Month', 60:'Last 60 Days', 90:'Last 90 Days', 180:'Last 6 Months', 365:'Last Year'}"  />
		</li></ul>	
	</div>
	
	<!-- Type Filter -->
	
		<div class="filter facet-form-field">
				<fieldset>
				<legend><b>Element Type</b></legend>
			<ul id="categoryOptions">
				<s:iterator value="categoryOptions">
					<li> 
						<s:checkbox id="category%{id}" name="selectedElementTypes" fieldValue="%{name}" value="%{name in sessionCriteria.elementTypes}" onclick="javascript:refreshSearch();" />
						<s:label for="category%{id}" value="%{name}"></s:label>
					</li>
				</s:iterator>
			</ul></fieldset>
		</div>
	


	<!-- Disease -->
	<div class="filter facet-form-field">
			<fieldset>
				<legend><b ><a href="#" class="selectedDisease" title="Click the disease link to choose Disease Domains and Sub-Domains">Disease&nbsp;&nbsp;&nbsp;<img src="../images/helpIcon.png" height="16px" width="16px" alt="Help Icon"></a></b>
				</legend>
				<div id="diseaseSelections" class="diseaseSelections" style="max-height:200px;"> 
		<ul >
			<s:iterator value="majorDiseaseOptions">
				<li>
					<s:checkbox id="disease%{id}" name="selectedDiseasesBox" fieldValue="%{name}" value="%{name in sessionCriteria.diseases}"/> 
					<s:label for="disease%{id}" value="%{name}" />
				</li>
			</s:iterator>
			<li>
 			<div style="display: none">	
 			<ul>	
			<s:iterator value="minorDiseaseOptions">
				<li>
					<s:checkbox id="disease%{id}" name="selectedDiseasesBox" fieldValue="%{name}" value="%{name in sessionCriteria.diseases}"/> 
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

	<!-- Population -->
	<div class="filter facet-form-field">
			<fieldset>
				<legend><b>Population</b></legend>
		<ul>
			<s:iterator value="populationOptions">
			<s:if test="publicArea == true && (name == 'Adult' || name == 'Pediatric' || name == 'Adult and Pediatric')">
				<li>
					<s:checkbox id="population%{id}" name="selectedPopulations" fieldValue="%{name}" 
						value="%{name in sessionCriteria.populations}" onclick="javascript:refreshSearch();" checked="checked" /> 
					<s:label for="population%{id}" value="%{name}"></s:label>
				</li>
			</s:if>
			<s:else>
				<li>
					<s:checkbox id="population%{id}" name="selectedPopulations" fieldValue="%{name}" 
						value="%{name in sessionCriteria.populations}" onclick="javascript:refreshSearch();" /> 
					<s:label for="population%{id}" value="%{name}"></s:label>
				</li>
			</s:else>
			</s:iterator>
		</ul>
		</fieldset>
	</div>
<!--  facets -->
<s:iterator value="facets"> 
			<div  class="filter facet-form-field">
				<fieldset>
				<legend><strong><s:property value="key" /></strong></legend>
			<ul>
			<s:iterator value="value" var="facetList">
				<li><s:checkbox id="facet%{key}" name="selectedFacets['%{key}']" fieldValue="%{facetList}" value="false" 
					onclick="javascript:refreshSearch();" /> 
				<s:label for="facet%{key}" value="%{facetList}" />
			</s:iterator>
			</ul></fieldset>
			</div>
		</s:iterator>
	<!-- Buttons 
	<div class="form-field">
		<div class="button margin-right">
			<input type="button" name="submitSearch" id="submitSearch" class="submit" value="Search"
				onclick="javascript:dataElementResetPagination(); dataElementResetSort(); dataElementSearch(); $(this).blur();" />
		</div>
		<input id="formReset" type="reset" value="invisible reset" style="display: none;" />
		<div class="button">
			<input type="button" name="reset" id="reset" class="reset" value="Reset"
				onclick="$('#formReset').click(); dataElementSearch();$(this).blur();" />
		</div>
	</div>-->

</form>

<script type="text/javascript">
//Load a search at the start
$('document').ready(function() {
	
	var titleValue = $('.dynamicTitle');
	titleValue.each(function(){
		//alert('titleValue'+$(this).attr('title'));
		if($(this).attr('title')==='Draft'){
			$(this).attr('title',"Draft - Preliminary version. Not finalized");
			
		}
		if($(this).attr('title')==='Published'){
			$(this).attr('title',"Published - Finalized Data Element ready to be used");
		}
		
		if($(this).attr('title')==='Awaiting Publication'){
			$(this).attr('title',"Awaiting Publication - A request has been made to publish the Data Element");
		}
		if($(this).attr('title')==='Deprecated'){
			$(this).attr('title',"Deprecated - Use is discouraged. Data Element is in the process of being Retired");
		}
		if($(this).attr('title')==='Retired'){
			$(this).attr('title',"Retired - No longer in use for data submission");
		}
		
	});
	
//reset disease domain, subdomain and classifications when unchecked
		$("[name='selectedDiseasesBox']").click(function(e){
			
			if($(this).prop('checked') == false) {
			
				//check for domains firs
				for (var i = 0; i < domainArray.length; i++) {
					
					domainDisease = domainArray[i].split('.')[0];
					if($(this).val() == domainDisease) {
						
						domainArray.splice(i, 1);
						//reset i to -1
						i = -1;
					};
				};
				
				//check for subdomains
				for (var i =0;  i < subDomainArray.length; i++) {
					subDomainDisease = subDomainArray[i].split('.')[0];
					if($(this).val() == subDomainDisease) {
						subDomainArray.splice(i, 1);
						i = -1;
					};
				};
				
				//check for classifications
				for (var i =0;  i < classificationsArray.length; i++) {
					classificationsDisease = classificationsArray[i].split('.')[0];
					if($(this).val() == classificationsDisease) {
						classificationsArray.splice(i, 1);
						i = -1;
					};
				};
				
			};
			
			//run search
			dataElementSearch();
			
		});
		
		$('.more').click(function() {
		    $(this).prev('div').slideToggle();
		    if ($(this).text() == "more"){
		    	$(this).text('less');
		    } else {
		    	$(this).text('more');
		    }
		});
		
		});
</script>