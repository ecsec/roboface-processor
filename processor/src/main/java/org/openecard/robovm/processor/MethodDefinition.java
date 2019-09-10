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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author Tobias Wich
 */
public class MethodDefinition {

    private final String name;
    private final TypeDefinition returnType;
    private final ArrayList<MethodParameter> params;

    public MethodDefinition(String name, Type returnType) {
	this.name = name;
	this.returnType = new TypeDefinition(returnType);
	this.params = new ArrayList<>();
    }

    public void addParam(MethodParameter param) {
	this.params.add(param);
    }

    public List<MethodParameter> getParameters() {
	return Collections.unmodifiableList(params);
    }

    public TypeDefinition getReturnType() {
	return returnType;
    }

    public String getName() {
	return name;
    }

}
