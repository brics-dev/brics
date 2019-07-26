<style>
/**we are just putting temporary this code here until we create separate CSS file.
**/

#advancedSearch{
	display:none;
}
#advancedSearchInner{
	padding: 20px;
}
ul{
	line-height:1.4;
}

#advancedSearchTable {
	border-spacing: 0 22px;
	border: medium solid;
	border-collapse: collapse;
}
#advancedSearchTable tr > th, #advancedSearchTable tr >td {
	padding: 14px;
	border: medium solid;
	width: 274px;	
}

</style>
<div id="advancedSearch">	
	<div id="advancedSearchInner">	
		<ul>
			<li>BASIC SEARCH: All results will contain all key words. Search terms are not case sensitive. Searching <b>man age</b> will return <b>human ages</b>, <b>manage</b> and <b>man age</b> but not <b>age man</b>.  </li>
			<li>ADVANCED SEARCH: The following operators can be used to perform an advanced search. </li>
		</ul>
		<br>
		<table id="advancedSearchTable">
			<tr>
				<th>Operator</th> 
				<th>Explanation</th>
				<th>Example</th>
			</tr>
			<tr>
				<td>"" Quotation marks</td>
				<td>Requires words to be searched as an exact phrase. When using this operator, all wild-card characters must be explicitly included.</td>
				<td><b>"Age"</b> will return results that have the whole word Age within them. It will not return manage or ages or aged.</td>
			</tr>
			<tr>
				<td>? Question Mark </td>
				<td> Matches exactly one character at the end of a search term.</td>
				<td><b>ma?</b> will only match search terms that end with three-letter words starting with ma, such as <b>man</b>,<b>mad</b>,<b>map</b>, and <b>mat</b> </td>
			</tr>
			<tr>
				<td>*Asterisk</td>
				<td>Matches zero or more characters (including spaces) </td>
				<td><b>*age</b> will match any word ending with age, such as <b>language</b>,<b>image</b>,<b>percentage</b>, and just <b>age</b></td>
			</tr>
			
		</table>
	</div>
</div>