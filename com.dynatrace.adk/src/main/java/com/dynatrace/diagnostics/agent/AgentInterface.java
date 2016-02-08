package com.dynatrace.diagnostics.agent;

public interface AgentInterface {

	boolean isCaptureAndLicenseOk();

	EventSenderInterface getEventSender();
	
}
