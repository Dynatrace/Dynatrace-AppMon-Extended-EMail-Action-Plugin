package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.mail.Address;

/**
 * @author eugene.turetsky
 * 
 */
public class MailPluginProperties {
	private Properties hostNameToIpAddress;
	private String hostNameToIpAddressRaw;
	private boolean emailsFiltersCoupled;
	private String emailsFiltersDependencyFile;
	private Address froms;
	private List<Address> tos;
	private List<Address> cc;
	private List<Address> bcc;
	private boolean htmlFormat;
	private String subjectPrefix;
	private String subject;
	private String subjectSuffix;
	private String bodyHeader;
	private String body;
	private String bodyFooter;
	private String[] dashboards;
	private String dashboardsType;
	private String smtpHost;
	private Long smtpPort;
	private int quietTimeFrom;
	private int quietTimeTo;
	private boolean SmtpUserPassword;
	private String smtpUser;
	private String smtpPassword;
	private boolean smtpSsl;
	private String dtHost;
	private Long dtPort;
	private String dtUser;
	private String dtPassword;
	private String systemProfileName;
	private Map<String, Pattern> agents;
	private Map<String, Pattern> agentServers;
	private Map<String, Pattern> agentGroups;
	private Map<String, Pattern> monitors;
	private Map<String, Pattern> monitorServers;
	private Map<String, Pattern> collectors;
	private Map<String, Pattern> collectorServers;
	private Map<String, Pattern> servers;
	private Map<String, Pattern> sendOnlyPatterns;
	private Map<String, Pattern> measureNamePatterns;
	private Map<String, Pattern> measureNamePatternsExtended;
	private String urlPrefix;
	private String urlSuffix;
	private Map<String, String> dashboardUrls;
	private boolean filterAgentNameHost;
	private boolean filterAgentGroup;
	private boolean filterCustomTimeframe;
	private URL footerUrl;
	private List<Filter> filters;
	private String incidentRuleName;
	private String identityString;
	private String thresholdsFile;
	private Map<String, Threshold> thresholds;
	private Set<String> metricNames;
		
	public Properties getHostNameToIpAddress() {
		return hostNameToIpAddress;
	}
	public void setHostNameToIpAddress(Properties hostNameToIpAddress) {
		this.hostNameToIpAddress = hostNameToIpAddress;
	}
	public String getHostNameToIpAddressRaw() {
		return hostNameToIpAddressRaw;
	}
	public void setHostNameToIpAddressRaw(String hostNameToIpAddressRaw) {
		this.hostNameToIpAddressRaw = hostNameToIpAddressRaw;
	}
	public boolean isEmailsFiltersCoupled() {
		return emailsFiltersCoupled;
	}
	public void setEmailsFiltersCoupled(boolean emailsFiltersCoupled) {
		this.emailsFiltersCoupled = emailsFiltersCoupled;
	}
	public String getEmailsFiltersDependencyFile() {
		return emailsFiltersDependencyFile;
	}
	public void setEmailsFiltersDependencyFile(String emailsFiltersDependencyFile) {
		this.emailsFiltersDependencyFile = emailsFiltersDependencyFile;
	}
	public Address getFroms() {
		return froms;
	}
	public void setFroms(Address froms) {
		this.froms = froms;
	}
	public List<Address> getTos() {
		return tos;
	}
	public void setTos(List<Address> tos) {
		this.tos = tos;
	}
	public List<Address> getCc() {
		return cc;
	}
	public void setCc(List<Address> cc) {
		this.cc = cc;
	}
	public List<Address> getBcc() {
		return bcc;
	}
	public void setBcc(List<Address> bcc) {
		this.bcc = bcc;
	}
	public synchronized boolean isHtmlFormat() {
		return htmlFormat;
	}
	public synchronized void setHtmlFormat(boolean htmlFormat) {
		this.htmlFormat = htmlFormat;
	}
	public String getSubjectPrefix() {
		return subjectPrefix;
	}
	public void setSubjectPrefix(String text) {
		this.subjectPrefix = text;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSubjectSuffix() {
		return subjectSuffix;
	}
	public void setSubjectSuffix(String subjectSuffix) {
		this.subjectSuffix = subjectSuffix;
	}
	public String getBodyHeader() {
		return bodyHeader;
	}
	public void setBodyHeader(String bodyHeader) {
		this.bodyHeader = bodyHeader;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getBodyFooter() {
		return bodyFooter;
	}
	public void setBodyFooter(String bodyFooter) {
		this.bodyFooter = bodyFooter;
	}
	public String[] getDashboards() {
		return dashboards;
	}
	public void setDashboards(String[] dashboards) {
		this.dashboards = dashboards;
	}
	public synchronized String getDashboardsType() {
		return dashboardsType;
	}
	public synchronized void setDashboardsType(String dashboardsType) {
		this.dashboardsType = dashboardsType;
	}
	public String getSmtpHost() {
		return smtpHost;
	}
	public void setSmtpHost(String mailHost) {
		this.smtpHost = mailHost;
	}
	public Long getSmtpPort() {
		return smtpPort;
	}
	public void setSmtpPort(Long mailPort) {
		this.smtpPort = mailPort;
	}
	public synchronized int getQuietTimeFrom() {
		return quietTimeFrom;
	}
	public synchronized void setQuietTimeFrom(int quietTimeFrom) {
		this.quietTimeFrom = quietTimeFrom;
	}
	public synchronized int getQuietTimeTo() {
		return quietTimeTo;
	}
	public synchronized void setQuietTimeTo(int quietTimeTo) {
		this.quietTimeTo = quietTimeTo;
	}
	public boolean isSmtpUserPassword() {
		return SmtpUserPassword;
	}
	public void setSmtpUserPassword(boolean smtpUserPassword) {
		SmtpUserPassword = smtpUserPassword;
	}
	public String getSmtpUser() {
		return smtpUser;
	}
	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}
	public String getSmtpPassword() {
		return smtpPassword;
	}
	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}
	public boolean isSmtpSsl() {
		return smtpSsl;
	}
	public void setSmtpSsl(boolean smtpSsl) {
		this.smtpSsl = smtpSsl;
	}
	public String getDtHost() {
		return dtHost;
	}
	public void setDtHost(String dtHost) {
		this.dtHost = dtHost;
	}
	public Long getDtPort() {
		return dtPort;
	}
	public void setDtPort(Long dtPort) {
		this.dtPort = dtPort;
	}
	public String getDtUser() {
		return dtUser;
	}
	public void setDtUser(String dtUser) {
		this.dtUser = dtUser;
	}
	public String getDtPassword() {
		return dtPassword;
	}
	public void setDtPassword(String dtPassword) {
		this.dtPassword = dtPassword;
	}
	public String getSystemProfileName() {
		return systemProfileName;
	}
	public void setSystemProfileName(String systemProfileName) {
		this.systemProfileName = systemProfileName;
	}
	public Map<String, Pattern> getAgents() {
		return agents;
	}
	public void setAgents(Map<String, Pattern> agents) {
		this.agents = agents;
	}
	public Map<String, Pattern> getAgentServers() {
		return agentServers;
	}
	public void setAgentServers(Map<String, Pattern> agentServers) {
		this.agentServers = agentServers;
	}
	public Map<String, Pattern> getAgentGroups() {
		return agentGroups;
	}
	public void setAgentGroups(Map<String, Pattern> agentGroups) {
		this.agentGroups = agentGroups;
	}
	public Map<String, Pattern> getMonitors() {
		return monitors;
	}
	public void setMonitors(Map<String, Pattern> monitors) {
		this.monitors = monitors;
	}
	public Map<String, Pattern> getMonitorServers() {
		return monitorServers;
	}
	public void setMonitorServers(Map<String, Pattern> monitorServers) {
		this.monitorServers = monitorServers;
	}
	public Map<String, Pattern> getCollectors() {
		return collectors;
	}
	public void setCollectors(Map<String, Pattern> collectors) {
		this.collectors = collectors;
	}
	public Map<String, Pattern> getCollectorServers() {
		return collectorServers;
	}
	public void setCollectorServers(Map<String, Pattern> collectorServers) {
		this.collectorServers = collectorServers;
	}
	public Map<String, Pattern> getServers() {
		return servers;
	}
	public void setServers(Map<String, Pattern> hosts) {
		this.servers = hosts;
	}
	public Map<String, Pattern> getSendOnlyPatterns() {
		return sendOnlyPatterns;
	}
	public void setSendOnlyPatterns(Map<String, Pattern> sendOnlyPatterns) {
		this.sendOnlyPatterns = sendOnlyPatterns;
	}
	public Map<String, Pattern> getMeasureNamePatterns() {
		return measureNamePatterns;
	}
	public void setMeasureNamePatterns(Map<String, Pattern> measureNamePatterns) {
		this.measureNamePatterns = measureNamePatterns;
	}
	public Map<String, Pattern> getMeasureNamePatternsExtended() {
		return measureNamePatternsExtended;
	}
	public void setMeasureNamePatternsExtended(
			Map<String, Pattern> measureNamePatternsExtended) {
		this.measureNamePatternsExtended = measureNamePatternsExtended;
	}
	public String getUrlPrefix() {
		return urlPrefix;
	}
	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}
	public String getUrlSuffix() {
		return urlSuffix;
	}
	public void setUrlSuffix(String urlSuffix) {
		this.urlSuffix = urlSuffix;
	}
	public String getDashboardUrl(String dashboard) {
		return dashboardUrls.get(dashboard);
	}
	public Map<String, String> getDashboardUrls() {
		return dashboardUrls;
	}
	public void setDashboardUrls(Map<String, String> dashboardUrls) {
		this.dashboardUrls = dashboardUrls;
	}
	public boolean isFilterAgentNameHost() {
		return filterAgentNameHost;
	}
	public void setFilterAgentNameHost(boolean filterAgentNameHost) {
		this.filterAgentNameHost = filterAgentNameHost;
	}
	public boolean isFilterAgentGroup() {
		return filterAgentGroup;
	}
	public void setFilterAgentGroup(boolean filterAgentGroup) {
		this.filterAgentGroup = filterAgentGroup;
	}
	public boolean isFilterCustomTimeframe() {
		return filterCustomTimeframe;
	}
	public void setFilterCustomTimeframe(boolean filterCustomTimeframe) {
		this.filterCustomTimeframe = filterCustomTimeframe;
	}
	public synchronized URL getFooterUrl() {
		return footerUrl;
	}
	public synchronized void setFooterUrl(URL footerUrl) {
		this.footerUrl = footerUrl;
	}
	public List<Filter> getFilters() {
		return filters;
	}
	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	public String getIncidentRuleName() {
		return incidentRuleName;
	}
	public void setIncidentRuleName(String incidentRuleName) {
		this.incidentRuleName = incidentRuleName;
	}
	public String getIdentityString() {
		return identityString;
	}
	public void setIdentityString(String identityString) {
		this.identityString = identityString;
	}
	public String getThresholdsFile() {
		return thresholdsFile;
	}
	public void setThresholdsFile(String thresholdsFile) {
		this.thresholdsFile = thresholdsFile;
	}
	public Map<String, Threshold> getThresholds() {
		return thresholds;
	}
	public void setThresholds(Map<String, Threshold> thresholds) {
		this.thresholds = thresholds;
	}
	public Set<String> getMetricNames() {
		return metricNames;
	}
	public void setMetricNames(Set<String> metricNames) {
		this.metricNames = metricNames;
	}	
}
