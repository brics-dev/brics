var inputs = location.search.split("\?");
var deName = inputs[1].split("=");
var deFormMap = [];
var allDeInfo = [];
var deInfo = [];
var formInfo = [];
var dataElementColor;
var originalColor;
var maxRadius = 50; 
var maxLinks = 0;
var minLinks = -1;
var highlightName = deName[1];
var subCategoryNameList = [];
var subCategoryNameMap = {};
// Hide the display until the 'see also' list returns with data:
d3.select(".kgContent").style("display", "none" );
if ( deName[0] === "dataElementName" ) {
	deShortName = deName[1];

	var color = "#147289";
	if ( location.host.indexOf("cdrns") !== -1 ) {
		color = "#C20072";
	}
	else if ( location.host.indexOf("cistar") !== -1 ) {
		color = "#b00836";
	}
	else if ( location.host.indexOf("cnrm") !== -1 ) {
		color = "#0a3040";
	}
	else if ( location.host.indexOf("eyegene") !== -1 ) {
		color = "#00336f";
	}
	else if ( location.host.indexOf("fitbir") !== -1 ) {
		color = "#004d00";
	}
	else if ( location.host.indexOf("gsdr") !== -1 ) {
		color = "#b00836";
	}
	else if ( location.host.indexOf("ninds") !== -1 ) {
		color = "#b00836";
	}
	else if ( location.host.indexOf("nti") !== -1 ) {
		color = "#000000";
	}
	else if ( location.host.indexOf("pdbp") !== -1 ) {
		color = "#da5b3d";
	}
	if ( inputs.length > 2 ) {
		var colorInput = inputs[2].split("=");
		if ( colorInput[0] === "color" ) {
			color = decodeURIComponent(colorInput[1]);
		}
	}
	
	originalColor = d3.color(color);
	dataElementColor = d3.color(color);
	dataElementColor = d3.hsl(dataElementColor);
	// increase lightness
	dataElementColor.l += .1;
	dataElementColor = d3.rgb(dataElementColor);

	
	var selectedDENames = [];
	selectedDENames.push(deShortName);
		
	$.ajax({
		url: "service/query/knowledgeGraphInfo",
		cache : false,
		traditional: true,
		data: {
			deNames : selectedDENames 
		},
		success : function(data) {				
			if ( data.length > 0 ) {
				if ( data[0].seeAlso.length > 0 ) {
					var valArray = data[0].seeAlso[0].split(";").map(function(s) { return s.trim(); });
					if ( valArray.length == 1 ) {
						if (valArray[0].length > 0 ) {
							valArray = data[0].seeAlso[0].split(" ").map(function(s) { return s.trim(); });
						}
						else {
							valArray = [];
						}
					}
					if ( valArray.length > 0 ) {
						// set the kgContent as visible:
						d3.select(".kgContent").style("display", null );
						
						// some 'see also' lists have the original name duplicated - removes the duplication.
						valArray = valArray.filter( function(name) { return name !== highlightName } ); 
						// look for subcategories s0: s1:, etc...
						var subcategories = {};
						valArray.forEach(function(val) {
							if ( val.indexOf(":") !== -1 ) {
								var pair = val.split(":");
								if ( subcategories[pair[0]] ) {
									subcategories[pair[0]].push(pair[1]);
								}
								else {
									subcategories[pair[0]] = [pair[1]];
								}
								subCategoryNameList.push(pair[1]);
							}
						});
						// remove the subcategories from the see also list:
						valArray = valArray.filter( function(name) { return ( name.indexOf(":") === -1 ) } ); 
						Object.keys(subcategories).forEach( function( key ) {
							var newKey = "sub" + key.substring(1);
							subcategories[key].forEach( function(name) {
								valArray.push( newKey + ":" + name);
							} );
						} );
						
						deFormMap.push( { title: data[0].elementTitle, varName: deShortName, URL: data[0].dictionaryLink, seeAlso: valArray, sub: subcategories, formStructure: data[0].formStructures, core: "" } );
						
						selectedDENames = [];
						valArray.forEach( function(de) {
							selectedDENames.push(de);
						} );

						$.ajax({
							url: "service/query/knowledgeGraphInfo",
							cache : false,
							traditional: true,
							data: {
								deNames : selectedDENames 
							},
							success : function(data) {
								data.forEach( function(kgInfo) {
									if ( kgInfo.formStructures !== undefined && kgInfo.formStructures.length !== 0 ) {
									if ( kgInfo.seeAlso.length === 0 ) {
										deFormMap.push( { title: kgInfo.elementTitle, varName: kgInfo.elementName, URL: kgInfo.dictionaryLink, seeAlso: [], formStructure: kgInfo.formStructures, core: "" } );
									}
									else {
									valArray = kgInfo.seeAlso[0].split(";").map(function(s) { return s.trim(); });
									if ( valArray.length == 1 ) {
										if (valArray[0].length > 0 ) {
											valArray = kgInfo.seeAlso[0].split(" ").map(function(s) { return s.trim(); });
										}
										else {
											valArray = [];
										}
									}
									// look for subcategories s0: s1:, etc...
									var subcategories = {};
									valArray.forEach(function(val) {
										if ( val.indexOf(":") !== -1 ) {
											var pair = val.split(":");
											if ( subcategories[pair[0]] ) {
												subcategories[pair[0]].push(pair[1]);
											}
											else {
												subcategories[pair[0]] = [pair[1]];
											}
											subCategoryNameList.push(pair[1]);
										}
									});
									// remove the subcategories from the see also list:
									valArray = valArray.filter( function(name) { return ( name.indexOf(":") === -1 ) } ); 
									Object.keys(subcategories).forEach( function( key ) {
										var newKey = "sub" + key.substring(1);
										subcategories[key].forEach( function(name) {
											valArray.push( newKey + ":" + name);
										} );
									} );
									
									
									deFormMap.push( { title: kgInfo.elementTitle, varName: kgInfo.elementName, URL: kgInfo.dictionaryLink, seeAlso: valArray, sub: subcategories, formStructure: kgInfo.formStructures, core: "" } );
									}
								}
								} );
								// got all the input data, display
								displayKnowledgeGraph();
							},
							error: function(err,x,a) {}
						});
					}
					else {
						// no see also list:
						// set error message as visible:
						d3.select(".errorContent").style("display", "flex" );
						d3.select(".errorContent").style("align-items", "center" );
						d3.select(".errorContent").style("justify-content", "center" );
					}
				}
			}
		},
		error: function(err,x,a) {         
			// set error message as visible:
			d3.select(".errorContent").style("display", "flex" );
			d3.select(".errorContent").style("align-items", "center" );
			d3.select(".errorContent").style("justify-content", "center" );
			console.log("error", deShortName, err);
		}
	});
}

function formIndex( formStructures, form ) {
	if ( typeof(form) === "string" ) {
		return formStructures.findIndex( function(fs) { return (fs.title === form); } );
	}
	return formStructures.findIndex( function(fs) { return (fs.title === form.title); } );
}

function linkInNodes(nodes, link) {
	return nodes.findIndex( function(node) { return ( node.varName === link); } ) !== -1;
}

function initForce(nodes) {
	var content = d3.select(".content");
	var clientRect = content.node().getBoundingClientRect();
	var margin = {top: 0, right: 0, bottom: 0, left: 0};
	var width = clientRect.width - margin.right - margin.left - 0;
	var height = clientRect.height - margin.top - margin.bottom;

	var svg = content.append("svg").attr("width", width).attr("height", height);
	var links = [];
	var forms = [];
	var sortNodes = nodes.sort(function(a,b) { 
		return (a.formStructure ? a.formStructure.length : 0) - (b.formStructure ? b.formStructure.length : 0); 
		});

	var formLimit = 40;
	var formCount = 0;
	var formCountLimit = 50;
	sortNodes.forEach( function(node, i) {
		if (!node.title) node.title = node.varName;
		var source = node.title;
		if ( source ) {
			var seeAlso = node.seeAlso;
			seeAlso.forEach( function(link) {
				if ( link && linkInNodes(nodes, link) ) {
					var linkIndex = nodes.findIndex( function(node) { return ( node.varName === link ) } );
					// add default title
					if (!nodes[linkIndex].title) nodes[linkIndex].title = nodes[linkIndex].varName;
					var linkTitle = nodes[linkIndex].title;
					links.push( { source: source, target: linkTitle, type: 'cde' } );
				}
			} );

			var formStructures = node.formStructure;
			if (formStructures) {
				// limit how many forms this node can add.
				formCountLimit = formCount + (formLimit - formCount) / (sortNodes.length - i);
				formStructures.forEach( function(form) {
					var fIndex = formIndex(forms, form);
					if ( form && fIndex === -1) {
						if (formCount < formCountLimit) {
							forms.push({title: form.title, URL: form.dictionaryLink, type: "form", color: 1, linkCount: 1, formsList: [form], seeAlsoList: [] });
							formCount += 1;
							links.push( {source: source, target: form.title, type: 'form' } );
						}
					}
					else {
						forms[fIndex].seeAlsoList.push(source);
						links.push( {source: source, target: form.title, type: 'form' } );
					}
	
				});
			} else {
				formStructures = [];
			}
		
			var sub = node.sub;
			if ( sub && Object.keys(sub).length > 0 ) {
				var keys = Object.keys(sub);
				keys.forEach( function(key) {
					var subNames = sub[key];
					var sourceNames = [source];
					subNames.forEach( function(name) {
						var title = subCategoryNameMap[name];
						if ( title === undefined ) title = name;
						sourceNames.push(title);
					});
					var dummyName = node.varName + ":" + key;
					forms.push({
						title: dummyName, type: "dummy", varName: "", 
						color: 2, linkCount: 1, URL: node.URL, seeAlsoList: [], formsList: [], core: ""
					});
					links.push( {source: source, target: dummyName, type: 'sub', sourceName: sourceNames  } );
					subNames.forEach( function(name) {
						var title = subCategoryNameMap[name];
						if ( title === undefined ) title = name;
						forms.push({
							title: title, type: "sub", varName: name, 
							color: 2, linkCount: 1, URL: node.URL, seeAlsoList: [], formsList: [], core: ""
						});
						links.push( {source: dummyName, target: title, type: 'sub', sourceName: sourceNames } );
					} );
				} );
			}
			
			// add the circle for this node:
			forms.push({
				title: source, type: "cde", varName: node.varName, 
				color: 2, linkCount: 1, URL: node.URL, seeAlsoList: seeAlso, formsList: formStructures, core: node.core
			});
		}
	} );
	
	links.forEach( function(link) {
		var sourceIndex = formIndex(forms, link.source);
		if (sourceIndex !== -1) {
			forms[sourceIndex].linkCount += 1;
		}
		var targetIndex = formIndex(forms, link.target);
		if (targetIndex !== -1) {
			forms[targetIndex].linkCount += 1;
		}
		if ( sourceIndex !== -1 && targetIndex !== -1 ) {
			if ( forms[sourceIndex].type == "form" ) {
				forms[sourceIndex].seeAlsoList.push(forms[targetIndex].varName);
			}
			if ( forms[targetIndex].type == "form" ) {
				forms[targetIndex].seeAlsoList.push(forms[sourceIndex].varName);
			}
		}
	});
	
	var labelMargin = 300;
	var count = 0;
	var halfCount = Math.ceil(formCount/2.0);
	var incY = height / (halfCount);
	// fix the position of the forms (fx, fy), with half on each side.
	forms.forEach( function(form) {
		if ( form.type === "form") {
			if ( count < halfCount ) {
				form.fx = labelMargin;
				form.fy = incY * (count+1) - incY/2;
			} else {
				form.fx = width - labelMargin;
				form.fy = incY * (count - halfCount + 1) - incY/2;
			}
			count++;
		}
		if ( form.linkCount > maxLinks ) {
			maxLinks = form.linkCount;
		}
		if ( minLinks === -1 ) {
			minLinks = form.linkCount;
		}
		if ( form.linkCount < minLinks ) {
			minLinks = form.linkCount;
		}
	} );
	

	var helpPane = content.append("div").classed("helpPane", true)
	  .style("display", "none")
	  .style("height", "400px")
	  .style("overflow", "auto")
	  .style("padding", "0px")
	  .on("click", function() { if (d3.event.ctrlKey) helpNode(); });
	
	var fm = helpPane.append("div").classed("helpPaneForms", true).style("margin-left", "10px");
	fm.append("h2").text("Forms");
	fm.append("p");
	var cde = helpPane.append("div").classed("helpPaneCdes", true);
	cde.append("h2").text("CDEs");
	cde.append("p");

    var header = helpPane.insert("span", "span")
      .style("display", "flex")
//      .style("margin-right", "10px")
      .style("float", "right");
    header.append("span").classed("kgHelpButton", true)
    .style("max-width", "25px")
    .style("max-heigt", "25px")
    .style("width", "25px")
    .style("height", "25px")
    .on("click", function() { helpNode(); } )
    .append("span")
//    .style("margin", "0 4px")
    // close
    .html('<svg style="width:24px;height:24px" viewBox="0 0 24 20"> <title>close</title>' +
'<path fill="#000000" d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />' +
'</svg>');

	var color = d3.scaleOrdinal(d3.schemeCategory10);

	var forceScale = Math.min(1.0, 60.0 / forms.length);

	var forceStrength = function(d) { return ( -600 * forceScale * ((d.type === "form") ? 1 : 1)); };
	var manyBody = d3.forceManyBody().strength(forceStrength).distanceMax(1200);

	var simulation = d3.forceSimulation()
	.force("link", d3.forceLink().id(function(d) { return d.title; }).distance( function(d) { return d.type === "sub" ? 50: 200; } ))
	.force("charge", manyBody)
	.force("center", d3.forceCenter(width / 2, height / 2));

	var linksEnter = null;
	var nodesEnter = null;
	var nodesUpdate = null;
	var linksUpdate = null;
	var svgLinks = null;
	var svgNodes = null;

	function redraw() {
		svgLinks = svg.selectAll(".link").data(links);

		linksEnter = svgLinks.enter().append("line").attr("class", "link");
		linksUpdate = linksEnter.merge(svgLinks); 
		linksUpdate.style("stroke", "#aaa");
		svgLinks.exit().remove();

		svgNodes = svg.selectAll(".node").data(forms, function(d) { return d.title; } );

		nodesEnter = svgNodes.enter().append("g")
		.attr("class", "node")
		.on("mouseover", function (d) { highlightNode(d3.select(this), d); })
		.on("mouseout", function() { return highlightNode(); } )
		.on("click", function (d) { if (d3.event.ctrlKey) helpNode(d3.select(this), d); });

		var circles = nodesEnter.append("circle")
		.attr("r", function(d) { return radius(d); } )
		.attr("fill", function(d) { return (d.type === "form" ? "#1D20A0" : dataElementColor); })
		.attr("stroke-width", function(d) { return ((d.varName === highlightName ?  "4" : "0")); })
//		.attr("stroke-dasharray", function(d) { return (d.varName === highlightName ? "10,10" : "none"); })
		.attr("stroke", function(d) { return (d.varName === highlightName ? "black" : "white"); })
		.call(d3.drag()
			// this accounts for the fact we are dragging the circle, but moving its parent 'g'
			.container(function() { return this.parentNode.parentNode; })
			.on("start", dragstarted)
			.on("drag", dragged)
			.on("end", dragended))
			.on("dblclick", function(d) { 
				if (d.type === "form"  || d.varName === highlightName || d.seeAlsoList.length === 0 ) { if (d.URL) { window.open( d.URL, '_blank'); } }
				else {
					var url = location.origin + location.pathname + "?dataElementName=" + d.varName + "?color=" + originalColor;
					window.open( url, '_blank');
				} } );

		var labels = nodesEnter.append("text")
		.text( function(d) { return ( d.type === "form" ? d.title : d.varName); } )
		.attr('x', function(d) { return ( d.fx === labelMargin ? -8 : 8); } )
		.attr('y', 3)
		.attr('dy', '0.3em')
		.style('text-anchor', function(d) { return ( d.fx === labelMargin ? 'end' : 'start'); } )

		labels.filter( function(d) { return ( d.type === "form"); } ).call(wrap, 280);

		nodesEnter.append("title")
		.text(function(d) { return d.title; });

		nodesUpdate = nodesEnter.merge(svgNodes);
		svgNodes.exit().remove();

		function ticked() {
			linksUpdate
			.attr("x1", function(d) { return d.source.x; })
			.attr("y1", function(d) { return d.source.y; })
			.attr("x2", function(d) { return d.target.x; })
			.attr("y2", function(d) { return d.target.y; });

			nodesUpdate
			.attr("transform", function(d) {
				return "translate(" + d.x + "," + d.y + ")";
			})
		}

		simulation
		.nodes(forms)
		.on("tick", ticked);

		simulation.force("link")
		.links(links);
	}
	
	redraw();

	// only cde can be dragged, not forms.
	function dragstarted(d) {
		if ( d.type === "form" ) return;
		if (!d3.event.active) simulation.alphaTarget(0.3).restart();
		d.fx = d.x;
		d.fy = d.y;
	}

	function dragged(d) {
		if ( d.type === "form" ) return;
		d.fx = d3.event.x;
		d.fy = d3.event.y;
	}

	function dragended(d) {
		if ( d.type === "form" ) return;
		if (!d3.event.active) simulation.alphaTarget(0);
		if ( d.orphaned ) return;
		d.fx = null;
		d.fy = null;
	}

	var selectedNode = null;
	function highlightNode(node, nodeData) {
		if (node) {
			selectedNode = nodeData;
			node.select("circle")
			.attr("r", function(d) { return radiusHighlight(d); } )
			// if ( nodeData.type === "cde" ) {
			//   infoText.text("see also: " + nodeData.seeAlsoList + "\n" + "forms: " + nodeData.formsList );
			// }
			// else {
			//   infoText.text("Count: " + (nodeData.linkCount - 1));
			// }

			var highlightNodes = [selectedNode];
			links.forEach( function(link) {
				if ( nodeData.title === link.source.title ) {
					highlightNodes.push(link.target);
				}
				else if ( nodeData.title === link.target.title ) {
					highlightNodes.push(link.source);
				}
			});

			nodesUpdate.selectAll("circle")
			.filter( function(d) { return (highlightNodes.indexOf(d) == -1 ); } )
			.attr("fill", function(d) { return (d.type === "form" ? "#BBBBE4" : dataElementColor); })
//			.attr("stroke-dasharray", function(d) { return (d.varName === highlightName ? "10,10" : "none"); })
			.attr("stroke", function(d) { return (d.varName === highlightName ? "black" : "white"); });

			nodesUpdate.selectAll("text")
			.filter( function(d) { return (highlightNodes.indexOf(d) == -1 ); } )
			.attr("opacity", 0.5);

			linksUpdate.style("stroke", function(d) { return (
					(d.type === "sub" && (d.sourceName.indexOf(nodeData.title) !== -1)) ||
			nodeData.title === d.source.title || nodeData.title === d.target.title ? (d.type === "form" ? "#1D20A0" : "#1DA027") : "#DFDEDE"); })
			linksUpdate.style("opacity", function(d) { return ( 
					(d.type === "sub" && (d.sourceName.indexOf(nodeData.title) !== -1)) ||
			nodeData.title === d.source.title || nodeData.title === d.target.title ? 1.0 : 0.3); } )
			linksUpdate.style("stroke-width", function(d) { return ( 
					(d.type === "sub" && (d.sourceName.indexOf(nodeData.title) !== -1)) ||
			nodeData.title === d.source.title || nodeData.title === d.target.title ? "2" : "1" ); } )
		} else {
			selectedNode = null;
			nodesUpdate.selectAll("circle")
			.attr("r", function(d) { return radius(d); } )
			.attr("fill", function(d) { return (d.type === "form" ? "#1D20A0" : dataElementColor); })
//			.attr("stroke-dasharray", function(d) { return (d.varName === highlightName ? "10,10" : "none"); })
			.attr("stroke", function(d) { return (d.varName === highlightName ? "black" : "white"); });
			nodesUpdate.selectAll("text")
			.attr("opacity", 1.0);    // infoText.text("Count:");

			linksUpdate.style("stroke", "#aaa");
			linksUpdate.style("opacity", 1.0);
			linksUpdate.style("stroke-width", 1);
		}
	}
	function helpNode(node, nodeData) {
		var shown = helpPane.style("display") !== "none";
		if (node && !shown) {
			if ( nodeData.seeAlsoList.length === 0 && nodeData.formsList.length === 0 ) return;
			helpPane.select(".helpPaneCdes").select("p").html(nodeData.seeAlsoList.join('<br/>'));
			helpPane.select(".helpPaneForms").select("p").html(nodeData.formsList.map(function(form){return form.title;}).join('<br/>'));
			helpPane.style("display", null);
		} else {
			helpPane.style("display", "none");
		}
	}

	function spliceLinksForNode(node) {
		toSplice = links.filter(
				function(l) { 
					return (l.source === node) || (l.target === node); });
		toSplice.map(
				function(l) {
					links.splice(links.indexOf(l), 1); });
	}

	function freezeOrphan(node) {

		toSplice = links.filter(
				function(l) { 
					return (l.source === node) || (l.target === node); });
		if ( toSplice.length === 0 ) {
			// fix orphaned node in place:
			node.fx = node.x;
			node.fy = node.y;
			node.orphaned = true;
		}
	}

	d3.select("body").on("keydown", function() {
		if ( selectedNode && ((d3.event.key === "Delete") || (d3.event.key === "Del")) ) {
			var index = formIndex(forms, selectedNode);
			if ( index !== -1 ) {
				if ( selectedNode.type !== "form" ) {
					forms.splice(index, 1);
				}
				spliceLinksForNode(selectedNode);
				forms.forEach( function(form) {
					freezeOrphan(form);
				});

				redraw();
				simulation.alphaTarget(0.3).restart();
				// ticked();
			}
			selectedNode = null;
		}
	});


	//	wraps long text, add number of lines to node data.
	function wrap(text, width) {
		text.each(function() {
			var text = d3.select(this);
			var dom_text = text.text();

			if (dom_text === "") return;
			
			var words = dom_text.split(/\s+/).reverse(),
			word,
			line = [],
			lineHeight = 1.2, // ems
			x = text.attr("x"),
			y = text.attr("y"),
			dy = parseFloat(text.attr("dy"));
			// margin for hover study box.
			var tspan = text.text(null).append("tspan").attr("x", x).attr("y", y),
			tspanList = [tspan];
			while (word = words.pop()) {
				line.push(word);
				tspan.text(line.join(" "));
				if (tspan.node().getComputedTextLength() > width) {
					line.pop();
					tspan.text(line.join(" "));
					line = [word];
					tspan = text.append("tspan").attr("x", x).attr("y", y).text(word);
					tspanList.push(tspan);
				}
			}
			var offset = tspanList.length > 1 ? Math.floor((tspanList.length)/2) : 0;
			tspanList.forEach(function(tsp, i) {
				tsp.attr("dy", (i - offset) * lineHeight + dy + "em");
			});
			// data_text.numLines = tspanList.length;

			// data_text.nodeWidth = this.getBoundingClientRect().width;
			//data_text.nodeHeight = this.getBoundingClientRect().height;
		} );
	}

}

function radius(d) {
	if ( d.type === "dummy" ) return 2;
	var linkCount = d.linkCount;
	var r = 4 + (maxRadius - 4) * (linkCount - minLinks) / (maxLinks - minLinks);
	return r;
}

function radiusHighlight(d) {
	if ( d.type === "dummy" ) return 2;
	var linkCount = d.linkCount;
	var r = 8 + (maxRadius + 4 - 8) * (linkCount - minLinks) / (maxLinks - minLinks);
	return r;
}

function displayKnowledgeGraph() {

	if ( subCategoryNameList.length > 0 ) {
		$.ajax({
			url: "service/query/knowledgeGraphInfo",
			cache : false,
			traditional: true,
			data: {
				deNames : subCategoryNameList 
			},
			success : function(data) {
				data.forEach( function(kgInfo) {
					subCategoryNameMap[kgInfo.elementName] = kgInfo.elementTitle;
				});		

				var nodes = deFormMap;
				initForce(nodes);
			},
			error: function(err,x,a) {}
		});
	}
	else {
		var nodes = deFormMap;
		initForce(nodes);		
	}
}
