package fr.cea.nabla.interpreter.nodes.local;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.NodeLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.nodes.NablaModuleNode;
import fr.cea.nabla.interpreter.nodes.NablaNode;
import fr.cea.nabla.interpreter.nodes.NablaRootNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaInstructionBlockNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWriteVariableNode;
import fr.cea.nabla.interpreter.runtime.NablaContext;
import fr.cea.nabla.interpreter.runtime.NablaNull;

@ExportLibrary(value = NodeLibrary.class)
public abstract class NablaScopedNode extends NablaNode {

	/**
	 * Index to the the {@link NablaInstructionBlockNode#getDeclaredLocalVariables()
	 * block's variables} that determine variables belonging into this scope
	 * (excluding parent scopes) on node enter. The scope variables are in the
	 * interval &lt;0, visibleVariablesIndexOnEnter).
	 */
	@CompilationFinal
	private volatile int visibleVariablesIndexOnEnter = -1;
	/**
	 * Similar to {@link #visibleVariablesIndexOnEnter}, but determines variables on
	 * node exit. The scope variables are in the interval &lt;0,
	 * visibleVariablesIndexOnExit).
	 */
	@CompilationFinal
	private volatile int visibleVariablesIndexOnExit = -1;

	/**
	 * For performance reasons, fix the library implementation for the particular
	 * node.
	 */
	@ExportMessage
	boolean accepts(@Shared("node") @Cached(value = "this", adopt = false) NablaScopedNode cachedNode) {
		return this == cachedNode;
	}

	/**
	 * We do provide a scope.
	 */
	@ExportMessage
	@SuppressWarnings("static-method")
	public boolean hasScope(@SuppressWarnings("unused") Frame frame) {
		return true;
	}

	/**
	 * The scope depends on the current node and the node's block. Cache the node
	 * and its block for fast access. Depending on the block node, we create either
	 * block variables, or function arguments (in the RootNode, but outside of a
	 * block).
	 */
	@ExportMessage
	@SuppressWarnings("static-method")
	final Object getScope(Frame frame, boolean nodeEnter,
			@Shared("node") @Cached(value = "this", adopt = false) NablaScopedNode cachedNode,
			@Cached(value = "this.findBlock()", adopt = false, allowUncached = true) Node blockNode) {
		if (cachedNode instanceof NablaModuleNode) {
			return new VariablesObject(frame, cachedNode, nodeEnter, (NablaModuleNode) cachedNode);
		} else if (blockNode instanceof NablaInstructionBlockNode) {
			return new VariablesObject(frame, cachedNode, nodeEnter, (NablaInstructionBlockNode) blockNode);
		} else {
			return new ArgumentsObject(frame, (NablaRootNode) blockNode);
		}
	}

	/**
	 * Test if a function of that name exists. The functions are context-dependent,
	 * therefore do a context lookup via {@link CachedContext}.
	 */
	@ExportMessage
	@TruffleBoundary
	final boolean hasRootInstance(@SuppressWarnings("unused") Frame frame,
			@CachedContext(NablaLanguage.class) ContextReference<NablaContext> contextRef) {
		String functionName = getRootNode().getName();
		NablaContext context = contextRef.get();
		// The instance of the current RootNode is a function of the same name.
		return context.getFunctionRegistry().getFunction(functionName) != null;
	}

	/**
	 * Provide function instance of that name. The function is context-dependent,
	 * therefore do a context lookup via {@link CachedContext}.
	 */
	@ExportMessage
	@TruffleBoundary
	final Object getRootInstance(@SuppressWarnings("unused") Frame frame,
			@CachedContext(NablaLanguage.class) ContextReference<NablaContext> contextRef)
			throws UnsupportedMessageException {
		String functionName = getRootNode().getName();
		NablaContext context = contextRef.get();
		// The instance of the current RootNode is a function of the same name.
		Object function = context.getFunctionRegistry().getFunction(functionName);
		if (function != null) {
			return function;
		} else {
			throw UnsupportedMessageException.create();
		}
	}

	/**
	 * Find block of this node. Traverse the parent chain and find the first
	 * {@link NablaInstructionBlockNode}. If none is found, {@link RootNode} is
	 * returned.
	 *
	 * @return the block node, always non-null. Either NablaInstructionBlockNode, or
	 *         NablaRootNode.
	 */
	public final Node findBlock() {
		Node parent = getParent();
		while (parent != null) {
			if (parent instanceof NablaInstructionBlockNode) {
				break;
			}
			Node p = parent.getParent();
			if (p == null) {
				assert parent instanceof RootNode : String.format("Not adopted node under %s.", parent);
				return parent;
			}
			parent = p;
		}
		return parent;
	}

	/**
	 * Set the index to the the
	 * {@link NablaInstructionBlockNode#getDeclaredLocalVariables() block's
	 * variables} that determine variables belonging into this scope (excluding
	 * parent scopes) on node enter.
	 */
	public final void setVisibleVariablesIndexOnEnter(int index) {
		assert visibleVariablesIndexOnEnter == -1 : "The index is set just once";
		assert 0 <= index;
		visibleVariablesIndexOnEnter = index;
	}

	/**
	 * Similar to {@link #setVisibleVariablesIndexOnEnter(int)}, but determines
	 * variables on node exit.
	 */
	public final void setVisibleVariablesIndexOnExit(int index) {
		assert visibleVariablesIndexOnExit == -1 : "The index is set just once";
		assert 0 <= index;
		visibleVariablesIndexOnExit = index;
	}

	/**
	 * Provide the index that determines variables on node enter.
	 */
	protected final int getVisibleVariablesIndexOnEnter() {
		return visibleVariablesIndexOnEnter;
	}

	/**
	 * Scope of function arguments. This scope is provided by nodes just under a
	 * {@link NablaRootNode}, outside of a {@link NablaInstructionBlockNode block}.
	 * <p>
	 * The arguments declared by {@link NablaRootNode#getDeclaredArguments() root
	 * node} are provided as scope members.
	 */
	@ExportLibrary(InteropLibrary.class)
	static final class ArgumentsObject implements TruffleObject {

		/**
		 * The member caching limit.
		 */
		static int LIMIT = 3;

		private final Frame frame;
		protected final NablaRootNode root;

		/**
		 * The arguments depend on the current frame and root node.
		 */
		ArgumentsObject(Frame frame, NablaRootNode root) {
			this.frame = frame;
			this.root = root;
		}

		/**
		 * For performance reasons, fix the library implementation for the particular
		 * root node.
		 */
		@ExportMessage
		boolean accepts(@Cached(value = "this.root", adopt = false) NablaRootNode cachedRoot) {
			return this.root == cachedRoot;
		}

		/**
		 * This is a scope object, providing arguments as members.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean isScope() {
			return true;
		}

		/**
		 * The scope must provide an associated language.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean hasLanguage() {
			return true;
		}

		@ExportMessage
		@SuppressWarnings("static-method")
		Class<? extends TruffleLanguage<?>> getLanguage() {
			return NablaLanguage.class;
		}

		/**
		 * Provide the function name as the scope's display string.
		 */
		@ExportMessage
		Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects) {
			return root.getName();
		}

		/**
		 * We provide a source section of this scope.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean hasSourceLocation() {
			return true;
		}

		/**
		 * @return Source section of the function.
		 */
		@ExportMessage
		SourceSection getSourceLocation() {
			return root.getSourceSection();
		}

		/**
		 * Scope must provide scope members.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean hasMembers() {
			return true;
		}

		/**
		 * We return an array of argument objects as scope members.
		 */
		@ExportMessage
		Object getMembers(@SuppressWarnings("unused") boolean includeInternal) {
			NablaWriteVariableNode[] writeNodes = root.getDeclaredArguments();
			return new KeysArray(writeNodes, writeNodes.length, writeNodes.length);
		}

		/**
		 * Test if a member exists. We cache the result for fast access.
		 */
		@ExportMessage(name = "isMemberReadable")
		static final class ExistsMember {

			/**
			 * If the member is cached, provide the cached result. Call
			 * {@link #doGeneric(ArgumentsObject, String)} otherwise.
			 */
			@Specialization(limit = "LIMIT", guards = { "cachedMember.equals(member)" })
			@SuppressWarnings("unused")
			static boolean doCached(ArgumentsObject receiver, String member, @Cached("member") String cachedMember,
					// We cache the member existence for fast-path access
					@Cached("doGeneric(receiver, member)") boolean cachedResult) {
				assert cachedResult == doGeneric(receiver, member);
				return cachedResult;
			}

			/**
			 * Test if an argument with that name exists.
			 */
			@Specialization(replaces = "doCached")
			@TruffleBoundary
			static boolean doGeneric(ArgumentsObject receiver, String member) {
				return receiver.hasArgumentIndex(member);
			}
		}

		/**
		 * Test if a member is modifiable. We cache the result for fast access.
		 */
		@ExportMessage(name = "isMemberModifiable")
		static final class ModifiableMember {

			@Specialization(limit = "LIMIT", guards = { "cachedMember.equals(member)" })
			@SuppressWarnings("unused")
			static boolean doCached(ArgumentsObject receiver, String member, @Cached("member") String cachedMember,
					// We cache the member existence for fast-path access
					@Cached("receiver.hasArgumentIndex(member)") boolean cachedResult) {
				return cachedResult && receiver.frame != null;
			}

			/**
			 * Test if an argument can be modified (it must exist and we must have a frame).
			 */
			@Specialization(replaces = "doCached")
			@TruffleBoundary
			static boolean doGeneric(ArgumentsObject receiver, String member) {
				return receiver.findArgumentIndex(member) >= 0 && receiver.frame != null;
			}
		}

		/**
		 * We can not insert new arguments.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean isMemberInsertable(@SuppressWarnings("unused") String member) {
			return false;
		}

		/**
		 * Read an argument value. Cache the argument's index for fast access.
		 */
		@ExportMessage(name = "readMember")
		static final class ReadMember {

			/**
			 * If the member is cached, use the cached index and read the value at that
			 * index. Call {@link #doGeneric(ArgumentsObject, String)} otherwise.
			 */
			@Specialization(limit = "LIMIT", guards = { "cachedMember.equals(member)" })
			@SuppressWarnings("unused")
			static Object doCached(ArgumentsObject receiver, String member, @Cached("member") String cachedMember,
					// We cache the member's index for fast-path access
					@Cached("receiver.findArgumentIndex(member)") int index) throws UnknownIdentifierException {
				return doRead(receiver, cachedMember, index);
			}

			/**
			 * Find the member's index and read the value at that index.
			 */
			@Specialization(replaces = "doCached")
			@TruffleBoundary
			static Object doGeneric(ArgumentsObject receiver, String member) throws UnknownIdentifierException {
				int index = receiver.findArgumentIndex(member);
				return doRead(receiver, member, index);
			}

			/**
			 * Read the argument at the provided index from {@link Frame#getArguments()}
			 * array.
			 */
			private static Object doRead(ArgumentsObject receiver, String member, int index)
					throws UnknownIdentifierException {
				if (index < 0) {
					throw UnknownIdentifierException.create(member);
				}
				if (receiver.frame != null) {
					return receiver.frame.getArguments()[index];
				} else {
					return NablaNull.SINGLETON;
				}
			}
		}

		/**
		 * Write not supported.
		 */
		@ExportMessage
		void writeMember(String member, Object value)
				throws UnsupportedMessageException, UnknownIdentifierException, UnsupportedTypeException {
			throw UnsupportedMessageException.create();
		}

		boolean hasArgumentIndex(String member) {
			return findArgumentIndex(member) >= 0;
		}

		int findArgumentIndex(String member) {
			NablaWriteVariableNode[] writeNodes = root.getDeclaredArguments();
			for (int i = 0; i < writeNodes.length; i++) {
				NablaWriteVariableNode writeNode = writeNodes[i];
				if (member.equals(writeNode.getSlot().getIdentifier())) {
					// First two arguments reserved for internal stuff.
					return i + 2;
				}
			}
			return -1;
		}

	}

	/**
	 * Scope of all variables accessible in the scope from the entered or exited
	 * node.
	 */
	@ExportLibrary(InteropLibrary.class)
	static final class VariablesObject implements TruffleObject {

		/**
		 * The member caching limit.
		 */
		static int LIMIT = 4;

		protected final Frame frame; // the current frame
		protected final NablaScopedNode node; // the current node
		final boolean nodeEnter; // whether the node was entered or is about to be exited
		protected final NablaInstructionBlockNode block; // the inner-most block of the current node
		protected final NablaModuleNode module; // the inner-most block of the current node

		VariablesObject(Frame frame, NablaScopedNode node, boolean nodeEnter, NablaInstructionBlockNode blockNode) {
			this.frame = frame;
			this.node = node;
			this.nodeEnter = nodeEnter;
			this.block = blockNode;
			this.module = null;
		}

		VariablesObject(Frame frame, NablaScopedNode node, boolean nodeEnter, NablaModuleNode moduleNode) {
			this.frame = frame;
			this.node = node;
			this.nodeEnter = nodeEnter;
			this.block = null;
			this.module = moduleNode;
		}

		/**
		 * For performance reasons, fix the library implementation for the current node
		 * and enter flag.
		 */
		@ExportMessage
		boolean accepts(@Cached(value = "this.node", adopt = false) NablaScopedNode cachedNode,
				@Cached(value = "this.nodeEnter") boolean cachedNodeEnter) {
			return this.node == cachedNode && this.nodeEnter == cachedNodeEnter;
		}

		/**
		 * This is a scope object, providing variables as members.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean isScope() {
			return true;
		}

		/**
		 * The scope must provide an associated language.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean hasLanguage() {
			return true;
		}

		@ExportMessage
		@SuppressWarnings("static-method")
		Class<? extends TruffleLanguage<?>> getLanguage() {
			return NablaLanguage.class;
		}

		/**
		 * Provide either "block", or the function name as the scope's display string.
		 */
		@ExportMessage
		@SuppressWarnings({ "static-method", "unused" })
		Object toDisplayString(boolean allowSideEffects,
				@Shared("block") @Cached(value = "this.block", adopt = false) NablaInstructionBlockNode cachedBlock,
				@Shared("parentBlock") @Cached(value = "this.block.findBlock()", adopt = false, allowUncached = true) Node parentBlock) {
			// Cache the parent block for the fast-path access
			if (parentBlock instanceof NablaInstructionBlockNode) {
				return "block";
			} else {
				return ((NablaRootNode) parentBlock).getName();
			}
		}

		/**
		 * There is a parent scope if we're in a block.
		 */
		@ExportMessage
		@SuppressWarnings({ "static-method", "unused" })
		boolean hasScopeParent(
				@Shared("block") @Cached(value = "this.block", adopt = false) NablaInstructionBlockNode cachedBlock,
				@Shared("parentBlock") @Cached(value = "this.block.findBlock()", adopt = false, allowUncached = true) Node parentBlock) {
			// Cache the parent block for the fast-path access
			return parentBlock instanceof NablaInstructionBlockNode;
		}

		/**
		 * The parent scope object is based on the parent block node.
		 */
		@ExportMessage
		@SuppressWarnings("unused")
		Object getScopeParent(
				@Shared("block") @Cached(value = "this.block", adopt = false) NablaInstructionBlockNode cachedBlock,
				@Shared("parentBlock") @Cached(value = "this.block.findBlock()", adopt = false, allowUncached = true) Node parentBlock)
				throws UnsupportedMessageException {
			// Cache the parent block for the fast-path access
			if (!(parentBlock instanceof NablaInstructionBlockNode)) {
				throw UnsupportedMessageException.create();
			}
			return new VariablesObject(frame, cachedBlock, true, (NablaInstructionBlockNode) parentBlock);
		}

		/**
		 * We provide a source section of this scope.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean hasSourceLocation() {
			return true;
		}

		/**
		 * The source section of this scope is either the block, or the function.
		 */
		@ExportMessage
		@TruffleBoundary
		SourceSection getSourceLocation() {
			if (block == null) {
				return module.getSourceSection();
			} else {
				Node parentBlock = block.findBlock();
				if (parentBlock instanceof RootNode) {
					// We're in the function block
					assert parentBlock instanceof NablaRootNode
							: String.format("In NablaLanguage we expect NablaRootNode, not %s", parentBlock.getClass());
					return ((NablaRootNode) parentBlock).getSourceSection();
				} else {
					return block.getSourceSection();
				}
			}
		}

		/**
		 * Scope must provide scope members.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean hasMembers() {
			return true;
		}

		/**
		 * Test if a member exists. We cache the result for fast access.
		 */
		@ExportMessage(name = "isMemberReadable")
		static final class ExistsMember {

			/**
			 * If the member is cached, provide the cached result. Call
			 * {@link #doGeneric(VariablesObject, String)} otherwise.
			 */
			@Specialization(limit = "LIMIT", guards = { "cachedMember.equals(member)" })
			@SuppressWarnings("unused")
			static boolean doCached(VariablesObject receiver, String member, @Cached("member") String cachedMember,
					// We cache the member existence for fast-path access
					@Cached("doGeneric(receiver, member)") boolean cachedResult) {
				assert cachedResult == doGeneric(receiver, member);
				return cachedResult;
			}

			/**
			 * Test if a variable with that name exists. It exists if we have a
			 * corresponding write node.
			 */
			@Specialization(replaces = "doCached")
			@TruffleBoundary
			static boolean doGeneric(VariablesObject receiver, String member) {
				return receiver.hasWriteNode(member);
			}
		}

		/**
		 * Test if a member is modifiable. We cache the result for fast access.
		 */
		@ExportMessage(name = "isMemberModifiable")
		static final class ModifiableMember {

			@Specialization(limit = "LIMIT", guards = { "cachedMember.equals(member)" })
			@SuppressWarnings("unused")
			static boolean doCached(VariablesObject receiver, String member, @Cached("member") String cachedMember,
					// We cache the member existence for fast-path access
					@Cached("receiver.hasWriteNode(member)") boolean cachedResult) {
				return cachedResult && receiver.frame != null;
			}

			/**
			 * Test if a variable with that name exists and we have a frame.
			 */
			@Specialization(replaces = "doCached")
			@TruffleBoundary
			static boolean doGeneric(VariablesObject receiver, String member) {
				return receiver.hasWriteNode(member) && receiver.frame != null;
			}
		}

//		/**
//		 * We do not support member modification.
//		 */
//		@ExportMessage
//		@SuppressWarnings("static-method")
//		boolean isMemberModifiable(@SuppressWarnings("unused") String member) {
//			return false;
//		}

		/**
		 * We do not support insertion of new variables.
		 */
		@ExportMessage
		@SuppressWarnings("static-method")
		boolean isMemberInsertable(@SuppressWarnings("unused") String member) {
			return false;
		}

		/**
		 * Read a variable value. Cache the frame slot by variable name for fast access.
		 */
		@ExportMessage(name = "readMember")
		static final class ReadMember {

			/**
			 * If the member is cached, use the cached frame slot and read the value from
			 * it. Call {@link #doGeneric(VariablesObject, String)} otherwise.
			 */
			@Specialization(limit = "LIMIT", guards = { "cachedMember.equals(member)" })
			@SuppressWarnings("unused")
			static Object doCached(VariablesObject receiver, String member, @Cached("member") String cachedMember,
					// We cache the member's frame slot for fast-path access
					@Cached("receiver.findSlot(member)") FrameSlot slot) throws UnknownIdentifierException {
				return doRead(receiver, cachedMember, slot);
			}

			/**
			 * The uncached version finds the member's slot and read the value from it.
			 */
			@Specialization(replaces = "doCached")
			@TruffleBoundary
			static Object doGeneric(VariablesObject receiver, String member) throws UnknownIdentifierException {
				FrameSlot slot = receiver.findSlot(member);
				return doRead(receiver, member, slot);
			}

			private static Object doRead(VariablesObject receiver, String member, FrameSlot slot)
					throws UnknownIdentifierException {
				if (slot == null) {
					throw UnknownIdentifierException.create(member);
				}
				if (receiver.frame != null) {
					return receiver.frame.getValue(slot);
				} else {
					return NablaNull.SINGLETON;
				}
			}
		}

		/**
		 * Write a variable value. Cache the write node by variable name for fast
		 * access.
		 */
		@ExportMessage(name = "writeMember")
		static final class WriteMember {

			@Specialization(limit = "LIMIT", guards = { "cachedMember.equals(member)" })
			@SuppressWarnings("unused")
			static void doCached(VariablesObject receiver, String member, Object value,
					@Cached("member") String cachedMember,
					@Cached(value = "receiver.frame.getFrameDescriptor().findFrameSlot(cachedMember)", allowUncached = true) FrameSlot slot)
					throws UnknownIdentifierException, UnsupportedMessageException {
				receiver.frame.setObject(slot, value);
			}

			@Specialization(replaces = "doCached")
			@SuppressWarnings("unused")
			static void doUncached(VariablesObject receiver, String member, Object value)
					throws UnknownIdentifierException, UnsupportedMessageException {
				final FrameSlot slot = receiver.frame.getFrameDescriptor().findFrameSlot(member);
				receiver.frame.setObject(slot, value);
			}
		}

		@ExportMessage(name = "getMembers")
		static final class GetMembers {

			@Specialization(guards = { "receiver.block != null" })
			@SuppressWarnings("unused")
			static Object doBlock(VariablesObject receiver, @SuppressWarnings("unused") boolean includeInternal,
					@Cached(value = "receiver.block.getDeclaredLocalVariables()", adopt = false, dimensions = 1, allowUncached = true) NablaWriteVariableNode[] writeNodes,
					@Cached(value = "receiver.getVisibleVariablesIndex()", allowUncached = true) int visibleVariablesIndex,
					@Cached(value = "receiver.block.getParentBlockIndex()", allowUncached = true) int parentBlockIndex) {
				return new KeysArray(writeNodes, visibleVariablesIndex, parentBlockIndex);
			}

			@Specialization(guards = { "receiver.block == null" })
			@SuppressWarnings("unused")
			static Object doModule(VariablesObject receiver, @SuppressWarnings("unused") boolean includeInternal,
					@Cached(value = "receiver.module.getDeclaredGlobalVariables()", adopt = false, dimensions = 1, allowUncached = true) NablaWriteVariableNode[] writeNodes) {
				return new KeysArray(writeNodes, writeNodes.length, writeNodes.length);
			}
		}

		int getVisibleVariablesIndex() {
			assert node.visibleVariablesIndexOnEnter >= 0;
			assert node.visibleVariablesIndexOnExit >= 0;
			return nodeEnter ? node.visibleVariablesIndexOnEnter : node.visibleVariablesIndexOnExit;
		}

		boolean hasWriteNode(String member) {
			return findWriteNode(member) != null;
		}

		FrameSlot findSlot(String member) {
			NablaWriteVariableNode writeNode = findWriteNode(member);
			if (writeNode != null) {
				return writeNode.getSlot();
			} else {
				return null;
			}
		}

		/**
		 * Find write node, which declares variable of the given name. Search through
		 * the variables declared in the block and its parents and return the first one
		 * that matches.
		 * 
		 * @param member the variable name
		 */
		NablaWriteVariableNode findWriteNode(String member) {
			NablaWriteVariableNode[] writeNodes;
			int parentBlockIndex;
			if (block == null) {
				writeNodes = module.getDeclaredGlobalVariables();
				parentBlockIndex = writeNodes.length;
			} else {
				writeNodes = block.getDeclaredLocalVariables();
				parentBlockIndex = block.getParentBlockIndex();
			}
			int index = getVisibleVariablesIndex();
			for (int i = 0; i < index; i++) {
				NablaWriteVariableNode writeNode = writeNodes[i];
				if (member.equals(writeNode.getSlot().getIdentifier())) {
					return writeNode;
				}
			}
			for (int i = parentBlockIndex; i < writeNodes.length; i++) {
				NablaWriteVariableNode writeNode = writeNodes[i];
				if (member.equals(writeNode.getSlot().getIdentifier())) {
					return writeNode;
				}
			}
			return null;
		}
	}

	/**
	 * Array of visible variables. The variables are based on their declaration
	 * write nodes and are represented as {@link Key} objects.
	 */
	@ExportLibrary(InteropLibrary.class)
	static final class KeysArray implements TruffleObject {

		@CompilationFinal(dimensions = 1)
		private final NablaWriteVariableNode[] writeNodes;
		private final int variableIndex;
		private final int parentBlockIndex;

		/**
		 * Creates a new array of visible variables.
		 * 
		 * @param writeNodes       all variables declarations in the scope, including
		 *                         parent scopes.
		 * @param variableIndex    index to the variables array, determining variables
		 *                         in the inner-most scope (from zero index up to the
		 *                         <code>variableIndex</code>, exclusive).
		 * @param parentBlockIndex index to the variables array, determining variables
		 *                         in the parent block's scope (from
		 *                         <code>parentBlockIndex</code> to the end of the
		 *                         <code>writeNodes</code> array).
		 */
		KeysArray(NablaWriteVariableNode[] writeNodes, int variableIndex, int parentBlockIndex) {
			this.writeNodes = writeNodes;
			this.variableIndex = variableIndex;
			this.parentBlockIndex = parentBlockIndex;
		}

		@SuppressWarnings("static-method")
		@ExportMessage
		boolean hasArrayElements() {
			return true;
		}

		@ExportMessage
		long getArraySize() {
			// We see all parent's variables (writeNodes.length - parentBlockIndex) plus the
			// variables in the inner-most scope visible by the current node
			// (variableIndex).
			return writeNodes.length - parentBlockIndex + variableIndex;
		}

		@ExportMessage
		boolean isArrayElementReadable(long index) {
			return index >= 0 && index < (writeNodes.length - parentBlockIndex + variableIndex);
		}

		@ExportMessage
		Object readArrayElement(long index) throws InvalidArrayIndexException {
			if (!isArrayElementReadable(index)) {
				throw InvalidArrayIndexException.create(index);
			}
			if (index < variableIndex) {
				// if we're in the inner-most scope, it's simply the variable on the index
				return new Key(writeNodes[(int) index]);
			} else {
				// else it's a variable declared in the parent's scope, we start at
				// parentBlockIndex
				return new Key(writeNodes[(int) index - variableIndex + parentBlockIndex]);
			}
		}

	}

	/**
	 * Representation of a variable based on a {@link NablaWriteVariableNode write
	 * node} that declares the variable. It provides the variable name as a
	 * {@link Key#asString() string} and the name node associated with the
	 * variable's write node as a {@link Key#getSourceLocation() source location}.
	 */
	@ExportLibrary(InteropLibrary.class)
	static final class Key implements TruffleObject {

		private final NablaWriteVariableNode writeNode;

		Key(NablaWriteVariableNode writeNode) {
			this.writeNode = writeNode;
		}

		@ExportMessage
		@SuppressWarnings("static-method")
		boolean isString() {
			return true;
		}

		@ExportMessage
		@TruffleBoundary
		String asString() {
			// FrameSlot's identifier object is not safe to convert to String on fast-path.
			return writeNode.getSlot().getIdentifier().toString();
		}

		@ExportMessage
		boolean hasSourceLocation() {
			return writeNode.getSourceCharIndex() >= 0;
		}

		@ExportMessage
		@TruffleBoundary
		SourceSection getSourceLocation() throws UnsupportedMessageException {
			if (!hasSourceLocation()) {
				throw UnsupportedMessageException.create();
			}
			return writeNode.getRootNode().getSourceSection().getSource().createSection(writeNode.getSourceCharIndex(),
					writeNode.getSourceLength());
		}
	}
}
