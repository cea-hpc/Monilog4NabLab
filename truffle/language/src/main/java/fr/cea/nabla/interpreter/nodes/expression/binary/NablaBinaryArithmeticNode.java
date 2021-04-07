package fr.cea.nabla.interpreter.nodes.expression.binary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.LoopConditionProfile;

import fr.cea.nabla.interpreter.values.NV0Int;
import fr.cea.nabla.interpreter.values.NV0Real;
import fr.cea.nabla.interpreter.values.NV1Int;
import fr.cea.nabla.interpreter.values.NV1IntJava;
import fr.cea.nabla.interpreter.values.NV1IntLibrary;
import fr.cea.nabla.interpreter.values.NV1Real;
import fr.cea.nabla.interpreter.values.NV1RealJava;
import fr.cea.nabla.interpreter.values.NV1RealLibrary;
import fr.cea.nabla.interpreter.values.NV2Int;
import fr.cea.nabla.interpreter.values.NV2Real;

public abstract class NablaBinaryArithmeticNode extends NablaBinaryExpressionNode {

	
	private final ArithmeticOperator op;

	public NablaBinaryArithmeticNode(ArithmeticOperator operator) {
		this.op = operator;
	}

	private int doBinaryInt(int a, int b) {
		switch (op) {
		case ADD:
			return Math.addExact(a, b);
		case SUB:
			return a - b;
		case DIV:
			return a / b;
		case MUL:
			return a * b;
		default:
			CompilerDirectives.shouldNotReachHere();
			return 0;
		}
	}

	double doBinaryDouble(double a, double b) {
		switch (op) {
		case ADD:
			return a + b;
		case SUB:
			return a - b;
		case DIV:
			return a / b;
		case MUL:
			return a * b;
		default:
			CompilerDirectives.shouldNotReachHere();
			return 0;
		}
	}

	@Specialization
	protected NV0Int doBinary(NV0Int left, NV0Int right) {
		return new NV0Int(doBinaryInt(left.getData(), right.getData()));
	}

	@Specialization
	protected NV0Real doBinary(NV0Int left, NV0Real right) {
		return new NV0Real(doBinaryDouble(left.getData(), right.getData()));
	}


	@Specialization(guards = "arrays.isArray(right)", limit = "3")
	protected NV1Int doBinary(NV0Int left, Object right, @CachedLibrary("right") NV1IntLibrary arrays, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final int leftData = left.getData();
		final int length = arrays.length(right);

		final int[] result = new int[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryInt(leftData, arrays.read(right, i));
		}

		return new NV1IntJava(result);
	}


	@Specialization(guards = "arrays.isArray(right)", limit = "3")
	protected NV1Real doBinary(NV0Int left, Object right, @CachedLibrary("right") NV1RealLibrary arrays, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final int leftData = left.getData();
		final int length = arrays.length(right);

		final double[] result = new double[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryDouble(leftData, arrays.read(right, i));
		}

		return new NV1RealJava(result);
	}


	@Specialization
	protected NV2Int doBinary(NV0Int left, NV2Int right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final int leftData = left.getData();
		final int[][] rightData = right.getData();

		final int[][] result = new int[rightData.length][rightData[0].length];

		outerLoopProfile.profileCounted(rightData.length);
		innerLoopProfile.profileCounted(rightData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < rightData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < rightData[0].length); j++) {
				result[i][j] = doBinaryInt(leftData, rightData[i][j]);
			}
		}

		return new NV2Int(result);
	}


	@Specialization
	protected NV2Real doBinary(NV0Int left, NV2Real right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final int leftData = left.getData();
		final double[][] rightData = right.getData();

		final double[][] result = new double[rightData.length][rightData[0].length];

		outerLoopProfile.profileCounted(rightData.length);
		innerLoopProfile.profileCounted(rightData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < rightData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < rightData[0].length); j++) {
				result[i][j] = doBinaryDouble(leftData, rightData[i][j]);
			}
		}

		return new NV2Real(result);
	}

	@Specialization
	protected NV0Real doBinary(NV0Real left, NV0Int right) {
		return new NV0Real(doBinaryDouble(left.getData(), right.getData()));
	}

	@Specialization
	protected NV0Real doBinary(NV0Real left, NV0Real right) {
		return new NV0Real(doBinaryDouble(left.getData(), right.getData()));
	}


	@Specialization(guards = "arrays.isArray(right)", limit = "3")
	protected NV1Real doBinary(NV0Real left, Object right, @CachedLibrary("right") NV1IntLibrary arrays, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final double leftData = left.getData();
		final int length = arrays.length(right);

		final double[] result = new double[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryDouble(leftData, arrays.read(right, i));
		}

		return new NV1RealJava(result);
	}


	@Specialization(guards = "arrays.isArray(right)", limit = "3")
	protected NV1Real doBinary(NV0Real left, Object right, @CachedLibrary("right") NV1RealLibrary arrays, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final double leftData = left.getData();
		final int length = arrays.length(right);

		final double[] result = new double[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryDouble(leftData, arrays.read(right, i));
		}

		return new NV1RealJava(result);
	}


	@Specialization
	protected NV2Real doBinary(NV0Real left, NV2Int right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final double leftData = left.getData();
		final int[][] rightData = right.getData();

		final double[][] result = new double[rightData.length][rightData[0].length];

		outerLoopProfile.profileCounted(rightData.length);
		innerLoopProfile.profileCounted(rightData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < rightData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < rightData[0].length); j++) {
				result[i][j] = doBinaryDouble(leftData, rightData[i][j]);
			}
		}

		return new NV2Real(result);
	}


	@Specialization
	protected NV2Real doBinary(NV0Real left, NV2Real right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final double leftData = left.getData();
		final double[][] rightData = right.getData();

		final double[][] result = new double[rightData.length][rightData[0].length];

		outerLoopProfile.profileCounted(rightData.length);
		innerLoopProfile.profileCounted(rightData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < rightData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < rightData[0].length); j++) {
				result[i][j] = doBinaryDouble(leftData, rightData[i][j]);
			}
		}

		return new NV2Real(result);
	}


	@Specialization(guards = "arrays.isArray(left)", limit = "3")
	protected NV1Int doBinary(Object left, NV0Int right, @CachedLibrary("left") NV1IntLibrary arrays, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final int length = arrays.length(left);
		final int rightData = right.getData();

		final int[] result = new int[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryInt(arrays.read(left, i), rightData);
		}

		return new NV1IntJava(result);
	}

	@Specialization(guards = "arrays.isArray(left)", limit = "3")
	protected NV1Real doBinary(Object left, NV0Real right, @CachedLibrary("left") NV1IntLibrary arrays, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final int length = arrays.length(left);
		final double rightData = right.getData();

		final double[] result = new double[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryDouble(arrays.read(left, i), rightData);
		}

		return new NV1RealJava(result);
	}


	@Specialization(guards = { "arraysLeft.isArray(left)", "arraysRight.isArray(right)" }, limit = "3")
	protected NV1Int doBinary(Object left, Object right, //
			@CachedLibrary("left") NV1IntLibrary arraysLeft, //
			@CachedLibrary("right") NV1IntLibrary arraysRight, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final int length = arraysLeft.length(left);

		final int[] result = new int[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryInt(arraysLeft.read(left, i), arraysRight.read(right, i));
		}

		return new NV1IntJava(result);
	}


	@Specialization(guards = { "arraysLeft.isArray(left)", "arraysRight.isArray(right)" }, limit = "3")
	protected NV1Real doBinary(Object left, Object right, //
			@CachedLibrary("left") NV1IntLibrary arraysLeft, //
			@CachedLibrary("right") NV1RealLibrary arraysRight, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final int length = arraysLeft.length(left);

		final double[] result = new double[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryDouble(arraysLeft.read(left, i), arraysRight.read(right, i));
		}

		return new NV1RealJava(result);
	}


	@Specialization
	protected NV1Real doBinary(NV1RealJava left, NV0Int right, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final double[] leftData = left.getData();
		final int rightData = right.getData();

		final double[] result = new double[leftData.length];

		loopProfile.profileCounted(leftData.length);
		for (int i = 0; loopProfile.inject(i < leftData.length); i++) {
			result[i] = doBinaryDouble(leftData[i], rightData);
		}

		return new NV1RealJava(result);
	}


	@Specialization
	protected NV1Real doBinary(NV1RealJava left, NV0Real right, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final double[] leftData = left.getData();
		final double rightData = right.getData();

		final double[] result = new double[leftData.length];

		loopProfile.profileCounted(leftData.length);
		for (int i = 0; loopProfile.inject(i < leftData.length); i++) {
			result[i] = doBinaryDouble(leftData[i], rightData);
		}

		return new NV1RealJava(result);
	}


	@Specialization(guards = "arrays.isArray(right)", limit = "3")
	protected NV1Real doBinary(NV1RealJava left, Object right, //
			@CachedLibrary("right") NV1IntLibrary arrays, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final double[] leftData = left.getData();

		final double[] result = new double[leftData.length];

		loopProfile.profileCounted(leftData.length);
		for (int i = 0; loopProfile.inject(i < leftData.length); i++) {
			result[i] = doBinaryDouble(leftData[i], arrays.read(right, i));
		}

		return new NV1RealJava(result);
	}

	
	@Specialization(guards = { "arraysLeft.isArray(left)", "arraysRight.isArray(right)",
			"length == arraysLeft.length(left)", "length == arraysRight.length(right)" }, limit = "3")
	protected NV1Real doNV1NV1Cached(Object left, Object right, //
			@CachedLibrary("left") NV1RealLibrary arraysLeft, //
			@CachedLibrary("right") NV1RealLibrary arraysRight, //
			@Cached("arraysLeft.length(left)") int length, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {

		final double[] result = new double[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryDouble(arraysLeft.read(left, i), arraysRight.read(right, i));
		}

		return new NV1RealJava(result);
	}


	@Specialization(guards = { "arraysLeft.isArray(left)", "arraysRight.isArray(right)",
			"arraysLeft.length(left) == arraysRight.length(right)" }, limit = "3", replaces = "doNV1NV1Cached")
	protected NV1Real doBinary(Object left, Object right, //
			@CachedLibrary("left") NV1RealLibrary arraysLeft, //
			@CachedLibrary("right") NV1RealLibrary arraysRight, //
			@Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
		final int length = arraysLeft.length(left);

		final double[] result = new double[length];

		loopProfile.profileCounted(length);
		for (int i = 0; loopProfile.inject(i < length); i++) {
			result[i] = doBinaryDouble(arraysLeft.read(left, i), arraysRight.read(right, i));
		}

		return new NV1RealJava(result);
	}


	@Specialization
	protected NV2Int doBinary(NV2Int left, NV0Int right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final int[][] leftData = left.getData();
		final int rightData = right.getData();

		final int[][] result = new int[leftData.length][leftData[0].length];

		outerLoopProfile.profileCounted(leftData.length);
		innerLoopProfile.profileCounted(leftData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < leftData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < leftData[0].length); j++) {
				result[i][j] = doBinaryInt(leftData[i][j], rightData);
			}
		}

		return new NV2Int(result);
	}


	@Specialization
	protected NV2Real doBinary(NV2Int left, NV0Real right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final int[][] leftData = left.getData();
		final double rightData = right.getData();

		final double[][] result = new double[leftData.length][leftData[0].length];

		outerLoopProfile.profileCounted(leftData.length);
		innerLoopProfile.profileCounted(leftData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < leftData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < leftData[0].length); j++) {
				result[i][j] = doBinaryDouble(leftData[i][j], rightData);
			}
		}

		return new NV2Real(result);
	}


	@Specialization
	protected NV2Int doBinary(NV2Int left, NV2Int right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final int[][] leftData = left.getData();
		final int[][] rightData = right.getData();

		final int[][] result = new int[leftData.length][leftData[0].length];

		outerLoopProfile.profileCounted(leftData.length);
		innerLoopProfile.profileCounted(leftData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < leftData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < leftData[0].length); j++) {
				result[i][j] = doBinaryInt(leftData[i][j], rightData[i][j]);
			}
		}

		return new NV2Int(result);
	}

	@Specialization
	protected NV2Real doBinary(NV2Real left, NV0Int right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final double[][] leftData = left.getData();
		final int rightData = right.getData();

		final double[][] result = new double[leftData.length][leftData[0].length];

		outerLoopProfile.profileCounted(leftData.length);
		innerLoopProfile.profileCounted(leftData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < leftData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < leftData[0].length); j++) {
				result[i][j] = doBinaryDouble(leftData[i][j], rightData);
			}
		}

		return new NV2Real(result);
	}

	@Specialization
	protected NV2Real doBinary(NV2Real left, NV0Real right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final double[][] leftData = left.getData();
		final double rightData = right.getData();

		final double[][] result = new double[leftData.length][leftData[0].length];

		outerLoopProfile.profileCounted(leftData.length);
		innerLoopProfile.profileCounted(leftData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < leftData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < leftData[0].length); j++) {
				result[i][j] = doBinaryDouble(leftData[i][j], rightData);
			}
		}

		return new NV2Real(result);
	}

	@Specialization
	protected NV2Real doBinary(NV2Real left, NV2Real right, //
			@Cached("createCountingProfile()") LoopConditionProfile outerLoopProfile, //
			@Cached("createCountingProfile()") LoopConditionProfile innerLoopProfile) {
		final double[][] leftData = left.getData();
		final double[][] rightData = right.getData();

		final double[][] result = new double[leftData.length][leftData[0].length];

		outerLoopProfile.profileCounted(leftData.length);
		innerLoopProfile.profileCounted(leftData[0].length);
		for (int i = 0; outerLoopProfile.inject(i < leftData.length); i++) {
			for (int j = 0; innerLoopProfile.inject(j < leftData[0].length); j++) {
				result[i][j] = doBinaryDouble(leftData[i][j], rightData[i][j]);
			}
		}

		return new NV2Real(result);
	}
}
