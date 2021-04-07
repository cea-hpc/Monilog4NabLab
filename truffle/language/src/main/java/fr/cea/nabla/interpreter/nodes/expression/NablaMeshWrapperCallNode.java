package fr.cea.nabla.interpreter.nodes.expression;

import org.graalvm.polyglot.Value;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.runtime.CartesianMesh2DWrapper;
import fr.cea.nabla.interpreter.runtime.NablaContext;
import fr.cea.nabla.interpreter.values.UnboxValueNode;
import fr.cea.nabla.interpreter.values.UnboxValueNodeGen;

public abstract class NablaMeshWrapperCallNode extends NablaExpressionNode {

	@Children
	private UnboxValueNode[] unboxArgNodes;
	protected final String methodName;

	public NablaMeshWrapperCallNode(String methodName, NablaExpressionNode[] argumentNodes) {
		this.unboxArgNodes = new UnboxValueNode[argumentNodes.length];
		for (int i = 0; i < unboxArgNodes.length; i++) {
			this.unboxArgNodes[i] = UnboxValueNodeGen.create(argumentNodes[i]);
		}
		this.methodName = methodName;
	}

	@ExplodeLoop
	@Specialization(assumptions = "contextActive")
	public Object executeGeneric(VirtualFrame frame,
			@CachedContext(NablaLanguage.class) NablaContext context,
			@Cached("context.getContextActive()") Assumption contextActive,
			@Cached("context.getMeshWrapper()") CartesianMesh2DWrapper meshWrapper,
			@Cached("meshWrapper.connectivityGetter(methodName)") Value connectivity) {
		final Object[] argumentValues = new Object[unboxArgNodes.length];
		for (int i = 0; i < unboxArgNodes.length; i++) {
			argumentValues[i] = unboxArgNodes[i].execute(frame);
		}
		return connectivity.execute(argumentValues);
	}

	@Override
	public boolean hasTag(Class<? extends Tag> tag) {
		if (tag == StandardTags.CallTag.class) {
			return true;
		}
		return super.hasTag(tag);
	}
}
