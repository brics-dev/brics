<script id="editQuestionBtrisMappingTabLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_BtrisMapping">BTRIS</a></li>
</script>

<script id="editQuestionBtrisMappingTab" type="text/x-handlebars-template">
	<div class="tabcontainer" id="dialog_editQuestion_BtrisMapping">

		<div class="row  clearfix">
			<div class="col-md-4">
				<label for="btrisMapping" title="Please check the checkbox if you would like to get the value from BTRIS.">
					Get Value from BTRIS: 
				</label>
			</div>
			<div class="col-md-2">
				<input type="checkbox" id="getBtrisValue" />
			</div>
		</div>
		<div class="row clearfix">
			<div class="col-md-12" >
				<div class="row clearfix">
					<div class="col-md-4">
						<label for="btrisMappingInfo">
							BTRIS Mapping Information: 
						</label>
					</div>
					<div class="col-md-8" >
						<table border="1">
							<tr>
								<th style="padding: 5px;">Observation Name</th>
								<th style="padding: 5px;">Red Concept Code</th>
								<th style="padding: 5px;">Specimen Type</th>
							</tr>
							<tr>
								<td style="padding: 5px;"><span id="btrisObservationName"></span></td>
								<td style="padding: 5px;"><span id="btrisRedCode"></span></td>
								<td style="padding: 5px;"><span id="btrisSpecimenType"></span></td>
							</tr>
						</table>
					</div>
				</div>			
			</div>
		</div>
</div>
</script>