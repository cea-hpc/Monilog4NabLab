package fr.cea.nabla.ui.graalvm.p2.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.progress.UIJob;

public class ExecuteAction extends ProvisioningAction {

	private static final String GRAAL_VM_URL = "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.0.0/graalvm-ce-java11-linux-amd64-21.0.0.tar.gz";
	private static final String GRAALVM_FOLDER = "graalvm-ce-java11-21.0.0";
	private static final String GRAALVM_ARCHIVE = "graalvm-ce-java11-linux-amd64-21.0.0.tar.gz";
	public static final String PLUGIN_ID = "fr.cea.nabla.ui.graalvm.p2.actions";
	public static final String INSTALL_FOLDER = "installFolder";
	public static final String ARTIFACT = "artifact";
	public static final String PROGRAM = "program";

	private void openDownloadingDialog() {
		
		final UIJob downloadingJob = new UIJob("Downloading GraalVM...") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						DownloadingDialog downloadingDialog = new DownloadingDialog(getDisplay().getActiveShell(), 0);
						downloadingDialog.open();
					}
				});
				return Status.OK_STATUS;
			}
		};

		downloadingJob.runInUIThread(new NullProgressMonitor());
	}

	private void openDownloadedDialog() {
		final UIJob downloadedJob = new UIJob("GraalVM downloaded.") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						DownloadedDialog downloadedDialog = new DownloadedDialog(getDisplay().getActiveShell());
						downloadedDialog.open();
					}
				});
				return Status.OK_STATUS;
			}
		};

		downloadedJob.runInUIThread(new NullProgressMonitor());
	}
	
	private void openDialog(String title, String message, int dialogType) {
		final UIJob uiJob = new UIJob(title) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						switch (dialogType) {
						case MessageDialog.CONFIRM:
							MessageDialog.openConfirm(getDisplay().getActiveShell(), title, message);
							break;
						case MessageDialog.ERROR:
							MessageDialog.openError(getDisplay().getActiveShell(), title, message);
							break;
						case MessageDialog.INFORMATION:
							MessageDialog.openInformation(getDisplay().getActiveShell(), title, message);
							break;
						case MessageDialog.QUESTION:
							MessageDialog.openQuestion(getDisplay().getActiveShell(), title, message);
							break;
						case MessageDialog.WARNING:
							MessageDialog.openWarning(getDisplay().getActiveShell(), title, message);
							break;
						}
					}
				});
				return Status.OK_STATUS;
			}
		};

		uiJob.runInUIThread(new NullProgressMonitor());
	}
	
	public IStatus execute(Map<String, Object> parameters) {
		final String installFolder = (String) parameters.get(INSTALL_FOLDER);
		final String artifact = (String) parameters.get(ARTIFACT);
		final String program = (String) parameters.get(PROGRAM);
		final String fullPath = computeProgramPath(artifact, program);
		final String nabLabComponentPath = computeNablaComponentPath(artifact);
		final String moniLogJarPath = computeMoniLogJarPath(artifact);
		final File graalFolder = new File(installFolder + "/" + GRAALVM_FOLDER);
		try {
			if (!(graalFolder.exists() && graalFolder.isDirectory())) {
				final File graalArchive = new File(installFolder + "/" + GRAALVM_ARCHIVE);
				if (!graalArchive.exists()) {
					final URL graalvmURL = new URL(GRAAL_VM_URL);
					final InputStream downloadStream = graalvmURL.openStream();
					openDownloadingDialog();
					FileUtils.copyInputStreamToFile(downloadStream, graalArchive);
					if (!graalArchive.exists()) {
						openDialog("Could not download GraalVM", "You will have to perform GraalVM installation manually.", MessageDialog.WARNING);
						return Status.OK_STATUS;
					} else {
						openDownloadedDialog();
					}
				}
			}
			ProcessBuilder pb = new ProcessBuilder(fullPath, installFolder, nabLabComponentPath, moniLogJarPath);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			br.close();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			openDialog("Could not complete GraalVM installation", "You will have to perform GraalVM installation manually.", MessageDialog.WARNING);
			return Status.OK_STATUS;
		}
		return Status.OK_STATUS;
	}

	private String computeProgramPath(String artifact, String programPath) {
		return artifact + "/" + programPath;
	}

	private String computeNablaComponentPath(String artifact) {
		return artifact + "/nabla-component.jar";
	}

	private String computeMoniLogJarPath(String artifact) {
		return artifact + "/monilogger.jar";
	}

	@Override
	public IStatus undo(Map<String, Object> parameters) {
		// TODO Auto-generated method stub
		return null;
	}
}
