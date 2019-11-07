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
public class LookupDeclarationDescriptor implements DeclarationDescriptor {

	private final Type givenType;
	private final TypeRegistry registry;

	public LookupDeclarationDescriptor(Type givenType, TypeRegistry registry) {
		this.givenType = givenType;
		this.registry = registry;
	}

	@Override
	public String getObjcName() {
		return this.registry.asDeclaration(this.givenType).getObjcName();
	}

	@Override
	public String toString() {
		return this.registry.asDeclaration(this.givenType).toString();
	}

}
