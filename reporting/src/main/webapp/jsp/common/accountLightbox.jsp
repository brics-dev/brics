<%@include file="/common/taglibs.jsp"%>
<div id="main-content">
	<h3>Account Information: <s:property value="currentAccount.displayName" /></h3>
	<div class="form-output">
		<div class="label">First Name:</div>
		<div class="readonly-text">
			<s:property value="currentAccount.user.firstName" />
		</div>
	</div>
	<div class="form-output">
		<div class="label">Last Name:</div>
		<div class="readonly-text">
			<s:property value="currentAccount.user.lastName" />
		</div>
	</div>
	<div class="form-output">
		<div class="label">Email Address:</div>
		<div class="readonly-text">
			<s:property value="currentAccount.user.email" />
		</div>
	</div>


</div>