$(document).ready(function(){
	//Not loading data table when no eforms has been selected
	var winUrl = $(location).attr('href'); 
	if (winUrl.indexOf("action=formParams") <= 0 ) {
		var url = basePath + "/response/btrisQuestionDTListAction!getMappedBtrisQuestionDTList.action";
		$("#btrisQuestionsTable").idtTable({
					idtUrl : url,
					idtData : {
						primaryKey : "sectionQuestionId"
					},
					length : 15,
					dom : 'frtip',
					select : "multi",
					columns : [
						{
							data : "sectionQuestionId",
							title : "SectionQuestionId",
							name: "sectionQuestionId",
							parameter : "sectionQuestionId",
							visible: false
						}, 
						{
							data : "sectionName",
							title : "Section NAME",
							name: "sectionName",
							parameter : "sectionName",
							className: 'dt-body-left'
						}, 
						{
							data : "questionName",
							title : "Question NAME",
							name: "questionName",
							parameter : "questionName",
							className: 'dt-body-left'
						}											
					],
					columnDefs: [
						{ targets: 0, className: 'dt-body-left', width: "3%"}
					],
					initComplete: function() {
						var oTable = $("#btrisQuestionsTable").idtApi("getTableApi");
						oTable.rows().select();
					}
		});
	}
}); // $(document).ready()

	function getDataFromBtris(sectionQuestionId) {
		var url = basePath + "/response/dataCollection.action?action=getBtrisData&sectionQuestionId=" + sectionQuestionId;
		$.ajax({
				type: "POST",
				url: url,
				datatype: "json",
				success: function(response) {
				var responseJson = response;//jQuery.parseJSON(response);
				var qType = responseJson.questionType; 
				var btrisValue = $.trim(responseJson.btrisValue); 
				var qText = responseJson.questionText;
				if(btrisValue.length == 0){
					var msg = "BTRIS data is empty for Question \""+ qText + "\"";
					showNoDataMsg(qText, msg);
				}
				if(qType == "Textbox" || qType == "Textarea" || qType == "Textblock") {
					$("#"+sectionQuestionId).val(btrisValue);
				} else {
					var noMatchedData = false;
					$("input[name="+sectionQuestionId+"]").each(function(index, element){
						if($(this).next().text() == btrisValue) {
							$(this).prop("checked", true);
							noMatchedData = true;
						}
					});
					if(!noMatchedData) {
						var msg = "BTRIS data does not match any options for Question \""+ qText + "\"";
						showNoDataMsg(qText, msg);
					}
				}		
				},
				error: function(xhr, status, error) {
					console.log('Error: ' + error);
					$.ibisMessaging("primary", "error", 'Fail to get BTRIS data');
				}
			});
	}
	

	function openMappedBtrisQuestionsDlg (){
		var btrisQuestionDlg = $("#btrisMappedQuestionsDialog").dialog({
			autoOpen : false,
			height : 420,
			width : 900,
			position : {
				my : "center",
				at : "center",
				of : window
			},
			buttons : [ {
				id : "getBtrisData",
				text : "Get BTRIS Data",
				click : function() {
					var oTable = $("#btrisQuestionsTable").idtApi("getTableApi");
					var selectedRows = oTable.rows('.selected').data().to$();
					var selectedSQIds = "";
					for (var i = 0; i < selectedRows.length; i++) {
						if(i == selectedRows.length - 1) {
							selectedSQIds += selectedRows[i].sectionQuestionId;
						} else {
							selectedSQIds += selectedRows[i].sectionQuestionId +",";
						}						
					}
					getBtrisDataForSelectedQ(selectedSQIds);
					$(this).dialog("close");
					
				}
			}, {
				id : "closeBtrisMappedQDlg",
				text : "Close",
				click : function() {
					$(this).dialog("close");
				}
			} ],

			close : function() {
				$(this).dialog('destroy');
			}
		});
		btrisQuestionDlg.dialog("open");
	}
	
	function getBtrisDataForSelectedQ(selectedSQIds) {

		var url = basePath + "/response/getBtrisDataAction!getBtrisDataForSelectedQ.action?sectionQuestionIds="+selectedSQIds;
		$.ajax({
				type: "POST",
				url: url,
				success: function(response) {//console.log("getBtrisDataForSelectedQ.response: "+JSON.stringify(response));
					var responseJsonArr = $.parseJSON(response);
					var noDataMsg = "";
					for(var i = 0; i < responseJsonArr.length; i ++) {
						var responseJson = responseJsonArr[i];
						var sectionQuestionId = responseJson.sectionQuestionId;
						var qType = responseJson.questionType; 
						var btrisValue = $.trim(responseJson.btrisValue);
						var sectionName = responseJson.sectionName;
						var qText = responseJson.questionText;
						if(btrisValue.length == 0){
							noDataMsg += "<tr><td style='width:20%;font-weight:normal;'>" + sectionName +"</td><td style='font-weight:normal;'>" + qText + "</td></tr>";
							
						}
						if(qType == "Textbox" || qType == "Textarea" || qType == "Textblock") {
							$("#"+sectionQuestionId).val(btrisValue);
						} else {
							var noMatchedData = false;
							$("input[name="+sectionQuestionId+"]").each(function(index, element){
								if($(this).next().text() == btrisValue) {
									$(this).prop("checked", true);
									noMatchedData = true;
								}
							});
							if(!noMatchedData) {
								noDataMsg += "<tr><td style='width:20%;font-weight:normal;'>" + sectionName +"</td><td style='font-weight:normal;'>" + qText + "</td></tr>";
							}
						}
					}
					
					if(noDataMsg.length > 0) {
						noDataMsg = "<p style='text-align:left;'>Answer(s) for following question(s) are empty in BTRIS or do not have matching options in BTRIS.</p><br/>"
									+"<table style='text-align:left;'><tr><th style='width:20%;'>Section</th><th>Question</th></tr>" + noDataMsg + "</table>";
						var title = "No Data";
						showRtnMsg(noDataMsg, title);
					}
		
				},
				error: function(xhr, status, error) {
					console.log('Error in getting BTRIS data due to database connection error.');
					var errMsg = "Fail to get BTRIS data due to database connection error.";
					var title = "Error";
					showRtnMsg(errMsg, title);
				}
			});
	}
	
	function showRtnMsg(msg, title){
		
		var dlgId = $.ibisMessaging("dialog", "info", 
			msg	, {
			modal : true,
			buttons : [
				{
					text : "Close",
					click : function() {
						$.ibisMessaging("close", {id: dlgId});					
					}
				}
			],
			width: "400px",
			title: title
		}); //end dialog
	}
