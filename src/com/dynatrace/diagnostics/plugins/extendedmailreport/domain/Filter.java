package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.mail.Address;

public class Filter {
	private Map<String, Pattern> agents;
	private Map<String, Pattern> agentServers;
	private Map<String, Pattern> agentGroups;
	private Map<String, Pattern> monitors;
	private Map<String, Pattern> monitorServers;
	private Map<String, Pattern> collectors;
	private Map<String, Pattern> collectorServers;
	private Map<String, Pattern> servers;
	private List<Address> tos;
	private List<Address> cc;
	private List<Address> bcc;
	
	public Map<String, Pattern> getAgents() {
		return agents;
	}
	public void setAgents(Map<String, Pattern> agents) {
		this.agents = agents;
	}
	public Map<String, Pattern> getAgentServers() {
		return agentServers;
	}
	public void setAgentServers(Map<String, Pattern> agentServers) {
		this.agentServers = agentServers;
	}
	public Map<String, Pattern> getAgentGroups() {
		return agentGroups;
	}
	public void setAgentGroups(Map<String, Pattern> agentGroups) {
		this.agentGroups = agentGroups;
	}
	public Map<String, Pattern> getMonitors() {
		return monitors;
	}
	public void setMonitors(Map<String, Pattern> monitors) {
		this.monitors = monitors;
	}
	public Map<String, Pattern> getMonitorServers() {
		return monitorServers;
	}
	public void setMonitorServers(Map<String, Pattern> monitorServers) {
		this.monitorServers = monitorServers;
	}
	public Map<String, Pattern> getCollectors() {
		return collectors;
	}
	public void setCollectors(Map<String, Pattern> collectors) {
		this.collectors = collectors;
	}
	public Map<String, Pattern> getCollectorServers() {
		return collectorServers;
	}
	public void setCollectorServers(Map<String, Pattern> collectorServers) {
		this.collectorServers = collectorServers;
	}
	public Map<String, Pattern> getServers() {
		return servers;
	}
	public void setServers(Map<String, Pattern> hosts) {
		this.servers = hosts;
	}
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
