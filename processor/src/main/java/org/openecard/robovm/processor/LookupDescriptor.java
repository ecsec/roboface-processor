/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.robovm.processor;

import com.sun.tools.javac.code.Type;

/**
 *
 * @author Neil Crossley
 */
public class LookupDescriptor implements TypeDescriptor {

	private final Type givenType;
	private final TypeDescriptorRegistry registry;

	public LookupDescriptor(Type givenType, TypeDescriptorRegistry registry) {
		this.givenType = givenType;
		this.registry = registry;
	}

	@Override
	public String getIosType() {
		return this.registry.asType(this.givenType).getIosType();
	}

	@Override
	public String toString() {
		return this.registry.asType(this.givenType).toString();
	}

	@Override
	public String marshaller() {
		return this.registry.asType(this.givenType).marshaller();
	}

}
