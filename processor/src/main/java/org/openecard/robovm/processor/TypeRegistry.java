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

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Neil Crossley
 */
public class TypeRegistry {

	Map<Type, TypeDescriptor> typeLookup = new HashMap<>();
	Map<Type, DeclarationDescriptor> declarationLookup = new HashMap<>();

	List<EnumDescriptor> enums = new LinkedList<>();
	Map<Type, ClassDescriptor> protocols = new HashMap<>();

	private final Set<String> inheritanceBlacklist;

	TypeRegistry(Set<String> inheritanceBlacklist) {
		this.inheritanceBlacklist = inheritanceBlacklist;
	}

	public List<EnumDescriptor> getEnums() {
		return Collections.unmodifiableList(enums);
	}

	public boolean hasClasses() {
		return !this.protocols.isEmpty();
	}

	DeclarationDescriptor asDeclaration(Type type) {
		if (declarationLookup.containsKey(type)) {
			return declarationLookup.get(type);
		}

		ClassDescriptor descriptor = addFakeProtocolDescriptor(type);
		return descriptor;
	}

	TypeDescriptor asType(Type type) {
		if (typeLookup.containsKey(type)) {
			return typeLookup.get(type);
		}
		if (type.tsym.isEnum()) {
			EnumDescriptor desc = new EnumDescriptor(type.tsym.getSimpleName().toString());
			typeLookup.put(type, desc);
			return desc;

		}
		if (type.isPrimitiveOrVoid()) {
			PrimitiveDescriptor desc = PrimitiveDescriptor.from(type);
			typeLookup.put(type, desc);
			return desc;
		}
		final String symbolName = type.tsym.toString();
		if (symbolName.equals("java.util.List")) {
			final Type innerType = type.allparams().head;
			if (innerType.isPrimitiveOrVoid()) {
				throw new IllegalArgumentException("Cannot wrap ");
			}
			ListDescriptor desc = new ListDescriptor(innerType, getReferencingType(innerType));
			typeLookup.put(type, desc);
			return desc;
		} else if (symbolName.equals("java.lang.String")) {
			PrimitiveDescriptor desc = new PrimitiveDescriptor(type, "NSString *");
			typeLookup.put(type, desc);
			return desc;
		} else if (symbolName.equals("java.lang.Integer")) {
			PrimitiveDescriptor desc = new PrimitiveDescriptor(type, "NSNumber *");
			typeLookup.put(type, desc);
			return desc;
		} else if (symbolName.equals("java.nio.ByteBuffer")) {
			PrimitiveDescriptor desc = new PrimitiveDescriptor(type, "NSData *");
			desc.setMarshaller("org.openecard.tools.roboface.marshaller.ByteBufferNSDataMarshaller");
			typeLookup.put(type, desc);
			return desc;
		}

		ClassDescriptor descriptor = addFakeProtocolDescriptor(type);
		return descriptor;
	}

	private ClassDescriptor addFakeProtocolDescriptor(Type type) {
		System.out.printf("WARNING: Found an undeclared type %s. Will assume it is a valid, available Java protocol.\n", type);
		ClassDescriptor descriptor = new ClassDescriptor(
				type.tsym.getSimpleName().toString(),
				new LinkedList<>(),
				ClassDescriptor.ClassType.Protocol,
				false);
		this.typeLookup.put(type, descriptor);
		this.declarationLookup.put(type, descriptor);
		return descriptor;
	}

	private LookupTypeDescriptor getReturnType(Type type) {
		return new LookupTypeDescriptor(type, this);
	}

	private LookupTypeDescriptor getParameterType(Type type) {
		return new LookupTypeDescriptor(type, this);
	}

	private LookupTypeDescriptor getReferencingType(Type type) {
		return new LookupTypeDescriptor(type, this);
	}

	private LookupDeclarationDescriptor getReferencingDeclaration(Type type) {
		return new LookupDeclarationDescriptor(type, this);
	}

	public EnumDescriptor createEnumDescriptor(Type type) {
		EnumDescriptor result = new EnumDescriptor(type.tsym.getSimpleName().toString());
		typeLookup.put(type, result);
		enums.add(result);
		return result;
	}

	public ClassDescriptor createClassDescriptor(JCTree.JCClassDecl ccd, ClassDescriptor.ClassType type) {
		final String simpleName = ccd.getSimpleName().toString();
		List<LookupDeclarationDescriptor> inheritanceTypes = findInheritanceTypes(ccd, simpleName);

		ClassDescriptor descriptor = new ClassDescriptor(
				simpleName,
				inheritanceTypes,
				type,
				ccd.sym.isDeprecated()
		);
		typeLookup.put(ccd.sym.type, descriptor);
		protocols.put(ccd.sym.type, descriptor);
		return descriptor;
	}

	private List<LookupDeclarationDescriptor> findInheritanceTypes(JCTree.JCClassDecl ccd, final String simpleName) {
		final List<LookupDeclarationDescriptor> inheritanceTypes = new LinkedList<>();
		ccd.implementing.forEach((nextIface) -> {
			if (nextIface.getKind() == Tree.Kind.IDENTIFIER) {
				String fullname = nextIface.type.asElement().enclClass().className();

				if (!this.inheritanceBlacklist.contains(fullname)) {

					inheritanceTypes.add(this.getReferencingDeclaration(nextIface.type));
				} else {
					System.out.printf("Blacklisting inheritance %s from %s\n", simpleName, fullname);
				}
			}
		});
		return inheritanceTypes;
	}

	public MethodDescriptor createMethod(JCTree.JCMethodDecl tree) {

		Symbol.MethodSymbol methodSymbol = tree.sym;

		switch (methodSymbol.getKind()) {
			case CONSTRUCTOR: {
				if (methodSymbol.isStatic()) {
					return null;
				}
				return this.createConstructorDescriptor(
						methodSymbol);
			}
			case METHOD: {
				return this.createMethodDescriptor(
						tree.name.toString(),
						tree.getReturnType().type,
						methodSymbol);
			}
			default:
				return null;
		}
	}

	private MethodDescriptor createMethodDescriptor(
			String name,
			Type returntype,
			Symbol.MethodSymbol methodSymbol) {
		MethodDescriptor descriptor = new MethodDescriptor(
				name,
				getReturnType(returntype),
				methodSymbol
		);

		return descriptor;
	}

	private MethodDescriptor createConstructorDescriptor(Symbol.MethodSymbol methodSymbol) {
		MethodDescriptor descriptor = new MethodDescriptor(
				"init",
				new KeywordDescriptor("id"),
				methodSymbol);

		return descriptor;
	}

	public MethodParameterDescriptor createMethodParameterDescriptor(String name, Type type) {
		MethodParameterDescriptor descriptor = new MethodParameterDescriptor(name, getParameterType(type));

		return descriptor;
	}

}
