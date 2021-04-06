package fr.cea.nabla.ui.graalvm.launchconfig;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class ExtensionClassLoader extends ClassLoader {

	private final Set<String> extensionPoints;

	private final ClassLoader sysClzLoader;

	public ExtensionClassLoader(Set<String> knownExtensionPoints, ClassLoader parent) {
		super(parent);
		extensionPoints = Collections.unmodifiableSet(knownExtensionPoints);
		sysClzLoader = getSystemClassLoader();
	}
	
	@Override
	public InputStream getResourceAsStream(String resource) {
		InputStream result = super.getResourceAsStream(resource);
		if (result != null) {
			return result;
		}
		final Iterator<IConfigurationElement> iterator = extensionPoints.stream().flatMap(s -> Arrays.stream(Platform.getExtensionRegistry()
				.getExtensionPoint(s).getConfigurationElements())).collect(Collectors.toList()).iterator();
		while(result == null && iterator.hasNext()) {
			final IConfigurationElement configurationElement = iterator.next();
			try {
				result = loadClass(configurationElement.getAttribute("class")).getResourceAsStream(resource);
			} catch (InvalidRegistryObjectException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> result = null;
		final Iterator<IConfigurationElement> iterator = extensionPoints.stream().flatMap(s -> Arrays.stream(Platform.getExtensionRegistry()
				.getExtensionPoint(s).getConfigurationElements())).collect(Collectors.toList()).iterator();
		while(result == null && iterator.hasNext()) {
			final IConfigurationElement configurationElement = iterator.next();
			if (configurationElement.getAttribute("class").contains(name)) {
				final Bundle bundle = Platform.getBundle(configurationElement.getContributor().getName());
				if (name.contains(":")) {
					result = bundle.loadClass(name.substring(name.lastIndexOf(":") + 1));
				} else {
					result = bundle.loadClass(name);
				}
				
			}
		}
		if (result != null) {
			return result;
		}
		throw new ClassNotFoundException("Could not find class " + name);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// has the class loaded already?
		Class<?> loadedClass = findLoadedClass(name);
		if (loadedClass == null) {
			try {
				if (sysClzLoader != null) {
					loadedClass = sysClzLoader.loadClass(name);
				}
			} catch (ClassNotFoundException ex) {
				// class not found in system class loader... silently skipping
			}

			try {
				// find the class from given extension points as in first constructor parameter.
				if (loadedClass == null) {
					loadedClass = findClass(name);
				}
			} catch (ClassNotFoundException e) {
				// class is not found in the given extension points.
				// Let's try it in parent classloader.
				// If class is still not found, then this method will throw class not found ex.
				loadedClass = super.loadClass(name);
			}
		}

		return loadedClass;
	}
}
