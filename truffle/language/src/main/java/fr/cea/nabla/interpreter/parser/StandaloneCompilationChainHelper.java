/**
 * Copyright (c) 2020 CEA
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * Contributors: see AUTHORS file
 */
package fr.cea.nabla.interpreter.parser;

import java.net.URL;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;

import com.google.common.collect.Streams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.oracle.truffle.api.source.Source;

import fr.cea.nabla.NablaStandaloneSetup;
import fr.cea.nabla.NablagenStandaloneSetup;
import fr.cea.nabla.generator.ir.IrRootBuilder;
import fr.cea.nabla.ir.ir.IrRoot;
import fr.cea.nabla.nablagen.NablagenApplication;

@InjectWith(NablaInjectorProvider.class)
public class StandaloneCompilationChainHelper implements ICompilationChainHelper {
	@Inject
	@Extension
	private ValidationTestHelper validationTestHelper;

	@Inject
	private Provider<IrRootBuilder> irRootBuilderProvider;

	@Inject
	private Provider<ResourceSet> resourceSetProvider;

	public StandaloneCompilationChainHelper() {
		NablaStandaloneSetup.doSetup();
		NablagenStandaloneSetup.doSetup();
	}
	
	@Override
	public IrRoot getIrRoot(Source source, final List<URI> nablaPaths) {
		final URI genModelURI = URI.createFileURI(source.getPath());
		final String mathPath = "/Math.n";
		final String linearAlgebraPath = "/LinearAlgebra.n";
		final String linearAlgebraProviderPath = "/LinearAlgebra.ngen";
		final String cartesianMesh2DPath = "/CartesianMesh2D.n";
		final String cartesianMesh2DProviderPath = "/CartesianMesh2D.ngen";
		
		try {
			final ResourceSet rs = resourceSetProvider.get();

			nablaPaths.add(genModelURI);

			final URL mathURL = getClass().getResource(mathPath);
			final URL linearURL = getClass().getResource(linearAlgebraPath);
			final URL linearProviderURL = getClass().getResource(linearAlgebraProviderPath);
			final URL meshURL = getClass().getResource(cartesianMesh2DPath);
			final URL meshProviderURL = getClass().getResource(cartesianMesh2DProviderPath);

			final URI mathURI = URI.createURI(mathURL.toString());
			final URI linearURI = URI.createURI(linearURL.toString());
			final URI linearProviderURI = URI.createURI(linearProviderURL.toString());
			final URI meshURI = URI.createURI(meshURL.toString());
			final URI meshProviderURI = URI.createURI(meshProviderURL.toString());
			
			nablaPaths.add(mathURI);
			nablaPaths.add(linearURI);
			nablaPaths.add(linearProviderURI);
			nablaPaths.add(meshURI);
			nablaPaths.add(meshProviderURI);
			
			nablaPaths.forEach(p -> {
				rs.getResource(p, true);
			});

			EcoreUtil.resolveAll(rs);
			
			final NablagenApplication nablaGenRoot = Streams.stream(rs.getAllContents())
					.filter(o -> o instanceof NablagenApplication).findFirst().map(o -> (NablagenApplication) o)
					.orElseThrow();
			
			validate(nablaGenRoot);

			if (nablaGenRoot != null) {
				final IrRootBuilder interpreter = irRootBuilderProvider.get();
				final IrRoot irRoot = interpreter.buildInterpreterIr(nablaGenRoot, "");
				return irRoot;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw Exceptions.sneakyThrow(e);
		}
	}

	private void validate(EObject modelElement) {
		final List<Issue> issues = validationTestHelper.validate(modelElement);
		if (!issues.isEmpty()) {
			final String msg = issues.stream().map(i -> "At line " + i.getLineNumber() + ": " + i.getMessage())
					.reduce((i1, i2) -> i1 + "\n" + i2).get();
			throw new IllegalArgumentException(msg);
		}
	}
}
