/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.protocols.ss7.cap.errors;

import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessage;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageCancelFailed;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageParameterless;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageRequestedInfoError;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageSystemFailure;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageTaskRefused;
import org.mobicents.protocols.ss7.cap.primitives.CAPAsnPrimitive;

/**
 * Base class of CAP ReturnError messages
 *
 * @author sergey vetyutnev
 *
 */
public abstract class CAPErrorMessageImpl implements CAPErrorMessage, CAPAsnPrimitive {

    protected Long errorCode;

    protected CAPErrorMessageImpl(Long errorCode) {
        this.errorCode = errorCode;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public boolean isEmParameterless() {
        return false;
    }

    public boolean isEmCancelFailed() {
        return false;
    }

    public boolean isEmRequestedInfoError() {
        return false;
    }

    public boolean isEmSystemFailure() {
        return false;
    }

    public boolean isEmTaskRefused() {
        return false;
    }

    public CAPErrorMessageParameterless getEmParameterless() {
        return null;
    }

    public CAPErrorMessageCancelFailed getEmCancelFailed() {
        return null;
    }

    public CAPErrorMessageRequestedInfoError getEmRequestedInfoError() {
        return null;
    }

    public CAPErrorMessageSystemFailure getEmSystemFailure() {
        return null;
    }

    public CAPErrorMessageTaskRefused getEmTaskRefused() {
        return null;
    }
}
