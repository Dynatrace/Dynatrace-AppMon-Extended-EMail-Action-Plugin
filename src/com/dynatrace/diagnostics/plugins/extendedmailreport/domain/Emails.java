package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

import java.util.List;

import javax.mail.Address;

public class Emails {
	private List<Address> tos;
	private List<Address> cc;
	private List<Address> bcc;
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
	
	
}
