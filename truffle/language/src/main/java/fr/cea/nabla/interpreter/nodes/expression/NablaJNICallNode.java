package fr.cea.nabla.interpreter.nodes.expression;

import org.graalvm.polyglot.Value;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.runtime.NablaContext;
import fr.cea.nabla.interpreter.runtime.NablaProviderObject;

public abstract class NablaJNICallNode extends NablaExpressionNode {

	@Children
	private final NablaExpressionNode[] args;
	protected final NablaProviderObject providerObject;
	protected final String providerName;
	private final String memberName;

	public NablaJNICallNode(NablaProviderObject providerObject, String memberName, NablaExpressionNode[] args) {
		this.providerObject = providerObject;
		this.providerName = providerObject.getProviderName();
		this.memberName = memberName;
		this.args = args;
	}

	@ExplodeLoop
	@Specialization(assumptions = "contextActive")
	public Object doCached(VirtualFrame frame,
			@CachedContext(NablaLanguage.class) NablaContext context,
//			@Cached("context.getNativeLibrary(providerName)") Value facadeObject,
			@Cached("providerObject.initialize()") Value facadeObject,
			@Cached("getMember(facadeObject)") Value methodMember,
			@Cached("context.getContextActive()") Assumption contextActive) {
		final Object[] argumentValues = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			argumentValues[i + 1] = args[i].executeGeneric(frame);
		}
		return facadeObject.invokeMember(memberName, argumentValues);
	}

	protected Value getMember(Value facadeObject) {
		return facadeObject.getMember(memberName);
	}
}
