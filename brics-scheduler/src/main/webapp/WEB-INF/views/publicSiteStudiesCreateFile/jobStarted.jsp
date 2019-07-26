<%@page contentType="text/html;charset=UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@page pageEncoding="UTF-8"%>
<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
<head>
	<META http-equiv="Content-Type" content="text/html;charset=UTF-8">
	<title><fmt:message key="welcome.title"/></title>
	<link rel="stylesheet" href="<c:url value="/resources/blueprint/screen.css" />" type="text/css" media="screen, projection">
	<link rel="stylesheet" href="<c:url value="/resources/blueprint/print.css" />" type="text/css" media="print">
	<!--[if lt IE 8]>
		<link rel="stylesheet" href="<c:url value="/resources/blueprint/ie.css" />" type="text/css" media="screen, projection">
	<![endif]-->
</head>
<body>
<div class="container">
	<h1>
		<%-- <fmt:message key="scheduler.title"/> --%>
	</h1>
	<hr>	
		Public Site Studies Create File Job started
</div>

<!-- <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.1/jquery.min.js"></script> 

<script type="text/javascript">  
      
    $(document).ready(function() {  
          
        // Check The Status Every 2 Seconds  
        var timer = setInterval(function() {  
              
            $.ajax({  
                  url: 'reportstatus.json',  
                  success: function(data) {  
                      
                    if(data === 'COMPLETE') {  
                        $('#reportLink').html("<a target='_target' href='report.html'>Download Report</a>");      
                        clearInterval(timer);  
                    }  
                  }  
            });  
              
        }, 2000);  
    });  
          
</script>

<div id="reportLink">Please wait while the rdf job is running. Once the job is complete you can see the result here</div>   -->

</body>
</html>