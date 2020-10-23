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

import com.sun.tools.javac.code.Symbol;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author Tobias Wich
 */
public class MethodDescriptor {

	private final String name;
	private final TypeDescriptor returnType;
	private final Symbol.MethodSymbol methodSymbol;
	private final List<MethodParameterDescriptor> params;
	private final boolean hasOverrideAnnotation;

	public MethodDescriptor(
			String name,
			TypeDescriptor returnType,
			Symbol.MethodSymbol methodSymbol,
			boolean hasOverrideAnnotation
	) {
		this.name = name;
		this.returnType = returnType;
		this.methodSymbol = methodSymbol;
		this.hasOverrideAnnotation = hasOverrideAnnotation;
		this.params = new ArrayList<>();
	}

	public Symbol.MethodSymbol getMethodSymbol() {
		return methodSymbol;
	}

	public void addParam(MethodParameterDescriptor param) {
		this.params.add(param);
	}

	public List<MethodParameterDescriptor> getParameters() {
		return Collections.unmodifiableList(params);
	}

	public TypeDescriptor getReturnType() {
		return returnType;
	}

	public String getName() {
		return name;
	}

	public boolean isDeprecated() {
		return methodSymbol.isDeprecated();
	}

	public boolean hasOverrideAnnotation() {
		return hasOverrideAnnotation;
	}

	public void printSignature(PrintWriter w) {
		String macroModifier = this.isDeprecated()
				? " DEPRECATED_ATTRIBUTE"
				: "";
		w.printf("(%s) %s%s", this.getReturnType().getIosType(), this.getName(), macroModifier);
		boolean isFirstParameter = true;
		for (MethodParameterDescriptor mp : this.params) {
			final String effectiveParameterType = mp.getType().getIosType();
			final String paramName = mp.getName();
			if (isFirstParameter) {
				w.printf(":(%s)%s", effectiveParameterType, paramName);
				isFirstParameter = false;
			} else {
				char firstCharacter = Character.toUpperCase(paramName.charAt(0));
				String remainingChar = paramName.substring(1);
				w.printf(" with%s%s:(%s)%s", firstCharacter, remainingChar, effectiveParameterType, paramName);
			}
		}
	}

	public String asSelector() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		boolean isFirstParameter = true;
		for (MethodParameterDescriptor mp : this.params) {
			final String paramName = mp.getName();
			if (isFirstParameter) {
				isFirstParameter = false;
			} else {
				builder.append("with");
				char firstCharacter = Character.toUpperCase(paramName.charAt(0));
				builder.append(firstCharacter);
				String remainingChar = paramName.substring(1);
				builder.append(remainingChar);
			}
			builder.append(":");
		}
		return builder.toString();
	}

}
