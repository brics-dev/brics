<script id="editQuestionValidationTabLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_validation">Validation</a></li>
</script>



<script id="editQuestionValidationTab" type="text/x-handlebars-template">
	<div class="tabcontainer" id="dialog_editQuestion_validation">
		<div class="row clearfix">
			<div class="col-md-2">
				<label for="answerRequired">
					Answer Required
				</label>
			</div>
			<div class="col-md-2">
				<div id="required"></div>
			</div>
			<div class="col-md-2 doNotShowForOnlyRequired">			
					<label for="answerType">
						Answer Type<span class="requiredStar">*</span>	
					</label>		
			</div>
			<div class="col-md-2 doNotShowForOnlyRequired">
				<select name="answerType" id="answerTypeId" >
				 	<option value="1">String</option>
				 	<option value="2">Numeric</option>
					<option value="3">Date</option>
					<option value="4">Date-Time</option>
				</select>
			</div>	
			
		</div>
		<div class="row clearfix stringSelectionDependent">
			<div class="col-md-2">				
				<label for="minChar">Minimum Characters</label>				
			</div>
			<div class="col-md-2">
				<div id="minCharacters" name="minCharacters"></div>
			</div>

			<div class="col-md-2">				
				<label for="maxChar">Maximum Characters</label>			
			</div>
			<div class="col-md-2">
				<div id="maxCharacters" name="maxCharacters"></div>
			</div>
			
		</div>

		<div class="row numericDependent clearfix">
			<div class="col-md-2 hideThisForTextArea">
				<label for="subDecPrec">
					Submitted Decimal Precision
				</label>
			</div>
			<div class="col-md-2 hideThisForTextArea">
				<select name="decimalPrecision">
				 	<option value="-1">Actual Value</option>
				 	<option value="0">0</option>
					<option value="1">1</option>
					<option value="2">2</option>
					<option value="3">3</option>
					<option value="4">4</option>
					<option value="5">5</option>
					<option value="6">6</option>
					<option value="7">7</option>
					<option value="8">8</option>
				</select>
			</div>

			<div class="col-md-2">				
					<label for="rangeOperator">Answer Must Be:</label>				
			</div>
			<div class="col-md-2">
				<div id="rangeOperator"></div>
			</div>
			
			
		</div>
		
	</div>
</script>