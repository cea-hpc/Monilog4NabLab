package fr.cea.nabla.interpreter.nodes;

import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import fr.cea.nabla.interpreter.NablaTypes;
import fr.cea.nabla.interpreter.nodes.instruction.NablaInstructionNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWriteVariableNode;
import fr.cea.nabla.interpreter.runtime.NablaNull;

@TypeSystemReference(NablaTypes.class)
public class NablaModulePrologNode extends NablaInstructionNode {
	@Children
	private NablaWriteVariableNode[] connectivitySizes;
	@Children
	private NablaWriteVariableNode[] optionDefinitions;
	@Children
	private NablaWriteVariableNode[] variableDefinitions;

	public NablaModulePrologNode(NablaWriteVariableNode[] connectivitySizes,
			NablaWriteVariableNode[] optionDefinitions,
			NablaWriteVariableNode[] variableDefinitions) {
		this.optionDefinitions = optionDefinitions;
		this.variableDefinitions = variableDefinitions;
		this.connectivitySizes = connectivitySizes;
	}

	@ExplodeLoop
	@Override
	public Object executeGeneric(VirtualFrame frame) {

		for (int i = 0; i < connectivitySizes.length; i++) {
			connectivitySizes[i].executeGeneric(frame);
		}

		for (int i = 0; i < optionDefinitions.length; i++) {
			optionDefinitions[i].executeGeneric(frame);
		}

		for (int i = 0; i < variableDefinitions.length; i++) {
			variableDefinitions[i].executeGeneric(frame);
		}

		return NablaNull.SINGLETON;
	}

	@Override
	public boolean isInstrumentable() {
		return false;
	}

}
