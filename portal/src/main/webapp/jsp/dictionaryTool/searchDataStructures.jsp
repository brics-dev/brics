<%@include file="/common/taglibs.jsp"%>
<input type="hidden" id="inAdmin" name="inAdmin" value="${inAdmin}" />
<input type="hidden" id="hostName" name="hostName" value="${pageContext.request.serverName}" />
<input type="hidden" id="hostStyle" name="hostStyle" value="${modulesConstants.modulesStyleKey}" />

<div class="clear-float" id="advancedSearchDialog">
	<jsp:include page="advancedSearch-lightbox.jsp"></jsp:include>
</div>

<div class="border-wrapper" style="min-height:300px;">
	<s:if test="inAdmin">
		<title>Manage Form Structures</title>
	</s:if>
	<s:else>
		<title>Search Form Structures</title>
	</s:else>
	
			<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
		<s:if test="inAdmin">
		<h1 class="float-left">(Admin) Manage Form Structures</h1>
		</s:if>
		<s:else>
		<h1 id="searchTitle" class="float-left">Search Form Structures</h1>
		</s:else>

<div style="clear:both;"></div>

<form>
	<s:textfield cssStyle="width:500px; height:30px; float:left; margin-left: 14px;" id="searchTerm" name="searchTerm" aria-label="Search Form Structures" />
	<div class="submit-container" style="margin-right:5px;">
		<input id="searchTermSubmit" type="submit" value="submit search button" class="submit submit-mag" />
	</div>
	<div id ="advancedSearchLink" style="float:left; width:150px; padding-left:6px; margin-top:7px; font-size: 14px">
		<a href="javascript:advancedSearch()">Advanced Search</a>
	</div><br/>
	<div style="clear:both;"></div>
	<div style="padding-left:15px; padding-top:5px;"><p>* Keyword search will be performed within the following form fields: Short Name, Title, Description, and Created By.</p></div>
</form>	

<div id="main-content">
	<div style="width:18%; float:left; padding:10px 15px 0 0;">
	
		<s:set var="dataStructureList" value="dataStructureList" />
		<s:set var="statusArray" value="statusArray" />
		<h3 style="margin-left:9px;">Narrow your search</h3>
		<div class="button" style=" margin: 0px 0px 0px 8px;">
			<input id="clearLink" type="button" value="Clear Filters" />
			<input id="resetLink" type="button" value="Restore Default" />
		</div>
		<br/>
		<form id="searchForm" class="searchForm" style="margin: 15px 8px;">

		</form>
	</div>

	<div style="width:79%; float:left;">

		<div id="dataStructureResultsContainer" class="idtTableContainer brics">
			<table id="dataStructureResultsTable" class="table table-striped table-bordered" width="100%">
			</table>
		</div>
	</div>
	<div style="clear:both;"><hr></div>
</div>
</div>
<style>
/**
added media queries for tablet screen size. 
we are just putting temporary this code here until we create separate CSS file for media queries.
**/
input#clearLink {
    display: inline;
    color: white;
    font-size: 10px;
    padding: 3px 5px
}
input#resetLink {
    display: inline;
    color: white;
    font-size: 10px;
    padding: 3px 5px;
}

@media  screen and (min-width : 768px) and (max-width : 1124px) {
	input#clearLink {
		font-size: 7px;
	}
	input#resetLink {
		font-size: 7px;
	}
}
@media  screen and (min-width : 1125px) and (max-width : 1208px) {
	input#clearLink {
		font-size: 8px;
	}
	input#resetLink {
		font-size: 9px;
	}
}
</style>
<script type="text/javascript" src="/portal/js/dataTables/2.0/idtCustomSearchPlugin.js"></script>
<script type="text/javascript">

// This loads the ALL filter to begin and sets results to page 1
	$('document').ready(function() {	

        var hostName = $("#hostName").val();
        var hostStyle = $("#hostStyle").val();
        if (typeof hostName === 'undefined') {
           hostStyle = "brics-style";           
        }

		//set scroll bar styles
		$("#diseaseSelections").mCustomScrollbar({
			theme:"inset-dark",
		    scrollButtons:{ enable: true },
		    autoHideScrollbar: true
		});
		
		//set scroll bar styles
		$("#labelSelections").mCustomScrollbar({
			theme:"inset-dark",
		    scrollButtons:{ enable: true },
		    autoHideScrollbar: true
		});
		
		$("#searchTermSubmit").click(function(e){
			e.preventDefault();
			var searchTerm = $('input[name="searchTerm"]').val().trim();
			var exactMatch = $("#exactMatch").is(':checked');
			var searchParams = {
					searchKey: searchTerm,
					exactMatch: exactMatch
			};
			$('.searchForm').idtCustomSearch('extendData', searchParams);
			
		})
		
		// this is configs for search list to add into idtCustomSearch plugin
		var orgName = '<s:property value="%{orgName}" />'; 
		var newOrgName = orgName.replace(/_/g, ' ');
		var searchListConfigs = [
            {
	              type: 'radio',
	              name: 'ownerId',
	              containerId: 'dataStructureOwnerOptions',
	              legend: 'Ownership',
	              defaultValue: '1',
	              options: [
	                {
	                  value: '1',
	                  id: 'ownerAll',
	                  label: 'All'
	                },
	                {
	                  value: '0',
	                  id: 'ownerMine',
	                  label: 'Mine'
	                }
	              ]
	            },
	            {
	              type: 'radio',
	              name: 'selectedRequiredOptions',
	              containerId: 'dataStructureRequiredOptions',
	              legend: newOrgName,
	              defaultValue: '',
	              options: [
	                {
	                  value: '',
	                  id: 'reqByAll',
	                  label: 'All'
	                },
	                {
	                  value: '<s:property value="orgNameURI" />',
	                  id: 'reqBy<s:property value="value" />',
	                  label: 'Program Specific'
	                }
	              ]
	            },
	            {
	              type: 'checkbox',
	              name: 'selectedFormTypeOptions',
	              containerId: 'formTypeOptions',
	              legend: 'Form Types',
	              options: [
	              <s:iterator value="@gov.nih.tbi.repository.model.SubmissionType@getMainTypes()" var="type">
	                {
	                  value:'<s:property value="%{#type}" />',
	                  id: 'formType<s:property value="%{id}" />',
	                  label: '<s:property value="%{type}" />',
	                  title: '<s:property value="%{type}" />'
	                },
	                </s:iterator>
	              ],
	            },
	            {
	              type: 'checkbox',
	              name: 'selectedStandardizationOptions',
	              containerId: 'dataStructureStandardizationOptions',
	              legend: 'Standardization',
	              dynamicTitle: true,
	              defaultValues: [
					<s:if test="%{(orgName == 'NINR')}">
						'Standard not NINDS'
					</s:if>
					<s:else>
						'Standard NINDS', 'Standard not NINDS'
					</s:else>
	            	  
	            	  ],
	              options: [
	              	<s:iterator value="@gov.nih.tbi.dictionary.model.FormStructureStandardization@getMainStandardizationTypes()">
	                 {
	                    value:'<s:property value="%{name}" />',
	                    id: 'standardization<s:property value="%{name}" />',
	                    label: '<s:property value="%{display}" />',
	                    title: '<s:property value="%{name}" />'
	                  },
	                </s:iterator>
	              ],
	            },
	            {
	              type: 'checkbox',
	              name: 'selectedFormLabelOptions',
	              containerId: 'dataStructureLabelOptions',
	              legend: 'Labels',
	              dynamicTitle: true,
					render: function(containerId, aName, $input, filterData) {
						var form = '', secondList = '',	id, name, value;
						
						<s:iterator value="formLabelOptions" status="stat">
							id ='label<s:property value="%{id}" />';
							value ='<s:property value="%{id}" />';
							name = '<s:property value="%{label}" />';

							<s:if test="#stat.index < 5">
								form += '<li><input type="checkbox" name="'+ aName +'" id="' + id +'" value="'+ value + '"/><label for="'+id+'">' + name + '</label></li>';
							</s:if>
							<s:else>
								secondList += '<li><input type="checkbox" name="'+ aName +'" id="' + id +'" value="'+ value + '"/><label for="'+id+'">' + name + '</label></li>';
							</s:else>
						</s:iterator>
						
						var list = form;
						if (secondList && secondList.length > 0) {
							list += '<div style="display: none;"><ul>' + secondList +  '</ul></div><a href="#labelSelections" class="more" onClick="javascript:showMore(this);" style="float:right; font-size:10px; padding-right:15px;">more</a>';
						}
						
						($input).find('ul').wrap('<div id="labelSelections" class="diseaseSelections" style="max-height:200px;overflow-y:scroll;"></div>').append(list);
						return [];
					},
	            },
	            {
	                type: 'checkbox',
	                name: 'selectedStatusOptions',
	                containerId: 'dataStructureFilterOptions',
	                legend: 'Status',
	                options: [
	                <s:iterator value="@gov.nih.tbi.commons.model.StatusType@values()">
	                  <s:if test="%{type != 'Unknown'}">
	                    <s:if test="%{(type == 'Draft' || type == 'Awaiting Publication' || type == 'Published') && !(isDictionaryAdmin)}">
	                    	<s:if test="%{(type == 'Published')}"> 
		                     {
		                        value: '<s:property value="%{id}" />' ,
		                        id: 'status<s:property value="%{id}" />',
		                        label:'<s:property value="%{type}" />',
		                        title: '<s:property value="%{type}" />',
		                        checked: true
		                      },
		                	</s:if>
		                    <s:else>		                
		                     {
		                        value: '<s:property value="%{id}" />' ,
		                        id: 'status<s:property value="%{id}" />',
		                        label:'<s:property value="%{type}" />',
		                        title: '<s:property value="%{type}" />'
			                   },		               
		                    </s:else>
	                    </s:if>
	                    <s:else>
	                    	<s:if test="%{(type == 'Published')}"> 
		                     {
		                        value: '<s:property value="%{id}" />' ,
		                        id: 'status<s:property value="%{id}" />',
		                        label:'<s:property value="%{type}" />',
		                        title: '<s:property value="%{type}" />',
		                        checked: true
		                      },
	                      </s:if>
			              <s:else>
		                     {
		                        value: '<s:property value="%{id}" />' ,
		                        id: 'status<s:property value="%{id}" />',
		                        label:'<s:property value="%{type}" />',
		                        title: '<s:property value="%{type}" />'
				              },			                      
			              </s:else>
	                    </s:else>
	                 </s:if>
	                </s:iterator>
	                ],
	              },
	              {
	                  type: 'radio',
	                  name: 'selectedCopyRightOptions',
	                  containerId: 'dataStructureCopyrightOptions',
	                  legend: "Form Copyright Status",
	                  defaultValue: 'all',
	                  options: [
	                    {
	                      value: 'all',
	                      id: 'selectAllCR',
	                      label: 'All'
	                    },
	                    {
	                      value: "true",
	                      id: "selectCopyright",
	                      label: 'Copyrighted'
	                    },
	                    {
	                      value: "false",
	                      id: 'selectNonCopyright',
	                      label: 'Non-Copyrighted'
	                    }
	                  ]

	                },
	                {
	                    type: 'checkbox',
	                    name: 'selectedDiseaseOptions',
	                    containerId: 'Diseases',
	                    legend: 'Diseases',
						render: function(containerId, aName, $input, filterData) {
							var form = '',	
								arr = [],
								id,
								name,
								inputName = aName,
								checked = '',
								secondList = '';
							<s:iterator value="majorDiseaseOptions">
								<s:if test="%{(orgName == 'FITBIR' && (id == 8 || id == 1))}">
									arr.push(<s:property value="%{id}"/>);
									checked = 'checked=checked';
									id = <s:property value="%{id}"/>;
									name = "<s:property value='%{name}'/>";
								</s:if>
								<s:elseif test="%{(orgName == 'PDBP' && (id == 5 || id == 1))}">
									arr.push(<s:property value="%{id}"/>);
									checked = 'checked=checked';
									id = <s:property value="%{id}"/>;
									name = "<s:property value='%{name}'/>";
								</s:elseif>
								<s:elseif test="%{(orgName == 'NINR' && (id == 1))}">
									arr.push(<s:property value="%{id}"/>);
									checked = 'checked=checked';
									id = <s:property value="%{id}"/>;
									name = "<s:property value='%{name}'/>";
								</s:elseif>
								<s:else>
									checked = '';
									id = <s:property value="%{id}"/>;
									name = "<s:property value='%{name}'/>";
								</s:else>
								form += '<li><input type="checkbox" ' + checked + '  name="'+ aName +'" id="'+ id +'" value="'+ id + '"/><label for="'+id+'">' + name + '</label></li>';
							</s:iterator>
							form += ''
							<s:iterator value="minorDiseaseOptions">
								checked = '';
								id = <s:property value="%{id}"/>;
								name = "<s:property value='%{name}'/>";
								secondList += '<li><input type="checkbox" ' + checked + '  name="'+ aName +'" id="'+ id +'" value="'+ id + '"/><label for="'+id+'">' + name + '</label></li>';
							</s:iterator>
							var list = form +'<div style="display: none;"><ul>' + secondList +  '</ul></div><a href="#diseaseSelections" aria-label="click here to show more or less of the list of diseases" class="more" onClick="javascript:showMore(this);" style="float: right; font-size: 10px;padding-right: 15px;">more</a>';
							($input).find('ul').wrap('<div id="diseaseSelections" class="diseaseSelections"  style="max-height:200px;overflow-y:scroll;"></div>')
												.append(list);
							return arr;
								
						}
	                  }	
		];

		
	    $('.searchForm').idtCustomSearch({
	    	searchList: searchListConfigs,
	        idtTableId: 'dataStructureResultsTable',
	        toolTipList: [
	     		{
		          title: 'Standard NINDS',
		          description: "Standard forms/measures recommended by the NINDS CDE project"
		        },
		        {
		          title: 'Standard not NINDS',
		          description: "Community used standard forms/measures"
		        },
		        {
		          title: 'Modified',
		          description: "Significant study specific modifications to an original standard"
		        },
		        {
		          title: 'Appendix',
		          description: "Additional study specific questions not found in standard forms/measures"
		        },
		        {
		          title: 'Unique',
		          description: "Study specific nonstandard forms/measures"
		        }
			],
	        idtConfigs: {
	          idtUrl: "listDataStructureAction!searchList.ajax",
	          idtData: {
	        	  primaryKey: 'shortName'
	          },
	          serverSide: true,
	          processing: true,
	          select: false,
              "lengthChange": true,
              "lengthMenu": [10, 25, 50, 100],
              paginationType: "full_numbers",
              "dom": '<"H"ilfr>t<"F"ip>',
              "autoWidth": false,
              "fnHeaderCallback": function( nHead, aData, iStart, iEnd, aiDisplay ) {
                $('#dataStructureResultsTable_info').css({"float": 'right', "padding": '0 10px'});
                
              },
	          columns: [
	              {
	                  name: 'title',
	                  title: 'Title',
	                  parameter: 'title',
	                  data: 'title',
	                  width: '45%'
	              },
	              {
	                  name: 'shortName',
	                  title: 'Short Name',
	                  parameter: 'shortName',
	                  data: 'shortName',
	                  width: '25%'
	              },
	              {
	                  name: 'status',
	                  title: 'Status',
	                  parameter: 'status',
	                  data: 'status',
	                  width: '15%'
	              },	      
	              {
	                  name: 'modifiedDate',
	                  title: 'Modified Date',
	                  parameter: 'date',
	                  data: 'modifiedDate',
	                  width: '15%'
	              }
	          ],
	  	    "aoColumnDefs": [{
	            "aTargets": ["_all"],
	            "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
	            	grantedAccess = true; ///currently granted access is always true, i'm not sure why this is needed.
	            	cellContent = sData.split("|");
	            	newContent = $(nTd).html();
	            	if(cellContent.length > 1) {
	            		if($('#public').val() != '' && $('#public').val() == "true") {
	            			newContent ='<a target="_blank" class="tdLink" href="/portal/publicData/dataStructureAction!view.action?dataStructureName='+cellContent[0]+'&publicArea=true&style.key='+hostStyle+'" title="'+cellContent[2]+'">'+cellContent[1]+'</a>';
	            		} 
	            		else {
		            		if($('#inAdmin').val() == "true") {
		            				newContent ='<a id="viewId'+cellContent[0]+'" class="tdLink tip_trigger" href="/portal/dictionaryAdmin/dataStructureAction!view.action?dataStructureName='+cellContent[0]+'" title="'+cellContent[2]+'">'+cellContent[1]+'</a>';
		            		} 
		            		else {
		            				newContent ='<a id="viewId'+cellContent[0]+'" class="tdLink tip_trigger" href="/portal/dictionary/dataStructureAction!view.action?dataStructureName='+cellContent[0]+'" title="'+cellContent[2]+'">'+cellContent[1]+'</a>';
	            			}
	            		}
	            	} 
	            	else {
	            		$(nTd).html(newContent);
	            	}
	            	
	            	//this is for asthetics give us some room to the text isn't so compact
		           $(nTd).html(newContent);

	            }
		    }],

	        }
	      })	
});
	$("#resetLink").click(function(e){
		  $('.searchForm').idtCustomSearch('reset');
	});
	$("#clearLink").click(function(e){
		$('.searchForm').idtCustomSearch('clear');
	});
	function showMore(el) {
	    $(el).prev('div').slideToggle();
	    if ($(el).text() == "more"){
	    	$(el).text('less');
	    } else {
	    	$(el).text('more');
	    }
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
 		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"listDataStructureLink"});
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataStructuresLink"});
	</s:else>
	
</script>