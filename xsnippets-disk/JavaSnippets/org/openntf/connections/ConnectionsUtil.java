package org.openntf.connections;

/*
 * © Copyright IBM Corp. 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * Author: Yun Zhi Lin
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Node;

import com.ibm.xsp.extlib.sbt.services.client.DataNavigator;
import com.ibm.xsp.extlib.sbt.services.client.Endpoint;

import sbt.XmlNavigator;


public class ConnectionsUtil 
{
	public static final String COMMUNITIES_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public static final String ACTIVITY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	public static final String getTags(Node entry)
	{
		XmlNavigator navigator = new XmlNavigator(entry);
		DataNavigator categories = navigator.get("category");
		StringBuilder sb = new StringBuilder();
		int count = categories.getCount();
		for(int i=0; i< count; i++){
			DataNavigator d = categories.get(i);
			String scheme = d.stringValue("@scheme");
			if(scheme == null){
				String tag = d.stringValue("@term");
				sb.append(tag);
				if(i != count - 1)
					sb.append(" ");
			}			
		}
		return sb.toString();
	}
	
	public static final String getTitle(Node entry)
	{
		XmlNavigator navigator = new XmlNavigator(entry);
		DataNavigator title = navigator.get("title");
		return title.stringValue(".");
	}
	
	public static final String getAuthorName(Node entry)
	{
		XmlNavigator navigator = new XmlNavigator(entry);
		DataNavigator author = navigator.get("author");
		DataNavigator name = author.get("name");
		return name.stringValue(".");
	}
	
	public static final String getAuthorUnid(Node entry)
	{
		XmlNavigator navigator = new XmlNavigator(entry);
		DataNavigator author = navigator.get("author");
		DataNavigator userId = author.get("snx:userid");
		return userId.stringValue(".");
	}
	
	public static final String getContent(Node entry)
	{
		XmlNavigator navigator = new XmlNavigator(entry);
		DataNavigator content = navigator.get("content");
		return content.stringValue(".");
	}
	
	public static final String getServiceURL(String url)
	{
		Endpoint endpoint = (Endpoint) JSFUtil.getVariable("connections");
		String host = endpoint.getUrl();
		String serviceUrl = url;
		
		if(url.startsWith(host)){
			serviceUrl = url.substring(host.length(), url.length());
		}
		
		return serviceUrl;
	}
	
	public static final Date getUpdatedTime(Node entry,String format)
	{
		XmlNavigator navigator = new XmlNavigator(entry);
		DataNavigator updated = navigator.get("updated");
		String dateString = updated.stringValue(".");
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date date = null;
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static final String getProxyUrl(String originalUrl)
	{
		String prefix = "/xsp/.proxy/connections/connections/";
		String url = originalUrl.replaceFirst("://", "\\/");
		
		return prefix + url;
	}
}
