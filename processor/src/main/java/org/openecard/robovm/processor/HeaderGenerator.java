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
import com.sun.tools.javac.code.Types;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Tobias Wich
 */
public class HeaderGenerator {

	private final List<ObjectDefinition> objects;
	private final List<IncludeHeaderDefinition> includes;
	private final TypeRegistry registry;
	private final Types types;

	public HeaderGenerator(
			List<ObjectDefinition> objects,
			List<IncludeHeaderDefinition> includes,
			TypeRegistry registry,
			Types types) {
		this.objects = objects;
		this.includes = includes;
		this.registry = registry;
		this.types = types;
	}

	public void writeHeader(Writer headerWriter) {
		try (PrintWriter w = new PrintWriter(headerWriter)) {
			writeFileHeader(w);

			writeIncludes(w);

			for (EnumDescriptor e : registry.getEnums()) {
				writeEnum(w, e);
			}
			w.println();

			List<Map.Entry<Type, ProtocolDescriptor>> sortedClass = sortProtocols(registry.protocols);
			for (Map.Entry<Type, ProtocolDescriptor> entry : sortedClass) {
				writeForwardDecl(w, entry.getValue());
			}
			w.println();

			for (Map.Entry<Type, ProtocolDescriptor> values : sortedClass) {
				Type key = values.getKey();
				ProtocolDescriptor p = values.getValue();
				writeProtocol(w, key, p);
			}

			w.println();

			for (ObjectDefinition o : objects) {
				writeInitiatorFunction(w, o);
			}
		}
	}

	private void writeIncludes(final PrintWriter w) {
		for (IncludeHeaderDefinition include: includes) {
			writeInclude(w, include);
		}
		if (!includes.isEmpty()) {
			w.println();
		}
	}

	private void writeFileHeader(PrintWriter w) {
		w.println("#import <Foundation/Foundation.h>");
		w.println();
	}

	private void writeForwardDecl(PrintWriter w, ProtocolDescriptor p) {
		String objcName = p.getObjcName();
		//w.printf("NS_SWIFT_NAME(%s)%n", objcName);
		String iosType = p.getType() == ProtocolDescriptor.IosType.Interface ?
				"interface" : "protocol";

		w.printf("@%s %s;%n", iosType, objcName);
	}

	private void writeProtocol(PrintWriter w, Type type, ProtocolDescriptor p) {
		String objcName = p.getObjcName();
		//w.printf("NS_SWIFT_NAME(%s)%n", objcName);
		boolean isProtocol = p.getType() == ProtocolDescriptor.IosType.Protocol;
		boolean isInterface = p.getType() == ProtocolDescriptor.IosType.Interface;
		String iosType = isProtocol
				? "protocol"
				: (isInterface
						? "interface"
						: "unknown");
		w.printf("@%s %s", iosType, objcName);
		// add protocol inheritance
		if (!p.getExtensions().isEmpty()) {
			if (isProtocol) {
				w.print("<");
			} else if (isInterface) {
				w.print(" : ");
			}
			String prefix = "";
			for (DeclarationDescriptor ext : p.getExtensions()) {
				w.printf("%s%s", prefix, ext.getObjcName());
				prefix = ", ";
			}
			if (isProtocol) {
				w.print(">");
			}
		}
		if (isInterface) {
			w.println(" {");
			w.println("}");
		} else if (isProtocol) {
			w.println();
		}

		for (MethodDescriptor md : p.getMethods()) {
			boolean overrides = isOverridingMethod(md, p, type);
			if (!overrides) {
				this.writeMethod(w, md);
			}
		}

		w.println("@end");
		if (isProtocol) {
			w.printf("typedef NSObject<%s> %s;%n", objcName, objcName);
		}
		w.println();
	}

	private boolean isOverridingMethod(MethodDescriptor method, ProtocolDescriptor owner, Type ownerType) {
		boolean overrides = false;
		for (LookupDeclarationDescriptor extension : owner.getExtensions()) {
			final Type inheritedType = extension.getType();
			ProtocolDescriptor protocol = this.registry.protocols.get(inheritedType);
			if (protocol != null) {
				for (MethodDescriptor inheritedMethod : protocol.getMethods()) {

					if (method.getMethodSymbol().overrides(inheritedMethod.getMethodSymbol(), ownerType.tsym, types, false)) {
						overrides = true;
						break;
					}
				}
			}
			if (overrides) {
				break;
			}
		}
		return overrides;
	}

	private void writeMethod(PrintWriter w, MethodDescriptor md) {
		w.print("-");
		md.printSignature(w);
		w.println(";");
	}

	private void writeInitiatorFunction(PrintWriter w, ObjectDefinition o) {
		String protocolName = o.getProtocolName(this.registry.getProtocols());
		w.printf("static %s* %s() {%n", protocolName, o.getFactoryMethodName());
		w.printf("\textern %s* rvmInstantiateFramework(const char *className);%n", protocolName);
		w.printf("\treturn rvmInstantiateFramework(\"%s\");%n", o.getJavaName());
		w.println("}");
		w.println();
	}

	private void writeEnum(PrintWriter w, EnumDescriptor e){
		w.printf("typedef NS_ENUM(NSUInteger, %s) {%n", e.getJavaName());
		for(String v : e.getValues()){
			w.printf("k%s%s,%n", e.getJavaName(), v);
		}
		w.println("};");
		w.println();
	}

	private void writeInclude(PrintWriter w, IncludeHeaderDefinition include) {
		w.printf("#import \"%s\"", include);
		w.println();

	}

	private List<Map.Entry<Type, ProtocolDescriptor>> sortProtocols(Map<Type, ProtocolDescriptor> input) {
		List<Map.Entry<Type, ProtocolDescriptor>> protocols = new ArrayList<>(input.size());
		for (Map.Entry<Type, ProtocolDescriptor> entry : input.entrySet()) {
			ProtocolDescriptor next = entry.getValue();
			if (next.getExtensions().isEmpty()) {
				// no dependency, insert at the beginning right away
				protocols.add(0, entry);
			} else {
				// we want to insert this element after the last element of the extension list
				// or if something is missing, insert it at the foremost position, so that a later insertion will if in
				// doubt precede the missing element
				int idx = 0;
				List<String> extensionNames = new ArrayList<>();
				for (DeclarationDescriptor extension : next.getExtensions()) {
					extensionNames.add(extension.getObjcName());
				}
				for (int i = 0; i < protocols.size(); i++) {
					Map.Entry<Type, ProtocolDescriptor> testEntry = protocols.get(i);
					ProtocolDescriptor testObj = testEntry.getValue();
					// remove and see if an element has been removed actually
					if (extensionNames.remove(testObj.getObjcName())) {
						// advance insertion index to the position after this element
						idx = i + 1;
					}
					// stop if there is nothing left
					if (extensionNames.isEmpty()) {
						break;
					}
				}
				// add the element at the correct psoition
				protocols.add(idx, entry);
			}
		}
		return protocols;
	}
}
