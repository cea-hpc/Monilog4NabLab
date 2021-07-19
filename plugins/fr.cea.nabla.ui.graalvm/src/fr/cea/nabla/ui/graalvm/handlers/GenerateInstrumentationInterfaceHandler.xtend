package fr.cea.nabla.ui.graalvm.handlers

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Singleton
import fr.cea.nabla.generator.NablaGeneratorMessageDispatcher
import fr.cea.nabla.generator.NablaGeneratorMessageDispatcher.MessageType
import fr.cea.nabla.ir.IrUtils
import fr.cea.nabla.nablagen.NablagenApplication
import fr.cea.nabla.ui.console.NabLabConsoleFactory
import fr.cea.nabla.ui.graalvm.generator.instrumentationitf.NablabInstrumentationInterfaceGenerator
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.jface.viewers.TreeSelection
import org.eclipse.swt.widgets.Shell
import org.eclipse.ui.handlers.HandlerUtil

@Singleton
class GenerateInstrumentationInterfaceHandler extends AbstractHandler {
	@Inject Provider<ResourceSet> resourceSetProvider
	@Inject Provider<NablabInstrumentationInterfaceGenerator> instrumentationInterfaceGeneratorProvider

	val traceFunction = [MessageType type, String msg|consoleFactory.printConsole(type, msg)]

	@Inject NabLabConsoleFactory consoleFactory
	@Inject NablaGeneratorMessageDispatcher dispatcher

	override execute(ExecutionEvent event) throws ExecutionException
	{
		val selection = HandlerUtil::getActiveMenuSelection(event)
		if (selection !== null && selection instanceof TreeSelection) {
			val elt = (selection as TreeSelection).firstElement
			if (elt instanceof IFile) {
				val shell = HandlerUtil::getActiveShell(event)
				generate(elt, shell)
			}
		}
		return selection
	}

	private def generate(IFile nablagenFile, Shell shell) {
		new Thread([
			try {
				dispatcher.traceListeners += traceFunction
				consoleFactory.openConsole
				consoleFactory.printConsole(MessageType.Start, "Starting generation process for: " + nablagenFile.name)
				consoleFactory.printConsole(MessageType.Exec, "Loading resources (.n and .ngen)")
				val project = nablagenFile.project
				val plaftormUri = URI::createPlatformResourceURI(project.name + '/' + nablagenFile.projectRelativePath,
					true)
				val resourceSet = resourceSetProvider.get
				val uriMap = resourceSet.URIConverter.URIMap
				uriMap.put(URI::createURI('platform:/resource/fr.cea.nabla/'),
					URI::createURI('platform:/plugin/fr.cea.nabla/'))
				val emfResource = resourceSet.createResource(plaftormUri)
				EcoreUtil::resolveAll(resourceSet)
				emfResource.load(null)

				val ngen = emfResource.contents.filter(NablagenApplication).head
				if (ngen !== null) {
					val projectFolder = ResourcesPlugin.workspace.root.getFolder(project.location)
					val wsPath = projectFolder.parent.fullPath.toString
					instrumentationInterfaceGeneratorProvider.get.
						generateInstrumentationInterface(ngen, wsPath, project.name)
	
					project.refreshLocal(IResource::DEPTH_INFINITE, null)
					consoleFactory.printConsole(MessageType.End, "Generation ended successfully for: " + nablagenFile.name)
				} else {
					consoleFactory.printConsole(MessageType.Error, "Generation failed for: " + nablagenFile.name)
					consoleFactory.printConsole(MessageType.Error, "No application found in " + nablagenFile.name)
				}
			} catch (Exception e) {
				consoleFactory.printConsole(MessageType.Error, "Generation failed for: " + nablagenFile.name)
				consoleFactory.printConsole(MessageType.Error, e.message)
				consoleFactory.printConsole(MessageType.Error, IrUtils.getStackTrace(e))
			} finally {
				dispatcher.traceListeners -= traceFunction
			}
		]).start
	}
}
