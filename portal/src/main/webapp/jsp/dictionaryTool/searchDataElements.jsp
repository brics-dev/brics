<%@page import="gov.nih.tbi.commons.model.StatusType"%>
<%@include file="/common/taglibs.jsp"%>

<!-- mapped is used to indicate that the elements returned are MapElements (in the case of searching from a DataStructure)  -->
<input type="hidden" id="mapped" name="mapped" value="${mappedList}" />
<input type="hidden" id="public" name="public" value="${publicArea}" />
<input type="hidden" id="hostName" name="hostName" value="${pageContext.request.serverName}" />
<input type="hidden" id="inAdmin" name="inAdmin" value="${inAdmin}" />
<input type="hidden" id="grantedAccess" name="grantedAccess" value="${accessIdList}" />
<input type="hidden" id="modulesDDTURL" name="modulesDDTURL" value="${modulesDDTURL}" />
<input type="hidden" id="portalRoot" name="portalRoot" value="${portalRoot}" />
<input type="hidden" id="requestedStatusChange" name="requestedStatusChange" value="${isRequestedStatusChange}"/>
<input type="hidden" id="documentationLimit" name="documentationLimit" value= "${documentationLimit}" />
<input type="hidden" id="actionName" name="actionName" value= "${actionName}" />
<input type="hidden" name="currentAction" id="currentAction" value="dataElementAction"/>

<div class="clear-float" id="advancedSearchDialog">
	<jsp:include page="advancedSearch-lightbox.jsp"></jsp:include>
</div>

<style>
	#dataElementResultsTable_wrapper .dt-buttons {
		float: right; 
		padding: 0;

	}
	.resetLink {
		float: left;
	}
	#dataElementResultsTable .idt_selectAllItem:first-of-type {
		display: none
	}
</style>

<div class="clear-float">
  <jsp:include page="dictionaryStatusChange.jsp"></jsp:include>
</div>


<s:set var="mapped" value="mappedList" />

<s:if test="inAdmin">
  <title>Manage Data Elements</title></s:if>
<s:else>
  <title>Search Data Elements</title>
</s:else>

<c:if test="${mapped}">
  <s:set var="repeatableGroup" value="repeatableGroup" />
</c:if>



<div class="border-wrapper">

  <jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
  <c:if test="${!mapped }">
    <s:if test="inAdmin"><h1 class="float-left">(Admin) Manage Data Elements</h1></s:if>
    <s:else><h1 class="float-left">Search Data Elements</h1></s:else>
  </c:if>
  <c:if test="${mapped}">
  <h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Add Data Elements</h1>
      <title>Add Data Elements</title>
  </c:if>
<div style="clear:both;"></div>




<div id="main-content">
<c:if test="${mapped}">
      <s:if test="%{!sessionDataStructure.newStructure && !sessionDataStructure.draftCopy}">
        <div id="breadcrumb">
          <s:if test="inAdmin">
            <s:a action="listDataStructureAction" method="list" namespace="/dictionaryAdmin">Manage Form Structures</s:a>
          </s:if>
          <s:else>
            <s:a action="listDataStructureAction" method="list" namespace="/dictionary">Search Form Structures</s:a>
          </s:else>&gt;
          <s:url action="dataStructureAction" method="view" var="viewTag">
            <s:param name="dataStructureId"><s:property value="currentDataStructure.id" /></s:param>
          </s:url>
          <a href="<s:property value="#viewTag" />"><s:property value="currentDataStructure.title" /></a> &gt; Edit Form Structure
        </div>
      </s:if>

      <ndar:dataStructureChevron action="dataStructureValidationAction" chevron="Attach Elements" />

    </c:if>
<!-- Search Box -->
<form>
  <div style="width:150px; height:50px; float:left;">
    <s:select id="dataElementLocationsSelect" name="dataElementLocationsSelect" multiple="true" list="#{
          'keywords':'Key Words',
          'description':'Definition',
          'permissibleValue':'Permissible Values',
          'title':'Title',
          'labels':'Labels',
          'externalId':'External IDs',
          'shortName':'Variable Name',
          'createdBy':'Created By'}"
        value="%{{'keywords','description','permissibleValue','title','labels','externalId','shortName','createdBy'}}" />
  </div>

  <s:textfield cssStyle="width:450px; height:30px; float:left;" name="searchKey" id="searchKey" />

  <div class="submit-container">
    <input id="searchKeySubmit" type="submit" value="search submit button" class="submit submit-mag" />
  </div>
  <div id ="advancedSearchLink" style="float:left; width:150px; padding-left:6px; margin-top:7px; font-size: 14px">
		<a href="javascript:advancedSearch()">Advanced Search</a>
  </div><br/>

  <c:if test="${mapped}">
  <div class="clear-both"></div>

        <p>Search existing elements to attach to this form structure</p>

      </c:if>
</form>
<div style="clear:both;"></div>

  <div style="width:18%; float:left; padding:10px 15px 0 0;">
    <s:set value="pageSize" var="pageSizeDefault" />
    <h3>Narrow your search</h3>

    <form id="searchForm" name="searchForm">
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
    </form>
  </div>

  <div style="width:79%; float:left; padding-top:10px;">
    
    <div id="dataElementResultsContainer" class="idtTableContainer brics">
      <table id='dataElementResultsTable' class="table table-striped table-bordered" width="100%"></table>
    </div>

    <c:if test="${mapped}">
      <div>
        <div class="form-field">
          <div class="button">
            <input id="addElementsButton" type="button" value="Add Selected Elements" onClick="javascript: addElements(${repeatableGroup.id}) " />
          </div>
          <a class="form-link" href="dataStructureElementAction!moveToElements.action?groupElementId=${repeatableGroup.id}">Cancel</a>
        </div>
      </div>
    </c:if>
    <c:if test="${!mapped && inAdmin}">
      <div>
        <div class="form-field">
          <div class="button">
            <input id="bulkPublish" type="button" value="Publish Selected Elements" onClick="javascript: bulkPublish()" />
          </div>
        </div>
      </div>
      <div>
        <div class="form-field">
          <div class="button">
            <input id="bulkAP" type="button" value="Awaiting Publication" onClick="javascript: bulkAP()" />
          </div>
        </div>
      </div>
    </c:if>
  </div>
  </div>
</div>

<div id="overlay"></div>

<!-- Search box for Diseases -->
<div id="treeFilterContainer">
  <h2 id="filterFocus">Diseases</h2>
  <p>Use the diseases listed below to refine your search for domains and sub-domains</p>
  <hr>
  <!-- Disease List -->
  <div id="columnOne" class="treeColumn"></div>

  <!-- Domains -->
  <div id="columnTwo" class="treeColumn"></div>


  <!-- Sub-Domains -->
  <div id="columnThree" class="treeColumn"></div>
  <div class="clear-both"></div>
  <div class="button" style="margin:10px;">
      &nbsp;<input type="button" value="Apply" class="float-left" onClick="javascript: saveTree();" />
      &nbsp;<input type="button" value="Cancel" class="float-left" onClick="javascript: closeTree(); " />
  </div>
</div>

<script type="text/javascript" src="/portal/js/dataTables/2.0/idtCustomSearchPlugin.js"></script>
<script type="text/javascript" src="/portal/js/statusChange/dictionaryStatusChange.js"></script>
<script type="text/javascript" src="/portal/js/statusChange/bulkStatusChange.js"></script>
<script type="text/javascript" src="/portal/js/common-source-files/jquery.qtip.js"></script>
<script type="text/javascript">
	var treeSelectedDisease = "";
	var domainArray = new Array();
	var subDomainArray = new Array();
	var classificationsArray = new Array();
	var dataElementsMappingArray = new Array();
	
	<c:if test="${mapped}">
	<c:forEach var="de" items="${sessionDataStructure.repeatableGroup.mapElements}" varStatus="theStatus">
	  dataElementsMappingArray.push('<c:out value="${de.structuralDataElement.name}" />');
	</c:forEach>
	</c:if>

console.log(dataElementsMappingArray);
  // Load a search at the start
  $('document').ready(function() {
	  
	// Grab search variables from form elements
	//var filterValue = $(".dataElementSelectedFilter")[0].id;
	var hostName = $("#hostName").val();
	var hostStyle = "brics-style";
	
	if(hostName.indexOf('pdbp') > -1 ){
		hostStyle = "pdbp-style";		
	}else if(hostName.indexOf('fitbir') > -1 ) {
		hostStyle = "fitbir-style";
	}else if(hostName.indexOf('eyegene') > -1 || hostName.indexOf('nei') > -1) {
		hostStyle = "eyegene-style";
	}else if(hostName.indexOf('cnrm') > -1 ) {
		hostStyle = "cnrm-style";
	}else if(hostName.indexOf('gsdr') > -1 ) {
		hostStyle = "gsdr-style";
	}else if(hostName.indexOf('ninds') > -1 ) {
		hostStyle = "ninds-style";
	}else if(hostName.indexOf('cistar') > -1 ) {
		hostStyle = "cistar-style";
	}else if(hostName.indexOf('cdrns') > -1 ) {
		hostStyle = "cdrns-style";
	}else if(hostName.indexOf('nia') > -1 ) {
		hostStyle = "nia-style";
	}else if(hostName.indexOf('grdr') > -1 ) {
		hostStyle = "grdr-style";
	}
		
    // this is configs for search list to add into idtCustomSearch plugin
    var searchListConfigs = [
		<s:if test="publicArea != true">
	        {
		         type: 'radio',
		         name: 'ownerId',
		         containerId: 'dataElementOwnerShipOptions',
		         legend: 'Ownership',
		         defaultValue: '0',
		         options: [
		           {
		             value: '0',
		             id: 'ownerAll',
		             label: 'All'
		           },
		           {
		             value: '1',
		             id: 'ownerMine',
		             label: 'Mine'
		           }
		         ]
	        },
		</s:if>
	        {
		         type: 'select',
		         name: 'modifiedDate',
		         containerId: 'modifiedDateList',
		         legend: 'Modified Date',
		         defaultValue: '',
		         options: [
		           {
		             value: '',
		             label: '------'
		           },
		           {
		             value: '0',
		             label: 'Today'
			       },
		           {
		             value: '7',
		             label: 'Within 7 days'
		           },	
		           {
		             value: '14',
		             label: 'Within 14 days'
			        },
		           {
		             value: '30',
		             label: 'Last Month'
			        },
		           {
		             value: '60',
		             label: 'Last 60 Days'
			        },
		           {
		             value: '90',
		             label: 'Last 90 Days'
			        },
		           {
		             value: '120',
		             label: 'Last 6 Months'
			        },
		           {
		             value: '365',
		             label: 'Last Year'
			        },			        
		         ]
	        },	        
			{
                type: 'checkbox',
                name: 'selectedStatuses',
                containerId: 'dataElementStatusOptions',
                legend: 'Status',
                dynamicTitle: true,
                options: [
	                <c:set var="hostname" value="${pageContext.request.serverName}"/>
	                <s:iterator value="@gov.nih.tbi.commons.model.DataElementStatus@values()"  >
	                <s:if test="(publicArea == true && name != 'Draft') || publicArea != true">
	                  <!-- Hide Deprecated and Retired status checkboxes for pdbp -->
			          <s:if test="publicArea == true && (name == 'Deprecated' || name == 'Retired')">
			              <c:if test="${!(fn:contains(hostname, 'pdbp'))}">
			                 <s:if test="%{(name == 'Draft' || name == 'Awaiting Publication' || name == 'Published') && !(isDictionaryAdmin)}">
			                           {
			                              value: "<s:property value='%{name}' />" ,
			                              id: "status<s:property value='%{id}' />",
			                              label:"<s:property value='%{name}' />",
			                              title: "<s:property value='%{name}' />",
			                              checked: true
			                            },
			                 </s:if>
			                 <s:else>
			                    {
			                       value: "<s:property value='%{name}' />" ,
			                       id: "status<s:property value='%{id}' />",
			                       label:"<s:property value='%{name}' />",
			                       title: "<s:property value='%{name}' />",
			                    },
			                 </s:else>
			             </c:if>
			        </s:if>
	                <s:else>
	                  <s:if test="%{(name == 'Draft' || name == 'Awaiting Publication' || name == 'Published') && !(isDictionaryAdmin)}">
	                     {
	                        value: "<s:property value='%{name}' />" ,
	                        id: "status<s:property value='%{id}' />",
	                        label:"<s:property value='%{name}' />",
	                        title: "<s:property value='%{name}' />",
	                        checked: true
	                      },
	                  </s:if>
	               	  <s:else>
	                      {
	                        value: "<s:property value='%{name}' />" ,
	                        id: "status<s:property value='%{id}' />",
	                        label:"<s:property value='%{name}' />",
	                        title: "<s:property value='%{name}' />",
	                      },
	                  </s:else>
	               </s:else>
	           </s:if>
	           </s:iterator>
	           ],
	           eventCallback: function(filterData) {
	        	   refreshPublicationBtns();
	           }
			},
            {
              type: 'checkbox',
              name: 'selectedElementTypes',
              containerId: 'selectedElementOptions',
              legend: 'Element Type',
              options: [
              	<s:iterator value="categoryOptions">
                  {
                     value: "<s:property value='%{name}' />" ,
                     id: "category<s:property value='%{id}' />",
                     label:"<s:property value='%{name}' />"
                  },
                </s:iterator>
              ]
           },
            {
              type: 'checkbox',
              name: 'selectedDiseases',
              containerId: 'selectedDiseasesOptions',
              legend: '<b ><a href="#" id="selectedDiseases" onClick="javascript:test();" title="Click the disease link to choose Disease Domains and Sub-Domains">Disease&nbsp;&nbsp;&nbsp;<img src="../images/helpIcon.png" height="16px" width="16px" alt="Help Icon"></a></b>',
	          render: function(containerId, aName, $input, filterData) {
		            var form = '',
		              arr = [],
		              id,
		              name,
		              inputName = aName,
		              secondList = '';
		            <s:iterator value="majorDiseaseOptions">
		              id = "disease<s:property value='%{id}'/>";
		              name = "<s:property value='%{name}'/>";
		              form += '<li><input type="checkbox"  name="'+ aName +'" id="'+ id +'" value="'+ name + '"/><label for="'+id+'">' + name + '</label></li>';
		            </s:iterator>
		            <s:iterator value="minorDiseaseOptions">
		              id = "disease<s:property value='%{id}'/>";
		              name = "<s:property value='%{name}'/>";
		              secondList += '<li><input type="checkbox"  name="'+ aName +'" id="'+ id +'" value="'+ name + '"/><label for="'+id+'">' + name + '</label></li>';
		            </s:iterator>
		            var list = form +'<div style="display: none;"><ul>' + secondList +  '</ul></div><a href="#diseaseSelections" class="more" onClick="javascript:showMore(this);" style="float: right; font-size: 10px;padding-right: 15px;">more</a>';
		            ($input).find('ul').wrap('<div id="diseaseSelections" class="diseaseSelections" style="max-height:200px;overflow:scroll;"></div>')
		                      .append(list);
		            return arr;
	
	          },
	          eventCallback: function(filterData) {
	
	            if($(this).prop('checked') == false) {
	              console.log('this', this);
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
	            var domainSelection = "";
	            var subDomainSelection = "";
	            var classificationSelection = "";
	
	            if( save ) {
		            //
		            domainSelection = domainArray;
		            subDomainSelection = subDomainArray.join(';');
		            classificationSelection = classificationsArray;
	
	            }
	
	            var obj = {
	                selectedDomains: domainSelection.toString(),
	                selectedSubdomains: subDomainSelection.toString(),
	                selectedClassifications: classificationSelection.toString()
	            };
	
	            $.extend(filterData, obj);
	          }
			},
            {
              type: 'checkbox',
              name: 'populationSelection',
              containerId: 'selectedPopulationOptions',
              legend: 'Population',
              options: [
            	 <s:iterator value="populationOptions">
                  {
                     value: "<s:property value='%{name}' />" ,
                     id: "population<s:property value='%{id}' />",
                     label:"<s:property value='%{name}' />"
                  },
                </s:iterator>
              ]
	        },			
	  ];
      $('#searchForm').idtCustomSearch({
        searchList: searchListConfigs,
          idtTableId: 'dataElementResultsTable',
	      toolTipList: [
	     		{
		          title: 'Draft',
		          description: "Draft - Preliminary version. Not finalized"
		        },
		        {
		          title: 'Published',
		          description: "Published - Finalized Data Element ready to be used"
		        },
		        {
		          title: 'Awaiting Publication',
		          description: "Awaiting Publication - A request has been made to publish the Data Element"
		        },
		        {
		          title: 'Deprecated',
		          description: "Deprecated - Use is discouraged. Data Element is in the process of being Retired"
		        },
		        {
		          title: 'Retired',
		          description: "Retired - No longer in use for data submission"
		        }
		  ],
          idtConfigs: {
            idtUrl: "searchDataElementAction!searchList.ajax",
            idtData: {
              primaryKey: 'name'
            },
          serverSide: true,
          processing: true,
          <c:if test="${!publicArea && (mapped || inAdmin)}">
          select: "multi",
          </c:if>
          "lengthChange": true,
          "lengthMenu": [10, 25, 50, 100],
          paginationType: "full_numbers",
          "dom": 'B<"H"lfr>t<"F"ip>',
          "autoWidth": false,
            columns: [
			          {
				          name: 'title',
				          title: 'Title',
				          data: 'title',
				          parameter: 'title',
				          "render": function(data, type, row, full) {

				        	  var oTable = $("#dataElementResultsTable").idtApi('getTableApi');
				        	  var disabledList = oTable.settings()[0].oInit.selectionDisabled;
				        	  if($('#mapped').val() || $('#inAdmin').val()) {
				        		  if($.inArray(row.DT_RowId,dataElementsMappingArray) > -1) {
					  					if (disabledList.indexOf(row.DT_RowId) === -1) {
											disabledList.push(row.DT_RowId);
											$("#"+ row.DT_RowId).addClass("idtSelectionDisabled");
											//data.selectionDisable = true;
										}
				        		  }
				        	  }
				        	  grantedAccess = true; ///currently granted access is always true, i'm not sure why this is needed.
				            	
				            	namespace = 'dictionary';
				            	if($('#inAdmin').val() == "true") { 
				            		namespace = 'dictionaryAdmin';
				            	} else {
				            		namespace = 'dictionary';
				            	}
				    			
				            	var newContent = "";

			            		if($('#public').val() != '' && $('#public').val() == "true") {
				            		newContent = '<a class="tdLink" target="_blank" href="/'+$('#portalRoot').val()+'/publicData/dataElementAction!view.action?dataElementName='+row.shortName+'&publicArea=true&style.key='+hostStyle+'">'+data+'</a>';
				            		
			            		} else {
			            			
			            			if(grantedAccess) {
			            			
			            				if(!$('#mapped').val()) {
			            					newContent ='<a class="tdLink" href="/'+$('#portalRoot').val()+'/'+namespace+'/dataElementAction!view.action?dataElementName='+row.shortName+'">'+data+'</a><i class="fa fa-info-circle tdLink" title="Quick View" onclick="quickView('+row.shortName+')" style="padding-left: 1px; font-size: 16px; color: rgb(61, 138, 221) ! important;cursor: pointer;"></i>';
			            				} else { 
			            					newContent ='<a class="lightbox tdLink" target="_blank" href="/'+$('#portalRoot').val()+'/'+namespace+'/dataElementAction!viewDetails.ajax?dataElementName='+row.shortName+'">'+data+'</a><i class="fa fa-info-circle tdLink" title="Quick View" onclick="quickView('+row.shortName+')" style="padding-left: 1px; font-size: 16px; color: rgb(61, 138, 221) ! important;cursor: pointer;"></i>';
			            				}
			            				
			            			} else {
			            				newContent = data;
			            			}
			            			
			            		}

					            return newContent;
				          }

			          },
				      {
				          name: 'shortName',
				          title: 'Variable Name',
				          parameter: 'shortName',
				          data: 'shortName'
				      },
			          {
				          name: 'category',
				          title: 'Type',
				          parameter: 'category',
				          data: 'category'
			          },
			          {
				          name: 'modifiedDate',
				          title: 'Modified Date',
				          parameter: 'date',
				          data: 'modifiedDate'
			          },
				      {
				          name: 'status',
				          title: 'Status',
				          parameter: 'status',
				          data: 'status'
				      }
           		],
           		"aoColumnDefs": [
           			 { "sWidth": !($('#mapped').val() || $('#inAdmin').val() === 'true') ? "45%" : "40px", "aTargets": [0] },
		             { "sWidth": !($('#mapped').val() || $('#inAdmin').val() === 'true') ? "20%" : "45%" , "aTargets": [1] },
		             { "sWidth": !($('#mapped').val() || $('#inAdmin').val() === 'true') ? "8%"  : "12%" , "aTargets": [2] }         			
           		],
           		drawCallback: function( oSettings ) {
	   	    	  	$('.downloadResultsBtn span').html('Download All ' + oSettings._iRecordsTotal + ' Results<i class="fa fa-caret-down" aria-hidden="true" style="font-size: 15px;position: relative;top: 2px;padding-left: 4px;"></i>');
	   	    		var test = '<div id=downloadLinks class=btn-downarrow-options style=top:28px;><p>Data Element Results:&nbsp;&nbsp;<a href=javascript:exportDataElements("XML")>XML</a>&nbsp;<a href=javascript:exportDataElements("CSV")>ZIP</a><br />REDCap Format:&nbsp;&nbsp;<a href=javascript:exportDataElements("REDCap")>CSV</a></p></div>';
				  
	   	 			$("#dataElementResultsTable_wrapper .dt-buttons").append(test);
	   	 			$('.lightbox').fancybox();
		        },
		        initComplete: function(){
		            iconTooltip();
		        },
           		buttons: [
           			{
           				text: "Reset All",
           				className: "resetLink",
           				action: function(e, dt, node, config) {
           					
           			        $('[name="dataElementLocations"]').attr('checked', true);
           			        $('#searchKey').val("");
           			        $('#exactMatch').attr('checked', false);
 
           					var searchTerm = $('input[name="searchKey"]').val().trim();
           					var exactMatch = $("#exactMatch").is(':checked');
           					var searchLocations = $('[name="dataElementLocations"]:checked').map(function() {
           					    return this.value;
           					}).get();
           					
           		            var options = $('#dataElementResultsTable').idtApi('getOptions');
           				    var obj = {
           					        selectedDomains: "",
           					        selectedSubdomains: "",
           					     	selectedClassifications: "",
           					     	dataElementLocations: searchLocations.toString(),
           					     	exactMatch: exactMatch,
           					     	searchKey: searchTerm
           					    };
           					  //submit form values
           					$.extend(options.filterData, obj);
           					closeTree();
           					$('#searchForm').idtCustomSearch('reset');
           					refreshPublicationBtns();

           				}
           			},
           			{
           				text: 'Download all Results<i class="fa fa-caret-down" aria-hidden="true" style="font-size: 15px;position: relative;top: 2px;padding-left: 4px;"></i>',
           				className: "downloadResultsBtn",
           				action: function(e, dt, node, config) {
           			
           					$("#downloadLinks").toggle( "blind",300 );
           				}
           			}           			
           			
           		]
          

          }
   	                  	
        
     });

    //set scroll bar styles
    $(".diseaseSelections").mCustomScrollbar({
      theme:"inset-dark",
        scrollButtons:{ enable: true },
        autoHideScrollbar: true
    });

    $("#searchKeySubmit").click(function(e){
      e.preventDefault();
		var searchTerm = $('input[name="searchKey"]').val().trim();
		var exactMatch = $("#exactMatch").is(':checked');
		var searchLocations = $('[name="dataElementLocations"]:checked').map(function() {
		    return this.value;
		}).get();		
		var searchParams = {
				searchKey: searchTerm,
				exactMatch: exactMatch,
				dataElementLocations:searchLocations.toString()
		};
		$('#searchForm').idtCustomSearch('extendData', searchParams);

    })

    //create select locations in search
    $("#dataElementLocationsSelect").multiselect({
          buttonText:function(options, select) {
              if (options.length == 0) {
                  return  'Search Locations <b class="caret"></b>';
                }
                else {
                  return  'Search Locations <b class="caret"></b>';
                }
             },
             checkboxName: 'dataElementLocations'
    });

/*     var filterParam = getURLParameter("filter");

    if (filterParam != "undefined") {
      $('input:radio[name=elementTypeSelection]')[0].checked = true;
      dataElementSetFilter(filterParam);
    } else {
      dataElementSearch();
    } */

    refreshPublicationBtns();

 });


  function wsTesting() {
    //TESTTEST
    $.ajax("searchDataElementAction!wsTest.action", {
      "type":   "POST",
      "async":  false,
      "data":   {"searchKey" : $("#searchKey").val()},
      "success":  function(data) {

              $("#columnThree").html(data);

            }
    });
  }




  function showMore(el) {
      $(el).prev('div').slideToggle();
      if ($(el).text() == "more"){
        $(el).text('less');
      } else {
        $(el).text('more');
      }
  }

  //function for export links
  function submitTheFormStructure(formAction) {
    //set a few variables to submit with the form
    var category = $("#categoryOptions input:checked").val()
    $("#category").val(category);



    var searchLocations = dataElementGetSearchLocations();
    $("#searchLocations").val(searchLocations);

    var theForm = document.forms['searchForm'];
    theForm.action = formAction;
    theForm.submit();

  }
  
  
  
	 var test = function() {
	     //el.preventDefault();
		
		//scroll to the top of the page
		 $("html, body").animate({ scrollTop: 0 }, "fast");
		
		 var diseaseSelect = $('input[name="selectedDiseases"]:checked').map(function() {
		 			return this.value;
		 }).get();
	
		$("#overlay").show(300, function() {
			 $("#treeFilterContainer").show(300,function(){
	              //populate disease list
	              $.ajax("searchDataElementAction!updateDisease.ajax", {
	                "type":   "POST",
	                "async":  false,
	                "data":   {"selectedDiseases" : diseaseSelect.toString()},
	                "success":  function(data) {
	                        $("#columnOne").html(data);
	                        //$('#content-wrapper').focus();
	                        $('#columnOne li.selected').each(function () {
		                          name = $(this).attr('name');
		                          nameArray = name.split("_");
		                          treeSelectedDisease = nameArray[1].toString();
		                          console.log('treeSelectedDisease', treeSelectedDisease);
								  $(this).addClass("currentView");
		
		                           //open classifications for first disease
		                          // Ajax call to add classifications
		                          $.ajax("searchDataElementAction!updateClassifications.ajax", {
		                            "type":   "POST",
		                            "async":  false,
		                            "data":   {"activeDisease" : treeSelectedDisease},
		                            "success":  function(data) {
											 $('.currentView').next('li').html(data);
		                             }
		                          });
		                          $('.currentView').next('li').show("slow",function(){});
		
		                          return false;
	                     	});
	                   }
	              });
	
	              //populate domain list
	              $.ajax("searchDataElementAction!updateDomain.ajax", {
	                "type":   "POST",
	                "async":  false,
	                "data":   {"selectedDiseases" : treeSelectedDisease},
	                "success":  function(data) {
			                  $("#columnTwo").html(data);
			                  //check columns from previous selection
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
		                    		$('#columnTwo [name="selectedAllDomains"]').prop("checked",true);
		                  	}
			          }
	              });
	
	              //populate sub-domain list
	              $.ajax("searchDataElementAction!updateSubDomain.ajax", {
	                "type":   "POST",
	                "async":  false,
	                "data":   {"selectedDiseases" : diseaseSelect.toString(),
	                       "selectedDomains" : (domainArray.length > 0) ? domainArray.toString() : "all"},
	                "success":  function(data) {
	
	                        $("#columnThree").html(data);
	                    //check columns from previous selection
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
	                          	$('#columnThree [name="allSubDomains"]').prop("checked",true);
	                        }
	                  }
	              });
	
	           });
	
	        });
		
	  };
      
	  //close tree
	  function closeTree(){
	    var overlayContainer = $("#overlay");
	    var treeContainer = $("#treeFilterContainer");
	    treeContainer.hide(300, function(){overlayContainer.hide(300);});
	    //empty tree session arrays
	    treeSelectedDisease = "";
	    domainArray = new Array();
	    subDomainArray = new Array();
	    classificationsArray = new Array();

	  }
	  

	  //save tree
	  function saveTree(){

	    var term = "";
	    //update disease checkboxes based on selections

	    $('.diseaseSelections li.selected').each(function(){
	          //get disease from selected li
	            term = $(this).attr('name').split("_")[1];
	          //loop through disease checkboxes and compare values to determine if it should be checked.
	          $('[name="selectedDiseases"]').each(function(){
	            console.log('val',$(this).val());
	            if($(this).val() == term) {

	              $(this).prop("checked",true);
	            }
	          });
	    })

	    var overlayContainer = $("#overlay");
	    var treeContainer = $("#treeFilterContainer");
	    treeContainer.hide(300, function(){overlayContainer.hide(300);});
	    save = true;
	    var domainSelection = "";
	    var subDomainSelection = "";
	    var classificationSelection = "";
	    if( save ) {
	    //
	    domainSelection = domainArray;
	    subDomainSelection = subDomainArray.join(';');
	    classificationSelection = classificationsArray;

	    }
	    var disease = $('input[name="selectedDiseases"]:checked').map(function() {
	        return this.value;
	    }).get();

	    var obj = {
	        selectedDiseases: disease.toString(),
	        selectedDomains: domainSelection.toString(),
	        selectedSubdomains: subDomainSelection.toString(),
	        selectedClassifications: classificationSelection.toString()
	    };
	  //submit form values
	    $('#searchForm').idtCustomSearch('extendData', obj);
	    //empty tree session arrays
	    treeSelectedDisease = "";


	  }
	  
	  function quickView(de){
		  	$.fancybox.showActivity();
			var actionUrl ="dataElementAction!viewDetails.ajax";
			
			$.ajax({
				type: "GET",
				url:actionUrl,
				cache: false,
				data: {dataElementName:de.id},
				success:function(data) {
					$.fancybox(data);
					$.fancybox.hideActivity();
				}
			});
	 }
	  
	function advancedSearch(){

			//Declare dialogs	
			var dialog = $("#advancedSearchInner").dialog({
				
				autoOpen :false,
				modal : true,
				height : 440,
				width : 990,
				draggable : false,
				resizable : false,
				title : "Advanced Search Capability"
			});
			
			dialog.dialog("open");
	}
	  
  <s:if test="!inAdmin">
    setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"searchDataElementLink"});
  </s:if>
  <s:else>
    setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataElementsLink"});
  </s:else>

  function iconTooltip() {
		$('i[title]').each(function() {
			$(this).qtip({	
				style: { classes: 'ui-tooltip-green ui-tooltip-shadow' },
				position: {
			        my: 'left top',  // Position my top left...
			        at: 'bottom right', // at the bottom right of...
			        target: $(this) // my target
			    }
			});
		});
	}

</script>
