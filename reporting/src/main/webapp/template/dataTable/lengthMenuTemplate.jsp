<script type="text/x-handlebars-template" id="lengthMenuTemplate">
<div class="lengthMenuContainer" style="float: left">
<div style="float: left;margin-bottom: 1px;margin-top:5px">Show</div>
<ul id="lmenu" class="nav" style="width:65px; display: inline-table;margin: 0px;">
			<li class="buttonWithIcon" style="padding:0px;margin-right: 22px;">
				<a href="javascript:;" class="lengthMenuDropdown">
					{{current}}
				</a>
				<span class="icon right pe-is-i-angle-down"></span>
				<ul style="z-index: 2; width:67%">
{{#each htmlOptions}}
<li>
						<a href="javascript:;" id="length_{{length}}" class="lengthMenuOption" data-value="{{length}}">{{length}}</a>
					</li>

{{/each}}
					
				</ul>

			</li>
		</ul>

<div style="margin-bottom: 1px;float: right;margin-right:5px;margin-top:5px"> Entries</div>
</div>
<span class="formJoinDescription" style="padding-top: 5px; display: block; float: left; width:50%"></span>
 <div style="clear:both;"></div>
</script>
