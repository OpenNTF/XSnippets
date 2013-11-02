/**
 * SyntaxHighlighter
 * http://alexgorbatchev.com/SyntaxHighlighter
 *
 * SyntaxHighlighter is donationware. If you are using it, please donate.
 * http://alexgorbatchev.com/SyntaxHighlighter/donate.html
 *
 * @version
 * 3.0.83 (July 02 2010)
 * 
 * @copyright
 * Copyright (C) 2004-2010 Alex Gorbatchev.
 *
 * @license
 * Dual licensed under the MIT and GPL licenses.
 */
 
 /**
  *	Formula Highlighting
  *	
  * Original Work: Michel Van der Meiren (http://blog.lotusnotes.be/)
  * Source: http://blog.lotusnotes.be/domino/archive/2007-06-21-code-to-html.html
  * 
  */
 
;(function()
{
	// CommonJS
	// Disabled due to a conflict in Dojo
	//typeof(require) != 'undefined' ? SyntaxHighlighter = require('shCore').SyntaxHighlighter : null;

	function Brush()
	{
		var keywords =	'DEFAULT ENVIRONMENT FIELD REM SELECT';

		var r = SyntaxHighlighter.regexLib;
		
		this.regexList = [
			{ regex: /"(?:\.|(\\\")|[^\""])*"/gm,					css: 'formula-string'},
			{ regex: /{[\s\S]*?}/gm,								css: 'formula-string'},
			{ regex: /@\w+/g,										css: 'formula-keyword'},
			{ regex: new RegExp(this.getKeywords(keywords), 'gmi'),	css: 'formula-keyword' }	// keywords without @
		];
	
		this.forHtmlScript(r.scriptScriptTags);
	};

	Brush.prototype	= new SyntaxHighlighter.Highlighter();
	Brush.aliases	= ['formula'];

	SyntaxHighlighter.brushes.JScript = Brush;

	// CommonJS
	typeof(exports) != 'undefined' ? exports.Brush = Brush : null;
})();
