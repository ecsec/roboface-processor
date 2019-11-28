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

import com.sun.tools.javac.code.Type;

/**
 *
 * @author Neil Crossley
 */
public class ListDescriptor implements TypeDescriptor {

	private final LookupTypeDescriptor referencingType;
	private final Type innerType;
	private final String symbolName;

	ListDescriptor(Type innerType, LookupTypeDescriptor referencingType) {
		this.innerType = innerType;
		this.referencingType = referencingType;
		this.symbolName = innerType.tsym.toString();
	}

	@Override
	public String getIosType() {
		return String.format("NSArray<%s> *", referencingType.getIosType());
	}

	@Override
	public String marshaller() {
		if (symbolName.equals("java.lang.String")) {
			return "org.robovm.apple.foundation.NSArray.AsStringListMarshaler";
		} else if (symbolName.equals("java.lang.Integer")) {
			return "org.robovm.apple.foundation.NSArray.AsIntegerListMarshaler";
		}
		return "org.robovm.apple.foundation.NSArray.AsListMarshaler";
	}

	@Override
	public String toString() {
		return this.getIosType();
	}

}
