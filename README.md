Scope
=====

This repository provides a Java annotation processor and the respective annotations, which can be used to transform Java
interfaces in a way that they are usable as protocols in ObjectiveC and Swift. It can also modify classes in a way that
they can be used as Framework entry points.

During the manipulation of the annotated classes, an ObjectiveC header containing the definitions is emitted.


Usage
=====

First of all, the `@FrameworkInterface` and `@FrameworkObject` annotations should be added to the interfaces and classes
which shall be exposed to the native world. Interfaces are not processed recursively and therefore any super interfaces
must be annotated explicitly if their methods shall be exposed as well. Framework objects must implement exactly one of
the exposed interfaces as this protocol will be used to identify the type of the object. After successful compilation
there will be a ObjectiveC header file named `FrameworkIface.h` in the target direktory of the compiler.

The class of the annotation processor is `org.openecard.robovm.processor.RobofaceProcessor` and can be used in the usual
fashion as an annotation processer. Also note that it is necessary to include `robovm-cocoatouch` and `robovm-rt` as
dependencies or the compilation of the modified code will fail.

When using a maven setup, the compiler plugin and the dependencies should look as follows:

```XML
<properties>
	<robovm.version>2.3.7</robovm.version>
	<roboface.version>1.0.0</roboface.version>
</properties>

<build>
	<pluginManagement>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<annotationProcessorPaths>
						<annotationProcessorPath>
							<groupId>org.openecard.tools</groupId>
							<artifactId>roboface-processor</artifactId>
							<version>${roboface.version}</version>
						</annotationProcessorPath>
					</annotationProcessorPaths>
					<annotationProcessors>
						<proc>org.openecard.robovm.processor.RobofaceProcessor</proc>
					</annotationProcessors>
				</configuration>
			</plugin>
		</plugins>
	</pluginManagement>
</build>

<dependencies>
	<dependency>
		<groupId>org.openecard.tools</groupId>
		<artifactId>roboface-annotation</artifactId>
		<version>${roboface.version}</version>
		<optional>true</optional>
	</dependency>

	<dependency>
		<groupId>com.mobidevelop.robovm</groupId>
		<artifactId>robovm-rt</artifactId>
		<version>${robovm.version}</version>
		<scope>provided</scope>
	</dependency>
	<dependency>
		<groupId>com.mobidevelop.robovm</groupId>
		<artifactId>robovm-cocoatouch</artifactId>
		<version>${robovm.version}</version>
		<scope>provided</scope>
	</dependency>
</dependencies>
```
