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

import java.util.List;


/**
 *
 * @author Tobias Wich
 */
public class ObjectDefinition {

	private final String javaName;
	private final String factoryName;
	private final List<String> ifaces;

	public ObjectDefinition(String javaName, String factoryName, List<String> ifaces) {
		this.javaName = javaName;
		this.factoryName = factoryName;
		this.ifaces = ifaces;
	}

	public String getFactoryMethodName() {
		return factoryName;
	}

	public <T extends DeclarationDescriptor> String getProtocolName(List<T> protocols) {
		for (String iface : ifaces) {
			//System.out.println("iface: " + iface);
			for (DeclarationDescriptor pd : protocols) {
				//System.out.println("protos: " + pd.getObjcName());
				if (iface.equals(pd.getObjcName())) {
					return iface;
				}
			}
		}

		throw new RuntimeException("Missing ObjcProtocol on NSObject instance.");
	}

	public String getJavaName() {
		return javaName;
	}

}
