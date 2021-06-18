package fr.cea.nabla.interpreter.nodes.instruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.nodes.BlockNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.NodeVisitor;

import fr.cea.nabla.interpreter.nodes.NablaNode;
import fr.cea.nabla.interpreter.nodes.local.NablaScopedNode;

public class NablaInstructionBlockNode extends NablaInstructionNode
		implements BlockNode.ElementExecutor<NablaInstructionNode> {

	@Child
	private BlockNode<NablaInstructionNode> block;

	/**
	 * All declared variables visible from this block (including all parent blocks).
	 * Variables declared in this block only are from zero index up to
	 * {@link #parentBlockIndex} (exclusive).
	 */
	@CompilationFinal(dimensions = 1)
	private NablaWriteVariableNode[] writeNodesCache;
	
	/**
	 * Index of the parent block's variables in the {@link #writeNodesCache list of
	 * variables}.
	 */
	@CompilationFinal
	private int parentBlockIndex = -1;

	public NablaInstructionBlockNode(NablaInstructionNode[] bodyNodes) {
		this.block = bodyNodes.length > 0 ? BlockNode.create(bodyNodes, this) : null;
	}

	public NablaInstructionBlockNode() {
	}

	@Override
	public Object executeGeneric(VirtualFrame frame) {
		if (this.block != null) {
			return this.block.executeGeneric(frame, BlockNode.NO_ARGUMENT);
		}
		return null;
	}

	public List<NablaInstructionNode> getStatements() {
		if (block == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(Arrays.asList(block.getElements()));
	}

	public Object executeGeneric(VirtualFrame frame, NablaInstructionNode node, int index, int argument) {
		return node.executeGeneric(frame);
	}

	@Override
	public void executeVoid(VirtualFrame frame, NablaInstructionNode node, int index, int argument) {
		node.executeGeneric(frame);
	}
	
	@Override
	public boolean isInstrumentable() {
		return false;
	}

	/**
	 * All declared local variables accessible in this block. Variables declared in
	 * parent blocks are included.
	 */
	public NablaWriteVariableNode[] getDeclaredLocalVariables() {
		NablaWriteVariableNode[] writeNodes = writeNodesCache;
		if (writeNodes == null) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			writeNodesCache = writeNodes = findDeclaredLocalVariables();
		}
		return writeNodes;
	}

	public int getParentBlockIndex() {
		return parentBlockIndex;
	}

	private NablaWriteVariableNode[] findDeclaredLocalVariables() {
		NablaInstructionBlockNode blockNode = this;
		if (block == null) {
			if (blockNode instanceof WrapperNode) {
				blockNode = (NablaInstructionBlockNode) ((WrapperNode) blockNode).getDelegateNode();
			} else {
				return new NablaWriteVariableNode[] {};
			}
		}
		// Search for those write nodes, which declare variables
		List<NablaWriteVariableNode> writeNodes = new ArrayList<>(4);
		int[] varsIndex = new int[] { 0 };
		NodeUtil.forEachChild(blockNode.block, new NodeVisitor() {
			@Override
			public boolean visit(Node node) {
				if (!(node instanceof NablaNode)) {
					return true;
				}
				if (node instanceof WrapperNode) {
					NodeUtil.forEachChild(node, this);
					return true;
				}
				if (node instanceof NablaScopedNode) {
					NablaScopedNode scopedNode = (NablaScopedNode) node;
					scopedNode.setVisibleVariablesIndexOnEnter(varsIndex[0]);
				}
				// Do not enter any nested blocks.
				if (!(node instanceof NablaInstructionBlockNode)) {
					NodeUtil.forEachChild(node, this);
				}
				// Write to a variable is a declaration unless it exists already in a parent
				// scope.
				if (node instanceof NablaWriteVariableNode) {
					NablaWriteVariableNode wn = (NablaWriteVariableNode) node;
					if (writeNodes.stream().noneMatch(n -> wn.getSlot().equals(n.getSlot()))) {
						writeNodes.add(wn);
						varsIndex[0]++;
					}
				}
				if (node instanceof NablaScopedNode) {
					NablaScopedNode scopedNode = (NablaScopedNode) node;
					scopedNode.setVisibleVariablesIndexOnExit(varsIndex[0]);
				}
				return true;
			}
		});
		Node parentBlock = findBlock();
		NablaWriteVariableNode[] parentVariables = null;
		if (parentBlock instanceof NablaInstructionBlockNode) {
			parentVariables = ((NablaInstructionBlockNode) parentBlock).getDeclaredLocalVariables();
		}
		NablaWriteVariableNode[] variables = writeNodes.toArray(new NablaWriteVariableNode[writeNodes.size()]);
		parentBlockIndex = variables.length;
		if (parentVariables == null || parentVariables.length == 0) {
			return variables;
		} else {
			int parentVariablesIndex = ((NablaInstructionBlockNode) parentBlock).getParentBlockIndex();
			int visibleVarsIndex = getVisibleVariablesIndexOnEnter();
			int allVarsLength = variables.length + visibleVarsIndex + parentVariables.length - parentVariablesIndex;
			NablaWriteVariableNode[] allVariables = Arrays.copyOf(variables, allVarsLength);
			System.arraycopy(parentVariables, 0, allVariables, variables.length, visibleVarsIndex);
			System.arraycopy(parentVariables, parentVariablesIndex, allVariables, variables.length + visibleVarsIndex,
					parentVariables.length - parentVariablesIndex);
			return allVariables;
		}
	}
}
