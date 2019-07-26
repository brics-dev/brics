<script id="editQuestionFormattingTabLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_formatting">Formatting</a></li>
</script>



<script id="editQuestionFormattingTab" type="text/x-handlebars-template">
	<div class="tabcontainer" id="dialog_editQuestion_formatting">
			<div class="row">
				<div class="col-md-12">Set up the appearance of the question which will display on any form it is associated with.</div>
			</div>
			<div class="row">
				<div class="col-md-2">
					<label for="align">Horizontal Align</label>
				</div>
				<div class="col-md-2">
					<select name="align" id="horizontalAlign">
						<option value="left">Left</option>
						<option value="right">Right</option>
						<option value="center">Center</option>
					</select>
				</div>
				<div class="col-md-2">
					<label for="fontFace">Font Face</label>
				</div>
				<div class="col-md-2">
					<select id="fontFace" name="fontFace">
						<option value="arial">Arial</option>
	                    <option value="courier new">Courier New</option>
	                    <option value="fixedsys">Fixedsys</option>
						<option value="sans-serif">MS Sans Serif</option>
						<option value="times new roman">Times New Roman</option>
					</select>
				</div>
				<div class="col-md-3 questionFormatHorizontalDisplayBreak">
					<label for="horizDisplayBreak">Question Text on Previous Line</label>
				</div>
				<div class="col-md-1 questionFormatHorizontalDisplayBreak">
					<input type="checkbox" name="horizDisplayBreak" id="horizDisplayBreak" />
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-2">
					<label for="verticalAlign">Vertical Align</label>
				</div>
				<div class="col-md-2">
					<select name="vAlign" id="verticalAlign">
						<option value="top">Top</option>
						<option value="middle">Middle</option>
						<option value="bottom">Bottom</option>
						<option value="baseline">Baseline</option>
					</select>
				</div>
				<div class="col-md-2">
					<label for="fontSize">Font Size</label>
				</div>
				<div class="col-md-2">
					<select id="fontSize" name="fontSize">
						<option value="0">Default(10)</option>
						<option value="-2">7.5</option>
	                    <option value="+1">13.5</option>
	                    <option value="+2">18</option>
	                    <option value="+3">24</option>
	                    <option value="+4">36</option>
					</select>
				</div>
				<div class="col-md-3 questionFormatHorizontalDisplay">
					<label for="horizontalDisplay">Display Options Horizontally</label>
				</div>
				<div class="col-md-1 questionFormatHorizontalDisplay">
					<input type="checkbox" name="horizontalDisplay" id="horizontalDisplay" />
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-2">
					<label for="indent">Indent (less than 50)</label>
				</div>
				<div class="col-md-2">
					<input type="text" name="indent" id="indent" value="{{indent}}" />
				</div>
				<div class="col-md-2">
					<label for="fontColor">Font Color</label>
				</div>
				<div class="col-md-2">
					<select name="color" id="fontColor">
						<option value="black">Black</option>
						<option value="aqua">Aqua</option>
						<option value="blue">Blue</option>
						<option value="fuchsia">Fuchsia</option>
						<option value="gray">Gray</option>
						<option value="green">Green</option>
						<option value="lime">Lime</option>
						<option value="maroon">Maroon</option>
						<option value="navy">Navy</option>
						<option value="olive">Olive</option>
						<option value="purple">Purple</option>
						<option value="red">Red</option>
						<option value="silver">Silver</option>
						<option value="teal">Teal</option>
						<option value="yellow">Yellow</option>
					</select>
				</div>
			</div>
		</div>
</script>