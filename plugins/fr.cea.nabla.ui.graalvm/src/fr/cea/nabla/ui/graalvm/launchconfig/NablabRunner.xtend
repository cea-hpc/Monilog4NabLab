/*******************************************************************************
 * Copyright (c) 2020 CEA
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * Contributors: see AUTHORS file
 *******************************************************************************/
package fr.cea.nabla.ui.graalvm.launchconfig

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Singleton
import fr.cea.nabla.generator.NablaGeneratorMessageDispatcher.MessageType
import fr.cea.nabla.nablagen.NablagenApplication
import fr.cea.nabla.ui.console.NabLabConsoleFactory
import fr.cea.nabla.ui.console.NabLabConsoleHandler
import java.io.File
import java.util.Map
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.Path
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.gemoc.monilog.api.MoniLogLibraryLocator
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import org.graalvm.polyglot.Source
import fr.cea.nabla.ui.console.NabLabConsoleRunnable
import org.graalvm.polyglot.PolyglotException
import fr.cea.nabla.ir.IrUtils

@Singleton
class NablabRunner {
	@Inject Provider<XtextResourceSet> resourceSetProvider
	@Inject NabLabConsoleFactory consoleFactory

	val Engine engine = Engine.newBuilder().build()
	
	package def launch(ILaunchConfiguration configuration) {
		val project = NablabLaunchConstants::getProject(configuration)
		val ngenFile = NablabLaunchConstants::getFile(project, configuration, NablabLaunchConstants.NGEN_FILE_LOCATION)
		val ngenPath = ngenFile.rawLocation.toString
		val nPath = newHashSet
		val jsonPath = NablabLaunchConstants::getFile(project, configuration, NablabLaunchConstants.JSON_FILE_LOCATION).
			rawLocation.toString
		val pythonExecPath = configuration.getAttribute(NablabLaunchConstants.PYTHON_EXEC_LOCATION, '')
		val wsLocation = "file:" + ResourcesPlugin.workspace.root.location

		if(ngenFile === null || !ngenFile.exists) throw new RuntimeException("Invalid file: " + ngenFile.fullPath)
		val plaftormUri = URI::createPlatformResourceURI(ngenFile.project.name + '/' + ngenFile.projectRelativePath,
			true)
		val resourceSet = resourceSetProvider.get
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
		val uriMap = resourceSet.URIConverter.URIMap
		uriMap.put(URI::createURI('platform:/resource/fr.cea.nabla/'), URI::createURI('platform:/plugin/fr.cea.nabla/'))
		val emfResource = resourceSet.createResource(plaftormUri)
		emfResource.load(null)
		EcoreUtil::resolveAll(resourceSet)

		val ngenApp = emfResource.contents.filter(NablagenApplication).head
		nPath += ngenApp.mainModule.type.findPath(wsLocation)
		ngenApp.additionalModules.forEach[m|nPath += m.type.findPath(wsLocation)]
		
		val configs = ngenApp.targets.map[t|t.extensionConfigs].flatten
		configs.forEach [ e |
			nPath += e.extension.findPath(wsLocation)
			nPath += e.provider.findPath(wsLocation)
		]

		val moniloggers = configuration.getAttribute(NablabLaunchConstants::MONILOGGER_FILES_LOCATIONS, newArrayList).
			filter[s|!s.blank].map [ m |
				ResourcesPlugin.workspace.root.getFile(new Path(m)).rawLocation.makeAbsolute.toString
			]

		val Map<String, String> optionsMap = newHashMap
		if (!moniloggers.empty) {
			val urls = MoniLogLibraryLocator.locate().stream.distinct
			val moniloggerFiles = moniloggers.reduce[s1, s2|s1 + ', ' + s2] + ", " +
				urls.reduce[s1, s2|s1 + ', ' + s2].get
			optionsMap.put("monilogger.files", moniloggerFiles)
			if (pythonExecPath !== null && !pythonExecPath.blank) {
				optionsMap.put("python.Executable", pythonExecPath)
			}
			optionsMap.put("python.ForceImportSite", "true")
		}

		val executionThread = new Thread([
			val parent = Thread.currentThread.contextClassLoader
			val hostClassLoader = new ExtensionClassLoader(#{"org.gemoc.monilog.appender", "org.gemoc.monilog.layout"},
				parent)
			Thread.currentThread.contextClassLoader = hostClassLoader
			try (val context = Context.newBuilder().engine(engine) //
					.allowAllAccess(true)
					.option("nabla.wd", wsLocation)
					.option("nabla.np", nPath.reduce[s1, s2|s1 + ':' + s2])
					.option("nabla.options", jsonPath)
					.options(optionsMap)
					.logHandler(new NabLabConsoleHandler(consoleFactory))
					.build()
			) {
				val Thread interpreterThread = new Thread([
					consoleFactory.printConsole(MessageType.Exec, "Starting code interpretation")
					val startTime = System::nanoTime
					val nablaSource = Source.newBuilder('nabla', new File(ngenPath)).build()
					val parsedSource = context.parse(nablaSource)
					try {
						parsedSource.execute
						val executionTime = (System::nanoTime - startTime) * 0.000000001
						consoleFactory.printConsole(MessageType.Exec, "Code interpretation ended in " + executionTime + "s")
						consoleFactory.printConsole(MessageType.End, "Interpretation ended successfully for: " + ngenFile.name)
					} catch (PolyglotException e) {
						if (e.cancelled) {
							consoleFactory.printConsole(MessageType.Error, "Interpretation interrupted for " + ngenFile.name)
						} else {
							consoleFactory.printConsole(MessageType.Error, "Interpretation failed for: " + ngenFile.name)
							consoleFactory.printConsole(MessageType.Error, e.message)
							consoleFactory.printConsole(MessageType.Error, IrUtils.getStackTrace(e))
						}
					} catch (Exception e) {
						consoleFactory.printConsole(MessageType.Error, "Interpretation failed for: " + ngenFile.name)
						consoleFactory.printConsole(MessageType.Error, e.message)
						consoleFactory.printConsole(MessageType.Error, IrUtils.getStackTrace(e))
					} finally {
						Thread.currentThread.contextClassLoader = parent
					}
				])
				val Runnable stopRunnable = [
					context.close(true)
				]
				new NabLabConsoleRunnable(consoleFactory, interpreterThread, stopRunnable).run
			} catch (Exception e) {
				e.printStackTrace
				consoleFactory.printConsole(MessageType.Error, e.message)
			}
		])
		executionThread.start
	}

	private def String findPath(EObject element, String wsPath) {
		val wsRelativePath = element.eResource.URI.toPlatformString(true)
		return ResourcesPlugin.workspace.root.findMember(wsRelativePath).locationURI.rawPath
	}
}
