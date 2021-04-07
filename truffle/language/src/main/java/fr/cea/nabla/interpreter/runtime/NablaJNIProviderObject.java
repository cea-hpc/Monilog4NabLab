package fr.cea.nabla.interpreter.runtime;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.xtext.util.Strings;
import org.graalvm.polyglot.Value;

import com.google.gson.JsonObject;

public class NablaJNIProviderObject extends NablaProviderObject {

	private final URLClassLoader classLoader;
	
	public NablaJNIProviderObject(String providerName, String extensionName, String path, JsonObject options) {
		super(providerName, extensionName, path, options);
		URLClassLoader classLoader = null;
		try {
			final URL url = new URL(path);
			classLoader = new URLClassLoader(new URL[] { url },
					Thread.currentThread().getContextClassLoader());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.classLoader = classLoader;
	}

	@Override
	public Value initialize() {
		try {
			System.out.println("Provider " + providerName + ": " + classLoader);
			final Class<?> facadeClass = classLoader.loadClass(providerName.toLowerCase() + '.' + extensionName);
			final Value javaLibValue = Value.asValue(facadeClass.getConstructor().newInstance());
			final String optionString = Strings.toFirstLower(facadeClass.getSimpleName());
			if (options.has(optionString)) {
				javaLibValue.invokeMember("jsonInit", new Object[] { options.get(optionString).toString() });
			}
			return javaLibValue;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
