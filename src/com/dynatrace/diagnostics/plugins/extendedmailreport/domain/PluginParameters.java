package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.MailPluginConstants;

public class PluginParameters implements MailPluginConstants {
	private String dtHost;
	private long dtPort;  //long
	private String dtUser;
	private String dtPassword;
	private String smtpHost;
	private long smtpPort;
	private boolean smtpSsl; // boolean
	private boolean smtpUserPassword; // boolean
	private String smtpUser;
	private String smtpPassword;
	private String quietTimeFrom;
	private String quietTimeTo;
	private String hostNameToIpAddress;
	private String from;
	private boolean emailsFiltersCoupled; // boolean
	private String emailsFiltersDependencyFile;
	private String to;
	private String cc;
	private String bcc;
	private String agents;
	private String agentServers;
	private String agentGroups;
	private String monitors;
	private String monitorServers;
	private String collectors;
	private String collectorServers;
	private String servers;
	private String sendOnlyPattern;
	private String measureNamePattern;
	private boolean htmlFormat; // boolean;
	private String subjectPrefix;
	private String subject;
	private String subjectSuffix;
	private String bodyHeader;
	private String body;
	private String bodyFooter;
	private String dashboardNames;
	private String dashboardsType;
	private boolean restFilteringAgentNameHost; // boolean
	private boolean restFilteringAgentGroup;	// boolean
	private boolean restFilteringCustomTimeFrame;// boolean
	private String dtServerName;
	private String systemProfile;
	private String incidentRuleName;
	private String identityString;
	private String thresholdsFile;
	
	public String getDtHost() {
		return dtHost;
	}
	public void setDtHost(String dtHost) {
		this.dtHost = dtHost;
	}
	public long getDtPort() {
		return dtPort;
	}
	public void setDtPort(long dtPort) {
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
	public String getSmtpHost() {
		return smtpHost;
	}
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	public long getSmtpPort() {
		return smtpPort;
	}
	public void setSmtpPort(long smtpPort) {
		this.smtpPort = smtpPort;
	}
	public boolean isSmtpSsl() {
		return smtpSsl;
	}
	public void setSmtpSsl(boolean smtpSsl) {
		this.smtpSsl = smtpSsl;
	}
	public boolean isSmtpUserPassword() {
		return smtpUserPassword;
	}
	public void setSmtpUserPassword(boolean smtpUserPassword) {
		this.smtpUserPassword = smtpUserPassword;
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
	public String getQuietTimeFrom() {
		return quietTimeFrom;
	}
	public void setQuietTimeFrom(String quietTimeFrom) {
		this.quietTimeFrom = quietTimeFrom;
	}
	public String getQuietTimeTo() {
		return quietTimeTo;
	}
	public void setQuietTimeTo(String quietTimeTo) {
		this.quietTimeTo = quietTimeTo;
	}
	public String getHostNameToIpAddress() {
		return hostNameToIpAddress;
	}
	public void setHostNameToIpAddress(String hostNameToIpAddress) {
		this.hostNameToIpAddress = hostNameToIpAddress;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
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
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public String getBcc() {
		return bcc;
	}
	public void setBcc(String bcc) {
		this.bcc = bcc;
	}
	public String getAgents() {
		return agents;
	}
	public void setAgents(String agents) {
		this.agents = agents;
	}
	public String getAgentServers() {
		return agentServers;
	}
	public void setAgentServers(String agentServers) {
		this.agentServers = agentServers;
	}
	public String getAgentGroups() {
		return agentGroups;
	}
	public void setAgentGroups(String agentGroups) {
		this.agentGroups = agentGroups;
	}
	public String getMonitors() {
		return monitors;
	}
	public void setMonitors(String monitors) {
		this.monitors = monitors;
	}
	public String getMonitorServers() {
		return monitorServers;
	}
	public void setMonitorServers(String monitorServers) {
		this.monitorServers = monitorServers;
	}
	public String getCollectors() {
		return collectors;
	}
	public void setCollectors(String collectors) {
		this.collectors = collectors;
	}
	public String getCollectorServers() {
		return collectorServers;
	}
	public void setCollectorServers(String collectorServers) {
		this.collectorServers = collectorServers;
	}
	public String getServers() {
		return servers;
	}
	public void setServers(String servers) {
		this.servers = servers;
	}
	public String getSendOnlyPattern() {
		return sendOnlyPattern;
	}
	public void setSendOnlyPattern(String sendOnlyPattern) {
		this.sendOnlyPattern = sendOnlyPattern;
	}
	public String getMeasureNamePattern() {
		return measureNamePattern;
	}
	public void setMeasureNamePattern(String measureNamePattern) {
		this.measureNamePattern = measureNamePattern;
	}
	public boolean isHtmlFormat() {
		return htmlFormat;
	}
	public void setHtmlFormat(boolean htmlFormat) {
		this.htmlFormat = htmlFormat;
	}
	public String getSubjectPrefix() {
		return subjectPrefix;
	}
	public void setSubjectPrefix(String subjectPrefix) {
		this.subjectPrefix = subjectPrefix;
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
	public String getDashboardNames() {
		return dashboardNames;
	}
	public void setDashboardNames(String dashboardNames) {
		this.dashboardNames = dashboardNames;
	}
	public String getDashboardsType() {
		return dashboardsType;
	}
	public void setDashboardsType(String dashboardsType) {
		this.dashboardsType = dashboardsType;
	}
	public boolean isRestFilteringAgentNameHost() {
		return restFilteringAgentNameHost;
	}
	public void setRestFilteringAgentNameHost(boolean restFilteringAgentNameHost) {
		this.restFilteringAgentNameHost = restFilteringAgentNameHost;
	}
	public boolean isRestFilteringAgentGroup() {
		return restFilteringAgentGroup;
	}
	public void setRestFilteringAgentGroup(boolean restFilteringAgentGroup) {
		this.restFilteringAgentGroup = restFilteringAgentGroup;
	}
	public boolean isRestFilteringCustomTimeFrame() {
		return restFilteringCustomTimeFrame;
	}
	public void setRestFilteringCustomTimeFrame(boolean restFilteringCustomTimeFrame) {
		this.restFilteringCustomTimeFrame = restFilteringCustomTimeFrame;
	}
	
	public String getDtServerName() {
		return dtServerName;
	}
	public void setDtServerName(String dtServerName) {
		this.dtServerName = dtServerName;
	}
	public String getSystemProfile() {
		return systemProfile;
	}
	public void setSystemProfile(String systemProfile) {
		this.systemProfile = systemProfile;
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
	@Override
	public String toString() {
		return new StringBuffer(incidentRuleName)
				.append("|").append(identityString)
				.append("|").append(dtHost)
				.append("|").append(dtPort)
				.append("|").append(dtUser)
				.append("|").append(dtPassword)
				.append("|").append(smtpHost)
				.append("|").append(smtpPort)
				.append("|").append(smtpSsl)
				.append("|").append(smtpUserPassword)
				.append("|").append(smtpUser)
				.append("|").append(smtpPassword)
				.append("|").append(quietTimeFrom)
				.append("|").append(quietTimeTo)
				.append("|").append(hostNameToIpAddress)
				.append("|").append(from)
				.append("|").append(emailsFiltersCoupled)
				.append("|").append(emailsFiltersDependencyFile)
				.append("|").append(to)
				.append("|").append(cc)
				.append("|").append(bcc)
				.append("|").append(agents)
				.append("|").append(agentServers)
				.append("|").append(agentGroups)
				.append("|").append(monitors)
				.append("|").append(monitorServers)
				.append("|").append(collectors)
				.append("|").append(collectorServers)
				.append("|").append(servers)
				.append("|").append(sendOnlyPattern)
				.append("|").append(measureNamePattern)
				.append("|").append(htmlFormat)
				.append("|").append(subjectPrefix)
				.append("|").append(subject)
				.append("|").append(subjectSuffix)
				.append("|").append(bodyHeader)
				.append("|").append(body)
				.append("|").append(bodyFooter)
				.append("|").append(dashboardNames)
				.append("|").append(dashboardsType)
				.append("|").append(restFilteringAgentNameHost)
				.append("|").append(restFilteringAgentGroup)
				.append("|").append(restFilteringCustomTimeFrame)
				.append("|").append(dtServerName)
				.append("|").append(systemProfile)
				.append("|").append(thresholdsFile)
				.toString();
	}
	
	
}
