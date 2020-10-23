/** **************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
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
public class KeywordDescriptor implements TypeDescriptor {

	private final String keyword;

	public KeywordDescriptor(String keyword) {
		this.keyword = keyword;
	}

	@Override
	public String getIosType() {
		return keyword;
	}

	@Override
	public String marshaller() {
		return null;
	}

}
