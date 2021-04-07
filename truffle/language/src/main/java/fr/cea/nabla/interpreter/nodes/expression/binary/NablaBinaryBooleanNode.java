package fr.cea.nabla.interpreter.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;

import fr.cea.nabla.interpreter.values.NV0Bool;

public abstract class NablaBinaryBooleanNode extends NablaBinaryExpressionNode {

	private final BooleanOperator op;

	public NablaBinaryBooleanNode(BooleanOperator operator) {
		this.op = operator;
	}

	private boolean doBinaryBoolean(boolean a, boolean b) {
		switch (op) {
		case AND:
			return a && b;
		case OR:
			return a || b;
		default:
			CompilerDirectives.shouldNotReachHere();
			return false;
		}
	}
	
	@Specialization
	protected NV0Bool and(NV0Bool left, NV0Bool right) {
		return new NV0Bool(doBinaryBoolean(left.isData(), right.isData()));
	}
	
}
