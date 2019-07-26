<script id="editQuestionCalculationRuleTabLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_calculationRule">Calculation Rule</a></li>
</script>

<script id="editQuestionCalculationRuleTab" type="text/x-handlebars-template">
<div class="tabcontainer" id="dialog_editQuestion_calculationRule">
	
	<div class="row clearfix">
		
		<div class="col-md-2">


				<div class="row clearfix">
					<div class="col-md-12" >
						<label>Questions available:</label>
					</div>
				</div>

				<div class="row clearfix">
					<div class="col-md-12" id="availableQuestionsToCalculateDiv">
						<select id="questionCalc">
							<%--show available questions there --%>
						</select>
					</div>
				</div>
							
							
		</div>
		<div class="col-md-2">
			<div class="row clearfix">
				<div class="col-md-12" >
						<table>
							<tr width="100%">
								<td colspan="5" width="100%">
									<a href="javascript:void(0)" id="calcBackspace"><img border="0" src="/portal/images/eform/calculator/calc_backspace.png"></a>
						            <a href="javascript:void(0)" id="calcClear"><img border="0" src="/portal/images/eform/calculator/calc_clear.png"></a>
								</td>
							</tr>
						    <tr>
								<td><a href="javascript:void(0)" id="calc7"><img border="0" src="/portal/images/eform/calculator/calc_seven.png"></a></td>
								<td><a href="javascript:void(0)" id="calc8"><img border="0" src="/portal/images/eform/calculator/calc_eight.png"></a></td>
								<td><a href="javascript:void(0)" id="calc9"><img border="0" src="/portal/images/eform/calculator/calc_nine.png"></a></td>
								<td><a href="javascript:void(0)" id="calcDivide"><img border="0" src="/portal/images/eform/calculator/calc_divide.png"></a></td>
								<td><a href="javascript:void(0)" id="calcSqrt"><img border="0" src="/portal/images/eform/calculator/calc_sqrt.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="calc4"><img border="0" src="/portal/images/eform/calculator/calc_four.png"></a></td>
								<td><a href="javascript:void(0)" id="calc5"><img border="0" src="/portal/images/eform/calculator/calc_five.png"></a></td>
								<td><a href="javascript:void(0)" id="calc6"><img border="0" src="/portal/images/eform/calculator/calc_six.png"></a></td>
								<td><a href="javascript:void(0)" id="calcMult"><img border="0" src="/portal/images/eform/calculator/calc_multiply_star.png"></a></td>
								<td><a href="javascript:void(0)" id="calcPercent"><img border="0" src="/portal/images/eform/calculator/calc_percent.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="calc1"><img border="0" src="/portal/images/eform/calculator/calc_one.png"></a></td>
								<td><a href="javascript:void(0)" id="calc2"><img border="0" src="/portal/images/eform/calculator/calc_two.png"></a></td>
								<td><a href="javascript:void(0)" id="calc3"><img border="0" src="/portal/images/eform/calculator/calc_three.png"></a></td>
								<td><a href="javascript:void(0)" id="calcSubt"><img border="0" src="/portal/images/eform/calculator/calc_minus.png"></a></td>
								<td><a href="javascript:void(0)" id="calcAdd"><img border="0" src="/portal/images/eform/calculator/calc_plus.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="calcFlip"><img border="0" src="/portal/images/eform/calculator/calc_plus_minus.png"></a></td>
								<td><a href="javascript:void(0)" id="calc0"><img border="0" src="/portal/images/eform/calculator/calc_zero.png"></a></td>
								<td><a href="javascript:void(0)" id="calcDot"><img border="0" src="/portal/images/eform/calculator/calc_decimal.png"></a></td>
								<td><a href="javascript:void(0)" id="calcLeftP"><img border="0" src="/portal/images/eform/calculator/calc_paren_left.png"></a></td>
								<td><a href="javascript:void(0)" id="calcRightP"><img border="0" src="/portal/images/eform/calculator/calc_paren_right.png"></a></td>
							</tr>
						</table>
				</div>
			</div>

			<div id="dateTimeConversionFactorDiv">
				<div class="row clearfix">
					<div class="col-md-12" >
						<label>Date-Time Conversion Factor:</label>
					</div>
				</div>
				<div class="row clearfix">
					<div class="col-md-12" >
						<select id="conversionFactor" name="conversionFactor">
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
			</div>

		</div>

		<div class="col-md-8" >
				<div class="row clearfix">
					<div class="col-md-12" >
						<label>Calculation Rule:</label>
					</div>
				</div>
				<div class="row clearfix">
					<div class="col-md-12" >
						<div id="calculationDisplay" name="calculationDisplay" style="border:1px solid; height: 140px; overflow-y: auto;">
						</div>
					</div>
				</div>
				<div class="row clearfix">
					<div class="col-md-12" >

							<input type="checkbox" id="conditionalForCalc" name="conditionalForCalc" {{#if conditionalForCalc}}checked="checked"{{/if}} />
					

							<label  id="conditionalForCalcLabel" for="conditionalForCalc">Do not calculate if any preceding component data elements are left blank or have a Permissible Value with an assigned score of 555 (Do Not Score)</label>

						
					</div>
				</div>
				
		</div>
	</div>



</div >
</script>

<script id="editQuestionCalculationRuleTabForCAT" type="text/x-handlebars-template">
<div class="tabcontainer" id="dialog_editQuestion_calculationRule">


<div class="row clearfix">

<div class='col-md-6'>

	<div class='ibisMessaging-message ibisMessaging-primary ibisMessaging-error'>This instrument is a computer adaptive test (CAT), so its questions are generated dynamically based on answers given</br>The calculation rule can not be applied to this form.

</div>
</script>