/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.robovm.processor;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Neil Crossley
 */
public class TypeDescriptorRegistry {

	Map<Type, TypeDescriptor> lookup = new HashMap<>();

	List<EnumDescriptor> enums = new LinkedList<>();
	List<ProtocolDescriptor> protocols = new LinkedList<>();

	public List<EnumDescriptor> getEnums() {
		return Collections.unmodifiableList(enums);
	}

	public List<ProtocolDescriptor> getProtocols() {
		return Collections.unmodifiableList(protocols);
	}

	TypeDescriptor asType(Type type) {
		if (lookup.containsKey(type)) {
			return lookup.get(type);
		}
		if (type.isPrimitiveOrVoid()) {
			PrimitiveDescriptor desc = PrimitiveDescriptor.from(type);
			lookup.put(type, desc);
			return desc;
		}
		final String symbolName = type.tsym.toString();
		if (symbolName.equals("java.util.List")) {
			final Type innerType = type.allparams().head;
			if (innerType.isPrimitiveOrVoid()) {
				throw new IllegalArgumentException("Cannot wrap ");
			}
			ListDescriptor desc = new ListDescriptor(getReferencingType(innerType));
			lookup.put(type, desc);
			return desc;
		} else if (symbolName.equals("java.lang.String")) {
			PrimitiveDescriptor desc = new PrimitiveDescriptor(type, "NSString *");
			lookup.put(type, desc);
			return desc;
		} else if (symbolName.equals("java.lang.Integer")) {
			PrimitiveDescriptor desc = new PrimitiveDescriptor(type, "int");
			lookup.put(type, desc);
			return desc;
		} else if (type.getTag() == TypeTag.ARRAY) {
			final Type innerType = ((Type.ArrayType) type).getComponentType();
			if (innerType.isPrimitive() && innerType.tsym.getSimpleName().toString().equals("byte")) {
				PrimitiveDescriptor desc = new PrimitiveDescriptor(type, "NSData *");
				lookup.put(type, desc);
				return desc;
			}
		}

		System.out.printf("WARNING: Found an undeclared type %s. Will assume it is a valid, available Java protocol.\n", type);
		ProtocolDescriptor descriptor = new ProtocolDescriptor(type.tsym.getSimpleName().toString(), com.sun.tools.javac.util.List.nil());
		lookup.put(type, descriptor);
		return descriptor;
	}

	private LookupDescriptor getReturnType(Type type) {
		return new LookupDescriptor(type, this);
	}

	private LookupDescriptor getParameterType(Type type) {
		return new LookupDescriptor(type, this);
	}

	private LookupDescriptor getReferencingType(Type type) {
		return new LookupDescriptor(type, this);
	}

	public EnumDescriptor createEnumDescriptor(Type type) {
		EnumDescriptor result = new EnumDescriptor(type.tsym.getSimpleName().toString());
		lookup.put(type, result);
		enums.add(result);
		return result;
	}

	public ProtocolDescriptor createProtocolDescriptor(JCTree.JCClassDecl ccd) {
		ProtocolDescriptor descriptor = new ProtocolDescriptor(ccd.getSimpleName().toString(), ccd.implementing);
		lookup.put(ccd.sym.type, descriptor);
		protocols.add(descriptor);
		return descriptor;
	}

	public MethodDescriptor createMethodDescriptor(String name, Type type) {
		MethodDescriptor descriptor = new MethodDescriptor(name,
				getReturnType(type));

		return descriptor;
	}

	public MethodParameterDescriptor createMethodParameterDescriptor(String name, Type type) {
		MethodParameterDescriptor descriptor = new MethodParameterDescriptor(name, getParameterType(type));

		return descriptor;
	}

}
