/**
 * Project: xInvolve
 * Version: 2.00 - 2012.02.13
 * Developer: Serdar Basegmez, Developi Information Systems, http://lotusnotus.com/en
*/


/**
* Extended Rating Control for xInvolve Project
* >>> Some new events and connections added
* >>> Fixed duplicate onchange bug
* >>> Fixed 'losing hovering' problem (regression in Dojo 1.6+)
*/
dojo.provide('dojox.form.CustomRating');
dojo.require('dojox.form.Rating');
(function(){
	dojo.declare("dojox.form.CustomRating", dojox.form.Rating, {
		onStarClick : function(_a) {
			var _b = +dojo.attr(_a.target, "value");
//			if(_b == this.value) {
//				this.onReset();   //we don't want to catch reset event...
//			} else {
				this.setAttribute("value", _b);
				this._renderStars(this.value);
//				this.onChange(this.value);  //this is a bug
//			}
		},
		onReset: function() {},
		_onMouse: function(evt){ 
			this.hovering=(evt.type!="mouseout"); //added this to fix it.
			if(this.hovering){
				var hoverValue = +dojo.attr(evt.target, "value");
				this.onMouseOver(evt, hoverValue);
				this._renderStars(hoverValue, true);
			}else{
				this._renderStars(this.value);
			}
		}
	})
}());

/**
 * It sets rating using the RPC control and reloads the panel with dojo animation.
 * 
 * @param ccId: Control unique ID.
 * @param panel: The Container node id for stars.
 * @param value: The rating value to be set.
 */
function setRating(ccId, panel, value) {
	var panelNode=dojo.byId(panel);
	var pos=dojo.position(panelNode, true);
	var img=dojo.query(".loadingImgPanel", panelNode)[0];
	new dojo.query("*", panelNode).style("display", "none")
	dojo.style(img, {
		"display": "",
		"width": pos.w+"px"
	});
	
	var rpcObj=eval('rating'+ccId);
	
	rpcObj.setRating(value).addCallback(function(response){
		XSP.partialRefreshGet(panel, {"onComplete": 
			function() {
				fadeIn = dojo.fadeIn({node: panel,duration: 500});
				dojo.style(panel, "opacity", "0");
				fadeIn.play();
		}})
		
	});
}

/**
 * Upon page load, it fills the informative text into the rating result (if there is a specified node)
 * @param target: id of the target container 
 * @param value: text/html value
 * @return
 */
function fillInRatingResult(target, value) {
	if(target!=null && target!="") {
		targetObj=dojo.byId(target);
		if(targetObj) {
			targetObj.innerHTML=value;
		}
	}
}

/**
 * Displays new comment dialog and hides the new comment link.
 * 
 * @param commentListId: Main wrapper. We search other elements inside this container.
 * @return
 */
function displayNewComment(commentListId) {
	var wrapperNode=dojo.byId(commentListId);
	var divNode=dojo.query(".commentListItemNew", wrapperNode)[0];
	var buttonNode=dojo.query(".commentListSubHeaderAction", wrapperNode)[0];
	var inputNode=dojo.query("textarea", divNode)[0];
	
	dojo.style(buttonNode, 'display', 'none');
	dojo.style(divNode, 'display', '');
	dojo.fadeIn({node:divNode, duration:1000}).play();
	inputNode.focus();
}

/**
 * Hides new comment dialog and shows the new comment link.
 * 
 * @param commentListId: Main wrapper. We search other elements inside this container.
 * @return
 */
function hideNewComment(commentListId, callback) {
	var wrapperNode=dojo.byId(commentListId);
	var divNode=dojo.query(".commentListItemNew", wrapperNode)[0];
	var buttonNode=dojo.query(".commentListSubHeaderAction", wrapperNode)[0];
	var inputNode=dojo.query("textarea", divNode)[0];

	dojo.style(buttonNode, 'display', '');
	dojo.fadeOut({
		node:divNode, 
		duration:500,
		onEnd: function() {
			dojo.style(divNode, {'display':'none'});
			inputNode.value = "";
			callback();
		}
	}).play();
}

/**
 * Submit comment and reload the main wrapper.
 * 
 * @param ccId: The unique id of the control. Used to find RPC.
 * @param commentListId: Main wrapper. We search other elements inside this container.
 * @return
 */
function submitComment(ccId, commentListId) {
	var wrapperNode=dojo.byId(commentListId);
	var divNode=dojo.query(".commentListItemNew", wrapperNode)[0];
	var buttonNode=dojo.query(".commentListSubHeaderAction", wrapperNode)[0];
	var inputNode=dojo.query("textarea", divNode)[0];

	if(inputNode.value.trim()=="") {return;}
	
	var rpcObj=eval('comment'+ccId);
	
	rpcObj.addComment(inputNode.value).addCallback(function(response) {
		dojo.fadeOut({
			node: wrapperNode,
			duration: 500,
			onEnd: function() {
				XSP.partialRefreshGet(commentListId, {});
			}
		}).play();
	});
}


