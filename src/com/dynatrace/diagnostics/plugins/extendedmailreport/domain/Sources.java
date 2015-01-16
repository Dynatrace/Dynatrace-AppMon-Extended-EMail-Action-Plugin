package com.dynatrace.diagnostics.plugins.extendedmailreport.domain;

public class Sources {
	private AgentFilter agentFilter;
	private AgentGroupFilter agentGroupFilter;
	private MonitorFilter monitorFilter;
	private CollectorFilter collectorFilter;
	private ServerFilter serverFilter;
	
	public AgentFilter getAgentFilter() {
		return agentFilter;
	}
	public void setAgentFilter(AgentFilter agentFilter) {
		this.agentFilter = agentFilter;
	}
	public AgentGroupFilter getAgentGroupFilter() {
		return agentGroupFilter;
	}
	public void setAgentGroupFilter(AgentGroupFilter agentGroupFilter) {
		this.agentGroupFilter = agentGroupFilter;
	}
	public MonitorFilter getMonitorFilter() {
		return monitorFilter;
	}
	public void setMonitorFilter(MonitorFilter monitorFilter) {
		this.monitorFilter = monitorFilter;
	}
	public CollectorFilter getCollectorFilter() {
		return collectorFilter;
	}
	public void setCollectorFilter(CollectorFilter collectorFilter) {
		this.collectorFilter = collectorFilter;
	}
	public ServerFilter getServerFilter() {
		return serverFilter;
	}
	public void setServerFilter(ServerFilter serverFilter) {
		this.serverFilter = serverFilter;
	}
	
}
