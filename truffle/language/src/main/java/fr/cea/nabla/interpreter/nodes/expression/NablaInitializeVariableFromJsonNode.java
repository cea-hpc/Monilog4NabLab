package fr.cea.nabla.interpreter.nodes.expression;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.runtime.NablaContext;
import fr.cea.nabla.interpreter.values.NV0Bool;
import fr.cea.nabla.interpreter.values.NV0Int;
import fr.cea.nabla.interpreter.values.NV0Real;
import fr.cea.nabla.interpreter.values.NV1Bool;
import fr.cea.nabla.interpreter.values.NV1IntJava;
import fr.cea.nabla.interpreter.values.NV1IntLibrary;
import fr.cea.nabla.interpreter.values.NV1RealJava;
import fr.cea.nabla.interpreter.values.NV1RealLibrary;
import fr.cea.nabla.interpreter.values.NV2Bool;
import fr.cea.nabla.interpreter.values.NV2Int;
import fr.cea.nabla.interpreter.values.NV2Real;
import fr.cea.nabla.interpreter.values.NV3Bool;
import fr.cea.nabla.interpreter.values.NV3Int;
import fr.cea.nabla.interpreter.values.NV3Real;
import fr.cea.nabla.interpreter.values.NV4Bool;
import fr.cea.nabla.interpreter.values.NV4Int;
import fr.cea.nabla.interpreter.values.NV4Real;

@NodeChild(value = "value", type = NablaExpressionNode.class)
public abstract class NablaInitializeVariableFromJsonNode extends NablaExpressionNode {

	private final String[] optionPath;

	public NablaInitializeVariableFromJsonNode(String[] optionPath) {
		this.optionPath = optionPath;
	}

	protected NablaInitializeVariableFromJsonNode() {
		this.optionPath = null;
	}

	@Specialization
	protected Object doInitialize(NV0Bool value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		return new NV0Bool(initialValue.getAsBoolean());
	}

	@Specialization
	protected Object doInitialize(NV1Bool value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final boolean[] data = new boolean[value.getData().length];
		for (int i = 0; i < data.length; i++) {
			data[i] = ja.get(i).getAsBoolean();
		}
		return new NV1Bool(data);
	}

	@Specialization
	protected Object doInitialize(NV2Bool value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final boolean[][] valueData = value.getData();
		final boolean[][] data = new boolean[valueData.length][valueData[0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = ja.get(i).getAsJsonArray().get(j).getAsBoolean();
			}
		}
		return new NV2Bool(data);
	}

	@Specialization
	protected Object doInitialize(NV3Bool value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final boolean[][][] valueData = value.getData();
		final boolean[][][] data = new boolean[valueData.length][valueData[0].length][valueData[0][0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; j++) {
					data[i][j][k] = ja.get(i).getAsJsonArray().get(j).getAsJsonArray().get(k).getAsBoolean();
				}
			}
		}
		return new NV3Bool(data);
	}

	@Specialization
	protected Object doInitialize(NV4Bool value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final boolean[][][][] valueData = value.getData();
		final boolean[][][][] data = new boolean[valueData.length][valueData[0].length][valueData[0][0].length][valueData[0][0][0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; j++) {
					for (int l = 0; l < data[i][j][k].length; j++) {
						data[i][j][k][l] = ja.get(i).getAsJsonArray().get(j).getAsJsonArray().get(k).getAsJsonArray().get(l).getAsBoolean();
					}
				}
			}
		}
		return new NV4Bool(data);
	}

	@Specialization
	protected Object doInitialize(NV0Int value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		return new NV0Int(initialValue.getAsInt());
	}

	@Specialization(guards = "arrays.isArray(value)", limit = "3")
	protected Object doInitialize(Object value, @CachedLibrary("value") NV1IntLibrary arrays,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final int[] data = new int[arrays.length(value)];
		for (int i = 0; i < data.length; i++) {
			data[i] = ja.get(i).getAsInt();
		}
		return new NV1IntJava(data);
	}

	@Specialization
	protected Object doInitialize(NV2Int value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final int[][] valueData = value.getData();
		final int[][] data = new int[valueData.length][valueData[0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = ja.get(i).getAsJsonArray().get(j).getAsInt();
			}
		}
		return new NV2Int(data);
	}

	@Specialization
	protected Object doInitialize(NV3Int value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final int[][][] valueData = value.getData();
		final int[][][] data = new int[valueData.length][valueData[0].length][valueData[0][0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; j++) {
					data[i][j][k] = ja.get(i).getAsJsonArray().get(j).getAsJsonArray().get(k).getAsInt();
				}
			}
		}
		return new NV3Int(data);
	}

	@Specialization
	protected Object doInitialize(NV4Int value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final int[][][][] valueData = value.getData();
		final int[][][][] data = new int[valueData.length][valueData[0].length][valueData[0][0].length][valueData[0][0][0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; j++) {
					for (int l = 0; l < data[i][j][k].length; j++) {
						data[i][j][k][l] = ja.get(i).getAsJsonArray().get(j).getAsJsonArray().get(k).getAsJsonArray().get(l).getAsInt();
					}
				}
			}
		}
		return new NV4Int(data);
	}

	@Specialization
	protected Object doInitialize(NV0Real value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		return new NV0Real(initialValue.getAsDouble());
	}
	
	@Specialization(guards = "arrays.isArray(value)", limit = "3")
	protected Object doInitialize(Object value, @CachedLibrary("value") NV1RealLibrary arrays,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final double[] data = new double[arrays.length(value)];
		for (int i = 0; i < data.length; i++) {
			data[i] = ja.get(i).getAsDouble();
		}
		return new NV1RealJava(data);
	}

	@Specialization
	protected Object doInitialize(NV2Real value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final double[][] valueData = value.getData();
		final double[][] data = new double[valueData.length][valueData[0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = ja.get(i).getAsJsonArray().get(j).getAsDouble();
			}
		}
		return new NV2Real(data);
	}

	@Specialization
	protected Object doInitialize(NV3Real value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final double[][][] valueData = value.getData();
		final double[][][] data = new double[valueData.length][valueData[0].length][valueData[0][0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; j++) {
					data[i][j][k] = ja.get(i).getAsJsonArray().get(j).getAsJsonArray().get(k).getAsDouble();
				}
			}
		}
		return new NV3Real(data);
	}

	@Specialization
	protected Object doInitialize(NV4Real value,//
			@CachedContext(NablaLanguage.class) NablaContext context) {
		final JsonElement initialValue = context.getOption(optionPath);
		final JsonArray ja = initialValue.getAsJsonArray();
		final double[][][][] valueData = value.getData();
		final double[][][][] data = new double[valueData.length][valueData[0].length][valueData[0][0].length][valueData[0][0][0].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; j++) {
					for (int l = 0; l < data[i][j][k].length; j++) {
						data[i][j][k][l] = ja.get(i).getAsJsonArray().get(j).getAsJsonArray().get(k).getAsJsonArray().get(l).getAsDouble();
					}
				}
			}
		}
		return new NV4Real(data);
	}

}
