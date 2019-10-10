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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Neil Crossley
 */
public class EnumDescriptor implements TypeDescriptor, DeclarationDescriptor {

	private final String javaName;
	private final List<String> values;

	public EnumDescriptor(String javaName) {
		this.javaName = javaName;
		this.values = new ArrayList<>();
	}

	public void addValue(String val) {
		values.add(val);
	}

	public List<String> getValues(){
		return Collections.unmodifiableList(values);
	}

	public String getJavaName() {
		return javaName;
	}

	@Override
	public String getIosType() {
		return this.getJavaName();
	}

	@Override
	public String getObjcName() {
		return this.getIosType();
	}
}
