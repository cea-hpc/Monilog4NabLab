package fr.cea.nabla.interpreter.nodes.job;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;

import fr.cea.nabla.interpreter.nodes.instruction.NablaWriteVariableNode;

public class NablaTimeLoopJobNode extends NablaJobNode {

	@Child
	private NablaWriteVariableNode indexInitializer;
	@Child
	private LoopNode loopNode;
	
	public NablaTimeLoopJobNode(String name, NablaWriteVariableNode indexInitializer, LoopNode loopNode) {
		super(name);
		this.indexInitializer = indexInitializer;
		this.loopNode = loopNode;
	}

	protected NablaTimeLoopJobNode() {
	}

	@Override
	public Object executeGeneric(VirtualFrame frame) {
		indexInitializer.executeGeneric(frame);
		return loopNode.execute(frame);
	}

}
