package org.mobicents.ss7.sctp;

import java.util.Properties;

abstract class MTPProviderImpl implements MTPProvider{

	
	public abstract void start() throws StartFailedException,IllegalStateException;
	public abstract void close() throws IllegalStateException;
	
	
}