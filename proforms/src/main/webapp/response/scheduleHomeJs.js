/**
 * 
 */

	var now = getDate();
	
	var defValProtoSelect = $("#currProtoId").val();
	var selectedProtocolId = "", selectedClinicalLocId = "", selectedPatId ="";
	$(document).ready(function(){
		setDefaultStartEndDate();
		
		/*Made protocol select list to be autocomplet dropdown */
		$("#protocolSearch").combobox({
			select: function(event, obj){
//				console.log("protocolSearch select: " + obj.item.value);
				selectedProtocolId= obj.item.value;
				loadClinicalLocListByProto(selectedProtocolId);
				loadSubjectListByProto(selectedProtocolId);
			}
		});

		$('#protocolSearch').next().find('input').attr("id", "protocolSearchInput");
		var protoPlaceHolder = "Select or Search a Protocol...";
		$("#protocolSearchInput").attr("placeholder", protoPlaceHolder);
		$("#protocolSearchInput").val("");
		
		/*Made clinical location select list to be autocomplet dropdown */
		$("#clinicalLocSearch").combobox({
			select: function(event, obj){
//				console.log("clinicalLocSearch select: " + obj.item.value);
				selectedClinicalLocId = obj.item.value;
			}
		});
		$('#clinicalLocSearch').next().find('input').attr("id", "clinicalLocSearchInput"); 
		var locPlaceHolder = "Select or Search a Clinical Location...";
		$("#clinicalLocSearchInput").attr("placeholder", locPlaceHolder);
		$("#clinicalLocSearchInput").val("");
		
		/*Made subject select list to be autocomplet dropdown */
		$("#patientSearch").combobox({
			select: function(event, obj){
//				console.log("patientSearch select: " + obj.item.value);
				selectedPatId = obj.item.value;
			}
		});
		$('#patientSearch').next().find('input').attr("id", "patientSearchInput");
		var patPlaceHolder = "Select or Search a Patient's Name...";
		$("#patientSearchInput").attr("placeholder", patPlaceHolder);
		$("#patientSearchInput").val("");

		$("#scheduleReportTable").idtTable({
			idtUrl: basePath + "/response/scheduleReportHomeAction!getScheduleReportDTList.action",
			idtData: {
				primaryKey: 'id'
			},
			dom: 'Bfrtip',
			columns: [
	            {
	                name: 'protocolNumber',
	                title: 'Protocol Number',
	                parameter: 'protocolNumber',
	                data: 'protocolNumber'
	            },
	        	{
	                name: 'patientId',
	                title: 'Patient ID<br/>(MRN / GUID / SubjectId) ',
	                parameter: 'patientId',
	                data: 'patientId'
	            },
	        	{
	                name: 'patientName',
	                title: 'Patient Name',
	                parameter: 'patientName',
	                data: 'patientName'
	            },
	            {
	                name: 'visitTypeName',
	                title: 'Visit Type',
	                parameter: 'visitTypeName',
	                data: 'visitTypeName'
	            },
	            {
	                name: 'visitDate',
	                title: 'Visit Date / Time',
	                parameter: 'visitDate',
	                data: 'visitDate'
	            },
	            {
	                name: 'procedure',
	                title: 'Procedure',
	                parameter: 'procedure',
	                data: 'procedure'
	            },
	            {
	                name: 'clinicalLocationInfo',
	                title: 'Clinical Location',
	                parameter: 'clinicalLocationInfo',
	                data: 'clinicalLocationInfo'
	            },
	            {
	                name: 'pointOfContactInfo',
	                title: 'Point Of Contact',
	                parameter: 'pointOfContactInfo',
	                data: 'pointOfContactInfo'
	            },
	            {
	                name: 'comments',
	                title: 'Comments / Notes',
	                parameter: 'comments',
	                data: 'comments'
	            }
	        ],
	    	buttons: [
	    		{
	    			extend: "collection",
	    			title: 'Schedule_Report_' + now,
	    			buttons: [
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
	   					action: IdtActions.exportAction(),
	   		            customize: IdtActions.pdfCustomizer()						
		   			 }	
	    		  ]
	    	   }
	    	]
		});
		$("#scheduleReportTableDiv").hide();
		
		$("input#protocolSearchInput").change(function() {
			var $protoSelect = $(this);
			var selectedProtocolId = $.trim($protoSelect.val());
			if(selectedProtocolId.length == 0){
				loadClinicalLocListByProto(selectedProtocolId);
				loadSubjectListByProto(selectedProtocolId);
			}
		});
	}); //end document.ready()
	
	function setDefaultStartEndDate() {
		var currentDate = new Date();
		var defStartDate = new Date();
		defStartDate.setDate(currentDate.getDate() - 7);
		var $startDatepicker = $('#scheduleStartDate');
		$startDatepicker.datepicker();
		$startDatepicker.datepicker('setDate', defStartDate);
		
		var $endDatepicker = $('#scheduleEndDate');
		$endDatepicker.datepicker();
		$endDatepicker.datepicker('setDate', currentDate);
	}
	//function to set the selected value as default
//	function setDefault(id, defValue) {
//	  $('#' + id +' option').each(function () {
//	    if ($(this).val() == defValue) {
//	        $(this).prop('selected', 'selected');
//	        $('#'+ id ).next().find('input').val($(this).text());
//	        //console.log("trigger change");
//	        //$(this).trigger("change");
//	    }
//	  });
//	}
	function loadClinicalLocListByProto(protocolId) {
		var postData = new FormData();
		postData.append("selectedProtocolId", protocolId);
		$.ajax({
			type : "POST",
			url : basePath + "/response/scheduleReportFilterAction!getClinicLocListByProto.action",
			data : postData,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				//reload select dropdown
//				console.log("loadClinicalLocListByProto() data: "+JSON.stringify(data));
				var clinicalArr = $.parseJSON(data);
			    if(typeof clinicalArr==='object') {
//			    	console.log("loadClinicalLocListByProto() clinicalArr: "+JSON.stringify(clinicalArr));
			        var $clinialLocSelect = $('#clinicalLocSearch');
			        $clinialLocSelect.find('option').remove();
			        $.each(clinicalArr, function(i, item) {//console.log("loadClinicalLocListByProto() iteme: "+JSON.stringify(item));
			        	$('<option>').val(item.id).text(item.name).appendTo($clinialLocSelect);
			        });
			    }
			}
		});
	}
	function loadSubjectListByProto(protocolId){
			var postData = new FormData();
			postData.append("selectedProtocolId", protocolId);
			$.ajax({
				type : "POST",
				url : basePath + "/response/scheduleReportFilterAction!getPatientListByProto.action",
				data : postData,
				cache : false,
				processData : false,
				contentType : false,
				success : function(data) {
					//reload select dropdown
//					console.log("loadSubjectListByProto() data: "+JSON.stringify(data));
					var patArr = $.parseJSON(data);
				    if(typeof patArr==='object') {
//				    	console.log("loadSubjectListByProto() patArr: "+JSON.stringify(patArr));
				        var $patSelect = $('#patientSearch');
				        $patSelect.find('option').remove();
				        $.each(patArr, function(i, item) {
				        	$('<option>').val(item.id).text(item.name).appendTo($patSelect);
				        });
				    }
				}
			});
	}

	function generateScheduleReport() {
		var scheduleStartDateStr = "", scheduleEndDateStr = "";

		scheduleStartDateStr = $("#scheduleStartDate").val();
		scheduleEndDateStr = $("#scheduleEndDate").val();
		if($.trim($("#protocolSearchInput").val()).length == 0){
			selectedProtocolId = "";
		}
		if($.trim($("#clinicalLocSearchInput").val()).length == 0){
			selectedClinicalLocId = "";
		}
		if($.trim($("#patientSearchInput").val()).length == 0){
			selectedPatId = "";
		}
		
//		console.log("protocolSearchInput: "+$("#protocolSearchInput").val());
//		console.log("clinicalLocSearchInput: "+$("#clinicalLocSearchInput").val());
//		console.log("patientSearchInput: "+$("#patientSearchInput").val());
		
		$("#selectedProtocolId").val(selectedProtocolId);
		$("#selectedClinicalLocId").val(selectedClinicalLocId);
		$("#selectedSubjectId").val(selectedPatId);
		$("#scheduleStartDateStr").val(scheduleStartDateStr);
		$("#scheduleEndDateStr").val(scheduleEndDateStr);

//		console.log("selectedProtocolId: "+selectedProtocolId);
//		console.log("selectedClinicalLocId: "+selectedClinicalLocId);
//		console.log("selectedSubjectId: "+selectedPatId);
//		console.log("startDate: "+scheduleStartDateStr);
//		console.log("endDate: "+scheduleEndDateStr);
		
		$table = $("#scheduleReportTable");
		var postData = new FormData();
		postData.append("selectedProtocolId", selectedProtocolId);
		postData.append("selectedClinicalLocId", selectedClinicalLocId);
		postData.append("selectedSubjectId", selectedPatId);
		postData.append("scheduleStartDateStr", scheduleStartDateStr);
		postData.append("scheduleEndDateStr", scheduleEndDateStr);
		$.ajax({
			type : "POST",
			url : basePath + "/response/scheduleReportHomeAction!generateScheduleReport.action",
			data : postData,
			cache : false,
			processData : false,
			contentType : false,
			success : function(data) {
				$table.idtApi('getTableApi').rows().deselect();
				$table.idtApi('getTableApi').ajax.reload();
				$("#scheduleReportTableDiv").show();
			},
			error : function(xhr, ajaxOptions, thrownError){
				console.log("generate schedule report error: "+thrownError);
				$.ibisMessaging("close", {type:"primary"}); 
				$.ibisMessaging("primary", "error", "Error occurred while generating the schedule report. Please contact the system administrator.");
			}
		});		
	}
	function destroyDataTable() {
		$table = $('#scheduleReportTable').idtApi('getTableApi');
		$table.clear();
		$table.destroy();
	}
