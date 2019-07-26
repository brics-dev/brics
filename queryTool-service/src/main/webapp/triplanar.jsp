<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<%-- <%@include file="/common/taglibs.jsp"%>  --%>
<html>
	<head>
		<meta charset="utf-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge" />
		<!-- always use the newest version of IE // disables compatibility mode -->
		<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
		
		<link type="text/css" href="css/jquery-ui.css" rel="stylesheet"
			media="all" />
		<link type="text/css" href="css/jquery.ibisMessaging-0.1.css"
			rel="stylesheet" />
		
		<link type="text/css" href="instances/pdbp/jquery-ui.theme.min.css"
			rel="stylesheet" />
		
		<link type="text/css" href="css/qt-glyphicons.css" rel="stylesheet" />
		
		<!--  change instance folder name for different styles -->
		<c:if test="${fn:contains(applicationConstants.styleKey, 'localhost')}">  
			<link type="text/css" href="css/scss/instances/default/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/default/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'nti')}">  
			<link type="text/css" href="css/scss/instances/default/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/default/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'pdbp')}">
			<link type="text/css" href="css/scss/instances/pdbp/style.css"
				rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/pdbp/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cistar')}">
			<link type="text/css" href="css/scss/instances/cistar/style.css"
				rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cistar/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cnrm')}">
			<link type="text/css" href="css/scss/instances/cnrm/style.css"
				rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cnrm/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'eyegene') || fn:contains(applicationConstants.styleKey, 'nei')}">
			<link type="text/css" href="css/scss/instances/eyegene/style.css"
				rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/eyegene/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'cdrns')}">  
			<link type="text/css" href="css/scss/instances/cdrns/style.css" rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/cdrns/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'fitbir')}">
			<link type="text/css" href="css/scss/instances/fitbir/style.css"
				rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/fitbir/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'ninds')}">
			<link type="text/css" href="css/scss/instances/ninds/style.css"
				rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/ninds/images/favicon.ico" rel="icon" />
		</c:if>
		<c:if test="${fn:contains(applicationConstants.styleKey, 'gsdr')}">
			<link type="text/css" href="css/scss/instances/gsdr/style.css"
				rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/gsdr/images/favicon.ico" rel="icon" />
		</c:if>
		
		<c:if test="${fn:contains(applicationConstants.styleKey, 'gsdr')}">
			<link type="text/css" href="css/scss/instances/gsdr/style.css"
				rel="stylesheet" />
			<!-- Favicon -->
			<link href="css/scss/instances/gsdr/images/favicon.ico" rel="icon" />
		</c:if>
		
		<link type="text/css" href="instances/pdbp/qt-footer.css"
			rel="stylesheet" />
		
		<!-- All library imports -->
		<script type="text/javascript" src="js/util/Util.js"></script>
		<script src="https://code.jquery.com/jquery-1.7.2.min.js" integrity="sha256-R7aNzoy2gFrVs+pNJ6+SokH04ppcEqJ0yFLkNGoFALQ=" 
			crossorigin="anonymous"></script>
		<script type="text/javascript" src="js/lib/jquery-ui.min.js"></script>
		<script type="text/javascript" src="js/lib/jquery.scrollTo.js"></script>
		<script type="text/javascript" src="js/lib/jquery.ibisMessaging-0.1.full.js"></script>
		<script type="text/javascript" src="js/lib/core_libs.min.js"></script>
		<script type="text/javascript" src="js/lib/core_classes.min.js"></script>
		
		<title>Tri-Planar Viewer</title>
	</head>

	<body>
		<header id="header" class="header">
			<div id="instanceHeaderOne" class="">
				<a href="javascript:;" class="bannerHome"> <c:if
						test="${fn:contains(applicationConstants.styleKey, 'localhost')}">
						<img name="" src="instances/pdbp/images/header.png"></img>
	
					</c:if> <c:if test="${fn:contains(applicationConstants.styleKey, 'pdbp')}">
						<img name="" src="instances/pdbp/images/header.png"></img>
	
					</c:if> <c:if
						test="${fn:contains(applicationConstants.styleKey, 'cistar')}">
						<img name="" src="instances/cistar/images/header.png"></img>
					</c:if> <c:if test="${fn:contains(applicationConstants.styleKey, 'cnrm')}">
						<img name="" src="instances/cnrm/images/header.png"></img>
					</c:if> <c:if
						test="${fn:contains(applicationConstants.styleKey, 'eyegene') || fn:contains(applicationConstants.styleKey, 'nei')}">
						<img name="" src="instances/eyegene/images/header.png"></img>
					</c:if> 
					<c:if test="${fn:contains(applicationConstants.styleKey, 'cdrns')}">  
						<img name="" src="instances/cdrns/images/header.png"></img>
					</c:if>
					<c:if test="${fn:contains(applicationConstants.styleKey, 'fitbir')}">
						<img name="" src="instances/fitbir/images/header.png"
							style="height: 39px; padding-right: 10px; background: #ececec; margin-right: 10px;"></img>
					</c:if> <c:if
						test="${fn:contains(applicationConstants.styleKey, 'ninds')}">
						<img name="" src="instances/ninds/images/header.png"></img>
					</c:if>
					<c:if
						test="${fn:contains(applicationConstants.styleKey, 'gsdr')}">
						<img name="" src="instances/ninds/images/header.png"></img>
					</c:if>
	
				</a>
			</div>
			<div id="instanceHeaderTwo" class="">
				<c:if
					test="${fn:contains(applicationConstants.styleKey, 'localhost')}">
					<span>
						<b>PDBP DMR</b> Parkinson's Disease Biomarkers Program
						Data Management Resources
					</span>
				</c:if>
				<c:if test="${fn:contains(applicationConstants.styleKey, 'pdbp')}">
					<span>
						<b>PDBP DMR</b> Parkinson's Disease Biomarkers Program
						Data Management Resources
					</span>
				</c:if>
				<c:if test="${fn:contains(applicationConstants.styleKey, 'cistar')}">
					<span></span>
				</c:if>
				<c:if test="${fn:contains(applicationConstants.styleKey, 'cnrm')}">
					<span></span>
				</c:if>
				<c:if
					test="${fn:contains(applicationConstants.styleKey, 'eyegene') || fn:contains(applicationConstants.styleKey, 'nei')}">
					<span></span>
				</c:if>
				<c:if test="${fn:contains(applicationConstants.styleKey, 'cdrns')}">  
					<span></span>
				</c:if>
				<c:if test="${fn:contains(applicationConstants.styleKey, 'fitbir')}">
					<span></span>
				</c:if>
				<c:if test="${fn:contains(applicationConstants.styleKey, 'gsdr')}">
					<span></span>
				</c:if>
				<c:if test="${fn:contains(applicationConstants.styleKey, 'ninds')}">
					<span><b>NINDS DMR</b> Parkinson's Disease Biomarkers Program
						Data Management Resources</span>
				</c:if>
			</div>
	
			<div class="clearfix"></div>
		</header>
	
		<div id="page-container" class="wrap">
			<p id="skiptargetholder" class="visuallyhidden">
				<a id="skiptarget" class="skip" tabindex="0" href="#skiptarget">Start
					of main content</a>
			</p>
	
			<div id="content-wrapper">
				<div class="content">
					<h2>Tri-Planar Viewer</h2>
	
					<div id="notificationDiv"
						style="padding-top: 15px; padding-left: 10px;">
						<span id="notificationText">Generating Tri-Planar Image ...</span><br />
						<img id="loadImg" src="images/slow_load.gif" />
					</div>
					<br />
	
					<form id="wrapper-form">
						<div id="topControls"
							style="vertical-align: middle; display: none;">
							<p>
								<a href="Javascript:reset()"
									onMouseOver="return changeSrc('home_Button', 'homeiconroll')"
									onMouseOut="return changeSrc('home_Button', 'homeicon')"> <img
									name="home_Button" src="images/triplanar/homeicon.gif"
									width="24" height="24" border="0" alt="reset" /></a> <a
									href="Javascript:CanvasScale(1)"
									onMouseOver="return changeSrc('zoomin_Button', 'zoomin2roll')"
									onMouseOut="return changeSrc('zoomin_Button', 'zoomin2')"> <img
									name="zoomin_Button" src="images/triplanar/zoomin2.gif"
									width="24" height="24" border="0" alt="bigger" /></a> <a
									href="Javascript:CanvasScale(-1)"
									onMouseOver="return changeSrc('zoomout_Button', 'zoomout2roll')"
									onMouseOut="return changeSrc('zoomout_Button', 'zoomout2')">
									<img name="zoomout_Button" src="images/triplanar/zoomout2.gif"
									width="24" height="24" border="0" alt="smaller" />
								</a> <a href="Javascript:handleLUT(0)"
									onMouseOver="return changeSrc('gray_LutButton', 'grayroll')"
									onMouseOut="return changeSrc('gray_LutButton', 'gray')"> <img
									name="gray_LutButton" src="images/triplanar/gray.gif" width="24"
									height="24" border="0" alt="gray LUT" /></a> <a
									href="Javascript:handleLUT(1)"
									onMouseOver="return changeSrc('red_LutButton', 'redlutroll')"
									onMouseOut="return changeSrc('red_LutButton', 'redlut')"> <img
									name="red_LutButton" src="images/triplanar/redlut.gif"
									width="24" height="24" border="0" alt="red LUT" /></a> <a
									href="Javascript:handleLUT(2)"
									onMouseOver="return changeSrc('green_LutButton', 'greenlutroll')"
									onMouseOut="return changeSrc('green_LutButton', 'greenlut')">
									<img name="green_LutButton" src="images/triplanar/greenlut.gif"
									width="24" height="24" border="0" alt="green LUT" />
								</a> <a href="Javascript:handleLUT(3)"
									onMouseOver="return changeSrc('blue_LutButton', 'bluelutroll')"
									onMouseOut="return changeSrc('blue_LutButton', 'bluelut')">
									<img name="blue_LutButton" src="images/triplanar/bluelut.gif"
									width="24" height="24" border="0" alt="blue LUT" />
								</a> <a href="Javascript:handleLUT(4)"
									onMouseOver="return changeSrc('graybr_LutButton', 'graybrroll')"
									onMouseOut="return changeSrc('graybr_LutButton', 'graybr')">
									<img name="graybr_LutButton" src="images/triplanar/graybr.gif"
									width="24" height="24" border="0" alt="gray blue/red LUT" />
								</a> <a href="Javascript:handleLUT(5)"
									onMouseOver="return changeSrc('hotmetal_LutButton', 'hotmetalroll')"
									onMouseOut="return changeSrc('hotmetal_LutButton', 'hotmetal')">
									<img name="hotmetal_LutButton"
									src="images/triplanar/hotmetal.gif" width="24" height="24"
									border="0" alt="hot metal LUT" />
								</a> <a href="Javascript:handleLUT(6)"
									onMouseOver="return changeSrc('spectrum_LutButton', 'spectrumroll')"
									onMouseOut="return changeSrc('spectrum_LutButton', 'spectrum')">
									<img name="spectrum_LutButton"
									src="images/triplanar/spectrum.gif" width="24" height="24"
									border="0" alt="spectrum LUT" />
								</a> <a href="Javascript:handleLUT(7)"
									onMouseOver="return changeSrc('coolhot_LutButton', 'coolhotroll')"
									onMouseOut="return changeSrc('coolhot_LutButton', 'coolhot')">
									<img name="coolhot_LutButton" src="images/triplanar/coolhot.gif"
									width="24" height="24" border="0" alt="cool hot LUT" />
								</a> <a href="Javascript:handleLUT(8)"
									onMouseOver="return changeSrc('skin_LutButton', 'skinroll')"
									onMouseOut="return changeSrc('skin_LutButton', 'skin')"> <img
									name="skin_LutButton" src="images/triplanar/skin.gif" width="24"
									height="24" border="0" alt="skin LUT" /></a> <a
									href="Javascript:handleLUT(9)"
									onMouseOver="return changeSrc('bone_LutButton', 'boneroll')"
									onMouseOut="return changeSrc('bone_LutButton', 'bone')"> <img
									name="bone_LutButton" src="images/triplanar/bone.gif" width="24"
									height="24" border="0" alt="bone LUT" /></a>
							</p>
						</div>
	
						<input type="hidden" id="studyName" name="studyName"
							value="%{param.studyName}"> <input type="hidden"
							id="datasetName" name="datasetName" value="%{param.datasetName}">
						<input type="hidden" id="triplanarName" name="triplanarName"
							value="%{param.triplanarName}">
	
						<div id="triView" style="display: none; padding-left: 10px;">
							<p>
								<!-- No right-click menu: oncontextmenu="return false" get drag events, not needed: draggable="true" -->
								<canvas id="AxialCanvas" style="border: 1px solid red"
									width="200" height="200" oncontextmenu="return false"></canvas>
								<canvas id="SagittalCanvas" style="border: 1px solid green"
									width="200" height="200" oncontextmenu="return false"></canvas>
								<canvas id="CoronalCanvas" style="border: 1px solid yellow"
									width="200" height="200" oncontextmenu="return false"></canvas>
							</p>
						</div>
						<div id="timeControls" style="display: none;">
							<p>
								<a href="Javascript:playPause()"><img name="play_Button"
									src="images/triplanar/play.gif" width="24" height="24"
									border="0" alt="play/pause" /></a> <input id="time4D" type="range"
									min="0" max="9" step="1" oninput="showTime(this.value)"
									onchange="showTime(this.value)" /> speed: <a
									href="Javascript:setDelay(-1)"><img name="faster_Button"
									src="images/triplanar/adjust.gif" width="24" height="24"
									border="0" alt="faster" /></a> <a href="Javascript:setDelay(1)"><img
									name="slower_Button" src="images/triplanar/adjustminus.gif"
									width="24" height="24" border="0" alt="slower" /></a>
							</p>
						</div>
					</form>
				</div>
			</div>
		</div>
	</body>

	<script type="text/javascript">
		var TIME_OUT_MESSAGE = "Timeout while generating triplanar images. Please contact your system administrator.";
		var FAILURE_MESSAGE = "An error occurred while trying to generate triplanar images. Please contact your system administrator.";
		var WAIT_MESSAGE = "Generating triplanar files, please wait ...";
		
		var pollIntervalId = -1;
		var timeoutThreshold = 10; // Polling timeout threshold is 10 min
		var startTime = 0;
		var urlObj = urlObject(window.location.href);
	
		$(document).ready(function() {
			pollServer();
			startTime = (new Date()).getTime();
			
			pollIntervalId = setInterval(function() { 
				pollServer();
			}, 15000);
		});
	
		function pollServer() {
			var studyName = urlObj.parameters.studyName;
			var datasetName = urlObj.parameters.datasetName;
			var triplanarName = decodeURIComponent(urlObj.parameters.triplanarName);
			
			$.ajax({
				url : "service/triplanar/viewTriplanarImg",
				data : {
					"studyName" : studyName,
					"datasetName" : datasetName,
					"triplanarName" : triplanarName
				},
				cache : false,
				type : "GET",
				dataType : "json",
				success : function(data) {
					if (data.triplanarReady) {
						clearInterval(pollIntervalId);
						$('#notificationDiv').hide();
						$('#topControls').show();
						$('#triView').show();

						if (data.is4DImage) {
							$('#timeControls').show();
						}
						
						changeDataset(data.imagePrefix, data.is4DImage, data.timeSlices);
					}
					else {
						if (isPollingTimeout()) {
							$('#notificationText').text(TIME_OUT_MESSAGE);
							$('#loadImg').hide();
							clearInterval(pollIntervalId);
						}
						else {
							$('#notificationText').text(WAIT_MESSAGE);
						}
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					$('#notificationText').text(FAILURE_MESSAGE);
					$('#loadImg').hide();
					clearInterval(pollIntervalId);
					
					// Log error to console.
					console.error("Error from polling request. " + jqXHR.status + " : " + errorThrown);
				}
			});
		}
	
		function isPollingTimeout() {
			var currentTime = (new Date()).getTime();
			var elapsedTime = (currentTime - startTime) / 1000;	// Time elapsed since first polling
			
			if (elapsedTime > (timeoutThreshold * 60)) {
				return true;
			}
			else {
				return false;
			}
		}
		
	</script>
	<!-- namespace setup -->
	<script type="text/javascript" src="js/lib/triplanar.js"></script>
</html>
