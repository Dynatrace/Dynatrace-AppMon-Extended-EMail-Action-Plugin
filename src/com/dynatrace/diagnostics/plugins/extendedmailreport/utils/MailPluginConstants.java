package com.dynatrace.diagnostics.plugins.extendedmailreport.utils;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.XMLTags;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.XMLThresholdTags;

/**
 * @author eugene.turetsky
 * 
 */
public interface MailPluginConstants {
	// Plugin's configuration parameter's constants
	public static final String CONFIG_HOST_NAME_TO_IP_ADDRESS = "HostNameToIpAddress";
	public static final String CONFIG_ARE_EMAILS_FILTERS_COUPLED = "emailsFiltersCoupled";
	public static final String CONFIG_EMAILS_FILTERS_DEPENDENCY_FILE = "emailsFiltersDependencyFile";
	public static final String CONFIG_FROM = "From";
	public static final String CONFIG_TO = "To";
	public static final String CONFIG_CC = "CC";
	public static final String CONFIG_BCC = "BCC";
	public static final String CONFIG_HTML_FORMAT = "HTMLFormat";
	public static final String CONFIG_SUBJECT_PREFIX = "SubjectPrefix";
	public static final String CONFIG_SUBJECT = "Subject";
	public static final String CONFIG_SUBJECT_SUFFIX = "SubjectSuffix";
	public static final String CONFIG_BODY_HEADER = "BodyHeader";
	public static final String CONFIG_BODY = "Body";
	public static final String CONFIG_BODY_FOOTER = "BodyFooter";
	public static final String CONFIG_DASHBOARD = "DashboardNames";
	public static final String CONFIG_DASHBOARDS_TYPE = "DashboardsType";
	public static final String CONFIG_REST_FILTERING_AGENT_NAME_HOST = "RESTFilteringAgentNameHost";
	public static final String CONFIG_REST_FILTERING_AGENT_GROUP = "RESTFilteringAgentGroup";
	public static final String CONFIG_REST_FILTERING_CUSTOM_TIMEFRAME = "RESTFilteringCustomTimeframe";
	public static final String CONFIG_SMTP_HOST = "SMTPHost";
	public static final String CONFIG_SMTP_PORT = "SMTPPort";
	public static final String CONFIG_SMTP_SSL = "SMTPSSL";
	public static final String CONFIG_QUIET_TIME_FROM = "QuietTimeFrom";
	public static final String CONFIG_QUIET_TIME_TO = "QuietTimeTo";
	public static final String CONFIG_SMTP_USER_PASSWORD = "SmtpUserPassword";
	public static final String CONFIG_SMTP_USER = "SMTPUser";
	public static final String CONFIG_SMTP_PASSWORD = "SMTPPassword";
	public static final String CONFIG_DT_HOST = "DTHost";
	public static final String CONFIG_DT_PORT = "DTPort";
	public static final String CONFIG_DT_USER = "DTUser";
	public static final String CONFIG_DT_PASSWORD = "DTPassword";
	public static final String CONFIG_AGENTS = "Agents";
	public static final String CONFIG_AGENT_SERVERS = "agentServers";
	public static final String CONFIG_SERVERS = "Servers";
	public static final String CONFIG_AGENT_GROUPS = "AgentGroups";
	public static final String CONFIG_MONITORS = "Monitors";
	public static final String CONFIG_MONITOR_SERVERS = "monitorServers";
	public static final String CONFIG_COLLECTORS = "Collectors";
	public static final String CONFIG_COLLECTOR_SERVERS = "collectorServers";
	public static final String CONFIG_SEND_ONLY_PATTERNS = "sendOnlyPatterns";
	public static final String CONFIG_INCIDENT_RULE_NAME = "incidentRuleName";
	public static final String CONFIG_IDENTIFYING_STRING = "identityString";
	public static final String CONFIG_THRESHOLDS_FILE = "thresholdsFile";
	public static final String CONFIG_MEASURE_NAME_PATTERNS = "measureNamePatterns";
	
	// Miscellaneous
	public static final Status STATUS_SUCCESS = new Status(Status.StatusCode.Success);
	public static final Status STATUS_PARTIAL_SUCCESS = new Status(Status.StatusCode.PartialSuccess);
	public static final int DEFAULT_STRING_LENGTH = 256;
//	public static final String MAIL_SEPARATOR = ";";
//	public static final String DASHBOARD_SEPARATOR = ";";
	public static final String[] EMPTY_STRINGS = {""};
	public static final String EMPTY_STRING = "";
	public static final String SYSPROP_BULKMAIL = "com.dynatrace.diagnostics.incidents.bulkMail";
	public static final boolean BULKMAIL_DEFAULT = true;
	public static final String BACKUP_FROM_EMAIL_ADDRESS = "alerting@compuware.com";
	public static final int FIRST_ELEMENT_COLLECTION = 0;
	public static final long DURATION_IS_NULL = Long.MIN_VALUE;
	public static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#.##");   
	public static final String SMTPS = "smtps";
	public static final String SMTP = "smtp";
	public static final String DT_BUNDLE_RESOURCE_PREFIX = "dtbundleresource:";
	public static final int LENGTH_DT_BUNDLE_RESOURCE_PREFIX = DT_BUNDLE_RESOURCE_PREFIX.length();
	public static final String WARNING_ICON = "notification_email_warning.png";
	public static final String OK_ICON = "notification_email_ok.png";
	public static final String CANONICAL_NAME_TRANSACTION_MEASURE = "com.dynatrace.diagnostics.core.realtime.measures.TransactionMeasure";
	public static final String UTF8 = "UTF-8";
	public static final String[] UNIT_TO_STRING = {"none", "ns", "ms", "s", "min", "h", "number", "bytes", "kilobytes", "megabytes", "gigabytes", "occurrences", "percent", "count", "auto"};
	public final static String HTML_STYLE = "<style type=\"text/css\"> body {font-family:Arial;} </style>";
	public final static String HTML_BODY_BEGIN = "<body style=\"font-family: 'Arial'; margin: 24px; color:#505050;\">";
	public final static String HTML_BEGIN_TITLE_TABLE = "<table style=\"border:0px;\" cellpadding=\"0\" cellspacing=\"0\">";
	public final static String HTML_BEGIN_TITLE_IMAGE_CELL = "<td style=\"width: 5px; margin-right: 8px;\">";
	public final static String HTML_BEGIN_TITLE_CELL = "<td style=\"width: 795px;\">";
	public final static String HTML_BEGIN_TITLE_DESCRIPTION_CELL = "<td colspan=\"2\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_TITLE_DESCRIPTION_DIV = "<div style=\"font-size: 0.8em; color:#737373;margin-top: 5px; width: 800px;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_HEADLINE_DIV = "<div style=\"font-weight:bold;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_DETAILS_TABLE_LABEL = "<td style=\"vertical-align: top; width: 180px;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_DETAILS_LABEL_DIV = "<div style=\"font-size: 0.8em;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_DETAILS_CONTENT_DIV = "<div style=\"font-size: 0.8em;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_TITLE_HEADLINE_CONTENT_DIV = "<div style=\"font-size: 1.5em; font-weight:bold;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_FOOTER_CLIENT_LINK = "<td style=\"width: 140px;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_FOOTER_REPORT_LINK = "<td style=\"width: 125px;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_FOOTER_LOGO = "<td style=\"width: 200px;\">"; //$NON-NLS-1$
	public final static String IMAGE_NOTIFICATION_EMAIL_OK = "<img src=\"dtbundleresource://img/notification_email_ok.png\"/>"; //$NON-NLS-1$
	public final static String IMAGE_NOTIFICATION_EMAIL_OK_PART2 = "\" />";
	public final static String IMAGE_NOTIFICATION_EMAIL_WARNING = "<img src=\"dtbundleresource://img/notification_email_warning.png\"/>"; //$NON-NLS-1$
	public final static String IMAGE_NOTIFICATION_EMAIL_PART1 = "<img src=\"cid:";
	public final static String IMAGE_NOTIFICATION_EMAIL_WARNING_PART2 = "\" />"; //$NON-NLS-1$
	public final static String IMAGE_DIVISION = "<div><img style=\"{0}\" src=\"{1}\"></div>"; //$NON-NLS-1$
	public final static String SEPERATOR_CLASS = "margin-top: 20px;"; //$NON-NLS-1$
	public final static String IMAGE_LOGO_PATH = "dtbundleresource://img/compuware_footer.png"; //$NON-NLS-1$
	public final static String IMAGE_SEPERATOR_PATH = "dtbundleresource://img/seperator.png"; //$NON-NLS-1$
	public final static String HTML_BEGIN_DETAILS_TABLE = "<table style=\"border: 0px; margin-top:5px;\" cellpadding=\"0\" cellspacing=\"0\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_VIOLATIONS_TABLE = "<table style=\"border: 0px; margin-top:5px;\" cellpadding=\"0\" cellspacing=\"0\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_VIOLATION_CONTENT_DIV = "<div style=\"font-size: 0.8em;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_VIOLATIONS_TABLE_LABEL = "<td style=\"vertical-align: top; width: 180px;\">"; //$NON-NLS-1$
	public final static String HTML_BEGIN_VIOLATIONS_LABEL_DIV = "<div style=\"font-size: 0.8em;\">"; //$NON-NLS-1$
	public final static String BEGIN_LINK = "<a style=\"a:link :text-decoration:underline; font-size: 0.8em; font-weight:bold; color:#505050; a:visited :text-decoration:underline; font-size: 0.8em; font-weight:bold; color:#505050;a:hover :text-decoration:underline; font-size: 0.8em; font-weight:bold; color:#808080;\" href=\""; //$NON-NLS-1$
	public final static String HTML_BRK= "<br>";
	public final static String REST_REPORT_URL = "/rest/management/reports/create/"; //$NON-NLS-1$
	public final static String REST_SOURCE_PARAM = "source"; //$NON-NLS-1$
	public final static String REST_LIVE_PARAM = "live"; //$NON-NLS-1$
	public final static String REST_FILTER_PARAM = "filter"; //$NON-NLS-1$
	public final static String REST_FILTER_TIMEFRAME_PARAM = "tf"; //$NON-NLS-1$
	public final static String REST_FILTER_TIMEFRAME_CUSTOM_TIMEFRAME_PARAM = "CustomTimeframe"; //$NON-NLS-1$
	public final static String REST_FILTER_INCIDENT_PARAM = "if"; //$NON-NLS-1$
	public final static String REST_FILTER_INCIDENT_RULE_PARAM = "rule"; //$NON-NLS-1$
	public final static String REST_REPORT_PROTOCOL = "http://";
	public final static String REST_REPORT_URL_SUFFIX = "?type=";
	public final static String TEMP_FILE_REPORT_PREFIX = "tempReport";
	public final static String REPORT_TYPE_PDF = ".pdf";
	public final static String REPORT_CONTENT_TYPE_PDF = "application/pdf";
	public final static String HTML_WBR = "<wbr>";
	public final static String AMPERSAND = "&amp;";
	public final static String QUERY_STRING_DASHBOARD_URI_1 = "?&amp;argument=-reuse&amp;argument=-incident&amp;argument=";
	public final static String QUERY_STRING_DASHBOARD_URI_2 = "&amp;argument=";
	public final static String URI_REST_DASHBOARDS = "/rest/html/management/dashboards";
	public final static long NO_TIME_SET = -1000; 
//	public final static String AGENT_NAME_HOST_FILTER = "&filter=ag:Agents?";
//	public final static String AGENT_GROUP_FILTER = "&filter=ag:AgentGroups?";
//	public final static String CUSTOM_TIMEFRAME_FILTER = "&filter=tf:CustomTimeframe?";
	public final static String REGEX_REMOVE_CHARS = "[\\(\\)\\{\\}\\\"]";
	public final static String REGEX_SPLIT_ON_WORDS = "[,\\(\\)\\{\\}\\'\\\"]";
	public final static String REGEX_SPLIT_AT_SIGN = "[@]";
//	public final static String ALL_AGENTS = "<all-agents>";
	public final static Map<String, String> CONTENT_TYPES_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {
		private static final long serialVersionUID = -4867569384183231001L;
	{
		put("HTML", "text/html");
		put("PDF", "application/pdf");
		put("XML", "application/xml");
		put("CSV", "text/csv");
		put("XSD", "text/plain");
		put("XLS", "application/vnd.ms-excel");
		}});
	public final static String HTML_CONTENT = "text/html";
	public final static String PLAIN_TEXT_CONTENT = "text/plain";
	public static final String ls = System.getProperty("line.separator");
	public static final String DEFAULT_ENCODING = System.getProperty("file.encoding");
	public static final String DATE_FORMAT_HH_MM = "HH:mm";
	public static final String REPORT_TYPE_XLS = "XLS";
	public static final String REPORT_TYPE_HTML = "HTML";
	public static final String HTML_FILE_UPDATED = "_updated";
	public static final String HTML_FILE_SUFFIX = ".html";
	public static final String REPORT_TYPE_URI_HTML = "?type=HTML";
	public static final String REPORT_TYPE_URI_XLS = "?type=XLS";
	public static final String PREPEND_EMBEDDED_IMAGE_SRC = "data:image/png;base64,";
	public static final String BaseIncidentFormatter_INCIDENT_APPLICATION_EMAIL_TEXT_ENTRY="Application";
	public static final String FILTERS_XSD_SCHEMA = "filters.xsd";
	public static final String THRESHOLDS_XSD_SCHEMA = "thresholds.xsd";
	public static final boolean VALIDATE_XML_WITH_SCHEMA = true;
	public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	public final static Map<String, XMLTags> XMLTags_MAP = Collections.unmodifiableMap(new HashMap<String, XMLTags>() {
		private static final long serialVersionUID = -4867569384183231001L;
	{
//		filters, filter, sources, emails, agent, agentGroup, monitor, collector, server, name, host, tos, cc, bcc; 
		put("filters", XMLTags.filters);
		put("filter", XMLTags.filter);
		put("sources", XMLTags.sources);
		put("emails", XMLTags.emails);
		put("agent", XMLTags.agent);
		put("agentGroup", XMLTags.agentGroup);
		put("monitor", XMLTags.monitor);
		put("collector", XMLTags.collector);
		put("server", XMLTags.server);
		put("name", XMLTags.name);
		put("host", XMLTags.host);
		put("tos", XMLTags.tos);
		put("cc", XMLTags.cc);
		put("bcc", XMLTags.bcc);
		}});
	public final static Map<String, XMLThresholdTags> OMNICARE_XMLThresholdTags_MAP = Collections.unmodifiableMap(new HashMap<String, XMLThresholdTags>() {
		private static final long serialVersionUID = 1250281977112360309L;
	{
		put(XMLThresholdTags.thresholds.name(), XMLThresholdTags.thresholds);
		put(XMLThresholdTags.threshold.name(), XMLThresholdTags.threshold);
		put(XMLThresholdTags.upper_severe.name(), XMLThresholdTags.upper_severe);
		put(XMLThresholdTags.upper_warning.name(), XMLThresholdTags.upper_warning);
		put(XMLThresholdTags.lower_warning.name(), XMLThresholdTags.lower_warning);
		put(XMLThresholdTags.lower_severe.name(), XMLThresholdTags.lower_severe);
		}});
	public static final String SPACE = " ";
	public static final String NULL = "null";
	public static final String PLUGIN_PROPERTIES = "res/extendedmailactionplugin.properties";
	public static final String EXPIRE_CACHE_INTERVAL = "expireCacheInterval";
	public static final String CLEANUP_INTERVAL = "cleanupInterval";
	public static final String DEFAULT_EXPIRE_CACHE_INTERVAL = "360"; // in minutes
	public static final String DEFAULT_CLEANUP_INTERVAL = "60";	  // in minutes
	public static final long MINUTES_TO_MILLIS = 60000;
	public static final String THRESHOLD_ATTRIBUTE_NAME = "name";
	public static final String THRESHOLD_ATTRIBUTE_METRIC_NAME = "metricname";
	public static final String DOUBLE_NaN = "NaN";
	public static final String MATCHES_ALL = ".*";
	
	// Error Messages
	public static final String PLUGIN_PROPERTIES_IS_NULL = "pluginProperties is null";
	public static final String DTUSER_IS_EMPTY = "The '" + CONFIG_DT_USER + "' parameter is null or empty";
	public static final String DTHOST_IS_EMPTY = "The '" + CONFIG_DT_HOST + "' parameter is null or empty";
	public static final String DTPASSWORD_IS_EMPTY = "The '" + CONFIG_DT_PASSWORD + "' parameter is null or empty";
	public static final String TO_CC_BCC_ARE_NOT_SET = "To:, CC:, and BCC: parameters are not set";
	public static final String IS_EMAIL_NEEDED_WARNING = "Incident(s) were raised but notification e-mail was not sent.";
	public static final String IS_EMAIL_NEEDED_ERROR = "In the isEmailNeeded method env and or record parameters are null";
	public static final String SOURCE_REFERENCE_OBJECT_IS_NULL = "SourceReference object must not be null";
	public static final String SOURCE_NAMES_IS_NULL = " SourceNames should not be null";
	public static final String ENV_IS_NULL = "ActionEnvironment object should not be null";
	public static final String INCIDENTS_SERVERS_MAP_IS_NULL = "IncidentsServers map should not be null";
	public static final String ACTION_RECORD_IS_NULL = "ActionRecord object should not be null";
	public static final String INCIDENT_IS_NULL = "Incident is null";
	public static final String ALL_HOSTS_IS_NULL = "allHosts List object must not be null";
	public static final String ALL_NAMES_IS_NULL = "allNames List object must not be null";
//	public static final String ALL_INCIDENTS_ARE_NULL = "The Collection of Incidents objects in the implementation of the ActionEnvironment interface does not contain non-null Incident";
	public static final String INCIDENTS_COLLECTION_IS_NULL_OR_EMPTY = "The Collection of Incidents objects in the implementation of the ActionEnvironment interface is null or empty";
	public static final String WRONG_FORMAT_OF_QUIET_FIELD = "Wrong format of the field '%s'. Expected 'HH:mm' format with 2 tokens separated by ':', received '%d' tokens";
	public static final String WRONG_NUMBER = "Wrong number of %s '%d' in the field '%s'";
	public static final String WRONG_FORMAT = "Wrong format of '%s' in the field '%s'";
	public static final String QUIET_TIME_FROM_TO_ERROR = "Quiet Time From and To parameters should be or both present or both not present";
	public static final String REMOVE_ACTION_ENTRY_ERROR = "removeActionEntry method returned 'false' what means that sharedPp object in the SHARED_PROPERTIES map is different from what needs to be removed. Internal error.";
}
