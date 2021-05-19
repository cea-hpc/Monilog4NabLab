package fr.cea.nabla.interpreter.nodes.instruction;

import java.util.Map;

import org.graalvm.polyglot.Value;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;

import fr.cea.nabla.interpreter.NablaTypesGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaExpressionNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadVariableNode;
import fr.cea.nabla.interpreter.nodes.interop.NodeObjectDescriptor;
import fr.cea.nabla.interpreter.values.NV0Int;
import fr.cea.nabla.interpreter.values.NV0Real;
import fr.cea.nabla.interpreter.values.NV1Int;
import fr.cea.nabla.interpreter.values.NV1IntJava;
import fr.cea.nabla.interpreter.values.NV1Real;
import fr.cea.nabla.interpreter.values.NV1RealJava;
import fr.cea.nabla.interpreter.values.NV2Int;
import fr.cea.nabla.interpreter.values.NV2Real;
import fr.cea.nabla.interpreter.values.NV3Real;
import fr.cea.nabla.interpreter.values.NV4Real;

@GenerateWrapper
@NodeChild(value = "value", type = NablaExpressionNode.class)
@NodeChild(value = "toWrite", type = NablaReadVariableNode.class)
public abstract class NablaWriteArrayNode extends NablaInstructionNode implements InstrumentableNode {

	private final FrameSlot slot;
	private final Class<?> baseType;
	private final int dimensions;
	@Children
	private final NablaExpressionNode[] indices;

	public NablaWriteArrayNode(FrameSlot slot, Class<?> baseType, int dimensions, NablaExpressionNode[] indices) {
		this.slot = slot;
		this.baseType = baseType;
		this.dimensions = dimensions;
		this.indices = indices;
	}

	protected NablaWriteArrayNode() {
		this.slot = null;
		this.baseType = null;
		this.dimensions = 0;
		this.indices = null;
	}

	@Specialization(guards = "isNV1Int()")
	protected NV1Int writeNV1Int(VirtualFrame frame, NV0Int value, NV1IntJava toWrite) {
		final int idx = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		toWrite.getData()[idx] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV2Int()")
	protected NV2Int writeNV2Int(VirtualFrame frame, NV0Int value, NV2Int toWrite) {
		final int idx1 = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		final int idx2 = NablaTypesGen.asNV0Int(indices[1].executeGeneric(frame)).getData();
		toWrite.getData()[idx1][idx2] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV2Int()")
	protected NV2Int writeNV2Int(VirtualFrame frame, NV1IntJava value, NV2Int toWrite) {
		final int idx = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		toWrite.getData()[idx] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV1Real()")
	protected NV1Real writeNV1Real(VirtualFrame frame, NV0Real value, NV1RealJava toWrite) {
		final int idx = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		toWrite.getData()[idx] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV1Real()")
	protected Value writeNV1Real(VirtualFrame frame, NV0Real value, Value toWrite) {
		final int idx = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		toWrite.setArrayElement(idx, value.getData());
		return toWrite;
	}

	@Specialization(guards = "isNV2Real()")
	protected NV2Real writeNV2Real(VirtualFrame frame, NV0Real value, NV2Real toWrite) {
		final int idx1 = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		final int idx2 = NablaTypesGen.asNV0Int(indices[1].executeGeneric(frame)).getData();
		toWrite.getData()[idx1][idx2] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV2Real()")
	protected NV2Real writeNV2Real(VirtualFrame frame, NV1RealJava value, NV2Real toWrite) {
		final int idx = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		toWrite.getData()[idx] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV3Real()")
	protected NV3Real writeNV3Real(VirtualFrame frame, NV0Real value, NV3Real toWrite) {
		final int idx1 = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		final int idx2 = NablaTypesGen.asNV0Int(indices[1].executeGeneric(frame)).getData();
		final int idx3 = NablaTypesGen.asNV0Int(indices[2].executeGeneric(frame)).getData();
		toWrite.getData()[idx1][idx2][idx3] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV3Real()")
	protected NV3Real writeNV3Real(VirtualFrame frame, NV1RealJava value, NV3Real toWrite) {
		final int idx1 = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		final int idx2 = NablaTypesGen.asNV0Int(indices[1].executeGeneric(frame)).getData();
		toWrite.getData()[idx1][idx2] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV3Real()")
	protected NV3Real writeNV3Real(VirtualFrame frame, NV2Real value, NV3Real toWrite) {
		final int idx = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		toWrite.getData()[idx] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV4Real()")
	protected NV4Real writeNV4Real(VirtualFrame frame, NV0Real value, NV4Real toWrite) {
		final int idx1 = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		final int idx2 = NablaTypesGen.asNV0Int(indices[1].executeGeneric(frame)).getData();
		final int idx3 = NablaTypesGen.asNV0Int(indices[2].executeGeneric(frame)).getData();
		final int idx4 = NablaTypesGen.asNV0Int(indices[3].executeGeneric(frame)).getData();
		toWrite.getData()[idx1][idx2][idx3][idx4] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV4Real()")
	protected NV4Real writeNV4Real(VirtualFrame frame, NV1RealJava value, NV4Real toWrite) {
		final int idx1 = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		final int idx2 = NablaTypesGen.asNV0Int(indices[1].executeGeneric(frame)).getData();
		final int idx3 = NablaTypesGen.asNV0Int(indices[2].executeGeneric(frame)).getData();
		toWrite.getData()[idx1][idx2][idx3] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV4Real()")
	protected NV4Real writeNV4Real(VirtualFrame frame, NV2Real value, NV4Real toWrite) {
		final int idx1 = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		final int idx2 = NablaTypesGen.asNV0Int(indices[1].executeGeneric(frame)).getData();
		toWrite.getData()[idx1][idx2] = value.getData();
		return toWrite;
	}

	@Specialization(guards = "isNV4Real()")
	protected NV4Real writeNV4Real(VirtualFrame frame, NV3Real value, NV4Real toWrite) {
		final int idx1 = NablaTypesGen.asNV0Int(indices[0].executeGeneric(frame)).getData();
		toWrite.getData()[idx1] = value.getData();
		return toWrite;
	}

	protected boolean isNV1Int() {
		return baseType == int.class && dimensions == 1;
	}

	protected boolean isNV2Int() {
		return baseType == int.class && dimensions == 2;
	}

	protected boolean isNV3Int() {
		return baseType == int.class && dimensions == 3;
	}

	protected boolean isNV4Int() {
		return baseType == int.class && dimensions == 4;
	}

	protected boolean isNV1Real() {
		return baseType == double.class && dimensions == 1;
	}

	protected boolean isNV2Real() {
		return baseType == double.class && dimensions == 2;
	}

	protected boolean isNV3Real() {
		return baseType == double.class && dimensions == 3;
	}

	protected boolean isNV4Real() {
		return baseType == double.class && dimensions == 4;
	}

	@Override
	public Map<String, Object> getDebugProperties() {
		Map<String, Object> debugProperties = super.getDebugProperties();
		debugProperties.put("variableSlot", slot);
		return debugProperties;
	}

	@Override
	public boolean isInstrumentable() {
		return true;
	}

	@Override
	public boolean hasTag(Class<? extends Tag> tag) {
		return tag.equals(StandardTags.WriteVariableTag.class) || super.hasTag(tag);
	}

	@Override
	public WrapperNode createWrapper(ProbeNode probeNode) {
		return new NablaWriteArrayNodeWrapper(this, probeNode);
	}

	@Override
	public Object getNodeObject() {
		return NodeObjectDescriptor.writeVariable(slot.getIdentifier().toString());
	}
}
