/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: TaggedRunnable.java
 * @date: 18.05.2009
 * @author: roland.mungenast
 */
package com.dynatrace.adk.impl;

import com.dynatrace.adk.Tagging;

/**
 * Wrapper for {@link Runnable} objects which 
 * tags the <tt>run</tt> method of a Runnable as a server-side sub-path.
 *
 * @author roland.mungenast
 */
public final class TaggedRunnable implements Runnable {

    private Runnable runnable;
    private byte[] tag;
    private Tagging tagging;

    /**
     * Constructs a new {@link TaggedRunnable} which tags the <tt>run</tt> method of a 
     * given {@link Runnable} as a server-side sub-path.
     *
     * @param tagging the tagging ADK to use for the tagging itself
     * @param runnable the runnable to tag
     * @param tag the tag to add to the runnable
     * @author roland.mungenast
     */
    TaggedRunnable(Tagging tagging, Runnable runnable, byte[] tag) {
        this.tagging = tagging;
        this.runnable = runnable;
        this.tag = tag;
    }

    /**
     *	Start the actual Runnable handling dynaTrace tagging automatically
     */
    public void run() {
        tagging.setTag(tag);
        tagging.startServerPurePath();
        runnable.run();
        tagging.endServerPurePath();
    }

}