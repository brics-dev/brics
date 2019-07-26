<%@include file="/common/taglibs.jsp"%>

<h3>Electronic Signature</h3>
<p>The system uses electronic documentation which may require you to provide an electronic signature when you enter, 
submit, change, access, download, or audit electronic data records.</p>
<p><strong>ELECTRONIC SIGNATURE.</strong>&nbsp; 
	This Acknowledgement and Certification of Understanding ("Acknowledgement") is to inform you that by submitting 
	an electronic signature, you are providing an electronic mark that is held to the same standard as a legally 
	binding equivalent of a handwritten signature provided by you.&nbsp; For purposes of the acknowledgement, a digital 
	mark is considered your legally typed First and Last Name (legal name may include middle name, initial or suffix) 
	followed by your password. A date will be recorded with both entries.&nbsp; Any part of the system requiring an 
	electronic signature may contain a signature acknowledgment statement provided in the same area requiring the 
	electronic signature.&nbsp;&nbsp;</p>
<p><strong>AGREEMENT:</strong> 
	By signing this Acknowledgement, I agree that my electronic signature is the legally binding equivalent to my 
	handwritten signature.&nbsp; Whenever I execute an electronic signature, it has the same validity and meaning as 
	my handwritten signature.&nbsp; I will not, at any time in the future, repudiate the meaning of my electronic 
	signature or claim that my electronic signature is not legally binding. I also understand that it is a violation 
	for any individual to sign/e-sign any transactions that occur within system on behalf of me. Any fraudulent 
	activities related to electronic signatures must be immediately reported to the system operations team. Violation 
	of these terms could lead to disciplinary action, up to termination, and prosecution under applicable Federal laws.</p>
<p><strong>CERTIFICATION OF UNDERSTANDING:</strong>&nbsp; I also understand, acknowledge, agree and certify that:</p>
<ul>
	<li>I accept my responsibilities in the use of electronic signatures as described on this form.</li>
	<li>My execution of any form of an electronic signature function performed on the system to be the legally binding 
	equivalent of my traditional handwritten signature, and that I am accountable and responsible for actions performed 
	under such an electronic signature.</li>
	<li>I will not share components of my electronic signature such that my signature could be executed by another 
	individual. Such components may include, but are not limited to legal name, passwords or any such identifiers.</li>
</ul>

<div style="display:flex; justify-content:space-evenly; flex-wrap:nowrap">
	<div class="form-field" style="width: 22%; float: left; display:flex;">
			<label for="firstName" class="required">First Name<span class="required">* </span></label>
		<div>
			<s:textfield id="firstName" name="accountSignatureForm.firstName" maxlength="55" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountSignatureForm.firstName" />
		</div>
	</div>
	<div class="form-field" style="width: 22%; float: left; display:flex;">
		<label for="middleName">Middle Name</label>
		<div>
			<s:textfield id="middleName" name="accountSignatureForm.middleName"  maxlength="55" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountSignatureForm.middleName" />
		</div>
	</div>
	<div class="form-field" style="width: 22%; float: left; display:flex;">
		<label for="lastName" class="required" style="float: left;">Last Name<span class="required">* </span></label>
		<div>
			<s:textfield id="lastName" name="accountSignatureForm.lastName" maxlength="55" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="accountSignatureForm.lastName" />
		</div>
	</div>
	<div class="form-field" style="width: 30%; float: left; display:flex;">
		<label for="password" class="required" style="float: left;">Password<span class="required">* </span></label>
		<div>
			<s:password id="password" name="accountSignatureForm.password" maxlength="55" escapeHtml="true" escapeJavaScript="true" autocomplete="new-password" />
			<s:fielderror fieldName="accountSignatureForm.password" />
		</div>
	</div>
	
</div>
<br/><br/>
