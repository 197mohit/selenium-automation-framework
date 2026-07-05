package com.paytm.framework.utils.email;

import com.paytm.framework.utils.CommonUtils;

public class EmailMessage {
	private String subject;
	private String from;
	private String to[];
	private String cc[];
	private String bcc[];
	private String plainText;
	private String htmlText;

	public EmailMessage(){
		this.subject = "";
		this.from = "";
		this.plainText = "";
		this.htmlText = "";
		this.to = new String[0];
		this.cc = new String[0];
		this.bcc = new String[0];
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String[] getTo() {
		return to;
	}

	public void setTo(String[] to) {
		this.to = to;
	}

	public String[] getCc() {
		return cc;
	}

	public void setCc(String[] cc) {
		this.cc = cc;
	}

	public String[] getBcc() {
		return bcc;
	}

	public void setBcc(String[] bcc) {
		this.bcc = bcc;
	}

	public String getPlainText() {
		return plainText;
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

	public String getHtmlText() {
		return (CommonUtils.getCompressedHTML(htmlText));
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

}
