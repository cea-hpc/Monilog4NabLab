package fr.cea.nabla.interpreter.nodes.instruction;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.RepeatingNode;

import fr.cea.nabla.interpreter.nodes.expression.NablaExpressionNode;
import fr.cea.nabla.interpreter.values.NV0Bool;

@NodeChild(value = "conditionNode", type = NablaExpressionNode.class)
public abstract class NablaWhileRepeatingNode extends NablaInstructionNode implements RepeatingNode {

	@Child
	private NablaInstructionNode body;

	protected final Assumption lengthUnchanged = Truffle.getRuntime().createAssumption();

	public NablaWhileRepeatingNode(NablaInstructionNode body) {
		this.body = body;
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
	public boolean doLoop(VirtualFrame frame, NV0Bool shouldContinue) {
		final boolean continueLoop = shouldContinue.isData();
		if (CompilerDirectives.injectBranchProbability(0.9, continueLoop)) {
			body.executeGeneric(frame);
		}
		return continueLoop;
	}
}
