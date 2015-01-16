package com.dynatrace.diagnostics.plugins.extendedmailreport.utils;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.Threshold;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.XMLThresholdTags;

public class SAXHandlerThresholds extends DefaultHandler implements MailPluginConstants {
	private Map<String, Threshold> thresholds;
	private Set<String> metricNames;
	private String content;
	private String tagName;
	private Threshold threshold;
	private static final Logger log = Logger.getLogger(SAXHandlerThresholds.class.getName());
	
	public SAXHandlerThresholds(Map<String, Threshold> thresholds, Set<String> metricNames) {
		this.thresholds = thresholds;
		this.metricNames = metricNames;
	}
	
	@Override
	//Triggered when the start of tag is found.
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("SAXHandlerThresholds class: Entering startElement method: qName is '" + qName + "'");
		}
		
		XMLThresholdTags qNameEnum;
		if (OMNICARE_XMLThresholdTags_MAP.containsKey(qName)) {
			qNameEnum = OMNICARE_XMLThresholdTags_MAP.get(qName);
		} else {
			String msg = "startElement method: unknown XML tag '" + qName + "'";
			throw new RuntimeException(msg);
		}
		
		switch (qNameEnum) {
		case threshold:
			tagName = XMLThresholdTags.threshold.name();
			threshold = new Threshold();
			threshold.setName(attributes.getValue(THRESHOLD_ATTRIBUTE_NAME));
			threshold.setMetricName(attributes.getValue(THRESHOLD_ATTRIBUTE_METRIC_NAME));
			break;
		default:
			break;
		}
			
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("SAXHandlerThresholds class: Entering endElement method: qName is '" + qName + "'");
		}
		
		XMLThresholdTags qNameEnum;
		if (OMNICARE_XMLThresholdTags_MAP.containsKey(qName)) {
			qNameEnum = OMNICARE_XMLThresholdTags_MAP.get(qName);
		} else {
			String msg = "SAXHandlerThresholds class: endElement method: unknown XML tag '" + qName + "'";
			throw new RuntimeException(msg);
		}
		
		XMLThresholdTags tagNameEnum;
		if (OMNICARE_XMLThresholdTags_MAP.containsKey(tagName)) {
			tagNameEnum = OMNICARE_XMLThresholdTags_MAP.get(tagName);
		} else {
			String msg = "SAXHandlerThresholds class: endElement method: unknown XML tag '" + tagName + "'";
			throw new RuntimeException(msg);
		}
		switch (qNameEnum) {
		case threshold:
			String name = threshold.getName();
			if (!thresholds.containsKey(name)) {
				thresholds.put(threshold.getName(), threshold);
			} else {
				String msg = "SAXHandlerThresholds class: endElement method: duplicate threshold name '" + name + "' in the thresholds XML file";
				throw new RuntimeException(msg);
			}
			metricNames.add(threshold.getMetricName());
			break;
		case upper_severe:
			switch (tagNameEnum) {
			case threshold:
				threshold.setUpperSevere(getDouble(content, XMLThresholdTags.upper_severe, XMLThresholdTags.threshold));
				content = "";
				break;
			default:
				break;
			}
			break;
		case upper_warning:
			switch (tagNameEnum) {
			case threshold:
				threshold.setUpperWarning(getDouble(content, XMLThresholdTags.upper_warning, XMLThresholdTags.threshold));
				content = "";
				break;
			default:
				break;
			}
			break;
		case lower_warning:
			switch (tagNameEnum) {
			case threshold:
				threshold.setLowerWarning(getDouble(content, XMLThresholdTags.lower_warning, XMLThresholdTags.threshold));
				content = "";
				break;
			default:
				break;
			}
			break;
		case lower_severe:
			switch (tagNameEnum) {
			case threshold:
				threshold.setLowerSevere(getDouble(content, XMLThresholdTags.lower_severe, XMLThresholdTags.threshold));
				content = "";
				break;
			default:
				break;
			}
			break;
		
		default:
			break;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("SAXHandlerThresholds class: Entering characters method");
		}
		content = String.copyValueOf(ch, start, length).trim();
		if (log.isLoggable(Level.FINER)) {
			log.finer("SAXHandlerThresholds class: characters method: content is '" + content + "'");
		}
	}
	
	private Double getDouble(String value, XMLThresholdTags tag, XMLThresholdTags parentTag) {
		if (value == null) {
			return null;
		}

		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			String msg = "getDouble method: NumberFormatException was thrown. The value '"
					+ value
					+ "' cannot be converted to double. Parent tag name is '"
					+ parentTag.name()
					+ "', tag name is '"
					+ tag.name()
					+ "'. Stacktrace is '"
					+ HelperUtils.getExceptionAsString(e) + "'";
			log.severe(msg);
			throw new RuntimeException(msg);
		}

	}

	public Map<String, Threshold> getThresholds() {
		return thresholds;
	}
}
