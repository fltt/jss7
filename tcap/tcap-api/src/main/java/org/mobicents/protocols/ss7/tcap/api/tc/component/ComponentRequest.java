package org.mobicents.protocols.ss7.tcap.api.tc.component;

import org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog;

public interface ComponentRequest {

	/**
	 * Returns type of component, each component is indetified by intiger code.
	 * @return
	 */
	public int getType();
	
	public Dialog getDialog();
	
	public int getInvokeId();
	
	public void setInvokeId(int id);
	
}