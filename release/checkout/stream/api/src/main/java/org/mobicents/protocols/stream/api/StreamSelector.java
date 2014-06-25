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

import javolution.util.FastList;

/**
 *  
 * @author abhayani
 * @author baranowb
 */
public interface StreamSelector {

	public static final int OP_READ = 0x1;
	public static final int OP_WRITE = 0x2;

	/**
	 * Performs query of registeres stream. Returns set of keys pointing to streams ready to perform IO.
	 * @param operation - operation which streams are queried. Value is equal to on of OP_X.
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
	public FastList<SelectorKey> selectNow(int operation, int timeout) throws IOException;

	/**
	 * Checks if selector has been closed.
	 * @return
	 */
	public boolean isClosed();
	/**
	 * closeses selector, removes all stream from internal register.
	 */
	public void close();
	/**
	 * Returns registered streams.
	 * @return
	 */
	public FastList<Stream> getRegisteredStreams();
}
