package com.dynatrace.diagnostics.plugins.extendedmailreport.utils;

import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

/**
 * @author eugene.turetsky
 * 
 */
public class EmailConfigAction {
	private MimeMessage email;
	private String subject;
	private String htmlText;
	private String plainText;
	private MimeBodyPart mbp1;
	private Multipart mp;
	
	public MimeMessage getEmail() {
		return email;
	}
	public void setEmail(MimeMessage email) {
		this.email = email;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getHtmlText() {
		return htmlText;
	}
	public void setHtmlText(String htmlContent) {
		this.htmlText = htmlContent;
	}
	public String getPlainText() {
		return plainText;
	}
	public void setPlainText(String plainTextContent) {
		this.plainText = plainTextContent;
	}
	public MimeBodyPart getMbp1() {
		return mbp1;
	}
	public void setMbp1(MimeBodyPart mbp1) {
		this.mbp1 = mbp1;
	}
	public Multipart getMp() {
		return mp;
	}
	public void setMp(Multipart mp) {
		this.mp = mp;
	}
}
