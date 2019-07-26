<%@include file="/common/taglibs.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>


<!-- this won't be displayed if there are no attached data structures -->

<div id="attachedDataStructureTableContainer" class="idtTableContainer">
	<table id="attachedDataStructureTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>

<script type="text/javascript">
	
		$( document ).ready(function() {

			$("#attachedDataStructureTable").idtTable({
				autoWidth: false,
				columns: [
					{
						data: "title",
						title: "Title",
						name: "title"
					},
					{
						data: "shortName",
						title: "Short Name",
						name: "shortName"
					},
					{
						data: "version",
						title: "Version",
						name: "version"
					},
					{
						data: "status",
						title: "Status",
						name: "status"
					}
				],
			     data: [
			      <s:iterator value="attachedDataStructures" var="attachedDataStructure">
		              {
		                  <s:if test="publicArea">
		               		"title": "<a class='lightbox' href='/portal/publicData/dataStructureAction!lightboxView.ajax?dataStructureName="+"<s:property value='shortName' />"+"&publicArea=true'>"+"<s:property value='title' />"+"</a>",
						</s:if>
						<s:else>
		               		"title": "<a class='lightbox' href='/portal/dictionary/dataStructureAction!lightboxView.ajax?dataStructureName="+"<s:property value='shortName' />"+"'>"+"<s:property value='title' />"+"</a>",
						</s:else>
		                	"shortName": "<s:property value='shortName' />",
		                	"version":   "<s:property value='version' />",
		                	"status":    "<s:property value='status.type' />"
		              },
	              </s:iterator>
		      ]	
				
			});			
		});

</script>