package fr.cea.nabla.interpreter.runtime;

import org.graalvm.polyglot.Value;

import com.google.gson.JsonObject;

public abstract class NablaProviderObject {

	protected final String providerName;
	protected final String extensionName;
	protected final String path;
	protected final JsonObject options;
	
	public NablaProviderObject(String providerName, String extensionName, String path, JsonObject options) {
		this.providerName = providerName;
		this.extensionName = extensionName;
		this.path = path;
		this.options = options;
	}
	
	public abstract Value initialize();
	
	public String getProviderName() {
		return providerName;
	}
}
