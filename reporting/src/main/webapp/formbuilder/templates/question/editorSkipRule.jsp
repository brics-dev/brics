<script id="editQuestionSkipRuleTabLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_skipRule">Skip Rule</a></li>
</script>





<script id="editQuestionSkipRuleTab" type="text/x-handlebars-template">
<div class="tabcontainer" id="dialog_editQuestion_skipRule">


<div class="row clearfix">

<div id='skipRuleContainer1' class='col-md-6'>

	<div class="row clearfix">
			<div class="col-md-4">
				<label>
					Skip Rule Operator
				</label>
			</div>


			<div class="col-md-2">
				<select id="skipRuleOperatorType" name="skipRuleOperatorType">
  					<option value="-2147483648">None</option>
  					<option value="4">Contains</option>
  					<option value="3">Has Any Value</option>
  					<option value="2">Is Blank</option>
					<option value="1">Equals</option>
				</select> 
			</div>

			<div class="col-md-5">
				<input type="textbox" size="20" maxlength="50" disabled="true" id="skipRuleEquals" name="skipRuleEquals" />
			</div>
	</div>


	<div class="row clearfix">
			<div class="col-md-4">
				<label>
					Skip Rule
				</label>
			</div>


			<div class="col-md-2">
				<select id="skipRuleType" name="skipRuleType" disabled="true">
  					<option value="-2147483648">None</option>
  					<option value="2">Disable</option>
					<option value="1">Require</option>
				</select> 
			</div>

		


	</div>

	<div class="row clearfix">
		<div class="col-md-3">
				<input type="button" id="addEditSkipQuestionsButton" value="Add/Edit Questions to skip" style="width: 140%;" disabled="true" />
			</div>
		</div>
	</div>







<div id='skipRuleContainer2' class='col-md-6'>
	
<div class="row clearfix divSkipRuleHeaders">	
		<div class="col-md-3">
			<label>Section</span></label>
		</div>
		<div class="col-md-3">
			<label>Question Name</label>
		</div>
		<div class="col-md-3">
			<label>Question Text</label>
		</div>
		<div class="col-md-3">
			<label>Question Type</label>
		</div>
		
	</div>



<div class="row clearfix">
		<div class="col-md-12  divQuestionsToSkip">

		</div>
	</div>
</div >










</div>




</script>











<script id="skipQuestionRow" type="text/x-handlebars-template">

<div class='row clearfix skipRow'>
	<div class='col-md-3 skipSection'>{{section}}</div>
	<div class='col-md-3 skipQuestionName' >{{questionName}}</div>
	<div class='col-md-3 skipQuestionText' >{{questionText}}</div>
	<div class='col-md-3 skipQuestionType' >{{questionType}}</div>
	
	<div class="clearfix"></div>
</div>

</script>



