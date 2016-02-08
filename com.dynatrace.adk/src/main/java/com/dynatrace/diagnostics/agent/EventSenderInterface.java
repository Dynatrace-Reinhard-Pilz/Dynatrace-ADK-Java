package com.dynatrace.diagnostics.agent;

public interface EventSenderInterface {

	void addInsertCustomLinkEvent(TraceTag t, int vicl, byte si, boolean a);

	void addStartLinkedRootPathEvent(TraceTag t, byte si);

	void addStartPathEvent(TraceTag t, byte si);

}
