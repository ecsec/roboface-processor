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

import org.robovm.apple.foundation.NSDate;
import org.robovm.apple.foundation.NSObject;
import org.robovm.rt.bro.annotation.MarshalsValue;

/**
 *
 * @author Florian Otto
 */
public class DateNSDateMarshaller {

    @MarshalsValue
    public static java.util.Date toObject(Class<?> cls, long handle, long flags) {
	if (handle == 0L) {
	    return null;
	}

	NSDate date = (NSDate) NSObject.Marshaler.toObject(NSDate.class, handle, flags);
	long epochMillisecs = (long) date.getTimeIntervalSince1970() * 1000;
	return new java.util.Date(epochMillisecs);
    }

    @MarshalsValue
    public static long toNative(java.util.Date d, long flags) {
	double epochSeconds = (double) d.getTime() / 1000;
	NSDate nd = new NSDate(epochSeconds);
	return NSObject.Marshaler.toNative(nd, flags);
    }

}
