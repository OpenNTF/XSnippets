/*
xInvolve Custom Control SSJS library

Version: 2.00 - 2012.02.13
Developer: Serdar Basegmez, Developi Information Systems, http://lotusnotus.com/en
*/

var invTools={
	
	defaults: {
		text_NoRating: "Nobody rated yet",
		text_RatingResult: "Overall rating is %1 by %2 person(s)", 
		text_OwnRating: "You have rated %1 star(s)",

		text_NoComments: "No comments yet",
		text_CommentsDetail: "%1 comment(s)",	
		text_CommentsClose: "Close", 
		text_CommentsCancel: "Cancel", 
		text_CommentsAdd: "Add Comment", 
		text_CommentListHeader: "Comments", 
		text_CommentListMore: "Older Comments...",
		
		err_SetRatingCookie: "An error occured while setting your rating. Check your cookie settings...",
		err_SetRating: "An error occured while saving your rating...",
		err_AddComment: "An error occured while saving your comment...",

		adminRole: "[Admin]",
		starCount: 5,
		commentDatePattern: "('at' HH:mm 'on' dd.MM.yyyy)",
		commentDefaultRowCount : 10		
	},
	
	mustHaveViews: ["RatingsByIdUser", "RateCalc", "CommentsById"],
		
	// This routine should be called before page load.
	// It places a unique idendity to user's cookies. This is used for anoymous rating.
	handleAnonymous: function() {
		if(cookie.containsKey("xiIdendity")) {
			// It's OK...
		} else {
			response = facesContext.getExternalContext().getResponse(); 
			userCookie = new javax.servlet.http.Cookie("xiIdendity", @Unique()); 
			userCookie.setMaxAge(60*60*24*365*10);
			userCookie.setPath("/");
			response.addCookie(userCookie);
		}
	},		
	
	// Checks the custom control for necessary conditions. This routine is the entrance point.
	//		It checks necessary conditions and keep the check and check result in memory.
	//		Parameter may be the compositeData object or a json style configuration.
	// 		InteractionDB must exist and have necessary view elements, iId must be valid.
	checkConfig: function(config) {
		try {
			ccId=config.ccId || "";
			interactionDB=config.interactionDB;
			iId=config.iId;
			
			if(invTools.isChecked(config.ccId)) {
				return invTools.isLoaded(config.ccId);
			} else {
				viewScope.put("xInvolveChecked"+ccId, true);			
			} 

			if((interactionDB=="") || ((db=session.getDatabase(session.getServerName(), interactionDB, false))==null)) {
				viewScope.put("xInvolveStatus"+ccId, false);
				invTools._errHandler(ccId, "Interaction database ("+interactionDB+") cannot be opened!", false);
				return false;
			}
			
			for(i=0; i<invTools.mustHaveViews.length; i++) 
				if(db.getView(invTools.mustHaveViews[i])==null) {
					viewScope.put("xInvolveStatus"+ccId, false);
					invTools._errHandler(ccId, "Interaction database ("+interactionDB+") has no necessarry views!", false);
					return false;
				}
			
			if(iId=="") {
				viewScope.put("xInvolveStatus"+ccId, false);
				invTools._errHandler(ccId, "Unable to identify the interaction document!", false);
				return false;
			}

			if(!invTools.calculateTotals(config)) {
				viewScope.put("xInvolveStatus"+ccId, false);
				return false;
			}

			viewScope.put("xInvolveStatus"+ccId, true);

			return true;
		} catch(e) {
				viewScope.put("xInvolveStatus"+ccId, false);
				invTools._errHandler(ccId, "Unable to check ("+e+")!", false);
				return false; 						
		}
	},

	// Checks if Rating function available. Used in UI checks and paranoid ones. 
	isRatingEnabled: function(config) {
		var displayOnly=config.ratesDisplayOnly || false;
		var anonymousRating=config.anonymousRating || false;
		
		if(displayOnly) {
			return false;
		}
		if(session.getEffectiveUserName()=="Anonymous" && (! anonymousRating)) {
			return false;
		}
		return true;
	},
	
	// Calculate rating count and average ratings for this CC. Caches findings into the scope.
	calculateTotals: function(config) {
		try {
			ccId=config.ccId;
			interactionDB=config.interactionDB;
			iId=config.iId;

			ratings=@DbLookup(["",interactionDB], "RateCalc", iId, 2, "[FailSilent]");
			
			if(ratings=="") {
				viewScope.put("xInvolveRating"+ccId, 0);
				viewScope.put("xInvolveRateCount"+ccId, 0);
				return true;
			}
			
			if(typeof(ratings)=="number") 
				ratings=[ratings];
				
			viewScope.put("xInvolveRating"+ccId, (@Sum(ratings)/ratings.length).toFixed(2));
			viewScope.put("xInvolveRateCount"+ccId, ratings.length);

			return true;
		} catch (e) {
			invTools._errHandler(ccId, "Unable to calculate totals ("+e+")", false)
			return false;
		}
	},
		
	// Returns an array like ["full", "full", "empty"...]. Used in read only star creation. 
	getRepeatArray: function(config) {
		result=[];
		count=config.starCount||invTools.defaults.starCount;
		rating=Math.round(invTools.getTotalRating(config.ccId));
		for(i=0;i<count; i++) {
			result.push(i<rating?"full":"empty")
		}
		return result;
	},
	
	// Returns the explanation of current rate counts and average.
	getRatingDetail: function(config){
		result=[];

		rating=invTools.getTotalRating(config.ccId);
		rateCount=invTools.getTotalRateCount(config.ccId);
		ownRate=invTools.getRating(config);
		
		noRating=config.text_NoRating || invTools.defaults.text_NoRating;
		ratingResult=config.text_RatingResult || invTools.defaults.text_RatingResult;
		ownRating=config.text_OwnRating || invTools.defaults.text_OwnRating
		
		if(rateCount==0) {
			result.push(noRating);
		} else {
			if(ownRate!=0) result.push(ownRating.replace("%1", ownRate));
			result.push(ratingResult.replace("%1", rating).replace("%2",  rateCount));
		}
		
		return result;
	},

	// Returns the explanation of current comment count.
	getCommentDetail: function(config){
		count=invTools.getCommentCount(config);

		noComments=config.text_NoComments || invTools.defaults.text_NoComments;
		commentsDetail=config.text_CommentsDetail || invTools.defaults.text_CommentsDetail;
		
		if(count==0) {
			return noComments;
		} else {
			return commentsDetail.replace("%1", count);
		}
	},

	// Returns current rating from the interaction document. 
	getRating: function(config) {
		doc=invTools.getRatingDoc(config);
		return doc?(doc.getItemValueInteger("Rating") || 0):0;
	},
	
	// Creates and returns a new document for interactions. used by both rating and commenting...	
	createNewInteraction: function(config) {
		try {
			interactionDB=config.interactionDB;
			iId=config.iId;

			db=session.getDatabase(session.getServerName(), interactionDB, false);
			userkey=invTools.getUserKey();

			var doc:NotesDocument=db.createDocument();
			doc.replaceItemValue("form", "interaction");
			doc.replaceItemValue("$PublicAccess", "1");
			doc.replaceItemValue("iId", iId);
			doc.replaceItemValue("username", session.getEffectiveUserName());
			doc.replaceItemValue("userkey", userkey);
			doc.replaceItemValue("authors", session.getEffectiveUserName()=="Anonymous"?invTools.defaults.adminRole:[invTools.defaults.adminRole, session.getEffectiveUserName()]);

			return doc;

		} catch(e) {
			invTools._errHandler(ccId, "Unable to create new interaction document! ("+e+")", false);
			return null;
		}		
	},
	
	// Returns the rating document for the current user and context.
	getRatingDoc:function(config) {
		try {
			ccId=config.ccId;
			interactionDB=config.interactionDB;
			iId=config.iId;

			db=session.getDatabase(session.getServerName(), interactionDB, false);
			iView=db.getView("RatingsByIdUser");
			userkey=invTools.getUserKey();

			doc=iView.getDocumentByKey(iId+"!!!"+userkey, true);

			if(doc==null) {
				doc=invTools.createNewInteraction(config);
			}

			return doc;

		} catch(e) {
			invTools._errHandler(ccId, "Unable to return interaction document! ("+e+")", false);
			return null;
		}
	},
	
	// Sets rating for the current user and current context, updates count and average caches. 
	setRating: function(config, rating) {
		try {
			if(isNaN(parseInt(rating))) return;

			// Checking unique identification through cookies...
			if(@UserName()=="Anonymous" && !cookie.containsKey("xiIdendity")) {
				invTools._errHandler(config.ccId, "Unable to set rating! No cookie found...", true, invTools.defaults.err_SetRatingCookie);
				return false;
			}
			
			doc=invTools.getRatingDoc(config);
			if(doc==null) {
				invTools._errHandler(config.ccId, "Unable to set rating!", true, invTools.defaults.err_SetRating);
				return false;
			} 
			doc.replaceItemValue("Rating", parseInt(rating));
			doc.replaceItemValue("Date", session.createDateTime(new Date()));
			doc.computeWithForm(false, false);
			if(! doc.save(false, false)) {
				invTools._errHandler(config.ccId, "Unable to save rating!", true, invTools.defaults.err_SetRating);
				return false;
			}
			invTools.calculateTotals(config);
			invTools.persistRating(config);
			return true;
		} catch(e) {
			invTools._errHandler(config.ccId, "Unable to set rating! Exception occured: ("+e+")", true, invTools.defaults.err_SetRating);
			return false;			
		}
	},
	
	// We are saving average rating into the document context.
	// Warning: You should do it everywhere you use rating to prevent inconsistencies.
	// Warning: Developer should be careful to prevent save conflicts. Never use in edit mode.
	// Warning: Signer should have editing rights on the document.
	persistRating: function(config) {
		try {
			targetDocumentId=config.ratingTargetDocumentId || "";
			targetFieldName=config.ratingTargetFieldName || "";
	
			if(targetDocumentId=="" || targetFieldName=="") {
				// no need to progresss...
				return;
			}
			
			db=sessionAsSigner.getCurrentDatabase();
			doc=db.getDocumentByUNID(targetDocumentId);
			doc.replaceItemValue(targetFieldName, parseFloat(invTools.getTotalRating(config.ccId)));
			doc.replaceItemValue(targetFieldName+"RateCount", parseInt(invTools.getTotalRateCount(config.ccId)));
			doc.save(false, false);
						
		} catch (e) {
			invTools._errHandler(ccId, "Unable to save average rating ("+e+")", false)
		}
	},

	// Returns how many comments we have.	
	getCommentCount: function(config) {
		try {
			ccId=config.ccId;
	
			if(!invTools.isLoaded(ccId)) return 0;

			return invTools.getComments(config).length;
						
		} catch (e) {
			invTools._errHandler(ccId, "Unable to get comment count ("+e+")", false)
		}
	},

	// Append a new comment.	
	addNewComment: function(config, commentText) {
		try {
			ccId=config.ccId;
			if(!invTools.isLoaded(ccId)) return;

			doc=invTools.createNewInteraction(config);
			
			doc.replaceItemValue("commentText", invTools.convertText(commentText));
			doc.replaceItemValue("Date", session.createDateTime(new Date()));
			doc.computeWithForm(false, false);

			if(! doc.save(false, false)) {
				invTools._errHandler(ccId, "Unable to save comment!", true, invTools.defaults.err_AddComment);
				return false;
			}

			//Clearing the cache for comments array
			invTools.clearComments(ccId);

			return true;
						
		} catch (e) {
			invTools._errHandler(ccId, "Unable to add comment ("+e+")", true, invTools.defaults.err_AddComment)
			return false;
		}	
	},

	// Creates and returns an array of objects for all comments. 
	// The parameter will be a configuration object, normally compositeData.
	getComments: function(config) {
		try {
			ccId=config.ccId;
			interactionDB=config.interactionDB;
			iId=config.iId;
			commentDatePattern=(config.commentDatePattern || invTools.defaults.commentDatePattern);
			results=[];
			
			if(viewScope.get("xInvolveComments"+ccId)!=null) {
				return viewScope.get("xInvolveComments"+ccId);
			}

			if(!invTools.isLoaded(ccId)) return results;
			
			db=sessionAsSigner.getDatabase("",interactionDB,false);
			cView=db.getView("CommentsById");
			entries=cView.getAllEntriesByKey(iId, true);
			
			for(i=0; i<entries.getCount(); i++){
				doc=entries.getNthEntry(i+1).getDocument();
				results.push({
					username: session.createName(doc.getItemValueString("username")).getCommon(),
					cDate: new java.text.SimpleDateFormat(commentDatePattern).format(doc.getItemValueDateTimeArray("date")[0].toJavaDate()),
					comment: doc.getItemValueString("commentText")
				})
			}

			//Cache results.
			viewScope.put("xInvolveComments"+ccId, results);
			return results;
			
		} catch (e) {
			invTools._errHandler(ccId, "Unable to get comments array ("+e+")", false);
			return "[]";
		}		
	},	

	// Get methods returns values from the scoped elements. 
	getInteractionDB: function(ccId) { return viewScope.get("xInvolveInteractionDB"+ccId) || ""; },
	getIID: function(ccId) { return viewScope.get("xInvolveIID"+ccId) || ""; },
	isLoaded:function(ccId) { return viewScope.get("xInvolveStatus"+ccId) || false; },
	isChecked:function(ccId) { return viewScope.get("xInvolveChecked"+ccId) || false; },
	getTotalRating: function(ccId) { return viewScope.get("xInvolveRating"+ccId) || 0; },
	getTotalRateCount: function(ccId) { return viewScope.get("xInvolveRateCount"+ccId) || 0; },
	getErrorMessage: function(ccId) { return viewScope.get("xInvolveErrorMsg"+ccId) || ""; },
	clearComments: function(ccId) { viewScope.remove("xInvolveComments"+ccId); },

	// Returns true if the new comment functionality enabled.
	canWeComment: function(config) {
		var userKey=session.getEffectiveUserName();
		
		if(userKey=="Anonymous") {
			return false;
		} else {
			return ! config.readOnly;
		}
	},
	
	// User key is the second identifier for the interaction document.
	// We don't check anonymous rights here. For comments, the check
	// should have been done before.
	getUserKey: function() {
		var userKey=session.getEffectiveUserName();
		
		if(userKey=="Anonymous" && cookie.containsKey("xiIdendity")) {
			userKey=cookie.get("xiIdendity").getValue();
		}
		
		return userKey;
	},

	// Comment text should be converted to a decoded HTML. Tags are stripping.
	convertText: function(txt) {
		return txt.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\n/g, "<br>").replace(/\r/g, "");
	},
	
	// Internal error handler. Prints the error message into the server console.
	// If displayUImsg is true, it caches UI message into the scope. We show this UI message 
	// in the error image's title. if uiMsg is left blank, msg will be used.
	// Can be extended to get developer's own error handler.
	_errHandler:function(ccId, msg, displayUImsg, uiMsg) {
		print("xInvolve ("+ccId+") Error: "+msg);
		if(displayUImsg) {
			viewScope.put("xInvolveErrorMsg"+ccId, (uiMsg || msg)); 
		}
		invTools.errHandler(ccId, msg);
	},
	
	// Custom error handler. This might be overrided in "afterPageLoad" script of the mother page.
	// Note that in case you are using multiple copies of custom control, you can only handle the
	// first control's errors 
	errHandler:function(){ }
}
