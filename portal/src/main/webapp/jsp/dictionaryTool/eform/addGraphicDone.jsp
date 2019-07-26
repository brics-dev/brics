<%@ taglib uri="/struts-tags" prefix="s" %>
<script type="text/javascript" src='/portal//formbuilder/js/lib/jquery-1.10.2.js'></script>
 <script type="text/javascript">
	$(document).ready(function(){
		if(typeof(parent.FormBuilder) !== 'undefined'){
			parent.FormBuilder.page.get("activeEditorView").finishQuestionAddEdit();
		}else{
			parent.finishQuestionAddEdit();
		}
	});
</script>
<html>
<div>add Graphic</div>
	<table>
		<tr>
			<td>
				<s:hidden value="%{graphicJSON}" name="graphicJSON" id='graphicJSON' ></s:hidden>
			</td>
		</tr>
	</table>
</html>