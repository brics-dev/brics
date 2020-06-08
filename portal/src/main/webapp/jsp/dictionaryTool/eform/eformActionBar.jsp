<%@taglib prefix="s" uri="/struts-tags"%>

	<div class="actionBarHeaderStatus">
		<b>Status: </b><s:property value="sessionEform.basicEform.status.type" />
	</div>
	<ul>
		<s:if test="!inAdmin && hasWritePermission && isDraft">
			<li>
				<a href="javascript:approve(1);" id="eformOps_requestPub">Request Publication</a>
			</li>
		</s:if>
		<s:if test="(isDictionaryAdmin || hasAdminPermission) && isDraft">
			<li>
				<a href="javascript:publish(2);" id="eformOps_publish">Publish</a>
			</li>
		</s:if>
		<s:if test="isPublished">
			<s:if test="basicEform.isShared && isDictionaryAdmin">
				<li>
					<a href="javascript:approve(3);" id="eformOps_archive">Archive</a>
				</li>
			</s:if>
			<s:elseif test="(!basicEform.isShared || basicEform.isShared == null) && (isDictionaryAdmin || hasAdminPermission)">
				<li>
					<a href="javascript:approve(3);" id="eformOps_archive">Archive</a>
				</li>
			</s:elseif>
			<s:if test="isDictionaryAdmin && (!basicEform.isShared || basicEform.isShared == null)">
				<li>
					<a href="/portal/dictionary/eFormAction!standardize.action?eformId=<s:property value="sessionEform.basicEform.id" />" id="eformOps_edit">Standardize</a>
				</li>
			</s:if>
		</s:if>
		<s:if test="(isDictionaryAdmin || hasAdminPermission) && isArchived">
			<li>
				<a href="javascript:approve(0);" id="eformOps_unarchive">Un-Archive</a>
			</li>
		</s:if>
		<s:if test="(isDictionaryAdmin || hasAdminPermission) && isAwaitingPublication">
			<li>
				<a href="javascript:publicationDecision(2);" id="eformOps_approve">Approve</a> 
				&nbsp;or&nbsp;
				<a href="javascript:publicationDecision(0);" id="eformOps_reject">Reject</a>
			</li>
		</s:if>
		<s:if test="sessionEform.basicEform.isLegacy == null || !sessionEform.basicEform.isLegacy">
			<s:if test="(isDictionaryAdmin || hasWritePermission) && (!isPublished && !isArchived)">
				<li>
					<a href="/portal/dictionary/eformEditAction!editEform.action?eformId=<s:property value="sessionEform.basicEform.id" />" id="eformOps_edit">Edit</a>
				</li>
			</s:if>
			<li>
				<a href="/portal/dictionary/eFormAction!copyEform.action?eformId=<s:property value="sessionEform.basicEform.id" />" id="eformOps_copy">Copy</a>
			</li>
		</s:if>
		<s:if test="sessionEform.basicEform.isLegacy">
		<li>
			<a href="javascript:displayExportLegacyFormError()">Export</a>
		</li>
		</s:if>
		<s:else>
		<li>
			<a href="/portal/dictionary/eformExportAction!export.action?eformId=<s:property value="sessionEform.basicEform.id" />" id="eformOps_export">Export</a>
		</li>
		</s:else>
		<s:if test="(isDictionaryAdmin || hasWritePermission) && (!isPublished&& !isArchived)">
			<li>
				<a href="javascript:checkForCollections()" id="eformOps_delete">Delete</a>
			</li>
		</s:if>
	</ul>