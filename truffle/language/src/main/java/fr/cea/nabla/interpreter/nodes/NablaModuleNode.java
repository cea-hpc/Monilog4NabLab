package fr.cea.nabla.interpreter.nodes;

import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import fr.cea.nabla.interpreter.NablaTypes;
import fr.cea.nabla.interpreter.nodes.instruction.NablaInstructionNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWriteVariableNode;
import fr.cea.nabla.interpreter.nodes.job.NablaJobBlockNode;
import fr.cea.nabla.interpreter.runtime.NablaContext;
import fr.cea.nabla.interpreter.runtime.NablaNull;

@GenerateWrapper
@TypeSystemReference(NablaTypes.class)
public class NablaModuleNode extends NablaInstructionNode implements InstrumentableNode {

	private static final NablaWriteVariableNode[] EMPTY_ARRAY = new NablaWriteVariableNode[0];
	
	@Child
	private NablaModulePrologNode prologNode;
	
	@Child
	private NablaJobBlockNode jobBlock;

	public NablaModuleNode(NablaWriteVariableNode[] connectivitySizes, NablaWriteVariableNode[] optionDefinitions,
			NablaWriteVariableNode[] variableDefinitions, NablaRootNode[] jobs) {
		this.prologNode = new NablaModulePrologNode(connectivitySizes, optionDefinitions, variableDefinitions);
		this.jobBlock = new NablaJobBlockNode(jobs);
	}

	protected NablaModuleNode() {
	}

	@ExplodeLoop
	@Override
	public Object executeGeneric(VirtualFrame frame) {

		final MaterializedFrame globalFrame = frame.materialize();
		NablaContext.getCurrent().setGlobalFrame(this, globalFrame);

		prologNode.executeGeneric(globalFrame);

		jobBlock.executeGeneric(globalFrame);

		return NablaNull.SINGLETON;
	}

	@Override
	public WrapperNode createWrapper(ProbeNode probe) {
		return new NablaModuleNodeWrapper(this, probe);
	}

	@Override
	public boolean hasTag(Class<? extends Tag> tag) {
		return tag.equals(StandardTags.RootTag.class) || super.hasTag(tag);
	}

	public NablaWriteVariableNode[] getDeclaredGlobalVariables() {
		final NablaWriteVariableNode[] wn = Streams.stream(prologNode.getChildren()).map(n -> (NablaWriteVariableNode) n)
				.collect(Collectors.toList())
				.toArray(EMPTY_ARRAY);
		setVisibleVariablesIndexOnEnter(wn.length);
		setVisibleVariablesIndexOnExit(wn.length);
		return wn;
	}

}
