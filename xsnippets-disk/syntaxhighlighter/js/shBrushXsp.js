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
;(function()
{
	// CommonJS
// Disabled due to a conflict in Dojo
//	typeof(require) != 'undefined' ? SyntaxHighlighter = require('shCore').SyntaxHighlighter : null;

	function Brush()
	{
		function process(match, regexInfo)
		{ 
			var constructor = SyntaxHighlighter.Match,
				code = match[0],
				tag = new XRegExp('(&lt;|<)[\\s\\/\\?]*(?<name>[:\\w-\\.]+)', 'xg').exec(code),
				closingBracket = code.match(/(&gt;|\>)/gm),
				result = []
				;

			if (match.attributes != null) 
			{
				var attributes,
					regex = new XRegExp('(?<name> [\\w:\\-\\.]+)' +
										'\\s*=\\s*' +
										'(?<value> ".*?"|\'.*?\'|\\w+)',
										'xg');

				while ((attributes = regex.exec(code)) != null) 
				{
					result.push(new constructor(attributes.name, match.index + attributes.index, 'xsp-attribute'));
					result.push(new constructor(attributes.value, match.index + attributes.index + attributes[0].indexOf(attributes.value), 'xsp-string'));
				}
			}

			if (tag != null)
				result.push(
					new constructor(tag[0], match.index, 'xsp-keyword')
//					new constructor(tag.name, match.index + tag[0].indexOf(tag.name), 'xsp-keyword') //We don't want brackets to be plain
				);
			if (closingBracket != null)
				result.push(	
					new constructor(closingBracket[0], match.index+code.length-closingBracket[0].length, 'xsp-keyword')
				);
				
			return result;
		}
		function cdataProcess(match, regexInfo)
		{ 
			var constructor = SyntaxHighlighter.Match,
				result=[],
				code=match[0],
				beginning=/(\&lt;|<)\!\[[\w\s]*?\[/,
				ending=/\]\](\&gt;|>)$/
			;
			//we will not match '<![CDATA[' and ']]'
			beginStr=code.match(beginning);
			clear1=code.replace(beginning,'');
			clear2=clear1.replace(ending,'');
			endStr=code.match(ending);
						
			result.push(new constructor(beginStr[0], match.index, 'xsp-keyword'));
			result.push(new constructor(clear2, match.index+(code.length-clear1.length), 'xsp-cdata'));
			result.push(new constructor(endStr[0], match.index+beginStr[0].length+clear2.length, 'xsp-keyword'));
				
			return result;
		}
			
		this.regexList = [
			{ regex: new XRegExp('(\\&lt;|<)\\!\\[CDATA\\[(.|\\s)*?\\]\\](\\&gt;|>)', 'gm'),		func:cdataProcess },	// <![ ... [ ... ]]>
			{ regex: SyntaxHighlighter.regexLib.xmlComments,												css: 'comments' },	// <!-- ... -->
			{ regex: new XRegExp('(&lt;|<)[\\s\\/\\?]*(\\w+)(?<attributes>.*?)[\\s\\/\\?]*(&gt;|>)', 'sg'), func: process }
		];
		
	};

	Brush.prototype	= new SyntaxHighlighter.Highlighter();
	Brush.aliases	= ['xsp'];

	SyntaxHighlighter.brushes.Xsp = Brush;

	// CommonJS
	typeof(exports) != 'undefined' ? exports.Brush = Brush : null;
})();
