<%@include file="/common/taglibs.jsp"%>

<!-- <div class="ui-clearfix">
	<div class="button-right btn-downarrow ">
		<input id="addDocBtn" type="button" value="Add Documentation" />
		<div id="selectAddDocDiv" class="btn-downarrow-options">
			<p>
				<a class="lightbox" onclick="DocumentationDialog.open('url')">URL</a>
				<a class="lightbox" onclick="DocumentationDialog.open('file')">File</a>
			</p>
		</div>
	</div>
</div>
<br />
 -->
<div id="documentationTable" class="dataTableContainer dataTableJSON">
	<ul>
		<li><input type="button" value="Edit"
			onclick="editDocumentation()" /></li>
		<li><input type="button" class="enabledOnMany" value="Delete"
			onclick="deleteDocumentation()" /></li>
	</ul>
	<idt:jsontable name="sessionStudy.study.supportingDocumentationSet" id="supportDocs" decorator="gov.nih.tbi.taglib.datatableDecorators.SupportDocListDecorator">
		<idt:setProperty name="basic.msg.empty_list" value="There are no documents at this time." />
		<idt:column title="" property="selectCheckbox" />
		<idt:column title="NAME" property="docNameLink" styleClass="breakword" />
		<idt:column title="TYPE" property="typeLink" />
		<idt:column title="DESCRIPTION" property="descriptionEllipsis" styleClass="breakword" />
	</idt:jsontable>
</div>

