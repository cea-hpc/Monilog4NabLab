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

import java.util.ArrayList
import java.util.List
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.ui.DebugUITools
import org.eclipse.debug.ui.ILaunchShortcut
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.jface.viewers.TreeSelection
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.PlatformUI
import org.eclipse.xtext.ui.editor.XtextEditor

class NablabShortcut implements ILaunchShortcut {
	override launch(ISelection selection, String mode) {
		if (selection !== null && selection instanceof TreeSelection) {
			val elt = (selection as TreeSelection).firstElement
			if (elt !== null && elt instanceof IResource)
				launchGeneration(elt as IResource, mode)
		}
	}

	override launch(IEditorPart editor, String mode) {
		if (editor instanceof XtextEditor) {
			val resource = editor.resource
			if (resource !== null)
				launchGeneration(resource, mode)
		}
	}

	private def launchGeneration(IResource file, String mode) {
		var canLaunch = true
		val errors = newArrayList
		if (file instanceof IFile) {
			// try to save dirty editors
			PlatformUI::workbench.activeWorkbenchWindow.activePage.saveAllEditors(true)

			val manager = DebugPlugin::^default.launchManager
			val type = manager.getLaunchConfigurationType(NablabLaunchConstants.LAUNCH_CONFIGURATION_TYPE_ID)
			var ILaunchConfigurationWorkingCopy configuration = null
			var configurations = getLaunchConfigurations(file)

			if (configurations.size == 0) {
				// no configuration found, create new one
				try {
					configuration = type.newInstance(null, file.fullPath.removeFileExtension.lastSegment)
					configuration.setAttribute(NablabLaunchConstants.PROJECT, file.project.name)
					configuration.setAttribute(NablabLaunchConstants.NGEN_FILE_LOCATION, file.projectRelativePath.toPortableString)
					configuration.setAttribute(NablabLaunchConstants.JSON_FILE_LOCATION, file.projectRelativePath.toPortableString.replace(".ngen", ".json"))
					
					// save and return new configuration
					configuration.doSave

					configurations = #[configuration]
				} catch (CoreException e) {
					canLaunch = false
				}
			}

			if (canLaunch) {
				// launch
				configurations.get(0).launch(mode, new NullProgressMonitor())
			} else {
				// open error dialog
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Could not generate complete launch configuration", getErrorMessage(errors).toString
				)
				// open launch configuration dialog
				if (configuration === null) {
					DebugUITools.openLaunchConfigurationDialogOnGroup(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						new StructuredSelection(type), "org.eclipse.debug.ui.launchGroup.run")
				} else {
					DebugUITools.openLaunchConfigurationDialogOnGroup(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						new StructuredSelection(configuration),
						DebugUITools.getLaunchGroup(configuration, mode).getIdentifier(),
						null)
				}
			}
		}
	}
	
	private def getErrorMessage(List<String> errors) {
		'''
			«IF errors.empty»
				The launch configuration could not be generated and must be created manually.
			«ELSE»
				The launch configuration could not be generated because:
				«FOR error : errors SEPARATOR ',\n' AFTER '.'»«error»«ENDFOR»
			«ENDIF»
		'''
	}

	/**
	 * Get all launch configurations that target a dedicated resource file.
	 * 
	 * @param resource root file to execute
	 * @return {@link ILaunchConfiguration}s using resource
	 */
	private def ILaunchConfiguration[] getLaunchConfigurations(IResource resource) {
		val configurations = new ArrayList<ILaunchConfiguration>
		val manager = DebugPlugin.^default.launchManager
		val type = manager.getLaunchConfigurationType(NablabLaunchConstants::LAUNCH_CONFIGURATION_TYPE_ID)

		// try to find existing configurations using the same file
		try {
			for (ILaunchConfiguration configuration : manager.getLaunchConfigurations(type)) {
				try {
					val project = NablabLaunchConstants::getProject(configuration)
					val file = NablabLaunchConstants::getFile(project, configuration, NablabLaunchConstants::NGEN_FILE_LOCATION)
					if (resource.equals(file))
						configurations.add(configuration)
				} catch (CoreException e) {
					// could not read configuration, ignore
				}
			}
		} catch (CoreException e) {
			// could not load configurations, ignore
		}

		return configurations
	}
}
