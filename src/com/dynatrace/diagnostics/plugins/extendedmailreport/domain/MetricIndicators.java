package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

public class MetricIndicators {
	boolean metricNameInFilter;
	boolean dynamicMeasureReturn;
	public MetricIndicators(boolean metricNameInFilter, boolean dynamicMeasureReturn) {
		this.metricNameInFilter = metricNameInFilter;
		this.dynamicMeasureReturn = dynamicMeasureReturn;
	}
	public boolean isMetricNameInFilter() {
		return metricNameInFilter;
	}
	public boolean isDynamicMeasureReturn() {
		return dynamicMeasureReturn;
	}
	@Override
	public String toString() {
		return "MetricIndicators [metricNameInFilter=" + metricNameInFilter
				+ ", dynamicMeasureReturn=" + dynamicMeasureReturn + "]";
	}
}
