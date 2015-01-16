package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

import java.util.Map;
import java.util.regex.Pattern;

public class MonitorFilter {
	private Map<String, Pattern> names;
	private Map<String, Pattern> hosts;
	
	public Map<String, Pattern> getNames() {
		return names;
	}
	public void setNames(Map<String, Pattern> names) {
		this.names = names;
	}
	public Map<String, Pattern> getHosts() {
		return hosts;
	}
	public void setHosts(Map<String, Pattern> hosts) {
		this.hosts = hosts;
	}
}
