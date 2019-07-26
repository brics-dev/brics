<script id="refineDataCart" type="text/x-handlebars-template">
<div class="refineDataDataCartHeader">
	<div class="refineData_dataCartLabel">
		Data Cart
		<span class="pe-is-ec-cart-1"></span>
	</div>
	<div class="dataCartButton buttonWithIcon moveDataCartToQueue">
		<span class="icon pe-is-i-inside"></span>
		<a href="javascript:;">Download Data Cart To Queue</a>
	</div>
</div>
<div style="float: left; width: 50%;">
	<div style=" width: 100%;">Select a form to refine your query</div>
	<div class="refineDataFormContainer droppable" style="width: 100%;"></div>
</div>
<div class="refineDataJoinContainer">
	<div class="dataCartActiveFormsHeader">
		<div class="dataCartActiveFormsHeaderContent">
			Drag here to join forms
		</div>
	</div>
	<div class="dataCartActiveFormsContainer">
		<div class="droppable formJoinDropArea" id="firstForm">First Form</div>
		<div class="droppable formJoinDropArea" id="secondForm">Second Form</div>
		<div class="droppable formJoinDropArea" id="thirdForm">Third Form</div>
		<div class="droppable formJoinDropArea" id="fourthForm">Fourth Form</div>
		<div class="droppable formJoinDropArea" id="fifthForm">Fifth Form</div>
	</div>
	<div class="dataCartActiveFormsButtonsContainer">
		<a href="javascript:;" class="buttonSecondary buttonWithIcon disabled" id="joinFormsButton">
			<span class="icon brics-qt-32232"></span>
			Join
		</a>
		<a href="javascript:;" class="buttonSecondary buttonWithIcon" id="resetFormsButton">
			<span class="icon pe-is-i-loop-1"></span>
			Reset
		</a>
		<div class="clearfix"></div>
	</div>
</div>
</script>