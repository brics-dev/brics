<%@taglib prefix="s" uri="/struts-tags"%>

<div id="actionsBar">
	<div class="actionBarHeaderStatus">
		<b>Status: </b><s:property value="currentMetaStudy.status.name" />
	</div>
	<ul>
		<s:if test="hasWritePermission && isDraft && !isMetaStudyAdmin">
			<li>
				<span class="icon">
					<img src="/portal/images/icons/publish.png" alt="request publication" width="15px" height="15px" style="padding-right: 5px" />
				</span>
				<a href="javascript:void(0);" onclick="confirmRequest()" id="metaStudyOps_requestPub">Request Publication</a>
			</li>
		</s:if>
		<s:if test="isMetaStudyAdmin && isDraft">
			<li>
				<span class="icon">
					<img src="/portal/images/icons/publish.png" alt="publish" width="15px" height="15px" style="padding-right: 5px" />
				</span>
				<a href="javascript:void(0);" onclick="confirmPublish()" id="metaStudyOps_publish">Publish</a>
			</li>
		</s:if>
		<s:if test="isMetaStudyAdmin && isPublished">
			<li>
				<span class="icon">
					<img src="/portal/images/icons/cancel_pub.png" alt="unpublish" width="15px" height="15px" style="padding-right: 5px" />
				</span>
				<a href="javascript:void(0);" onclick="confirmUnpublish()" id="metaStudyOps_unpublish">Unpublish</a>
			</li>
		</s:if>
		<s:if test="%{canAssignDoiForMetaStudy()}">
			<li id="assignDoiLinkDisplay">
				<span class="icon">
					<img src="/portal/images/icons/cancel_pub.png" alt="unpublish" width="15px" height="15px" style="padding-right: 5px" />
				</span>
				<a href="javascript:void(0);" id="metaStudyOps_assignDoi">Assign DOI</a>
			</li>
		</s:if>
		<s:if test="isMetaStudyAdmin && isAwaitingPublication">
			<li>
				<span class="icon">
					<img src="/portal/images/icons/confirm.png" alt="approve" width="15px" height="15px" style="padding-right: 3px" />
				</span>
				<a href="javascript:void(0);" onclick="confirmApprove()" id="metaStudyOps_approve">Approve</a> 
				&nbsp;or&nbsp;
				<span class="icon">
					<img src="/portal/images/icons/deleteDot.png" alt="reject" width="15px" height="15px" style="padding-right: 3px" />
				</span>
				<a href="javascript:void(0);" onclick="confirmReject()" id="metaStudyOps_reject">Reject</a>
			</li>
		</s:if>
		<s:if test="(hasAdminPermission || isMetaStudyAdmin) && !isPublished">
			<li>
				<span class="icon">
					<img src="/portal/images/icons/shared-draft.png" alt="edit permissions" width="15px" height="15px" style="padding-right: 5px" />
				</span>
				<a href="/portal/metastudy/metaStudyAction!editPermissions.action?metaStudyId=${currentMetaStudy.id}" id="metaStudyOps_editPerms">Edit Permissions</a>
			</li>
		</s:if>
		<s:if test="(hasWritePermission || isMetaStudyAdmin) && !isPublished">
			<li>
				<span class="icon">
					<img src="/portal/images/icons/edit.png" alt="edit" width="15px" height="15px" style="padding-right: 5px" />
				</span>
				<a href="/portal/metastudy/metaStudyAction!edit.action?metaStudyId=${currentMetaStudy.id}" id="metaStudyOps_edit">Edit</a>
			</li>
		</s:if>
		<s:if test="(hasAdminPermission && isDraft) || (isMetaStudyAdmin && !isPublished)">
			<li>
				<span class="icon">
					<img src="/portal/images/icons/delete.png" alt="delete" width="15px" height="15px" style="padding-right: 5px" />
				</span>
				<a href="javascript:void(0);" onclick="confirmDelete()" id="metaStudyOps_delete">Delete</a>
			</li>
		</s:if>
	</ul>
</div>

<script>

function confirmRequest() {
	var msgText = "Are you sure you want to request publication?";
	var yesBtnText = "Request";
	var noBtnText = "Do Not Request";
	var action = "metaStudyAction!requestPublication.action"; 
	
	confirmationDialog("warning", msgText, yesBtnText, noBtnText, action, true, "400px", "Confirm Request Publication");
}

function confirmPublish() {
	var msgText = "Are you sure you want to publish? Further changes will not be allowed without first unpublishing the meta study.";
	var yesBtnText = "Publish";
	var noBtnText = "Do Not Publish";
	var action = "metaStudyAction!approve.action"; 
	
	confirmationDialog("warning", msgText, yesBtnText, noBtnText, action, true, "400px", "Confirm Publish");
}

function confirmUnpublish() {
	var msgText = "Are you sure you want to unpublish?<s:if test="%{isDoiEnabled && currentMetaStudy.doi != null && !currentMetaStudy.doi.isEmpty()}"> This action will allow the editing of information within a meta study that has an active DOI. The changes could effect publications that are referencing the meta study.</s:if>";
	var yesBtnText = "Unpublish";
	var noBtnText = "Do Not Unpublish";
	var action = "metaStudyAction!unpublish.action"; 
	
	confirmationDialog("warning", msgText, yesBtnText, noBtnText, action, true, "400px", "Confirm Unpublish");
}

function confirmApprove() {
	var msgText = "Are you sure you want to approve the request for publication? Further changes will not be allowed without first unpublishing the meta study.";
	var yesBtnText = "Approve";
	var noBtnText = "Do Not Approve";
	var action = "metaStudyAction!approve.action"; 
	
	confirmationDialog("warning", msgText, yesBtnText, noBtnText, action, true, "400px", "Confirm Approve");
}

function confirmReject() {
	var msgText = "Are you sure you want to reject the request for publication?";
	var yesBtnText = "Reject";
	var noBtnText = "Do Not Reject";
	var action = "metaStudyAction!reject.action"; 
	
	confirmationDialog("warning", msgText, yesBtnText, noBtnText, action, true, "400px", "Confirm Reject");
}

function confirmDelete() {
	var msgText = "Are you sure you want to delete this meta study?";
	var yesBtnText = "Delete";
	var noBtnText = "Do Not Delete";
	var action = "metaStudyAction!delete.action?metaStudyId=${currentMetaStudy.id}"; 
	
	confirmationDialog("warning", msgText, yesBtnText, noBtnText, action, true, "400px", "Confirm Deletion");
}
</script>
