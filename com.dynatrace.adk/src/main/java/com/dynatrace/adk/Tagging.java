/**
 *  Copyright (c) Dynatrace 2001-2016
 */
package com.dynatrace.adk;

/**
 * {@link Tagging} provides the user with everything needed for 
 * implementing tagging for proprietary protocols.
 * Use {@link DynaTraceADKFactory} to acquire an instance.   
 * @author ardeshir.arfaian, rainer.klaffenboeck
 */
public interface Tagging {
	/**
	 * Gets the current dynaTrace tag on the client side represented
	 * as byte array for serialization by the user.
	 * @return a byte array representing the tag
	 */
	byte[] getTag();
	
	/**
	 * Gets the current dynaTrace tag on the client side represented
	 * as {@link String} for serialization by the user.
	 * @return a {@link String} representing the tag
	 */
	String getTagAsString();
	
	/**
	 * Sets the dynaTrace tag on the server side from a byte array as
	 * deserialized by the user.
	 * @param tag a byte array representing the tag
	 */
	void setTag(byte[] tag);
	
	/**
	 * Sets the dynaTrace tag on the server side from a {@link String} as
	 * deserialized by the user.
	 * @param tag a {@link String} representing the tag
	 */
	void setTagFromString(String tag);

	/**
	 * Verifies whether a given tag is valid or not
	 * @param tag	dynaTrace tag either as byte array or string
	 * @return	true, if tag is valid, false otherwise
	 */
	boolean isTagValid(Object tag);
	
	/**
	 * Inserts a synchronous or asynchronous link on the client side.
	 * <p><b>Note:</b> There cannot be any instrumented calls between {@link #getTag()}
	 *       resp. {@link #getTagAsString()} and this call.
	 * @param asynchronous true if asynchronous, false if synchronous
	 */
	void linkClientPurePath(boolean asynchronous);
	
	/**
	 * Inserts a synchronous or asynchronous link on the client side.
	 * @param asynchronous true if asynchronous, false if synchronous
	 * @param tag specify a certain tag to be linked with (this allows
	 *    having instrumented calls between {@link #getTag()} and this call).
	 *    The tag can be specified either as byte array or as string.
	 */
	void linkClientPurePath(boolean asynchronous, Object tag);
	
	/**
	 * Starts a server-side PurePath
	 */
	void startServerPurePath();
	
	/**
	 * Ends a server-side PurePath
	 */
	void endServerPurePath();

    /**
     * Returns a wrapper for a given {@link Runnable} which tags the Runnable as a server-side sub
     * path. The sub path will start when the <tt>run</tt> method of the given Runnable is called.
     * 
     * <p>
     * <b>Important</b>: To link the client side path with the new sub path,
     * {@link #linkClientPurePath(boolean)} has to be called immediately before the returned
     * {@link Runnable} is scheduled for execution.
     * </p>
     * 
     * @param runnable the runnable to tag
     * @return the runnable to use instead of the given runnable
     * @author roland.mungenast
     */
    Runnable createServerPathRunnable(Runnable runnable);

    /**
     * Convert the specified dynaTrace tag to its string format. 
     * @param  tag	dynaTrace tag (byte array)
     * @return dynaTrace tag as string
     */
    public String convertTagToString(byte[] tag);

    /**
     * Convert the specified dynaTrace string tag to the tag's byte array representation
     * @param  tag	dynaTrace tag (string format)
     * @return dynaTrace tag as byte array
     */
    public byte[] convertStringToTag(String tag);

    /**
     * Custom tagging: wrapper class for custom tags
     */
    public interface CustomTag {
    	/**
    	 * Get current custom tag as byte array
    	 * @return	custom tag if present, null otherwise
    	 */
    	public byte[] getTag();
    	
    	/**
    	 * Get previous custom tag as byte array (i.e. the tag the current tag is linked to)
    	 * Note: a "previous tag" is no longer required and used, therefore this method has been deprecated.
    	 * @return	previous custom tag if exists, null otherwise
    	 * @deprecated
    	 */
    	public byte[] getPrevTag();
    	
    	/**
    	 * Get string representation of this custom tag in the format <tt>&lt;currentTag&gt;:&lt;prevTag&gt;</tt>
    	 * @return hex string representation of this tag
    	 */
    	public String asString();
    }
    
    /**
     * Create a custom tag object
     * 		Note: the "prevTagData" is no longer required and used, therefore this method has been deprecated.
     * 
     * @param tagData byte array containing the custom tag
     * @param prevTagData byte array containing the previous custom tag (optional)
     *        This parameter allows to chain up custom tags. It is usually null
     *        if no previous custom tag is present - in this case the custom tag
     *        will be linked to the existing dynaTrace tag of the current thread.
     * @return CustomTag object
     * @see Tagging#createCustomTag(byte[] tagData)
     * @deprecated
     */
    CustomTag createCustomTag(byte[] tagData, byte[] prevTagData);
    
    /**
     * Create a custom tag object
     * @param tagData byte array containing the custom tag
     * @return CustomTag object
     */
    CustomTag createCustomTag(byte[] tagData);
    
	/**
	 * Set specified custom tag at server side. The function has the same
	 * functionality as setTag(), but allows to continue a PurePath by using
	 * the specified custom tag. 
	 * @param customTag Custom tag to be continued
	 */
	void setCustomTag(byte[] customTag);
	
}
