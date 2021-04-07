package fr.cea.nabla.interpreter.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.graalvm.options.OptionValues;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.NablaOptions;
import fr.cea.nabla.interpreter.nodes.NablaEvalRootNode;
import fr.cea.nabla.ir.ir.IrPackage;
import fr.cea.nabla.ir.ir.IrRoot;

public class NablaParser {

	private ICompilationChainHelper compilationChainHelper;
	
	public RootCallTarget parseNabla(NablaLanguage nablaLanguage, Source source) {
		try {
			return CompilerDirectives.interpreterOnly(() -> {
				LogManager.shutdown();
				final Env env = NablaLanguage.getCurrentContext().getEnv();
				final OptionValues options = env.getOptions();
				final List<URI> nablaFileUris = options.get(NablaOptions.NABLAPATH).stream()
						.map(s -> URI.createFileURI(s)).collect(Collectors.toList());
				final String jsonOptionsFilePath = options.get(NablaOptions.OPTIONS);
				EPackage.Registry.INSTANCE.put(IrPackage.eNS_URI, IrPackage.eINSTANCE);
				final IrRoot irRoot = getIrRoot(source, nablaFileUris);

				final JsonObject jsonOptions;
				final String jsonOptionsString = readFileAsString(jsonOptionsFilePath);
				if (jsonOptionsString != null && !jsonOptionsString.isBlank()) {
					final Gson gson = new Gson();
					jsonOptions = gson.fromJson(jsonOptionsString, JsonObject.class);
				} else {
					jsonOptions = null;
				}

				final RootCallTarget moduleCallTarget = Truffle.getRuntime()
						.createCallTarget(new NablaNodeFactory(nablaLanguage, source).createModule(irRoot, jsonOptions));
				final RootNode evalModule = new NablaEvalRootNode(nablaLanguage, moduleCallTarget);
				final RootCallTarget result = Truffle.getRuntime().createCallTarget(evalModule);
				return result;
			});
		} catch (Exception e) {
			CompilerDirectives.shouldNotReachHere(e);
			return null;
		}
	}

	private String readFileAsString(String path) throws FileNotFoundException {
		final InputStream is = new FileInputStream(new File(path));
		return new BufferedReader(new InputStreamReader(is)).lines()
				.reduce((s1, s2) -> s1 + "\n" + s2).get();
	}
	
	private IrRoot getIrRoot(Source source, List<URI> nablaFileUris) {
		if (compilationChainHelper == null) {
			compilationChainHelper = new StandaloneCompilationChainHelper();
			NablaInjectorProvider inj = new NablaInjectorProvider();
			inj.getInjector().injectMembers(compilationChainHelper);
		}
		return compilationChainHelper.getIrRoot(source, nablaFileUris);
	}
}
