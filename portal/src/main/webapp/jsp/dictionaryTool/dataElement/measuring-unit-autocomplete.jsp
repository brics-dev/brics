<%@include file="/common/taglibs.jsp"%>

<div class="form-field form-field-vert">
	<label for="valueRangeForm.measurementType">Unit of Measure:</label> 
	
	<s:select id="theForm_valueRangeForm_measuringUnit" class="autocomplete" list="measuringUnitList" name="valueRangeForm.measuringUnit" listKey="displayName"
						listValue="displayName" value="valueRangeForm.measuringUnit" headerKey="Begin typing a unit of measure" headerValue="Begin typing a unit of measure"/> <a href="javascript:clear()">clear</a>
	<div class="special-instruction">If you cannot find the desired unit of measurement, please contact the operations team.</div>
	<s:fielderror fieldName="valueRangeForm.measuringUnit" />
</div>

<script src="/portal/js/jquery.select-to-autocomplete.js"></script>

<script type="text/javascript">
(function( $ ){
    $(function(){
      $( '#theForm_valueRangeForm_measuringUnit' ).selectToAutocomplete();
      $(".ui-autocomplete-input").addClass("textfield");
    });
  })( jQuery );
  
  function clear() {
	  $(".ui-autocomplete-input").val('Enter a unit of measure');
	  $("#theForm_valueRangeForm_measuringUnit").val('Begin typing a unit of measure');
  }
   </script>