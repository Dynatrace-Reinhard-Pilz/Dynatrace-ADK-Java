/**
 *  Copyright (c) Dynatrace 2001-2016
 */
package com.dynatrace.adk;

import com.dynatrace.adk.impl.DynaTraceADKFactoryImpl;

/**
 * {@link DynaTraceADKFactory} creates instances of the different dynaTrace 
 * ADKs.
 * @author ardeshir.arfaian, rainer.klaffenboeck
 */
public class DynaTraceADKFactory {
	/**
	 * Initializes the {@link DynaTraceADKFactory}
	 */
	public static void initialize() {
		DynaTraceADKFactoryImpl.initialize();
	}

	/**
	 * Creates an instance of {@link Tagging}
	 * @return a {@link Tagging} instance
	 */
	public static final Tagging createTagging() {
		return DynaTraceADKFactoryImpl.createTagging();
	}
	
	/**
	 * Uninitializes the {@link DynaTraceADKFactory}
	 */
	public static void uninitialize() {
		DynaTraceADKFactoryImpl.uninitialize();
	}
}
