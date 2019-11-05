/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.tools.roboface.marshaller;

import org.robovm.apple.foundation.NSData;
import org.robovm.rt.bro.annotation.MarshalsValue;

/**
 *
 * @author Neil Crossley
 */
public class ByteArrayNSDataMarshaller {

	@MarshalsValue
	public static byte[] toObject(Class<?> cls, NSData value, long flags) {
		return value.getBytes();
	}

	@MarshalsValue
	public static NSData toNative(byte[] v, long flags) {
		return new NSData(v);
	}

}
