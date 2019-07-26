<%--Generates the GUID table based on the search results. --%>
<%--JSP should be included on other pages --%>
<%@ include file="/common/taglibs.jsp"%>
<%-- <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%> --%>
<c:set var="hostname" value="${pageContext.request.serverName}"/>

		<!-- GUID table container -->
		<div id="guidResultsId" class="idtTableContainer">
			<table id="guidResultsTable" class="table table-striped table-bordered"></table>
			
			<form action='searchGuidAction!downloadPdf.action' method='POST' id='theForm' autocomplete="off">
				<input type="hidden" id="guidType" name="guidType" />
				<input type="hidden" id="hideDuplicate" name="hideDuplicate" />
				<input type="hidden" id="showGuidsEntity" name="showGuidsEntity" />	
				<input type="hidden" id="searchInput" name="searchInput" />	
				<input type="hidden" id="rowCount" name="rowCount" />	
				<input type="hidden" id="searchColumns" name="searchColumns" />	
			</form>
		</div>
		<script type="text/javascript">
		var url = "<s:url value='/guid/searchGuidAction!combinedSearchIdt.ajax' />";
		<s:if test="inAdmin">
			var url = "<s:url value='/guidAdmin/searchGuidAction!combinedSearchIdt.ajax' />";
		</s:if>
		
	 	function getValue(value) {
	        return (value < 10) ? "0" + value : value;
	    };
	    function getDate () {
	        var newDate = new Date();

	        var sMonth = getValue(newDate.getMonth() + 1);
	        var sDay = getValue(newDate.getDate());
	        var sYear = newDate.getFullYear();

	        return sYear + sMonth + sDay;
	    }
	    var now = getDate();
	    
		$("#guidResultsTable").idtTable({
         idtUrl: url,
         idtData: {
             primaryKey: "id"
         },
         pages: 1,
         "processing": true,
         "serverSide": true,
         "deferRender": true,
		 length: 15,
		 autoWidth: false,
         "columns": [
           {
               "data": 'guid',
               "title":'ID',
               "parameter" : 'guidLink',
               "name" : 'guid',
               "width": '20%'

           },
           {
               "data": 'type',
               "title": 'TYPE',
               "parameter" : 'formattedType',
               "name" : 'type',
               "width": '10%'
           },
           {
               "data": 'serverShortName',
               "title": 'ENTITY',
               "parameter" : 'serverShortName',
               "name" : 'serverShortName',
               "width": '10%'
           },
           {
               "data": 'organization',
               "title": 'ORGANIZATION REGISTERED BY',
               "parameter" : 'organization',
               "name" : 'organization',
               "width": '10%'
           },
           {
               "data": 'fullName',
               "title": 'USER REGISTERED BY',
               "parameter" : 'user',
               "name" : 'fullName',
               "width": '10%'
           },
           {
               "data": 'dateCreated',
               "title": 'DATE REGISTERED',
				"parameter" : 'date',
				"name" : 'dateCreated',
				"width": '10%'
			},
			{
               "data": 'linked',
               "title": 'LINKED TO',
				"parameter" : 'linked',
				'name' : 'mapped',
				orderable: false,
				"width": '10%',
				"render": IdtActions.ellipsis(100)
			},
			<c:if test="${fn:contains(hostname, 'pdbp' )}">
			{
               "data": 'cohort',
               "title": 'COHORT',
				"parameter" : 'cohort',
				'name' : 'cohort',
				"width": '10%'
			},
			</c:if>
			{
               "data": 'detailsFlag',
               "title": '',
				"parameter" : 'detailsFlag',
				'name' : 'detailsFlag',
				visible: false
			}
          ],
	dom : 'Bfrtip',
	bFilter: true,
	filters: [
		{
			type: "select",
			name: "All",
			columnIndex: 0,
			options: [
				{
					value: "guids",
					label: "GUIDs"
				},
				{
					value: "PseudoGUIDs",
					label: "PseudoGUIDs"
				},
				{
					value: "convertedPseudoguids",
					label: "Converted PseudoGUIDs"
				},
				{
					value: "unconvertedPseudoguids",
					label: "Unconverted PseudoGUIDs"
				}
			]
		}
		<s:if test="inAdmin">
		,{
			type: "select",
			name: "Show Duplicate Entries",
			columnIndex: 6,
			options: [
				{
					value: "hide",
					label: "Hide Duplicate Entries"
				}
			]
		}
		</s:if>
		<s:else>
		,{
			type: "select",
			name: "Mine Only",
			columnIndex: 0,
			options: [
				{
					value: "showAll",
					label: "Show All"
				}
			]
		}
		</s:else>
		<s:if test="inAdmin">
		,{
			type: "select",
			name: "Show GUIDs From My Entity",
			columnIndex: 0,
			options: [
				{
					value: "showAll",
					label: "Show GUIDs Across Entities"
				}
			]
		},
		</s:if>
	],
	buttons: [
		{
			extend: "collection",
			title: 'Guid_List_' + now,
			buttons: [				
				{
					extend: 'csv',
					text: 'csv',
					className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
					extension: '.csv',
					name: 'csv',
					exportOptions: {
						columns: ':visible',
						orthogonal: 'export'
					},
					enabled: true,
					action: IdtActions.exportAction()
					
				},
				{
                    extend: 'excel',
                    text: 'excel',
                    className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-excel',
                    title: 'export_filename',
                    extension: '.xlsx',
                    exportOptions: {
                    	columns: ':visible',
                    	orthogonal: 'export'
                    },
                    enabled: true,
                    action: IdtActions.exportAction()
					
				},
				{
					extend: 'pdf',
					text: 'pdf',
					className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn buttons-pdf',
					extension: '.pdf',
					name: 'pdf',
					exportOptions: {
						columns: ':visible',
						orthogonal: 'export'
					},
					enabled: true,
					orientation: 'landscape',
					action: function (e, dt, node, config) {
								var theForm = document.forms['theForm'];
								var rowCount = dt.settings()[0]._iRecordsTotal;
								$("#rowCount").val(rowCount);
								$("#guidType").val($("#All option:selected").val());
								$("#hideDuplicate").val($("#Show_Duplicate_Entries option:selected").val());
								$("#showGuidsEntity").val( $("#Show_GUIDs_From_My_Entity option:selected").val());
								$("#searchInput").val($(".idt_searchInput").val());
								var searchColumns = $('.idt_selectColumnRow input:checked').map(function(){
								      return $(this).val();
								    }).toArray();
								
								$("#searchColumns").val(searchColumns.toString());
								theForm.submit();
			        }
				}			
				
			]
		}
	]
 });
</script>