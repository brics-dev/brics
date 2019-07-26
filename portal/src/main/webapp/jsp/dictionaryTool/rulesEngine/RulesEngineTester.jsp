<%@include file="/common/taglibs.jsp"%>

<title>Rules Engine Testing Platform</title>
<h1>Rules Engine Tester</h1>

<h3>The purpose of the Rules Engine Tester is to allow testing
	without the use of cumbersome front-end process</h3>
<br>
<h3>How to use:</h3>
<div>
	<form name="rulesEngineTesterForm" action="RulesEngineTestAction.java"
		method="rulesEngineEvaluation">
		<h2>Select a Data Dictionary Object</h2>
		<table>
			<tr>
				<td><input type="radio" name="dataDictionaryObj"
					value="Data Element">Data Element</td>
				<td><input type="radio" name="dataDictionaryObj"
					value="Form Structure">Form Structure</td>
			</tr>
		</table>
		<br> Original DDO: <input type="text" name="original"><br>
		Incoming DDO: <input type="text" name="incoming"><br> <br>
		<input type="submit" value="Submit"
			onClick="javascript: submitTheForm()"><br> <br>
	</form>
</div>
<s:if test="completedTest">
	<s:if test="hasActionErrors()">
		<div class="form-error">
			<s:actionerror />
		</div>
	</s:if>
	<s:else>
	<div>
		<h2>Changes Made</h2>
			<ol>
			<s:if test="readableSeverityRecords.size !=0">
					<s:iterator var="severity" value="readableSeverityRecords" status="status">
							<li><s:property /></li>
						</tr>
					</s:iterator>
				</s:if>
			</ol>
				
			
	</div>
	</s:else>
</s:if>
	

<s:else>
	<h3>Please enter items to compare</h3>
	<h6>Please enter the short name for either a Data Element or Form
		Structure</h6>
	<br>
	<br>
</s:else>

<script type="text/javascript">
	function submitTheForm() {

		var theForm = document.forms['rulesEngineTesterForm'];
		theForm.action = 'rulesEngineTestAction!rulesEngineEvaluation.action';
		theForm.submit();
	}
</script>
