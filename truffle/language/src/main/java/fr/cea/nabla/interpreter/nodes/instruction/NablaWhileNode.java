package fr.cea.nabla.interpreter.nodes.instruction;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;

public class NablaWhileNode extends NablaInstructionNode {

	@Child
	private LoopNode loopNode;

	public NablaWhileNode(LoopNode loopNode) {
		this.loopNode = loopNode;
	}

	protected NablaWhileNode() {
	}

	@Override
	public Object executeGeneric(VirtualFrame frame) {
		return loopNode.execute(frame);
	}

}
