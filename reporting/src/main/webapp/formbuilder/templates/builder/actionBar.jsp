<script id="actionBarTemplate" type="text/x-handlebars-template">
		<div id="actionBar_left" class="col-md-2">
			<div id="formTitle" style="width:500px; word-wrap: break-word;">
				<div>
					<span name="name">{{name}}</span>
				</div>
			</div>
			<div id="formSubtitle">
				<div>
					<span name="dataStructureName">{{structure}}</span>: <span name="status">{{status}}</span>
				</div>
			</div>
		</div>

		<div id="actionBar_right" class="text-right col-md-10">
			<nav>
				<ul>
					{{> saveMenuItem }}
					{{> editFormDetailsMenuItem }}
					{{> addSectionMenuItem }}
					{{> addQuestionMenuItem }}
					{{> addTextMenuItem }}
					{{> layoutMenuItem }}
					{{> cancelMenuItem }}
				</ul>
			</nav>
		<div id="questionGraphicsMessageContainer"></div>
		</div>
		
</script>