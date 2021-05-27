package fr.cea.nabla.monilog.nablalib;

import com.google.inject.Inject;

import fr.cea.nabla.generator.NablaGeneratorMessageDispatcher.MessageType;
import fr.cea.nabla.ui.console.NabLabConsoleFactory;
import fr.cea.nabla.ui.graalvm.Activator;

import org.gemoc.monilog.api.IMoniLogAppender;

public class NabLabConsoleAppender implements IMoniLogAppender {

	@Inject NabLabConsoleFactory consoleFactory;

	public NabLabConsoleAppender() {
		Activator.getDefault().getInjector().injectMembers(this);
	}
	
	@Override
	public void call(Object... args) {
		consoleFactory.printConsole(MessageType.Exec, args[0].toString());
	}

	@Override
	public int getNbMinArgs() {
		return 1;
	}

	@Override
	public int getNbMaxArgs() {
		return 1;
	}

	@Override
	public Class<?> getArgType(int argIndex) {
		if (argIndex == 0) {
			return String.class;
		}
		return null;
	}
	
}
