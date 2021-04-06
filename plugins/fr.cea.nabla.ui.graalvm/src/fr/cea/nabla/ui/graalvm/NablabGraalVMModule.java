package fr.cea.nabla.ui.graalvm;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.AbstractModule;

public class NablabGraalVMModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ResourceSet.class).to(XtextResourceSet.class);
	}

}
