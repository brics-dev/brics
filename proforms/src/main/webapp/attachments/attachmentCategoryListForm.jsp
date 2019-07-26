<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.attachments.domain.AttachmentCategory" %>
<%@ page import="java.util.List"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<script type="text/javascript">
	$(document).ready(function() {
		$("select[name='attachment.category.id']").val("<%=request.getSession().getAttribute("_categoryid")%>");
	});
</script>	

<select style="width:164px" name="attachment.category.id">

<% 
	List<AttachmentCategory> _attachment_categories = (List<AttachmentCategory>) request
			.getSession().getAttribute("_attachments_categories");
	for (AttachmentCategory cat : _attachment_categories) {
%>
		<option value="<%=cat.getId()%>"><%=cat.getName() %></option>
<%	} 
%>

</select>
