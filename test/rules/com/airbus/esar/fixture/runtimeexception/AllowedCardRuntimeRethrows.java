/*
 * Copyright (c) 2026 Airbus Defence and Space Oy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.airbus.esar.fixture.runtimeexception;

import javacard.framework.APDUException;
import javacard.framework.CardRuntimeException;
import javacard.framework.ISOException;
import javacard.framework.PINException;
import javacard.framework.SystemException;
import javacard.framework.TransactionException;
import javacard.framework.service.ServiceException;
import javacard.security.CryptoException;
import javacardx.biometry.BioException;
import javacardx.biometry1toN.Bio1toNException;
import javacardx.external.ExternalException;
import javacardx.framework.string.StringException;
import javacardx.framework.time.DateTimeException;
import javacardx.framework.tlv.TLVException;
import javacardx.framework.util.UtilException;
import javacardx.security.cert.CertificateException;

/**
 * Rethrows each CardRuntimeException API subtype — all must stay silent under AFSCM 45.
 */
public final class AllowedCardRuntimeRethrows {

    public void rethrowCardRuntimeException() {
        try {
            CardRuntimeException.throwIt((short) 0);
        } catch (final CardRuntimeException e) {
            throw e;
        }
    }

    public void rethrowIsoException() {
        try {
            ISOException.throwIt((short) 0x6A80);
        } catch (final ISOException e) {
            throw e;
        }
    }

    public void rethrowApduException() {
        try {
            APDUException.throwIt((short) 0);
        } catch (final APDUException e) {
            throw e;
        }
    }

    public void rethrowPinException() {
        try {
            PINException.throwIt((short) 0);
        } catch (final PINException e) {
            throw e;
        }
    }

    public void rethrowSystemException() {
        try {
            SystemException.throwIt((short) 0);
        } catch (final SystemException e) {
            throw e;
        }
    }

    public void rethrowTransactionException() {
        try {
            TransactionException.throwIt((short) 0);
        } catch (final TransactionException e) {
            throw e;
        }
    }

    public void rethrowServiceException() {
        try {
            ServiceException.throwIt((short) 0);
        } catch (final ServiceException e) {
            throw e;
        }
    }

    public void rethrowCryptoException() {
        try {
            CryptoException.throwIt((short) 0);
        } catch (final CryptoException e) {
            throw e;
        }
    }

    public void rethrowBioException() {
        try {
            BioException.throwIt((short) 0);
        } catch (final BioException e) {
            throw e;
        }
    }

    public void rethrowBio1toNException() {
        try {
            Bio1toNException.throwIt((short) 0);
        } catch (final Bio1toNException e) {
            throw e;
        }
    }

    public void rethrowExternalException() {
        try {
            ExternalException.throwIt((short) 0);
        } catch (final ExternalException e) {
            throw e;
        }
    }

    public void rethrowStringException() {
        try {
            StringException.throwIt((short) 0);
        } catch (final StringException e) {
            throw e;
        }
    }

    public void rethrowDateTimeException() {
        try {
            DateTimeException.throwIt((short) 0);
        } catch (final DateTimeException e) {
            throw e;
        }
    }

    public void rethrowTlvException() {
        try {
            TLVException.throwIt((short) 0);
        } catch (final TLVException e) {
            throw e;
        }
    }

    public void rethrowUtilException() {
        try {
            UtilException.throwIt((short) 0);
        } catch (final UtilException e) {
            throw e;
        }
    }

    public void rethrowCertificateException() {
        try {
            CertificateException.throwIt((short) 0);
        } catch (final CertificateException e) {
            throw e;
        }
    }
}
