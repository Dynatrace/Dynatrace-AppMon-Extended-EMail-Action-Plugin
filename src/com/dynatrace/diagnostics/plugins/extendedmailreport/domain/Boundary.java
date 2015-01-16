package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

public class Boundary {
	double upperSevere = Double.NaN;
	double upperWarning = Double.NaN;
	double lowerWarning = Double.NaN;
	double lowerSevere = Double.NaN;
	
	public double getUpperSevere() {
		return upperSevere;
	}
	public void setUpperSevere(double upperSevere) {
		this.upperSevere = upperSevere;
	}
	public double getUpperWarning() {
		return upperWarning;
	}
	public void setUpperWarning(double upperWarning) {
		this.upperWarning = upperWarning;
	}
	public double getLowerWarning() {
		return lowerWarning;
	}
	public void setLowerWarning(double lowerWarning) {
		this.lowerWarning = lowerWarning;
	}
	public double getLowerSevere() {
		return lowerSevere;
	}
	public void setLowerSevere(double lowerSevere) {
		this.lowerSevere = lowerSevere;
	}
	@Override
	public String toString() {
		return "Boundary [upperSevere=" + upperSevere + ", upperWarning="
				+ upperWarning + ", lowerWarning=" + lowerWarning
				+ ", lowerSevere=" + lowerSevere + "]";
	}

}
