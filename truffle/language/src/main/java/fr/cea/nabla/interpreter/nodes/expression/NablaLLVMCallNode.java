package fr.cea.nabla.interpreter.nodes.expression;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.runtime.NablaContext;

public abstract class NablaLLVMCallNode extends NablaExpressionNode {

	@Children
	private final NablaExpressionNode[] args;
	protected final String providerName;
	private final String memberName;

	public NablaLLVMCallNode(String providerName, String memberName, NablaExpressionNode[] args) {
		this.providerName = providerName;
		this.memberName = memberName;
		this.args = args;
	}

	@ExplodeLoop
	@Specialization(assumptions = "contextActive")
	public Object doCached(VirtualFrame frame, @Cached("getMember()") Value member,
			@CachedContext(NablaLanguage.class) NablaContext context,
			@Cached("context.getNativeLibrary(providerName)") Value meshWrapper,
			@Cached("context.getContextActive()") Assumption contextActive) {
		final Object[] argumentValues = new Object[args.length + 1];
		argumentValues[0] = meshWrapper;
		for (int i = 0; i < args.length; i++) {
			argumentValues[i + 1] = args[i].executeGeneric(frame);
		}
		return member.execute(argumentValues);
	}

	protected Value getMember() {
		final Value llvmBindings = Context.getCurrent().getBindings("llvm");
		return llvmBindings.getMember(memberName);
	}
}
