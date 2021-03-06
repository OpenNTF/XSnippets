package org.openntf.xsnippets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum SnippetLanguage {
	XSP("XPages", "shBrushXsp.js", "xsp"), SSJS("JavaScript (Server)", "shBrushJScript_custom.js", "js"), CSJS(
			"JavaScript (Client)", "shBrushJScript_custom.js",
			"js"), JAVA("Java", "shBrushJava_custom.js", "java"), XML("XML", "shBrushXml.js", "xml"), CSS(
					"Cascaded Style Sheets", "shBrushCss_custom.js",
					"css"), THEMES("Themes", "shBrushXml.js", "xml"), FORMULA("Formula", "shBrushFormula.js",
							"formula"), LSS("LotusScript", "shBrushLscript.js", "lscript"), JS("JavaScript (Generic)",
									"shBrushJScript_custom.js", "js"), JS_NODE("JavaScript (Domino Node.js)",
											"shBrushJScript_custom.js",
											"js"), VOLT("JavaScript (Volt)",
											"shBrushJScript_custom.js",
											"js"), DQL("DQL", "shBrushSql", "sql"), ADMIN("Admin Scripts", "",
													"admin"), JS_ICEC("JavaScript (ICEC)", "shBrushJScript_custom.js",
															"js"), JS_CUSTOMIZER("JavaScript (Connections Customizer)",
																	"shBrushJScript_custom.js", "js"), CSS_CUSTOMIZER(
																			"Cascaded Style Sheets (Connections Customizer)",
																			"shBrushCss_custom.js", "css");

	private String codeLanguage;
	private String brushFile;
	private String brushAlias;

	// Keeping two lists. Second list could be extracted from the first one but we care about sorting.
	private static Map<String, SnippetLanguage> mapping;
	private static List<String> listOfLanguages;

	SnippetLanguage(String codeLanguage, String brushFile, String brushAlias) {
		this.codeLanguage = codeLanguage;
		this.brushFile = brushFile;
		this.brushAlias = brushAlias;
	}

	public String getCodeLanguage() {
		return codeLanguage;
	}

	public String getBrushFile() {
		return brushFile;
	}

	public String getBrushAlias() {
		return brushAlias;
	}

	private static void initLists() {
		mapping = new HashMap<String, SnippetLanguage>();
		listOfLanguages = new ArrayList<String>();

		for (SnippetLanguage lang : values()) {
			mapping.put(lang.getCodeLanguage(), lang);
			listOfLanguages.add(lang.getCodeLanguage());
		}
	}

	public static SnippetLanguage byCodeLanguage(String codeLanguage) {
		if (null == mapping) {
			initLists();
		}
		return mapping.get(codeLanguage);
	}

	public static List<String> getLanguages() {
		if (null == listOfLanguages) {
			initLists();
		}
		return listOfLanguages;
	}

}
