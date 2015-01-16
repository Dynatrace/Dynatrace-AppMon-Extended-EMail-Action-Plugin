package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

public class Threshold extends Boundary {
	private String name;
	private String metricName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	@Override
	public String toString() {
		return "Threshold [name=" + name + ", metricName=" + metricName + ", Boundary [upperSevere="
				+ upperSevere + ", upperWarning=" + upperWarning
				+ ", lowerWarning=" + lowerWarning + ", lowerSevere="
				+ lowerSevere + "]";
	}

}
