package fr.cea.nabla.interpreter.nodes;

import java.io.FileNotFoundException;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadVariableNode;
import fr.cea.nabla.interpreter.runtime.CartesianMesh2DWrapper;
import fr.cea.nabla.interpreter.runtime.NablaContext;
import fr.cea.nabla.interpreter.values.NV0Int;
import fr.cea.nabla.interpreter.values.NV0Real;
import fr.cea.nabla.interpreter.values.NV2Real;
import fr.cea.nabla.javalib.mesh.PvdFileWriter2D;
import fr.cea.nabla.javalib.mesh.Quad;

@NodeChild(value = "index", type = NablaReadVariableNode.class)
@NodeChild(value = "time", type = NablaReadVariableNode.class)
@NodeChild(value = "coords", type = NablaReadVariableNode.class)
public abstract class NablaDumpVariablesNode extends NablaNode {

	@Children
	private NablaDumpVariableNode[] dumpNodeVariables;

	@Children
	private NablaDumpVariableNode[] dumpCellVariables;
	
	public NablaDumpVariablesNode(NablaDumpVariableNode[] dumpNodeVariables, NablaDumpVariableNode[] dumpCellVariables) {
		this.dumpNodeVariables = dumpNodeVariables;
		this.dumpCellVariables = dumpCellVariables;
	}

	@ExplodeLoop
	@Specialization
	public Object dumpVariables(VirtualFrame frame, NV0Int iteration, NV0Real time, NV2Real coords, //
			@CachedContext(NablaLanguage.class) NablaContext context, //
			@Cached("context.getMeshWrapper()") CartesianMesh2DWrapper meshWrapper, //
			@Cached(value="getQuads(meshWrapper)", dimensions = 1) Quad[] quads, //
			@Cached("context.getWriter()") PvdFileWriter2D w) {
		if (((w != null) && (!w.isDisabled()))) {
//			TODO
//			val periodValue = context.getNumber(ppInfo.periodValue)
//			val periodReference = context.getNumber(ppInfo.periodReference)
//			val lastDump = context.getNumber(ppInfo.lastDumpVariable)
//			if (periodReference >= lastDump + periodValue)
			try {
				w.startVtpFile(iteration.getData(), time.getData(), coords.getData(), quads);

				w.openNodeData();
				for (int i = 0; i < dumpNodeVariables.length; i++) {
					dumpNodeVariables[i].executeGeneric(frame);
				}
				w.closeNodeData();

				w.openCellData();
				for (int i = 0; i < dumpCellVariables.length; i++) {
					dumpCellVariables[i].executeGeneric(frame);
				}
				w.closeCellData();
				
				w.closeVtpFile();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	protected Quad[] getQuads(CartesianMesh2DWrapper meshWrapper) {
		return meshWrapper.getQuads().asHostObject();
	}

}
