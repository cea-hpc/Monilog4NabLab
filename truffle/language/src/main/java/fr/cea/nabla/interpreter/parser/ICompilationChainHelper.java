package fr.cea.nabla.interpreter.parser;

import java.util.List;

import org.eclipse.emf.common.util.URI;

import com.oracle.truffle.api.source.Source;

import fr.cea.nabla.ir.ir.IrRoot;

public interface ICompilationChainHelper {

	IrRoot getIrRoot(Source source, List<URI> nablaFileUris);

}
