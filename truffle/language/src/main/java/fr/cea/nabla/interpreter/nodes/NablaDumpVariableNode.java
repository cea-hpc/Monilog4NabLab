package fr.cea.nabla.interpreter.nodes;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadVariableNode;
import fr.cea.nabla.interpreter.runtime.CartesianMesh2DWrapper;
import fr.cea.nabla.interpreter.runtime.NablaContext;
import fr.cea.nabla.interpreter.values.NV1RealJava;
import fr.cea.nabla.interpreter.values.NV2Real;
import fr.cea.nabla.javalib.mesh.PvdFileWriter2D;

@NodeChild(value = "value", type = NablaReadVariableNode.class)
public abstract class NablaDumpVariableNode extends NablaNode {

	private final String outputName;
	private final int size;
	private final boolean node;
	
	protected NablaDumpVariableNode(String outputName, int size, boolean node) {
		this.outputName = outputName;
		this.size = size;
		this.node = node;
	}
	
	@Specialization
	public Object dumpVariables(VirtualFrame frame, NV1RealJava value, //
			@CachedContext(NablaLanguage.class) NablaContext context, //
			@Cached("context.getMeshWrapper()") CartesianMesh2DWrapper meshWrapper, //
			@Cached("getLength(meshWrapper)") long length, //
			@Cached("context.getWriter()") PvdFileWriter2D w) {
		if (node) {
			w.openNodeArray(outputName, size);
			for (int i = 0; i < length; i++) {
				w.write(value.getData()[i]);
			}	
			w.closeNodeArray();
		} else {
			w.openCellArray(outputName, size);
			for (int i = 0; i < length; i++) {
				w.write(value.getData()[i]);
			}	
			w.closeCellArray();
		}
		return null;
	}
	
	@Specialization
	public Object dumpVariables(VirtualFrame frame, NV2Real value, //
			@CachedContext(NablaLanguage.class) NablaContext context, //
			@Cached("context.getMeshWrapper()") CartesianMesh2DWrapper meshWrapper, //
			@Cached("getLength(meshWrapper)") long length, //
			@Cached("context.getWriter()") PvdFileWriter2D w) {
		if (node) {
			w.openNodeArray(outputName, size);
			for (int i = 0; i < length; i++) {
				w.write(value.getData()[i]);
			}	
			w.closeNodeArray();
		} else {
			w.openCellArray(outputName, size);
			for (int i = 0; i < length; i++) {
				w.write(value.getData()[i]);
			}	
			w.closeCellArray();
		}
		return null;
	}
	
	protected long getLength(CartesianMesh2DWrapper meshWrapper) {
		if (node) {
			return meshWrapper.getNodes().getArraySize();
		} else {
			return meshWrapper.getQuads().getArraySize();
		}
	}

}
