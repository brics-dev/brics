<%@include file="/common/taglibs.jsp"%>
<%@page language="java" import="java.util.*"%>

<div class="clear-float">
	<h1 class="float-left">Access Request and Response Example!</h1>
</div>
<div class="border-wrapper wide">
	<div id="main-content">
		<span style="color: #ff0000;"> <b>Request: </b><%=request%><br> <b>Response: </b><%=response%><br> <b>Date:
		</b><%=new Date()%> <br> <b>SM_USER: </b> <s:property value="headerInfo.sm_user" /><br> <b>USER_UPN: </b> <s:property
				value="headerInfo.user_upn" /><br>
		</span>
	</div>
</div>
