/**
 *  Copyright (c) Dynatrace 2001-2016
 */
package com.dynatrace.adk.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dynatrace.adk.DynaTraceADKFactory;
import com.dynatrace.adk.Tagging;
import com.dynatrace.diagnostics.agent.AgentNative;

/**
 * @see {@link DynaTraceADKFactory}
 * @author ardeshir.arfaian, rainer.klaffenboeck
 */
public class DynaTraceADKFactoryImpl {
	// Please note: we leave this sysproperty intact as the only exception to agent options, cf JLT-95251
	private static final boolean debug = Boolean.valueOf(System.getProperty("com.dynatrace.adk.debug")).booleanValue();
	private static final String NATIVEAGENTPROPERTY_AGENT_VERSION = "agent.version";

	/// instance of a tagging implementation
	private static Tagging tagging;

	/// current ADK requires Agent 5.x
	private static final int minAgentVersion = 50000;
	private static final String agentVersionError = "This version of Tagging ADK requires at least dynaTrace Agent 5";

	// for debugging/logging...
	private static final int LOGLEVEL_WARNING = 5;
	private static final int LOGLEVEL_DEBUG   = 7;
	private static final String logPrefix = " [adk   ] ";

	/// Exceptions on ADK setup (during creating/connecting to dynaTrace Agent)
	private static class ADKException extends Exception {
		private static final long serialVersionUID = 1L;
		/// c'tor
		public ADKException(String message) {
			super(message);
		}
	}

	/**
	 *	Get/create tagging stub
	 *	@return tagging instance
	 */
	public static final synchronized Tagging createTagging() {
		return tagging;
	}

	/**
	 * Initialize tagging, check minimum version requirements
	 */
	public static synchronized void initialize() {
		if (tagging != null)
			return;

		log(LOGLEVEL_DEBUG, "Initializing " + DynaTraceADKFactory.class.getName());

		if (isAgentCompatible()) {
			try {
			    tagging = new TaggingImpl();
			}
			catch (Throwable t) {
				tagging = null;
				log(LOGLEVEL_WARNING, "Failed to initialize dynaTrace Tagging library: " + t.toString());
			}
		}
		if (tagging == null) {
			tagging = new DummyTaggingImpl();
		}
		log(LOGLEVEL_DEBUG, "Returning instance of " + tagging.getClass().toString());
	}

	/**
	 *	Free tagging library
	 */
	public static synchronized void uninitialize() {
		log(LOGLEVEL_DEBUG, "Uninitializing " + DynaTraceADKFactory.class.getName());
		tagging = null;
	}

	private static boolean isAgentCompatible() {
		try {
			String agentVersion = AgentNative.getInstance().getNativeProperty(NATIVEAGENTPROPERTY_AGENT_VERSION);
			if (agentVersion == null) {
				throw new ADKException("Agent version information is not available");
			}
			String[] versionInfo = agentVersion.split("\\.");
			if (versionInfo.length < 4) {
				throw new ADKException("Agent version information incomplete: " + agentVersion);
			}
			int major = Integer.valueOf(versionInfo[0]).intValue();
			int minor = Integer.valueOf(versionInfo[1]).intValue();
			int revision = Integer.valueOf(versionInfo[2]).intValue();
			// int bldno = Integer.valueOf(versionInfo[3]).intValue();

			int version = major * 10000 + minor * 100 + revision;
			if (version >= minAgentVersion) {
				log(LOGLEVEL_DEBUG, "Agent library version: " + agentVersion);
				return true;
			}
			log(LOGLEVEL_WARNING, agentVersionError + " (current version is: " + agentVersion + ")");
		}
		catch (Throwable t) {
			// this usually happens if not connected to the Server...
			log(LOGLEVEL_DEBUG, "Failed to determine Agent version: " + t.toString());
		}
		return false;
	}

	private static void log(int level, String msg) {
		if ((debug && level == LOGLEVEL_DEBUG) || level != LOGLEVEL_DEBUG) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
			System.err.println(sdf.format(new Date()) + getLogLevelPrefix(level) + logPrefix + msg);
		}
	}

	private static String getLogLevelPrefix(int level) {
		return level == LOGLEVEL_WARNING ? "warning" : "debug  ";
	}
}
