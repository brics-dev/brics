<%@ taglib uri="/struts-tags" prefix="s"%>

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
						<select id="questionCalc" multiple">
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
								<td colspan="9" width="100%">
									<a href="javascript:void(0)" id="calcBackspace"><img border="0" src="<s:property value="#webRoot"/>/images/calc_backspace.png"></a>
						            <a href="javascript:void(0)" id="calcClear"><img border="0" src="<s:property value="#webRoot"/>/images/calc_clear.png" ></a>
								</td>
							</tr>
						    <tr>
								<td><a href="javascript:void(0)" id="calc7"><img border="0" src="<s:property value="#webRoot"/>/images/calc_seven.png"></a></td>
								<td><a href="javascript:void(0)" id="calc8"><img border="0" src="<s:property value="#webRoot"/>/images/calc_eight.png"></a></td>
								<td><a href="javascript:void(0)" id="calc9"><img border="0" src="<s:property value="#webRoot"/>/images/calc_nine.png"></a></td>
								<td><a href="javascript:void(0)" id="calcDivide"><img border="0" src="<s:property value="#webRoot"/>/images/calc_divide.png"></a></td>
								<td><a href="javascript:void(0)" id="calcSqrt"><img border="0" src="<s:property value="#webRoot"/>/images/calc_sqrt.png"></a></td>								
								<td><a href="javascript:void(0)" id="calcEqualEqual"><img border="0" src="<s:property value="#webRoot"/>/images/calc_equal_equal.png"></a></td>
								<td><a href="javascript:void(0)" id="calcNotEqual"><img border="0" src="<s:property value="#webRoot"/>/images/calc_not_equal.png"></a></td>
								<td colspan="2"><a href="javascript:void(0)" id="calcIf"><img border="0" src="<s:property value="#webRoot"/>/images/calc_if.png"></a></td>								
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="calc4"><img border="0" src="<s:property value="#webRoot"/>/images/calc_four.png"></a></td>
								<td><a href="javascript:void(0)" id="calc5"><img border="0" src="<s:property value="#webRoot"/>/images/calc_five.png"></a></td>
								<td><a href="javascript:void(0)" id="calc6"><img border="0" src="<s:property value="#webRoot"/>/images/calc_six.png"></a></td>
								<td><a href="javascript:void(0)" id="calcMult"><img border="0" src="<s:property value="#webRoot"/>/images/calc_multiply_star.png"></a></td>
								<td><a href="javascript:void(0)" id="calcPercent"><img border="0" src="<s:property value="#webRoot"/>/images/calc_percent.png"></a></td>
								<td><a href="javascript:void(0)" id="calcGreater"><img border="0" src="<s:property value="#webRoot"/>/images/calc_greater.png"></a></td>
								<td><a href="javascript:void(0)" id="calcGreaterEqual"><img border="0" src="<s:property value="#webRoot"/>/images/calc_greater_equal.png"></a></td>
								<td colspan="2"><a href="javascript:void(0)" id="calcElse"><img border="0" src="<s:property value="#webRoot"/>/images/calc_else.png"></a></td>								
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="calc1"><img border="0" src="<s:property value="#webRoot"/>/images/calc_one.png"></a></td>
								<td><a href="javascript:void(0)" id="calc2"><img border="0" src="<s:property value="#webRoot"/>/images/calc_two.png"></a></td>
								<td><a href="javascript:void(0)" id="calc3"><img border="0" src="<s:property value="#webRoot"/>/images/calc_three.png"></a></td>
								<td><a href="javascript:void(0)" id="calcSubt"><img border="0" src="<s:property value="#webRoot"/>/images/calc_minus.png"></a></td>
								<td><a href="javascript:void(0)" id="calcAdd"><img border="0" src="<s:property value="#webRoot"/>/images/calc_plus.png"></a></td>
								<td><a href="javascript:void(0)" id="calcLesser"><img border="0" src="<s:property value="#webRoot"/>/images/calc_lesser.png"></a></td>
								<td><a href="javascript:void(0)" id="calcLesserEqual"><img border="0" src="<s:property value="#webRoot"/>/images/calc_lesser_equal.png"></a></td>
								<td colspan="2"><a href="javascript:void(0)" id="calcElseIf"><img border="0" src="<s:property value="#webRoot"/>/images/calc_else_if.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="calcFlip"><img border="0" src="<s:property value="#webRoot"/>/images/calc_plus_minus.png"></a></td>
								<td><a href="javascript:void(0)" id="calc0"><img border="0" src="<s:property value="#webRoot"/>/images/calc_zero.png"></a></td>
								<td><a href="javascript:void(0)" id="calcDot"><img border="0" src="<s:property value="#webRoot"/>/images/calc_decimal.png"></a></td>
								<td><a href="javascript:void(0)" id="calcLeftP"><img border="0" src="<s:property value="#webRoot"/>/images/calc_paren_left.png"></a></td>
								<td><a href="javascript:void(0)" id="calcRightP"><img border="0" src="<s:property value="#webRoot"/>/images/calc_paren_right.png"></a></td>
								<td><a href="javascript:void(0)" id="calcAnd"><img border="0" src="<s:property value="#webRoot"/>/images/calc_and.png"></a></td>
								<td><a href="javascript:void(0)" id="calcOr"><img border="0" src="<s:property value="#webRoot"/>/images/calc_or.png"></a></td>
								<td colspan="2"><a href="javascript:void(0)" id="calcOpenBrac"><img border="0" src="<s:property value="#webRoot"/>/images/calc_open_brac.png"></a><a href="javascript:void(0)" id="calcCloseBrac"><img border="0" src="<s:property value="#webRoot"/>/images/calc_close_brac.png"></a></td>
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
				
				
		</div>
	</div>



</div >







</script>