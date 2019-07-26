<%@include file="/common/taglibs.jsp"%>

<div style="border-bottom:1px solid #d9d9d9; margin-bottom:5px;">
	<b>Status:</b>&nbsp;<s:property value="currentDataElement.status.name" />
</div>

<!-- displays different things depending on the status of the form structure -->
<ul>

<s:if test="inAdmin && currentDataElement.status.id == 1">
					
					<li id="approvePublicatoin">
						<span class="icon"><img alt="Approve Publication" src="<s:url value='/images/icons/publish.png' />" style="padding-right:10px;" height="15px" width="15px" /></span>
								<a id="approvePublication" href="javascript:void(0);"
									onclick="approvePublication();">Approve Publication</a>
						</li>
							
							<li id="denyPublication">
							<span class="icon"><img alt="Deny Publication" src="<s:url value='/images/icons/cancel_pub.png' />" style="padding-right:10px;" height="15px" width="15px" /></span>
								<a id="denyPublication"  href="javascript:void(0);"
									onclick="denyPublication();" >Deny Publication</a></li>
</s:if>
<s:if test="%{currentDataElement.status.id == 0}">

	<%-- <p>This Data Element is currently in the "Draft" state. A request to move it to publication will change its status
		to "Awaiting Publication" until it has been reviewed.</p>
	<p>If changes are made to the Data Element, the Data Element will return to the "Draft" state.</p>--%>
	<s:if test="!inAdmin">
		<li id="requestPublicatoin">
			<span class="icon"><img alt="Request Publication" src="<s:url value='/images/icons/publish.png' />" style="padding-right:10px;" height="15px" width="15px" /></span><a id="reqPubId" href="javascript:publication(1)">Request Publication</a>
		</li>
	</s:if>
	<s:else>
	
		<li id="publication">
			<span class="icon"><img alt="Publish" src="<s:url value='/images/icons/publish.png' />" style="padding-right:10px;" height="15px" width="15px" /></span><a id="reqPubId" href="javascript:publication(2)">Publish</a>
		</li>
		
		
	</s:else>
</s:if>
<s:elseif test="%{currentDataElement.status.name=='Awaiting Publication'}">
	
	<%-- <p>This Data Element is currently "Awaiting Publication", any changes to it will cause it to revert to "Draft"
		status. Upon review, an administrator can approve the Data Element and move it into "Published" status.</p>
	<p>If desired, the owner of this Data Element may cancel the publication request.</p>--%>
	<s:if test="!inAdmin">
		<li id="cancelRequest">
			<span class="icon"><img alt="Cancel Request" src="<s:url value='/images/icons/cancel_pub.png' />" style="padding-right:10px;" height="15px" width="15px" /></span><a id="reqPubId" href="javascript:publication(0)">Cancel Request</a>
		</li>
	</s:if>
</s:elseif>
<s:elseif test="%{currentDataElement.status.name=='Published'}">

	<%--<p>This Data Element is currently "Published" and accessible to all users.</p> --%>
</s:elseif>

</ul>