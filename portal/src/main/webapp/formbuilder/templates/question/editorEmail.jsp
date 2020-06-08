<script type="text/javascript">
Handlebars.registerHelper("ifPredefinedQuestion", function (model, options) {
	var questionType = model.get("questionType");
	var questionTypes = Config.questionTypes;
	if(questionType == questionTypes.radio || questionType == questionTypes.checkbox 
			||questionType == questionTypes.select || questionType == questionTypes.multiSelect) {
		return options.fn(this);
	} else {
		return options.inverse(this);
	}

});

Handlebars.registerHelper("ifFreeTextQuestion", function (model, options) {
	var questionType = model.get("questionType");
	var answerType = this.model.get("answerType");
	var questionTypes = Config.questionTypes;
	if(questionType == questionTypes.textbox && answerType == 2) { //answerType can only be numeric
		return options.fn(this);
	} else {
		return options.inverse(this);
	}

});

function allowNumbersOnly(e) {
    var code = (e.which) ? e.which : e.keyCode;
    if (code > 31 && (code < 48 || code > 57)) {
        e.preventDefault();
    }
}
</script>

<script id="editQuestionEmailTabLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_email">Email Notification</a></li>
</script>



<script id="editQuestionEmailTab" type="text/x-handlebars-template">

<div class="tabcontainer" id="dialog_editQuestion_email">
	<div class="row clearfix">
		<div class="col-md-4">
			<label for="align">Create Email Trigger</label><span class='createET' style='display:none'>(Once Uncheck, the Email trigger will be deleted)</span>
		</div>
		<div class="col-md-2">
			<input type="checkbox" id="createTrigger" />
		</div>
	</div>
	
	<div class='createET' style='display:none'>
			<div class="row  clearfix">
				<div class="col-md-12">By setting an email trigger, the recipients of the email list will receive a notification when responses to the questions asked meet the required threshold.</div>
				<div class="col-md-12"><b style="font-size: 11px;">More than one email address may be entered, and separated by a semicolon.</b></div>
			</div>
		
		<div class="col-md-6">
			<div class="row clearfix">
				<div class="col-md-6">
					<label for="align" class="required">Email Recipient(s)</label>
				</div>
				<div class="col-md-6">
					<input type="textbox" name="toEmailAddress" id="toEmailAddress" />
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-6">
					<label for="align">CC Email Recipient(s)</label>
				</div>
				<div class="col-md-6">
					<input type="textbox" name="ccEmailAddress" id="ccEmailAddress" />
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-6">
					<label for="align">Subject</label>
				</div>	
				<div class="col-md-6">
					<input type="textbox" name="subject" id="subject" />
				</div>
			</div>
		</div>
		<div class="col-md-4">
			<div class="row clearfix">
				<div class="col-md-3">
					<label for="align">Body</label>
				</div>
				<div class="col-md-9">
					<textarea name="body" id="body" rows="3" cols="25" maxlength="4000"/>
				</div>
			</div>
		</div>
		{{#ifPredefinedQuestion model}}
		<div class="col-md-4">
			<div class="row  clearfix">
				<div class="col-md-6">
					<label for="align">Answer(s) to trigger<span class="requiredStar">*</span></label>
				</div>
				<div class="col-md-6">
					<select multiple name="triggerAnswers" id="triggerAnswers">
					</select>
				</div>
			</div>	
		</div>
		{{/ifPredefinedQuestion}} 
		{{#ifFreeTextQuestion model}}

		<div class="row  clearfix">
			<div class="col-md-3" >
				<label for="align" style="text-align: left;">Condition(s) to trigger<span class="requiredStar">*</span></label>
			</div>
		</div>

	<div class="row clearfix">
		<%-- Available Question list --%>
		<div class="col-md-2">
				<div class="row clearfix">
					<div class="col-md-12">
						<label style="font-size: 11px; text-align: left;">Questions available:</label>
					</div>
				</div>

				<div class="row clearfix">
					<div class="col-md-12" id="availableQuestionsToEmailDiv">
						<select id="triggerQuestion">
							<%--show available questions there --%>
						</select>
					</div>
				</div>		
		</div>
		<%-- calculator --%>
		<div class="col-md-2">
				<div class="row clearfix">
					<div class="col-md-12" >
						<label style="font-size: 11px; text-align: left;">Numbers and Opeators:</label>
					</div>
				</div>
			<div class="row clearfix">
				<div class="col-md-12" >
						<table>
							<tr width="100%">
								<td colspan="8" width="100%">
									<a href="javascript:void(0)" id="etBackspace"><img border="0" src="/portal/images/eform/calculator/calc_backspace.png"></a>
						            <a href="javascript:void(0)" id="etClear"><img border="0" src="/portal/images/eform/calculator/calc_clear.png" ></a>
								</td>
							</tr>
						    <tr>
								<td><a href="javascript:void(0)" id="et7"><img border="0" src="/portal/images/eform/calculator/calc_seven.png"></a></td>
								<td><a href="javascript:void(0)" id="et8"><img border="0" src="/portal/images/eform/calculator/calc_eight.png"></a></td>
								<td><a href="javascript:void(0)" id="et9"><img border="0" src="/portal/images/eform/calculator/calc_nine.png"></a></td>
								<td><a href="javascript:void(0)" id="etDivide"><img border="0" src="/portal/images/eform/calculator/calc_divide.png"></a></td>
								<td><a href="javascript:void(0)" id="etSqrt"><img border="0" src="/portal/images/eform/calculator/calc_sqrt.png"></a></td>								
								<td><a href="javascript:void(0)" id="etEqualEqual"><img border="0" src="/portal/images/eform/calculator/calc_equal_equal.png"></a></td>
								<td><a href="javascript:void(0)" id="etNotEqual"><img border="0" src="/portal/images/eform/calculator/calc_not_equal.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="et4"><img border="0" src="/portal/images/eform/calculator/calc_four.png"></a></td>
								<td><a href="javascript:void(0)" id="et5"><img border="0" src="/portal/images/eform/calculator/calc_five.png"></a></td>
								<td><a href="javascript:void(0)" id="et6"><img border="0" src="/portal/images/eform/calculator/calc_six.png"></a></td>
								<td><a href="javascript:void(0)" id="etMult"><img border="0" src="/portal/images/eform/calculator/calc_multiply_star.png"></a></td>
								<td><a href="javascript:void(0)" id="etPercent"><img border="0" src="/portal/images/eform/calculator/calc_percent.png"></a></td>
								<td><a href="javascript:void(0)" id="etGreater"><img border="0" src="/portal/images/eform/calculator/calc_greater.png"></a></td>
								<td><a href="javascript:void(0)" id="etGreaterEqual"><img border="0" src="/portal/images/eform/calculator/calc_greater_equal.png"></a></td>									
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="et1"><img border="0" src="/portal/images/eform/calculator/calc_one.png"></a></td>
								<td><a href="javascript:void(0)" id="et2"><img border="0" src="/portal/images/eform/calculator/calc_two.png"></a></td>
								<td><a href="javascript:void(0)" id="et3"><img border="0" src="/portal/images/eform/calculator/calc_three.png"></a></td>
								<td><a href="javascript:void(0)" id="etSubt"><img border="0" src="/portal/images/eform/calculator/calc_minus.png"></a></td>
								<td><a href="javascript:void(0)" id="etAdd"><img border="0" src="/portal/images/eform/calculator/calc_plus.png"></a></td>
								<td><a href="javascript:void(0)" id="etLess"><img border="0" src="/portal/images/eform/calculator/calc_lesser.png"></a></td>
								<td><a href="javascript:void(0)" id="etLessEqual"><img border="0" src="/portal/images/eform/calculator/calc_lesser_equal.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="etFlip"><img border="0" src="/portal/images/eform/calculator/calc_plus_minus.png"></a></td>
								<td><a href="javascript:void(0)" id="et0"><img border="0" src="/portal/images/eform/calculator/calc_zero.png"></a></td>
								<td><a href="javascript:void(0)" id="etDot"><img border="0" src="/portal/images/eform/calculator/calc_decimal.png"></a></td>
								<td><a href="javascript:void(0)" id="etLeftP"><img border="0" src="/portal/images/eform/calculator/calc_paren_left.png"></a></td>
								<td><a href="javascript:void(0)" id="etRightP"><img border="0" src="/portal/images/eform/calculator/calc_paren_right.png"></a></td>
								<td><a href="javascript:void(0)" id="etAnd"><img border="0" src="/portal/images/eform/calculator/calc_and.png"></a></td>
								<td><a href="javascript:void(0)" id="etOr"><img border="0" src="/portal/images/eform/calculator/calc_or.png"></a></td>
							</tr>
						</table>
				</div>
			</div>
		</div>
		<%-- DateTime Conversion Factor --%>
		<%--<div class="col-md-2" id="etDateTimeConversionDiv" style="display: none;">
				<div class="row clearfix">
					<div class="col-md-12" >
						<label style="font-size: 11px; text-align: left;">Date-Time Conversion Factor:</label>
					</div>
				</div>
				<div class="row clearfix">
					<div class="col-md-12" >
						<select id="triggerConversionFactor" name="triggerConversionFactor">
        					<option value="-2147483648">None</option>
        					<option value="7">Seconds</option>
							<option value="6">Minutes</option>
							<option value="5">Hours</option>
							<option value="4">Days</option>
							<option value="3">Weeks</option>
							<option value="2">Months</option>
							<option value="1">Years</option>
        				</select>
					</div>
				</div>
		</div> --%>
		<div class="col-md-8" >
				<div class="row clearfix">
					<div class="col-md-12" >
						<label style="font-size: 11px; text-align: left;">Trigger Conditions:</label>
					</div>
				</div>
				<div class="row clearfix">
					<div class="col-md-12" >
						<div id="conditionsDisplay" style="border:1px solid; height: 140px; overflow-y: auto;">
						</div>
					</div>
				</div>
				<div class="row clearfix">
					<div class="col-md-12" >
						<div id="condInputWarning" >
						</div>
					</div>
				</div>
		</div>
	</div>

		{{/ifFreeTextQuestion}}		
	</div>
 <div class="clearfix">
  </div>
</div>

</script>
