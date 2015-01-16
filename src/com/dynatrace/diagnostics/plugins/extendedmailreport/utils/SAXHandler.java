package com.dynatrace.diagnostics.plugins.extendedmailreport.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.AgentFilter;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.AgentGroupFilter;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.CollectorFilter;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.Emails;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.Filter;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.MonitorFilter;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.ServerFilter;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.Sources;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.XMLTags;
import com.dynatrace.diagnostics.sdk.resources.BaseConstants;

public class SAXHandler extends DefaultHandler implements MailPluginConstants {
	private List<Filter> list;
	private Filter filter;
	private Sources sources;
	private AgentFilter agentFilter;
	private AgentGroupFilter agentGroupFilter;
	private MonitorFilter monitorFilter;
	private CollectorFilter collectorFilter;
	private ServerFilter serverFilter;
	private Emails emails;
	private String content;
	private String tagName;
	
	private static final Logger log = Logger.getLogger(SAXHandler.class.getName());
	
	public SAXHandler(List<Filter> list) {
		this.list = list;
	}

	@Override
	//Triggered when the start of tag is found.
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering startElement method: qName is '" + qName + "'");
		}
		XMLTags qNameEnum;
		if (XMLTags_MAP.containsKey(qName)) {
			qNameEnum = XMLTags_MAP.get(qName);
		} else {
			String msg = "startElement method: unknown XML tag '" + qName + "'";
			throw new RuntimeException(msg);
		}
		switch(qNameEnum){
		case filter:
			tagName = "filter";
			filter = new Filter();
			break;
		case sources:
			tagName = "sources";
			sources = new Sources();
			break;
		case emails:
			tagName = "emails";
			emails = new Emails();
			break;
		case agent:
			tagName = "agent";
			agentFilter = new AgentFilter();
			break;
		case agentGroup:
			tagName = "agentGroup";
			agentGroupFilter = new AgentGroupFilter();
			break;
		case monitor:
			tagName = "monitor";
			monitorFilter = new MonitorFilter();
			break;
		case collector:
			tagName = "collector";
			collectorFilter = new CollectorFilter();
			break;
		case server:
			tagName = "server";
			serverFilter = new ServerFilter();
			break;
		default:
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering endElement method: qName is '" + qName + "'");
		}
		XMLTags qNameEnum;
		if (XMLTags_MAP.containsKey(qName)) {
			qNameEnum = XMLTags_MAP.get(qName);
		} else {
			String msg = "startElement method: qName unknown XML tag '" + qName + "'";
			throw new RuntimeException(msg);
		}
		Map<String, Pattern> map;
		switch(qNameEnum){
		//Add the employee to list once end tag is found
		case sources:
			// populate filter with sources data
			// set agent filters
			if (sources.getAgentFilter() != null) {
				filter.setAgents(sources.getAgentFilter().getNames());
				filter.setAgentServers(sources.getAgentFilter().getHosts());
			}
			// set agent group filters
			if (sources.getAgentGroupFilter() != null) {
				filter.setAgentGroups(sources.getAgentGroupFilter().getAgentGroupFilter());
			}
			// set monitor filters
			if (sources.getMonitorFilter() != null) {
				filter.setMonitors(sources.getMonitorFilter().getNames());
				filter.setMonitorServers(sources.getMonitorFilter().getHosts());
			}
			// set collector filters
			if (sources.getCollectorFilter() != null) {
				filter.setCollectors(sources.getCollectorFilter().getNames());
				filter.setCollectorServers(sources.getCollectorFilter().getHosts());
			}
			// set servers
			if (sources.getServerFilter() != null) {
				filter.setServers(sources.getServerFilter().getNames());
			}
			tagName="";
			break;
		case agent:
			sources.setAgentFilter(agentFilter);
			tagName="";
			break;
		case agentGroup:
			sources.setAgentGroupFilter(agentGroupFilter);
			tagName="";
			break;
		case monitor:
			sources.setMonitorFilter(monitorFilter);
			tagName="";
			break;
		case collector:
			sources.setCollectorFilter(collectorFilter);
			tagName="";
			break;
		case server:
			sources.setServerFilter(serverFilter);
			tagName="";
			break;
		case name:
			XMLTags tagNameEnum;
			if (XMLTags_MAP.containsKey(tagName)) {
				tagNameEnum = XMLTags_MAP.get(tagName);
			} else {
				String msg = "startElement method: unknown tagName XML tag '" + tagName + "'";
				throw new RuntimeException(msg);
			}
			switch (tagNameEnum) {
			case agent:
				setPatterns(content, (map = new HashMap<String, Pattern>()));
				agentFilter.setNames(map);
				content = "";
				break;
			case agentGroup:
				setPatterns(content, (map = new HashMap<String, Pattern>()));
				agentGroupFilter.setAgentGroupFilter(map);
				content = "";
				break;
			case monitor:
				setPatterns(content, (map = new HashMap<String, Pattern>()));
				monitorFilter.setNames(map);
				content = "";
				break;
			case collector:
				setPatterns(content, (map = new HashMap<String, Pattern>()));
				collectorFilter.setNames(map);
				content = "";
				break;
			case server:
				setPatterns(content, (map = new HashMap<String, Pattern>()));
				serverFilter.setNames(map);
				content = "";
				break;
			default:
				content = "";
				throw new RuntimeException("endElement method: wrong structure of XML parameters file, tagname is '" + tagName + "'");	
			}
			break;
		case host:
			if (XMLTags_MAP.containsKey(tagName)) {
				tagNameEnum = XMLTags_MAP.get(tagName);
			} else {
				String msg = "startElement method: unknown tagName XML tag '" + tagName + "'";
				throw new RuntimeException(msg);
			}
			switch (tagNameEnum) {
			case agent:
				setPatterns(content, (map = new HashMap<String, Pattern>()));
				agentFilter.setHosts(map);
				content="";
				break;
			case monitor:
				setPatterns(content, (map = new HashMap<String, Pattern>()));
				monitorFilter.setHosts(map);
				content="";
				break;
			case collector:
				setPatterns(content, (map = new HashMap<String, Pattern>()));
				collectorFilter.setHosts(map);
				content="";
				break;
			default:
				content="";
				throw new RuntimeException("endElement method: wrong structure of XML parameters file, tagname is '" + tagName + "' is unknown.");	
			}
			break;
		case emails:
			// if tos, cc, and bcc are all empty - throw exception
			if ((emails.getTos() == null || emails.getTos().isEmpty()) && (emails.getCc() == null || emails.getCc().isEmpty()) && (emails.getBcc() == null || emails.getBcc().isEmpty())) {
				String msg = "endElement method: tos, cc, and bcc are nulls or empty";
				log.severe(msg);
				throw new RuntimeException(msg);
			}
			if (emails.getTos() != null) {
				filter.setTos(emails.getTos());
			}
			if (emails.getCc() != null) {
				filter.setCc(emails.getCc());
			}
			if (emails.getBcc() != null) {
				filter.setBcc(emails.getBcc());
			}
			tagName="";
			break;
		case tos:
			emails.setTos(parseEmails(content.trim().split(BaseConstants.SCOLON)));
			content="";
			break;
		case cc:
			emails.setCc(parseEmails(content.trim().split(BaseConstants.SCOLON)));
			content="";
			break;
		case bcc:
			emails.setBcc(parseEmails(content.trim().split(BaseConstants.SCOLON)));
			content="";
			break;
		case filter:
			list.add(filter);
			tagName="";
		default:
			break;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering characters method");
		}
		content = String.copyValueOf(ch, start, length).trim();
		if (log.isLoggable(Level.FINER)) {
			log.finer("characters method: content is '" + content + "'");
		}
	}
	
	private Status setPatterns(String value, Map<String, Pattern> map) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setPatterns method");
		}
		for (String pattern : value.trim().split(BaseConstants.SCOLON)) {
			try {
				if ((pattern = pattern.trim()).isEmpty()) {
					continue;
				}
				map.put(pattern,  Pattern.compile(pattern));
			} catch (PatternSyntaxException e) {
				String message = new StringBuilder().append("setPatterns method: pattern is '" + pattern + "', pattern syntax exception; ").append(e.getMessage()).toString();
				log.severe("setPatterns method: " + message);	
				return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, message, message, e);
			}
		}
		return null;
	}
	
	public static List<Address> parseEmails(String[] emails) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering parseEmails method");
		}
		if (emails == null || emails.length == 0) {
			return null;
		}

		List<Address> addresses = new ArrayList<Address>();
		for (String email : emails) {
			if (email.trim().isEmpty()) {
				continue;
			}
			
			try {
				addresses.add(new InternetAddress(email));
			} catch (AddressException e) {
				String msg = "parseEmails method: The following e-mail address is incorrect '" + email + "': " + e.getMessage();
				log.severe(msg);
				throw new RuntimeException(msg, e);
			}
		}

		return addresses;

	}
}
