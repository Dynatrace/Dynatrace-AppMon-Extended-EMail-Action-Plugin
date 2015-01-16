package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

import java.util.Map;
import java.util.regex.Pattern;

public class AgentGroupFilter {
	private Map<String, Pattern> agentGroupFilter;

	public Map<String, Pattern> getAgentGroupFilter() {
		return agentGroupFilter;
	}

	public void setAgentGroupFilter(Map<String, Pattern> agentGroupFilter) {
		this.agentGroupFilter = agentGroupFilter;
	}
}
