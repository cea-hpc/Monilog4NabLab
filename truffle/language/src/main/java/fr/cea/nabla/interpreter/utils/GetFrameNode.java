package fr.cea.nabla.interpreter.utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;

import fr.cea.nabla.interpreter.runtime.NablaInitializationPerformedException;

public abstract class GetFrameNode extends Node {

	@CompilationFinal
	protected int depth = 0;
	private final String slotName;

	public GetFrameNode(String name) {
		this.slotName = name;
	}

	public abstract Frame execute(VirtualFrame frame);
	
	@Specialization(rewriteOn = NablaInitializationPerformedException.class)
	protected Frame initialize(VirtualFrame frame) throws NablaInitializationPerformedException {
		CompilerDirectives.transferToInterpreterAndInvalidate();
		final FrameDescriptor descriptor = frame.getFrameDescriptor();
		if (descriptor.findFrameSlot(slotName) == null) {
			Frame theFrame = frame;
			while (theFrame.getArguments().length != 0) {
				theFrame = (Frame) theFrame.getArguments()[0];
				depth++;
			}
		}
		throw new NablaInitializationPerformedException();
	}

	@ExplodeLoop
	@Specialization
	protected Frame doCached(VirtualFrame frame) {
		Frame theFrame = frame;
		for (int i = 0; i < depth; i++) {
			theFrame = (Frame) theFrame.getArguments()[0];
		}
		return theFrame;
	}
}