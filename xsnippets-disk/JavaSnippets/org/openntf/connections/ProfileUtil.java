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
 * Author: Yun Zhi Lin, Niklas Heidloff
 */
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sbt.GenericService;
import sbt.XmlNavigator;


import com.ibm.xsp.extlib.sbt.services.client.ClientServicesException;
import com.ibm.xsp.extlib.sbt.services.client.DataNavigator;
import com.ibm.xsp.extlib.sbt.services.client.Endpoint;

public class ProfileUtil 
{
	//cache for profile image url
	private HashMap<String, String> images = new HashMap<String, String>();
	
	private final static String CONNECTIONS_ENDPOINT_BEAN = "connections";
	private final static String PROFILE_SERVICE_URL = "/profiles/atom/profile.do";
	private final static String PROFILE_SEVICEDOCUMENT_URL = "/profiles/atom/profileService.do";	
	
	private final static String CONNECTIONS_PROFILE_BEAN = "connectionsProfile";
	
	public String getPeopleImageUrl(String unid)
	{
		String url = images.get(unid);
		if(url != null && url.length() > 0 )
			return url;
		
		Endpoint endpoint = (Endpoint) JSFUtil.getVariable(CONNECTIONS_ENDPOINT_BEAN);
		GenericService svc = new GenericService(endpoint, PROFILE_SERVICE_URL);
		HashMap<String, String> parameters =  new HashMap<String, String>();
		parameters.put("userid", unid);
		
		try {
			Object feed = svc.get(parameters, "xml");
			XmlNavigator navigator = new XmlNavigator((Document) feed);			
			DataNavigator entry = navigator.get("feed").get("entry");
			DataNavigator links = entry.get("link");
			url = links.selectEq("@type", "image").stringValue("@href");
			//TODO : Need to translate to proxy url
			
			if(url != null && url.length() > 0){
				url = ConnectionsUtil.getProxyUrl(url);
				images.put(unid, url);
			}
		} catch (ClientServicesException e) {
			e.printStackTrace();
		}
			
		return url;
	}
	
	/**
	 * To get unid for current authenticated user, use:
	 * https://greenhouse.lotus.com/profiles/atom/profileService.do
	 * Sample: profileService.xml
	 * 
	 * This return a service document, including the unid
	 */
	/**
	 * Return the unid of current authenticated user
	 * @return
	 */
	public String getUnid()
	{		
		loadProfileServiceDocument();
		Map sessionScope = (Map)JSFUtil.getVariable("sessionScope");
		return (String)sessionScope.get("unid");
	}

	public void loadProfileServiceDocument()
	{
		Endpoint endpoint = (Endpoint) JSFUtil.getVariable(CONNECTIONS_ENDPOINT_BEAN);
		GenericService svc = new GenericService(endpoint, PROFILE_SEVICEDOCUMENT_URL);
		try {
			Document doc = (Document)svc.get(null, "xml");
			loadUnid(doc);
			loadStatusFeed(doc);
		} catch (ClientServicesException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load the unid for current authenticated user and put in sessionScope.unid
	 * @param doc
	 */
	private void loadUnid(Document doc)
	{
		XmlNavigator navigator = new XmlNavigator(doc);	
		//service:workspace:collection:(snx:userid)
		DataNavigator collectionNode = navigator.get("service").get("workspace").get("collection");
		String url= collectionNode.stringValue("@href");
		//Do not know why the snx:userid does not work here, it shows on the service document, but fails to read
		//parse the url to get unid instead

		final String prefix = "userid=";
		int start = url.indexOf(prefix) + prefix.length();
		int end = url.indexOf("&");
		String unid = "";
		
		if(start >= prefix.length() && end != -1)
			unid = url.substring(start, end);
		
		//put it in sessionScope
		Map sessionScope = (Map)JSFUtil.getVariable("sessionScope");
		sessionScope.put("unid", unid);
	}
	
	/**
	 * Load the board feed url and related parameters and put them in sessionScope.statusFeedUrl, sessionScope.statusFeedKey
	 * @param doc
	 */
	private void loadStatusFeed(Document doc)
	{
		Element service = doc.getDocumentElement();
		NodeList workspaceNode = service.getElementsByTagName("workspace");
		if(workspaceNode.getLength() > 0){
			Element workspace = (Element)workspaceNode.item(0);
			NodeList links = workspace.getElementsByTagName("atom:link");
			
			// Due to the service document difference between Connections 2.5 & 3.0.1
			// atom:link for 3.0.1 and link for 2.5
			if(links.getLength() == 0){
				links = workspace.getElementsByTagName("link");
			}
			
			for(int i=0; i < links.getLength(); i++){
				Element link = (Element)links.item(i);
				String rel = link.getAttribute("rel");
				if(rel != null && rel.equalsIgnoreCase("http://www.ibm.com/xmlns/prod/sn/status")){
					String feedUrl = link.getAttribute("href");
					
					Endpoint endpoint = (Endpoint) JSFUtil.getVariable(CONNECTIONS_ENDPOINT_BEAN);
					String host = endpoint.getUrl();
					int start = host.length();
					int end = feedUrl.indexOf("?");
					String serviceUrl = "";
					if(end != -1)
						serviceUrl = feedUrl.substring(start, end);
					else
						serviceUrl = feedUrl.substring(start, feedUrl.length());
					Map sessionScope = (Map)JSFUtil.getVariable("sessionScope");
					sessionScope.put("statusFeedUrl", serviceUrl);
					
					final String KEY = "key=";
					start = feedUrl.indexOf(KEY) + KEY.length();
					end = feedUrl.indexOf("&");
					if(start >= KEY.length() && end != -1){
						String key = feedUrl.substring(start, end);
						sessionScope.put("statusFeedKey", key);
					}
					break;
				}
			}
		}
	}

	public void publishStatus(String message)
	{
		Endpoint endpoint = (Endpoint) JSFUtil.getVariable(CONNECTIONS_ENDPOINT_BEAN);		
		String content =  createMessageEntryDocument(message);

		
		Map sessionScope = (Map)JSFUtil.getVariable("sessionScope");
		String serviceUrl = (String)sessionScope.get("statusFeedUrl");
		String key = (String)sessionScope.get("statusFeedKey");
		GenericService svc = new GenericService(endpoint, serviceUrl);
		HashMap<String, String> parameters =  new HashMap<String, String>();
		if(key != null && key.length() > 0)
			parameters.put("key", key);

		try {
			svc.put(parameters, content);
		} catch (ClientServicesException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 		Entry Document
	 * 
			<?xml version="1.0" encoding="UTF-8"?>
			<entry xmlns:snx="http://www.ibm.com/xmlns/prod/sn"
					xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/" xmlns:thr="http://purl.org/syndication/thread/1.0"
					xmlns="http://www.w3.org/2005/Atom">
				<category term="entry" scheme="http://www.ibm.com/xmlns/prod/sn/type"></category>
				<category scheme="http://www.ibm.com/xmlns/prod/sn/message-type" term="status"/>
				<content type="text">@content@</content>
			</entry>

	 */
	private String createMessageEntryDocument(String content)
	{
		//quick way to make the document
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<entry xmlns:snx=\"http://www.ibm.com/xmlns/prod/sn\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:thr=\"http://purl.org/syndication/thread/1.0\" xmlns=\"http://www.w3.org/2005/Atom\">");
		sb.append("<category term=\"entry\" scheme=\"http://www.ibm.com/xmlns/prod/sn/type\"></category>");
		sb.append("<category scheme=\"http://www.ibm.com/xmlns/prod/sn/message-type\" term=\"status\"/>");
		sb.append("<content type=\"text\">");
		sb.append(content);
		sb.append("</content>");
		sb.append("</entry>");
		return sb.toString();
	}
	
	/*
	 * Call this method after login to connections, 
	 * it will load profile data into sessionScope
	 * The assumption is sessionScope.unid has already been set to current user's id
	 */
	public void loadProfile()
	{		
		Endpoint endpoint = (Endpoint) JSFUtil.getVariable(CONNECTIONS_ENDPOINT_BEAN);
		boolean autheticated = false;
		try {
			autheticated = endpoint.isAuthenticated();
		} catch (ClientServicesException e) {
			e.printStackTrace();
		}
		
		if(!autheticated)
			return;
		
		//only load once
		ConnectionsProfile profile = (ConnectionsProfile) JSFUtil.getVariable(CONNECTIONS_PROFILE_BEAN);
		if(profile.isLoaded())
			return;
		
		//get unid from sessionScope
		Map sessionScope = (Map)JSFUtil.getVariable("sessionScope");
		String unid = (String) sessionScope.get("unid");
		if(unid == null || unid.length() == 0)
			return;
		
		//send request
		GenericService svc = new GenericService(endpoint, PROFILE_SERVICE_URL);
		HashMap<String, String> parameters =  new HashMap<String, String>();
		parameters.put("userid", unid);
		
		try {
			Document feed = (Document) svc.get(parameters, "xml");
			//get the vcard span using DOM API first then use DataNavigator
			NodeList entries = feed.getElementsByTagName("entry");
			if(entries.getLength() == 0)
				return;
			
			Element entry = (Element) entries.item(0);
			NodeList contents = entry.getElementsByTagName("content");
			if(contents.getLength() == 0)
				return;
			
			Element content = (Element) contents.item(0);
			
			//get div or sp_0:div, the assumption is the content element only has one child
			Node div = content.getFirstChild();
			if(div == null)
				return;
			
			//get vcard span
			Node dataSpan = div.getFirstChild();
			if(dataSpan == null)
				return;
			
			XmlNavigator navigator = new XmlNavigator(dataSpan);
			
			//read data and set in profile
			DataNavigator divs = navigator.get("div");
			
			String bu = divs.selectEq("@class", "org").get("span").stringValue(".");
			profile.setBu(bu);
			String role = divs.selectEq("@class", "role").stringValue(".");
			profile.setRole(role);
			String title = divs.selectEq("@class", "title").stringValue(".");
			profile.setTitle(title);
			
			DataNavigator adr = divs.selectEq("@class", "adr work postal");
			String street = adr.get("div").selectEq("@class", "street-address").stringValue(".");
			String locality = adr.get("span").selectEq("@class", "locality").stringValue(".");
			String region = adr.get("span").selectEq("@class", "region").stringValue(".");
			String country = adr.get("div").selectEq("@class", "country-name").stringValue(".");
			
			StringBuilder addr = new StringBuilder();
			addr.append(street);
			addr.append(" ");
			addr.append(locality);
			addr.append(", ");
			addr.append(region);
			addr.append(", ");
			addr.append(country);
			profile.setAddress(addr.toString());
			
			DataNavigator officeDiv = divs.selectEq("@class", "x-office");
			String building = officeDiv.get("span").selectEq("@class", "x-building").stringValue(".");
			profile.setBuilding(building);
			String floor = officeDiv.get("span").selectEq("@class", "x-floor").stringValue(".");
			profile.setFloor(floor);
			String office = officeDiv.get("span").selectEq("@class", "x-office-number").stringValue(".");
			profile.setOffice(office);
			
			String tel = divs.selectEq("@class", "tel").get("span").stringValue(".");
			profile.setTel(tel);
			
			profile.setLoaded(true);
		} catch (ClientServicesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	private String createCommunityBookmarkEntryDocument(String title, String bookmark, String description) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<entry xmlns:snx=\"http://www.ibm.com/xmlns/prod/sn\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:thr=\"http://purl.org/syndication/thread/1.0\" xmlns=\"http://www.w3.org/2005/Atom\">");
		sb.append("<title type=\"text\">");
		sb.append(title);
		sb.append("</title>");
		sb.append("<category scheme=\"http://www.ibm.com/xmlns/prod/sn/type\" term=\"bookmark\"/>");
		sb.append("<link  href=\"" + bookmark + "\"/>");	
		sb.append("<content type=\"html\">");
		sb.append("</content>");
		sb.append("</entry>");
		return sb.toString();
	}
	
	HashMap<String, String> communities;
	
	public java.util.List getMyCommunities() {
		
		communities =  new HashMap<String, String>();
		java.util.List communitiesList = new java.util.Vector();
		
		Endpoint endpoint = (Endpoint) JSFUtil.getVariable(CONNECTIONS_ENDPOINT_BEAN);
		GenericService svc = new GenericService(endpoint, "/communities/service/atom/communities/my");
		HashMap<String, String> parameters =  new HashMap<String, String>();
		parameters.put("userid", getUnid());
		
		try {
			Document feed = (Document) svc.get(parameters, "xml");
			NodeList entries = feed.getElementsByTagName("entry");
			if(entries.getLength() == 0)
				return null;			
			for (int i = 0; i < entries.getLength(); i++) {
				Element entry = (Element) entries.item(i);
				NodeList titleNodes = entry.getElementsByTagName("title");
				if(titleNodes.getLength() == 1) {
					NodeList links = entry.getElementsByTagName("link");
					for (int l = 0; l < links.getLength(); l++) {
						Node e = links.item(l);
						NamedNodeMap nnm = e.getAttributes();						
						for (int nti = 0; nti < nnm.getLength(); nti ++) {
							Node attribute = nnm.item(nti);
							if (attribute.getNodeName().equalsIgnoreCase("rel")) {
								if (attribute.getNodeValue().equalsIgnoreCase("http://www.ibm.com/xmlns/prod/sn/bookmarks")) {
									Node href = nnm.getNamedItem("href");									
									communities.put(titleNodes.item(0).getTextContent(), href.getTextContent());
									communitiesList.add(titleNodes.item(0).getTextContent());
								}
							}
						}
					}															
				}
			}
		} catch (ClientServicesException e) {
			e.printStackTrace();
		}
		return communitiesList;
	}
	
	public void publishBookmark(String communityName, String title, String bookmark, String description) {
		Endpoint endpoint = (Endpoint) JSFUtil.getVariable(CONNECTIONS_ENDPOINT_BEAN);		
		String content =  createCommunityBookmarkEntryDocument(title, bookmark, description);

		String serviceUrl = communities.get(communityName);
		String cId = serviceUrl.substring(serviceUrl.indexOf("Uuid")+5, serviceUrl.length());
		serviceUrl = "/communities/service/atom/community/bookmarks";
		
		GenericService svc = new GenericService(endpoint, serviceUrl);
		HashMap<String, String> parameters =  new HashMap<String, String>();
		parameters.put("communityUuid", cId);

		try {
			Object out = svc.post(parameters, content, "xml");
		} 
		catch (ClientServicesException e) {
			e.printStackTrace();
		}
	}
}
