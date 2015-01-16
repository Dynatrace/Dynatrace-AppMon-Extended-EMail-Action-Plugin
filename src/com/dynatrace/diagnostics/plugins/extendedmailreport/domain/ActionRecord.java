package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Session;

import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.SourceType;
import com.dynatrace.diagnostics.pdk.Timestamp;
import com.dynatrace.diagnostics.plugin.actionhelper.SourceReferences;

/**
 * @author eugene.turetsky
 * 
 */
public class ActionRecord {
	private String SystemProfileName;
//	private ServerAccessWarrant serverAccess;
	private List<Incident> incidents;
	private String message;
	private String application;
//	private List<String> descriptions;
	private Map<String, String> substitutorMap;
	private String subjectPrefixUpdated;
	private String subjectUpdated;
	private String subjectSuffixUpdated;
	private String bodyHeaderUpdated;
	private String bodyUpdated;
	private String bodyFooterUpdated;
	private Map<SourceType, SourceReferences> sources = new HashMap<SourceType, SourceReferences>();
	private Session smtpSession;
	private String cidStatusIconWarning;
	private String cidStatusIconOk;
	private String cidFooterLogo;
	private String cidSeparatorIcon;
	private String cidSeparatorFooterIcon;
	private Map<String, String> dtBundleResourceMap;
	private Timestamp startTime;
	private Timestamp endTime;
	private Map<String, String> urlRestFilteringMap;
	private String[] dashboards;
	private Map<String, String> dashboardUrls;
	private Filter filter;
	
	
	public String getSystemProfileName() {
		return SystemProfileName;
	}
	public void setSystemProfileName(String systemProfileName) {
		SystemProfileName = systemProfileName;
	}
//	public ServerAccessWarrant getServerAccess() {
//		return serverAccess;
//	}
//	public void setServerAccess(ServerAccessWarrant serverAccess) {
//		this.serverAccess = serverAccess;
//	}
	public List<Incident> getIncidents() {
		return incidents;
	}
	public void setIncidents(List<Incident> incidents) {
		this.incidents = incidents;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	//	public List<String> getDescriptions() {
//		return descriptions;
//	}
//	public void setDescriptions(List<String> descriptions) {
//		this.descriptions = descriptions;
//	}
	public Map<String, String> getSubstitutorMap() {
		return substitutorMap;
	}
	public void setSubstitutorMap(Map<String, String> substitutorMap) {
		this.substitutorMap = substitutorMap;
	}
	public String getSubjectPrefixUpdated() {
		return subjectPrefixUpdated;
	}
	public void setSubjectPrefixUpdated(String subjectPrefixUpdated) {
		this.subjectPrefixUpdated = subjectPrefixUpdated;
	}
	public String getSubjectUpdated() {
		return subjectUpdated;
	}
	public void setSubjectUpdated(String subjectUpdated) {
		this.subjectUpdated = subjectUpdated;
	}
	public String getSubjectSuffixUpdated() {
		return subjectSuffixUpdated;
	}
	public void setSubjectSuffixUpdated(String subjectSuffixUpdated) {
		this.subjectSuffixUpdated = subjectSuffixUpdated;
	}
	public String getBodyHeaderUpdated() {
		return bodyHeaderUpdated;
	}
	public void setBodyHeaderUpdated(String bodyHeaderUpdated) {
		this.bodyHeaderUpdated = bodyHeaderUpdated;
	}
	public String getBodyUpdated() {
		return bodyUpdated;
	}
	public void setBodyUpdated(String bodyUpdated) {
		this.bodyUpdated = bodyUpdated;
	}
	public String getBodyFooterUpdated() {
		return bodyFooterUpdated;
	}
	public void setBodyFooterUpdated(String bodyFooterUpdated) {
		this.bodyFooterUpdated = bodyFooterUpdated;
	}
	public Map<SourceType, SourceReferences> getSources() {
		return sources;
	}
	public void setSources(Map<SourceType, SourceReferences> sources) {
		this.sources = sources;
	}
	public Session getSmtpSession() {
		return smtpSession;
	}
	public void setSmtpSession(Session smtpSession) {
		this.smtpSession = smtpSession;
	}
	public String getCidStatusIconWarning() {
		return cidStatusIconWarning;
	}
	public void setCidStatusIconWarning(String cidStatusIconWarning) {
		this.cidStatusIconWarning = cidStatusIconWarning;
	}
	public String getCidStatusIconOk() {
		return cidStatusIconOk;
	}
	public void setCidStatusIconOk(String cidStatusIconOk) {
		this.cidStatusIconOk = cidStatusIconOk;
	}
	public String getCidFooterLogo() {
		return cidFooterLogo;
	}
	public void setCidFooterLogo(String cidFooterLogo) {
		this.cidFooterLogo = cidFooterLogo;
	}
	public String getCidSeparatorIcon() {
		return cidSeparatorIcon;
	}
	public void setCidSeparatorIcon(String cidSeparatorIcon) {
		this.cidSeparatorIcon = cidSeparatorIcon;
	}
	public String getCidSeparatorFooterIcon() {
		return cidSeparatorFooterIcon;
	}
	public void setCidSeparatorFooterIcon(String cidSeparatorFooterIcon) {
		this.cidSeparatorFooterIcon = cidSeparatorFooterIcon;
	}
	public Map<String, String> getDtBundleResourceMap() {
		return dtBundleResourceMap;
	}
	public void setDtBundleResourceMap(Map<String, String> dtBundleResourceMap) {
		this.dtBundleResourceMap = dtBundleResourceMap;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
	public synchronized Map<String, String> getUrlRestFilteringMap() {
		return urlRestFilteringMap;
	}
	public synchronized void setUrlRestFilteringMap(
			Map<String, String> urlRestFilteringMap) {
		this.urlRestFilteringMap = urlRestFilteringMap;
	}
	public String[] getDashboards() {
		return dashboards;
	}
	public void setDashboards(String[] dashboards) {
		this.dashboards = dashboards;
	}
	public Map<String, String> getDashboardUrls() {
		return dashboardUrls;
	}
	public void setDashboardUrls(Map<String, String> dashboardUrls) {
		this.dashboardUrls = dashboardUrls;
	}
	public Filter getFilter() {
		return filter;
	}
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
}
