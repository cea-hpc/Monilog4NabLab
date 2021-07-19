package fr.cea.nabla.interpreter.runtime;

import org.eclipse.xtext.util.Strings;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import com.google.gson.JsonObject;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import fr.cea.nabla.ir.ir.Iterator;
//import fr.cea.nabla.javalib.mesh.CartesianMesh2D;
//import fr.cea.nabla.javalib.mesh.Quad;
import fr.cea.nabla.javalib.mesh.CartesianMesh2D;

public class CartesianMesh2DWrapper {

	@CompilationFinal
	private Value meshWrapper;

	@TruffleBoundary
	public CartesianMesh2DWrapper() {
	}

	@TruffleBoundary
	public void initialize(JsonObject jsonMesh) {
		assert (jsonMesh != null);
		CompilerDirectives.transferToInterpreterAndInvalidate();
		final CartesianMesh2D mesh = new CartesianMesh2D();
		mesh.jsonInit(jsonMesh.toString());
		this.meshWrapper = Context.getCurrent().asValue(mesh);
	}

	public Value getMeshWrapper() {
		return meshWrapper;
	}

	// FIXME ugly getNb
	public Value connectivityGetter(String connectivityName) {
		if (connectivityName.equals("nodes") || connectivityName.equals("cells")) {
			return meshWrapper.getMember("getNb" + Strings.toFirstUpper(connectivityName));
		} else if (connectivityName.startsWith("Max")) {
			return Context.getCurrent().asValue(this).getMember("get" + connectivityName);
		} else if (connectivityName.startsWith("get")) {
			if (connectivityName.equals("getNodes")) {
				return Context.getCurrent().asValue(this).getMember(connectivityName);
			} else {
				return meshWrapper.getMember(connectivityName);
			}
		} else {
			return meshWrapper.getMember("get" + Strings.toFirstUpper(connectivityName));
		}
	}

//	@TruffleBoundary
//	public int[] getElements(final String connectivityName, final int[] args) {
//		int[] _switchResult = null;
//		if (connectivityName == null || connectivityName.isEmpty() ) {
//			throw new IllegalArgumentException("Connectivity name can't be null or empty");
//		} else {
//			switch (connectivityName) {
//			case "nodes":
//				_switchResult = this.mesh.getNodes();
//				break;
//			case "cells":
//				_switchResult = this.mesh.getCells();
//				break;
//			case "faces":
//				_switchResult = this.mesh.getFaces();
//				break;
//			case "innerNodes":
//				_switchResult = this.mesh.getInnerNodes();
//				break;
//			case "outerFaces":
//				_switchResult = this.mesh.getOuterFaces();
//				break;
//			case "nodesOfCell":
//				_switchResult = this.mesh.getNodesOfCell(args[0]);
//				break;
//			case "cellsOfNode":
//				_switchResult = this.mesh.getCellsOfNode(args[0]);
//				break;
//			case "nodesOfFace":
//				_switchResult = this.mesh.getNodesOfFace(args[0]);
//				break;
//			case "neighbourCells":
//				_switchResult = this.mesh.getNeighbourCells(args[0]);
//				break;
//			case "cellsOfFace":
//				_switchResult = this.mesh.getCellsOfFace(args[0]);
//				break;
//			default:
//				throw new RuntimeException((("Not implemented yet (" + connectivityName) + ")"));
//			}
//		}
//		return _switchResult;
//	}

	@TruffleBoundary
	public int getIndexOf(final Iterator iterator, final int id) {
		throw new RuntimeException("Not implemented yet");
	}

//	@TruffleBoundary
//	public int getSingleton(final String connectivityName, final int[] params) {
//		int _switchResult = (int) 0;
//		if (connectivityName != null) {
//			switch (connectivityName) {
//			case "commonFace":
//				_switchResult = this.mesh.getCommonFace(params[0], params[1]);
//				break;
//			default:
//				throw new RuntimeException((("Not implemented yet (" + connectivityName) + ")"));
//			}
//		} else {
//			throw new RuntimeException((("Not implemented yet (" + connectivityName) + ")"));
//		}
//		return _switchResult;
//	}

	@TruffleBoundary
	public Value getNodes() {
		return meshWrapper.invokeMember("getGeometry").invokeMember("getNodes");
	}

	@TruffleBoundary
	public Value getQuads() {
		return meshWrapper.invokeMember("getGeometry").invokeMember("getQuads");
	}

//	@TruffleBoundary
//	public int getNbElems(final String connectivityName) {
//		int _switchResult = (int) 0;
//		if (connectivityName != null) {
//			switch (connectivityName) {
//			case "nodes":
//				_switchResult = this.mesh.getNbNodes();
//				break;
//			case "cells":
//				_switchResult = this.mesh.getNbCells();
//				break;
//			case "faces":
//				_switchResult = this.mesh.getNbFaces();
//				break;
//			case "outerFaces":
//				_switchResult = this.mesh.getNbOuterFaces();
//				break;
//			case "innerNodes":
//				_switchResult = this.mesh.getNbInnerNodes();
//				break;
//			default:
//				throw new RuntimeException("Not implemented yet");
//			}
//		} else {
//			throw new RuntimeException("Not implemented yet");
//		}
//		return _switchResult;
//	}

	public int getMaxNbNodesOfCell() {
		return CartesianMesh2D.MaxNbNodesOfCell;
	}

	public int getMaxNbNodesOfFace() {
		return CartesianMesh2D.MaxNbNodesOfFace;
	}

	public int getMaxNbCellsOfNode() {
		return CartesianMesh2D.MaxNbCellsOfNode;
	}

	public int getMaxNbCellsOfFace() {
		return CartesianMesh2D.MaxNbCellsOfFace;
	}

	public int getMaxNbNeighbourCells() {
		return CartesianMesh2D.MaxNbNeighbourCells;
	}

	public int getMaxNbFacesOfCell() {
		return CartesianMesh2D.MaxNbFacesOfCell;
	}
	
	@TruffleBoundary
	public int getMaxNbElems(final String connectivityName) {
		int _switchResult = (int) 0;
		if (connectivityName != null) {
			switch (connectivityName) {
			case "MaxNbNodesOfCell":
				_switchResult = CartesianMesh2D.MaxNbNodesOfCell;
				break;
			case "MaxNbNodesOfFace":
				_switchResult = CartesianMesh2D.MaxNbNodesOfFace;
				break;
			case "MaxNbCellsOfNode":
				_switchResult = CartesianMesh2D.MaxNbCellsOfNode;
				break;
			case "MaxNbCellsOfFace":
				_switchResult = CartesianMesh2D.MaxNbCellsOfFace;
				break;
			case "MaxNbNeighbourCells":
				_switchResult = CartesianMesh2D.MaxNbNeighbourCells;
				break;
			case "MaxNbFacesOfCell":
				_switchResult = CartesianMesh2D.MaxNbFacesOfCell;
				break;
			default:
				throw new RuntimeException("Not implemented yet");
			}
		} else {
			throw new RuntimeException("Not implemented yet");
		}
		return _switchResult;
	}

//	@TruffleBoundary
//	public Quad[] getQuads() {
//		return this.mesh.getGeometry().getQuads();
//	}
}
