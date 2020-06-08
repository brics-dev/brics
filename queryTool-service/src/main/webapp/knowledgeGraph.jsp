<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import ="gov.nih.tbi.constants.ApplicationConstants" %>
<jsp:useBean id="bean" class="gov.nih.tbi.constants.ApplicationConstants"></jsp:useBean>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Knowledge Graph</title>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script type="text/javascript" src="js/lib/d3.v4.js"></script>
<script type="text/javascript" src="js/lib/array_find_polyfill.js"></script>

<style>
body {
  box-sizing: border-box;
}

.links line {
  stroke: #999;
  stroke-opacity: 0.6;
  stroke-width: 2px;
}

.nodes circle {
  stroke: #fff;
  stroke-width: 1.5px;
}

.content {
	width: 100%;
	position: relative;
	flex: 1;
}

.infoPane {
	float: right;
  width: 160px;
  height: calc(95vh - 20px);
	margin: 0 10px;
  padding: 6px;
  border: 1px solid #bbb;
  border-radius: 8px;
}

.infoText {
  margin-top: 10px;
}

.helpPane {
  position: absolute;
  top: 100px;
  left: 250px;
  padding: 15px;
  background-color: white;
  display: flex;
  border: 1px solid #bbb;
  border-radius: 8px;
}

.helpPaneForms, .helpPaneCdes {
  flex: 1 1 auto;
}

text {
  font-family: sans-serif;
  font-size: 14px;
}

svg text {
  user-select: none;
  -moz-user-select: none;
}
.kgHeader {
	display: flex;
	justify-content: space-between;
	font-size: larger;
	font-weight: bold;
	flex: 0;
}
.kgFooter {
	width: calc(100% - 300px);
	margin-right: 300px;
	text-align: right;
	color: darkblue;
	flex: 0;
}
.kgContent {
	display: flex;
	flex-direction: column;
	height: calc(100vh - 3em);
}


.kgHelpButton {
  float: right;
  cursor: pointer;
}
.kgHelpButton:hover {
  background-color: #ddd;
}
 </style>

</head>

<body>
<jsp:getProperty name="bean" property="modulesQTURL" />
<br>
<div style="display:none" class="errorContent">
Knowledge Graph Information Unavailable
</div>
<div class="kgContent">
 <div class="kgHeader">
 	<div style="width: 300px;text-align: right;">Forms</div>
 	<div>Common Data Elements</div>
 	<div style="width: 300px;text-align: left;">Forms</div>
 </div>
  <div class="content"></div>
  <div class="kgFooter">
	Ctrl-click for links list, double-click for full info page<br>
	<span style="font-style: italic">Node diameter is proportional to the number of linked forms and data elements.</span>
  </div>
</div>
<script type="text/javascript" src="js/QueryTool.js"></script>
<script type="text/javascript" src="js/dataTable/views/KnowledgeGraphView.js"></script>
</body>
</html>