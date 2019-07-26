function viewDataset(prefixedId, isAdmin) {
	$.fancybox.showActivity();
	var action = "/portal/study/datasetAction!viewLightbox.ajax";
	if(isAdmin == "true"){
		action = "/portal/studyAdmin/datasetAction!viewLightbox.ajax";
	}
	
	console.log("prefixedId: " + prefixedId);
	console.log($.fancybox);
	
	$.post(action, 
		{ prefixedId:prefixedId }, 
		function (data) {
			$.fancybox(data);
			$("#fancybox-wrap").unbind('mousewheel.fb');
		}
	);
}

function viewStudy(studyId, isAdmin) {
	$.fancybox.showActivity();
	var action = "/portal/study/viewStudyAction!lightbox.ajax?studyId=" + studyId;
	if(isAdmin == "true"){
		action = "/portal/studyAdmin/viewStudyAction!lightbox.ajax?studyId=" + studyId;
	}
	
	$.get(action, 
		function (data) {
			$.fancybox(data);
			$("#fancybox-wrap").unbind('mousewheel.fb');
		}
	);
}

// 