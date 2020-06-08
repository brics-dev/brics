<%@include file="/common/taglibs.jsp"%>
<title>Edit Study: ${sessionStudy.study.title}</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1>Data Repository</h1>
	<div style="clear:both"></div>
	
	<!--begin #center-content -->
	<div id="main-content">
		<div id="breadcrumb">
			<a href="study/studyAction!list.action">View Studies</a> &nbsp;&gt;&nbsp;<a
				href="viewStudyAction!view.action?studyId=${sessionStudy.study.prefixedId}">${sessionStudy.study.title}</a>
			&nbsp;&gt;&nbsp;Manage Datasets
		</div>

		<s:form id="theForm" cssClass="validate" method="post" name="studyDatasetForm" validate="true" enctype="multipart/form-data">
			<s:token />
			<ndar:editStudyChevron action="studyAction" chevron="Manage Datasets" />

			<h2>Edit Study: ${sessionStudy.study.title}</h2>
			<h3>Manage Datasets</h3>
			<p>The table below displays dataset information for an individual study. Additional detailed dataset information
				is displayed when the dataset title is selected. Requests to change the status of datasets is performed here.</p>
					
			<div id="datasetTableDiv">
				<%-- datasetTableInterface.jsp & datasetList.tag --%>
			</div>

			<s:if test="%{isAdmin}">
				<div class="form-field clear-left">
					<div class="button">
						<input type="button" value="Continue" onClick="javascript:submitForm('studyAction!moveToPermissions.action')" />
					</div>
					<a class="form-link" href="javascript:submitForm('studyAction!submit.action')">Save &amp; Finish</a> <a
						class="form-link" href="/portal/study/viewStudyAction!view.action?studyId=${sessionStudy.study.prefixedId}">Cancel</a>
				</div>
			</s:if>

			<s:else>
				<div class="form-field clear-left">
					<div class="button">
						<input type="button" value="Save & Finish" onClick="javascript:submitForm('studyAction!submit.action')" />
					</div>
					<a class="form-link" href="viewStudyAction!view.action?studyId=${sessionStudy.study.prefixedId}">Cancel</a>
				</div>
			</s:else>
		</s:form>
	</div>
</div>

<script type="text/javascript">
	<s:if test="!inAdmin" >
		<s:if test="isCreate">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"createStudyLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"studyToolLink", "tertiaryLinkID":"browseStudyLink"});
		</s:else>
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"studyList"});
	</s:else>


	$('document').ready(function() { 
		init("studyDatasetAction!viewDatasetTable.ajax");
	}); 
	 
	function init(action) {
		$.post(action, 
			{editMode : true}, 
			function (data) {
				$("#datasetTableDiv").html(data);
			}
		);
	}

	/**
	 * Resets the option dropdown and button to its default status
	 */
	function resetUI() {
		$('#datasetStatusSelect')
			.prop('disabled', true)
			.attr("disabled", "disabled")
			.empty()
			.append('<option selected value="">Select a Dataset</option>');
		$("#changeStatusButton").prop("value", "Change Status");
		disableStatusButton();
	}
	
	/**
	 * Handles click events on dataset checkboxes, changes the status update dropdown and the change status button
	 */
	function updateStatusOptions(apiRow, api) {
		var id = apiRow.id();
		var status = apiRow.data().status;
		var isRequest = apiRow.data().requestedStatus === "true";
		var $table = $("#datasetTable");
		
		var selectedOptionsLength = $table.idtApi("getSelected").length;
		if(selectedOptionsLength != 0) {
			api.rows().every(function(rowIndex, tableLoopCounter, rowLoopCounter) {
				var data = this.data();
				var currentStatus = data.status;
				var currentIsRequest = data.requestedStatus === "true";
				if(!(isRequest && currentIsRequest) && (status != currentStatus || isRequest != currentIsRequest)) {
					$table.idtApi("disableRow", this);
				}
			});
			
			if(!isRequest) {
				if(status == "Private") {
					$('#datasetStatusSelect').removeProp('disabled');
					$('#datasetStatusSelect').removeAttr("disabled");
					$('#datasetStatusSelect').empty().append('<option selected value="">- Select One -</option><option selected value="1">Request to Share</option><option selected value="2">Request to Archive</option><option selected value="3">Request to Delete</option>');
					$("#datasetStatusSelect option:first-child").attr("selected","selected");
					enableStatusButton();
				} else if(status=="Shared") {
					$('#datasetStatusSelect').removeProp('disabled');
					$('#datasetStatusSelect').removeAttr("disabled");
					$('#datasetStatusSelect').empty().append('<option selected value="2">Request to Archive</option>');
					$("#datasetStatusSelect option:first-child").attr("selected","selected");
					enableStatusButton();
				} else if(status=="Archived") {
					$('#datasetStatusSelect').prop('disabled', true);
					$('#datasetStatusSelect').removeAttr("disabled");
					$('#datasetStatusSelect').empty().append('<option selected value="">No Action</option>');
					disableStatusButton();
				}else if(status=="Loading Data") {
					$('#datasetStatusSelect').prop('disabled', true);
					$('#datasetStatusSelect').attr("disabled", "disabled");
					disableStatusButton();
				}else if(status=="Uploading") {
					$('#datasetStatusSelect').prop('disabled', true);
					$('#datasetStatusSelect').attr("disabled", "disabled");
					disableStatusButton();
				}else if(status=="Error During Load") {
					$('#datasetStatusSelect').prop('disabled', true);
					$('#datasetStatusSelect').attr("disabled", "disabled");
					//add disabled look and change the button to disabled 
					disableStatusButton();
				}
				
				$("#changeStatusButton").prop("value", "Change Status");
				$("#changeStatusButton").attr('onclick', 'javascript:changeStatus()');
			} else {
				$('#datasetStatusSelect').prop('disabled', true);
				$('#datasetStatusSelect').attr("disabled", "disabled");
				$('#datasetStatusSelect').empty().append('<option selected value="">Pending Request</option>');
				$("#changeStatusButton").prop("value", "Cancel Request");
				$("#changeStatusButton").attr('onclick', "javascript:cancelRequest("+id+",'"+status+"')");
				enableStatusButton();
			}
		}
		else if(selectedOptionsLength == 0) { //enable all checkboxes if no checkboxes are checked
			api.rows().every(function(rowIndex, tableLoopCounter, rowLoopCounter) {
				var data = this.data();
				var currentStatus = data.status;
				var currentIsRequest = data.requestedStatus === "true";
				if(currentIsRequest || currentStatus == "Private" || currentStatus == "Shared") {
					$table.idtApi("enableRow", this);
				}
			});
			
			resetUI();
		}
	}
	

	function enableStatusButton(){
		$("#changeStatusButtonDiv").removeClass("disabled");
		document.getElementById("changeStatusButton").disabled=false;
	}
	

	function disableStatusButton(){
		$("#changeStatusButtonDiv").addClass("disabled");
		document.getElementById("changeStatusButton").disabled=true;
	}
	
	/**
	 * Used to cancel a dataset request but only for certain statuses
	 */
	function cancelRequest() {
		var statusSelectVal = $("#datasetStatusSelect").val();
		var data = {
			selectedDatasetIds: $("#datasetTable").idtApi("getSelected"),
			datasetStatusSelect: statusSelectVal
		};
		$.ajax({
			type: "POST",
			cache: false,
			url: "studyDatasetAction!cancelDatasetRequest.ajax",
			data: data,
			traditional: true,
			success: function(data) {
				$("#datasetTableDiv").html(data);
			}
		});
	}
	
	function changeStatus() {
		var statusSelectVal = $("#datasetStatusSelect").val();
		var data = {
			selectedDatasetIds: $("#datasetTable").idtApi("getSelected"),
			datasetStatusSelect: statusSelectVal
		};
		
		if(statusSelectVal != "") {
			$.ajax({
				type: "POST",
				cache: false,
				url: "studyDatasetAction!requestDatasetStatus.ajax",
				data: data,
				traditional: true,
				success: function(data) {
					$('#check1').attr('checked', false);
					$('#datasetStatusSelect').empty().append('<option selected value="">Select a Dataset</option>');
					$('#datasetStatusSelect').prop('disabled', true);
					
					$("#datasetTableDiv").html(data);
				}
			});
		}
		else {
			$.ibisMessaging("dialog", "warning", "Please select a status for the dataset(s)");
		}
	}
	
	var showLightBox = true;
	
	function viewDataset(prefixedId, isAdmin) {
		//$.fancybox.showActivity();
		console.log(' viewDataset' + showLightBox);
		if(showLightBox){
			showLightBox = false;
			var action = "/portal/study/datasetAction!viewLightbox.ajax";
			if(isAdmin == "true"){
				action = "/portal/studyAdmin/datasetAction!viewLightbox.ajax";
			}
			$.post(action, 
				{ prefixedId:prefixedId }, 
				function (data) {
					$.bricsDialog(data);
					$("#fancybox-wrap").unbind('mousewheel.fb');
					showLightBox = true;
				}
			);
			console.log(' viewDataset' + showLightBox);
		}
	}
</script>