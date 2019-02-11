package org.openntf.xsnippets;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.DataObject;

import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

/**
 * @author sbasegmez
 *
 */
public class ConfigBean implements Serializable, DataObject {

	private static final long serialVersionUID = -3732463043439724416L;

	private static final String LOGDB_FILENAME = ""; // Current database if
														// empty
	private static final String VS_VAR_TO_REMEMBER = "DownloadLogged"; // To
																		// remember
																		// download
																		// logging,
																		// we
																		// use a
																		// scoped
																		// variable.

	private boolean loaded = false;
	private Calendar dateLoaded;

	private Map<String, String> fields;

	private List<Link> headerLinks;
	private List<Link> leftMenuLinks;
	private List<Link> bottomLinks;

	public ConfigBean() {
		if (!loadConfig()) {
			System.out.println("Error loading XSnippets Configuration!");
		}
	}

	private Session getSession() {
		return ExtLibUtil.getCurrentSession();
	}

	private Database getCurrentDatabase() {
		return ExtLibUtil.getCurrentDatabase();
	}

	@SuppressWarnings("unchecked")
	private boolean loadConfig() {

		fields = new HashMap<String, String>();

		Database db = getCurrentDatabase();

		View configView = null;
		Document configDoc = null;

		try {
			configView = db.getView("Configuration");
			configDoc = configView.getDocumentByKey("Configuration", true);

			if (configDoc == null) {
				configDoc = db.createDocument();
				configDoc.replaceItemValue("Form", "Configuration");
				configDoc.computeWithForm(false, false);
				configDoc.save();
			}

			for (Object itemObj : configDoc.getItems()) {
				if (itemObj instanceof Item) {
					Item item = (Item) itemObj;
					if (!item.getName().startsWith("$")) {
						fields.put(item.getName(), item.getText());
					}
					Utils.recycleObjects(item);
				}
			}

			headerLinks = getLinks(configDoc.getItemValue("headerLinks"));
			leftMenuLinks = getLinks(configDoc.getItemValue("leftMenuLinks"));
			bottomLinks = getLinks(configDoc.getItemValue("bottomLinks"));

			setLoaded(true);

			System.out.println("XSnippets Configuration loaded successfully...");

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			Utils.recycleObjects(configView, configDoc);
		}
	}

	public boolean isLoaded() {
		return loaded;
	}

	private void setLoaded(boolean loaded) {
		this.loaded = loaded;
		this.dateLoaded = (loaded) ? Calendar.getInstance() : null;
	}

	public Calendar getDateLoaded() {
		return dateLoaded;
	}

	public void reload() {
		if (!loadConfig()) {
			System.out.println("Error reloading XSnippets Configuration!");
		}
	}

	public Class<?> getType(Object arg0) {
		return ConfigBean.class;
	}

	public Object getValue(Object key) {
		if (key == null) {
			return "";
		}

		if ("headerLinks".equals(key)) {
			return getHeaderLinks();
		}
		if ("leftMenuLinks".equals(key)) {
			return getLeftMenuLinks();
		}
		if ("bottomLinks".equals(key)) {
			return getBottomLinks();
		}
		if ("codeLanguages".equals(key)) {
			return getCodeLanguages();
		}

		String result = fields.get(key);
		return (null == result) ? "" : result;
	}

	public boolean isReadOnly(Object arg0) {
		return true;
	}

	public void setValue(Object arg0, Object arg1) {
		// do nothing
	}

	public List<String> getCodeLanguages() {
		return SnippetLanguage.getLanguages();
	}

	public SnippetLanguage getSnippetLanguage(String codeLanguage) {
		return SnippetLanguage.byCodeLanguage(codeLanguage);
	}

	public String getSnippetUrl(String snippetId) {
		String url = fields.get("shortWidgetUrl");
		if (StringUtil.isEmpty(url)) {
			url = Utils.getBaseURL() + "snippet.xsp?id=";
		}

		return url + snippetId;
	}

	public List<Link> getHeaderLinks() {
		return headerLinks;
	}

	public List<Link> getLeftMenuLinks() {
		return leftMenuLinks;
	}

	public List<Link> getBottomLinks() {
		return bottomLinks;
	}

	// Extracts "Title|Link|Class" formatted list of links from a vector of
	// fields
	private static List<Link> getLinks(Vector<Object> listOfLinks) {
		List<Link> result = new ArrayList<Link>();

		for (Object o : listOfLinks) {
			if (StringUtil.isNotEmpty(o.toString())) {
				String[] a = StringUtil.splitString(o.toString() + "||", '|'); // to
																				// make
																				// sure
																				// we
																				// will
																				// have
																				// at
																				// least
																				// 3
																				// elements.

				result.add(new Link(a[0], a[1], a[2]));
			}
		}

		return result;
	}

	public boolean snippetCopied(String projectName) {

		Map<String, Object> viewScope = ExtLibUtil.getViewScope();

		if (viewScope.containsKey(VS_VAR_TO_REMEMBER)) { // if download logged
																// at the same view
															// life-cycle,
															// We don't need to do it again!
			return false;
		}

		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest request = (HttpServletRequest) ec.getRequest();

		String rUser = request.getRemoteUser();
		if ("127.0.0.1".equals(rUser)) {
			rUser = request.getHeader("HTTP_X_FORWARDED_FOR");
		}

		Session session = getSession();
		Database currentDb = getCurrentDatabase();

		Database logDb = null;
		DateTime now = null;
		Document logDoc = null;
		Name userName = null;

		try {
			if ("".equals(LOGDB_FILENAME)) {
				logDb = currentDb;
			} else {
				logDb = session.getDatabase("", LOGDB_FILENAME, false);
			}

			now = session.createDateTime(new Date());
			userName = session.createName(rUser);

			if ("Anonymous".equals("Anonymous")) {
				rUser = "";
			} else {
				rUser = userName.getCommon();
			}

			logDoc = logDb.createDocument();

			logDoc.replaceItemValue("Form", "Log");
			logDoc.replaceItemValue("Type", "fileDownload");
			logDoc.replaceItemValue("Date", now);
			logDoc.replaceItemValue("FileName", ""); // no attachment
			logDoc.replaceItemValue("UserAddress", request.getRemoteAddr());
			logDoc.replaceItemValue("User", rUser);
			logDoc.replaceItemValue("DBasePath", currentDb.getFileName());
			logDoc.replaceItemValue("Project", projectName);
			logDoc.replaceItemValue("Release", (new Date()).toString());
			logDoc.replaceItemValue("DownloadType", "snippet");
			logDoc.replaceItemValue("ServerAddress", request.getServerName());

			if (logDoc.computeWithForm(false, false)) {
				if (!logDoc.save()) {
					System.out.println("Error saving Download Log record for XSnippets");
					return false;
				}
			} else {
				System.out.println("Error computing Download Log record for XSnippets");
				return false;
			}

			viewScope.put(VS_VAR_TO_REMEMBER, "1");

			return true;

		} catch (NotesException ne) {
			ne.printStackTrace();
		} finally {
			Utils.recycleObjects(logDoc, now);
			if ("".equals(LOGDB_FILENAME)) {
				Utils.recycleObjects(logDb);
			}
		}

		return false;
	}

	public LinkedList<SnippetEntry> getMostDownloaded() {
		LinkedList<SnippetEntry> coll = new LinkedList<SnippetEntry>();
		View vwDownloaded = null;
		View vwSnippets = null;
		try {
			vwDownloaded = getCurrentDatabase().getView("DownloadsLastThreeMonthsSnapshot");
			vwDownloaded.setAutoUpdate(false);
			vwSnippets = getCurrentDatabase().getView("SnippetsAll");
			vwSnippets.setAutoUpdate(false);
			ViewNavigator nav = vwDownloaded.createViewNav();
			ViewEntry ent = nav.getFirst();
			int offset = 0;
			while (null != ent) {
				ViewEntry snippet = vwSnippets.getEntryByKey(ent.getColumnValues().get(1), true);
				if (snippet != null) {
					snippet.setPreferJavaDates(true);
					SnippetEntry snippetObj = new SnippetEntry(snippet, offset,
							getSnippetUrl((String) snippet.getColumnValues().get(0)));
					coll.add(snippetObj);
				}
				if (coll.size() == 15) {
					break;
				}
				ent = nav.getNext();
			}
		} catch (NotesException e) {
			e.printStackTrace();
		} finally {
			Utils.recycleObjects(vwDownloaded, vwSnippets);
		}
		return coll;
	}

	public LinkedList<SnippetEntry> getMostRecent() {
		LinkedList<SnippetEntry> coll = new LinkedList<SnippetEntry>();
		View vwSnippets = null;
		try {
			vwSnippets = getCurrentDatabase().getView("SnippetsRecentlyAdded");
			vwSnippets.setAutoUpdate(false);
			ViewNavigator nav = vwSnippets.createViewNav();
			ViewEntry snippet = nav.getFirst();
			int offset = 1;
			while (null != snippet) {
				snippet.setPreferJavaDates(true);
				SnippetEntry snippetObj = new SnippetEntry(snippet, offset,
						getSnippetUrl((String) snippet.getColumnValues().get(0 + offset)));
				coll.add(snippetObj);
				if (coll.size() == 15) {
					break;
				}
				snippet = nav.getNext();
			}
		} catch (NotesException e) {
			e.printStackTrace();
		} finally {
			Utils.recycleObjects(vwSnippets);
		}
		return coll;
	}

	public LinkedList<SnippetEntry> getFavorites() {
		LinkedList<SnippetEntry> coll = new LinkedList<SnippetEntry>();
		View vwFavourites = null;
		View vwSnippets = null;
		try {
			String userKey = getSessScopeString("xInvolveMyFavoritesUserKey");
			if ("".equals(userKey)) {
				System.out.println("No user key");
				return null;
			}
			vwFavourites = getCurrentDatabase().getView("FavoritesByUserKey");
			vwFavourites.setAutoUpdate(false);
			vwSnippets = getCurrentDatabase().getView("SnippetsAll");
			vwSnippets.setAutoUpdate(false);
			ViewNavigator nav = vwFavourites.createViewNavFromCategory(userKey);
			ViewEntry ent = nav.getFirst();
			int offset = 0;
			while (null != ent) {
				System.out.println(ent.getColumnValues().get(1));
				ViewEntry snippet = vwSnippets.getEntryByKey(ent.getColumnValues().get(1), true);
				if (snippet != null) {
					snippet.setPreferJavaDates(true);
					SnippetEntry snippetObj = new SnippetEntry(snippet, offset,
							getSnippetUrl((String) snippet.getColumnValues().get(0)));
					coll.add(snippetObj);
				}
				ent = nav.getNext();
			}
		} catch (NotesException e) {
			e.printStackTrace();
		} finally {
			Utils.recycleObjects(vwFavourites, vwSnippets);
		}
		return coll;
	}

	public LinkedList<String> getAuthors() {
		LinkedList<String> coll = new LinkedList<String>();
		View vwAuthors = null;
		try {
			vwAuthors = getCurrentDatabase().getView("SnippetsByAuthor");
			ViewNavigator nav = vwAuthors.createViewNav();
			nav.setMaxLevel(0);
			ViewEntry ent = nav.getFirst();
			while (ent != null) {
				Vector colVals = ent.getColumnValues();
				coll.add((String) ent.getColumnValues().get(0));
				ent.recycle(colVals);
				ent = nav.getNextSibling();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return coll;
	}

	public void doSearch() {
		try {
			String language = getSessScopeString("searchLanguage");
			String author = getSessScopeString("searchAuthor");
			String searchVariable = getSessScopeString("searchString");

			if ("".equals(language) && "".equals(author) && "".equals(searchVariable)) {
				ExtLibUtil.getSessionScope().put("searchView", "");
			} else if (!"".equals(language) && "".equals(author) && "".equals(searchVariable)) {
				// language only, use that view
				ExtLibUtil.getSessionScope().put("searchView", "SnippetsByLanguage");
			} else if (!"".equals(author) && "".equals(language) && "".equals(searchVariable)) {
				// Author only, use that view
				ExtLibUtil.getSessionScope().put("searchView", "SnippetsByAuthor");
			} else {
				LinkedList<String> searchList = new LinkedList<String>();
				if (!"".equals(language)) {
					searchList.add("[Language] = \"" + language + "\"");
				}
				if (!"".equals(author)) {
					searchList.add("[Author] = \"" + author + "\"");
				}
				if (!"".equals(searchVariable)) {
					searchList.add("\"" + searchVariable + "\"");
				}
				String fullSearch = String.join(" AND ", searchList);
				ExtLibUtil.getSessionScope().put("searchStringFull", fullSearch);
				ExtLibUtil.getSessionScope().put("searchView", "SnippetsAll");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * There are 441 snippets currently in OpenNTF Snippets. We'll load all into memory for now and review if
	 * performance is bad
	 *
	 * @return
	 */
	public LinkedList<SnippetEntry> getSearchResults() {
		LinkedList<SnippetEntry> coll = new LinkedList<SnippetEntry>();
		View vwSnippets = null;
		ExtLibUtil.getSessionScope().put("hasSearchResults", false);
		try {
			String viewName = getSessScopeString("searchView");
			if ("".equals(viewName)) {
				return null;
			}
			vwSnippets = getCurrentDatabase().getView(viewName);
			vwSnippets.setAutoUpdate(false);

			String language = getSessScopeString("searchLanguage");
			String author = getSessScopeString("searchAuthor");

			ViewNavigator nav = null;
			ViewEntryCollection ec = null;
			int offset = 0;
			if ("SnippetsByLanguage".equals(viewName)) {
				// language only, use that view
				nav = vwSnippets.createViewNavFromCategory(language);
				offset = 1;
			} else if ("SnippetsByAuthor".equals(viewName)) {
				// Author only, use that view
				nav = vwSnippets.createViewNavFromCategory(author);
				offset = 1;
			} else {
				int count = vwSnippets.FTSearch(getSessScopeString("searchStringFull"));
				ec = vwSnippets.getAllEntries();
			}

			ViewEntry snippet = null;
			if (null != nav) {
				snippet = nav.getFirst();
			} else {
				snippet = ec.getFirstEntry();
			}
			while (null != snippet) {
				snippet.setPreferJavaDates(true);
				SnippetEntry snippetObj = new SnippetEntry(snippet, offset,
						getSnippetUrl((String) snippet.getColumnValues().get(0 + offset)));
				coll.add(snippetObj);
				if (null != nav) {
					snippet = nav.getNext();
				} else {
					snippet = ec.getNextEntry();
				}
			}
			if (coll.size() > 0) {
				ExtLibUtil.getSessionScope().put("hasSearchResults", true);
			}
		} catch (NotesException e) {
			e.printStackTrace();
		} finally {
			Utils.recycleObjects(vwSnippets);
		}
		return coll;
	}

	public static String getSessScopeString(String key) {
		if (!ExtLibUtil.getSessionScope().containsKey(key)) {
			return "";
		} else if (null == (ExtLibUtil.getSessionScope().get(key))) {
			return "";
		} else {
			return (String) ExtLibUtil.getSessionScope().get(key);
		}
	}

	public static class Link implements Serializable {

		private static final long serialVersionUID = 1L;

		private String title = "";
		private String href = "";
		private String className = "";

		public Link(String title, String href, String className) {
			this.title = title;
			this.href = href;
			this.className = className;
		}

		public String getTitle() {
			return title;
		}

		public String getHref() {
			return href;
		}

		public String getClassName() {
			return className;
		}
	}

	public static class SnippetEntry implements Serializable {

		private static final long serialVersionUID = 1L;
		private String id;
		private String name;
		private String url;
		private String author;
		private String language;
		private String createdDate;
		private String notes;

		public SnippetEntry(ViewEntry ent, int offset, String passedUrl) {
			try {
				Vector<Object> colVals = ent.getColumnValues();
				setId((String) colVals.get(offset + 0));
				setName((String) colVals.get(offset + 1));
				setUrl(passedUrl);
				setAuthor((String) colVals.get(offset + 2));
				setLanguage((String) colVals.get(offset + 3));
				setCreatedDate(convertDate((Date) colVals.get(offset + 4)));
				setNotes((String) colVals.get(offset + 5));
				ent.recycle(colVals);
			} catch (NotesException e) {
				e.printStackTrace();
			}
		}

		private static String convertDate(Date dt) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
			String text = "Added " + sdf.format(dt);
			return text;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(String createdDate) {
			this.createdDate = createdDate;
		}

		public String getNotes() {
			return notes;
		}

		public void setNotes(String notes) {
			this.notes = notes;
		}
	}
}
