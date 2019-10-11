/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.robovm.processor;

/**
 *
 * @author Neil Crossley
 */
public class ListDescriptor implements TypeDescriptor {

	private final TypeDescriptor descriptor;

	public ListDescriptor(TypeDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public String getIosType() {
		return String.format("NSArray<%s>", descriptor.getIosType());
	}

	@Override
	public String marshaller() {
		return "org.robovm.apple.foundation.NSArray.AsListMarshaler";
	}

	@Override
	public String toString() {
		return this.getIosType();
	}

}
