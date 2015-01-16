/**
 * 
 */
package com.dynatrace.diagnostics.plugins.extendedmailreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jxl.Image;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.AgentGroupSource;
import com.dynatrace.diagnostics.pdk.AgentSource;
import com.dynatrace.diagnostics.pdk.CollectorSource;
import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.IncidentRule;
import com.dynatrace.diagnostics.pdk.Measure;
import com.dynatrace.diagnostics.pdk.MonitorSource;
import com.dynatrace.diagnostics.pdk.Sensitivity.Type;
import com.dynatrace.diagnostics.pdk.ServerSource;
import com.dynatrace.diagnostics.pdk.Source;
import com.dynatrace.diagnostics.pdk.SourceType;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;
import com.dynatrace.diagnostics.pdk.Violation;
import com.dynatrace.diagnostics.pdk.Violation.TriggerValue;
import com.dynatrace.diagnostics.plugin.actionhelper.ActionData;
import com.dynatrace.diagnostics.plugin.actionhelper.ActionHelper;
import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugin.actionhelper.SourceReferences;
import com.dynatrace.diagnostics.plugin.actionhelper.SubstituterFields;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.ActionRecord;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.Filter;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.MetricIndicators;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.PluginParameters;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.SendOnlyVariableNames;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.MailPluginProperties;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.SharedProperties;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.Threshold;
import com.dynatrace.diagnostics.plugins.extendedmailreport.exception.ReportCreationException;
import com.dynatrace.diagnostics.plugins.extendedmailreport.exception.SimpleErrorHandler;
import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.EmailConfigAction;
import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.IncidentUtils;
import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.MailPluginConstants;
import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.MessagesConstants;
import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.SAXHandler;
import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.SAXHandlerThresholds;
import com.dynatrace.diagnostics.sdk.resources.BaseConstants;
import com.dynatrace.diagnostics.sdk.ui.utils.TextUtils;
import com.dynatrace.diagnostics.sdk.ui.utils.format.PredefinedDateFormat;
//import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.smtp.SMTPTransport;

import sun.net.www.protocol.http.AuthCacheValue;
import sun.net.www.protocol.http.AuthCacheImpl;

/**
 * @author eugene.turetsky
 * 
 */
public class MailExecutor implements MailPluginConstants, MessagesConstants {
	
	protected static ConcurrentHashMap<String, SharedProperties> SHARED_PROPERTIES = new ConcurrentHashMap <String, SharedProperties>();
	
	String pluginId;
	SharedProperties sharedPp;
	
	private boolean isSetup;
	private boolean isExecute;
	Properties p;
	
	CleanupCache cc;
	
	private static final Logger log = Logger.getLogger(MailExecutor.class.getName());

	protected Status setup(ActionEnvironment env) throws Exception {
		Status status;
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setup method");
		}
		
		isSetup = true;
		isExecute = false;
		
		if (env == null) {
			log.severe(ENV_IS_NULL);
			return new Status(StatusCode.ErrorInternalException, ENV_IS_NULL, ENV_IS_NULL);
		}
		
		String pluginId = getPluginId(env);
		if (log.isLoggable(Level.FINER)) {
			log.finer("setup method: pluginId is '" + pluginId + "'");
		}
		
		try {
			cc = CleanupCache.getInstance();
			if (log.isLoggable(Level.FINER)) {
				log.finer("setup method: CleanupCache state I: alive indicator is '" + cc.isAlive() 
						+ "', error indicator is '" + cc.isError() 
						+ "', shutdown indicator is '" + cc.isShutdown() + "'");
			}
			
			// set expire cache interval and cleanup interval temporarily here until work with Lab
			p = getProperties();
			cc.setExpireCacheInterval(Long.parseLong(p.getProperty(EXPIRE_CACHE_INTERVAL, DEFAULT_EXPIRE_CACHE_INTERVAL)) * 60 * 1000); // convert from minutes to ms
			cc.setCleanupInterval(Long.parseLong(p.getProperty(CLEANUP_INTERVAL, DEFAULT_CLEANUP_INTERVAL)) * 60 * 1000 ); // convert from minutes to ms
			
			if (!cc.isAlive()) {
				// start cleanup process
				if (log.isLoggable(Level.FINER)) {
					String msg = "setup method: expireCacheInterval is " + cc.getExpireCacheInterval() + " ms, cleanupInterval is " + cc.getCleanupInterval() + " ms";
					log.finer(msg);
				}
				cc.startCleanpCache();
			} else if (cc.isError()){
				// cleanup a whole map and reset error indicator
				cc.cleanupMap();
			}
			
			if (log.isLoggable(Level.FINER)) {
				log.finer("setup method: CleanupCache state II: alive indicator is '" + cc.isAlive() 
						+ "', error indicator is '" + cc.isError() 
						+ "', shutdown indicator is '" + cc.isShutdown() + "'");
			}
			
			synchronized(sharedPp = getSharedProperties(pluginId)) {
				if (!SHARED_PROPERTIES.containsKey(pluginId)) {
					log.finer("setup method: pluginId is not found in the SHARED_PROPERTIES hashmap. Hashcode is " + SHARED_PROPERTIES.hashCode() + ", identity hash code is " + System.identityHashCode(SHARED_PROPERTIES));
					sharedPp.setId(pluginId);
					if ((status = setSharedProperties(env, sharedPp)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
						log.severe("setup method: setConfiguration method returned message '" + status.getMessage() + "'");
						return status;
					}
					SHARED_PROPERTIES.put(pluginId, sharedPp);
				} else {
					log.finer("setup method: pluginId is found in the SHARED_PROPERTIES hashmap. Hashcode is " + SHARED_PROPERTIES.hashCode() + ", identity hash code is " + System.identityHashCode(SHARED_PROPERTIES));
					if ((sharedPp = SHARED_PROPERTIES.get(pluginId)) == null || sharedPp.getMailPluginProperties() == null) {
						String msg = "setup method: retrieved shared properties object is null; ";
						if (!removeActionEntry(pluginId, sharedPp)) {
							msg += REMOVE_ACTION_ENTRY_ERROR;
						}
						log.severe(msg);
						return new Status(StatusCode.ErrorInternal, msg, msg);
					}
					// check if the EmailsFilters dependency file is used
					if (sharedPp.getMailPluginProperties().isEmailsFiltersCoupled()) {
						// check if the last modified date of the EmailsFilters dependency file hasn't been changed
						long l = new File(sharedPp.getMailPluginProperties().getEmailsFiltersDependencyFile()).lastModified();
						
						if (log.isLoggable(Level.FINER)) {
							log.finer("setup method: last modified time is " + l + " ms, date is " + new Date(l) 
								+ "; updateDateEmailsFiltersFile is " + sharedPp.getUpdateDateEmailsFiltersFile() + " ms, date is " + new Date(sharedPp.getUpdateDateEmailsFiltersFile()));
						}
	
						if (l != sharedPp.getUpdateDateEmailsFiltersFile()) {
							// will re-fresh plugin properties
							sharedPp.clean();
							if ((status = setSharedProperties(env, sharedPp)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
								String msg = "setup method: setConfiguration method returned message '" + status.getMessage() + "'";
								if (!removeActionEntry(pluginId, sharedPp)) {
									msg += REMOVE_ACTION_ENTRY_ERROR;
								} 
								log.severe(msg);
								return status;
							}
							SHARED_PROPERTIES.put(pluginId, sharedPp);
						}
					}
					
					// check if thresholds file is used
					String s;
					if ((s = sharedPp.getMailPluginProperties().getThresholdsFile()) != null && !s.trim().isEmpty()) {
						long l = new File(sharedPp.getMailPluginProperties().getThresholdsFile()).lastModified();
						
						if (log.isLoggable(Level.FINER)) {
							log.finer("setup method: last modified time of the thresholds file is " + l + " ms, date is " + new Date(l) 
								+ "; updateDateEmailsFiltersFile is " + sharedPp.getUpdateDateThresholdsFile() + " ms, date is " + new Date(sharedPp.getUpdateDateThresholdsFile()));
						}
	
						if (l != sharedPp.getUpdateDateThresholdsFile()) {
							// will re-fresh plugin properties
							sharedPp.clean();
							if ((status = setSharedProperties(env, sharedPp)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
								String msg = "setup method: setConfiguration method returned message '" + status.getMessage() + "'";
								if (!removeActionEntry(pluginId, sharedPp)) {
									msg += REMOVE_ACTION_ENTRY_ERROR;
								} 
								log.severe(msg);
								return status;
							}
							SHARED_PROPERTIES.put(pluginId, sharedPp);
						}
					}
					
					// set lastRunTime
					sharedPp.setLastRunTime(System.currentTimeMillis());
				}
			}
		} catch (Exception e) {
			String msg = HelperUtils.getExceptionAsString(e);
			if (!removeActionEntry(pluginId, sharedPp)) {
				msg += REMOVE_ACTION_ENTRY_ERROR;
			}
			log.severe("setup method: " + msg);
			return new Status(StatusCode.ErrorInternalException, e.getMessage(), msg, e);
		}

		return STATUS_SUCCESS;
	}
	
	protected static synchronized SharedProperties getSharedProperties(String id) {
		SharedProperties sharedPp;
		return (sharedPp = SHARED_PROPERTIES.get(id)) == null ? new SharedProperties() : sharedPp;
	}
	
	private Properties getProperties() {
		URL url = this.getClass().getClassLoader().getResource(PLUGIN_PROPERTIES);
		Properties p = new Properties();
		try {
			p.load(url.openStream());
		} catch (Exception e) {
			String msg = "setSharedProperties method: Properties load method threw exception '" + HelperUtils.getExceptionAsString(e) + "'";
			log.severe(msg);
			throw new RuntimeException(msg);
		}
		
		return p;
	}
	
	private boolean removeActionEntry(String key, SharedProperties sharedPp) {
		boolean rc = SHARED_PROPERTIES.remove(key, sharedPp);
		if (log.isLoggable(Level.FINER)) {
			log.finer("removeActionEntry method: removeActionEntry method returned '" + rc + "'");
		}
		
		return rc;
	}
	
	private Status setSharedProperties(ActionEnvironment env, SharedProperties sharedPp) {
		Status status;
		MailPluginProperties pp = new MailPluginProperties();
		// set plugin's configuration parameters
		if ((status = setConfiguration(env, pp)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
			log.severe("setup method: setConfiguration method returned message '" + status.getMessage() + "'");
			return status;
		}
		// set URL prefix and suffix for the dashboards
		setDashboardUrl(pp);
		// set footer.png URL
		setFooterUrl(pp);
		
		// setup SharedProperties object
		sharedPp.setMailPluginProperties(pp);
		long l = System.currentTimeMillis();
		sharedPp.setCreateTime(l);
		sharedPp.setLastRunTime(l);
		sharedPp.setDtServer(env.getIncidents().iterator().next().getServerName());
		sharedPp.setSystemProfile(env.getSystemProfileName());
		if (pp.isEmailsFiltersCoupled()) {
			sharedPp.setUpdateDateEmailsFiltersFile(new File(sharedPp.getMailPluginProperties().getEmailsFiltersDependencyFile()).lastModified());
		} else {
			sharedPp.setUpdateDateEmailsFiltersFile(-1);
		}
		if (pp.getThresholdsFile() != null && !pp.getThresholdsFile().isEmpty()) {
			sharedPp.setUpdateDateThresholdsFile(new File(sharedPp.getMailPluginProperties().getThresholdsFile()).lastModified());
		} else {
			sharedPp.setUpdateDateThresholdsFile(-1);
		}
		
		return STATUS_SUCCESS;
	}
	
	private String getPluginId(ActionEnvironment env) {
		PluginParameters params = new PluginParameters();
		
		// set parameters
		params.setDtHost(env.getConfigString(CONFIG_DT_HOST));
		params.setDtPort(env.getConfigLong(CONFIG_DT_PORT));
		params.setDtUser(env.getConfigString(CONFIG_DT_USER));
		params.setDtPassword(env.getConfigPassword(CONFIG_DT_PASSWORD));
		params.setSmtpHost(env.getConfigString(CONFIG_SMTP_HOST));
		params.setSmtpPort(env.getConfigLong(CONFIG_SMTP_PORT));
		params.setSmtpSsl(env.getConfigBoolean(CONFIG_SMTP_SSL));
		params.setSmtpUserPassword(env.getConfigBoolean(CONFIG_SMTP_USER_PASSWORD));
		params.setSmtpUser(env.getConfigString(CONFIG_SMTP_USER));
		params.setSmtpPassword(env.getConfigPassword(CONFIG_SMTP_PASSWORD));
		params.setQuietTimeFrom(env.getConfigString(CONFIG_QUIET_TIME_FROM));
		params.setQuietTimeTo(env.getConfigString(CONFIG_QUIET_TIME_TO));
		params.setHostNameToIpAddress(env.getConfigString(CONFIG_HOST_NAME_TO_IP_ADDRESS));
		params.setFrom(env.getConfigString(CONFIG_FROM));
		params.setEmailsFiltersCoupled(env.getConfigBoolean(CONFIG_ARE_EMAILS_FILTERS_COUPLED));
		params.setEmailsFiltersDependencyFile(env.getConfigString(CONFIG_EMAILS_FILTERS_DEPENDENCY_FILE));
		params.setTo(env.getConfigString(CONFIG_TO));
		params.setCc(env.getConfigString(CONFIG_CC));
		params.setBcc(env.getConfigString(CONFIG_BCC));
		params.setAgents(env.getConfigString(CONFIG_AGENTS));
		params.setAgentServers(env.getConfigString(CONFIG_AGENT_SERVERS));
		params.setAgentGroups(env.getConfigString(CONFIG_AGENT_GROUPS));
		params.setMonitors(env.getConfigString(CONFIG_MONITORS));
		params.setMonitorServers(env.getConfigString(CONFIG_MONITOR_SERVERS));
		params.setCollectors(env.getConfigString(CONFIG_COLLECTORS));
		params.setCollectorServers(env.getConfigString(CONFIG_COLLECTOR_SERVERS));
		params.setServers(env.getConfigString(CONFIG_SERVERS));
		params.setSendOnlyPattern(env.getConfigString(CONFIG_SEND_ONLY_PATTERNS));
		params.setMeasureNamePattern(env.getConfigString(CONFIG_MEASURE_NAME_PATTERNS));
		params.setHtmlFormat(env.getConfigBoolean(CONFIG_HTML_FORMAT));
		params.setSubjectPrefix(env.getConfigString(CONFIG_SUBJECT_PREFIX));
		params.setSubject(env.getConfigString(CONFIG_SUBJECT));
		params.setSubjectSuffix(env.getConfigString(CONFIG_SUBJECT_SUFFIX));
		params.setBodyHeader(env.getConfigString(CONFIG_BODY_HEADER));
		params.setBody(env.getConfigString(CONFIG_BODY));
		params.setBodyFooter(env.getConfigString(CONFIG_BODY_FOOTER));
		params.setDashboardNames(env.getConfigString(CONFIG_DASHBOARD));
		params.setDashboardsType(env.getConfigString(CONFIG_DASHBOARDS_TYPE));
		params.setRestFilteringAgentNameHost(env.getConfigBoolean(CONFIG_REST_FILTERING_AGENT_NAME_HOST));
		params.setRestFilteringAgentGroup(env.getConfigBoolean(CONFIG_REST_FILTERING_AGENT_GROUP));
		params.setRestFilteringCustomTimeFrame(env.getConfigBoolean(CONFIG_REST_FILTERING_CUSTOM_TIMEFRAME));
		params.setDtServerName(env.getIncidents().iterator().next().getServerName());
		params.setSystemProfile(env.getSystemProfileName());
		params.setIncidentRuleName(env.getConfigString(CONFIG_INCIDENT_RULE_NAME));
		params.setIdentityString(env.getConfigString(CONFIG_IDENTIFYING_STRING));
		params.setThresholdsFile(env.getConfigString(CONFIG_THRESHOLDS_FILE));

		return params.toString();
//		return new StringBuilder(params.getIncidentRuleName()).append(params.getIdentityString()).toString();
 	}

	protected Status execute(ActionEnvironment env) throws Exception {
		Status status;
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering execute method");
		}
		
		isExecute = true;
		if (env == null) {
			String msg = "execute method: " + ENV_IS_NULL;
			if (!removeActionEntry(sharedPp.getId(), sharedPp)) {
				msg += REMOVE_ACTION_ENTRY_ERROR;
			} 
			log.severe(msg);
			return new Status(StatusCode.ErrorInternalException, msg, msg);
		}
		
		MailPluginProperties pp = sharedPp.getMailPluginProperties();
		// setup data from the ActionEnvironment
		ActionRecord record = new ActionRecord();
		try {
			record.setSources(ActionHelper.populateSourceOfIncidents(env).getSources());
			// get substituter map
			ActionData ad;
			Map<String, String> substituterMap = ActionHelper.populateSubstituterMap(env, (ad = getActionData(pp, record.getSources())));	
			
			// get quietTime
			boolean quietTimeIndicator = isQuietTime(pp);
			
			// send only patterns
			final List<String> sendOnlyPatterns = new ArrayList<String>(pp.getSendOnlyPatterns().keySet());
			boolean usedSendOnlyPatterns = sendOnlyPatterns != null && !sendOnlyPatterns.isEmpty() ? true : false;
			boolean sendOnlyPatternsIndicator = isSendOnlyPatterns(substituterMap, sendOnlyPatterns, pp);
			// measure name pattern
			final List<String> measureNamePatterns = new ArrayList<String>(pp.getMeasureNamePatterns().keySet());
			boolean usedMeasureNamePatterns = measureNamePatterns != null && !measureNamePatterns.isEmpty() ? true : false;
			boolean measureNamePatternsIndicator = isMatchFound(substituterMap, measureNamePatterns, pp);
			// check dynamic measures
			MetricIndicators mIndicators = getMetricIndicators(substituterMap, pp);
			
			Map<String, Pattern> m;
			if (!pp.isEmailsFiltersCoupled()) {
				Filter filter = new Filter();
				filter.setAgents((m = pp.getAgents()) == null ? new HashMap<String, Pattern>() : m);
				filter.setAgentServers((m = pp.getAgentServers()) == null ? new HashMap<String, Pattern>() : m);
				filter.setAgentGroups((m = pp.getAgentGroups()) == null ? new HashMap<String, Pattern>() : m);
				filter.setMonitors((m = pp.getMonitors()) == null ? new HashMap<String, Pattern>() : m);
				filter.setMonitorServers((m = pp.getMonitorServers()) == null ? new HashMap<String, Pattern>() : m);
				filter.setCollectors((m = pp.getCollectors()) == null ? new HashMap<String, Pattern>() : m);
				filter.setCollectorServers((m = pp.getCollectorServers()) == null ? new HashMap<String, Pattern>() : m);
				filter.setServers((m = pp.getServers()) == null ? new HashMap<String, Pattern>() : m);
				filter.setTos(pp.getTos());
				filter.setCc(pp.getCc());
				filter.setBcc(pp.getBcc());
				record.setFilter(filter);
				if ((status = processEmails(env, pp, record, substituterMap, ad, quietTimeIndicator, usedSendOnlyPatterns, sendOnlyPatternsIndicator, usedMeasureNamePatterns, measureNamePatternsIndicator, mIndicators)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
					String msg = "execute method: processEmails method returned message '" + status.getMessage() + "'";
					if (!removeActionEntry(sharedPp.getId(), sharedPp)) {
						msg += REMOVE_ACTION_ENTRY_ERROR;
					} 
					log.severe(msg);
					return status;
				}
			} else {
				status = STATUS_SUCCESS;
				for (Filter filter : pp.getFilters()) {
					record.setFilter(filter);
					if ((status = processEmails(env, pp, record, substituterMap, ad, quietTimeIndicator, usedSendOnlyPatterns, sendOnlyPatternsIndicator, usedMeasureNamePatterns, measureNamePatternsIndicator, mIndicators)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
						String msg = "execute method: processEmails method returned message '" + status.getMessage() + "'";
						if (!removeActionEntry(sharedPp.getId(), sharedPp)) {
							msg += REMOVE_ACTION_ENTRY_ERROR;
						} 
						log.severe(msg);
						return status;
					}
				}
			}
		} catch (Exception e) {
			String msg = "execute method: " + (msg = HelperUtils.getExceptionAsString(e));
			if (!removeActionEntry(sharedPp.getId(), sharedPp)) {
				msg += REMOVE_ACTION_ENTRY_ERROR;
			} 
			log.severe(msg);
			return new Status(StatusCode.ErrorInternalException, msg, msg, e);
		}
		
		// return on success
		return status;		
	}
	
	public static MetricIndicators getMetricIndicators(Map<String, String> substituterMap, MailPluginProperties props) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getMetricIndicators method");
		}
		// check dynamic measures
		String value;
		String metricName = (value = substituterMap.get(SubstituterFields.VIOLATED_MEASURE_METRIC_NAME.name())) == null ? null : value.trim();
		if (log.isLoggable(Level.FINE)) {
			log.fine("getMetricIndicators method: metricName is '" + metricName + "'");
		}
		boolean metricNameInFilter;
		boolean dynamicMeasureReturn = false;
		if (log.isLoggable(Level.FINE)) {
			log.fine("getMetricIndicators method: props.getThresholdsFile() is '" + props.getThresholdsFile() + "'");
		}
		if (!props.getThresholdsFile().isEmpty() && metricName != null && !metricName.isEmpty() && !metricName.equals(BaseConstants.DASH)) {
			metricNameInFilter = props.getMetricNames().contains(metricName);
			if (log.isLoggable(Level.FINE)) {
				log.fine("getMetricIndicators method: metricNameInFilter is '" + metricNameInFilter + "', props.getMetricNames() is " + Arrays.toString(props.getMetricNames().toArray()));
			}
			if (metricNameInFilter && props.getThresholds() != null) {
				String splittings = substituterMap.get(SubstituterFields.VIOLATED_MEASURE_SPLITTINGS_ALL.name());
				String measures = substituterMap.get(SubstituterFields.VIOLATED_MEASURE_VALUE_ALL.name());
				if (log.isLoggable(Level.FINE)) {
					log.fine("getMetricIndicators method: splittings is '" + splittings + "', measures is " + measures + "'");
				}
				if (splittings != null && !splittings.trim().isEmpty() && !splittings.equals(BaseConstants.DASH) 
						&& measures != null && !measures.trim().isEmpty()) {
					if (isDynamicMeasureExceeded(splittings, measures, props.getThresholds())) {
						dynamicMeasureReturn = true;
					}
				}
			}
		} else {
			metricNameInFilter = false;
		}
		
		if (log.isLoggable(Level.FINE)) {
			log.fine("getMetricIndicators method: metricNameInFilter is '" + metricNameInFilter + "', dynamicMeasureReturn is " + dynamicMeasureReturn + "'");
		}
		
		return new MetricIndicators(metricNameInFilter, dynamicMeasureReturn);
	}
	
	private Status processEmails(ActionEnvironment env, MailPluginProperties props, ActionRecord record, Map<String, String> substituterMap, ActionData ad, boolean quietTime, 
			boolean usedSendOnlyPatterns, boolean sendOnlyPatternsIndicator, boolean usedMeasureNamePatterns, boolean measureNamePatternsIndicator, MetricIndicators mIndicators) {
		Status status;
		
		// check if we need to send e-mail on incidents, e.g. check if incidents are coming from the customer's selected agents/hosts
		if (!isEmailNeeded(env, substituterMap, props, record, quietTime, usedSendOnlyPatterns, sendOnlyPatternsIndicator, usedMeasureNamePatterns, measureNamePatternsIndicator, mIndicators)) {
			log.fine("execute method: " + IS_EMAIL_NEEDED_WARNING);
			return new Status(StatusCode.PartialSuccess, IS_EMAIL_NEEDED_WARNING, IS_EMAIL_NEEDED_WARNING);
		}
				
		//escape characters for HTML format
		if (props.isHtmlFormat()) {
			HelperUtils.escapeHtml4(substituterMap);
		}
		// set incidents, start/end time, etc. of the first incident in the ActionRecord
		setActionData(record, ad, substituterMap);

		// Apply substituterMap map to the plugin parameters
		if ((status = updatePluginParams(substituterMap, props, record)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
			log.severe("execute method: " + status.getMessage());
			return status;
		}
					
		if ((status = sendMail(env, props, record)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode() && log.isLoggable(Level.SEVERE)) {
			log.severe("execute method: " + status.getMessage());
			return status;
		}
		
		return STATUS_SUCCESS;
	}

	protected void teardown(ActionEnvironment env) throws Exception {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering teardown method");
		}

		if (cc != null && !isSetup && !isExecute) {
			log.finer("teardown method: calling shutdownService");
			cc.setShutdown(true);
			cc.shutdownService();
			log.finer("teardown method: the shutdownService call is executed");
		}
		
		isSetup = false;
		isExecute = false;
	}
	
	protected Status sendMail(ActionEnvironment env, MailPluginProperties props, ActionRecord record) {
		Status status;
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering sendMail method");
		}

		EmailConfigAction emailConfigAction = new EmailConfigAction();
		if ((status = processIncident(env, emailConfigAction, props, record)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
			log.severe("sendMail method: " + status.getMessage());
			return status;
		}
		
		return STATUS_SUCCESS;
	}
	
	public static boolean isEmailNeeded(ActionEnvironment env, Map<String, String> substituterMap, MailPluginProperties props, ActionRecord record, boolean quietTime, 
			boolean usedSendOnlyPatterns, boolean sendOnlyPatternsIndicator, boolean usedMeasureNamePatterns, boolean measureNamePatternsIndicator, MetricIndicators mIndicators) {
		Map<String, Pattern> m;
		Set<String> s;
		// set filters
		final List<String> agents = new ArrayList<String>((m = record.getFilter().getAgents()) != null ? m.keySet() : (s = Collections.emptySet()));
		final List<String> agentServers = new ArrayList<String>((m = record.getFilter().getAgentServers()) != null ? m.keySet() : (s = Collections.emptySet()));
		final List<String> agentGroups = new ArrayList<String>((m = record.getFilter().getAgentGroups()) != null ? m.keySet() : (s = Collections.emptySet()));
		final List<String> monitors = new ArrayList<String>((m = record.getFilter().getMonitors()) != null ? m.keySet() : (s = Collections.emptySet()));
		final List<String> monitorServers = new ArrayList<String>((m = record.getFilter().getMonitorServers()) != null ? m.keySet() : (s = Collections.emptySet()));
		final List<String> collectors = new ArrayList<String>((m = record.getFilter().getCollectors()) != null ? m.keySet() : (s = Collections.emptySet()));
		final List<String> collectorServers = new ArrayList<String>((m = record.getFilter().getCollectorServers()) != null ? m.keySet() : (s = Collections.emptySet()));
		final List<String> servers = new ArrayList<String>((m = record.getFilter().getServers()) != null ? m.keySet() : (s = Collections.emptySet()));
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering isEmailNeeded method");
		}
		
		// check dynamic measures
		boolean metricNameInFilter = mIndicators.isMetricNameInFilter();
		boolean dynamicMeasureReturn = mIndicators.isDynamicMeasureReturn();
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("isEmailNeeded method: metricNameInFilter is '" + metricNameInFilter + ", dynamicMeasureReturn is '" + dynamicMeasureReturn + "'");
		}
		
		if (agents.isEmpty() && agentGroups.isEmpty() && monitors.isEmpty() && collectors.isEmpty() && servers.isEmpty() 
				&& !usedSendOnlyPatterns && !usedMeasureNamePatterns && !metricNameInFilter) { 
			return !quietTime; // was return true;
		}
		if (log.isLoggable(Level.FINER)) {
			log.finer("isEmailNeeded method: quiet time indicator is '" + quietTime + "'");
		}
		
		// add sendOnlyPatterns and measureNamePatterns
		if (log.isLoggable(Level.FINER)) {
			log.finer("isEmailNeeded method: usedSendOnlyPatterns is " + usedSendOnlyPatterns 
					+ ", sendOnlyPatternsIndicator is " + sendOnlyPatternsIndicator
					+ ", usedMeasureNamePatterns is " + usedMeasureNamePatterns
					+ ", measureNamePatternsIndicator is " + measureNamePatternsIndicator);
		}
		if (usedSendOnlyPatterns) {
			if (!sendOnlyPatternsIndicator) {
				// will not send notification e-mail if match for sendOnlyPatterns wasn't found
				return false; 
			} else if (agents.isEmpty() && agentGroups.isEmpty() && monitors.isEmpty() && collectors.isEmpty() 
					&& servers.isEmpty() && !usedMeasureNamePatterns && !metricNameInFilter) {
				return !quietTime; // was return true;
			}	
		}
		
		if (usedMeasureNamePatterns) {
			if (!measureNamePatternsIndicator) {
				// will not send notification e-mail if match for measureNamePatterns wasn't found
				return false;
			} else if (agents.isEmpty() && agentGroups.isEmpty() && monitors.isEmpty() && collectors.isEmpty() 
					&& servers.isEmpty() && !metricNameInFilter) { 
				return !quietTime; // was return true;
			}
		}
		
		if (metricNameInFilter) {
			if (dynamicMeasureReturn) {
				if (agents.isEmpty() && agentGroups.isEmpty() && monitors.isEmpty() && collectors.isEmpty() 
						&& servers.isEmpty()) {
					return !quietTime; // was return true;
				} else {
						return isSourceFiltersMatches(record, agents, agentServers, agentGroups,monitors, monitorServers, collectors, collectorServers, servers, quietTime);			
				}
			}
		} else {			
			return isSourceFiltersMatches(record, agents, agentServers, agentGroups,monitors, monitorServers, collectors, collectorServers, servers, quietTime);
		}
		
		return false;

	}
	
	public static boolean isSourceFiltersMatches(ActionRecord record, 
			List<String> agents, List<String> agentServers, 
			List<String> agentGroups, 
			List<String> monitors, List<String> monitorServers, 
			List<String> collectors, List<String> collectorServers, 
			List<String> servers, 
			boolean quietTime) {
		SourceReferences sr;
		List<String> names;
		Map<SourceType, SourceReferences> map = record.getSources();
		// check if we have a match on any of the sources
		// agent names and agent servers
		if (log.isLoggable(Level.FINER)) {
			logSourcesServers(map, SourceType.Agent, agents, agentServers);
		}
		if (!agents.isEmpty() && (sr = map.get(SourceType.Agent)) != null && (names = sr.getSourceNames()) != null && !names.isEmpty()) {
			if (isSourceNamesAndHostsMatches(record, SourceType.Agent, sr, agents, agentServers, quietTime)) {
				return !quietTime;
			}
		}
		
		// agentGroup names
		if (log.isLoggable(Level.FINER)) {
			logSources(map, SourceType.AgentGroup, agentGroups);
		}
		if (agentGroups != null && !agentGroups.isEmpty() && (sr = map.get(SourceType.AgentGroup)) != null && (names = sr.getSourceNames()) != null && !names.isEmpty()) {
			if (isSourceNamesMatches(record, SourceType.AgentGroup, sr, agentGroups, quietTime)) {
				return !quietTime;
			}
		}
		
		// monitor names
		if (log.isLoggable(Level.FINER)) {
			logSourcesServers(map, SourceType.Monitor, monitors, monitorServers);
		}
		if (monitors != null && !monitors.isEmpty() && (sr = map.get(SourceType.Monitor)) != null && (names = sr.getSourceNames()) != null && !names.isEmpty()) {
			if (isSourceNamesAndHostsMatches(record, SourceType.Monitor, sr, monitors, monitorServers, quietTime)) {
				return !quietTime;
			}
		}
		
		// collector names
		if (log.isLoggable(Level.FINER)) {
			logSourcesServers(map, SourceType.Collector, collectors, collectorServers);
		}
		if (collectors != null && !collectors.isEmpty() && (sr = map.get(SourceType.Collector)) != null && (names = sr.getSourceNames()) != null && !names.isEmpty()) {
			if (isSourceNamesAndHostsMatches(record, SourceType.Collector, sr, collectors, collectorServers, quietTime)) {
				return !quietTime;
			}
		}
		
		// server names
		if (log.isLoggable(Level.FINER)) {
			logSources(map, SourceType.Server, servers);
		}
		if (servers != null && !servers.isEmpty() && (sr = map.get(SourceType.Server)) != null && (names = sr.getSourceHosts()) != null && !names.isEmpty()) {
			if (isSourceNamesMatches(record, SourceType.Server, sr, servers, quietTime)) {
				return !quietTime;
			}
		}
		
		return false;
	}
	
	public static boolean isDynamicMeasureExceeded(String splittings, String measures, Map<String, Threshold> thresholds) {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Entering isDynamicMeasuresExceeded method: spllittings is '" + splittings 
					+ "', measures is '" + measures 
					+ "', thresholds is '" + Arrays.toString(thresholds.entrySet().toArray()) + "'");
		}
		
		String[] mNames = splittings.split(BaseConstants.SCOLON);
		if (log.isLoggable(Level.FINE)) {
			log.fine("isDynamicMeasuresExceeded method: mNames is " + Arrays.toString(mNames));
		}
		String[] mValues = measures.split(BaseConstants.SCOLON);
		if (log.isLoggable(Level.FINE)) {
			log.fine("isDynamicMeasuresExceeded method: mValues is " + Arrays.toString(mValues));
		}
		int i = 0;
		for (String mName : mNames) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("isDynamicMeasuresExceeded method: mName is '" + mName + "', i = " + i + ", length of mValues is "
						+ mValues.length);
			}
			if (mName.trim().isEmpty()) {
				continue;
			}
			if (!thresholds.containsKey(mName) || i >= mValues.length || mValues[i].trim().isEmpty() 
					|| mValues[i].trim().equals(BaseConstants.DASH) || mValues[i].trim().equals(DOUBLE_NaN)) {
				continue;
			}
			
			double d;
			try {
				d = Double.valueOf(mValues[i]);
			} catch (NumberFormatException e) {
				String msg = "isDynamicMeasuresExceeded method: mValues[ " + i + "] of the violated measure value array is '" + mValues[i] + "' and cannot be converted to double";
				log.severe(msg);
				continue;
			}
				
			Threshold t = thresholds.get(mName);
			if (d > t.getUpperSevere() || d > t.getUpperWarning() || d < t.getLowerSevere() || d < t.getLowerWarning()) {
				return true;
			}
			
			i++;
		}
		
		return false;		
	}
	
	/**
	 * 
	 * @param record 		- ActionRecord object
	 * @param type			- SourceType, i.e. Agent, AgentGroup, Monitor, Collector, or Server
	 * @param sr			- SourceReference object
	 * @param filterNames	- List of strings which contains filters for one of the SourceTypes, i.e. filters for Agent, AgentGroup, Monitor, Collector, or Server
	 * @param filterServers	- List of strings which contains filters for servers of either Agents, or Monitors, or Collectors.
	 * @param quietTime		- quietTime indicator
	 * @return
	 */
	public static boolean isSourceNamesAndHostsMatches(ActionRecord record, SourceType type, SourceReferences sr, List<String> filterNames, List<String> filterServers, boolean quietTime) {
		Pattern pattern;
		List<String> hosts;
		for (String sourceName : sr.getSourceNames()) {				
			if (sourceName != null && !sourceName.isEmpty()) {
				for (String filterName : filterNames) {
					// check if filterName from the filterNames filter list matches the name
					if (filterName == null || filterName.isEmpty()) {
						continue;
					}
					pattern = getPattern(record, type, filterName, true); 
					if (pattern != null) {
						if (pattern.matcher(sourceName).matches()) {
							if (log.isLoggable(Level.FINER)) {
								log.finer("isEmailNeeded method: pattern for type '" + type.name() + "' and filter '"+ filterName + "' matches source name '" + sourceName + "'");
							}
							
							if (type == SourceType.Agent || type == SourceType.Monitor || type == SourceType.Collector) {
								if ((hosts = sr.getSourceHosts()) != null && !hosts.isEmpty() && filterServers != null && !filterServers.isEmpty()) {
									return isHostsMatches(record, type, hosts, filterServers, quietTime);
								} else {
									return !quietTime; // was return true;
								}
							}
						} else {
							if (log.isLoggable(Level.FINER)) {
								log.finer("isEmailNeeded method: pattern for type '" + type.name() + "' and filter '" + filterName + "' does not match name '" + sourceName + "'");
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public static Pattern getPattern(ActionRecord record, SourceType type, String filterName, boolean isSourceName) {
		switch (type) {
		case Agent:
			if (isSourceName) {
				return record.getFilter().getAgents().get(filterName);
			} else {
				return record.getFilter().getAgentServers().get(filterName);
			}
		case AgentGroup:
			return record.getFilter().getAgentGroups().get(filterName);
		case Monitor:
			if (isSourceName) {
				return record.getFilter().getMonitors().get(filterName);
			} else {
				return record.getFilter().getMonitorServers().get(filterName);
			}
		case Collector:
			if (isSourceName) {
				return record.getFilter().getCollectors().get(filterName);
			} else {
				return record.getFilter().getCollectorServers().get(filterName);
			}
		case Server:
			return record.getFilter().getServers().get(filterName);
		default:
			throw new RuntimeException("isSourceNamesAndHostsMatches method: incorrect SourceType '" + type.name() + "' was passed to the method.");
		}
	}
	
	public static void logSourcesServers(Map<SourceType, SourceReferences> map, SourceType type, List<String> filterNames, List<String> filterNameServers) {
		log.finer("logSourcesFilters method: " + type.name() + " names: '" + Arrays.toString(filterNames.toArray()) + "', " + type.name() + " agent servers: '" + Arrays.toString(filterNameServers.toArray()) + "'"); 
		if (map.get(type) != null) {
			log.finer("logSourcesFilters method: source references: " + type.name() + " source names '" + Arrays.toString(map.get(type).getSourceNames().toArray()) 
					+ "', " + type.name() + " source hosts '" + Arrays.toString(map.get(type).getSourceHosts().toArray()) + "'");
		} else {
			log.finer("logSourcesFilters method: " + type.name() + " names: no entries with type " + type.name());
		}
	}
	
	public static void logSources(Map<SourceType, SourceReferences> map, SourceType type, List<String> filterNames) {
		log.finer("logSources method: " + type.name() + " names: '" + Arrays.toString(filterNames.toArray()) + "'"); 
		if (map.get(type) != null) {
			log.finer("logSources method: " + type.name() + " source references: '" + Arrays.toString(map.get(type).getSourceNames().toArray()) + "'");
		} else {
			log.finer("logSources method: " + type.name() + " names: no entries with type " + type.name());
		}
	}
	
	/**
	 * 
	 * @param record	- ActionRecord object
	 * @param type		- SourceType, i.e. Agent, AgentGroup, Monitor, Collector, or Server
	 * @param hosts		- List of strings which contains incident source's hosts
	 * @param servers	- List of strings which contains filters for servers of either Agents, or Monitors, or Collectors.
	 * @param quietTime	- quietTime indicator
	 * @return
	 */
	public static boolean isHostsMatches(ActionRecord record, SourceType type, List<String> hosts, List<String> servers, boolean quietTime) {
		Pattern pattern;
		for (String host : hosts) {
			if (host != null && !host.isEmpty()) {
				for (String server : servers) {
					if (server == null || server.isEmpty()) {
						continue;
					}
					pattern = getPattern(record, type, server, false);
					if (pattern != null) {
						if (pattern.matcher(host).matches()) {
							if (log.isLoggable(Level.FINER)) {
								log.finer("isEmailNeeded method: pattern for '" + type.name() + "' and server'" + server + "' matches host '" + host + "'");
							}
							
							return !quietTime; // was return true;
						}
					}
				}
			}
		}
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("isEmailNeeded method: there are no matches found for patterns " + Arrays.toString(servers.toArray()));
		}
		return false;
	}
	
	/**
	 * 
	 * @param record
	 * @param type
	 * @param sr
	 * @param filterNames
	 * @param quietTime
	 * @return
	 */
	public static boolean isSourceNamesMatches(ActionRecord record, SourceType type, SourceReferences sr, List<String> filterNames, boolean quietTime) {
		Pattern pattern;
		for (String name : sr.getSourceNames()) {				
			if (name != null && !name.isEmpty()) {
				for (String filterName : filterNames) {
					if (filterName == null || filterName.isEmpty()) {
						continue;
					}
					pattern = getPattern(record, type, filterName, true);
					// check if agent group from the agent groups filter list matches the name
					if (pattern != null) {
						if (pattern.matcher(name).matches()) {
							if (log.isLoggable(Level.FINER)) {
								log.finer("isEmailNeeded method: pattern for type '" + type.name() + "' and filter name '" + filterName + "' matches name '" + name + "'");
							}
							return !quietTime; // was return true;
						} else {
							if (log.isLoggable(Level.FINER)) {
								log.finer("isEmailNeeded method: pattern for type '" + type.name() + "' and filter name '" + filterName + "' does not match name '" + name + "'");
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public static boolean isSendOnlyPatterns(Map<String, String> substituterMap, List<String> sendOnlyPatterns, MailPluginProperties props) {
		Pattern pattern;
		boolean matchFound = false;
		
		// add sendOnlyPatterns
		if (sendOnlyPatterns != null && !sendOnlyPatterns.isEmpty()) {
			// Concatenate strings which we need pattern to match against
			StringBuilder sb = new StringBuilder();
			for (SendOnlyVariableNames sendOnly : SendOnlyVariableNames.values()) {
				sb.append(substituterMap.get(sendOnly.name()) == null ? "" : substituterMap.get(sendOnly.name())).append(";");
			}
			String sendOnlyVariables = sb.toString();
			if (log.isLoggable(Level.FINER)) {
				log.finer("isEmailNeeded method: sendOnlyVariables is '" + sendOnlyVariables + "'");
			}
			for (String p : sendOnlyPatterns) {
				if ((pattern = props.getSendOnlyPatterns().get(p)) != null) {
					if (pattern.matcher(sendOnlyVariables).matches()) {
						if (log.isLoggable(Level.FINER)) {
							log.finer("isEmailNeeded method: pattern '" + p + "' matches one of the SendOnlyVariables '" + sendOnlyVariables + "'");
						}
						matchFound = true;
						break;
					}
				}
			}
		}
		
		return matchFound;
	}
	
	public static boolean isMatchFound(Map<String, String> substituterMap, List<String> measureNamePatterns, MailPluginProperties props) {
		boolean matchFound = false;
		String s;
		
		if (measureNamePatterns != null && !measureNamePatterns.isEmpty()) {
			// check match for the VIOLATED_MEASURE_METRIC_NAME
			s = substituterMap.get(SubstituterFields.VIOLATED_MEASURE_METRIC_NAME.name());
			if (matchFound = isPatternMatches(s, measureNamePatterns, props.getMeasureNamePatterns(), null, false)) {
				return matchFound;
			}
			
			// check match for the VIOLATED_MEASURE_SPLITTINGS
			s = substituterMap.get(SubstituterFields.VIOLATED_MEASURE_SPLITTINGS.name());
			if (matchFound = isPatternMatches(s, measureNamePatterns, props.getMeasureNamePatterns(), null, false)) {
				return matchFound;
			}
			
			// check match for the VIOLATED_MEASURE_NAME_ALL
			s = substituterMap.get(SubstituterFields.VIOLATED_MEASURE_NAME_ALL.name());
			if (matchFound = isPatternMatches(s, measureNamePatterns, props.getMeasureNamePatterns(), props.getMeasureNamePatternsExtended(), true)) {
				return matchFound;
			}
			
			// check match for the VIOLATED_MEASURE_SPLITTINGS_ALL
			s = substituterMap.get(SubstituterFields.VIOLATED_MEASURE_SPLITTINGS_ALL.name());
			if (matchFound = isPatternMatches(s, measureNamePatterns, props.getMeasureNamePatterns(), props.getMeasureNamePatternsExtended(), true)) {
				return matchFound;
			}
			
		}
		
		return matchFound;
	}
		
	public static boolean isPatternMatches(String source, List<String> measureNamePatterns, Map<String, Pattern> measureNamePatternsMap, Map<String, Pattern> measureNamePatternsExtendedMap, boolean isExtended) {
		Pattern pattern;
		boolean matchFound = false;
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering isPatternMatches method");
		}
		
		if (!source.isEmpty()) {
			for (String p : measureNamePatterns) {
				if (isExtended) {
					if (!p.startsWith(MATCHES_ALL)) {
						p = MATCHES_ALL + p;
					}
					if (!p.endsWith(MATCHES_ALL)) {
						p = p + MATCHES_ALL;
					}
					pattern = measureNamePatternsExtendedMap.get(p);
				} else {
					pattern = measureNamePatternsMap.get(p);
				}
				if (log.isLoggable(Level.FINER)) {
					log.finer("isEmailNeeded method: checking if pattern '" + p + "' matches to the source string '" + source + "'");
				}
				if (pattern != null) {
					if (pattern.matcher(source).matches()) {
						if (log.isLoggable(Level.FINER)) {
							log.finer("isEmailNeeded method: pattern '" + p + "' matches the source '" + source + "'");
						}
						matchFound = true;
						break;
					}
				}
			}
		}
		
		return matchFound;
	}
	
	public static boolean isQuietTime(MailPluginProperties props) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_HH_MM);
		
		// calculate if it is quiet time now
		boolean quietTime = false; // send e-mail if it is not a quiet time, i.e. quietTime = false
		int quietFrom = props.getQuietTimeFrom();
		int quietTo = props.getQuietTimeTo();
		if (quietFrom != -1 && quietTo != -1) {
			int quietMinutes = getTimeMinutes(sdf.format(new Date()));
			if (((quietFrom < quietTo) && (quietMinutes >= quietFrom) && (quietMinutes <= quietTo)) || ((quietFrom >= quietTo) && (quietMinutes <= quietTo || quietMinutes >= quietFrom))) {
				quietTime = true;
			}
		}
		
		return quietTime;
	}
	
	public static Status updatePluginParams(Map<String, String> substituterMap, MailPluginProperties props, ActionRecord record) {
		StrSubstitutor sub = new StrSubstitutor(substituterMap);
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering updatePluginParams method");
		}
		
		//apply substitution to the subjectPrefix parameter
		if (props.getSubjectPrefix() != null && !props.getSubjectPrefix().isEmpty()) {
			record.setSubjectPrefixUpdated(sub.replace(props.getSubjectPrefix()));
		}
		
		//apply substitution to the subject parameter
		if (props.getSubject() != null && !props.getSubject().isEmpty()) {
			record.setSubjectUpdated(sub.replace(props.getSubject()));
		}
		
		//apply substitution to the subjectSuffix parameter
		if (props.getSubjectSuffix() != null && !props.getSubjectSuffix().isEmpty()) {
			record.setSubjectSuffixUpdated(sub.replace(props.getSubjectSuffix()));
		}
		
		//apply substitution to the bodyHeader parameter
		if (props.getBodyHeader() != null && !props.getBodyHeader().isEmpty()) {
			record.setBodyHeaderUpdated(sub.replace(props.getBodyHeader()));
		}
				
		//apply substitution to the body parameter
		if (props.getBody() != null && !props.getBody().isEmpty()) {
			record.setBodyUpdated(sub.replace(props.getBody()));
		}
		
		//apply substitution to the bodyFooter parameter
		if (props.getBodyFooter() != null && !props.getBodyFooter().isEmpty()) {
			record.setBodyFooterUpdated(sub.replace(props.getBodyFooter()));
		}
		
		return STATUS_SUCCESS;
	}
	
	public static void getSourceHostNames(String caller, Source source) {
		AgentSource agent;
		AgentGroupSource agentGroup;
		MonitorSource monitor;
		CollectorSource collector;
		ServerSource server;
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getSourceHostNames method");
		}
		
		if (source == null) {
			return;
		}
		
		StringBuilder sb = new StringBuilder(caller);
		switch(source.getSourceType()){
		case Agent:
			log.finer(sb.append(" Agent Name: '").append(((agent = (AgentSource)source).getName()) != null && !agent.getName().isEmpty() ? agent.getName() : BaseConstants.DASH).append("'").toString()); 
			log.finer(sb.append(" Agent Host: '").append(((agent = (AgentSource)source).getHost()) != null && !agent.getHost().isEmpty() ? agent.getHost() : BaseConstants.DASH).append("'").toString()); 
			break;
		case AgentGroup:
			log.finer(sb.append(" Agent Group Names: '").append(((agentGroup = (AgentGroupSource)source).getAgentGroupNames()) != null && !agentGroup.getAgentGroupNames().isEmpty() ? Arrays.toString(agentGroup.getAgentGroupNames().toArray()) : BaseConstants.DASH).append("'").toString()); 
			break;
		case Monitor:
			log.finer(sb.append(" Monitor Name: '").append(((monitor = (MonitorSource)source).getName()) != null && !monitor.getName().isEmpty() ? monitor.getName() : BaseConstants.DASH).append("'").toString()); 
			break;
		case Collector:
			log.finer(sb.append(" Collector Name: '").append(((collector = (CollectorSource)source).getName()) != null && !collector.getName().isEmpty() ? collector.getName() : BaseConstants.DASH).append("'").toString()); 
			log.finer(sb.append(" Collector Host: '").append(((collector = (CollectorSource)source).getHost()) != null && !collector.getHost().isEmpty() ? collector.getHost() : BaseConstants.DASH).append("'").toString()); 
			break;
		case Server:
			log.finer(sb.append(" Server Name: '").append(((server = (ServerSource)source).getName()) != null && server.getName() != null && !server.getName().isEmpty() ? server.getName() : BaseConstants.DASH).append("'").toString()); 
			break;
		default:
			break;
			
		}
		
	}
	
	protected Status processIncident(ActionEnvironment env, EmailConfigAction emailConfigAction, MailPluginProperties props, ActionRecord record) {
		Status status;
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering processIncident method");
		}
		
		emailConfigAction.setEmail(createEmail(props, record));
		emailConfigAction.setSubject(formatSubject(record));
		//TODO remove next commented lines. Duplicate call. Call is done in the sendMailAction method
//		if ((status = setEmailSubject(emailConfigAction)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
//			log.severe("processIncident method: " + status.getMessage());
//			return status;
//		}
		// first set the HTML text if needed
		if (props.isHtmlFormat()) {
			try {
				emailConfigAction.setHtmlText(updateImageReferences(formatHtmlContent(env, props, record), record));
			} catch (Exception e) {
				log.severe("MailExecutor: updateImageReferences method: " + e.getMessage());
				return new Status(StatusCode.ErrorInternalConfigurationProblem, e.getMessage(), e.getMessage(), e);
			}
		} else {		
			// then build a ASCII text message
			emailConfigAction.setPlainText(formatPlainTextContent(env, props, record));
		}

		if ((status = sendMailAction(emailConfigAction, props, record)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
			log.severe("processIncident method: " + status.getMessage());
			return status;
		}
		
		// return on success
		return status;
	}
	
	public static String updateImageReferences(String htmlContent, ActionRecord record) {
		int i, j;		
		String icon;
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering updateImageReferences method");
		}
		
		Map<String, String> dtBundleResourceMap = new HashMap<String, String>();
		while ((i = htmlContent.indexOf(DT_BUNDLE_RESOURCE_PREFIX)) != -1) {
			if (i > 0 && htmlContent.charAt(i - 1) == '"' && (j = htmlContent.indexOf('"', i)) != -1) {
				// path to the icon
				icon = htmlContent.substring(i + LENGTH_DT_BUNDLE_RESOURCE_PREFIX, j);
				String cidIcon = IncidentUtils.getContentId();
				dtBundleResourceMap.put(cidIcon, icon);
				htmlContent = new StringBuilder(DEFAULT_STRING_LENGTH).append(htmlContent.substring(0, i)).append("cid:").append(cidIcon).append(htmlContent.substring(j)).toString();
			} else {
				log.severe("MailExecutor: updateImageReferences method: wrong syntax for embedded images - missing '\"' before the 'dtbundleresource:' text or at the end of the string which embedded image");
				throw new RuntimeException("MailExecutor: updateImageReferences method: wrong syntax for embedded images - missing '\"' before the 'dtbundleresource' text");
			}
			
		}
		
		record.setDtBundleResourceMap(dtBundleResourceMap);
		
		return htmlContent;
	}
	
	protected Status sendMailAction(EmailConfigAction emailConfigAction, MailPluginProperties props, ActionRecord record) {
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering sendMailAction method");
		}
		
		Status status;
		if ((status = setEmailFields(emailConfigAction, props, record)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
			log.severe("sendMailAction method: " + status.getMessage());
			return status;
		}
		if ((status = setEmailSubject(emailConfigAction)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
			log.severe("sendMailAction method: " + status.getMessage());
			return status;
		}
		if ((status = setEmailBody(emailConfigAction, props, record)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) { //<======= continuue
			log.severe("sendMailAction method: " + status.getMessage());
			return status;
		}
		if ((status = setEmailBodyDashboards(emailConfigAction, props, record)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
			log.severe("sendMailAction method: " + status.getMessage());
			return status;
		}
		
    	// send the message
		if ((status = sendMessage(emailConfigAction.getEmail(), props, record)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
			log.severe("sendMailAction method: " + status.getMessage());
			return status;
		}
		
		// return on success
		return status;
	}
	
	public Status sendMessage(MimeMessage message, MailPluginProperties props, ActionRecord record) {
			if (log.isLoggable(Level.INFO)) {
				log.info("Entering sendMessage method");
			}
    		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			SMTPTransport t = null; // <-- main code 
	    	try {
	    		if (log.isLoggable(Level.INFO)) {
	    			String msg = "sendMessage method: recipients are '" + Arrays.toString(message.getAllRecipients()) + "'";
	    			log.info(msg);
	    		}
	    		if (!props.isSmtpSsl()) {
	    			t = (SMTPTransport)(record.getSmtpSession()).getTransport("smtp"); // <--- main...
	    			if (!props.isSmtpUserPassword()) { // ET: 06-27-2014 added if statement to check if we need user/password authentication for non-SSL communication
	    				t.connect(); // no authentication <-- MAIN code
	    			} else {
	    				t.connect(props.getSmtpUser(), props.getSmtpPassword());
	    			}
	    		} else {
	    			t = (SMTPTransport)(record.getSmtpSession()).getTransport("smtps");
	    			//t.connect(pp.getSmtpHost(), pp.getSmtpUser(), pp.getSmtpPassword());
	    			t.connect(props.getSmtpUser(), props.getSmtpPassword());
	    		}
	    		Address[] addresses = message.getAllRecipients();
	    		if (addresses == null) {
	    			throw new RuntimeException("Array of MimeMessage recipients is null");
	    		} else if (addresses.length == 0) {
	    			throw new RuntimeException("Array of MimeMessage recipients is empty");
	    		}
	    		t.sendMessage(message, addresses); // <--- main code
	    		if (log.isLoggable(Level.FINER)) {
					log.finer("sendMessage method: transport.sendMessage method call is successful");
				}
//	    		Transport.send(message);  // temporary code remove it
			} catch (NoSuchProviderException e) {
				String s = null;
				try {
						s = "recipients are '" + Arrays.toString(message.getAllRecipients()) + "'";
					} catch (MessagingException e1) {
						log.severe("sendMessage method: MessagingException exception, cannot get recipients. Exception is '" + HelperUtils.getExceptionAsString(e1) + "'");
						s = "cannot get recipients";
					}

				log.severe("sendMessage method: " + s + ", NoSuchProviderException exception is '" + HelperUtils.getExceptionAsString(e) + "'");
				return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, e.getMessage(), e.getMessage(), e);
			} catch (MessagingException e) {
				String s = null;
				try {
						s = "recipients are '" + Arrays.toString(message.getAllRecipients()) + "'";
					} catch (MessagingException e1) {
						log.severe("sendMessage method: MessagingException exception, cannot get recipients. Exception is '" + HelperUtils.getExceptionAsString(e1) + "'");
						s = "cannot get recipients";
					}

				log.severe("sendMessage method: " + s + ", MessagingException exception is '" + HelperUtils.getExceptionAsString(e) + "'");
				return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, e.getMessage(), e.getMessage(), e);
			} finally {
				try {
					if (t != null) {
						t.close();
					}
				} catch (Exception e2) {
					// do nothing
				}
			}
	    	
	    	return STATUS_SUCCESS;
    }
	
	public static Status setEmailBodyDashboards(EmailConfigAction emailConfigAction, MailPluginProperties props, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setEmailBodyDashboards method");
		}
		
		try {
			if (record.getDashboards() != null) {
				File file = null;
				MimeBodyPart mbp;
				
				// workaround to mitigate issue with attached reports in the HTML format (these reports are missing images when they are attached to the e-mail)
				if (!props.getDashboardsType().equalsIgnoreCase(REPORT_TYPE_HTML)) {
					for (String dashboard : record.getDashboards()) {
						try {
							file = getFileFromUrl(record.getUrlRestFilteringMap().get(dashboard), dashboard, props.getDashboardsType(), props.getDtUser(), props.getDtPassword());
						} catch (ReportCreationException e) {
							// go to the next dashboard
							log.finer("setEmailBodyDashboards method: skip report '" + dashboard + "' because of the following ReportCreationException '" + HelperUtils.getExceptionAsString(e));
							continue;
						}
						if (log.isLoggable(Level.FINER)) {
							log.finer("setEmailBodyDashboards method: the retrieved file is : '" + file.getCanonicalPath() + "'");
						}
						
						// attach the file to the message
						mbp = new MimeBodyPart();
						mbp.attachFile(file.getCanonicalPath());
						
						// add message to multipart
						emailConfigAction.getMp().addBodyPart(mbp);
					}
				} else {
					// Reports are set in the HTML format
					for (String dashboard : record.getDashboards()) {
						try {
							// get report in Excel format
							File fileExcel;
							fileExcel = getFileFromUrl(record.getUrlRestFilteringMap().get(dashboard).replace(REPORT_TYPE_URI_HTML, REPORT_TYPE_URI_XLS), dashboard, REPORT_TYPE_XLS, props.getDtUser(), props.getDtPassword());
							WorkbookSettings ws = new WorkbookSettings();
							ws.setLocale(Locale.getDefault());
							Workbook workbook = Workbook.getWorkbook(fileExcel, ws);
							// extract images from the workbook 
							List<String> list = getImagesFromExcelReport(workbook, props);
							// get report in the HTML format
							String htmlPage = FileUtils.readFileToString(getFileFromUrl(record.getUrlRestFilteringMap().get(dashboard), dashboard, props.getDashboardsType(), props.getDtUser(), props.getDtPassword()));
							// get updated HTML page
							String newHtmlPage = replaceImagesIntoHtmlPage(list, htmlPage); 
							// Write new html page to a file
							file = getNewHtmlPageFile(newHtmlPage, dashboard);
						
							if (log.isLoggable(Level.FINER)) {
								log.finer("setEmailBodyDashboards method: the retrieved file is : '" + file.getCanonicalPath() + "'");
							}
							
							// attach the file to the message
							mbp = new MimeBodyPart();
							mbp.attachFile(file.getCanonicalPath());
							
							// add message to multipart
							emailConfigAction.getMp().addBodyPart(mbp);
						} catch (ReportCreationException e) {
							// go to the next dashboard
							log.finer("setEmailBodyDashboards method: skip report '" + dashboard + "' because of the following exception '" + HelperUtils.getExceptionAsString(e));
							continue;
						} catch (BiffException e) {
							// go to the next dashboard
							log.finer("setEmailBodyDashboards method: skip report '" + dashboard + "' because of the following exception '" + HelperUtils.getExceptionAsString(e));
							continue;
						} catch (IOException e) {
							// go to the next dashboard
							log.finer("setEmailBodyDashboards method: skip report '" + dashboard + "' because of the following exception '" + HelperUtils.getExceptionAsString(e));
							continue;
						}						
					}
				}
			}
			
			// add the Multipart to the message
			emailConfigAction.getEmail().setContent(emailConfigAction.getMp());
			
			// set the Date: header
			emailConfigAction.getEmail().setSentDate(new Date());
			if (log.isLoggable(Level.FINER)) {
				log.finer("setEmailBodyDashboards method: ready to send mail to : " + record.getFilter().getTos());
			}
		} catch (AddressException e) {
			String msg;
			log.severe("setEmailBodyDashboards method: " + (msg = HelperUtils.getExceptionAsString(e)));
			return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, msg, msg, e);
		} catch (IOException e) {
			String msg;
			log.severe("setEmailBodyDashboards method: " + (msg = HelperUtils.getExceptionAsString(e)));
			return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, msg, msg, e);
		} catch (MessagingException e) {
			String msg;
			log.severe("setEmailBodyDashboards method: " + (msg = HelperUtils.getExceptionAsString(e)));
			return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, msg, msg, e);
		} catch (RuntimeException e) {
			String msg;
			log.severe("setEmailBodyDashboards method: " + (msg = HelperUtils.getExceptionAsString(e)));
			return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, msg, msg, e);
		}

		return STATUS_SUCCESS;
	}
	
	public static File getFileFromUrl(String strUrl, String dashboardName, String dashboardType, final String user, final String pwd) {
		String s, s1;
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getFileFromUrl method");
		}
		
		File file = null;
		if (log.isLoggable(Level.FINER)) {
			log.finer("getFileFromUrl method: contacting URL : '" + strUrl + "'; dashboardName is '" + dashboardName + "'; user is '" + user + "'; password is '" + pwd + "'");
		}

		URL url;
		FileOutputStream fos1 = null;
		InputStream is1 = null;
		try {
			url = new URL(strUrl);

			AuthCacheValue.setAuthCache(new AuthCacheImpl());
			Authenticator.setDefault(new Authenticator() { 
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					PasswordAuthentication pa = new PasswordAuthentication(user, pwd.toCharArray());
					if (log.isLoggable(Level.FINER)) {
						log.finer(String.format("getPasswordAuthentication method: user is '%s'; password is '%s'; class is '%s'", pa.getUserName(), new String(pa.getPassword()), pa.getClass().getCanonicalName()));
					}
					
					return pa;
				}
			});
			
			file = File.createTempFile(dashboardName, (s = new StringBuilder().append(".").append(dashboardType.toLowerCase()).toString()));
			if (log.isLoggable(Level.FINER)) {
				log.finer("getFileFromUrl method: suffix of the report file is '" + s + "'");
			}
			// The temp file needs to be deleted upon termination of this application
			file.deleteOnExit();

			byte[] ba1 = new byte[1024];
			int baLength;
			fos1 = new FileOutputStream(file);

			// Checking whether the URL contains requested report type
			URLConnection urlConnection = url.openConnection();
			int code;
			if (isHttpURLConnection(url) && !((code = getResponseCode(urlConnection)) >= 100 && code < 300)) {
				String msg = String.format("Response code '%d' returned with the message '%s' from url path '%s' url query '%s'", code, getResponseMessage(urlConnection), url.getPath(), url.getQuery());
				log.severe("getFileFromUrl method: '" + msg + "'");
				throw new ReportCreationException(msg);
			}
			if (!(s = urlConnection.getContentType()).equalsIgnoreCase((s1 = CONTENT_TYPES_MAP.get(dashboardType.toUpperCase())))) {
				String msg = String.format("Retrieved dashboard report has content type '%s', while it should have '%s' content type", s, s1);
				log.severe("getFileFromUrl method: " + msg);
				throw new ReportCreationException(msg);
			} else {
				if (log.isLoggable(Level.FINER)) {
					log.finer("getFileFromUrl method: content of the report file is '" + s + "'");
				}
				// Read report file from the URL and save to a local file
				is1 = url.openStream();
				while ((baLength = is1.read(ba1)) != -1) {
					fos1.write(ba1, 0, baLength);
				}
			}

		} catch (FileNotFoundException e) {
			String msg;
			log.severe("getFileFromUrl method: " + (msg = HelperUtils.getExceptionAsString(e)));
			throw new ReportCreationException(msg, e);
		} catch (MalformedURLException e) {
			String msg;
			log.severe("getFileFromUrl method: " + (msg = HelperUtils.getExceptionAsString(e)));
			throw new ReportCreationException(msg, e);
		} catch (IOException e) {
			String msg;
			log.severe("getFileFromUrl method: " + (msg = HelperUtils.getExceptionAsString(e)));
			throw new ReportCreationException(msg, e);
		} catch (RuntimeException e) {
			String msg;
			log.severe("getFileFromUrl method: " + (msg = HelperUtils.getExceptionAsString(e)));
			throw new ReportCreationException(msg, e);
		} finally {
			try {
				if (fos1 != null) {
					fos1.flush();
					fos1.close();
				}
				if (is1 != null) is1.close();
			} catch (IOException e) {
				// ignore
			}
		}

		return file;
	}
	
	protected Status setEmailBody(EmailConfigAction eConfig, MailPluginProperties props, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setEmailBody method");
		}
		
		String cid;
		// create and set Multipart message and Mime body part
//		eConfig.setMp(new MimeMultipart("alternative"));
		eConfig.setMp(new MimeMultipart("related"));
		MimeBodyPart wrap = new MimeBodyPart();
		MimeMultipart cover = new MimeMultipart("alternative");
		eConfig.setMbp1(new MimeBodyPart());
		
		// added 11-25-2014
		MailcapCommandMap mc=(MailcapCommandMap)CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);

		try {
			// fill the first message part of the multipart message
			if (props.isHtmlFormat()) {
				eConfig.getMbp1().setContent(eConfig.getHtmlText(), HTML_CONTENT);
				cover.addBodyPart(eConfig.getMbp1()); // ET 10-09-2013
				wrap.setContent(cover); // ET 10-09-2013
			
				// add first message to multipart email
//				eConfig.getMp().addBodyPart(eConfig.getMbp1());
				eConfig.getMp().addBodyPart(wrap); // ET 10-09-2013
				
				// set img icon if needed for status
				if (((cid = record.getCidStatusIconWarning()) != null && !cid.isEmpty()) || (cid = record.getCidStatusIconOk()) != null && !cid.isEmpty()) {
					MimeBodyPart imagePart = new MimeBodyPart();
					if ((cid = record.getCidStatusIconWarning()) != null && !cid.isEmpty()) {
						imagePart.attachFile((getFile("res/notification_email_warning.png")).getCanonicalPath());
					} else if ((cid = record.getCidStatusIconOk()) != null && !cid.isEmpty()) {
						imagePart.attachFile((getFile("res/notification_email_ok.png")).getCanonicalPath());
					}
					imagePart.setDisposition(MimeBodyPart.INLINE);
					imagePart.setContentID("<" + cid + ">");
					eConfig.getMp().addBodyPart(imagePart);
				}
				
				// set img for separator in the body of the e-mail if needed
				if ((cid = record.getCidSeparatorIcon()) != null && !cid.isEmpty()) {
					MimeBodyPart imagePartSeparator = new MimeBodyPart();
					imagePartSeparator.attachFile((getFile("res/separator.png")).getCanonicalPath());
					imagePartSeparator.setDisposition(MimeBodyPart.INLINE);
					imagePartSeparator.setContentID("<" + cid + ">");
					eConfig.getMp().addBodyPart(imagePartSeparator);
				}
				
				// set img icon if needed for before footer logo
				if ((cid = record.getCidSeparatorFooterIcon()) != null && !cid.isEmpty()) {
					MimeBodyPart imagePartSeparatorFooter = new MimeBodyPart();
					imagePartSeparatorFooter.attachFile((getFile("res/separator.png")).getCanonicalPath());
					imagePartSeparatorFooter.setDisposition(MimeBodyPart.INLINE);
					imagePartSeparatorFooter.setContentID("<" + cid + ">");
					eConfig.getMp().addBodyPart(imagePartSeparatorFooter);
				}
				
				// set img icon if needed for footer logo
				if ((cid = record.getCidFooterLogo()) != null && !cid.isEmpty()) {
					MimeBodyPart imagePartLogo = new MimeBodyPart();
					imagePartLogo.attachFile((getFile("res/compuware_logo.png")).getCanonicalPath());
					imagePartLogo.setDisposition(MimeBodyPart.INLINE);
					imagePartLogo.setContentID("<" + cid + ">");
					eConfig.getMp().addBodyPart(imagePartLogo);
				}
				
				// set embedded images which used the dtbundleresource notation
				Map<String, String> map;
				if ((map = record.getDtBundleResourceMap()) != null && !map.isEmpty()) {
					Set<Entry<String, String>> entries = map.entrySet();
					for (Entry<String, String> entry : entries) {
						MimeBodyPart imagePart = new MimeBodyPart();
						imagePart.attachFile(getFile(entry.getValue()).getCanonicalPath());
						imagePart.setDisposition(MimeBodyPart.INLINE);
						imagePart.setContentID("<" + entry.getKey() + ">");
						eConfig.getMp().addBodyPart(imagePart);
					}
				}
			} else {
				eConfig.getMbp1().setContent(eConfig.getPlainText(), PLAIN_TEXT_CONTENT);
				cover.addBodyPart(eConfig.getMbp1()); // ET 10-09-2013
				wrap.setContent(cover); // ET 10-09-2013
				// add first message to multipart email
//				eConfig.getMp().addBodyPart(eConfig.getMbp1());
				eConfig.getMp().addBodyPart(wrap); // ET 10-09-2013
			}
		} catch (MessagingException e) {
			String msg;
			log.severe("setEmailBody method: " + (msg = HelperUtils.getExceptionAsString(e)));
			return new Status(Status.StatusCode.ErrorInternalException, msg, msg, e);
		} catch (IOException e) {
			String msg;
			log.severe("setEmailBody method: " + (msg = HelperUtils.getExceptionAsString(e)));
			return new Status(Status.StatusCode.ErrorInternalException, msg, msg, e);
		} catch (RuntimeException e) {
			String msg;
			log.severe("setEmailBody method: " + (msg = HelperUtils.getExceptionAsString(e)));
			return new Status(Status.StatusCode.ErrorInternalException, msg, msg, e);
		}
		
		return STATUS_SUCCESS;
	}
	
	public File getFile(String fileName) throws IOException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getFile method: fileName is '" + fileName + "'");
		}
		URL url = this.getClass().getClassLoader().getResource(fileName);
		File file = File.createTempFile("tempimage", ".png");
		file.deleteOnExit();
		FileUtils.copyURLToFile(url, file);
		return file;
	}
	
	public static Status setEmailFields(EmailConfigAction emailConfig, MailPluginProperties props, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setEmailFields method");
		}
		
		List<Address> list;
		
		try {
			emailConfig.getEmail().setFrom(props.getFroms());
			if ((list = record.getFilter().getTos()) != null && !list.isEmpty()) {
				emailConfig.getEmail().setRecipients(Message.RecipientType.TO, (list = record.getFilter().getTos()).toArray(new Address[list.size()]));
			}
			if ((list = record.getFilter().getCc()) != null && !list.isEmpty()) {
				emailConfig.getEmail().setRecipients(Message.RecipientType.CC, (list = record.getFilter().getCc()).toArray(new Address[list.size()]));
			}
			if ((list = record.getFilter().getBcc()) != null && !list.isEmpty()) {
				emailConfig.getEmail().setRecipients(Message.RecipientType.BCC, (list = record.getFilter().getBcc()).toArray(new Address[list.size()]));
			}
		} catch (MessagingException e) {
			log.severe(e.getMessage());
			return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, e.getMessage(), e.getMessage(), e);
		}
		
		return STATUS_SUCCESS;
	}
	
	public static Status setEmailSubject(EmailConfigAction emailConfig) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setEmailSubject method");
		}
		
		try {
			emailConfig.getEmail().setSubject(emailConfig.getSubject());
		} catch (MessagingException e) {
			log.severe(e.getMessage());
			return new Status(Status.StatusCode.ErrorTargetServiceExecutionFailed, e.getMessage(), e.getMessage(), e);
		}
		return STATUS_SUCCESS;
	}
	
	// ET: added plain text processing 09-23-2013 start insertion
	public static String formatPlainTextContent(ActionEnvironment env, MailPluginProperties props, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering formatPlainTextContent method");
		}
		StringBuilder sb = new StringBuilder();
		// loop through the incidents
		int j = 0;
		for (Incident i : env.getIncidents()) {
			if (i != null) {
				appendBodyHeader(sb, record);
				sb.append(ls); // added line separator
				if (record != null && (record.getBodyUpdated() == null || record.getBodyUpdated().isEmpty())) {
					appendPlainTextTitle(sb, i, record);
					record.setMessage(i.getMessage()); // 06-09-2014 ET: added message
					appendPlainTextDetails(sb, env, i, record);
					appendPlainTextViolations(sb, env, i, record);
					appendPlainTextCustomData(sb, record);
				} else {
					appendBody(sb, record);
				}
			}
			if (log.isLoggable(Level.FINER)) {
				log.finer("formatPlainTextContent method: incident #" + ++j + ": string builder's content is '" + sb.toString() + "'");
			}
		}
		
		// add footer
		sb.append(ls);
		sb.append(ls);
		if (record != null && record.getBodyFooterUpdated() != null && !record.getBodyFooterUpdated().isEmpty()) {
			appendBodyFooter(sb, record);
		} else {
			appendPlainTextClientLink(sb, props, record);
			appendPlainTextReportLink(sb, props, record);
		}
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("formatPlainTextContent method: created string builder's content is '" + sb.toString() + "'");
		}
		return sb.toString();
	}
	
	public static void appendPlainTextTitle(StringBuilder sb, Incident i, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendPlainTextTitle method");
		}
		String s;
		appendRecordInformation(sb, i, record);
		sb.append(BaseConstants.COLON_WS);
		IncidentRule rule = i.getIncidentRule();
		if (rule != null && !(s = rule.getName()).isEmpty() && !s.equals(BaseConstants.DASH)) {
			sb.append(s);  // was "sb.append(record.getRuleConfigReference().getKeyId());"
		}
		if (rule != null && !(s = rule.getDescription()).isEmpty()) {
			sb.append(ls);
			sb.append(s);
		}

		sb.append(ls);
		sb.append(ls);
	}
	
	public static void appendRecordInformation(StringBuilder sb, Incident i, ActionRecord record){
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendRecordInformation method");
		}
		// JLT-26782 Add "Started" and "Ended" information to subject of incident emails
		if (Type.PerViolation == IncidentUtils.getSensitivityType(record) || i.getDuration().getDurationInMs() == 0) {
			sb.append(TextUtils.merge(MessagesConstants.BaseIncidentFormatter_INCIDENT_OCCURED_EMAIL_SUBJECT, IncidentUtils.getSeverityString(record)) );
			return;
		}
		if (i.isOpen()) {
			sb.append(TextUtils.merge(MessagesConstants.BaseIncidentFormatter_INCIDENT_STARTED_EMAIL_SUBJECT, IncidentUtils.getSeverityString(record)) );
		}
		else {
			sb.append(TextUtils.merge(MessagesConstants.BaseIncidentFormatter_INCIDENT_ENDED_EMAIL_SUBJECT, IncidentUtils.getSeverityString(record)) );
		}
	}
	
	public static void appendPlainTextDetails(StringBuilder sb, ActionEnvironment env, Incident i, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendPlainTextDetails method");
		}
		sb.append(MessagesConstants.BaseIncidentFormatter_DETAILS_HEADLINE_PLAIN_TEXT_EMAIL);
		sb.append(ls);
		sb.append(ls);

		appendPlainTextGeneralInfoRow(sb, getTimeDurationEntry(i));
		Map<SourceType, List<String>> sourceEntries = getSourceEntries(i);
		appendPlainTextGeneralInfoRow(sb, getSourceEntry(SourceType.Agent, sourceEntries, false));
		appendPlainTextGeneralInfoRow(sb, getSourceEntry(SourceType.AgentGroup, sourceEntries, false));
		appendPlainTextGeneralInfoRow(sb, getSourceEntry(SourceType.Monitor, sourceEntries, false));
		appendPlainTextGeneralInfoRow(sb, getSourceEntry(SourceType.Collector, sourceEntries, false));
		appendPlainTextGeneralInfoRow(sb, getSourceEntry(SourceType.Server, sourceEntries, false));
		appendPlainTextGeneralInfoRow(sb, getBusinessTransactionsEntry(i));
		appendPlainTextGeneralInfoRow(sb, getSystemProfileEntry(env));
		appendHTMLGeneralInfoRow(sb, getApplicationEntry(record.getApplication()));
		appendPlainTextGeneralInfoRow(sb, getServerNameEntry(i));

		for(Map.Entry<String, String> entry : getAdditionalGeneralInfoEntries(i)) {
			appendPlainTextGeneralInfoRow(sb, entry);
		}

		sb.append(ls);
	}
	
	public static void appendPlainTextGeneralInfoRow(StringBuilder sb, Map.Entry<String, String> entry) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendPlainTextGeneralInfoRow method");
		}
		if(entry != null && entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && !entry.getValue().isEmpty()) {
			sb.append(entry.getKey());
			sb.append(BaseConstants.COLON);
			sb.append(BaseConstants.WS);
			sb.append(entry.getValue());
			sb.append(ls);
		}
	}
	
	public static void appendPlainTextViolations(StringBuilder sb, ActionEnvironment env, Incident i, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendPlainTextViolations method");
		}
		sb.append(MessagesConstants.BaseIncidentFormatter_VIOLATIONS_HEADLINE_PLAIN_TEXT_EMAIL);
		sb.append(ls);
		sb.append(ls);

		Map<String, List<String>> violations = ActionHelper.getViolations(env, i);
		
		// check if we have '?' in the violation message
		// if yes, we will use message instead of violations
		boolean isMessage = false;
		top: for (String key : violations.keySet()) {
			for (String value : violations.get(key)) {
				if (value.indexOf(BaseConstants.QMARK) != -1) {
					// will use message instead of violations
					isMessage = true;
					if (log.isLoggable(Level.FINER)) {
						log.finer("appendHTMLViolations method: violation '" + value + "' contains '?'. Will use message in the e-mail body.");
					}
					break top;
				}
			}
		}
				
		if(!isMessage && violations != null && !violations.isEmpty()) {
			for(String key : violations.keySet()) {
				sb.append(key);
				sb.append(BaseConstants.COLON);
				sb.append(ls);

				for(String value : violations.get(key)) {
					sb.append(BaseConstants.WS);
					sb.append(value);
					sb.append(ls);
				}

				sb.append(ls);
			}
		} else {
			//no violated measures -> display message and description
			// ET 06-09-2014: commented out to avoid duplicate rule description
//			if ((rule = i.getIncidentRule()) != null && (s = rule.getDescription()) != null && !s.isEmpty()) {
//				sb.append(s);
//				sb.append(ls);
//			}

			sb.append(record.getMessage());
			sb.append(ls);
		}
	}
	
	public static void appendPlainTextCustomData(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendPlainTextCustomData method");
		}
		// to be overridden by subclasses
	}
	
	public static void appendPlainTextClientLink(StringBuilder sb, MailPluginProperties props, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendPlainTextClientLink method");
		}
		// assuming that Web Start is always available
		sb.append(BaseIncidentFormatter_OPEN_IN_DYNATRACE_EMAIL_LINK_TEXT).append(BaseConstants.COLON_WS);
		sb.append(getDashboardURI(props, record));

		sb.append(ls);
	}
	
	public static void appendPlainTextReportLink(StringBuilder sb, MailPluginProperties props, ActionRecord record) {
		// assuming that Web Start is always available
		sb.append(BaseIncidentFormatter_OPEN_IN_BROWSER_EMAIL_LINK_TEXT).append(BaseConstants.COLON_WS);
		sb.append(getDashboardRestURI(props, record));
	}
	
	// ET: added plain text processing 09-23-2013 end of insertion  
	
	public static String formatHtmlContent(ActionEnvironment env, MailPluginProperties props, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering formatHtmlContent method");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"); //$NON-NLS-1$
		sb.append(BaseConstants.HTML_BEGIN);
		sb.append(BaseConstants.HTML_HEAD_BEGIN);
		sb.append(HTML_STYLE);
		sb.append(BaseConstants.HTML_HEAD_END);
		sb.append(HTML_BODY_BEGIN);
		// loop through the incidents
		for (Incident i : env.getIncidents()) {
			if (i != null) {
				appendBodyHeader(sb, record);
				if (record != null && (record.getBodyUpdated() == null || record.getBodyUpdated().isEmpty())) {
					appendHTMLTitle(sb, i, record);
					appendSeparator(sb, record);
					sb.append(BaseConstants.HTML_BR);
					record.setMessage(i.getMessage());
					appendHTMLDetails(sb, env, i, record);
					appendHTMLViolations(sb, env, i, record);
					appendHTMLCustomData(sb, record);
				} else {
					appendBody(sb, record);
				}
			}
		}

		if (record != null && record.getBodyFooterUpdated() != null && !record.getBodyFooterUpdated().isEmpty()) {
			appendBodyFooter(sb, record);
		} else {
			appendSeparatorFooter(sb, record);
			appendLogoFooter(sb, props, record);
			appendBodyFooter(sb, record);
		}
		sb.append(BaseConstants.HTML_BODY_END);
		sb.append(BaseConstants.HTML_END);
		
		return sb.toString();
	}
	
	public static void appendBodyHeader(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendBodyHeader method");
		}
		
		if (record != null && record.getBodyHeaderUpdated() != null && !record.getBodyHeaderUpdated().isEmpty()) {
			sb.append(record.getBodyHeaderUpdated());
		}
	}
	
	public static void appendBodyFooter(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendBodyFooter method");
		}
		
		if (record != null && record.getBodyFooterUpdated() != null && !record.getBodyFooterUpdated().isEmpty()) {
			sb.append(record.getBodyFooterUpdated());
		}
	}

	public static void appendBody(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendBodyFooter method");
		}
		
		if (record != null && record.getBodyUpdated() != null && !record.getBodyUpdated().isEmpty()) {
			sb.append(record.getBodyUpdated());
		}
	}
	
	public static void appendLogoFooter(StringBuilder sb, MailPluginProperties props, ActionRecord record){
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendLogoFooter method");
		}
		
		sb.append(BaseConstants.HTML_BEGIN_TABLE);
		sb.append(BaseConstants.HTML_BEGIN_TABLE_ROW);
		sb.append(HTML_BEGIN_FOOTER_LOGO);
		appendLogo(sb, record);
		sb.append(BaseConstants.HTML_END_TABLE_DATA);
		sb.append(HTML_BEGIN_FOOTER_CLIENT_LINK);
		appendHTMLClientLink(sb, props, record);
		sb.append(BaseConstants.HTML_END_TABLE_DATA); 
		sb.append(HTML_BEGIN_FOOTER_REPORT_LINK);
		appendHTMLReportLink(sb, props, record);
		sb.append(BaseConstants.HTML_END_TABLE_DATA);
		sb.append(BaseConstants.HTML_END_TABLE_ROW);
		sb.append(BaseConstants.HTML_END_TABLE);
	}
	
	public static void appendHTMLReportLink(StringBuilder sb, MailPluginProperties props, ActionRecord record) {
			if (log.isLoggable(Level.FINER)) {
				log.finer("Entering appendHTMLReportLink method");
			}
		
			sb.append(BaseConstants.HTML_DIV_BEGIN);
			sb.append(BEGIN_LINK);
			sb.append(getDashboardRestURI(props, record));
			sb.append(BaseConstants.DQUOTE);
			sb.append(BaseConstants.RABRA);
			sb.append(MessagesConstants.BaseIncidentFormatter_OPEN_IN_BROWSER_EMAIL_LINK_TEXT);
			sb.append(BaseConstants.HTML_END_LINK);
			sb.append(BaseConstants.HTML_DIV_END);
	}
	
	public static String getDashboardRestURI(MailPluginProperties props, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getDashboardRestURI method");
		}
		
		// Create link to the http://<dtHost>:<dtPort>/rest/html/management/dashboards page
		return new StringBuilder().append(REST_REPORT_PROTOCOL).append(props.getDtHost()).append(BaseConstants.COLON).append(props.getDtPort()).append(URI_REST_DASHBOARDS).toString();
	}
	
	
	
	public static void appendLogo(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendLogo method");
		}
		
		if (record == null) {
			throw new RuntimeException("appendLogo method: ActionRecord object should not be null");
		}
		String cidFooterLogo = IncidentUtils.getContentId();
		record.setCidFooterLogo(cidFooterLogo);
		sb.append(TextUtils.merge(IMAGE_DIVISION, SEPERATOR_CLASS, new StringBuilder("cid:").append(cidFooterLogo).toString()));
	}
	
	public static void appendHTMLClientLink(StringBuilder sb, MailPluginProperties props, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendHTMLClientLink method");
		}
		
		sb.append(BaseConstants.HTML_DIV_BEGIN);
		sb.append(BEGIN_LINK);
		sb.append(getDashboardURI(props, record)); 
		sb.append(BaseConstants.DQUOTE);
		sb.append(BaseConstants.RABRA);
		sb.append(MessagesConstants.BaseIncidentFormatter_OPEN_IN_DYNATRACE_EMAIL_LINK_TEXT);
		sb.append(BaseConstants.HTML_END_LINK);
		sb.append(BaseConstants.HTML_DIV_END);
	}
	
	public static String getDashboardURI(MailPluginProperties props, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getDashboardURI method");
		}
		
		// assuming WebStart Interface is always available for the client
		Map<String, String> sub;
		StringBuilder link = new StringBuilder(DEFAULT_STRING_LENGTH).append(REST_REPORT_PROTOCOL).append(props.getDtHost()).append(BaseConstants.COLON).append(props.getDtPort()).append("/webstart/Client/client.jnlp");
		if (record != null && (sub = record.getSubstitutorMap()) != null) {
			String s;
			StringBuilder link1;
			link1 = link.append(QUERY_STRING_DASHBOARD_URI_1).append((s = sub.get("SYSTEM_PROFILE")) != null ? s : "").append(QUERY_STRING_DASHBOARD_URI_2).append((s = sub.get("KEY")) != null ? s : "");
			if (log.isLoggable(Level.FINER)) {
				log.finer("getDashboardURI method: Dashboard link is " + link1.toString());
			}
			return link1.toString().replaceAll(" ", "%20");
		} else {
			// shorten link
			return link.toString().replaceAll(" ", "%20");
		}
	}
	
	private static void appendHTMLCustomData(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendHTMLCustomData method");
		}
	}
	
	public static void appendHTMLViolations(StringBuilder sb, ActionEnvironment env, Incident incident, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendHTMLViolations method");
		}
		
		appendHTMLHeadline(sb, MessagesConstants.BaseIncidentFormatter_VIOLATIONS_HEADLINE_HTML_EMAIL);
		sb.append(HTML_BEGIN_VIOLATIONS_TABLE);

		Map<String, List<String>> violations = ActionHelper.getViolations(env, incident);
		
		// check if we have '?' in the violation message
		// if yes, we will use message instead of violations
		boolean isMessage = false;
		top: for (String key : violations.keySet()) {
			for (String value : violations.get(key)) {
				if (value.indexOf(BaseConstants.QMARK) != -1) {
					// will use message instead of violations
					isMessage = true;
					if (log.isLoggable(Level.FINER)) {
						log.finer("appendHTMLViolations method: violation '" + value + "' contains '?'. Will use message in the e-mail body.");
					}
					break top;
				}
			}
		}

		if(!isMessage && violations != null && !violations.isEmpty()) {
			
			for(String key : violations.keySet()) {
				sb.append(BaseConstants.HTML_BEGIN_TABLE_ROW);

				addViolationTableLabel(sb, key);
				List<String> list = violations.get(key);
				for(int i = 0; i < list.size(); ++i) {
					if(i > 0) {
						sb.append(BaseConstants.HTML_BEGIN_TABLE_ROW);
						addEmptyLabel(sb);
					}
					addViolationTableContent(sb, list.get(i));
					sb.append(BaseConstants.HTML_END_TABLE_ROW);
				}
			}
		}
		else {
			//no violated measures -> display message and description
			addViolationTableContent(sb, record.getMessage());
			addEmptyLabel(sb);
			if(record.getMessage() != null && !record.getMessage().isEmpty()){ //JLT-71424
				sb.append(BaseConstants.HTML_BEGIN_TABLE_DATA);
				sb.append(HTML_BEGIN_VIOLATION_CONTENT_DIV);
				sb.append(BaseConstants.COLON_WS);
				sb.append(BaseConstants.HTML_DIV_END);
				sb.append(BaseConstants.HTML_END_TABLE_DATA);
				addEmptyLabel(sb);
			}
			addViolationTableContent(sb, EMPTY_STRING); // no description exists b/c there are no violations
			addEmptyLabel(sb);
		}

		sb.append(BaseConstants.HTML_END_TABLE);
	}
			
	public static void addViolationTableContent(StringBuilder sb, String message) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering addViolationTableContent method");
		}
		
		if(message == null || message.isEmpty()) {
			return;
		}

		sb.append(BaseConstants.HTML_BEGIN_TABLE_DATA);
		sb.append(HTML_BEGIN_VIOLATION_CONTENT_DIV);
		sb.append(message);
		sb.append(BaseConstants.WS);
		sb.append(BaseConstants.HTML_DIV_END);
		sb.append(BaseConstants.HTML_END_TABLE_DATA);
	}
	
	public static void addEmptyLabel(StringBuilder sb) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering addEmptyLabel method");
		}
		
		sb.append(BaseConstants.HTML_BEGIN_TABLE_DATA);
		sb.append(BaseConstants.HTML_END_TABLE_DATA);
	}

	
	public static void addViolationTableLabel(StringBuilder sb, String message) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering addViolationTableLabel method");
		}
		
		sb.append(HTML_BEGIN_VIOLATIONS_TABLE_LABEL);
		sb.append(HTML_BEGIN_VIOLATIONS_LABEL_DIV);
		sb.append(message);
		sb.append(BaseConstants.COLON);
		sb.append(BaseConstants.HTML_DIV_END);
		sb.append(BaseConstants.HTML_END_TABLE_DATA);
	}
	
    
	public static void appendHTMLDetails(StringBuilder sb, ActionEnvironment env, Incident incident, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendHTMLDetails method");
		}
		
		appendHTMLHeadline(sb, MessagesConstants.BaseIncidentFormatter_DETAILS_HEADLINE_HTML_EMAIL);
		sb.append(HTML_BEGIN_DETAILS_TABLE);
		appendHTMLGeneralInfoRow(sb, getTimeDurationEntry(incident));
		Map<SourceType, List<String>> sourceEntries = getSourceEntries(incident);
		appendHTMLGeneralInfoRow(sb, getSourceEntry(SourceType.Agent, sourceEntries, true));
		appendHTMLGeneralInfoRow(sb, getSourceEntry(SourceType.AgentGroup, sourceEntries, true));
		appendHTMLGeneralInfoRow(sb, getSourceEntry(SourceType.Monitor, sourceEntries, true));
		appendHTMLGeneralInfoRow(sb, getSourceEntry(SourceType.Collector, sourceEntries, true));
		appendHTMLGeneralInfoRow(sb, getSourceEntry(SourceType.Server, sourceEntries, true));
		appendHTMLGeneralInfoRow(sb, getBusinessTransactionsEntry(incident));
		appendHTMLGeneralInfoRow(sb, getSystemProfileEntry(env));
		appendHTMLGeneralInfoRow(sb, getApplicationEntry(record.getApplication()));
		appendHTMLGeneralInfoRow(sb, getServerNameEntry(incident));

		for(Map.Entry<String, String> entry : getAdditionalGeneralInfoEntries(incident)) {
			appendHTMLGeneralInfoRow(sb, entry);
		}

		sb.append(BaseConstants.HTML_END_TABLE);
		sb.append(BaseConstants.HTML_BR);
		sb.append(BaseConstants.HTML_BR);
	}
	
	public static List<Map.Entry<String, String>> getAdditionalGeneralInfoEntries(Incident incident) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getAdditionalGeneralInfoEntries method");
		}
		// to be overridden by subclasses
		return Collections.emptyList();
	}
	
	public static Map.Entry<String, String> getServerNameEntry(Incident incident) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getServerNameEntry method");
		}
		
		if (incident != null) {
			return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_DYNATRACE_SERVER_EMAIL_TEXT_ENTRY, incident.getServerName());
		} else {
			return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_DYNATRACE_SERVER_EMAIL_TEXT_ENTRY, BaseConstants.DASH);
		}
	}
	
	public static Map.Entry<String, String> getSystemProfileEntry(ActionEnvironment env) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getSystemProfileEntry method");
		}
		
		return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_SYSTEM_PROFILE_EMAIL_TEXT_ENTRY, env.getSystemProfileName());
	}
	
	public static Map.Entry<String, String> getApplicationEntry(String application) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getApplicationEntry method");
		}
		
		if (application != null && !(application = application.trim()).isEmpty() && !application.equals(BaseConstants.DASH)) {
			return new StringEntry(MailPluginConstants.BaseIncidentFormatter_INCIDENT_APPLICATION_EMAIL_TEXT_ENTRY, application);
		} else {
			return null;
		}
	}
	
	public static Map.Entry<String, String> getBusinessTransactionsEntry(Incident incident) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getBusinessTransactionsEntry method");
		}
		
		StringBuilder s = new StringBuilder();
		
		Collection<Violation> violations;
		if (incident != null && (violations = incident.getViolations()) != null) {
			List<String> btList = new ArrayList<String>();
			for (Violation violation : violations) {
				Measure measure = null;
				if (violation != null && (measure = violation.getViolatedMeasure()) != null) {
					if (isInterfaceImplemented(measure.getClass().getCanonicalName(), CANONICAL_NAME_TRANSACTION_MEASURE)) {
						addToList(measure.getName(), btList);
					}
				}
			}
			
			for (String name : btList) {
				s.append(name).append(" is business transaction");
				s.append(BaseConstants.COMMA_WS);
			}
		}
		
		if(s.length() > 0) {
			s.delete(s.length() - 2, s.length() - 1);
			return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_BUSINESS_TRANSACTIONS_EMAIL_TEXT_ENTRY, s.toString());
		} else {
			//skip Business Transaction line in the Details section
			return null;
//			return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_BUSINESS_TRANSACTIONS_EMAIL_TEXT_ENTRY, s.append(BaseConstants.WS).append(BaseConstants.DASH).append(BaseConstants.WS).toString());
		}
	}
	
	public static void addToList(String mName, List<String> btList) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering addToList method");
		}
		
		if (mName != null && !mName.isEmpty()) {
			if (btList.size() > 0 && !btList.contains(mName)) {
				for (String name : btList) {
					if (name.indexOf(mName) != -1) {
						// mName is already in the list as a substring
						break;
					} else {
						// mName is new or name is part of mName
						if (mName.indexOf(name) != -1) {
							// replace name with mName
							if (!btList.remove(name)) {
								throw new RuntimeException(
										"method getBusinessTransaction entry: List does not contain entry '"
												+ name
												+ "' when it should. Internal Error");
							} else {
								btList.add(mName);
								break;
							}
						} else {
							// add mName
							btList.add(mName);
							break;
						}
					}
				} // for loop
			} else {
				btList.add(mName);
			}
		}
	}
	
	/**
	 * @param className is canonical name of the class, i.e. including full package name
	 * @param interfaceName is canonical name of the interface, i.e. including full package name
	 * @return
	 */
	public static boolean isInterfaceImplemented(String className,
			String interfaceName) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering isInterfaceImplemented method");
		}
		
		String s, s1;
		if (className != null && !(s = className.trim()).isEmpty()
				&& interfaceName != null
				&& !(s1 = interfaceName.trim()).isEmpty()) {
			try {
				Class<?> clazz = Class.forName(s);
				java.lang.reflect.Type[] interfaces = clazz
						.getGenericInterfaces();
				for (java.lang.reflect.Type i : interfaces) {
					if (i.toString().indexOf(s1) != -1) {
						return true;
					}
				}

			} catch (ClassNotFoundException e) {
				log.severe(e.getMessage());
				throw new RuntimeException(e.getMessage(), e);
			}
		} else {
			throw new RuntimeException("isInterfaceImplemented method: one or both parameters are not correct");
		}

		return false;
	}
	
	public static Map<SourceType, List<String>> getSourceEntries(Incident incident) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getSourceEntries method");
		}
		
		Map<SourceType, List<String>> sourceEntries = new HashMap<SourceType, List<String>>();
		sourceEntries.put(SourceType.Agent, new ArrayList<String>());
		sourceEntries.put(SourceType.AgentGroup, new ArrayList<String>());
		sourceEntries.put(SourceType.Monitor, new ArrayList<String>());
		sourceEntries.put(SourceType.Collector, new ArrayList<String>());
		sourceEntries.put(SourceType.Server, new ArrayList<String>());
		
		Collection<Violation> violations;
		if (incident != null && (violations = incident.getViolations()) != null) {
			if (log.isLoggable(Level.FINER)) {
				log.finer("getSourceEntries method: violations.size = " + violations.size());
			}
			for (Violation violation : violations) {
				// skip nulls
				if (violation == null) {
					continue;
				}
				Collection<TriggerValue> triggerValues ;
				if (log.isLoggable(Level.FINER)) {
					log.finer("getSourceEntries method: violation.getTriggerValues().size() =" + violation.getTriggerValues().size());
				}
				if ((triggerValues = violation.getTriggerValues()) != null && !triggerValues.isEmpty()) {
					for (TriggerValue tvalue : triggerValues) {
						Source source;
						if (tvalue != null && (source = tvalue.getSource()) != null) {
							if (source instanceof AgentSource) {
								StringBuilder sb = new StringBuilder();
								AgentSource agent = (AgentSource)source;
								String sTemp = sb.append(agent.getName()).append(BaseConstants.AT).append(agent.getHost()).toString();
								if (!sourceEntries.get(SourceType.Agent).contains(sTemp)) {
									sourceEntries.get(SourceType.Agent).add(sTemp);
								}
							} else if (source instanceof AgentGroupSource) {
								StringBuilder sb = new StringBuilder();
								AgentGroupSource agentGroupSource = (AgentGroupSource) source;
								Collection<String> agentNames;
								if (!(agentNames = agentGroupSource.getAgentGroupNames()).isEmpty()) {
									for (String name : agentNames) {
										sb.append(name).append(BaseConstants.COMMA_WS);
									}
									if(sb.length() > 0) {
										sb.delete(sb.length() - 2, sb.length() - 1);
										String sTemp = sb.toString();
										if (!sourceEntries.get(SourceType.AgentGroup).contains(sTemp)) {
											sourceEntries.get(SourceType.AgentGroup).add(sTemp);
										}
									}
								}
							} else if (source instanceof MonitorSource) {
								MonitorSource monitor = (MonitorSource)source;
								if (!sourceEntries.get(SourceType.Monitor).contains(monitor.getName())) {
									sourceEntries.get(SourceType.Monitor).add(monitor.getName());
								}
							} else if (source instanceof CollectorSource) {
								StringBuilder sb = new StringBuilder();
								CollectorSource collector = (CollectorSource) source;
								String sTemp = sb.append(collector.getName()).append(BaseConstants.AT).append(collector.getHost()).toString();
								if (!sourceEntries.get(SourceType.Collector).contains(sTemp)) {
									sourceEntries.get(SourceType.Collector).add(sTemp);
								}
							} else if (source instanceof ServerSource) {
								ServerSource server = (ServerSource)source;
								if (!sourceEntries.get(SourceType.Server).contains(server.getName())) {
									sourceEntries.get(SourceType.Server).add(server.getName());
								}
							}
						}
					}
				}
			}
		}
		
		return sourceEntries;
	}

	public static Map.Entry<String, String> getSourceEntry(SourceType sourceString, Map<SourceType, List<String>> sourceEntries, boolean isHtml) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getSourceEntry method");
		}
		
		String value;
		StringBuilder sb = new StringBuilder();
		switch (sourceString) {
		case Agent:
			for (String s : sourceEntries.get(SourceType.Agent)) {
				if (s.trim().equals("-@-") || s.trim().isEmpty()) {
					continue;
				}
				sb.append(s).append(BaseConstants.COMMA_WS);
			}
			if (sb.length() > 0) {
				sb.delete(sb.length() - 2, sb.length() - 1);
				value = sb.toString().trim();
				if (isHtml) {
					value = StringEscapeUtils.escapeHtml4(value);
				}
				return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_AFFECTED_AGENTS_EMAIL_TEXT_ENTRY, value);
			}
			break;
		case AgentGroup:
			for (String s : sourceEntries.get(SourceType.AgentGroup)) {
				if (s.trim().equals("-@-") || s.trim().isEmpty()) {
					continue;
				}
				sb.append(s).append(BaseConstants.COMMA_WS);
			}
			if (sb.length() > 0) {
				sb.delete(sb.length() - 2, sb.length() - 1);
				value = sb.toString().trim();
				if (isHtml) {
					value = StringEscapeUtils.escapeHtml4(value);
				}
				return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_AFFECTED_AGENT_GROUPS_EMAIL_TEXT_ENTRY, value);
			}
			break;
		case Monitor:
			for (String s : sourceEntries.get(SourceType.Monitor)) {
				if (s.trim().equals("-@-") || s.trim().isEmpty()) {
					continue;
				}
				sb.append(s).append(BaseConstants.COMMA_WS);
			}
			if (sb.length() > 0) {
				sb.delete(sb.length() - 2, sb.length() - 1);
				value = sb.toString().trim();
				if (isHtml) {
					value = StringEscapeUtils.escapeHtml4(value);
				}
				return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_AFFECTED_MONITORS_EMAIL_TEXT_ENTRY, value);
			}
			break;
		case Collector:
			for (String s : sourceEntries.get(SourceType.Collector)) {
				if (s.trim().equals("-@-") || s.trim().isEmpty()) {
					continue;
				}
				sb.append(s).append(BaseConstants.COMMA_WS);
			}
			if (sb.length() > 0) {
				sb.delete(sb.length() - 2, sb.length() - 1);
				value = sb.toString().trim();
				if (isHtml) {
					value = StringEscapeUtils.escapeHtml4(value);
				}
				return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_AFFECTED_COLLECTORS_EMAIL_TEXT_ENTRY, value);
			}
			break;
		case Server:
			for (String s : sourceEntries.get(SourceType.Server)) {
				if (s.trim().equals("-@-") || s.trim().isEmpty()) {
					continue;
				}
				sb.append(s).append(BaseConstants.COMMA_WS);
			}
			if (sb.length() > 0) {
				sb.delete(sb.length() - 2, sb.length() - 1);
				value = sb.toString().trim();
				if (isHtml) {
					value = StringEscapeUtils.escapeHtml4(value);
				}
				return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_AFFECTED_HOSTS_EMAIL_TEXT_ENTRY, value);
			}
			break;			
		}
		
		return null;
	}

	
	public static Map.Entry<String, String> getTimeDurationEntry(Incident incident) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getTimeDurationEntry method");
		}
		
		// time/duration
		if (incident != null && incident.getDuration() != null) {
			if (log.isLoggable(Level.FINER)) {
				log.finer("getTimeDurationEntry method: incident.getDuration().getDurationInMs() = " + incident.getDuration().getDurationInMs());
			}
			if (incident.getDuration().getDurationInMs() > 0) {
				StringBuilder timeSpanValueText = new StringBuilder();
				timeSpanValueText.append(TextUtils.formatDate(new Date(incident.getStartTime().getTimestampInMs()), PredefinedDateFormat.yyyyMMddHHmmss));
				timeSpanValueText.append(BaseConstants.WS);
				timeSpanValueText.append(BaseConstants.DASH);
				timeSpanValueText.append(BaseConstants.WS);
				timeSpanValueText.append(TextUtils.formatDate(new Date(incident.getEndTime().getTimestampInMs()), PredefinedDateFormat.yyyyMMddHHmmss));
				timeSpanValueText.append(BaseConstants.WS);
				timeSpanValueText.append(BaseConstants.LSBRA);
				timeSpanValueText.append(ActionHelper.getDurationAsString(incident));
				timeSpanValueText.append(BaseConstants.RSBRA);
				if (log.isLoggable(Level.FINER)) {
					log.finer("getTimeDurationEntry method: " + MessageFormat.format(MessagesConstants.BaseIncidentFormatter_INCIDENT_DURATION_EMAIL_TEXT_ENTRY, timeSpanValueText.toString()));
				}
				return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_DURATION_EMAIL_TEXT_ENTRY, timeSpanValueText.toString());
			} else {
				if (log.isLoggable(Level.FINER)) {
					log.finer("getTimeDurationEntry method: incident.getStartTime().getTimestampInMs() = " + incident.getStartTime().getTimestampInMs());
				}
				return new StringEntry(MessagesConstants.BaseIncidentFormatter_START_TIME_OF_INCIDENT_EMAIL_TEXT_ENTRY, TextUtils.formatDate(new Date(incident.getStartTime().getTimestampInMs()), PredefinedDateFormat.yyyyMMddHHmmss));
			}
		} else {
			return new StringEntry(MessagesConstants.BaseIncidentFormatter_INCIDENT_DURATION_EMAIL_TEXT_ENTRY, new StringBuilder().append(BaseConstants.WS).append(BaseConstants.DASH).append(BaseConstants.WS).toString());
		}
	}
	
	public static void appendHTMLGeneralInfoRow(StringBuilder sb, Map.Entry<String, String> entry) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendHTMLGeneralInfoRow(StringBuilder sb, Map.Entry<String, String> entry) method");
		}
		
		if(entry != null) {
			appendHTMLGeneralInfoRow(sb, entry.getKey(), entry.getValue());
		}
	}
	
	public static void appendHTMLGeneralInfoRow(StringBuilder sb, String header, String value) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendHTMLGeneralInfoRow(StringBuilder sb, String header, String value) method");
		}
		
		if(header == null || header.isEmpty() || value == null || value.isEmpty()) {
			return;
		}

		sb.append(BaseConstants.HTML_BEGIN_TABLE_ROW);
		sb.append(HTML_BEGIN_DETAILS_TABLE_LABEL);
		sb.append(HTML_BEGIN_DETAILS_LABEL_DIV);
		sb.append(header);
		sb.append(BaseConstants.COLON);
		sb.append(BaseConstants.HTML_DIV_END);
		sb.append(BaseConstants.HTML_END_TABLE_DATA);

		sb.append(BaseConstants.HTML_BEGIN_TABLE_DATA);
		sb.append(HTML_BEGIN_DETAILS_CONTENT_DIV);
		sb.append(value);
		sb.append(BaseConstants.HTML_DIV_END);
		sb.append(BaseConstants.HTML_END_TABLE_DATA);
		sb.append(BaseConstants.HTML_END_TABLE_ROW);
	}
	
	public static void appendHTMLHeadline(StringBuilder sb, String headline) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendHTMLHeadline method");
		}
		
		sb.append(HTML_BEGIN_HEADLINE_DIV);
		sb.append(headline);
		sb.append(BaseConstants.HTML_DIV_END);
	}

	
	public static void appendHTMLTitle(StringBuilder sb, Incident incident, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendHTMLTitle method");
		}
		
		sb.append(HTML_BEGIN_TITLE_TABLE);
		sb.append(BaseConstants.HTML_BEGIN_TABLE_ROW);

		sb.append(HTML_BEGIN_TITLE_IMAGE_CELL);
		appendStatusIcon(sb, incident, record);
		sb.append(BaseConstants.HTML_END_TABLE_DATA);

		sb.append(HTML_BEGIN_TITLE_CELL);
		sb.append(HTML_BEGIN_TITLE_HEADLINE_CONTENT_DIV);
		appendRecordInformation(sb, incident); // Severe/Warning/Informational incident occured/started/ended
		sb.append(BaseConstants.COLON_WS);
		
		String s = "";
		IncidentRule rule = record.getIncidents().get(FIRST_ELEMENT_COLLECTION).getIncidentRule();
		if (rule != null && !(s = rule.getName()).isEmpty() && !s.equals(BaseConstants.DASH)) { // et: added check on dash
			sb.append(record.getIncidents().get(FIRST_ELEMENT_COLLECTION).getIncidentRule().getName()); // record.getRuleConfigReference().getKeyId());
			if (log.isLoggable(Level.FINER)) {
				log.finer("appendHTMLTitle method: getName: '" + record.getIncidents().get(FIRST_ELEMENT_COLLECTION).getIncidentRule().getName() + "'");
			}
		}
		sb.append(BaseConstants.HTML_DIV_END);
		sb.append(BaseConstants.HTML_END_TABLE_DATA);

		sb.append(BaseConstants.HTML_END_TABLE_ROW);
		
		if (rule != null && !(s = rule.getDescription()).isEmpty() && !(s.equals(BaseConstants.DASH))) {
			sb.append(BaseConstants.HTML_BEGIN_TABLE_ROW);
			sb.append(HTML_BEGIN_TITLE_DESCRIPTION_CELL);
			sb.append(HTML_BEGIN_TITLE_DESCRIPTION_DIV);
			sb.append(rule.getDescription());
			sb.append(BaseConstants.HTML_DIV_END);
			sb.append(BaseConstants.HTML_END_TABLE_DATA);
			sb.append(BaseConstants.HTML_END_TABLE_ROW);
		}
		
		sb.append(BaseConstants.HTML_END_TABLE);
	}
	
	public static void appendSeparator(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendSeparator method");
		}
		
		if (record == null) {
			throw new RuntimeException("ActionRecord object must not be null");
		}
		String cidSeparatorIcon = IncidentUtils.getContentId();
		record.setCidSeparatorIcon(cidSeparatorIcon);
		sb.append(HTML_BRK);
		sb.append(TextUtils.merge(IMAGE_DIVISION, SEPERATOR_CLASS, new StringBuilder("cid:").append(cidSeparatorIcon).toString()));
	}
	public static void appendSeparatorFooter(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendSeparatorFooter method");
		}
		
		String cidSeparatorFooterIcon = IncidentUtils.getContentId();
		if (record == null) {
			throw new RuntimeException("ActionRecord object muxt not be null");
		}
		record.setCidSeparatorFooterIcon(cidSeparatorFooterIcon);
		sb.append(TextUtils.merge(IMAGE_DIVISION, SEPERATOR_CLASS, new StringBuilder("cid:").append(cidSeparatorFooterIcon).toString()));
	}	
	public static void appendStatusIcon(StringBuilder sb, Incident incident, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendStatusIcon method");
		}
		
		String cidStatusIcon = IncidentUtils.getContentId();
		if (Type.PerViolation == IncidentUtils.getSensitivityType(incident) || incident.isOpen() || incident.getDuration().getDurationInMs() == 0){
			sb.append("<img src=\"cid:").append(cidStatusIcon).append("\" />");
			record.setCidStatusIconWarning(cidStatusIcon);
		} else {
			sb.append("<img src=\"cid:").append(cidStatusIcon).append("\" />");
			record.setCidStatusIconOk(cidStatusIcon);
		}

	}
	
	public static String formatSubject(ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering formatSubject method");
		}
		// <Severity> Incident <Started/Ended/Occurred>: <IncidentRule name>
		// example: Informational Incident Started: dynaTrace Server Online
		
		StringBuilder sb = new StringBuilder(DEFAULT_STRING_LENGTH);
		appendSubjectPrefix(sb, record);
		String subjectUpdated;
		if (record != null && (subjectUpdated = record.getSubjectUpdated()) != null && !subjectUpdated.isEmpty()) {
			sb.append(subjectUpdated).append(BaseConstants.WS);
		} else {
			Incident incident;
			if (record != null && record.getIncidents() != null && !record.getIncidents().isEmpty() && (incident = record.getIncidents().get(FIRST_ELEMENT_COLLECTION)) != null) {
				appendRecordInformation(sb, incident);
			} else {
				sb.append(BaseConstants.DASH);
			}
			
			sb.append(BaseConstants.COLON_WS);
			sb.append(IncidentUtils.getIncidentRuleName(record)); // was record.getRuleConfigReference().getKeyId());
		}

		appendSubjectSuffix(sb, record);
		return sb.toString();
	}
		
	public static void appendSubjectPrefix(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendSubjectPrefix method");
		}
		
		if (record != null && record.getSubjectPrefixUpdated() != null && !record.getSubjectPrefixUpdated().isEmpty()) {
			sb.append(record.getSubjectPrefixUpdated());
			sb.append(BaseConstants.WS);
		}
	}
	
	public static void appendRecordInformation(StringBuilder sb, Incident incident){
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendRecordInformation method");
		}

		if (Type.PerViolation == IncidentUtils.getSensitivityType(incident) || IncidentUtils.getDuration(incident) == 0) {
			sb.append(TextUtils.merge(MessagesConstants.BaseIncidentFormatter_INCIDENT_OCCURED_EMAIL_SUBJECT, IncidentUtils.getSeverityString(incident)) );
			return;
		}
		if (incident.isOpen()) {
			sb.append(TextUtils.merge(MessagesConstants.BaseIncidentFormatter_INCIDENT_STARTED_EMAIL_SUBJECT, IncidentUtils.getSeverityString(incident)) );
		}
		else {
			sb.append(TextUtils.merge(MessagesConstants.BaseIncidentFormatter_INCIDENT_ENDED_EMAIL_SUBJECT, IncidentUtils.getSeverityString(incident)) );
		}
	}
	
	public static void appendSubjectSuffix(StringBuilder sb, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering appendSubjectSuffix method");
		}
		
		if (record != null && record.getSubjectSuffixUpdated() != null && !record.getSubjectSuffixUpdated().isEmpty()) {
				sb.append(record.getSubjectSuffixUpdated());
		}
	}
	
	public static MimeMessage createEmail(MailPluginProperties mpp, ActionRecord record) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering createEmail method");
		}
    	
    	Properties props = new Properties(System.getProperties());
    	
    	String smtp;
		if (mpp.isSmtpSsl()) {
			smtp = SMTPS;
		} else {
			smtp = SMTP;
		}
		
	    props.put(new StringBuffer().append("mail.").append(smtp).append(".host").toString(), mpp.getSmtpHost());
//		props.put(new StringBuffer().append("mail.").append(smtp).append(".auth").toString(), mpp.isSmtpSsl() ? "true" : "false"); // ET: 06-27-2014 replaced with next line to accommodate need in user/password for non-SSL communication 
	    props.put(new StringBuffer().append("mail.").append(smtp).append(".auth").toString(), mpp.isSmtpUserPassword() ? "true" : "false");
		props.put(new StringBuffer().append("mail.").append(smtp).append(".port").toString(), mpp.getSmtpPort().toString());
		
		if (log.isLoggable(Level.FINER)) {
			for (Entry<Object, Object> entry : props.entrySet()) {
				log.finer("createEmail method: props object:  key is '" + entry.getKey() + "'; value is '" + entry.getValue() + "'");
			}
		}
		

		// Get and set a Session object
		Session session = Session.getInstance(props, null);
		record.setSmtpSession(session);

		// create a message
		return new MimeMessage(session);
    }    
	
	public static void setDashboardUrl(MailPluginProperties props) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setDashboardUrl method");
		}
		
		if (props == null) {
			log.severe("setDashboardUrl method: " + PLUGIN_PROPERTIES_IS_NULL);
			throw new RuntimeException(PLUGIN_PROPERTIES_IS_NULL);
		}
		
		StringBuilder sbUrlPrefix = new StringBuilder(DEFAULT_STRING_LENGTH);
		StringBuilder sbUrlSuffix = new StringBuilder();
		props.setDashboardUrls(new HashMap<String, String>());
		props.setUrlPrefix(sbUrlPrefix.append(REST_REPORT_PROTOCOL).append(props.getDtHost()).append(BaseConstants.COLON).append(props.getDtPort()).append(REST_REPORT_URL).toString());
		props.setUrlSuffix(sbUrlSuffix.append(REST_REPORT_URL_SUFFIX).append(props.getDashboardsType()).toString());
		if (props.getDashboards() != null) {
			for (String dashboard : props.getDashboards()) {
				StringBuilder sbUrl = new StringBuilder(DEFAULT_STRING_LENGTH);
				props.getDashboardUrls().put(dashboard, sbUrl.append(props.getUrlPrefix()).append(dashboard.replaceAll(" ", "%20")).append(props.getUrlSuffix()).toString());
			}
		}
	}
	
	public void setFooterUrl(MailPluginProperties props) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setFooterUrl method");
		}
		
		if (props == null) {
			log.severe("setFooterUrl method: " + PLUGIN_PROPERTIES_IS_NULL);
			throw new RuntimeException(PLUGIN_PROPERTIES_IS_NULL);
		}
		props.setFooterUrl(this.getClass().getClassLoader().getResource("res/footer.png"));
	}
		
    private Status setConfiguration(ActionEnvironment env, MailPluginProperties props) {
    	if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setConfiguration method");
		}
    	
		// Variable which holds reference to parameters values
		String value;
				
		if (props == null) {
			log.severe("setConfiguration method: " + PLUGIN_PROPERTIES_IS_NULL);
			return new Status(Status.StatusCode.ErrorInternal, PLUGIN_PROPERTIES_IS_NULL, PLUGIN_PROPERTIES_IS_NULL);
		}
		
		// set Incident Rule Name
		props.setIncidentRuleName(value = env.getConfigString(CONFIG_INCIDENT_RULE_NAME));
		if (value == null || value.trim().isEmpty()) {
			String msg = "setConfiguration method: the Incident Rule Name is null or empty. Please set plugin's mandatory parameter Incident Rule Name";
	    	log.severe(msg);
			return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg);
		}
		
		// set Identity String
		props.setIdentityString(env.getConfigString(CONFIG_IDENTIFYING_STRING));
		
		// set hostNameToIpAddressRaw and hostNameToIpAddress parameters
  		props.setHostNameToIpAddressRaw(value = env.getConfigString(CONFIG_HOST_NAME_TO_IP_ADDRESS).trim());
  		Properties p = new Properties();
  	    try {
  	    	if (value != null && !value.isEmpty()) {
  	    		p.load(new StringReader(value));
  	    	}
  			props.setHostNameToIpAddress(p);
  		} catch (IOException e) {
  			throw new RuntimeException(e.getMessage());
  		}
  	    
  	    // Set froms
  		// move it to the setFrom method
  		try {
  			value = env.getConfigString(CONFIG_FROM);
  			if (value != null && !value.trim().isEmpty()) {
  				props.setFroms(new InternetAddress(value.trim()));
  			} else {
  				// Set default from e-mail address
  				Address froms = new InternetAddress(MailPluginConstants.BACKUP_FROM_EMAIL_ADDRESS);
  				props.setFroms(froms);
  			}
  		} catch (AddressException e) {
  			log.severe("setConfiguration method: " + e.getMessage());
  			return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, e.getMessage(), e.getMessage(), e);
  		}
  	    
  	    props.setEmailsFiltersCoupled(env.getConfigBoolean(CONFIG_ARE_EMAILS_FILTERS_COUPLED));
  	    
  	    if (!props.isEmailsFiltersCoupled()) {
			try {
				// Set tos
				props.setTos(parseEmails((value = env.getConfigString(CONFIG_TO)) == null ? null : value.trim().split(BaseConstants.SCOLON)));
				
				// Set cc
				props.setCc(parseEmails((value = env.getConfigString(CONFIG_CC)) == null ? null : value.trim().split(BaseConstants.SCOLON))); 
				
				// Set bcc
				props.setBcc(parseEmails((value = env.getConfigString(CONFIG_BCC)) == null ? null : value.trim().split(BaseConstants.SCOLON)));
			
				if (((props.getTos() == null || props.getTos().isEmpty()) && (props.getCc() == null || props.getCc().isEmpty()) && (props.getBcc() == null || props.getBcc().isEmpty()))) {
					log.severe("setConfiguration method: " + TO_CC_BCC_ARE_NOT_SET);
					return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, TO_CC_BCC_ARE_NOT_SET, TO_CC_BCC_ARE_NOT_SET);
				}
			} catch (RuntimeException e) {
				log.severe("setConfiguration method: " + e.getMessage());
				return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, e.getMessage(), e.getMessage(), e);
			}
  	    } else {
  	    	// process Emails/Filters couples
  	    	props.setEmailsFiltersDependencyFile((value = env.getConfigString(CONFIG_EMAILS_FILTERS_DEPENDENCY_FILE)) != null ? value.trim() : EMPTY_STRING);
  	    	if (props.getEmailsFiltersDependencyFile() == null || props.getEmailsFiltersDependencyFile().isEmpty()) {
  	    		String msg = "setConfiguration method: the Emails-Filters Dependency File is null or empty. Please set plugin's parameter Emails-Filters Dependency File";
  	    		log.severe(msg);
				return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg);
  	    	}
  	    	Status status;
            props.setFilters(new ArrayList<Filter>());
  	    	if ((status = setEmailsFiltersCouples(props)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
				log.severe("setConfiguration method: setEmailsFiltersCouples method returned message '" + status.getMessage() + "'");
				return status;
			}
  	    }
		
		// set html format indicator
		props.setHtmlFormat(env.getConfigBoolean(CONFIG_HTML_FORMAT));
				
		// Set subject prefix
		props.setSubjectPrefix(env.getConfigString(CONFIG_SUBJECT_PREFIX));
		
		// Set subject
		props.setSubject(env.getConfigString(CONFIG_SUBJECT));		
		
		// Set subject suffix
		props.setSubjectSuffix(env.getConfigString(CONFIG_SUBJECT_SUFFIX));
				 
		// Set e-mail body header
		props.setBodyHeader(env.getConfigString(CONFIG_BODY_HEADER));
		
		// Set e-mail body
		props.setBody(env.getConfigString(CONFIG_BODY));
		
		// Set e-mail body footer
		props.setBodyFooter(env.getConfigString(CONFIG_BODY_FOOTER));

		// Set dashboards
//		props.setDashboards((value = env.getConfigString(CONFIG_DASHBOARD)) != null && !value.trim().isEmpty() ? value.split(BaseConstants.SCOLON) : null);
		props.setDashboards(trimArray((value = env.getConfigString(CONFIG_DASHBOARD)) != null && !value.trim().isEmpty() ? value.split(BaseConstants.SCOLON) : null));
		
		// set dashboards' type
		props.setDashboardsType(env.getConfigString(CONFIG_DASHBOARDS_TYPE).toUpperCase());
		
		// Set REST filter by agent names/hosts
		props.setFilterAgentNameHost(env.getConfigBoolean(CONFIG_REST_FILTERING_AGENT_NAME_HOST));
		
		// Set REST filter by agent groups
		props.setFilterAgentGroup(env.getConfigBoolean(CONFIG_REST_FILTERING_AGENT_GROUP));
		
		// Set REST filter by custom timeframe
		props.setFilterCustomTimeframe(env.getConfigBoolean(CONFIG_REST_FILTERING_CUSTOM_TIMEFRAME));
		
		// Set SMTP host
		props.setSmtpHost((value = env.getConfigString(CONFIG_SMTP_HOST)) != null ? value.trim() : null);
		
		// Set mailPort
		Long mailPort;
		props.setSmtpPort(mailPort = env.getConfigLong(CONFIG_SMTP_PORT));
		if (mailPort == null || mailPort < 1) {
			String message = new StringBuilder(DEFAULT_STRING_LENGTH)
					.append("Value of the 'Port' parameter '").append(mailPort).append("' is incorrect.").toString();
			log.severe("setConfiguration method: " + message);
			return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, message, message);
		} else {
			props.setSmtpPort(mailPort);
		}
		
		// Set quiet time from
		try {
			props.setQuietTimeFrom(getTimeMinutes(env.getConfigString(CONFIG_QUIET_TIME_FROM).trim()));
		} catch (Exception e) {
			String msg = HelperUtils.getExceptionAsString(e);
			log.severe("setConfiguration method: " + msg);
			return new Status(StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
		}
		
		// Set quiet time to
		try {
			props.setQuietTimeTo(getTimeMinutes(env.getConfigString(CONFIG_QUIET_TIME_TO).trim()));
		} catch (Exception e) {
			String msg = HelperUtils.getExceptionAsString(e);
			log.severe("setConfiguration method: " + msg);
			return new Status(StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
		}
		// check that quiet time to and from are or both present or both not present
		if ((props.getQuietTimeFrom()!= -1 || props.getQuietTimeTo() != -1) && !(props.getQuietTimeFrom() != -1 && props.getQuietTimeTo() != -1)) {
			log.severe("setConfiguration method: " + QUIET_TIME_FROM_TO_ERROR);
			return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, QUIET_TIME_FROM_TO_ERROR, QUIET_TIME_FROM_TO_ERROR);
		}
		// set isSMTPUserPassword
		props.setSmtpUserPassword(env.getConfigBoolean(CONFIG_SMTP_USER_PASSWORD));
		
		// Set smtp user
		props.setSmtpUser((value = env.getConfigString(CONFIG_SMTP_USER)) != null ? value.trim() : null);
		
		// Set smtp password
		props.setSmtpPassword(env.getConfigPassword(CONFIG_SMTP_PASSWORD));
		
		// set smtp ssl
		props.setSmtpSsl(env.getConfigBoolean(CONFIG_SMTP_SSL));
		
		if (props.isSmtpUserPassword()) {
			if (props.getSmtpUser().isEmpty() || props.getSmtpPassword().isEmpty()) {
				String msg = "isSMTPUserPassword indicator is set to '" + props.isSmtpSsl() + "', while smtp user or/and smtp password were not provided. Smtp user is '" + props.getSmtpUser() 
						+ "' and smtp password is '" + props.getSmtpPassword() 
						+ "'.";
				log.severe("setConfiguration method: " + msg);
				return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg);
			} 
		}

		// Set dtuser
		props.setDtUser(value = (env.getConfigString(CONFIG_DT_USER) != null ? env.getConfigString(CONFIG_DT_USER).trim() : null));
		if (value == null || value.trim().isEmpty()) {
			log.severe("setConfiguration method: " + DTUSER_IS_EMPTY);
			return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, DTUSER_IS_EMPTY, DTUSER_IS_EMPTY);
		}
		
		// Set dtHost
		props.setDtHost(value = (env.getConfigString(CONFIG_DT_HOST) != null ? env.getConfigString(CONFIG_DT_HOST).trim() : null));
		if (value == null || value.isEmpty()) {
			log.severe("setConfiguration method: " + DTHOST_IS_EMPTY);
			return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, DTHOST_IS_EMPTY, DTHOST_IS_EMPTY);
		}
		if (log.isLoggable(Level.FINER)) {
			log.finer("setConfiguration method: dthost is '" + props.getDtHost() + "'");
		}
		
		// Set dtport
		Long port;
		props.setDtPort(port = env.getConfigLong(CONFIG_DT_PORT));
		if (port == null || port < 1) {
			String message = new StringBuilder(DEFAULT_STRING_LENGTH)
				.append("Value of the 'Port' parameter '").append(port).append("' is incorrect.").toString();
			log.severe("setConfiguration method: " + message);		
			return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, message, message);	
		} else {
			props.setDtPort(port);
		}

		// Set dtpassword
		props.setDtPassword(value = env.getConfigPassword(CONFIG_DT_PASSWORD));
		// Check dtpassword: allow spaces hence no trim method
		if (value == null || value.isEmpty()) {
			log.severe("setConfiguration method: " + DTPASSWORD_IS_EMPTY);	
			return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, DTPASSWORD_IS_EMPTY, DTPASSWORD_IS_EMPTY);
		}
		
		Status status;
		if (!props.isEmailsFiltersCoupled()) {
			// Set agents
			if ((value = env.getConfigString(CONFIG_AGENTS)) != null && !value.trim().isEmpty()) {
				props.setAgents(new HashMap<String, Pattern>());
				if ((status = setPatterns(value, props.getAgents())).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
					log.severe("execute method: setPatterns method for Agents returned error status. Message is '" + status.getMessage() + "'");
					return status;
				}
			}
			
			// Set agent servers
			if ((value = env.getConfigString(CONFIG_AGENT_SERVERS)) != null && !value.trim().isEmpty()) {
				props.setAgentServers(new HashMap<String, Pattern>());
				if ((status = setPatterns(value, props.getAgentServers())).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
					log.severe("execute method: setPatterns method for Agent Servers returned error status. Message is '" + status.getMessage() + "'");
					return status;
				}
			}
			
			// Set agent groups
			if ((value = env.getConfigString(CONFIG_AGENT_GROUPS)) != null && !value.trim().isEmpty()) {
				props.setAgentGroups(new HashMap<String, Pattern>());
				if ((status = setPatterns(value, props.getAgentGroups())).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
					log.severe("execute method: setPatterns method for AgentGroups returned error status. Message is '" + status.getMessage() + "'");
					return status;
				}
			}
			
			// Set monitors
			if ((value = env.getConfigString(CONFIG_MONITORS)) != null && !value.trim().isEmpty()) {
				props.setMonitors(new HashMap<String, Pattern>());
				if ((status = setPatterns(value, props.getMonitors())).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
					log.severe("execute method: setPatterns method for Monitors returned error status. Message is '" + status.getMessage() + "'");
					return status;
				}
			}
			
			// Set monitor servers
			if ((value = env.getConfigString(CONFIG_MONITOR_SERVERS)) != null && !value.trim().isEmpty()) {
				props.setMonitorServers(new HashMap<String, Pattern>());
				if ((status = setPatterns(value, props.getMonitorServers())).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
					log.severe("execute method: setPatterns method for Monitors returned error status. Message is '" + status.getMessage() + "'");
					return status;
				}
			}
			
			// Set collectors
			if ((value = env.getConfigString(CONFIG_COLLECTORS)) != null && !value.trim().isEmpty()) {
				props.setCollectors(new HashMap<String, Pattern>());
				if ((status = setPatterns(value, props.getCollectors())).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
					log.severe("execute method: setPatterns method for Collectors returned error status. Message is '" + status.getMessage() + "'");
					return status;
				}
			}
			
			// Set collector servers
			if ((value = env.getConfigString(CONFIG_COLLECTOR_SERVERS)) != null && !value.trim().isEmpty()) {
				props.setCollectorServers(new HashMap<String, Pattern>());
				if ((status = setPatterns(value, props.getCollectorServers())).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
					log.severe("execute method: setPatterns method for Collectors returned error status. Message is '" + status.getMessage() + "'");
					return status;
				}
			}
					
			// Set hosts
			if ((value = env.getConfigString(CONFIG_SERVERS)) != null && !value.trim().isEmpty()) {
				props.setServers(new HashMap<String, Pattern>());
				if ((status = setPatterns(value, props.getServers())).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
					log.severe("execute method: setPatterns method for Hosts returned error status. Message is '" + status.getMessage() + "'");
					return status;
				}
			}
		}
		
		// set sendOnlyPatterns
		props.setSendOnlyPatterns(new HashMap<String, Pattern>());
		if ((value = env.getConfigString(CONFIG_SEND_ONLY_PATTERNS)) != null) {
			for (String pattern : value.trim().split(BaseConstants.SCOLON)) {
				try {
					if ((pattern = pattern.trim()).isEmpty()) {
						continue;
					}
					props.getSendOnlyPatterns().put(pattern,  Pattern.compile(pattern));
				} catch (PatternSyntaxException e) {
					String message = new StringBuilder().append("setConfiguration method: set sendOnlyPatterns: pattern is '" + pattern + "', pattern syntax exception; ").append(e.getMessage()).toString();
					log.severe(message);	
					return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, message, message, e);
				}
			}
		}
		
		// set measureNamePatterns
		props.setMeasureNamePatterns(new HashMap<String, Pattern>());
		props.setMeasureNamePatternsExtended(new HashMap<String, Pattern>());
		if ((value = env.getConfigString(CONFIG_MEASURE_NAME_PATTERNS)) != null) {
			for (String pattern : value.trim().split(BaseConstants.SCOLON)) {
				try {
					if ((pattern = pattern.trim()).isEmpty()) {
						continue;
					}
					props.getMeasureNamePatterns().put(pattern,  Pattern.compile(pattern));
					// add extended pattern
					if (!pattern.startsWith(MATCHES_ALL)) {
						pattern = MATCHES_ALL + pattern;
					}
					if (!pattern.endsWith(MATCHES_ALL)) {
						pattern = pattern + MATCHES_ALL;
					}
					try {
						props.getMeasureNamePatternsExtended().put(pattern,  Pattern.compile(pattern));
					} catch (PatternSyntaxException e) {
						log.finer("isPatternMatches method: thrown PatternSyntaxException when building modified pattern '" + pattern + "'. Stacktrace is '" + HelperUtils.getExceptionAsString(e) + "'");
						props.getMeasureNamePatternsExtended().put(pattern, null);
					}
				} catch (PatternSyntaxException e) {
					String message = new StringBuilder().append("setConfiguration method: set measureNamePatterns: pattern is '" + pattern + "', pattern syntax exception; ").append(e.getMessage()).toString();
					log.severe(message);	
					return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, message, message, e);
				}
			}
		}
		
		// set thresholdsFile to manage violations for dynamic measures
		if ((value = env.getConfigString(CONFIG_THRESHOLDS_FILE)) != null && !value.trim().isEmpty()) {
			props.setThresholdsFile(value.trim());
		    if ((status = setThresholds(props)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
				log.severe("setConfiguration method: setThresholds method returned message '" + status.getMessage() + "'");
				return status;
			}
		} else {
			props.setThresholdsFile(EMPTY_STRING);
		}
		
		if (log.isLoggable(Level.FINER)) {
			StringBuilder sb = new StringBuilder(DEFAULT_STRING_LENGTH).append("setConfiguration method: Email Report Plugin properties:").append(ls)
					.append(" incidentRuleName is ").append(props.getIncidentRuleName() != null ? "'" + props.getIncidentRuleName() + "'" : "'null'").append(";").append(ls)
					.append(" identityString is ").append(props.getIdentityString() != null ? "'" + props.getIdentityString() + "'" : "'null'").append(";").append(ls)
					.append(" hostNameToIpAddressRaw is ").append(props.getHostNameToIpAddressRaw() != null ? "'" + props.getHostNameToIpAddressRaw() + "'" : "'null'").append(";").append(ls)
					.append(" hostNameToIpAddress is ").append(props.getHostNameToIpAddress() != null ? "'" + props.getHostNameToIpAddress().toString() + "'" : "'null'").append(";").append(ls)
					.append(" isEmailsFiltersCoupled is '").append(props.isEmailsFiltersCoupled()).append("';").append(ls)
					.append(" mailsFiltersDependencyFile is '").append(props.getEmailsFiltersDependencyFile() == null ? "-" : props.getEmailsFiltersDependencyFile()).append("';").append(ls)
					.append(" froms is ").append(props.getFroms() != null ? "'" + props.getFroms().toString() + "'" : "'null'").append(";").append(ls)
					.append(" EmailsFiltersCoupled indicator is ").append(props.isEmailsFiltersCoupled()).append(";").append(ls);
			if (!props.isEmailsFiltersCoupled()) {
				sb.append(" tos is ").append(props.getTos() != null ? "'" + Arrays.toString(props.getTos().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" cc is ").append(props.getCc() != null ? "'" + Arrays.toString(props.getCc().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" bcc is ").append(props.getBcc() != null ? "'" + Arrays.toString(props.getBcc().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" Agents are ").append(props.getAgents() != null ? "'" + Arrays.toString(props.getAgents().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" Agent Servers are ").append(props.getAgentServers() != null ? "'" + Arrays.toString(props.getAgentServers().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" AgentGroups are ").append(props.getAgentGroups() != null ? "'" + Arrays.toString(props.getAgentGroups().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" Monitors are ").append(props.getMonitors() != null ? "'" + Arrays.toString(props.getMonitors().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" Monitor Servers are ").append(props.getMonitorServers() != null ? "'" + Arrays.toString(props.getMonitorServers().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" Collectors are ").append(props.getCollectors() != null ? "'" + Arrays.toString(props.getCollectors().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" Collector Servers are ").append(props.getCollectorServers() != null ? "'" + Arrays.toString(props.getCollectorServers().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
					.append(" Hosts are ").append(props.getServers() != null ? "'" + Arrays.toString(props.getServers().keySet().toArray()) + "'" : "'null'").append(";").append(ls);
			} else {
				// add filters captured when EmailsFiltersCoupled is true
				List<Filter> filters = props.getFilters();
				int i = 0;
				for (Filter filter : filters) {
					sb.append(" filter #").append(i).append(";").append(ls)
						.append("   Agents are ").append(filter.getAgents() != null ? "'" + Arrays.toString(filter.getAgents().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
						.append("   AgentGroups are ").append(filter.getAgentGroups() != null ? "'" + Arrays.toString(filter.getAgentGroups().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
						.append("   Monitors are ").append(filter.getMonitors() != null ? "'" + Arrays.toString(filter.getMonitors().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
						.append("   Collectors are ").append(filter.getCollectors() != null ? "'" + Arrays.toString(filter.getCollectors().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
						.append("   Hosts are ").append(filter.getServers() != null ? "'" + Arrays.toString(filter.getServers().keySet().toArray()) + "'" : "'null'").append(";").append(ls)
						.append("   tos is ").append(filter.getTos() != null ? "'" + Arrays.toString(filter.getTos().toArray()) + "'" : "'null'").append(";").append(ls)
						.append("   cc is ").append(filter.getCc() != null ? "'" + Arrays.toString(filter.getCc().toArray()) + "'" : "'null'").append(";").append(ls)
						.append("   bcc is ").append(filter.getBcc() != null ? "'" + Arrays.toString(filter.getBcc().toArray()) + "'" : "'null'").append(";").append(ls);
					i++;
				}
			}
			// add the rest of parameters
			sb.append(" html format is ").append(props.isHtmlFormat()).append(";").append(ls)
					.append(" subject prefix is ").append(props.getSubjectPrefix() != null ? "'" + props.getSubjectPrefix() + "'" : "'null'").append(";").append(ls)
					.append(" subject is ").append(props.getSubject() != null ? "'" + props.getSubject() + "'" : "'null'").append(";").append(ls)
					.append(" subject suffix is ").append(props.getSubjectSuffix() != null ? "'" + props.getSubjectSuffix() + "'" : "'null'").append(";").append(ls)
					.append(" body header is ").append(props.getBodyHeader() != null ? "'" + props.getBodyHeader() + "'" : "'null'").append(";").append(ls)
					.append(" body is ").append(props.getBody() != null ? "'" + props.getBody() + "'" : "'null'").append(";").append(ls)
					.append(" body footer is ").append(props.getBodyFooter() != null ? "'" + props.getBodyFooter() + "'" : "'null'").append(";").append(ls)
					.append(" dashboards is ").append(props.getDashboards() != null ? "'" + Arrays.toString(props.getDashboards()) + "'" : "'null'").append(";").append(ls)
					.append(" dashboards type is ").append(props.getDashboardsType() != null ? "'" + props.getDashboardsType() + "'" : "'null'").append(";").append(ls)
					.append(" filter agent name/host is ").append(props.isFilterAgentNameHost() ? "true" : "false").append(";").append(ls)
					.append(" filter agent group is ").append(props.isFilterAgentGroup() ? "true" : "false").append(";").append(ls)
					.append(" filter custom timeframe is ").append(props.isFilterCustomTimeframe() ? "true" : "false").append(";").append(ls)
					.append(" mailHost is ").append(props.getSmtpHost() != null ? "'" + props.getSmtpHost() + "'" : "'null'").append(";").append(ls)
					.append(" mailPort is ").append(props.getSmtpPort() != null ? "'" + props.getSmtpPort() + "'" : "'null'").append(";").append(ls)
					.append(" quiet time from is ").append(props.getQuietTimeFrom()).append(";").append(ls)
					.append(" quiet time to is ").append(props.getQuietTimeFrom()).append(";").append(ls)
					.append(" smtp user is ").append(props.getSmtpUser() != null ? "'" + props.getSmtpUser() + "'" : "'null'").append(";").append(ls)
					.append(" smtp password is ").append(props.getSmtpPassword() != null ? "'" + props.getSmtpPassword() + "'" : "'null'").append(";").append(ls)
					.append(" smtp ssl is '").append(props.isSmtpSsl()).append("';").append(ls)
					.append(" dtHost is ").append("'" + props.getDtHost() + "'").append(";").append(ls)
					.append(" dtPort is ").append(props.getDtPort() != null ? "'" + props.getDtPort() + "'" : "'null'").append(";").append(ls)
					.append(" dtUser is ").append(props.getDtUser() != null ? "'" + props.getDtUser() + "'" : "'null'").append(";").append(ls)
					.append(" dtPassword is ").append(props.getDtPassword() != null ? "'" + props.getDtPassword() + "'" : "'null'").append(";").append(ls)
					.append(" sendOnlyPatterns is ").append(props.getSendOnlyPatterns() != null ? "'" + Arrays.toString(props.getSendOnlyPatterns().keySet().toArray()) + "'" : "'null'").append(";").append(ls);
			log.finer(sb.toString());
		}
		
		return STATUS_SUCCESS;
	}
    
    private Status setPatterns(String value, Map<String, Pattern> map) {
    	if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setPatterns method");
		}
		for (String pattern : value.trim().split(BaseConstants.SCOLON)) {
			try {
				if ((pattern = pattern.trim()).isEmpty()) {
					continue;
				}
				map.put(pattern,  Pattern.compile(pattern));
			} catch (PatternSyntaxException e) {
				String message = new StringBuilder().append("setPatterns method: pattern is '" + pattern + "', pattern syntax exception; ").append(e.getMessage()).toString();
				log.severe("setPatterns method: " + message);	
				return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, message, message, e);
			}
		}
		return STATUS_SUCCESS;
    }
    
    private Status setEmailsFiltersCouples(MailPluginProperties props) {
    	if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setEmailsFiltersCouples method");
		}
    	String fn = props.getEmailsFiltersDependencyFile();
   	 	try {
            if (!VALIDATE_XML_WITH_SCHEMA) {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				XMLReader reader = saxParser.getXMLReader();
				reader.setContentHandler(new SAXHandler(props.getFilters()));
				reader.setErrorHandler(new SimpleErrorHandler());
				reader.parse(new InputSource(fn));
			} else {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setNamespaceAware(true);
				factory.setValidating(true); 
				SAXParser saxParser = factory.newSAXParser();
				saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
				String destDir = FilenameUtils.getFullPath(fn) + FILTERS_XSD_SCHEMA;
				copyFile("res/" + FILTERS_XSD_SCHEMA, destDir);
				saxParser.setProperty(JAXP_SCHEMA_SOURCE, new File(destDir)); 
				SAXHandler handler = new SAXHandler(props.getFilters());
				XMLReader reader = saxParser.getXMLReader();
				reader.setContentHandler(handler);
				reader.setErrorHandler(new SimpleErrorHandler());
				reader.parse(new InputSource(fn));
			}
        } catch (ParserConfigurationException e) {
            String msg = "setEmailsFiltersCouples method: the ParserConfigurationException occurred during parsing the '" + fn + "' file - '" + HelperUtils.getExceptionAsString(e) + "'";
            log.severe(msg);
            return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
        } catch (SAXException e) {
       	 	String msg = "setEmailsFiltersCouples method: the SAXException occurred during parsing the '" + fn + "' file - '" + HelperUtils.getExceptionAsString(e) + "'";
            log.severe(msg);
            return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
        } catch (IOException e) {
       	 	String msg = "setEmailsFiltersCouples method: the IOException occurred during parsing the '" + fn + "' file - '" + HelperUtils.getExceptionAsString(e) + "'";
            log.severe(msg);
            return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
        } catch (RuntimeException e) {
        	String msg = "setEmailsFiltersCouples method: the RuntimeException occurred during parsing the '" + fn + "' file - '" + HelperUtils.getExceptionAsString(e) + "'";
            log.severe(msg);
            return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
        }
   	 
   	 	return STATUS_SUCCESS;
    }
    
    private Status setThresholds(MailPluginProperties props) {
    	if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setThresholds method");
		}
    	Map<String, Threshold> thresholds = new HashMap<String, Threshold>();
    	Set<String> metricNames = new HashSet<String>();
    	props.setThresholds(thresholds);
    	props.setMetricNames(metricNames);
    	String fn = props.getThresholdsFile();
   	 	try {
	   	 	if (!VALIDATE_XML_WITH_SCHEMA) {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				XMLReader reader = saxParser.getXMLReader();
				reader.setContentHandler(new SAXHandlerThresholds(thresholds, metricNames));
				reader.setErrorHandler(new SimpleErrorHandler());
				reader.parse(new InputSource(fn));
			} else {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setNamespaceAware(true);
				factory.setValidating(true); 
				SAXParser saxParser = factory.newSAXParser();
				saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
				String destDir = FilenameUtils.getFullPath(fn) + THRESHOLDS_XSD_SCHEMA;
				copyFile("res/" + THRESHOLDS_XSD_SCHEMA, destDir);
				saxParser.setProperty(JAXP_SCHEMA_SOURCE, new File(destDir)); 
				SAXHandlerThresholds handler = new SAXHandlerThresholds(thresholds, metricNames);
				XMLReader reader = saxParser.getXMLReader();
				reader.setContentHandler(handler);
				reader.setErrorHandler(new SimpleErrorHandler());
				reader.parse(new InputSource(fn));
			}
        } catch (ParserConfigurationException e) {
            String msg = "setThresholds method: the ParserConfigurationException occurred during parsing the '" + fn + "' file - '" + HelperUtils.getExceptionAsString(e) + "'";
            log.severe(msg);
            return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
        } catch (SAXException e) {
       	 	String msg = "setThresholds method: the SAXException occurred during parsing the '" + fn + "' file - '" + HelperUtils.getExceptionAsString(e) + "'";
            log.severe(msg);
            return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
        } catch (IOException e) {
       	 	String msg = "setThresholds method: the IOException occurred during parsing the '" + fn + "' file - '" + HelperUtils.getExceptionAsString(e) + "'";
            log.severe(msg);
            return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
        } catch (RuntimeException e) {
        	String msg = "setThresholds method: the RuntimeException occurred during parsing the '" + fn + "' file - '" + HelperUtils.getExceptionAsString(e) + "'";
            log.severe(msg);
            return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, msg, msg, e);
        }
   	 
   	 	return STATUS_SUCCESS;
    }
    
    public void copyFile(String srcFileName, String destFileName) throws IOException {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Entering copyFile method: source fileName is '" + srcFileName + "', destination file is '" + destFileName + "'");
		}
		URL url = this.getClass().getClassLoader().getResource(srcFileName);
		FileUtils.copyURLToFile(url, new File(destFileName));
	}

	public static List<Address> parseEmails(String[] emails) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering parseEmails method");
		}

		if (emails == null || emails.length == 0) {
			return null;
		}

		List<Address> addresses = new ArrayList<Address>();
		for (String email : emails) {
			if (email.trim().isEmpty()) {
				continue;
			}
			
			try {
				addresses.add(new InternetAddress(email));
			} catch (AddressException e) {
				log.severe("parseEmails method: The following e-mail address is incorrect '" + email + "': " + e.getMessage());
				throw new RuntimeException("The following e-mail address is incorrect '" + email + "': " + e.getMessage(), e);
			}
		}

		return addresses;

	}
	
	public static String getImageAsString(URL url) throws IOException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getImageAsString method");
		}
		// get images for HTML img tag as data image png file base64 encoded. Examples, "res/header.png", "res/footer.png"
//		return new String(Base64.encodeBase64(IOUtils.toByteArray(this.getClass().getClassLoader().getResource(fileName))));
		return new String(Base64.encodeBase64(IOUtils.toByteArray(url)), Charset.defaultCharset());

	}
    
    public static class StringEntry implements Map.Entry<String, String> {
		String key;
		String value;

		StringEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public String setValue(String value) {
			throw new UnsupportedOperationException("setValue"); //$NON-NLS-1$
		}
	}
    
    public static int getTimeMinutes(String parm) {
    	if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getTimeMinutes method");
		}
		//check format
    	if (parm.isEmpty()) {
    		return -1;
    	}
		// parm field should have format "HH:mm"
		String[] as = parm.split(":");
		if (as.length != 2) {
			String msg = String.format(WRONG_FORMAT_OF_QUIET_FIELD, parm, as.length);
			log.severe("getTimeMinutes method: " + msg);
			throw new RuntimeException(msg);
		}
		// check hours part of the parm field
		int hours;
		try {
			hours = Integer.parseInt(as[0]);
			if (hours < 0 || hours > 24 ) {
				String msg = String.format(WRONG_NUMBER, "hours", hours, parm);
				log.severe("getTimeMinutes method: " + msg);
				throw new RuntimeException(msg);
			}
		} catch (NumberFormatException e) {
			String msg = String.format(WRONG_FORMAT, "hours", parm);
			log.severe("getTimeMinutes method: " + msg);
			throw new RuntimeException(msg);
		}
		
		// check minutes field
		int mm;
		try {
			mm = Integer.parseInt(as[1]);
			if (mm < 0 || mm > 59 ) {
				String msg = String.format(WRONG_NUMBER, "minutes", mm, parm);
				log.severe("getTimeMinutes method: " + msg);
				throw new RuntimeException(msg);
			}
		} catch (NumberFormatException e) {
			String msg = String.format(WRONG_FORMAT, "minutes", parm);
			log.severe("getTimeMinutes method: " + msg);
			throw new RuntimeException(msg);
		}
		
		// convert parm into number of minutes and return it
		return 60*hours + mm;
	}
	
	public static ActionData getActionData(MailPluginProperties pp, Map<SourceType, SourceReferences> sources) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getActionData method");
		}
		ActionData action = new ActionData();
		action.setSources(sources);
		action.setDtPort(pp.getDtPort());
		action.setDashboards(pp.getDashboards());
		action.setDashboardUrls(pp.getDashboardUrls());
		action.setFilterAgentNameHost(pp.isFilterAgentNameHost());
		action.setFilterAgentGroup(pp.isFilterAgentGroup());
		action.setFilterCustomTimeframe(pp.isFilterCustomTimeframe());
		action.setHostNameToIpAddress(pp.getHostNameToIpAddress());
		
		return action;
	}
	
	public static void setActionData(ActionRecord record, ActionData ad, Map<String, String> substituterMap) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setActionData method");
		}
		record.setDashboards(ad.getDashboards());
		record.setDashboardUrls(ad.getDashboardUrls());
		record.setIncidents(ad.getIncidents());
		record.setStartTime(ad.getStartTime());
		record.setEndTime(ad.getEndTime());	
		record.setUrlRestFilteringMap(ad.getUrlRestFilteringMap());
		record.setApplication(substituterMap.get(SubstituterFields.APPLICATION.name()));
	}
	
	public static List<String> getImagesFromExcelReport(Workbook wb, MailPluginProperties props) throws IOException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getImagesFromExcelReport method");
		}
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet s = wb.getSheet(i);
			for (int j = 0; j < s.getNumberOfImages(); j++) {
				Image c = s.getDrawing(j);
				list.add(new StringBuilder(PREPEND_EMBEDDED_IMAGE_SRC).append(new String(Base64.encodeBase64(c.getImageData()), Charset.defaultCharset())).toString());
			}
		}
		
		// add footer images which Excel does not have
		list.add(new StringBuilder(PREPEND_EMBEDDED_IMAGE_SRC).append(getImageAsString(props.getFooterUrl())).toString());

		return list; 
	}
	
	public static String replaceImagesIntoHtmlPage(List<String> list, String page) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering replaceImagesIntoHtmlPage method");
		}
		Document doc = Jsoup.parse(page);
        Elements srces = doc.select("[src]");

        int i = 0;
        for (Element src : srces) {
            if (src.tagName().equals("img")) {
            	if (i >=list.size()) {
            		log.warning("replaceImagesIntoHtmlPage method: image '" + (i + 1) + "' on the HTML page exceeds size '" + list.size() + "' of extracted images from the Excel spreadsheet.");
            		continue;
            	}
            	src.attr("src", list.get(i++));
            }
            else {
            	// skip non-img tags where src attribute is present
            	continue;
            }
        }
//        if (log.isLoggable(Level.FINER)) {
//             log.finer("replaceImagesIntoHtmlPage method: modified page is '" + doc.html() + "'");
//		}
       
        return doc.html();
	}
	
	public static File getNewHtmlPageFile(String page, String dashboard) throws IOException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getNewHtmlPageFile method");
		}
		File file = File.createTempFile(new StringBuilder(dashboard).append(HTML_FILE_UPDATED).toString(), HTML_FILE_SUFFIX);
		file.deleteOnExit();
		if (log.isLoggable(Level.FINER)) {
			log.finer("getNewHtmlPageFile method: canonical path to file is '" + file.getCanonicalPath() + "'");
//			log.finer("getNewHtmlPageFile method: page is '" + page + "'");
		}
		FileUtils.writeStringToFile(file, page);
		return file;
	}
	
	public static String[] trimArray(String[] array) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering trimArray method");
		}
		if (array != null) {
			List<String> list = new ArrayList<String>();
			for (String element : array) {
				if ((element = element.trim()).isEmpty()) {
					continue;
				} else {
					list.add(element);
				}
			}
			return list.toArray(new String[list.size()]);
		} else {
			return null;
		}
	}
	
	public static boolean isHttpURLConnection(URL url) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering isHttpURLConnection method");
			log.finer("isHttpURLConnection method: protocol is '" + url.getProtocol() + "'");
		}
		return url.getProtocol().toUpperCase().startsWith("HTTP");
	}
	
	public static int getResponseCode(URLConnection urlConnection) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getResponseCode method");
		}
		if (!(urlConnection instanceof HttpURLConnection)) {
			return -1;
		}
		
		try {
			if (log.isLoggable(Level.FINER)) {
				log.finer("getResponseCode method: response code is '" + ((HttpURLConnection)urlConnection).getResponseCode() + "'");
			}
			return ((HttpURLConnection)urlConnection).getResponseCode();
		} catch (IOException e) {
			log.severe("getResponseCode method: '" + e.getMessage() + "'");
			throw new ReportCreationException(e.getMessage(), e);
		}
	}
	
	public static String getResponseMessage(URLConnection urlConnection) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getResponseMessage method");
		}
		if (!(urlConnection instanceof HttpURLConnection)) {
			return EMPTY_STRING;
		}
		
		try {
			if (log.isLoggable(Level.FINER)) {
				log.finer("getResponseMessage method: response message is '" + ((HttpURLConnection)urlConnection).getResponseMessage() + "'");
			}
			return ((HttpURLConnection)urlConnection).getResponseMessage();
		} catch (IOException e) {
			log.severe("getResponseMessage method: '" + e.getMessage() + "'");
			throw new ReportCreationException(e.getMessage(), e);
		}
	}
}
