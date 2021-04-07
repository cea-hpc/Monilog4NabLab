package fr.cea.nabla.interpreter.values;

import org.graalvm.polyglot.Value;

import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.nodes.expression.NablaExpressionNode;
import fr.cea.nabla.interpreter.runtime.NablaContext;

@NodeChild(value = "expression", type = NablaExpressionNode.class)
public abstract class CreateNablaValueNode extends NablaExpressionNode {

	protected final Class<?> expectedType;
	
	protected CreateNablaValueNode(Class<?> expectedType) {
		this.expectedType = expectedType;
	}
	
	@Specialization
	protected NablaValue createNablaValue(NablaValue v) {
		return v;
	}

	@Specialization(guards = { "value.isHostObject()", "expectsDouble1()" })
	protected Object doHostObjectValueDouble1(Value value) {
		return new NV1RealJava(value.asHostObject());
	}

	@Specialization(guards = { "value.isHostObject()", "expectsDouble2()" })
	protected Object doHostObjectValueDouble2(Value value) {
		return new NV2Real(value.asHostObject());
	}
	
	@Specialization(guards = { "value.isHostObject()", "expectsInt1()" })
	protected Object doHostObjectValueInt1(Value value) {
		return new NV1IntJava(value.asHostObject());
	}
	
	@Specialization(guards = { "value.isHostObject()", "expectsInt2()" })
	protected Object doHostObjectValueInt2(Value value) {
		return new NV2Int(value.asHostObject());
	}
	
	@Specialization(guards = "env.isHostObject(value)")
	protected Object doHostObject(Object value, //
			@CachedContext(NablaLanguage.class) NablaContext context, //
			@Cached("context.getEnv()") Env env) {
		return env.asHostObject(value);
	}
	
	@Specialization
	protected NablaValue createNablaValue(Boolean x) {
		return new NV0Bool(x);
	}

	@Specialization
	protected NablaValue createNablaValue(boolean[] x) {
		return new NV1Bool(x);
	}

	@Specialization
	protected NablaValue createNablaValue(boolean[][] x) {
		return new NV2Bool(x);
	}

	@Specialization
	protected NablaValue createNablaValue(Integer x) {
		return new NV0Int(x);
	}

	@Specialization
	protected NablaValue createNablaValue(Long x) {
		return new NV0Int(x.intValue());
	}

	@Specialization(guards = "x.fitsInInt()")
	protected NablaValue createNablaValue(Value x) {
		return new NV0Int(x.asInt());
	}

	@Specialization
	protected NablaValue createNablaValue(int[] x) {
		return new NV1IntJava(x);
	}

	@Specialization(guards = { "expectsInt1", "nativePointer", "isArray" })
	protected NablaValue createNablaValueInt1(Value v, //
			@Cached("expectsInt1()") boolean expectsInt1, //
			@Cached("v.isNativePointer()") boolean nativePointer, //
			@Cached("v.hasArrayElements()") boolean isArray) {
		return new NV1IntNative(v);
	}

	@Specialization(guards = { "expectsInt1()", "pointers.isPointer(v)", "pointers.hasArrayElements(v)" })
	protected NablaValue createNablaValueInt1(Object v, @CachedLibrary(limit="3") InteropLibrary pointers) {
		return new NV1IntNative(Value.asValue(v));
	}

	@Specialization
	protected NablaValue createNablaValue(int[][] x) {
		return new NV2Int(x);
	}

	@Specialization
	protected NablaValue createNablaValue(Double x) {
		return new NV0Real(x);
	}

	@Specialization
	protected NablaValue createNablaValue(double[] x) {
		return new NV1RealJava(x);
	}

	@Specialization(guards = { "expectsDouble1()", "v.isNativePointer()", "v.hasArrayElements()" })
	protected NablaValue createNablaValueDouble1(Value v) {
		return new NV1RealNative(v);
	}

	@Specialization(guards = { "expectsDouble1()", "pointers.isPointer(v)", "pointers.hasArrayElements(v)" })
	protected NablaValue createNablaValueDouble1(Object v, @CachedLibrary(limit="3") InteropLibrary pointers) {
		return new NV1RealNative(Value.asValue(v));
	}

	@Specialization
	protected NablaValue createNablaValue(double[][] x) {
		return new NV2Real(x);
	}
	
	protected boolean expectsInt1() {
		return expectedType.equals(int[].class);
	}
	
	protected boolean expectsInt2() {
		return expectedType.equals(int[][].class);
	}
	
	protected boolean expectsDouble1() {
		return expectedType.equals(double[].class);
	}
	
	protected boolean expectsDouble2() {
		return expectedType.equals(double[][].class);
	}

}
