<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="form-field">
	<label for="name">File Name</label>
	<div class="readonly-text">${selectedDocumentName }</div>
</div>
<div class="form-field">
	<label for="pubMedId">PubMed ID </label>
	${pubEntry.pubmedId}
</div>

<div class="form-field">
	<label for="docTitle">Title </label>
	<div class="readonly-text">${pubEntry.title}</div>
</div>

<div class="form-field">
	<label for="pubDate">Publication Date</label>
	<div class="readonly-text">${pubEntry.dateString}</div>
</div>

<div class="form-field">
<h3 class="underlined">First Author</h3>
</div>

<div class="form-field">
	<label for="faFirstName">First Name</label>
	<div class="readonly-text">${pubEntry.firstAuthor.firstName }</div>
</div>

<div class="form-field">
	<label for="faMiddleName">Middle Name </label>
	<div class="readonly-text">${pubEntry.firstAuthor.mi }</div>
</div>

<div class="form-field">
	<label for="faLastName">Last Name</label>
	<div class="readonly-text">${pubEntry.firstAuthor.lastName }</div>
</div>

<div class="form-field">
	<label for="faEmail">Author Email </label>
	<div class="readonly-text">${pubEntry.firstAuthor.email }</div>
</div>

<div class="form-field">
	<label for="faOrg">Author Organization </label>
	<div class="readonly-text">${pubEntry.firstAuthor.orgName }</div>
</div>

<div class="form-field">
	<h3 class="underlined">Last Author</h3>
</div>

<div class="form-field">
	<label for="laFirstName">First Name </label>
	<div class="readonly-text">${pubEntry.lastAuthor.firstName }</div>
</div>

<div class="form-field">
	<label for="laMiddleName">Middle Name </label>
	<div class="readonly-text">${pubEntry.lastAuthor.mi }</div>
</div>

<div class="form-field">
	<label for="laLastName">Last Name </label>
	<div class="readonly-text">${pubEntry.lastAuthor.lastName }</div>
</div>

<div class="form-field">
	<label for="laEmail">Author Email </label>
	<div class="readonly-text">${pubEntry.lastAuthor.email }</div>
</div>

<div class="form-field">
	<label for="laOrg">Author Organization </label>
	<div class="readonly-text">${pubEntry.lastAuthor.orgName }</div>
</div>

<div class="form-field" id="abstractDiv">
	<label for="abstract">Abstract</label>
	<div class="readonly-text">${pubEntry.description }</div>
</div>

