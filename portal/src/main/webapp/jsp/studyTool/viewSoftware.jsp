<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<div class="form-field">
	<label for="name">File Name</label>
	<div class="readonly-text">${selectedDocumentName }</div>
</div>
<div class="form-field">
	<label for="supportingDocTitle">Title</label>
	<div class="readonly-text">${supportingDocTitle }</div>
</div>
<div class="form-field">
	<label for="version">Version</label>
	<div class="readonly-text">${version }</div>
</div>
<%-- <div class="form-field" id="descDiv">
	<label for="description">Description</label>
	<div class="readonly-text">${supportingDocDescription }</div>
</div> --%>

