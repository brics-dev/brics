<%@include file="/common/taglibs.jsp"%>

<s:set var="currentDataStructure" value="currentDataStructure" />
<s:set var="isDictionaryAdmin" value="isDictionaryAdmin" />
<s:set var="isDataStructureAdmin" value="isDataStructureAdmin" />
<div style="border-bottom:1px solid #d9d9d9; margin-bottom:5px;"><b>Status:</b>&nbsp;<s:property value="currentDataStructure.status.type" /></div>

<ul>
	<s:if test="%{currentDataStructure.status.type=='Draft'}">
		<s:if test="%{inAdmin}">

			<li><span class="icon"><img alt="Publish"
					src="<s:url value='/images/icons/publish.png' />"
					style="padding-right: 10px;" height="15px" width="15px" /></span><a
				id="reqPubId" href="javascript:publication(2)"
				alt='This Form Structure is currently in the "Draft" state. You may publish this Data Element below.'>Publish</a></li>

			<li><span class="icon"><img alt="Create Shared Draft"
					src="<s:url value='/images/icons/shared-draft.png' />"
					style="padding-right: 10px;" height="15px" width="15px" /></span><a
				id="reqPubId" href="javascript:publication(5)">Convert To Shared Draft</a></li>

		</s:if>
		<s:elseif test="%{canAdmin}">
			<li><span class="icon"><img alt="Request Publication"
					src="<s:url value='/images/icons/publish.png' />"
					style="padding-right: 10px;" height="15px" width="15px" /></span><a
				id="reqPubId" href="javascript:publication(1)">Request Publication</a></li>
		</s:elseif>
	</s:if>
	<s:elseif
		test="%{currentDataStructure.status.type=='Awaiting Publication'}">
		<s:if test="%{canAdmin}">
			<s:if test="%{!inAdmin}">
				<li><span class="icon"><img alt="Cancel Request"
						src="<s:url value='/images/icons/cancel_pub.png' />"
						style="padding-right: 10px;" height="15px" width="15px" /></span><a
					id="reqPubId" href="javascript:publication(0)">Cancel Request</a></li>
			</s:if>
		</s:if>
	</s:elseif>
	<s:elseif test="%{currentDataStructure.status.type=='Published'}">

		<s:if test="%{inAdmin}">
			<li><span class="icon"><img alt="Archive"
					src="<s:url value='/images/icons/archive.png' />"
					style="padding-right: 10px;" height="15px" width="15px" /></span><a
				id="reqPubId" href="javascript:publication(3)">Archive</a></li>
		</s:if>
	</s:elseif>
	<s:elseif test="%{currentDataStructure.status.type=='Archived'}">
		<s:if test="%{inAdmin}">
			<li><span class="icon"><img alt="Un-Archive"
					src="<s:url value='/images/icons/unarchive.png' />"
					style="padding-right: 10px;" height="15px" width="15px" /></span><a
				id="reqPubId" href="javascript:publication(2)">Un-Archive</a></li>
		</s:if>
	</s:elseif>
	<s:elseif test="%{currentDataStructure.status.type=='Shared Draft'}">
		<s:if test="%{inAdmin}">
			<li><span class="icon"><img alt="Revert Shared Draft"
					src="<s:url value='/images/icons/publish.png' />"
					style="padding-right: 10px;" height="15px" width="15px" /></span><a
				id="reqPubId" href="javascript:publication(0)">Revert Shared Draft</a></li>

		</s:if>
	</s:elseif>
	<s:if test="inAdmin && currentDataStructure.status.id == 1">
		<li><span class="icon"><img alt="Approve Publication"
				src="<s:url value='/images/icons/publish.png' />"
				style="padding-right: 10px;" height="15px" width="15px" /></span> <a
			class="reqPubId" id="approvePublication" href="javascript:void(0);"
			onclick="approvePublication();">Approve Publication</a>
		</li>

		<li><span class="icon"><img alt="Deny Publication"
				src="<s:url value='/images/icons/cancel_pub.png' />"
				style="padding-right: 10px;" height="15px" width="15px" /></span> <a
			class="reqPubId" id="denyPublication" href="javascript:void(0);"
			onclick="denyPublication();">Deny Publication</a></li>

	</s:if>

</ul>
