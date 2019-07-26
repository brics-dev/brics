function viewDataset(prefixedId, isAdmin) {
	// TODO: start activity spinner
	var action = "/portal/study/datasetAction!viewLightbox.ajax";
	if (isAdmin == "true") {
		action = "/portal/studyAdmin/datasetAction!viewLightbox.ajax";
	}

	console.log("prefixedId: " + prefixedId);
	console.log($.fancybox);

	$.post(action, {
		prefixedId: prefixedId
	}, function(data) {
		$.bricsDialog(data);
		// TODO: end activity spinner
	});
}

function viewStudy(studyId, isAdmin) {
	// TODO: start activity spinner
	var action = "/portal/study/viewStudyAction!lightbox.ajax?studyId=" + studyId;
	if (isAdmin == "true") {
		action = "/portal/studyAdmin/viewStudyAction!lightbox.ajax?studyId=" + studyId;
	}

	$.get(action, function(data) {
		$.bricsDialog(data);
		// TODO: end activity spinner
	});
}

var guidDetails = {};
$(document).ready(function() {
	// this comes from viewGuid.jsp
	if (guidDetailsStr) {
		guidDetails = JSON.parse(guidDetailsStr.replace(new RegExp("&quot;", 'g'), "\""));
		guidDetails.inAdmin = inAdmin || false;

		var guidViewTemplate = TemplateManager.getTemplate("guidViewMain");
		$("#main-content").prepend(guidViewTemplate(guidDetails));

		var $linkedContainer = $("#linkedList");
		var hasLinked = guidDetails.linked;
		var isGuid = guidDetails.type == "GUID";
		/**
		 * If type is pseudoguid and no linked - show "Not yet converted" If
		 * type is guid and no linked - hide "linked to" If type is pseudoguid
		 * and linked - show linked If type is guid and linked, show linked
		 */
		if (!isGuid && !hasLinked) {
			$("#linkedContainer").show();
			$linkedContainer.html("Not yet converted");
		}
		else if (isGuid && !hasLinked) {
			$("#linkedContainer").hide();
		}
		else {
			$("#linkedContainer").show();
			var linkedLength = guidDetails.linked.length;
			for (var j = 0; j < linkedLength; j++) {
				var guid = guidDetails.linked[j];
				$linkedContainer.append('<a href= "javascript:redirectWithReferrer(\'/portal/guid/viewGuidAction!view.action?guid=' + guid + '\');"> ' + guid + '</a><br />');
			}
		}

		// Hide relatedGuid section if it's pseudoGuid
		if (!isGuid) {
			$("#relatedGuids").hide();
		}
	}

	$("#registrationTable").idtTable({
		autoWidth: false,
		pageLength: 15,
		dom: "frtip",
		columns: [{
			data: "fullName",
			title: "USER",
			name: "user"
		}, {
			data: "serverShortName",
			title: "ENTITY",
			name: "serverShortName"
		}, {
			data: "date",
			title: "DATE REGISTERED",
			name: "date"
		}],
		data: guidDetails.registrations
	});

	$("#relatedGuidsTable").idtTable({
		autoWidth: false,
		pageLength: 15,
		dom: "frtip",
		columns: [{
			data: "guid",
			title: "GUID",
			name: "guid"
		}, {
			data: "serverShortName",
			title: "ENTITY",
			name: "entity"
		}, {
			data: "fullName",
			title: "USER",
			name: "user"
		}, {
			data: "date",
			title: "DATE REGISTERED",
			name: "date"
		}],
		data: guidDetails.relatedGuids
	});

	$("#guidDatasetsTable").idtTable({
		idtUrl: "viewGuidAction!datasetDatatable.action?guid=" + guidDetails.guid,
		idtData: {
			primaryKey: "dataset.dataId"
		},
		pages: 1,
		"processing": false,
		"serverSide": false,
		length: 10,
		"columns": [{
			data: "dsId",
			title: "DATASET ID",
			parameter: "dataset.dataId",
			width: "150px",
			name: "datasetId"
		}, {
			data: "viewLink",
			title: "DATASET NAME",
			parameter: "datasetNameLink",
			name: "name"
		}, {
			data: "date",
			title: "DATASET SUBMISSION DATE",
			parameter: "data.submissionDate",
			name: "date",
			render: IdtActions.formatDate()
		}, {
			data: "studyLink",
			title: "STUDY",
			parameter: "studyNameLink",
			name: "studyLink"
		}],
		dom: 'frtip',
		fixedHeader: true,
		bFilter: true
	});
});
// 