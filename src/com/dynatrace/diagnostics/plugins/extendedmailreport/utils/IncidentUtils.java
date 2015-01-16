package com.dynatrace.diagnostics.plugins.extendedmailreport.utils;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import com.dynatrace.diagnostics.pdk.Duration;
import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.Incident.Severity;
//import com.dynatrace.diagnostics.pdk.Incident.Severity;
import com.dynatrace.diagnostics.pdk.IncidentRule;
import com.dynatrace.diagnostics.pdk.Sensitivity;
import com.dynatrace.diagnostics.pdk.Sensitivity.Type;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.ActionRecord;
import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.MessagesConstants;
//import com.dynatrace.diagnostics.sdk.ui.resources.MessageConstants;
import com.dynatrace.diagnostics.sdk.resources.BaseConstants;

/**
 * @author eugene.turetsky
 * 
 */
public class IncidentUtils implements MailPluginConstants {
	
	static int seq = 0;
	static String hostname;

	static {
		try {
			hostname = java.net.InetAddress.getLocalHost()
					.getCanonicalHostName();
		} catch (UnknownHostException e) {
			hostname = new Random(System.currentTimeMillis()).nextInt(100000)
					+ ".localhost";
		}
	}
	
	public static Type getSensitivityType(ActionRecord record) {
		// Custom, Immediate, Medium, Low, PerViolation, After60s;
		//TODO using the first element of Incidents collection. Are there any examples when Incidents collection has more than one incident?
		Type sensitivityType = null;
		Sensitivity sensitivity;
		if (record != null && record.getIncidents() != null && !record.getIncidents().isEmpty() && record.getIncidents().get(FIRST_ELEMENT_COLLECTION) != null) {
			IncidentRule incidentRule = record.getIncidents().get(FIRST_ELEMENT_COLLECTION).getIncidentRule();
			if (incidentRule != null && (sensitivity = incidentRule.getSensitivity()) != null) {
				sensitivityType = sensitivity.getType();
			}
		}
		return sensitivityType;
	}
	
	public static Type getSensitivityType(Incident incident) {
		// Custom, Immediate, Medium, Low, PerViolation, After60s;
		//TODO using the first element of Incidents collection. Are there any examples when Incidents collection has more than one incident?
		Type sensitivityType = null;
		Sensitivity sensitivity;
		if (incident != null) {
			IncidentRule incidentRule = incident.getIncidentRule();
			if (incidentRule != null && (sensitivity = incidentRule.getSensitivity()) != null) {
				sensitivityType = sensitivity.getType();
			}
		}
		return sensitivityType;
	}
	
	public static long getDuration(ActionRecord record) {
		Duration duration;
		List<Incident> incidents;
		Incident incident;
		if (record != null && (incidents = record.getIncidents()) != null && !incidents.isEmpty() && (incident = incidents.get(FIRST_ELEMENT_COLLECTION)) != null 
				&& (duration = incident.getDuration()) != null) {
			return duration.getDurationInMs();
		}
			
		return DURATION_IS_NULL;
	}
	
	public static long getDuration(Incident incident) {
		Duration duration;
		if (incident != null && (duration = incident.getDuration()) != null) {
			return duration.getDurationInMs();
		}
			
		return DURATION_IS_NULL;
	}
	
	public static String getIncidentRuleName(ActionRecord record) {
		String incidentRuleName = "";
		if ((record != null) && record.getIncidents() != null && !record.getIncidents().isEmpty() && record.getIncidents().get(FIRST_ELEMENT_COLLECTION) != null) {
			IncidentRule incidentRule = record.getIncidents().get(FIRST_ELEMENT_COLLECTION).getIncidentRule();
			if (incidentRule != null) {
				incidentRuleName = incidentRule.getName();
			}
		}
		return incidentRuleName;
	}
	
	public static String getSeverityString(ActionRecord record) {
		// Enum is Error, Warning, Informational
		Severity severity = record.getIncidents().get(FIRST_ELEMENT_COLLECTION).getSeverity();
		switch (severity) {
		case Error:
			return MessagesConstants.STRING_SEVERE;
		case Warning:
			return MessagesConstants.STRING_WARNING;
		case Informational:
			return MessagesConstants.STRING_INFORMATIONAL;
		}
		
		return BaseConstants.DASH;
	}
	
	public static String getSeverityString(Incident incident) {
		// Enum is Error, Warning, Informational
		if (incident != null) {
			Severity severity = incident.getSeverity();
			switch (severity) {
			case Error:
				return MessagesConstants.STRING_SEVERE;
			case Warning:
				return MessagesConstants.STRING_WARNING;
			case Informational:
				return MessagesConstants.STRING_INFORMATIONAL;
			}
		}
		
		return BaseConstants.DASH;
	}
	
	public static synchronized int getSeq() {
		return (seq++) % 100000;
	}
	
	public static String getContentId() {
		int c = getSeq();
		return c + "." + System.currentTimeMillis() + "@" + hostname;
	}
}
