package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

import java.util.Map;
import java.util.regex.Pattern;

public class ServerFilter {
	private Map<String, Pattern> names;

	public Map<String, Pattern> getNames() {
		return names;
	}

	public void setNames(Map<String, Pattern> names) {
		this.names = names;
	}
}
