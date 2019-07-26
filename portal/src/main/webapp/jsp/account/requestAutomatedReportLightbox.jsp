<%@include file="/common/taglibs.jsp"%>

<div class="flex-column-evenly" style="height: 250px;">
	<p>Here you can manage the frequency of emails containing list
	of the accounts in the selected status(es) for the email address associated with your account
	</p>
<form>
	<div style="width: 60%">
		<h5>Status:</h5>
		<div class="flex-vertical">
			<span class="flex-checkbox-left">
				<input id="requested" type="checkbox" class="statusCheckbox" name="accountStatus" value="REQUESTED" ${fn:contains(currentEmailReportSetting.accountStatusEnums, 'REQUESTED')? 'checked="checked"' : ''}/>
				<label for="requested">Requested</label>
			</span>
			<span class="flex-checkbox-left">
				<input id="pending" type="checkbox" class="statusCheckbox" name="accountStatus" value="PENDING" ${fn:contains(currentEmailReportSetting.accountStatusEnums, 'PENDING')? 'checked="checked"' : ''}/>
				<label for="pending">Pending</label>
			</span>
			<span class="flex-checkbox-left">
				<input id="changeRequested" type="checkbox" class="statusCheckbox" name="accountStatus" value="CHANGE_REQUESTED" ${fn:contains(currentEmailReportSetting.accountStatusEnums, 'CHANGE_REQUESTED')? 'checked="checked"' : ''}/>
				<label for="changeRequested">Change Requested</label>
			</span>
			<span id="selectStatusError" style="display: none">
									<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
									<span class="required" ><strong>Select one or more statuses</strong></span>
					 </span>
		</div>
	</div>

	<div style="width: 60%">
		<h5>Frequency:</h5>
		<div class="flex-vertical">
	 		<span class="flex-checkbox-left">
	 			<input id="daily" type="radio" name="frequency" value="DAILY" ${currentEmailReportSetting.frequency == 'DAILY'? 'checked="checked"' : ''}/>
	 			<label for="daily">Daily</label>
 			</span>
			<span class="flex-checkbox-left">
				<span class="flex-checkbox-left">
					<input id="weekly" type="radio" name="frequency" value="weekly" id="weekly" />
					<label for="weekly">Weekly</label>
				</span>
				<span class="flex-checkbox-left">
					<span class="flex-checkbox-horizontal">
						<input id="monday" type="radio" name="frequencyWeekly" value="MONDAY" disabled ="disabled" class="weekDays" ${currentEmailReportSetting.frequency == 'MONDAY'? 'checked="checked"' : ''}/>
						<label for="monday">M</label>
					</span>
			 		<span class="flex-checkbox-horizontal">
			 			<input id="tuesday" type="radio" name="frequencyWeekly" value="TUESDAY" disabled ="disabled" class="weekDays" ${currentEmailReportSetting.frequency == 'TUESDAY'? 'checked="checked"' : ''}/>
			 			<label for="tuesday">T</label>
		 			</span>
					<span class="flex-checkbox-horizontal">
						<input id="wednesday" type="radio" name="frequencyWeekly" value="WEDNESDAY" disabled ="disabled" class="weekDays" ${currentEmailReportSetting.frequency == 'WEDNESDAY'? 'checked="checked"' : ''} />
						<label for="wednesday">W</label>
					</span>
					<span class="flex-checkbox-horizontal">
						<input id="thursday" type="radio" name="frequencyWeekly" value="THURSDAY" disabled ="disabled" class="weekDays"/ ${currentEmailReportSetting.frequency == 'THURSDAY'? 'checked="checked"' : ''}>
						<label for="thursday">Th</label>
					</span>
			 		<span class="flex-checkbox-horizontal">
			 			<input id="friday" type="radio" name="frequencyWeekly" value="FRIDAY" disabled ="disabled" class="weekDays" ${currentEmailReportSetting.frequency == 'FRIDAY'? 'checked="checked"' : ''} />
			 			<label for="friday">F</label>
			 		</span>
			 	</span>
		 	</span>
			<span class="flex-checkbox-left">
				<input id="monthly" type="radio" name="frequency" value="MONTHLY" ${currentEmailReportSetting.frequency == 'MONTHLY'? 'checked="checked"' : ''} />
				<label for="monthly">Monthly(1st of every month)</label>
			</span>
		 	<span class="flex-checkbox-left">
		 		<input id="none" type="radio" name="frequency" value="none" ${currentEmailReportSetting == null? 'checked="checked"' : ''} />
		 		<label for="none">None(Default)</label>
	 		</span>
	 	</div>
	</div>
</form>
</div>

 <script>
 $(document).ready(function () {
	 
	 //it makes sense to enable the weekdays if one of the options is selected during pre-poplulation
	 
	 if( $(".weekDays").is(":checked")  ){
		 $(".weekDays").removeAttr('disabled');
		 $("#weekly").attr("checked",true);
	 }
	 
	 $("input[name=frequency]").change(function(){
		 if($('input:radio[name=frequency][value=weekly]').is(":checked")){
			 $(".weekDays").removeAttr('disabled');
			 //also default weekly to monday if none selected
			  if(!$('input:radio[name=frequencyWeekly]').is(":checked")) {
				  $("#monday").attr("checked",true);
			  }
			 
		 }
		 else{
			 $(".weekDays").attr('disabled', 'disabled');
			 $(".weekDays").attr('checked', false);
		 }
	 });
	 
 });
  
 </script>
