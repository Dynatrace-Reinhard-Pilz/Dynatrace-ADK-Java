/**
 *  Copyright (c) Dynatrace 2001-2016
 */
package com.dynatrace.adk.impl;

import com.dynatrace.adk.Tagging;

/**
 * @see {@link Tagging}
 * @author ardeshir.arfaian, rainer.klaffenboeck
 */
public class DummyTaggingImpl implements Tagging {

	// copy of TraceTag.NOT_TAG to prevent a dependency from this class to the agent
	private static final byte[] NOT_TAG = new byte[] {
			1, // TraceTag.TRACETAG_VERSION
			30, // TraceTag.TRACETAG_SIZE
			-1, -1, -1, -1,
			-1, -1, -1, -1,
			-1, -1, -1, -1,
			-1, -1, -1, -1,
			-1, -1, -1, -1,
			-1, -1, -1, -1,
			-1, -1, -1, -1
	};

	// copy of TraceTag.NOT_TAG_STR to prevent a dependency from this class to the agent
	private static final String NOT_TAG_STR = "FW1;-1;-1;-1;-1;-1;-1;-1";

	public byte[] getTag() {
		return NOT_TAG;
	}

	public String getTagAsString() {
		return NOT_TAG_STR;
	}

	public void setTag(byte[] tag) {
	}

	public void setTagFromString(String tag) {
	}

	public boolean isTagValid(Object tag) {
		return false;
	}

	public void linkClientPurePath(boolean asynchronous) {
	}

	public void startServerPurePath() {
	}

	public void endServerPurePath() {
	}

	public Runnable createServerPathRunnable(Runnable runnable) {
		return runnable;
	}

	public String convertTagToString(byte[] tag) {
		return NOT_TAG_STR;
	}

	public byte[] convertStringToTag(String tag) {
		return NOT_TAG;
	}

	public void linkClientPurePath(boolean asynchronous, Object tag) {
	}

	public void setCustomTag(byte[] customTag) {
	}

	/**
	 * @deprecated
	 */
	public CustomTag createCustomTag(byte[] tagData, byte[] prevTagData) {
		return null;
	}

	public CustomTag createCustomTag(byte[] tagData) {
		return null;
	}
}
