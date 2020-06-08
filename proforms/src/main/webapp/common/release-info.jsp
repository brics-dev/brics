<%@ taglib uri="/struts-tags" prefix="s"%>

<div class="build-notes" >
	<p class="left">
		Build Version: <s:property value="%{buildID}" /><br/>
		Repository ID: <s:property value="%{deploymentID}" />
	</p>
</div>