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
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Tobias Wich
 */
public class HeaderGenerator {

	private final List<ObjectDefinition> objects;
	private final List<IncludeHeaderDefinition> includes;
	private final TypeDescriptorRegistry registry;

	public HeaderGenerator(List<ObjectDefinition> objects, List<IncludeHeaderDefinition> includes, TypeDescriptorRegistry registry) {
		this.objects = objects;
		this.includes = includes;
		this.registry = registry;
	}

	public void writeHeader(Writer headerWriter) {
		try (PrintWriter w = new PrintWriter(headerWriter)) {
			writeFileHeader(w);

			writeIncludes(w);

			for (EnumDescriptor e : registry.getEnums()) {
				writeEnum(w, e);
			}
			w.println();

			for (ProtocolDescriptor p : sortProtocols(registry.getProtocols())) {
				writeForwardDecl(w, p);
			}
			w.println();

			for (ProtocolDescriptor p : registry.getProtocols()) {
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
		w.printf("@protocol %s;%n", objcName);
	}

	private void writeProtocol(PrintWriter w, ProtocolDescriptor p) {
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

		for (MethodDescriptor md : p.getMethods()) {
			writeMethod(w, md);
		}

		w.println("@end");
		w.printf("typedef NSObject<%s> %s;%n", objcName, objcName);
		w.println();
	}

	private void writeMethod(PrintWriter w, MethodDescriptor md) {
		w.printf("-(%s) %s", md.getReturnType().getIosType(), md.getName());
		if (! md.getParameters().isEmpty()) {
			boolean isFirstParameter = true;
			for (MethodParameterDescriptor mp : md.getParameters()) {
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
		w.println(";");
	}

	private void writeObject(PrintWriter w, ObjectDefinition o) {
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

	private List<ProtocolDescriptor> sortProtocols(List<ProtocolDescriptor> input) {
		List<ProtocolDescriptor> protocols = new ArrayList<>(input.size());
		for (ProtocolDescriptor next : input) {
			if (next.getExtensions().isEmpty()) {
				// no dependency, insert at the beginning right away
				protocols.add(0, next);
			} else {
				// we want to insert this element after the last element of the extension list
				// or if something is missing, insert it at the foremost position, so that a later insertion will if in
				// doubt precede the missing element
				int idx = 0;
				List<String> extensions = new ArrayList<>(next.getExtensions());
				for (int i = 0; i < protocols.size(); i++) {
					ProtocolDescriptor testObj = protocols.get(i);
					// remove and see if an element has been removed actually
					if (extensions.remove(testObj.getObjcName())) {
						// advance insertion index to the position after this element
						idx = i + 1;
					}
					// stop if there is nothing left
					if (extensions.isEmpty()) {
						break;
					}
				}
				// add the element at the correct psoition
				protocols.add(idx, next);
			}
		}
		return protocols;
	}
}
