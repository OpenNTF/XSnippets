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
	typeof(require) != 'undefined' ? SyntaxHighlighter = require('shCore').SyntaxHighlighter : null;

	function Brush()
	{
		var keywords =	'ACos ASin Abs Access ActivateApp Alias And Any AppActivate Append ArrayAppend ' +
						'ArrayGetIndex ArrayReplace ArrayUnique As Asc Atn Atn2 Base Beep Bin Bin$ Binary ' +
						'Bind Boolean ByVal Byte CBool CByte CCur CDat CDbl CInt CLng CSng CStr CVDate CVar ' +
						'Call Case ChDir ChDrive Chr Chr$ Close CodeLock CodeLockCheck CodeUnlock Command ' +
						'Command$ Compare Const Cos CreateLock CurDir CurDir$ CurDrive CurDrive$ Currency ' +
						'DataType Date Date$ DateNumber DateSerial DateValue Day Declare DefBool DefByte ' +
						'DefCur DefDbl DefInt DefLng DefSng DefStr DefVar Delete DestroyLock Dim Dir Dir$ ' +
						'Do DoEvents Double EOF Else ElseIf End Environ Environ$ Eqv Erase Erl Err Error ' +
						'Error$ Evaluate Event Execute Exit Exp Explicit FALSE FileAttr FileCopy FileDateTime ' +
						'FileLen Fix For ForAll Format Format$ Fraction FreeFile From FullTrim Function Get ' +
						'GetAttr GetFileAttr GetThreadInfo GoSub GoTo Hex Hex$ Hour IMESetMode IMEStatus If ' +
						'Imp Implode Implode$ In InStr InStrB InStrBP InStrC Input Input$ InputB InputB$ ' +
						'InputBP InputBP$ InputBox InputBox$ Int Integer Is IsA IsArray IsDate IsElement ' +
						'IsEmpty IsList IsNull IsNumeric IsObject IsScalar IsUnknown Join Kill LBound LCase ' +
						'LCase$ LMBCS LOC LOF LSI_Info LSServer LSet LTrim LTrim$ Left Left$ LeftB LeftB$ ' +
						'LeftBP LeftBP$ LeftC LeftC$ Len LenB LenBP LenC Let Lib Like Line List ListTag Lock ' +
						'Log Long Loop Me MessageBox Mid Mid$ MidB MidB$ MidBP MidBP$ MidC MidC$ Minute MkDir ' +
						'Mod Month MsgBox NOTHING NULL Name New Next NoCase NoPitch Not Now Oct Oct$ On Open ' +
						'Option Or Output PI Pitch Preserve Print Private Property Public Published Put RSet ' +
						'RTrim RTrim$ Random Randomize ReDim Read Rem Remove Replace Reset Resume Return Right ' +
						'Right$ RightB RightB$ RightBP RightBP$ RightC RightC$ RmDir Rnd Round Second Seek ' +
						'Select SendKeys Set SetAttr SetFileAttr Sgn Shared Shell Sin Single Sleep Space Space$ ' +
						'Spc Split Sqr Static Step Stop Str Str$ StrComp StrCompare StrConv StrLeft StrLeft$ ' +
						'StrLeftBack StrLeftBack$ StrRight StrRight$ StrRightBack StrRightBack$ StrToken StrToken$ ' +
						'String String$ Sub TRUE Tab Tan Text Then Time Time$ TimeNumber TimeSerial TimeValue Timer ' +
						'To Today Trim Trim$ Type TypeName UBound UCase UCase$ UChr UChr$ UString UString$ Uni ' +
						'Unicode Unlock Until Use UseLSX Val VarType Variant Weekday Wend While Width With Write Xor Year Yield';

		this.regexList = [
			{ regex: /'.*$/gm,										css: 'ls-comment' },			// one line comments
			{ regex: SyntaxHighlighter.regexLib.doubleQuotedString,	css: 'ls-string' },			// strings
			{ regex: /^\%(Else|ElseIf|End|If|Include).*$/gmi,		css: 'ls-preprocessor' },		// preprocessor tags like #region and #endregion
			{ regex: /\%REM(.|\n)+\%END REM/g,					css: 'ls-comment' },			// one line comments
			{ regex: new RegExp(this.getKeywords(keywords), 'gmi'),	css: 'ls-keyword' }			// vb keyword
			];

		this.forHtmlScript(SyntaxHighlighter.regexLib.aspScriptTags);
	};

	Brush.prototype	= new SyntaxHighlighter.Highlighter();
	Brush.aliases	= ['lscript', 'lss'];

	SyntaxHighlighter.brushes.Vb = Brush;

	// CommonJS
	typeof(exports) != 'undefined' ? exports.Brush = Brush : null;
})();
