package fr.cea.nabla.interpreter.nodes;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.NodeVisitor;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadArgumentNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaInstructionNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaPrologBlockNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWriteVariableNode;

@NodeInfo(language = "Nabla", description = "The root of all Nabla execution trees")
public class NablaRootNode extends RootNode {

	@Child
	private NablaPrologBlockNode prologNode;

	@Child
	private NablaInstructionNode bodyNode;

	@CompilationFinal
	private boolean isCloningAllowed = true;

	private final String name;

	@CompilationFinal
	private SourceSection sourceSection;

	@CompilationFinal(dimensions = 1)
	private volatile NablaWriteVariableNode[] argumentNodesCache;

	public NablaRootNode(TruffleLanguage<?> language, FrameDescriptor frameDescriptor, NablaPrologBlockNode prologNode,
			NablaInstructionNode bodyNode, String name) {
		super(language, frameDescriptor);
		this.name = name;
		this.prologNode = prologNode;
		this.bodyNode = bodyNode;
		if (this.bodyNode != null) {
			bodyNode.addRootBodyTag();
		}
	}

	@Override
	public SourceSection getSourceSection() {
		return sourceSection;
	}

	public final void setSourceSection(SourceSection sourceSection) {
		assert this.sourceSection == null : "source must only be set once";
		this.sourceSection = sourceSection;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		assert lookupContextReference(NablaLanguage.class).get() != null;
		if (prologNode != null) {
			prologNode.executeGeneric(frame);
		}
		return bodyNode.executeGeneric(frame);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isCloningAllowed() {
		return isCloningAllowed;
	}

	public void setCloningAllowed(boolean isCloningAllowed) {
		this.isCloningAllowed = isCloningAllowed;
	}

	public final NablaWriteVariableNode[] getDeclaredArguments() {
		NablaWriteVariableNode[] argumentNodes = argumentNodesCache;
		if (argumentNodes == null) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			argumentNodesCache = argumentNodes = findArgumentNodes();
		}
		return argumentNodes;
	}

	private NablaWriteVariableNode[] findArgumentNodes() {
		if (this.prologNode == null) {
			return new NablaWriteVariableNode[0];
		}
		List<NablaWriteVariableNode> writeArgNodes = new ArrayList<>(4);
		NodeUtil.forEachChild(this.prologNode, new NodeVisitor() {

			private NablaWriteVariableNode wn; // The current write node containing a slot

			@Override
			public boolean visit(Node node) {
				// When there is a write node, search for NablaReadArgumentNode among its
				// children:
				if (node instanceof InstrumentableNode.WrapperNode) {
					return NodeUtil.forEachChild(node, this);
				}
				if (node instanceof NablaWriteVariableNode) {
					wn = (NablaWriteVariableNode) node;
					boolean all = NodeUtil.forEachChild(node, this);
					wn = null;
					return all;
				} else if (wn != null && (node instanceof NablaReadArgumentNode)) {
					writeArgNodes.add(wn);
					return true;
				} else {
					return NodeUtil.forEachChild(node, this);
				}
			}
		});
		return writeArgNodes.toArray(new NablaWriteVariableNode[writeArgNodes.size()]);
	}

}
