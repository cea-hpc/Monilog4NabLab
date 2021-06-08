package fr.cea.nabla.interpreter.nodes.job;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;

import fr.cea.nabla.interpreter.nodes.expression.NablaExpressionNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaInstructionNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWriteVariableNode;
import fr.cea.nabla.interpreter.values.NV0Bool;
import fr.cea.nabla.interpreter.values.NV0Int;

@GenerateWrapper
// Index is updated at the beginning of the loop, in conformance with the NabLab interpreter.
@NodeChild(value = "indexUpdate", type = NablaWriteVariableNode.class)
@NodeChild(value = "innerJobBlock", type = NablaJobBlockNode.class)
@NodeChild(value = "conditionNode", type = NablaExpressionNode.class)
public abstract class NablaTimeLoopJobRepeatingNode extends Node implements RepeatingNode, InstrumentableNode {

	@Child
	private NablaInstructionNode copyInstructionNode;

	protected NablaTimeLoopJobRepeatingNode(NablaInstructionNode copyInstructionNode, boolean dumpVariables) {
		this.copyInstructionNode = copyInstructionNode;
	}

	protected NablaTimeLoopJobRepeatingNode(NablaInstructionNode copyInstructionNode) {
		this(copyInstructionNode, false);
	}

	protected NablaTimeLoopJobRepeatingNode() {
	}

	/**
	 * Necessary to avoid errors in generated class.
	 */
	@Override
	public final Object executeRepeatingWithValue(VirtualFrame frame) {
		if (executeRepeating(frame)) {
			return CONTINUE_LOOP_STATUS;
		} else {
			return BREAK_LOOP_STATUS;
		}
	}

	@ExplodeLoop
	@Specialization
	public boolean doLoop(VirtualFrame frame, NV0Int index, Object blockResult, NV0Bool shouldContinue) {
		final boolean continueLoop = shouldContinue.isData();
		if (CompilerDirectives.injectBranchProbability(0.9, continueLoop)) {
			copyInstructionNode.executeGeneric(frame);
		}
		return continueLoop;
	}

	@Override
	public boolean isInstrumentable() {
		return true;
	}

	@Override
	public boolean hasTag(Class<? extends Tag> tag) {
		return tag.equals(StandardTags.RootBodyTag.class);
	}

	@Override
	public WrapperNode createWrapper(ProbeNode probe) {
		return new NablaTimeLoopJobRepeatingNodeWrapper(this, probe);
	}
}
