package fr.cea.nabla.ui.graalvm.p2.actions;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class DownloadingDialog extends TitleAreaDialog {

	private ProgressBar progressBar;
	
	private final Display display;
	
	private final int maximum;
	
	public DownloadingDialog(Shell parentShell, int maximum) {
		super(parentShell);
		this.display = parentShell.getDisplay();
		this.maximum = maximum;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Downloading GraalVM Community Edition 21.0.0");
		setMessage("Please wait while GraalVM Community Edition 21.0.0 is being downloaded...", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
//		progressBar = new ProgressBar(area, SWT.NULL);
//		progressBar.setMinimum(0);
//		progressBar.setMaximum(maximum);
		
		return area;
	}
	
	public void update(int progress) {
		display.asyncExec(() -> {
			progressBar.setSelection(progress);
		});
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

}