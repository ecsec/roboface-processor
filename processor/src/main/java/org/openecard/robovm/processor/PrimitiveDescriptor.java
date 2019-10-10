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
public class PrimitiveDescriptor implements TypeDescriptor {

	private final String iosType;
	private final Type type;

	public PrimitiveDescriptor(Type type, String iosType) {
		this.type = type;
		this.iosType = iosType;
	}

	@Override
	public String getIosType() {
		return iosType;
	}

	@Override
	public String marshaller() {
		return null;
	}

	public static PrimitiveDescriptor from(Type type) {
		String simpleName = type.tsym.getSimpleName().toString();
		String converted;
		switch (simpleName) {
			case "void":
				converted = "void";
				break;
			case "Boolean":
			case "boolean":
				converted = "bool";
				break;
			case "Byte":
			case "byte":
				converted = "char";
				break;
			case "Integer":
			case "int":
				converted = "int";
				break;
			case "Short":
			case "short":
				converted = "short";
				break;
			case "Long":
			case "long":
				converted = "long";
				break;
			case "Float":
			case "float":
				converted = "float";
				break;
			case "Double":
			case "double":
				converted = "double";
				break;
			default:
				throw new IllegalArgumentException("Could not convert to a known primitive type: " + type);
		}
		return new PrimitiveDescriptor(type, converted);
	}

	private static String convertSimpleType(String type) {
		switch (type) {
			case "void": return "void";
			case "Boolean":
			case "boolean": return "bool";
			case "Byte":
			case "byte": return "char";
			case "Integer":
			case "int": return "int";
			case "Short":
			case "short": return "short";
			case "Long":
			case "long": return "long";
			case "Float":
			case "float": return "float";
			case "Double":
			case "double": return "double";
			case "String": return "NSString *";
			default: return String.format("NSObject<%s> *", type);
		}
	}

}
