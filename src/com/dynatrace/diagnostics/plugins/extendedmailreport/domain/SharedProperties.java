package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

public class SharedProperties {
	
	private MailPluginProperties mailPluginProperties;
	private long createTime;
	private long lastRunTime;
	private String dtServer;
	private String systemProfile;
	private String id;
	private long updateDateEmailsFiltersFile;
	private long updateDateThresholdsFile;
	
	public MailPluginProperties getMailPluginProperties() {
		return mailPluginProperties;
	}
	public void setMailPluginProperties(MailPluginProperties mailPluginProperties) {
		this.mailPluginProperties = mailPluginProperties;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public long getLastRunTime() {
		return lastRunTime;
	}
	public void setLastRunTime(long lastRunTime) {
		this.lastRunTime = lastRunTime;
	}
	public String getDtServer() {
		return dtServer;
	}
	public void setDtServer(String dtServer) {
		this.dtServer = dtServer;
	}
	public String getSystemProfile() {
		return systemProfile;
	}
	public void setSystemProfile(String systemProfile) {
		this.systemProfile = systemProfile;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getUpdateDateEmailsFiltersFile() {
		return updateDateEmailsFiltersFile;
	}
	public void setUpdateDateEmailsFiltersFile(long updateDateEmailsFiltersFile) {
		this.updateDateEmailsFiltersFile = updateDateEmailsFiltersFile;
	}
	
	public long getUpdateDateThresholdsFile() {
		return updateDateThresholdsFile;
	}
	public void setUpdateDateThresholdsFile(long updateDateThresholdsFile) {
		this.updateDateThresholdsFile = updateDateThresholdsFile;
	}
	public void clean() {
		createTime = 0;
		lastRunTime = 0;
		dtServer = null;
		systemProfile = null;
		id = null;
		updateDateEmailsFiltersFile = 0;
		updateDateThresholdsFile = 0;
	}

}
