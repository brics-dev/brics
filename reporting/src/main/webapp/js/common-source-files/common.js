/* This function should be called by each page to notify the navigation scheme of where the user is.
 * 
 * A navigationOptions object is passed in which can contain any of the following:
 * bodyClass - The class to be set on the body element which determines the structure of the page
 * navigationLinkID - The ID of the link to activate in the primary navigation (webapp/common/navigation.jsp)
 * subnavigationLinkID - The ID of the link to set in secondary navigation 
 * tertiaryLinkID - The ID of the link to set in tertiary navigation
 * 
 * Example from index.jsp:
 * 	setNavigation({"bodyClass":"primary", "navigationLinkID":"define-link", "subnavigationLinkID":"listDataStructureLink"});
 */
function setNavigation(navigationOptions){
	// Valid values:  primary, secondary
	if( level = navigationOptions.bodyClass) {
		$('body').addClass(level);
		
	}
	
	// Valid values:  home-link, access-link, contribute-link, define-link, about-link, data-link
	if( id = navigationOptions.navigationLinkID){
		$('#' + id).find('a').addClass('current');	//Add the class current class to the a tag of that link
	}
	
	// Valid values depend on the subnavigation included in the page
	if( id = navigationOptions.subnavigationLinkID){
		$('#' + id).addClass('active');
		
		$('#' + id + ' a').click(function(e) {
			
			e.preventDefault();
			if( id = navigationOptions.tertiaryLinkID)
			{
				if( subNavId = navigationOptions.subnavigationLinkID)
				{
					//If the tertiary link id is displayed, then we need to show that section of the navigation
					$('.' + 'tertiary-links', $('#' + subNavId)).toggle();
				}
				
			}
			
			
		})
	}
	
	// Valid values depend on the subnavigation included in the page
	if( id = navigationOptions.tertiaryLinkID)
	{
		//Remove the class navigation from the subnav and make it the tertiary class
		if( subNavId = navigationOptions.subnavigationLinkID)
		{
			$('#' + subNavId).removeClass('active');
			$('#' + subNavId).addClass('display-ter-nav active-sub');
		}
		
		//Activate the level 3 element
		$('#' + id).addClass('active-ter');
		
		//If the tertiary link id is displayed, then we need to show that section of the navigation
		$('.' + 'tertiary-links', $('#' + subNavId)).show();
		
		
	}
}


/* EMAIL ENCRYPTION SCRIPT */

//This script is (c) copyright 2008 by Dan Appleman under the
//GNU General Public License (http://www.gnu.org/licenses/gpl.html)
//This script is modified from an original script by Jim Tucek
//For more information, visit www.danappleman.com 
//Leave the above comments alone!
//see encryption_instructions.txt for explanation of usage

var decryption_cache = new Array();

function decrypt_string(crypted_string,n,decryption_key,just_email_address) {
	var cache_index = "'"+crypted_string+","+just_email_address+"'";

	if(decryption_cache[cache_index])					// If this string has already been decrypted, just
		return decryption_cache[cache_index];				// return the cached version.

	if(addresses[crypted_string])						// Is crypted_string an index into the addresses array
		var crypted_string = addresses[crypted_string];			// or an actual string of numbers?

	if(!crypted_string.length)						// Make sure the string is actually a string
		return "Error, not a valid index.";

	if(n == 0 || decryption_key == 0) {					// If the decryption key and n are not passed to the
		var numbers = crypted_string.split(' ');			// function, assume they are stored as the first two
		n = numbers[0];	decryption_key = numbers[1];			// numbers in crypted string.
		numbers[0] = ""; numbers[1] = "";				// Remove them from the crypted string and continue
		crypted_string = numbers.join(" ").substr(2);
	}

	var decrypted_string = '';
	var crypted_characters = crypted_string.split(' ');

	for(var i in crypted_characters) {
		var current_character = crypted_characters[i];
		var decrypted_character = exponentialModulo(current_character,n,decryption_key);
		if(just_email_address && i < 7)				// Skip 'mailto:' part
			continue;
		if(just_email_address && decrypted_character == 63)	// Stop at '?subject=....'
			break;
		decrypted_string += String.fromCharCode(decrypted_character);
	}
		decryption_cache[cache_index] = decrypted_string;			// Cache this string for any future calls

	return decrypted_string;
}

function decrypt_and_e(crypted_string,n,decryption_key) {			//Changed method name (MT 03/29/2012)
	if(!n || !decryption_key) { n = 0; decryption_key = 0; }
	if(!crypted_string) crypted_string = 0;

	var decrypted_string = decrypt_string(crypted_string,n,decryption_key,false);
	parent.location = decrypted_string;
}

function decrypt_and_echo(crypted_string,n,decryption_key) {
	if(!n || !decryption_key) { n = 0; decryption_key = 0; }
	if(!crypted_string) crypted_string = 0;

	var decrypted_string = decrypt_string(crypted_string,n,decryption_key,true);
	document.write(decrypted_string);
	return true;
}

//Finds base^exponent % y for large values of (base^exponent)
function exponentialModulo(base,exponent,y) {
	if (y % 2 == 0) {
		answer = 1;
		for(var i = 1; i <= y/2; i++) {
			temp = (base*base) % exponent;
			answer = (temp*answer) % exponent;
		}
	} else {
		answer = base;
		for(var i = 1; i <= y/2; i++) {
			temp = (base*base) % exponent;
			answer = (temp*answer) % exponent;
		}
	}
	return answer;
}

if(!addresses) var addresses = new Array();
addresses.push("24511 24509 7033 6182 19840 14752 3018 19427 13476 272 9873 7828 17704 9873 2575 3521 19427 15166 12622 18169 7033 6182 19840 14752 13834 774 19840 17090 13834 19453 19427 14989"); 
addresses.push("24511 24509 7033 6182 19840 14752 3018 19427 13476 272 9873 7828 17704 9873 2575 3521 17090 11140 14752 15166 18169 7033 6182 19840 14752 13834 774 19840 17090 13834 19453 19427 14989");  
addresses.push("24511 24509 7033 6182 19840 14752 3018 19427 13476 17090 19840 5244 8274 12622 15968 6182 18169 7033 6182 19840 14752 13834 774 19840 17090 13834 19453 19427 14989");  
addresses.push("24511 24509 7033 6182 19840 14752 3018 19427 13476 21326 19427 21869 19453 14752 6182 12622 13834 2809 13834 19453 19840 2809 12622 19427 774 18169 21869 12622 13834 6182 15968 7033 6361 13834 7033 19840 14752");  
addresses.push("24511 24509 7033 6182 19840 14752 3018 19427 13476 842 6182 3018 3018 17090 11140 20161 13834 842 5244 17076 21869 14752 19840 16769 16769 11140 18169 774 19840 17090 13834 19453 19427 14989");  
addresses.push("24511 24509 7033 6182 19840 14752 3018 19427 13476 14752 19840 12622 6182 13834 16769 21869 5244 5244 19840 2809 6182 8274 11140 15968 18169 21869 12622 13834 6182 15968 7033 6361 13834 7033 19840 14752");  
addresses.push("24511 24509 7033 6182 19840 14752 3018 19427 13476 12622 3018 19840 7033 12622 19427 774 21326 18169 774 19840 774 21326 12622 13834 774 19840 17090 13834 19453 19427 14989");  
addresses.push("24511 24509 7033 6182 19840 14752 3018 19427 13476 21326 19427 774 774 6182 13834 2809 11140 15968 15968 6361 18169 774 19840 17090 13834 19453 19427 14989");  