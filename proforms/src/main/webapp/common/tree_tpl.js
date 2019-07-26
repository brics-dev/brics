/*
	Feel free to use your custom icons for the tree. Make sure they are all of the same size.
	If you don't use some keys you can just remove them from this config
*/

var TREE_TPL = {

	// general
	'target':'_self',	// name of the frame links will be opened in
							// other possible values are:
							// _blank, _parent, _search, _self and _top

    'keep_states':'true',
	// icons - root	
	'icon_48':'/ctdb/images/treeIcons/book.gif',   // root icon normal
	'icon_52':'/ctdb/images/treeIcons/booksel.gif',   // root icon selected
	'icon_56':'/ctdb/images/treeIcons/bookopen.gif',   // root icon opened
	'icon_60':'/ctdb/images/treeIcons/booksel.gif',   // root icon selected opened

	// icons - node	
	'icon_16':'/ctdb/images/treeIcons/book.gif', // node icon normal
	'icon_20':'/ctdb/images/treeIcons/bookopen.gif', // node icon selected
	'icon_24':'/ctdb/images/treeIcons/bookopen.gif', // node icon opened
	'icon_28':'/ctdb/images/treeIcons/booksel.gif', // node icon selected opened


	'icon_80':'/ctdb/images/treeIcons/bookopen.gif', // mouseovered node icon normal

	// icons - leaf
	'icon_0':'/ctdb/images/treeIcons/page.gif', // leaf icon normal
	'icon_4':'/ctdb/images/treeIcons/page.gif', // leaf icon selected

	// icons - junctions	
	'icon_2':'/ctdb/images/treeIcons/joinbottom.gif', // junction for leaf
	'icon_3':'/ctdb/images/treeIcons/join.gif',       // junction for last leaf
	'icon_18':'/ctdb/images/treeIcons/plusbottom.gif', // junction for closed node
	'icon_19':'/ctdb/images/treeIcons/plus.gif',       // junctioin for last closed node
	'icon_26':'/ctdb/images/treeIcons/minusbottom.gif',// junction for opened node
	'icon_27':'/ctdb/images/treeIcons/minus.gif',      // junctioin for last opended node

	// icons - misc
	'icon_e':'/ctdb/images/treeIcons/empty.gif', // empty image
	'icon_l':'/ctdb/images/treeIcons/line.gif',  // vertical line
	
	// styles - root
	'style_48':'mout', // normal root caption style
	'style_52':'mout', // selected root caption style
	'style_56':'mout', // opened root caption style
	'style_60':'mout', // selected opened root caption style
	'style_112':'mover', // mouseovered normal root caption style
	'style_116':'mover', // mouseovered selected root caption style
	'style_120':'mover', // mouseovered opened root caption style
	'style_124':'mover', // mouseovered selected opened root caption style
	
	// styles - node
	'style_16':'mout', // normal node caption style
	'style_20':'mout', // selected node caption style
	'style_24':'mout', // opened node caption style
	'style_28':'mout', // selected opened node caption style
	'style_80':'mover', // mouseovered normal node caption style
	'style_84':'mover', // mouseovered selected node caption style
	'style_88':'mover', // mouseovered opened node caption style
	'style_92':'mover', // mouseovered selected opened node caption style

	// styles - leaf
	'style_0':'mout', // normal leaf caption style
	'style_4':'mout', // selected leaf caption style
	'style_64':'mover', // mouseovered normal leaf caption style
	'style_68':'mover' // mouseovered selected leaf caption style

	// make sure there is no comma after the last key-value pair
};
