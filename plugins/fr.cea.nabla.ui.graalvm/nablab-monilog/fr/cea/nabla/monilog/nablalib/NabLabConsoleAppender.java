package fr.cea.nabla.monilog.nablalib;

import org.gemoc.monilog.api.IMoniLogAppender;

import com.google.inject.Inject;

import fr.cea.nabla.generator.NablaGeneratorMessageDispatcher.MessageType;
import fr.cea.nabla.ui.console.NabLabConsoleFactory;
import fr.cea.nabla.ui.internal.NablaActivator;

public class NabLabConsoleAppender implements IMoniLogAppender {

	@Inject NabLabConsoleFactory consoleFactory;

	public NabLabConsoleAppender() {
		NablaActivator activator = NablaActivator.getInstance();
		activator.getInjector(NablaActivator.FR_CEA_NABLA_NABLA).injectMembers(this);
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
