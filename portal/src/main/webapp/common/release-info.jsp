<%@taglib prefix="s" uri="/struts-tags"%>

<div class="build-notes">
	<p>
		Version: <s:property value="%{buildID}" /><br />
		Repository ID: <s:property value="%{deploymentID}" />
	</p>
</div>