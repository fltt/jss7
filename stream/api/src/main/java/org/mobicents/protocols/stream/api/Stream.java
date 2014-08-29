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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface which hides impl details of stream.
 * 
 * @author amit bhayani
 * @author baranowb
 * 
 */
public interface Stream {

	/**
	 * Registers this stream with the given selector, returning a selection key.
	 * This method first verifies that this channel is open and that the given
	 * initial interest set is valid.
	 * 
	 * If this stream is already registered with the given selector then the
	 * selection key representing that registration is returned after setting
	 * its interest set to the given value.
	 * 
	 * @param selector
	 * @param op
	 *            The selector with which this channel is to be registered
	 * @return
	 */
	public SelectorKey register(StreamSelector selector) throws IOException;

	/**
	 * Reads a sequence of bytes from this channel into the given buffer.
	 * 
	 * @param b
	 *            The buffer into which bytes are to be transferred
	 * @return The number of bytes read, possibly zero, or -1 if the channel has
	 *         reached end-of-stream
	 * @throws IOException
	 */
	public int read(ByteBuffer b) throws IOException;

	/**
	 * Writes a sequence of bytes to this channel from the given buffer.
	 * 
	 * @param b
	 *            The buffer from which bytes are to be retrieved
	 * @return The number of bytes written, possibly zero
	 * @throws IOException
	 */
	public int write(ByteBuffer b) throws IOException;

	/**
	 * Closes this streamer implementation. After closing stream its selectors
	 * are invalidated!
	 */
	public void close();

	/**
	 * Returns the provider that created this stream.
	 * 
	 * @return
	 */
	public SelectorProvider provider();
}
