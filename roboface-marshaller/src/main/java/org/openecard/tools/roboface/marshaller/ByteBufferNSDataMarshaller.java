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

import java.nio.ByteBuffer;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSObject;
import org.robovm.rt.bro.annotation.MarshalsPointer;
import org.robovm.rt.bro.annotation.MarshalsValue;

/**
 *
 * @author Neil Crossley
 */
public class ByteBufferNSDataMarshaller {

	@MarshalsPointer
	public static ByteBuffer toObject(Class<?> cls, long handle, long flags) {
		if (handle == 0L) {
			return null;
		}
		NSData nsData = (NSData)NSObject.Marshaler.toObject(NSData.class, handle, flags);
		return ByteBuffer.wrap(nsData.getBytes());
	}

	@MarshalsValue
	public static long toNative(ByteBuffer v, long flags) {
		if (v == null) {
			return 0L;
		}
		return NSObject.Marshaler.toNative(new NSData(v), flags);
	}

}
