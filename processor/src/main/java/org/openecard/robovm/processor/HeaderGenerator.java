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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;


/**
 *
 * @author Tobias Wich
 */
public class HeaderGenerator {

	private final List<EnumDefinition> enums;
	private final List<ProtocolDefinition> protocols;
	private final List<ObjectDefinition> objects;
	private final ForwardDecl fwDecl;
	private final List<IncludeHeaderDefinition> includes;

	public HeaderGenerator(List<EnumDefinition> enums, List<ProtocolDefinition> protocols, List<ObjectDefinition> objects, List<IncludeHeaderDefinition> includes) {
		this.enums = enums;
		this.protocols = protocols;
		this.objects = objects;
		this.includes = includes;
		this.fwDecl = new ForwardDecl(protocols);
	}


	public void writeHeader(Writer headerWriter) {
		try (PrintWriter w = new PrintWriter(headerWriter)) {
			writeFileHeader(w);
			for (IncludeHeaderDefinition include: includes) {
				writeInclude(w, include);
			}

			for (EnumDefinition e : enums) {
				writeEnum(w, e);
			}
			w.println();
			for (ProtocolDefinition p : fwDecl.getProtocols()) {
				writeForwardDecl(w, p);
			}
			w.println();
			for (ProtocolDefinition p : protocols) {
				writeProtocol(w, p);
			}
			w.println();
			for (ObjectDefinition o : objects) {
				writeObject(w, o);
				//only create one object for framework entry
				break;
			}
		}
	}

	private void writeFileHeader(PrintWriter w) {
		w.println("#import <Foundation/Foundation.h>");
		w.println();
	}

	private void writeForwardDecl(PrintWriter w, ProtocolDefinition p) {
		String objcName = p.getObjcName();
		//w.printf("NS_SWIFT_NAME(%s)%n", objcName);
		w.printf("@protocol %s;%n", objcName);
	}

	private void writeProtocol(PrintWriter w, ProtocolDefinition p) {
		String objcName = p.getObjcName();
		//w.printf("NS_SWIFT_NAME(%s)%n", objcName);
		w.printf("@protocol %s", objcName);
		// add protocol inheritance
		if (! p.getExtensions().isEmpty()) {
			w.print("<");
			String prefix = "";
			for (String ext : p.getExtensions()) {
				w.printf("%s%s", prefix, ext);
				ext = ", ";
			}
			w.print(">");
		}
		w.println();

		for (MethodDefinition md : p.getMethods()) {
			writeMethod(w, md);
		}

		w.println("@end");
		w.printf("typedef NSObject<%s> %s;%n", objcName, objcName);
		w.println();
	}

	private void writeMethod(PrintWriter w, MethodDefinition md) {
		w.printf("-(%s) %s", md.getReturnType(), md.getName());
		if (! md.getParameters().isEmpty()) {
			boolean isFirstParameter = true;
			for (MethodParameter mp : md.getParameters()) {
				if (isFirstParameter) {
					w.printf(":(%s)%s", mp.getType(), mp.getName());
					isFirstParameter = false;
				} else {
					String paramName = mp.getName();
					char firstCharacter = paramName.charAt(0);
					String remainingChar = paramName.substring(1);
					w.printf(" with%s%s:(%s)%s", firstCharacter, remainingChar, mp.getType(), mp.getName());
				}
			}
		}
		w.println(";");
	}

	private void writeObject(PrintWriter w, ObjectDefinition o) {
		String protocolName = o.getProtocolName(protocols);
		w.printf("static %s* %s() {%n", protocolName, o.getFactoryMethodName());
		w.printf("\textern %s* rvmInstantiateFramework(const char *className);%n", protocolName);
		w.printf("\treturn rvmInstantiateFramework(\"%s\");%n", o.getJavaName());
		w.println("}");
		w.println();
	}

	private void writeEnum(PrintWriter w, EnumDefinition e){
		w.printf("typedef NS_ENUM(NSUInteger, %s) {%n", e.getJavaName());
		for(String v : e.getValues()){
			w.printf("k%s%s,%n", e.getJavaName(), v);
		}
		w.println("};");
		w.println();
	}

	private void writeInclude(PrintWriter w, IncludeHeaderDefinition include) {
		w.printf("#import \"%s\"", include);

	}

}
