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

import fr.cea.nabla.ui.launchconfig.JsonFileSelectionAdapter
import fr.cea.nabla.ui.launchconfig.NablagenFileSelectionAdapter
import fr.cea.nabla.ui.launchconfig.NablagenProjectSelectionAdapter
import fr.cea.nabla.ui.launchconfig.SourceFileContentProvider
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab
import org.eclipse.jface.window.Window
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Group
import org.eclipse.swt.widgets.Text
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog
import org.eclipse.ui.model.WorkbenchLabelProvider

class NablabLaunchConfigurationMainTab extends AbstractLaunchConfigurationTab
{
	public static val SourceFileExtension = 'n'
	public static val GenFileExtension = 'ngen'
	public static val OptionsFileExtension = 'json'
	public static val MoniloggerFileExtension = 'mnlg'
	boolean fDisableUpdate = false

	Text fTxtProject
	Text fTxtNGenFile
	Text fTxtOptionsFile
	Text fTxtMoniloggerFile
	Text fTxtPythonExec

	/*
	 * TODO: add advanced options where you can specify the path to the Python venv you wish to use.
	 */

	override createControl(Composite parent) 
	{
		val topControl = new Composite(parent, SWT.NONE)
		topControl.setLayout(new GridLayout(1, false))

		val grpProject = new Group(topControl, SWT.NONE)
		grpProject.setLayout(new GridLayout(2, false))
		grpProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))
		grpProject.setText("Project")

		fTxtProject = new Text(grpProject, SWT.BORDER)
		fTxtProject.addModifyListener([e | if (!fDisableUpdate) updateLaunchConfigurationDialog])
		fTxtProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))
		
		val fBtnBrowseProject = new Button(grpProject, SWT.NONE)
		fBtnBrowseProject.addSelectionListener(new NablagenProjectSelectionAdapter(parent, fTxtProject))
		fBtnBrowseProject.setText("Browse...");

		val grpGen = new Group(topControl, SWT.NONE)
		grpGen.setLayout(new GridLayout(2, false))
		grpGen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))
		grpGen.setText("Nablagen File")

		fTxtNGenFile = new Text(grpGen, SWT.BORDER)
		fTxtNGenFile.addModifyListener([e | if (!fDisableUpdate) updateLaunchConfigurationDialog])
		fTxtNGenFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))

		val btnBrowseGen = new Button(grpGen, SWT::NONE)
		btnBrowseGen.addSelectionListener(new NablagenFileSelectionAdapter(parent, fTxtNGenFile))
		btnBrowseGen.setText("Browse...")

		val grpOptions = new Group(topControl, SWT.NONE)
		grpOptions.setLayout(new GridLayout(2, false))
		grpOptions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))
		grpOptions.setText("Json File")

		fTxtOptionsFile = new Text(grpOptions, SWT.BORDER)
		fTxtOptionsFile.addModifyListener([e | if (!fDisableUpdate) updateLaunchConfigurationDialog])
		fTxtOptionsFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))

		val btnBrowseOptions = new Button(grpOptions, SWT::NONE)
		btnBrowseOptions.addSelectionListener(new JsonFileSelectionAdapter(parent, fTxtOptionsFile))
		btnBrowseOptions.setText("Browse...")

		val grpMonilogger = new Group(topControl, SWT.NONE)
		grpMonilogger.setLayout(new GridLayout(2, false))
		grpMonilogger.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))
		grpMonilogger.setText("MoniLog File")

		fTxtMoniloggerFile = new Text(grpMonilogger, SWT.BORDER)
		fTxtMoniloggerFile.addModifyListener([e | if (!fDisableUpdate) updateLaunchConfigurationDialog])
		fTxtMoniloggerFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))

		val btnBrowseMonilogger = new Button(grpMonilogger, SWT::NONE)
		btnBrowseMonilogger.addSelectionListener(new MoniloggerFileSelectionAdapter(parent, fTxtMoniloggerFile))
		btnBrowseMonilogger.setText("Browse...")
		
		val grpPythonExec = new Group(topControl, SWT.NONE)
		grpPythonExec.setLayout(new GridLayout(2, false))
		grpPythonExec.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))
		grpPythonExec.setText("Python Executable")

		fTxtPythonExec = new Text(grpPythonExec, SWT.BORDER)
		fTxtPythonExec.addModifyListener([e | if (!fDisableUpdate) updateLaunchConfigurationDialog])
		fTxtPythonExec.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1))

		setControl(topControl)
	}
	
	override getName()
	{
		'Global'
	}

	override initializeFrom(ILaunchConfiguration configuration)
	{
		fDisableUpdate = true

		fTxtNGenFile.text = ''
		fTxtOptionsFile.text = ''
		fTxtMoniloggerFile.text = ''
		fTxtPythonExec.text = ''

		try
		{
			fTxtProject.text = configuration.getAttribute(NablabLaunchConstants::PROJECT, '')
			fTxtNGenFile.text = configuration.getAttribute(NablabLaunchConstants::NGEN_FILE_LOCATION, '')
			fTxtOptionsFile.text = configuration.getAttribute(NablabLaunchConstants::JSON_FILE_LOCATION, '')
			val moniloggers = configuration.getAttribute(NablabLaunchConstants::MONILOGGER_FILES_LOCATIONS, newArrayList)
			fTxtMoniloggerFile.text = if (moniloggers.empty) '' else moniloggers.head
			fTxtPythonExec.text = configuration.getAttribute(NablabLaunchConstants::PYTHON_EXEC_LOCATION, '')
		}
		catch (CoreException e)
		{
		}
		fDisableUpdate = false
	}

	override performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(NablabLaunchConstants::PROJECT, fTxtProject.text)
		configuration.setAttribute(NablabLaunchConstants::NGEN_FILE_LOCATION, fTxtNGenFile.text)
		configuration.setAttribute(NablabLaunchConstants::JSON_FILE_LOCATION, fTxtOptionsFile.text)
		configuration.setAttribute(NablabLaunchConstants::MONILOGGER_FILES_LOCATIONS, newArrayList(fTxtMoniloggerFile.text))
		configuration.setAttribute(NablabLaunchConstants::PYTHON_EXEC_LOCATION, fTxtPythonExec.text)
	}

	override setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(NablabLaunchConstants::PROJECT, '')
		configuration.setAttribute(NablabLaunchConstants::NGEN_FILE_LOCATION, '')
		configuration.setAttribute(NablabLaunchConstants::JSON_FILE_LOCATION, '')
		configuration.setAttribute(NablabLaunchConstants::MONILOGGER_FILES_LOCATIONS, newArrayList)
		configuration.setAttribute(NablabLaunchConstants::PYTHON_EXEC_LOCATION, '')
	}
}

class MoniloggerFileSelectionAdapter extends SelectionAdapter
{
	val Composite parent
	val Text fTxtFile

	new(Composite parent, Text fTxtFile)
	{
		this.parent = parent
		this.fTxtFile = fTxtFile
	}

	override void widgetSelected(SelectionEvent e)
	{
		val dialog = new ElementTreeSelectionDialog(parent.shell, new WorkbenchLabelProvider, new SourceFileContentProvider(NablabLaunchConfigurationMainTab::MoniloggerFileExtension))
		dialog.setTitle("Select Monilogger File")
		dialog.setMessage("Select a monilogger file to add to the execution:")
		dialog.setInput(ResourcesPlugin.workspace.root)
		if (dialog.open == Window.OK)
			fTxtFile.setText((dialog.firstResult as IFile).fullPath.makeRelative.toPortableString)
	}
}
