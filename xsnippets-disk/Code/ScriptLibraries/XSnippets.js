dojo.require("dojo.io.script");

function load() {
	var height;
	var width;
	var count;
	var view;
	var author;
	if (typeof xSnippetsHeight == "undefined") {
		height = 400;
	}
	else {
		height = xSnippetsHeight;
	}
	if (typeof xSnippetsWidth == "undefined") {
		width = 200;
	}
	else {
		width = xSnippetsWidth;
	}	
	if (typeof xSnippetsCount == "undefined") {
		count = 10;
	}
	else {
		count = xSnippetsCount;
	}	
	if (typeof xSnippetsView == "undefined") {
		view = "recent";
	}
	else {
		view = xSnippetsView;
	}	
	if (typeof xSnippetsAuthor != "undefined") {
		author = xSnippetsAuthor;
	}	
	
	var xSnippetsNode = dojo.byId("xsnippets");
	var xSnippetsJSNode = dojo.byId("xsnippetsJS");	
	var urlBase;
	if (xSnippetsNode) {
		if (!xSnippetsJSNode) {
			urlBase = "http://openntf.org/XSnippets.nsf/"
			//urlBase = "http://nheidloff-1/XSnippets.nsf/"
		}
		else {
			urlBase = xSnippetsJSNode.getAttribute('src');
			urlBase = urlBase.substring(0, urlBase.indexOf(".nsf") + 4) + "/";
		}
		
		var outerDiv = document.createElement('div');
		outerDiv.setAttribute('style','height:' + height + 'px; width:' + width + 'px;');
		outerDiv.setAttribute('class','outerDiv');
		xSnippetsNode.appendChild(outerDiv);
		
		var innerDiv = document.createElement('div');
		innerDiv.setAttribute('class','innerDiv');
		outerDiv.appendChild(innerDiv);
		
		var link = document.createElement('a');
		link.setAttribute('href','http://openntf.org/xsnippets');
		innerDiv.appendChild(link);
		
		var imgDiv = document.createElement('img');
		imgDiv.setAttribute('style', 'background:black; margin-bottom: 10px;border:white;');
		imgDiv.setAttribute('src', urlBase + 'OpenNTFLogo.png');
		link.appendChild(imgDiv);
		
		var xSnippetsEntriesNode = document.createElement('div');
		xSnippetsEntriesNode.setAttribute('id', 'xSnippetsEntries');
		xSnippetsEntriesNode.setAttribute('class','xSnippetsEntries');		
		innerDiv.appendChild(xSnippetsEntriesNode);
			            
		var jsonpArgs = {
			url: urlBase + "snippetsAsJson.xsp?count=" + count + "&view=" + view + "&author=" + author,		
		    callbackParamName: "json",
		    load: function(data) {
		        var results = data.responseData.results;
				
		        var displayedHTML = "";	        
				if (results != null) {
					for (var i = 0; i < results.length; i++) {	                    	                	
						displayedHTML = displayedHTML + "<div class='xSnippetEntry'><a href='" + results[i].url + "' target='_blank'>" + results[i].name + "</a>" + "<div class='developer'>" + results[i].developer + "</div></div>";
					}
				}                    
				xSnippetsEntriesNode.innerHTML = displayedHTML;  

				for (var i = xSnippetsEntriesNode.children.length - 1; i >= 0; i--) {
					var snippet = xSnippetsEntriesNode.children[i];
					if (height < snippet.offsetTop + snippet.offsetHeight) {
						xSnippetsEntriesNode.removeChild(snippet);
					}					
				}	
				for (var i = xSnippetsEntriesNode.children.length - 1; i >= 0; i--) {
					var snippet = xSnippetsEntriesNode.children[i];
					if (height < snippet.offsetTop + snippet.offsetHeight) {
						xSnippetsEntriesNode.removeChild(snippet);
					}					
				}				
			},
		    
			error: function(error) {
				xSnippetsEntriesNode.innerHTML = "An unexpected error occurred: " + error;
			}
		};
		
		dojo.io.script.get(jsonpArgs);
	}
}

dojo.addOnLoad(load);