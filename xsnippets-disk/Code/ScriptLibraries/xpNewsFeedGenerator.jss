/**
 *
 * Copyright 2010 Thomas Ladehoff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 */

/* Changes for XSnippets
 * - Added FEED_VIEW constant and changed references.
 * - Modified other constants.
 * - Modified Link generation. Instead of providing a complete absolute URL in view, we are calculating with either context or short url.
 */

// constants defining feed information
FEED_TITLE = "XSnippets";
FEED_SUBTITLE = "OpenNTF XSnippets Feed";
FEED_LINK = "http://openntf.org/xsnippets";
FEED_LINK_SELF = "http://openntf.org/xsnippets/feedRecentAll.xsp";

FEED_STD_ENTRY_COUNT = 40; // used, if no "count" URL parameter is set

FEED_FORMAT_ATOM = "atom";
FEED_FORMAT_RSS = "rss";

FEED_VIEW = "FeedRecentAll";

/**
 *  Class for generating the feed
 *	
 *	@author		Thomas Ladehoff <tladehoff@assono.de>
 *	@created	2010-11-15
 * 	@modified	2010-11-15
 *
 */
function FeedGenerator()
{
	/**
	 * Generate the Feed
	 */
	this.prototype.getFeed = function()
	{
		var exCon = facesContext.getExternalContext(); 
		var writer = facesContext.getResponseWriter();
		var response = exCon.getResponse();
		var feed:String;
		var entryCount:int;
		var format:String;
		
	
		entryCount = FEED_STD_ENTRY_COUNT; // set standard value
		// check for "count" URL parameter
		if (context.getUrl().hasParameter("count"))
		{
			entryCount = parseInt(context.getUrl().getParameter("count"), 10);
			if (isNaN(entryCount))
				entryCount = FEED_STD_ENTRY_COUNT;
		}
		
		format = FEED_FORMAT_ATOM; // set standard value
		// check for "format" URL parameter
		if (context.getUrl().hasParameter("format"))
		{
			format = context.getUrl().getParameter("format").toLowerCase();
			if (!format.equals(FEED_FORMAT_ATOM) && !format.equals(FEED_FORMAT_RSS))
				format = FEED_FORMAT_ATOM;
		}
		
		
		response.setContentType("text/xml");
		response.setHeader("Cache-Control", "no-cache");
		
		
		var dataService = new NotesDataService();
		
		var feedFormatter = new FeedFormatter(format);
		
		feed = feedFormatter.startFeed();
		// append feed information (header)
		feed += feedFormatter.getFeedInfo(dataService.getFeedInfo(), entryCount);
		// append entries
		feed += feedFormatter.getFeedEntries(dataService.getFeedEntries(entryCount));
		feed += feedFormatter.endFeed();
		
		
		writer.write(feed);
		writer.endDocument();
		facesContext.responseComplete();
		
	}
}




/**
 * Class for generating the feed string in given format
 *
 *	@author		Thomas Ladehoff <tladehoff@assono.de>
 *	@created	2010-11-15
 * 	@modified	2010-11-23
 *
 */
function FeedFormatter(feedFormat)
{

	this.prototype.startFeed = function()
	{
		switch (feedFormat)
		{
			case FEED_FORMAT_ATOM:
				return '<?xml version="1.0" encoding="utf-8"?>\n\n<feed xmlns="http://www.w3.org/2005/Atom">\n';
			case FEED_FORMAT_RSS:
				return '<?xml version="1.0" encoding="utf-8"?>\n\n<rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:dc="http://purl.org/dc/elements/1.1/">\n\n\t<channel>\n';
		}
		
	}


	this.prototype.endFeed = function()
	{
		switch (feedFormat)
		{
			case FEED_FORMAT_ATOM:
				return '</feed>';
			case FEED_FORMAT_RSS:
				return '\t</channel>\n</rss>';
		}
		
	}


	/**
	 * generates the string representation of the feed head
	 */
	this.prototype.getFeedInfo = function(feedInfo, entryCount)
	{
		var dateFormat:java.text.SimpleDateFormat;
		var sb:java.lang.StringBuilder = new java.lang.StringBuilder();
		var temp:String;
		
		switch (feedFormat)
		{
			case FEED_FORMAT_ATOM:
				dateFormat =  new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
				
				// write title
				sb.append('\t<title>').append(feedInfo["Title"]).append('</title>\n');
		
				// write subtitle, if available
				if (feedInfo["Subtitle"] != "")
				{
					sb.append('\t<subtitle>').append(feedInfo["Subtitle"]).append('</subtitle>\n');
				}
		
				// write link
				sb.append('\t<link href="').append(escapeHTML(feedInfo["Link"])).append('" />\n');
				sb.append('\t<link rel="self" href="').append(FEED_LINK_SELF).append('?format=').append(feedFormat).append('&amp;count=' + entryCount.toString()).append('" />\n');
		
		
				// write id
				sb.append('\t<id>').append(escapeHTML(feedInfo["ID"])).append('</id>\n');

				// write updated
				temp = dateFormat.format(feedInfo["Updated"].toJavaDate());
				sb.append('\t<updated>').append(temp.substr(0, temp.length-2) + ":").append(temp.substr(temp.length-2, 2)).append('</updated>\n');
				break;
				

			case FEED_FORMAT_RSS:
				dateFormat = new java.text.SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", new java.util.Locale("en"));
				
				// write title
				sb.append('\t\t<title>').append(feedInfo["Title"]).append('</title>\n');
		
				// write subtitle, if available
				if (feedInfo["Subtitle"] != "")
				{
					sb.append('\t\t<description>').append(feedInfo["Subtitle"]).append('</description>\n');
				}
		
				// write link
				sb.append('\t\t<link>').append(escapeHTML(feedInfo["Link"])).append('</link>\n');
				sb.append('\t\t<atom:link rel="self" type="application/rss+xml" href="').append(FEED_LINK_SELF).append('?format=').append(feedFormat).append('&amp;count=' + entryCount.toString()).append('" />\n');

				// write updated
				sb.append('\t\t<lastBuildDate>').append(dateFormat.format(feedInfo["Updated"].toJavaDate())).append('</lastBuildDate>\n');
				
				break;
		}
		
		return sb.toString();
	}
	
	/**
	 * generates the string representation of the feed entries
	 */
	this.prototype.getFeedEntries = function(entryList)
	{
		var dateFormat:java.text.SimpleDateFormat;
		var sb:java.lang.StringBuilder = new java.lang.StringBuilder();
		var temp:String;
		
		switch (feedFormat)
		{
			case FEED_FORMAT_ATOM:
				dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
				
				for (var i = 0; i < entryList.length; i++)
				{
					sb.append('\t<entry>\n');
			
					// write title
					sb.append('\t\t<title>').append(escapeHTML(entryList[i]["Title"])).append('</title>\n');
			
			
					temp = escapeHTML(entryList[i]["Link"]);
					// write link
					sb.append('\t\t<link rel="alternate" type="text/html" href="').append(temp).append('" />\n');

					// write ID
					sb.append('\t\t<id>').append(temp).append('</id>\n');
	

					// write last updated
					temp = dateFormat.format(entryList[i]["Updated"].toJavaDate());
					sb.append('\t\t<updated>').append(temp.substr(0, temp.length-2) + ":").append(temp.substr(temp.length-2, 2)).append('</updated>\n');

					// write published
					temp = dateFormat.format(entryList[i]["Published"].toJavaDate());
					sb.append('\t\t<published>').append(temp.substr(0, temp.length-2) + ":").append(temp.substr(temp.length-2, 2)).append('</published>\n');
			
			
					// write author
					sb.append('\t\t<author>\n');
					sb.append('\t\t\t<name>').append(entryList[i]["Author"]).append('</name>\n');
					sb.append('\t\t</author>\n');

					// write summary
					sb.append('\t\t<summary type="html">\n');
					//sb.append('\t\t\t<![CDATA[').append(escapeHTML(entryList[i]["Abstract"])).append(']]>\n');
					sb.append('\t\t\t<![CDATA[').append(entryList[i]["Abstract"].replace(']]>', '')).append(']]>\n');
					sb.append('\t\t</summary>\n');
				
			
					sb.append('\t</entry>\n\n');
				}
				break;
				
				
			case FEED_FORMAT_RSS:
				dateFormat = new java.text.SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", new java.util.Locale("en"));
				
				for (var i = 0; i < entryList.length; i++)
				{
					sb.append('\t\t<item>\n');
			
					// write title
					sb.append('\t\t\t<title>').append(escapeHTML(entryList[i]["Title"])).append('</title>\n');
			
			
					temp = escapeHTML(entryList[i]["Link"]);
					// write link
					sb.append('\t\t\t<link>').append(temp).append('</link>\n');

					// write ID
					sb.append('\t\t\t<guid>').append(temp).append('</guid>\n');
	

					// write published
					sb.append('\t\t\t<pubDate>').append(dateFormat.format(entryList[i]["Published"].toJavaDate())).append('</pubDate>\n');
			
			
					// write author
					sb.append('\t\t\t<dc:creator>').append(entryList[i]["Author"]).append('</dc:creator>\n');


					// write summary
					// [TOTO: write description element]
					sb.append('\t\t\t<content:encoded>\n');
					sb.append('\t\t\t\t<![CDATA[').append(entryList[i]["Abstract"].replace(']]>', '')).append(']]>\n');
					sb.append('\t\t\t</content:encoded>\n');
				
			
					sb.append('\t\t</item>\n\n');
				}
				break;
		}
		
		return sb.toString();
	}
	
	/**
	 * Escape certain HTML special characters
	 */
	function escapeHTML(escapeString)
	{
   		return escapeString.replace('&', "&amp;").replace('<', "&lt;").replace('>', "&gt;").replace('"', "&quot;");
	}
	
}


/**
 * Class for data access
 *
 *	@author		Thomas Ladehoff <tladehoff@assono.de>
 *	@created	2010-11-15
 * 	@modified	2010-11-15
 *
 */
function NotesDataService()
{
	
	/**
	 * Get feed information, like title, link, ...
	 */
	this.prototype.getFeedInfo = function()
	{
		var feedInfo = new Object();
		var feedView = database.getView(FEED_VIEW);
		var nav:NotesViewNavigator = feedView.createViewNav();
		var entry:NotesViewEntry;
		
		
		feedInfo["Title"] = FEED_TITLE;
		feedInfo["Subtitle"] = FEED_SUBTITLE;
		feedInfo["Link"] = FEED_LINK;
		feedInfo["ID"] = FEED_LINK;
		
		entry = nav.getFirstDocument();
		if (entry != null)
		{
			feedInfo["Updated"] = entry.getColumnValues().elementAt(0);
		}
		else
		{
			feedInfo["Updated"] = @Now(); // just to have a value (required for atom standard)
		}
		
		return feedInfo;
	}
	
	/**
	 * Get feed data
	 */
	this.prototype.getFeedEntries = function(entryCount)
	{
		var entryList = new Array();
		var feedEntry;
		var i;
		
		var feedView = database.getView(FEED_VIEW);
		var nav:NotesViewNavigator = feedView.createViewNav();
		var entry:NotesViewEntry;
		var colValues:java.util.Vector;
		
		entry = nav.getFirstDocument();
		i = 0;
		while (i < entryCount && entry != null) {
			
			// get column values and build object
			colValues = entry.getColumnValues();
			feedEntry = new Object();
			
			feedEntry["Published"] = colValues.elementAt(1);
			feedEntry["Updated"] = colValues.elementAt(2);
			feedEntry["Title"] = colValues.elementAt(3);
			feedEntry["Author"] = colValues.elementAt(4);
//			feedEntry["Link"] = colValues.elementAt(5);

		// Added for XSnippets
			var shortURL = getConfig(config, 'shortWidgetUrl');
			if(shortURL==null){
				url=context.getUrl();
				baseURL=url.toString().replace(url.getSiteRelativeAddress(context), "/");
				feedEntry["Link"]=baseURL+"snippet.xsp?id=" + colValues.elementAt(5);
			}else{
				feedEntry["Link"]=shortURL + colValues.elementAt(5);
			}
		// End of the addition...
			
			feedEntry["Abstract"] = colValues.elementAt(6);
			//feedEntry["ID"] = colValues.elementAt(7);
			
			// add feed entry to entry list
			entryList[i] = feedEntry;
			i++;
			
			// go on with next view entry
			entry = nav.getNextDocument();	
		}
		
		return entryList;
	}
}
