/**
 *  Copyright (c) Dynatrace 2001-2016
 */
package com.dynatrace.adk.impl;

import com.dynatrace.adk.Tagging;
import com.dynatrace.diagnostics.agent.Agent;
import com.dynatrace.diagnostics.agent.DebugFlags;
import com.dynatrace.diagnostics.agent.Logger;
import com.dynatrace.diagnostics.agent.ThreadLocalTag;
import com.dynatrace.diagnostics.agent.TraceTag;
import com.dynatrace.diagnostics.agent.introspection.Introspection;
import com.dynatrace.diagnostics.agent.shared.Constants;

/**
 * @see {@link Tagging}
 * @author ardeshir.arfaian, rainer.klaffenboeck
 */
public class TaggingImpl implements Tagging {

	public byte[] getTag() {
		TraceTag traceTag = ThreadLocalTag.getTraceTag();
		if (traceTag == null || !traceTag.isTag()) {
			logDebug("getTag", traceTag, null);
			return TraceTag.NOT_TAG;
		}
		byte[] tag = new byte[TraceTag.TRACETAG_SIZE];
		traceTag.enter();
		traceTag.writeTraceTag(tag);
		logDebug("getTag", traceTag, null);
		return tag;
	}

	public String getTagAsString() {
		TraceTag traceTag = ThreadLocalTag.getTraceTag();
		if (traceTag == null || !traceTag.isTag()) {
			logDebug("getTagAsString", traceTag, null);
			return "";
		}
		traceTag.enter();
		logDebug("getTagAsString", traceTag, null);
		return traceTag.asString();
	}

	public void setTag(byte[] tag) {
		TraceTag traceTag = ThreadLocalTag.getOrCreateTraceTag();
		if (traceTag.isTag()){
			logDebug("setTag: localTag already set", traceTag, null);
			return;
		}
		TraceTag remoteTag = traceTag.getTemporaryTraceTag();
		remoteTag.readTraceTag(tag);
		if (!remoteTag.isTag()){
			logDebug("setTag: remoteTag is invalid", remoteTag, null);
			return;
		}
		// reset startLinkedRootPath flag, it's not touched in applyRemoteTag
		traceTag.pathState.startLinkedRootPath = false;
		traceTag.applyRemoteTag(remoteTag);
		traceTag.setCustomTag(null);
		traceTag.setPrevCustomTag(null);
		logDebug("setTag", traceTag, null);
	}

	public void setTagFromString(String tag) {
		TraceTag traceTag = ThreadLocalTag.getOrCreateTraceTag();
		if (traceTag.isTag()) {
			logDebug("setTagFromString: localTag already set", traceTag, null);
			return;
		}
		TraceTag remoteTag = traceTag.getTemporaryTraceTag();
		remoteTag.readTraceTag(tag);
		if (!remoteTag.isTag()){
			logDebug("setTag: remoteTag is invalid", remoteTag, null);
			return;
		}
		// reset startLinkedRootPath flag prior to applyRemoteTag (where it is set only if present)
		traceTag.pathState.startLinkedRootPath = false;
		traceTag.applyRemoteTag(remoteTag);
		traceTag.setCustomTag(null);
		traceTag.setPrevCustomTag(null);
		logDebug("setTagFromString", traceTag, null);
	}

	public boolean isTagValid(Object tagObject) {
		byte[] tag = null;
		if (tagObject instanceof byte[]) {
			tag = (byte[])tagObject;
		}
		else if (tagObject instanceof String){
			tag = convertStringToTag((String)tagObject);
		}
		if (tag != null) {
			TraceTag traceTag = new TraceTag();
			traceTag.readTraceTag(tag);
			return traceTag.isTag();
		}
		return false;
	}

	public void linkClientPurePath(boolean asynchronous) {
		linkClientPurePath(asynchronous, null);
	}

	public void linkClientPurePath(boolean asynchronous, Object specificTag) {
		if (!mayCapture()) {
			logDebug("linkClientPurePath: capturing disabled", null, null);
			return;
		}

		// JLT-63777: Avoid dead links, if the counterpart agent is disabled
		if (DebugFlags.debugDisableADKLinksJava) {
			if (DebugFlags.debugPathCorrelationJava) {
				Logger.getInstance().log(Constants.LOGLEVEL_DEBUG, "Tagging.linkClientPurePath: Ignoring ADK link event");
			}
			return;
		}
		
		TraceTag traceTag = Introspection.checkTag();
		if (traceTag == null) {
			logDebug("linkClientPurePath: checkTag failed", traceTag, null);
			return;
		}

		if (!traceTag.isTag()) {
			logDebug("linkClientPurePath: traceTag is invalid", traceTag, null);
			return;
		}

		try {
			if (!traceTag.beginIntrospection()) {
				logDebug("linkClientPurePath: beginIntrospecation failed", traceTag, null);
				return;
			}

			boolean isTagByteArray = false;
			boolean isTagString = false;
			boolean isCustomTag = false;
			if (specificTag != null) {
				if (specificTag instanceof byte[]) isTagByteArray = true;
				else if (specificTag instanceof String) isTagString = true;
				else if (specificTag instanceof CustomTag) isCustomTag = true;
				else {
					// got an unknown tag?
					Logger.getInstance().log(Constants.LOGLEVEL_SEVERE,
							"Tagging.linkClientPurePath failed: got unknown tag information");
					return;
				}
			}

			// no tag specified or is it a specific dynaTrace tag?
			if (specificTag == null || isTagByteArray || isTagString) {
				TraceTag linkTag = traceTag;
				if (isTagByteArray) {
					linkTag = new TraceTag();
					linkTag.readTraceTag((byte[])specificTag);
				}
				else if (isTagString) {
					linkTag = new TraceTag();
					linkTag.readTraceTag(convertStringToTag((String)specificTag));
				}
				logDebug("linkClientPurePath", linkTag, null);
				linkTag.insertPlainLink(Constants.SENSOR_ID_SDK, !asynchronous);
				return;
			}
			// got a custom tag?
			if (isCustomTag) {
				CustomTag customTag = (CustomTag)specificTag;
				traceTag.setCustomTag(customTag.getTag());
				logDebug("linkClientPurePath", traceTag, customTag);
				Agent.getInstance().getEventSender().addInsertCustomLinkEvent(traceTag,
						Constants.EVENT_TYPE_INSERT_CUSTOMTAGGED_LINK, Constants.SENSOR_ID_SDK, asynchronous);
				return;
			}
			Logger.getInstance().log(Constants.LOGLEVEL_WARNING, "Tagging.linkClientPurePath failed: got unknown tag information");
		}
		finally {
			traceTag.endIntrospection();
		}
	}

	public void startServerPurePath() {
		if (!mayCapture()) {
			logDebug("startServerPurePath: capturing disabled", null, null);
			return;
		}
		TraceTag traceTag = Introspection.checkTag();
		if (traceTag == null) {
			logDebug("startServerPurePath: checkTag failed", traceTag, null);
			return;
		}
		if (traceTag.isTag()) {
			traceTag.ignoredSubPathCount++;
			logDebug("startServerPurePath: Ignoring this event, path is started yet", traceTag, null);
			return;
		}
		if (!traceTag.hasPredecessor()) {
			logDebug("startServerPurePath: Skipping this event, got tag without predecessor", traceTag, null);
			return;
		}

		traceTag.initTagValues();
		try {
			if (!traceTag.beginIntrospection()) {
				logDebug("startServerPurePath: beginIntrospection failed", traceTag, null);
				return;
			}
			if ((traceTag.pathState.modePathBlocked & TraceTag.PATH_IS_BLOCK) != TraceTag.PATH_IS_BLOCK) {
				if (DebugFlags.debugPathCorrelationJava) {
					logDebug("startServerPurePath", traceTag, new CustomTagImpl(traceTag.getCustomTag()));
				}
				// native browser agents (ie, firefox) set the startLinkedRootPath to true
				// this means, that instead of starting a subpath, we start a root path and link it
				// to the parent path. we end up with 2 rootpaths (1 starting in the browser, 1 starting in the server)
				// similar code is in the servlet sensor (ServletIntrospection.MethodEnterHttpServlet)
				if(traceTag.pathState.startLinkedRootPath) {
					Agent.getInstance().getEventSender().addStartLinkedRootPathEvent(traceTag, Constants.SENSOR_ID_SDK);
					// rest of the events should be linked to server side path (since it's the new root path now)
					traceTag.pathState.entryAgentId = traceTag.pathState.agentId;
					traceTag.pathState.entryTagId = traceTag.pathState.tagId;
					traceTag.pathState.prevAgentId = -1;
					traceTag.pathState.prevTagId = Constants.INVALID_TAG_ID;
					traceTag.pathState.prevTagHopCount = -1;
					// but *keep* the customTag/prevCustomTag information (indicator for 'isOnSubPath' - which is still required though)
				} else {
					Agent.getInstance().getEventSender().addStartPathEvent(traceTag, Constants.SENSOR_ID_SDK);
				}
				traceTag.pathState.startLinkedRootPath = false;
			}
		} finally {
			traceTag.endIntrospection();
		}
	}

	public void endServerPurePath() {
		if (!mayCapture()) {
			logDebug("endServerPurePath: capturing disabled", null, null);
			return;
		}

		TraceTag traceTag = Introspection.checkTag();
		if (traceTag == null) {
			logDebug("endServerPurePath: checkTag failed", traceTag, null);
			return;
		}

		try {
			if (!traceTag.beginIntrospection()) {
				logDebug("endServerPurePath: beginIntrospecation failed", traceTag, null);
				return;
			}
			if (traceTag.isTag() && traceTag.isOnSubPath()) {
				if (traceTag.ignoredSubPathCount > 0) {
					traceTag.ignoredSubPathCount--;
					logDebug("endServerPurePath: Ignoring this event, subpath started yet", traceTag, null);
					return;
				}
				logDebug("endServerPurePath", traceTag, null);
				traceTag.clearTag(true);
			}
		} finally {
			traceTag.endIntrospection();
		}
	}

	private boolean mayCapture() {
		return Agent.getInstance().isCaptureAndLicenseOk();
	}

    public Runnable createServerPathRunnable(Runnable runnable) {
        byte[] tag = getTag();
        return new TaggedRunnable(this, runnable, tag);
    }

	public String convertTagToString(byte[] tag) {
		return TraceTag.convertTagToString(tag);
	}

	public byte[] convertStringToTag(String tag) {
		return TraceTag.convertStringToTag(tag);
	}

	private static void logDebug(String text, TraceTag tag, CustomTag customTag) {
		if (DebugFlags.debugPathCorrelationJava) {
			String logMessage = "Tagging." + text + ": tag=" + (tag == null ? "<null>" : tag.asString());
//					+ ", entryAgentId: " + tag.pathState.getEntryAgentIdString()
//					+ ", entryTagId: "   + tag.pathState.entryTagId
//					+ ", prevAgentId: "  + tag.pathState.getPrevAgentIdString()
//					+ ", prevTagId: "    + tag.pathState.prevTagId
//					+ ", prevHopCount: " + tag.pathState.prevTagHopCount;
			if (customTag != null) {
				logMessage += ", customTag=" + customTag.asString();
			}
			Logger.getInstance().log(Constants.LOGLEVEL_DEBUG, logMessage);
		}
	}

	/**
	 * Custom Tagging
	 */
    public class CustomTagImpl implements CustomTag {
    	private byte[] tagData = null;

    	CustomTagImpl(byte[] customTag) {
    		tagData = customTag;
    	}
    	public byte[] getTag() {
    		return tagData;
    	}
		/**
		 * @deprecated
		 */
		public byte[] getPrevTag() {
			return null;
		}
    	public String asString() {
    		String tagString = tagData != null ? TraceTag.toHex(tagData) : "<null>";
    		return tagString;
    	}
    }

	/**
	 * @deprecated
	 */
	public CustomTag createCustomTag(byte[] tagData, byte[] prevTagData) {
		CustomTag ct = new CustomTagImpl(tagData);
		if (DebugFlags.debugPathCorrelationJava) {
			Logger.getInstance().log(Constants.LOGLEVEL_DEBUG, "Tagging.createCustomTag: tag=" + ct.asString());
		}
		return ct;
	}

	public CustomTag createCustomTag(byte[] tagData) {
		CustomTag ct = new CustomTagImpl(tagData);
		if (DebugFlags.debugPathCorrelationJava) {
			Logger.getInstance().log(Constants.LOGLEVEL_DEBUG, "Tagging.createCustomTag: tag=" + ct.asString());
		}
		return ct;
	}

	public void setCustomTag(byte[] customTag) {
		TraceTag traceTag = ThreadLocalTag.getOrCreateTraceTag();
		if (traceTag.isTag()) {
			logDebug("setCustomTag: localTag already set", traceTag, null);
			return;
		}
		// Note: the 'prevCustomTag' is used for the 'inbound' tag, then being sent by EventSender.addStartLinkedRootPathEvent
		traceTag.setCustomTag(null);
		traceTag.setPrevCustomTag(customTag);
		// always start a linked rootPath in case of custom tagging
		traceTag.pathState.startLinkedRootPath = true;
		if (DebugFlags.debugPathCorrelationJava) {
			logDebug("setCustomTag", traceTag, new CustomTagImpl(customTag));
		}
	}

}
