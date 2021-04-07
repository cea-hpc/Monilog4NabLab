package fr.cea.nabla.interpreter.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.options.OptionKey;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.interop.NodeLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.Layout;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.NablaOptions;

public final class NablaContext {

	static final Layout LAYOUT = Layout.createLayout();

	private final Env env;
	private final BufferedReader input;
	private final PrintWriter output;
	private final NablaFunctionRegistry functionRegistry;
	private final AllocationReporter allocationReporter;
	@CompilationFinal(dimensions = 1)
	private Value[] nativeLibraries;
	@CompilationFinal(dimensions = 1)
	private String[] libraryProviders;
	private final CartesianMesh2DWrapper meshWrapper = new CartesianMesh2DWrapper();

	private final Assumption contextActive = Truffle.getRuntime().createAssumption("context active");

	public Assumption getContextActive() {
		return contextActive;
	}

	@CompilationFinal
	private MaterializedFrame globalFrame;

	@CompilationFinal
	private Object topScope;

	public NablaContext(NablaLanguage language, TruffleLanguage.Env env) {
		this.env = env;
		this.input = new BufferedReader(new InputStreamReader(env.in()));
		this.output = new PrintWriter(env.out(), true);
		this.allocationReporter = env.lookup(AllocationReporter.class);
		this.functionRegistry = new NablaFunctionRegistry(language);
	}

	private JsonObject getJsonOptions() {
		try {
			final String jsonOptionsFile = env.getOptions().get(NablaOptions.OPTIONS);
			if (jsonOptionsFile != null && !jsonOptionsFile.isBlank()) {
				final String jsonOptionsString = new String(Files.readAllBytes(Paths.get(jsonOptionsFile)));
				if (jsonOptionsString != null && !jsonOptionsString.isEmpty()) {
					final Gson gson = new Gson();
					return gson.fromJson(jsonOptionsString, JsonObject.class);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	
//	public void setProvider(String providerName, Value providerValue) {
//		final int length = this.nativeLibraries.length;
//		final Value[] nativeLibraries = new Value[length + 1];
//		final String[] libraryProviders = new String[length + 1];
//		System.arraycopy(this.nativeLibraries, 0, nativeLibraries, 0, length);
//		System.arraycopy(this.libraryProviders, 0, libraryProviders, 0, length);
//		this.nativeLibraries = nativeLibraries;
//		this.libraryProviders = libraryProviders;
//		this.nativeLibraries[length] = providerValue;
//		this.libraryProviders[length] = providerName;
//	}
	
	public void initializeMesh() {
		final JsonObject jsonOptions = getJsonOptions().getAsJsonObject("mesh");
		meshWrapper.initialize(jsonOptions);
	}
	
	public CartesianMesh2DWrapper getMeshWrapper() {
		return meshWrapper;
	}
	
	public void initializeNativeExtensions() {
		final Map<String, String> extensions = new HashMap<>();
		env.getOptions().get(NablaOptions.NAT_LIBS).entrySet().stream().forEach(e -> extensions.put(e.getKey(), e.getValue()));
		// TODO dynamicize
//		extensions.put("CartesianMesh2D", env.getOptions().get(NablaOptions.MESH));
		
		final JsonObject jsonOptions = getJsonOptions();

		this.nativeLibraries = new Value[extensions.size()];
		this.libraryProviders = new String[extensions.size()];
		final int[] iPtr = new int[] { 0 };

		extensions.entrySet().forEach(extension -> {
			final String extensionProvider = extension.getKey();
			final String pathToNativeLibrary = extension.getValue();
			try {
				final Value llvmBindings = Context.getCurrent().getBindings("llvm");
				final org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source
						.newBuilder("llvm", new File(pathToNativeLibrary)).internal(true).build();
				Context.getCurrent().eval(source);
				if (llvmBindings.hasMember(extensionProvider + "_jsonInit")) {
					final Value jsonInit = llvmBindings.getMember(extensionProvider + "_jsonInit");
					final String jsonContent;
					if (jsonOptions == null) {
						jsonContent = "\0";
					} else {
						if (extensionProvider.equals("CartesianMesh2D")) {
							jsonContent = jsonOptions.get("mesh") + "\0";
						} else {
							jsonContent = jsonOptions.get(extensionProvider) + "\0";
						}
					}
					this.libraryProviders[iPtr[0]] = extensionProvider;
					this.nativeLibraries[iPtr[0]] = jsonInit.execute(jsonContent);
				} else {
					throw new IllegalArgumentException("Library " + pathToNativeLibrary + " is missing a \""
							+ extensionProvider + "_jsonInit(const void *value)\" initialization function.");
				}
			} catch (IOException e) {
				CompilerDirectives.shouldNotReachHere(e);
			} finally {
				iPtr[0]++;
			}
		});
	}

	public Env getEnv() {
		return env;
	}

	public BufferedReader getInput() {
		return input;
	}

	public PrintWriter getOutput() {
		return output;
	}

	public NablaFunctionRegistry getFunctionRegistry() {
		return functionRegistry;
	}

	public static NodeInfo lookupNodeInfo(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}
		NodeInfo info = clazz.getAnnotation(NodeInfo.class);
		if (info != null) {
			return info;
		} else {
			return lookupNodeInfo(clazz.getSuperclass());
		}
	}

	public static NablaContext getCurrent() {
		return NablaLanguage.getCurrentContext();
	}

	public static Object fromForeignValue(Object object) {
		return null;
	}

	public AllocationReporter getAllocationReporter() {
		return allocationReporter;
	}

	public boolean getBooleanOption(OptionKey<Boolean> key) {
		return this.getEnv().getOptions().get(key);
	}

	public String getStringOption(OptionKey<String> key) {
		return this.getEnv().getOptions().get(key);
	}

	public Frame getGlobalFrame() {
		return globalFrame;
	}

	public void setGlobalFrame(Node node, MaterializedFrame globalFrame) {
		this.globalFrame = globalFrame;
		try {
			this.topScope = NodeLibrary.getUncached(node).getScope(node, globalFrame, true);
		} catch (UnsupportedMessageException e) {
			throw new NablaInternalError(e, "Top scope initialization failed");
		}
	}
	
	public Object getScope() {
		return topScope;
	}

	@ExplodeLoop
	public Value getNativeLibrary(String providerName) {
		Value result = null;
		for (int i = 0; i < nativeLibraries.length; i++) {
			final String libraryProvider = libraryProviders[i];
			if (libraryProvider.equals(providerName)) {
				result = nativeLibraries[i];
				break;
			}
		}
		return result;
	}

}
