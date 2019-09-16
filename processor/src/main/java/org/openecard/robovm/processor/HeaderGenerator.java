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

	private final List<ProtocolDefinition> protocols;
	private final List<ObjectDefinition> objects;
	private final ForwardDecl fwDecl;

	public HeaderGenerator(List<ProtocolDefinition> protocols, List<ObjectDefinition> objects) {
		this.protocols = protocols;
		this.objects = objects;
		this.fwDecl = new ForwardDecl(protocols);
	}


	public void writeHeader(Writer headerWriter) {
		try (PrintWriter w = new PrintWriter(headerWriter)) {
			writeFileHeader(w);
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
		w.printf("NS_SWIFT_NAME(%s)%n", objcName);
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
			String prefix = ": ";
			for (MethodParameter mp : md.getParameters()) {
				w.printf("%s(%s) %s", prefix, mp.getType(), mp.getName());
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

}
