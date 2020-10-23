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
 * @author Tobias Wich
 * @author Neil Crossley
 */
public class ClassDescriptor implements TypeDescriptor, DeclarationDescriptor {

	private final String objcName;
	private final List<LookupDeclarationDescriptor> extensions;
	private final List<MethodDescriptor> methods;
	private final ClassType type;

	public ClassDescriptor(String ifaceName, List<LookupDeclarationDescriptor> implementing, ClassType type) {
		this.objcName = ifaceName;
		this.methods = new ArrayList<>();
		this.extensions = implementing;
		this.type = type;
	}

	@Override
	public String getObjcName() {
		return objcName;
	}

	public ClassType getClassType() {
		return this.type;
	}

	public void addMethod(MethodDescriptor method) {
		methods.add(method);
	}

	public List<MethodDescriptor> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	public List<LookupDeclarationDescriptor> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}

	public List<DeclarationDescriptor> getExtensions(List<DeclarationDescriptor> objcProtocols) {
		ArrayList<DeclarationDescriptor> result = new ArrayList<>(extensions);
		result.retainAll(objcProtocols);
		return result;
	}

	@Override
	public String getIosType() {
		return this.getClassType().asReference(this);
	}

	@Override
	public String marshaller() {
		return null;
	}

	public static enum ClassType {
		Interface("%s *"),
		Protocol("NSObject<%s> *");

		private final String iosTypeFormat;

		ClassType(String iosTypeFormat){
			this.iosTypeFormat = iosTypeFormat;
		}

		public String asReference(DeclarationDescriptor descriptor) {
			return String.format(iosTypeFormat, descriptor.getObjcName());
		}
	}
}
