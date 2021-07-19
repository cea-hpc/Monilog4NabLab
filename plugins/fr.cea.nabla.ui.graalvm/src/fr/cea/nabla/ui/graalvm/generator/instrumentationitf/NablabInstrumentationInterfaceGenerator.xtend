package fr.cea.nabla.ui.graalvm.generator.instrumentationitf

import com.google.inject.Inject
import fr.cea.nabla.generator.NablaGeneratorMessageDispatcher.MessageType
import fr.cea.nabla.generator.StandaloneGeneratorBase
import fr.cea.nabla.generator.ir.IrRootBuilder
import fr.cea.nabla.ir.ir.Affectation
import fr.cea.nabla.ir.ir.ArgOrVar
import fr.cea.nabla.ir.ir.Function
import fr.cea.nabla.ir.ir.IrRoot
import fr.cea.nabla.ir.ir.Job
import fr.cea.nabla.nablagen.NablagenApplication
import instrumentationInterface.InstrumentationInterface
import instrumentationInterface.InstrumentationInterfaceFactory
import java.util.HashSet

class NablabInstrumentationInterfaceGenerator extends StandaloneGeneratorBase {
	@Inject IrRootBuilder irRootBuilder

	val InstrumentationInterfaceWriter instrumentationInterfaceWriter = new InstrumentationInterfaceWriter
	val InstrumentationInterfaceFactory factory = InstrumentationInterfaceFactory.eINSTANCE

	def void generateInstrumentationInterface(NablagenApplication ngenApp, String wsPath, String projectName) {
		try {
			val ir = irRootBuilder.buildGeneratorGenericIr(ngenApp)
			dispatcher.post(MessageType.Exec, "Starting instrumentation interface generation")
			val startTime = System.currentTimeMillis

			val projectPath = wsPath + '/' + projectName
			var fsa = getConfiguredFileSystemAccess(projectPath, true)

			val instrumentationInterface = getInstrumentationInterfaceContent(ir)

			val fileName = instrumentationInterfaceWriter.createAndSaveResource(fsa, instrumentationInterface)
			dispatcher.post(MessageType::Exec, '    Resource saved: ' + fileName)

			val endTime = System.currentTimeMillis
			dispatcher.post(MessageType.Exec,
				"Instrumentation interface generation ended in " + (endTime - startTime) / 1000.0 + "s")
		} catch (Exception e) {
			dispatcher.post(MessageType::Error, '\n***' + e.class.name + ': ' + e.message)
			if (e.stackTrace !== null && !e.stackTrace.empty) {
				val stack = e.stackTrace.head
				dispatcher.post(MessageType::Error,
					'at ' + stack.className + '.' + stack.methodName + '(' + stack.fileName + ':' + stack.lineNumber + ')')
			}
			throw (e)
		}
	}

	def InstrumentationInterface getInstrumentationInterfaceContent(IrRoot ir) {

		val resource = ir.eResource

		factory.createInstrumentationInterface => [
			instrumentableElements += resource.allContents.filter[o|o instanceof Job].map[o|o as Job].map [ j |
				factory.createCallableElement => [
					name = j.name
				]
			].toList

			instrumentableElements += resource.allContents.filter[o|o instanceof Function].map[o|o as Function].map [ f |
				factory.createCallableElement => [
					name = f.name
				]
			].toList

			val writableNames = new HashSet<String>

			instrumentableElements +=
				resource.allContents.filter[o|o instanceof Affectation].map[o|o as Affectation].map[a|a.left.target].
					toSet.map [ v |
						writableNames.add(v.name)
						factory.createWriteableElement => [
							name = v.name
						]
					].toList

			readableElements += resource.allContents.filter[o|o instanceof ArgOrVar].map[o|o as ArgOrVar].filter [ v |
				!writableNames.contains(v.name)
			].map [ v |
				factory.createReadableElement => [
					name = v.name
				]
			].toList
		]
	}

}
