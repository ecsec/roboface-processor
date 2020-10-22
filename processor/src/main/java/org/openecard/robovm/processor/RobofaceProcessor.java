/** **************************************************************************
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
 ************************************************************************** */
package org.openecard.robovm.processor;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import static com.sun.tools.javac.code.Flags.PARAMETER;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.openecard.robovm.annotations.FrameworkEnum;
import org.openecard.robovm.annotations.FrameworkInterface;
import org.openecard.robovm.annotations.FrameworkObject;

/**
 *
 * @author Tobias Wich
 */
@SupportedAnnotationTypes({
	"org.openecard.robovm.annotations.FrameworkEnum",
	"org.openecard.robovm.annotations.FrameworkInterface",
	"org.openecard.robovm.annotations.FrameworkObject"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
//@AutoService(Processor.class)
public class RobofaceProcessor extends AbstractProcessor {

	// commandline options for use with -Aoption=...
	public static final String HEADER_PATH = "roboface.headerpath";
	public static final String HEADER_NAME = "roboface.headername";
	public static final String INCLUDE_HEADERS = "roboface.include.headers";
	public static final String INHERITANCE_BLACKLIST = "roboface.inheritance.blacklist";

	// defaults for the options
	public static final String HEADER_PATH_DEFAULT = "roboheaders";
	public static final String HEADER_NAME_DEFAULT = "RoboFrameworkInterface.h";

	private JavacProcessingEnvironment jcProcEnv;

	private boolean firstPass;
	private List<ObjectDefinition> objDefs;
	private TypeRegistry registry;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		jcProcEnv = (JavacProcessingEnvironment) processingEnv;
		firstPass = true;
		objDefs = new ArrayList<>();
		registry = new TypeRegistry(getInheritanceBlacklist(jcProcEnv));
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		if (firstPass) {
			System.out.println("Executing processor.");
			firstPass = false;
		} else {
			// skipping further executions
			return true;
		}

		Set<Type.ClassType> knownRobofaceInterfaces = new HashSet<>();

		final Trees trees = Trees.instance(processingEnv);
		final TreePathScanner<Object, CompilationUnitTree> enumScanner, ifaceScanner, objScanner, implScanner;
		final Names names = Names.instance(jcProcEnv.getContext());

		enumScanner = new TreePathScanner<Object, CompilationUnitTree>() {

			@Override
			public Trees visitClass(ClassTree classTree, CompilationUnitTree unitTree) {
				if (classTree instanceof JCTree.JCClassDecl && unitTree instanceof JCCompilationUnit) {
					final JCCompilationUnit compilationUnit = (JCCompilationUnit) unitTree;
					final JCTree.JCClassDecl ccd = (JCTree.JCClassDecl) classTree;

					// Only process on files which have been compiled from source
					if (compilationUnit.sourcefile.getKind() == JavaFileObject.Kind.SOURCE) {
						String enumName = classTree.getSimpleName().toString();
						System.out.println("Processing enum " + enumName);

						// record type information for header generation
						final EnumDescriptor enumDesc = registry.createEnumDescriptor(ccd.sym.type);

						for (JCTree next : ccd.getMembers()) {
							if (next instanceof JCTree.JCVariableDecl) {
								JCTree.JCVariableDecl decl = (JCTree.JCVariableDecl) next;
								if (Flags.isEnum(decl.sym)) {
									//System.out.println("next: " + decl.name);
									enumDesc.addValue(decl.name.toString());
								}
							}
						}
					}
				}

				return trees;
			}
		};

		TreeMaker tm = TreeMaker.instance(jcProcEnv.getContext());
		List<Map.Entry<TypeDescriptor, JCTree.JCModifiers>> methodDeclarations = new LinkedList<>();

		// create ObjCProtocol type
		Symbol.ClassSymbol nsObjectProtocolSymbol = jcProcEnv.getElementUtils().getTypeElement("org.robovm.apple.foundation.NSObjectProtocol");
		JCTree.JCExpression nsObjectProtocolExp = tm.Type(nsObjectProtocolSymbol.asType());

		ifaceScanner = new TreePathScanner<Object, CompilationUnitTree>() {

			Symbol.ClassSymbol methodAttributeSym = jcProcEnv.getElementUtils().getTypeElement("org.robovm.objc.annotation.Method");
			Symbol.ClassSymbol stringClazz = jcProcEnv.getElementUtils().getTypeElement("java.lang.String");
			Symbol.MethodSymbol methodSymbol = new Symbol.MethodSymbol(PARAMETER, names.fromString("selector"), stringClazz.type, methodAttributeSym.owner);

			@Override
			public Trees visitClass(ClassTree classTree, CompilationUnitTree unitTree) {
				if (classTree instanceof JCTree.JCClassDecl && unitTree instanceof JCCompilationUnit) {
					final JCCompilationUnit compilationUnit = (JCCompilationUnit) unitTree;
					final JCTree.JCClassDecl ccd = (JCTree.JCClassDecl) classTree;

					// Only process on files which have been compiled from source
					if (compilationUnit.sourcefile.getKind() == JavaFileObject.Kind.SOURCE) {
						String ifaceName = classTree.getSimpleName().toString();
						System.out.println("Processing class " + ifaceName);

						knownRobofaceInterfaces.add((Type.ClassType)ccd.sym.type);

						// record type information for header generation
						final ProtocolDescriptor protoDesc = registry.createProtocolDescriptor(
								ccd,
								ProtocolDescriptor.IosType.Protocol);

						// add ObjCProtocol type to tree
						ccd.implementing = ccd.implementing.append(nsObjectProtocolExp);

						annotateMethodsForSelectors(ccd, protoDesc, (tree) -> {
							return tree.name.toString();
						});

					}
				}

				return trees;
			}

			private void annotateMethodsForSelectors(final JCTree.JCClassDecl ccd, final ProtocolDescriptor protoDesc,
					Function<JCTree.JCMethodDecl, String> nameResolver) {
				// find methods in this interface and add @Method annotation
				ccd.accept(new TreeTranslator() {
					@Override
					public <T extends JCTree> T translate(T tree) {
						if (tree != null && (tree.getKind() == Tree.Kind.METHOD)) {
							return super.translate(tree);
						} else {
							return tree;
						}
					}

					@Override
					public void visitMethodDef(JCTree.JCMethodDecl tree) {
						super.visitMethodDef(tree);

						// capture method definitions
						final MethodDescriptor methodDescriptor = registry.createMethodDescriptor(
								nameResolver.apply(tree),
								tree.getReturnType().type);
						protoDesc.addMethod(methodDescriptor);
						methodDeclarations.add(new AbstractMap.SimpleImmutableEntry<>(methodDescriptor.getReturnType(), tree.getModifiers()));

						for (JCTree.JCVariableDecl paramDecl : tree.params) {
							Type parameterType = paramDecl.getType().type;

							final MethodParameterDescriptor methodParamDescr = registry.createMethodParameterDescriptor(
									paramDecl.name.toString(),
									parameterType);
							methodDescriptor.addParam(methodParamDescr);

							methodDeclarations.add(new AbstractMap.SimpleImmutableEntry<>(methodParamDescr.getType(), paramDecl.getModifiers()));
						}

						// create @Method type
						Pair<Symbol.MethodSymbol,Attribute> ds = new Pair<>(methodSymbol, new Attribute.Constant(stringClazz.type, methodDescriptor.asSelector()));
						JCTree.JCAnnotation at = tm.Annotation(new Attribute.Compound(methodAttributeSym.asType(), com.sun.tools.javac.util.List.of(ds)));
						tree.mods.annotations = tree.mods.annotations.append(at);

						System.out.printf("Adding @Method annotation %s to method %s\n", at, tree.name);
					}
				});
			}
		};

		// create ObjCProtocol type
		Symbol.ClassSymbol nsObjectSymbol = jcProcEnv.getElementUtils().getTypeElement("org.robovm.apple.foundation.NSObject");
		JCTree.JCExpression nsObjectExpression = tm.Type(nsObjectSymbol.asType());

		objScanner = new TreePathScanner<Object, CompilationUnitTree>() {

			@Override
			public Trees visitClass(ClassTree classTree, CompilationUnitTree unitTree) {
				if (classTree instanceof JCTree.JCClassDecl && unitTree instanceof JCCompilationUnit) {
					final JCCompilationUnit compilationUnit = (JCCompilationUnit) unitTree;
					final JCTree.JCClassDecl ccd = (JCTree.JCClassDecl) classTree;

					// Only process on files which have been compiled from source
					if (compilationUnit.sourcefile.getKind() == JavaFileObject.Kind.SOURCE) {
						final Name simpleName = classTree.getSimpleName();
						System.out.println("Processing class " + simpleName);

						String className = ccd.sym.fullname.toString();

						// TODO: find out how to properly access the annotation on the tree
						//System.out.println(ccd.mods.getAnnotations());
						// when we hit this code, the annotation must be present, so assume it will always be set
						String factoryName = null;
						for (JCTree.JCAnnotation annotation : ccd.mods.getAnnotations()) {
							if (annotation.type.toString().equals(FrameworkObject.class.getName())) {
								for (JCTree.JCExpression arg : annotation.getArguments()) {
									JCTree.JCAssign arg1 = (JCTree.JCAssign) arg;
									JCTree.JCLiteral val = (JCTree.JCLiteral) arg1.rhs;
									factoryName = (String) val.value;
								}
							}
						}

						// record type information for header generation
						ArrayList<String> ifaces = new ArrayList<>();
						for (JCTree.JCExpression exp : ccd.getImplementsClause()) {
							//System.out.println("finding ifaces: " + exp.getClass());
							if (exp instanceof JCTree.JCFieldAccess) {
								JCTree.JCFieldAccess exp1 = (JCTree.JCFieldAccess) exp;
								ifaces.add(exp1.getIdentifier().toString());
							} else {
								ifaces.add(exp.toString());
							}
						}


						// record type information for header generation
						final ProtocolDescriptor protoDesc = registry.createProtocolDescriptor(
								ccd,
								ProtocolDescriptor.IosType.Interface);

						final ObjectDefinition objDef = new ObjectDefinition(className, factoryName, protoDesc);

						objDefs.add(objDef);

						// add NSObject type to tree
						ccd.extending = nsObjectExpression;
					}
				}

				return trees;
			}
		};

		Types types = Types.instance(jcProcEnv.getContext());


		Predicate<Type.ClassType> mustExtendNSObject = someType -> {
			for (Type.ClassType knownRobofaceProtocolInterfaces : knownRobofaceInterfaces) {

				if (types.isSubtype(someType, knownRobofaceProtocolInterfaces)) {
					return true;
				}
			}

			return types.isSubtype(someType, nsObjectProtocolSymbol.asType());
		};

		implScanner = new TreePathScanner<Object, CompilationUnitTree>() {

			@Override
			public Trees visitClass(ClassTree classTree, CompilationUnitTree unitTree) {

				if (classTree instanceof JCTree.JCClassDecl && unitTree instanceof JCCompilationUnit) {
					final JCCompilationUnit compilationUnit = (JCCompilationUnit) unitTree;
					final JCTree.JCClassDecl ccd = (JCTree.JCClassDecl) classTree;

					// Only process on files which have been compiled from source
					if (compilationUnit.sourcefile.getKind() == JavaFileObject.Kind.SOURCE && !ccd.sym.isInterface()) {
						boolean extendsPublicRobofaceInterface = scanInterfaces(ccd, mustExtendNSObject);
						if (extendsPublicRobofaceInterface && ccd.extending == null) {
							System.out.printf("Modifying class %s to inherit from NSObject, because it must be bindable to iOS.\n", ccd.sym);
							ccd.extending = nsObjectExpression;
						}
					}
				}
				return trees;
			}

			private boolean scanInterfaces(final JCTree.JCClassDecl ccdm, Predicate<Type.ClassType> checker) {

				for (JCTree.JCExpression implmentStatement : ccdm.implementing) {

					Type.ClassType currentType = (Type.ClassType)implmentStatement.type;

					if (checker.test(currentType)) {
						return true;
					}


				}
				return false;
			}
		};
		try {

			for (final Element element : env.getElementsAnnotatedWith(FrameworkEnum.class)) {
				if (element.getKind() == ElementKind.ENUM) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
							"@FrameworkEnum is being applied to an enum.", element);

					final TreePath path = trees.getPath(element);
					//processingEnv.getElementUtils().printElements(new OutputStreamWriter(System.out), element);
					enumScanner.scan(path, path.getCompilationUnit());
				} else {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
							"@FrameworkEnum must only be applied to enums.", element);
				}
			}

			for (final Element element : env.getElementsAnnotatedWith(FrameworkInterface.class)) {
				if (element.getKind() == ElementKind.INTERFACE) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
							"@FrameworkInterface is being applied to an interface.", element);

					final TreePath path = trees.getPath(element);
					//processingEnv.getElementUtils().printElements(new OutputStreamWriter(System.out), element);
					ifaceScanner.scan(path, path.getCompilationUnit());
				} else {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
							"@FrameworkInterface must only be applied to interfaces.", element);
				}
			}

			for (final Element element : env.getElementsAnnotatedWith(FrameworkObject.class)) {
				if (element.getKind() == ElementKind.CLASS) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
							"@FrameworkObject is being applied to a class.", element);

					final TreePath path = trees.getPath(element);
					//processingEnv.getElementUtils().printElements(new OutputStreamWriter(System.out), element);
					objScanner.scan(path, path.getCompilationUnit());
				} else {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
							"@FrameworkObject must only be applied to classes.", element);
				}
			}

			for (final Element element : env.getRootElements()) {
				if (element.getKind() == ElementKind.CLASS) {

					final TreePath path = trees.getPath(element);

					implScanner.scan(path, path.getCompilationUnit());
				}
			}

			insertMarshallers(methodDeclarations, tm);

			if (!registry.getProtocols().isEmpty()) {
				try {
					String headerPath = processingEnv.getOptions().getOrDefault(HEADER_PATH, HEADER_PATH_DEFAULT);
					String headerName = processingEnv.getOptions().getOrDefault(HEADER_NAME, HEADER_NAME_DEFAULT);
					List<IncludeHeaderDefinition> includeHeaders = this.getIncludeHeaders();
					HeaderGenerator h = new HeaderGenerator(objDefs, includeHeaders, registry);
					FileObject f = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, headerPath, headerName);
					h.writeHeader(f.openWriter());
				} catch (IOException ex) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error writing native header: " + ex.getMessage());
				}
			}
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}

		// claim annotation and prevent further processing
		return true;
	}

	private void insertMarshallers(List<Map.Entry<TypeDescriptor, JCTree.JCModifiers>> methodDeclarations, TreeMaker tm) {
		Map<String, Symbol.ClassSymbol> marshallerByName = new HashMap<>();
		Symbol.ClassSymbol classClazz = jcProcEnv.getElementUtils().getTypeElement("java.lang.Class");
		Symbol.ClassSymbol marshallerClass = jcProcEnv.getElementUtils().getTypeElement("org.robovm.rt.bro.annotation.Marshaler");
		Names names = Names.instance(jcProcEnv.getContext());
		Types types = Types.instance(jcProcEnv.getContext());
		Symbol.MethodSymbol methodSymbol = new Symbol.MethodSymbol(PARAMETER, names.value, classClazz.type, marshallerClass.owner);
		for (Map.Entry<TypeDescriptor, JCTree.JCModifiers> methodDeclaration : methodDeclarations) {
			final TypeDescriptor type = methodDeclaration.getKey();
			String marshaller = type.marshaller();
			if (marshaller != null) {
				JCTree.JCModifiers mods = methodDeclaration.getValue();

				Symbol.ClassSymbol marshallerClassSym = marshallerByName.get(marshaller);
				if (marshallerClassSym == null) {
					marshallerClassSym = jcProcEnv.getElementUtils().getTypeElement(marshaller);
					marshallerByName.put(marshaller, marshallerClassSym);
				} else {
				}
				Pair<Symbol.MethodSymbol,Attribute> ds = new Pair<>(methodSymbol, new Attribute.Class(types, marshallerClassSym.type));

				JCTree.JCAnnotation at = tm.Annotation(new Attribute.Compound(marshallerClass.asType(), com.sun.tools.javac.util.List.of(ds)));
				System.out.printf("Conversion to iOS type %s requires marshaller annotation %s\n", type.getIosType(), at);
				mods.annotations = mods.annotations.append(at);
			}
		}
	}

	private List<IncludeHeaderDefinition> getIncludeHeaders() {

		String headerIncludes = processingEnv.getOptions().getOrDefault(INCLUDE_HEADERS, "");
		List<IncludeHeaderDefinition> result = new ArrayList<>();

		for (String rawPath : headerIncludes.split(";")) {
			String trimmedPath = rawPath.trim();
			if (!trimmedPath.isBlank()) {
				result.add(new IncludeHeaderDefinition(trimmedPath));
			}
		}
		return result;
	}

	private static Set<String> getInheritanceBlacklist(JavacProcessingEnvironment processingEnv) {

		String rawInheritanceBlacklist = processingEnv.getOptions().getOrDefault(INHERITANCE_BLACKLIST, "");

		Set<String> result = new HashSet<>();
		for (String rawClassPath : rawInheritanceBlacklist.split(";")) {
			String trimmedClassPath = rawClassPath.trim();
			if (!trimmedClassPath.isBlank()) {
				result.add(trimmedClassPath);
			}
		}
		return result;
	}
}
