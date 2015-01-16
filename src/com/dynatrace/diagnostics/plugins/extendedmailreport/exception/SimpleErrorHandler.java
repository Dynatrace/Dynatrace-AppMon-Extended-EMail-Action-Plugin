package com.dynatrace.diagnostics.plugins.extendedmailreport.exception;

import java.util.logging.Logger;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.SAXHandler;

public class SimpleErrorHandler implements ErrorHandler {
	private static final Logger log = Logger.getLogger(SimpleErrorHandler.class.getName());
	
	@Override
	public void error(SAXParseException e) throws SAXException {
		log.severe("error handler: message is '" + HelperUtils.getExceptionAsString(e) + "'");
		throw e;
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		log.severe("fatalError handler: message is '" + HelperUtils.getExceptionAsString(e) + "'");
		throw e;
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		log.finer("warning handler: message is '" + HelperUtils.getExceptionAsString(e) + "'");
		throw e;
	}

}
