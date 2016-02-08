package com.dynatrace.diagnostics.agent;

public class TraceTag {

	public static final byte[] NOT_TAG = new byte[0];

	public static final String NOT_TAG_STR = null;

	public static final int TRACETAG_VERSION = 0;
	public static final int TRACETAG_SIZE = 0;

	public static final int PATH_IS_BLOCK = 0;
	public final PathState pathState = new PathState();

	public int ignoredSubPathCount = 0;
	
	public boolean isTag() {
		return false;
	}

	public void enter() {
	}

	public void writeTraceTag(byte[] tag) {
	}

	public String asString() {
		return null;
	}

	public TraceTag getTemporaryTraceTag() {
		return null;
	}

	public void readTraceTag(byte[] tag) {
	}

	public void applyRemoteTag(TraceTag remoteTag) {
	}

	public void setCustomTag(Object object) {
	}

	public void setPrevCustomTag(Object object) {
	}

	public void readTraceTag(String tag) {
	}

	public boolean beginIntrospection() {
		return false;
	}

	public void insertPlainLink(byte sensorIdSdk, boolean b) {
	}

	public void endIntrospection() {
	}

	public boolean hasPredecessor() {
		return false;
	}

	public void initTagValues() {
	}

	public byte[] getCustomTag() {
		return null;
	}

	public boolean isOnSubPath() {
		return false;
	}

	public static String convertTagToString(byte[] tag) {
		return null;
	}

	public static byte[] convertStringToTag(String tag) {
		return null;
	}

	public static String toHex(byte[] tagData) {
		return null;
	}

	public void clearTag(boolean b) {
	}

	public static class PathState {

		public boolean startLinkedRootPath = false;
		public int modePathBlocked = 0;
		public int entryAgentId = 0;
		public int entryTagId = 0;
		public int agentId = 0;
		public int tagId = 0;
		public int prevAgentId = 0;
		public int prevTagId = 0;
		public int prevTagHopCount = 0;
		
	}
}
