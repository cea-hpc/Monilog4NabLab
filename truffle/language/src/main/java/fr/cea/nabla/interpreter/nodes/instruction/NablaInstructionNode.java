package fr.cea.nabla.interpreter.nodes.instruction;

import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.ProbeNode;

import fr.cea.nabla.interpreter.nodes.local.NablaScopedNode;

@GenerateWrapper
public abstract class NablaInstructionNode extends NablaScopedNode {

	@Override
	public WrapperNode createWrapper(ProbeNode probe) {
		return new NablaInstructionNodeWrapper(this, probe);
	}
}
