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
			</div>
		
		<div class="col-md-4">
			<div class="row clearfix">
				<div class="col-md-6">
					<label for="align" class="required">Email Recipient</label>
				</div>
				<div class="col-md-6">
					<input type="textbox" name="toEmailAddress" id="toEmailAddress" />
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-6">
					<label for="align">CC Email Recipient</label>
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
					<textarea name="body" id="body" rows="5" cols="15" maxlength="4000"/>
				</div>
			</div>
		</div>
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
	</div>
 <div class="clearfix">
  </div>
</div>

</script>