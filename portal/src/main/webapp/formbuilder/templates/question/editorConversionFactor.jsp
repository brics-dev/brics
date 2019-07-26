<script id="editQuestionConversionFactorTabLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_conversionFactor">Conversion Factor</a></li>
</script>

<script id="editQuestionConversionFactorTab" type="text/x-handlebars-template">
<div class="tabcontainer" id="dialog_editQuestion_conversionFactor">
	
	<div class="row clearfix">
		
		<div class="col-md-2">


				<div class="row clearfix">
					<div class="col-md-12" >
						<label>Question to convert:</label>
					</div>
				</div>
				<div class="row clearfix">
					<div class="col-md-12" >
						<select id="questionConv" multiple style="height: 140px; overflow: scroll;border: solid 1px">
								<option value="this">This Question</option>
						</select>
					</div>
				</div>
							
							
		</div>
		<div class="col-md-2">
						<table>
							<tr width="100%">
								<td colspan="5" width="100%">
									<a href="javascript:void(0)" id="convBackspace"><img border="0" src="/portal/images/eform/calculator/calc_backspace.png"></a>
						            <a href="javascript:void(0)" id="convClear"><img border="0" src="/portal/images/eform/calculator/calc_clear.png"></a>
								</td>
							</tr>
						    <tr>
								<td><a href="javascript:void(0)" id="conv7"><img border="0" src="/portal/images/eform/calculator/calc_seven.png"></a></td>
								<td><a href="javascript:void(0)" id="conv8"><img border="0" src="/portal/images/eform/calculator/calc_eight.png"></a></td>
								<td><a href="javascript:void(0)" id="conv9"><img border="0" src="/portal/images/eform/calculator/calc_nine.png"></a></td>
								<td><a href="javascript:void(0)" id="convDivide"><img border="0" src="/portal/images/eform/calculator/calc_divide.png"></a></td>
								<td><a href="javascript:void(0)" id="convSqrt"><img border="0" src="/portal/images/eform/calculator/calc_sqrt.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="conv4"><img border="0" src="/portal/images/eform/calculator/calc_four.png"></a></td>
								<td><a href="javascript:void(0)" id="conv5"><img border="0" src="/portal/images/eform/calculator/calc_five.png"></a></td>
								<td><a href="javascript:void(0)" id="conv6"><img border="0" src="/portal/images/eform/calculator/calc_six.png"></a></td>
								<td><a href="javascript:void(0)" id="convMult"><img border="0" src="/portal/images/eform/calculator/calc_multiply_star.png"></a></td>
								<td><a href="javascript:void(0)" id="convPercent"><img border="0" src="/portal/images/eform/calculator/calc_percent.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="conv1"><img border="0" src="/portal/images/eform/calculator/calc_one.png"></a></td>
								<td><a href="javascript:void(0)" id="conv2"><img border="0" src="/portal/images/eform/calculator/calc_two.png"></a></td>
								<td><a href="javascript:void(0)" id="conv3"><img border="0" src="/portal/images/eform/calculator/calc_three.png"></a></td>
								<td><a href="javascript:void(0)" id="convSubt"><img border="0" src="/portal/images/eform/calculator/calc_minus.png"></a></td>
								<td><a href="javascript:void(0)" id="convAdd"><img border="0" src="/portal/images/eform/calculator/calc_plus.png"></a></td>
							</tr>
							<tr>
								<td><a href="javascript:void(0)" id="convFlip"><img border="0" src="/portal/images/eform/calculator/calc_plus_minus.png"></a></td>
								<td><a href="javascript:void(0)" id="conv0"><img border="0" src="/portal/images/eform/calculator/calc_zero.png"></a></td>
								<td><a href="javascript:void(0)" id="convDot"><img border="0" src="/portal/images/eform/calculator/calc_decimal.png"></a></td>
								<td><a href="javascript:void(0)" id="convLeftP"><img border="0" src="/portal/images/eform/calculator/calc_paren_left.png"></a></td>
								<td><a href="javascript:void(0)" id="convRightP"><img border="0" src="/portal/images/eform/calculator/calc_paren_right.png"></a></td>
							</tr>
						</table>
		</div>

		<div class="col-md-8" >
				<div class="row clearfix">
					<div class="col-md-12" >
						<label>Conversion Factor:</label>
					</div>
				</div>
				<div class="row clearfix">
					<div class="col-md-12" >
						<div id="unitConversionFactor" name="unitConversionFactor" style="border:1px solid; height: 140px">
						</div>
					</div>
				</div>							
		</div>
	</div>
</div >
</script>


<script id="editQuestionConversionFactorTabForCAT" type="text/x-handlebars-template">
<div class="tabcontainer" id="dialog_editQuestion_conversionFactor">


<div class="row clearfix">

<div id='skipRuleContainer1' class='col-md-6'>

	<div class='ibisMessaging-message ibisMessaging-primary ibisMessaging-error'>This instrument is a computer adaptive test (CAT), so its questions are generated dynamically based on answers given</br>The conversion factor can not be applied to this form.</div>

</div>
</script>