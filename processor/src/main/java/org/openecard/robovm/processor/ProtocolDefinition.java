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

import com.sun.tools.javac.tree.JCTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author Tobias Wich
 */
public class ProtocolDefinition {

	private final String objcName;
	private final ArrayList<String> extensions;
	private final ArrayList<MethodDefinition> methods;

	public ProtocolDefinition(String ifaceName, com.sun.tools.javac.util.List<JCTree.JCExpression> implementing) {
		this.objcName = ifaceName;
		this.extensions = new ArrayList<>();
		this.methods = new ArrayList<>();

		implementing.forEach((nextIface) -> {
			extensions.add(nextIface.type.asElement().getSimpleName().toString());
		});
	}


	public String getObjcName() {
		return objcName;
	}

	public void addMethod(MethodDefinition method) {
		methods.add(method);
	}

	public List<MethodDefinition> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	public List<String> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}

	public List<String> getExtensions(List<String> objcProtocols) {
		ArrayList<String> result = new ArrayList<>(extensions);
		result.retainAll(objcProtocols);
		return result;
	}

}
