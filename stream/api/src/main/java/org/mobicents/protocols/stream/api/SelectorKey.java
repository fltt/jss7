/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.stream.api;

/**
 * SelectorKey. Implementation represents stream inside selector.
 * 
 * @author baranowb
 * 
 */
public interface SelectorKey {
	/**
	 * Attach application specific object to this key. When underlying stream is
	 * ready for IO and key is returned, this attachment will be accessible.
	 * 
	 * @param obj
	 */
	public void attach(Object obj);

	/**
	 * Gets attachemnt.
	 * 
	 * @return
	 */
	public Object attachment();

	/**
	 * Returns validity indicator.
	 * 
	 * @return
	 */
	public boolean isValid();

	/**
	 * Indicates if underlying stream is ready to read.
	 * 
	 * @return
	 */
	public boolean isReadable();

	/**
	 * Indicates if underlying stream is ready to write.
	 * 
	 * @return
	 */
	public boolean isWriteable();

	/**
	 * Returns stream associated with this key
	 * 
	 * @return
	 */
	public Stream getStream();

	/**
	 * Get selector for this key.
	 * 
	 * @return
	 */
	public StreamSelector getStreamSelector();

	/**
	 * Cancels this key. Equals deregistration of stream
	 */
	public void cancel(); // Oleg verify this.
}
