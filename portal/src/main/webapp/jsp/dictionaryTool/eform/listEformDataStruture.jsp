<%@include file="/common/taglibs.jsp"%>
		<div id="eform">
				<div id="eformListContainer" class="dataTableContainer dataTableJSON">
					<ul></ul>
					<idt:jsontable name="basicEformList" id="basicEform" decorator="gov.nih.tbi.taglib.datatableDecorators.ListEformDataStructureDecorator">
						<idt:column title="Title" property="titleViewOnlyLink" styleClass="breakword" />
						<idt:column title="Description" property="descriptionEllipsis" styleClass="breakword" />
						<idt:column title="Last Update" property="modifiedDate" styleClass="breakword" />
					</idt:jsontable>
				</div>
		</div>