package fr.cea.nabla.interpreter.runtime;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.exception.AbstractTruffleException;

public final class NablaInternalError extends AbstractTruffleException {

	private static final long serialVersionUID = 80698622974155216L;

	private final String verboseStackTrace;

	public NablaInternalError(String message, Object... args) {
		super(String.format(message, args));
		verboseStackTrace = createVerboseStackTrace();
	}

	public NablaInternalError(Throwable cause, String message, Object... args) {
		super(String.format(message, args));
		CompilerAsserts.neverPartOfCompilation();
		verboseStackTrace = createVerboseStackTrace();
	}

	/**
	 * Constructor that does not use {@code String.format} so that the message may
	 * contain formatting instructions.
	 */
	public NablaInternalError(Throwable cause, String message) {
		super(message);
		CompilerAsserts.neverPartOfCompilation();
		verboseStackTrace = createVerboseStackTrace();
	}

	public String getVerboseStackTrace() {
		return verboseStackTrace;
	}

	public static void guarantee(boolean condition, String message) {
		if (!condition) {
			throw CompilerDirectives.shouldNotReachHere("failed guarantee: " + message);
		}
	}

	public static void guarantee(boolean condition) {
		if (!condition) {
			throw CompilerDirectives.shouldNotReachHere("failed guarantee");
		}
	}

	public static <T> T guaranteeNonNull(T value) {
		if (value == null) {
			throw CompilerDirectives.shouldNotReachHere("should not be null");
		}
		return value;
	}

	public static RuntimeException unimplemented() {
		CompilerDirectives.transferToInterpreter();
		throw new NablaInternalError("not implemented");
	}

	public static RuntimeException unimplemented(String message) {
		CompilerDirectives.transferToInterpreter();
		throw new NablaInternalError("not implemented: %s", message);
	}

	public static RuntimeException unimplemented(String format, Object... args) {
		CompilerDirectives.transferToInterpreter();
		throw new NablaInternalError("not implemented: %s", String.format(format, args));
	}

	static String createVerboseStackTrace() {
		return "";
	}
}