package fr.cea.nabla.interpreter.nodes.expression;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import fr.cea.nabla.interpreter.runtime.NablaContext;
import fr.cea.nabla.interpreter.runtime.NablaInvokeNode;
import fr.cea.nabla.interpreter.runtime.NablaInvokeNodeGen;
import fr.cea.nabla.interpreter.values.UnboxValueNode;
import fr.cea.nabla.interpreter.values.UnboxValueNodeGen;

public class NablaJavaMethodCallNode extends NablaExpressionNode {

	@Children
	private UnboxValueNode[] unboxArgNodes;
	@Child
	private NablaInvokeNode invokeNode;
	private final String methodName;
	private final Object receiverObject;

	public NablaJavaMethodCallNode(String receiverClass, String methodName, Class<?> returnType,
			NablaExpressionNode[] argumentNodes) {
		this.unboxArgNodes = new UnboxValueNode[argumentNodes.length];
		for (int i = 0; i < unboxArgNodes.length; i++) {
			this.unboxArgNodes[i] = UnboxValueNodeGen.create(argumentNodes[i]);
		}
		this.methodName = methodName;
		this.receiverObject = NablaContext.getCurrent().getEnv().lookupHostSymbol(receiverClass);
		this.invokeNode = NablaInvokeNodeGen.create();
	}

	@Override
	@ExplodeLoop
	public Object executeGeneric(VirtualFrame frame) {
		final Object[] argumentValues = new Object[unboxArgNodes.length];
		for (int i = 0; i < unboxArgNodes.length; i++) {
			argumentValues[i] = unboxArgNodes[i].execute(frame);
		}

		return invokeNode.execute(receiverObject, methodName, argumentValues);
	}

	@Override
	public boolean hasTag(Class<? extends Tag> tag) {
		if (tag == StandardTags.CallTag.class) {
			return true;
		}
		return super.hasTag(tag);
	}
}
