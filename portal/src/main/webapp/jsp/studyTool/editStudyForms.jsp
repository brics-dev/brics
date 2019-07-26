<%@include file="/common/taglibs.jsp"%>
<style>
#formStructureTableTable .idt_selectAllSelector {
	display: none;
}
</style>
<p>Add Form Structures you initially intend to use to the table below by clicking on the "Add Form Structure" button.
When you click the button, you will be presented with a list of published Form Structures to choose from.
If you don't know the Form Structures you intend to use or if the forms haven't been created yet, leave this section blank.
You can add your Form Structures to the study at a later time or when you submit data to your study.  When submittind data, 
the system will automatically add the Form Structure used to the study's Form Structure list.</p>

<div id="formListTable" class="idtTableContainer" style="padding-left:50px; padding-top:10px; width:900px">
  	<div id="dialog"></div>
	<table id="formListTableTable" class="table table-striped table-bordered" width="100%"></table>
</div>
<div class="form-field inline-right-button" style="padding-bottom:20px;">
	<div class="button" style="padding-right:250px">
		<input type="button" value="Add Form Structure" onClick="javascript:openAddFsDialog()" />
	</div>
</div>
<div id="formSelectionDialog" class="dialogWithLoading">
	<div id="formStructureTable" class="idtTableContainer">
	  	<div id="dialog"></div>
		<table id="formStructureTableTable" class="table table-striped table-bordered" width="100%"></table>
	</div>
</div>

<script type="text/javascript">
var studyId = "${sessionStudy.study.prefixedId}";
var studyForms = "${sessionStudy.study.prefixedId}";
$('#formStructureTableTable').idtTable({
	idtUrl: "<s:url value='/study/studyAction!getAllFormStructures.action' />",
	pageLength: 15,
	processing: true,
	idtData: {
		primaryKey: 'shortNameAndVersion'
	}, 
	select: 'multi',
	initComplete : function(settings) {
		var api = new $.fn.DataTable.Api( settings );
		var data = api.rows().data();
		var selected = settings.oInit.selected;
		$('thead .idt_selectAllHeader').remove();
		for (var i = 0; i < data.length; i++) {
			if(data[i].isLinked == "true") {
				api.row("#"+ data[i].DT_RowId).select(); 
				selected.push(data[i].DT_RowId);									
			}
		}
		api.on("select", function(e, dt, type, index) {
			
			var row;
			if(index.length>1){
				for (var i=0; i<index.length; i++){
					row = api.row(index[i]);
					row.data().isLinked = "true";
					addFsToStudy(row.data());
					api.rows().invalidate();
				}
			}
			else{
				row = api.row(index);
				row.data().isLinked = "true";
				addFsToStudy(row.data());
				api.rows().invalidate();
			}
			

		})
		.on("deselect", function(e, dt, type, index) {
			var row = api.row(index);
			row.data().isLinked = "false";
			removeFsFromStudy(row.data());
			api.rows().invalidate();
		});

	},
	"columns": [
             {
                 "data": 'title',
                 "title":'TITLE',
                 "name":'TITLE',
                 "parameter" : 'title',
                 "width": "35%",
                 "searchable": true,
                 "orderable": true

             },
             {
                 "data": 'isLinked',
                 "title": '',
                 "name": 'ISLINKED',
                 "parameter" : 'isLinked',
                 "visible": false


             },
              {
                 "data": 'submissionType',
                 "title": '',
                 "name": 'TYPE',
                 "parameter" : 'submissionType',
                 "visible": false

             },
             {
                 "data": 'submissionTypeId',
                 "title": '',
                 "name": 'ID',
                 "parameter" : 'formType.id',
           		 "visible": false
             }, 
             {
                 "data": 'shortName',
                 "title": 'SHORT NAME',
                 "name": 'SHORT NAME',
                 "parameter" : 'shortName',
                 "searchable": true,
                 "orderable": true,
             },
             {
                 "data": 'version',
                 "title": 'VERSION',
                 "name": 'VERSION',
                 "parameter" : 'version',
                 "searchable": true,
                 "orderable": true,
             },
              {
                 "data": 'description',
                 "title": 'DESCRIPTION',
                 "name": 'DESCRIPTION',
                 "parameter" : 'description',
                 "width": "35%",
                 "searchable": true,
                 "orderable": true,
                 "render": IdtActions.ellipsis(90)
             }, 
             
          ]
          
});	

function loadFormStructuresForStudy() {
	$.ajax({
		url: "studyFormAction!getFsForStudy.ajax",
		type: "GET",
		dataType: "json",
		success : function(data) {
			try {
				data = JSON.parse(data);
				if (data.status == "success") {
					// adds all rows in the array.  NOT one at a time
					IDT.addRow($("#formsListContainer table"), data.aaData);
					for (var i = 0; i < data.aaData.length; i++) {
						var singleRow = data.aaData[i];
						var saveVal = $(singleRow[0]).text();
					}
				}
				else {
					alert("There was a problem listing the currently-added form structures for this study");
				}
			}
			catch(e) {
				alert("The form could not be added because the respones from the server was not correct");
				console.error(e);
			}
		}
	});
}

$(document).ready(function() {
	$("#formListTableTable").idtTable({
		idtUrl: "<s:url value='/study/studyAction!getFormList.action' />",
		idtData: {
			primaryKey: 'shortNameAndVersion'
		}, 
		"columns": [
			{
	            "data": 'title',
	            "title":'FORM STRUCTURE TITLE',
	            "name":'FORM STRUCTURE TITLE',
	            "parameter" : 'title',
	            "searchable": true,
	            "orderable": true,				
			},
	        {
	            "data": 'shortName',
	            "title": 'SHORT NAME',
	            "name": 'SHORT NAME',
	            "parameter" : 'shortName',
	            "searchable": true,
	            "orderable": true,
	        },
	        {
	            "data": 'version',
	            "title": '',
	            "name": 'VERSION',
	            "parameter" : 'version',
				"visible": false
	        },
	        {
	            "data": 'submissionType',
	            "title": 'FORM TYPE',
	            "name": 'FORM TYPE',
	            "parameter" : 'submissionType',
	            "searchable": true,
	            "orderable": true,

	        },
	        {
	            "data": 'remove',
	            "title": 'ACTIONS',
	            "name": 'remove',
	            "parameter" : '',
	            "searchable": true,
	            "orderable": true,
	            "render": function(data, type, row, full) {
	            	return "<a href=\"javascript:;\" class=\"removeFsLink tdLink\" shortName=\""+ row.shortName + "\" version=\"" +row.version+ "\" onclick=\"handleClickRemoveFsFromStudy(this)\">Remove</a>"
	            }
	        }			
		]
	});
	$("#formSelectionDialog").height(window.innerHeight - 25).width(window.innerWidth * 0.85);
	$("#formSelectionDialog").dialog({
		title: "Select Form Structures for Study",
		autoOpen: false,
		height: window.innerHeight - 25,
		width: window.innerWidth * 0.85,
		position: { my: "center", at: "center", of: window },
		buttons : [
			{
				text: "Finished",
				click : function() {
					$(this).dialog("close");
				}
			}
		],
		close: function() {
			
		}
	});
	
	
});


function openAddFsDialog() {

	$("#formSelectionDialog").dialog("open");
	

	 $('#formStructureTableTable').idtApi("getTableApi").order( [[0, 'asc'], [2, 'desc']] ).draw(); 

}



function handleClickRemoveFsFromStudy(e) {	
	//setting global variables fo IdtApi use
	var formStructureTableApi = $('#formStructureTableTable').idtApi("getTableApi");
	var formListTableApi = $("#formListTableTable").idtApi("getTableApi");
	var formStructureSelectedOptions = $('#formStructureTableTable').idtApi("getSelected");
 	var row =  $(e).closest('tr');
 	var row_object = formListTableApi.row(row).data();
 	var row_index = $.inArray(row_object.DT_RowId, formStructureSelectedOptions);
 	formStructureTableApi.row("#"+ row_object.DT_RowId).deselect();
	formStructureSelectedOptions.splice(row_index, 1);
}

function addFsToStudy(row) {
	var formListTableApi = $("#formListTableTable").DataTable();
	$.ajax({
		url: "studyFormAction!addFs.ajax",
		type: "POST",
		dataType: "json",
		//traditional: true,
		data : {
			"studyFormEntry.DT_RowId": row.DT_RowId,
			"studyFormEntry.shortName": row.shortName,
			"studyFormEntry.version": row.version,
			"studyFormEntry.submissionTypeId": Number(row.submissionTypeId),
			"studyFormEntry.title": row.title
		},
		success : function(data) {
			try {
				data = JSON.parse(data);
				if (data.status == "success") {
					/* IDT.addRow($("#formsListContainer table"), data.row);  */
					var rows = [];
					rows.push(data.row);
					formListTableApi.rows.add(rows).draw();;
				}
				else {
					alert("The form could not be added because of missing data");
				}
			}
			catch(e) {
				alert("The form could not be added because the respones from the server was not correct");
			}
		}
	});
	return true;
}

function removeFsFromStudy(row) {
	var formListTableApi = $("#formListTableTable").idtApi("getTableApi");
	$.ajax({
		url: "studyFormAction!removeFs.ajax",
		type: "POST",
		data : {
			"studyFormEntry.shortName": row.shortName,
			"studyFormEntry.version": row.version
		},
		success : function(data) {
			var jsonResponse = JSON.parse(data);
			if (jsonResponse.status == "success") {
				/* IDT.removeRow($("#formsListContainer table"), saveVal); */
				var rowId = row.DT_RowId;
				formListTableApi.row("#"+ rowId).remove().draw();
			}
			else {
				if (jsonResponse.reason == "400") {
					alert("The page encountered a problem that caused it to be unable to remove the Form Structure");
				}
				else if (jsonResponse.reason == "404") {
					alert("That Form Structure has already been removed");
				}
			}
		}
	});
}

</script>