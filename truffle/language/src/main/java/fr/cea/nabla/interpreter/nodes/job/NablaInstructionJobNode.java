package fr.cea.nabla.interpreter.nodes.job;

import com.oracle.truffle.api.frame.VirtualFrame;

import fr.cea.nabla.interpreter.nodes.instruction.NablaInstructionNode;

public class NablaInstructionJobNode extends NablaJobNode {

	@Child
	private NablaInstructionNode body;
	
	public NablaInstructionJobNode(String name, NablaInstructionNode body) {
		super(name);
		this.body = body;
	}

	protected NablaInstructionJobNode() {
	}
	
	@Override
	public Object executeGeneric(VirtualFrame frame) {
		return body.executeGeneric(frame);
	}
}
