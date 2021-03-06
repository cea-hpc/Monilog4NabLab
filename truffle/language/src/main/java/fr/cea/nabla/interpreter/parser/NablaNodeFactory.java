package fr.cea.nabla.interpreter.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.xtext.util.Strings;

import com.google.common.collect.Iterators;
import com.google.gson.JsonObject;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.source.Source;

import fr.cea.nabla.interpreter.NablaLanguage;
import fr.cea.nabla.interpreter.NablaOptions;
import fr.cea.nabla.interpreter.nodes.NablaModuleNode;
import fr.cea.nabla.interpreter.nodes.NablaNode;
import fr.cea.nabla.interpreter.nodes.NablaRootNode;
import fr.cea.nabla.interpreter.nodes.NablaUndefinedFunctionRootNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaBool1NodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaBool2NodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaContractedIfNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaExpressionNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaFunctionCallNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaIndexOfNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaInitializeVariableFromJsonNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaInitializeVariableFromJsonNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaInt1NodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaInt2NodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaJNICallNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaJavaMethodCallNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaMeshWrapperCallNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaParenthesisNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadArgumentNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadArgumentNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadArrayDimensionNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadArrayDimensionNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadArrayNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadVariableNode;
import fr.cea.nabla.interpreter.nodes.expression.NablaReadVariableNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaReal1NodeGen;
import fr.cea.nabla.interpreter.nodes.expression.NablaReal2NodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.ArithmeticOperator;
import fr.cea.nabla.interpreter.nodes.expression.binary.BooleanOperator;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaBinaryArithmeticNode;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaBinaryArithmeticNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaBinaryBooleanNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaEqNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaGeqNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaGtNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaLeqNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaLtNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaMaxNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaMinNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaModNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.binary.NablaNeqNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaBaseTypeConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaBool1ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaBool2ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaBool3ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaBool4ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaBoolConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaInt1ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaInt2ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaInt3ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaInt4ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaIntConstantNode;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaIntConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaReal1ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaReal2ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaReal3ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaReal4ConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaRealConstantNode;
import fr.cea.nabla.interpreter.nodes.expression.constant.NablaRealConstantNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.unary.NablaAbsNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.unary.NablaMinusNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.unary.NablaNotNodeGen;
import fr.cea.nabla.interpreter.nodes.expression.unary.NablaSqrtNodeGen;
import fr.cea.nabla.interpreter.nodes.instruction.NablaExitNodeGen;
import fr.cea.nabla.interpreter.nodes.instruction.NablaIfNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaIfNodeGen;
import fr.cea.nabla.interpreter.nodes.instruction.NablaInstructionBlockNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaInstructionNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaLoopNodeGen;
import fr.cea.nabla.interpreter.nodes.instruction.NablaNopNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaPrologBlockNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaReturnNodeGen;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWhileNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWhileRepeatingNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWhileRepeatingNodeGen;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWriteArrayNodeGen;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWriteVariableNode;
import fr.cea.nabla.interpreter.nodes.instruction.NablaWriteVariableNodeGen;
import fr.cea.nabla.interpreter.nodes.job.NablaInstructionJobNode;
import fr.cea.nabla.interpreter.nodes.job.NablaJobBlockNode;
import fr.cea.nabla.interpreter.nodes.job.NablaJobNode;
import fr.cea.nabla.interpreter.nodes.job.NablaTimeLoopJobNode;
import fr.cea.nabla.interpreter.nodes.job.NablaTimeLoopJobRepeatingNode;
import fr.cea.nabla.interpreter.nodes.job.NablaTimeLoopJobRepeatingNodeGen;
import fr.cea.nabla.interpreter.runtime.NablaContext;
import fr.cea.nabla.interpreter.runtime.NablaJNIProviderObject;
import fr.cea.nabla.interpreter.runtime.NablaProviderObject;
import fr.cea.nabla.interpreter.utils.GetFrameNodeGen;
import fr.cea.nabla.interpreter.values.CreateNablaValueNodeGen;
import fr.cea.nabla.interpreter.values.FunctionCallHelper;
import fr.cea.nabla.ir.ContainerExtensions;
import fr.cea.nabla.ir.IrTypeExtensions;
import fr.cea.nabla.ir.ir.Affectation;
import fr.cea.nabla.ir.ir.Arg;
import fr.cea.nabla.ir.ir.ArgOrVar;
import fr.cea.nabla.ir.ir.ArgOrVarRef;
import fr.cea.nabla.ir.ir.BaseType;
import fr.cea.nabla.ir.ir.BaseTypeConstant;
import fr.cea.nabla.ir.ir.BinaryExpression;
import fr.cea.nabla.ir.ir.BoolConstant;
import fr.cea.nabla.ir.ir.Cardinality;
import fr.cea.nabla.ir.ir.Connectivity;
import fr.cea.nabla.ir.ir.ConnectivityCall;
import fr.cea.nabla.ir.ir.ConnectivityType;
import fr.cea.nabla.ir.ir.Container;
import fr.cea.nabla.ir.ir.ContractedIf;
import fr.cea.nabla.ir.ir.ExecuteTimeLoopJob;
import fr.cea.nabla.ir.ir.Exit;
import fr.cea.nabla.ir.ir.Expression;
import fr.cea.nabla.ir.ir.ExtensionProvider;
import fr.cea.nabla.ir.ir.ExternFunction;
import fr.cea.nabla.ir.ir.Function;
import fr.cea.nabla.ir.ir.FunctionCall;
import fr.cea.nabla.ir.ir.If;
import fr.cea.nabla.ir.ir.Instruction;
import fr.cea.nabla.ir.ir.InstructionBlock;
import fr.cea.nabla.ir.ir.IntConstant;
import fr.cea.nabla.ir.ir.InternFunction;
import fr.cea.nabla.ir.ir.Interval;
import fr.cea.nabla.ir.ir.IrAnnotable;
import fr.cea.nabla.ir.ir.IrAnnotation;
import fr.cea.nabla.ir.ir.IrPackage;
import fr.cea.nabla.ir.ir.IrRoot;
import fr.cea.nabla.ir.ir.IrType;
import fr.cea.nabla.ir.ir.ItemIdDefinition;
import fr.cea.nabla.ir.ir.ItemIdValue;
import fr.cea.nabla.ir.ir.ItemIdValueContainer;
import fr.cea.nabla.ir.ir.ItemIdValueIterator;
import fr.cea.nabla.ir.ir.ItemIndex;
import fr.cea.nabla.ir.ir.ItemIndexDefinition;
import fr.cea.nabla.ir.ir.IterationBlock;
import fr.cea.nabla.ir.ir.Iterator;
import fr.cea.nabla.ir.ir.Job;
import fr.cea.nabla.ir.ir.LinearAlgebraType;
import fr.cea.nabla.ir.ir.Loop;
import fr.cea.nabla.ir.ir.MaxConstant;
import fr.cea.nabla.ir.ir.MinConstant;
import fr.cea.nabla.ir.ir.Parenthesis;
import fr.cea.nabla.ir.ir.PrimitiveType;
import fr.cea.nabla.ir.ir.RealConstant;
import fr.cea.nabla.ir.ir.Return;
import fr.cea.nabla.ir.ir.SetDefinition;
import fr.cea.nabla.ir.ir.SetRef;
import fr.cea.nabla.ir.ir.UnaryExpression;
import fr.cea.nabla.ir.ir.Variable;
import fr.cea.nabla.ir.ir.VariableDeclaration;
import fr.cea.nabla.ir.ir.VectorConstant;
import fr.cea.nabla.ir.ir.While;

public class NablaNodeFactory {

	static class LexicalScope {
		protected final FrameDescriptor descriptor;
		protected final Map<String, FrameSlot> locals;
		protected final LexicalScope outer;

		LexicalScope(LexicalScope outer) {
			this(outer, false);
		}

		LexicalScope(LexicalScope outer, boolean newDescriptor) {
			this.outer = outer;
			this.locals = new HashMap<String, FrameSlot>() {
				private static final long serialVersionUID = 3595092172639258017L;

				@Override
				public FrameSlot put(String key, FrameSlot value) {
					return super.put(key, value);
				}
			};
			if (outer != null) {
				locals.putAll(outer.locals);
			}
			if (newDescriptor || outer == null) {
				this.descriptor = new FrameDescriptor();
			} else {
				this.descriptor = outer.descriptor;
			}
		}
	}

	private final Map<Function, NablaRootNode> functions = new HashMap<>();
	private final NablaLanguage language;

	private LexicalScope lexicalScope;

	private FrameDescriptor moduleFrameDescriptor;

	private final Source source;

	private final NablaExpressionNode[] emptyExpressionArray = new NablaExpressionNode[0];
	private final NablaWriteVariableNode[] emptyWriteArray = new NablaWriteVariableNode[0];
	private final NablaInstructionNode[] emptyInstructionArray = new NablaInstructionNode[0];

	public NablaNodeFactory(NablaLanguage language, Source source) {
		this.language = language;
		this.source = source;
	}

	private Variable collectSizeVariable(Expression expression) {
		if (expression instanceof ArgOrVarRef) {
			final ArgOrVarRef argOrVarRef = (ArgOrVarRef) expression;
			final ArgOrVar argOrVar = argOrVarRef.getTarget();
			if (argOrVar instanceof Variable) {
				return (Variable) argOrVar;
			}
		}
		return null;
	}

	private NablaExpressionNode createBaseTypeConstantNode(BaseTypeConstant baseTypeConstant) {
		final IrType type = baseTypeConstant.getType();
		NablaExpressionNode result = null;
		switch (type.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE: {
			final BaseType baseType = (BaseType) type;
			final List<NablaExpressionNode> sizes = baseType.getSizes().stream().map(s -> createNablaExpressionNode(s))
					.collect(Collectors.toList());
			final NablaExpressionNode value = createNablaExpressionNode(baseTypeConstant.getValue());
			switch (baseType.getPrimitive()) {
			case BOOL:
				if (sizes == null) {
					result = value;
				} else {
					switch (sizes.size()) {
					case 0:
						result = NablaBaseTypeConstantNodeGen.create(value);
						break;
					case 1:
						result = NablaBool1ConstantNodeGen.create(value, sizes.get(0));
						break;
					case 2:
						result = NablaBool2ConstantNodeGen.create(value, sizes.toArray(emptyExpressionArray));
						break;
					default:
						throw new UnsupportedOperationException();
					}
				}
				break;
			case INT:
				if (sizes == null) {
					result = value;
				} else {
					switch (sizes.size()) {
					case 0:
						result = NablaBaseTypeConstantNodeGen.create(value);
						break;
					case 1:
						result = NablaInt1ConstantNodeGen.create(value, sizes.get(0));
						break;
					case 2:
						result = NablaInt2ConstantNodeGen.create(value, sizes.toArray(emptyExpressionArray));
						break;
					default:
						throw new UnsupportedOperationException();
					}
				}
				break;
			case REAL:
				if (sizes == null) {
					result = value;
				} else {
					switch (sizes.size()) {
					case 0:
						result = NablaBaseTypeConstantNodeGen.create(value);
						break;
					case 1:
						result = NablaReal1ConstantNodeGen.create(value, sizes.get(0));
						break;
					case 2:
						result = NablaReal2ConstantNodeGen.create(value, sizes.toArray(emptyExpressionArray));
						break;
					default:
						throw new UnsupportedOperationException();
					}
				}
				break;
			default:
				throw new UnsupportedOperationException();
			}
			break;
		}
		case IrPackage.CONNECTIVITY_TYPE:
			throw new UnsupportedOperationException();
		}
		if (result == null) {
			throw new UnsupportedOperationException();
		}

		return result;
	}

	private NablaExpressionNode createGetIndexValueNode(ItemIdValueIterator itemIdValueIterator) {
		final FrameSlot indexSlot = lexicalScope.locals.get(itemIdValueIterator.getIterator().getIndex().getName());
		final NablaExpressionNode indexValueNode = getReadVariableNode(indexSlot);
		if (itemIdValueIterator.getShift() == 0) {
			return indexValueNode;
		}
		final Container container = itemIdValueIterator.getIterator().getContainer();
		switch (container.eClass().getClassifierID()) {
		case IrPackage.CONNECTIVITY_CALL: {
			final ConnectivityCall connectivityCall = (ConnectivityCall) container;
			final NablaExpressionNode nbElemsNode = getReadVariableNode(
					lexicalScope.locals.get(connectivityCall.getConnectivity().getName()));
			final NablaExpressionNode shiftNode = NablaIntConstantNodeGen.create(itemIdValueIterator.getShift());

			return createNablaBinaryExpressionNode(createNablaBinaryExpressionNode(indexValueNode, "+",
					createNablaBinaryExpressionNode(shiftNode, "+", nbElemsNode)), "%", nbElemsNode);
		}
		case IrPackage.SET_REF: {
			final SetRef setRef = (SetRef) container;
			final NablaExpressionNode nbElemsNode = getReadVariableNode(
					lexicalScope.locals.get(setRef.getTarget().getValue().getConnectivity().getName()));
			final NablaExpressionNode shiftNode = NablaIntConstantNodeGen.create(itemIdValueIterator.getShift());

			return createNablaBinaryExpressionNode(createNablaBinaryExpressionNode(indexValueNode, "+",
					createNablaBinaryExpressionNode(shiftNode, "+", nbElemsNode)), "%", nbElemsNode);
		}
		}
		throw new UnsupportedOperationException();
	}

	private NablaInstructionNode createItemIdDefinitionNode(ItemIdDefinition itemIdDefinition) {
		final String idName = itemIdDefinition.getId().getName();
		final NablaExpressionNode itemIdValueNode = createItemIdValueNode(itemIdDefinition.getValue());
		final FrameSlot idSlot = lexicalScope.descriptor.findOrAddFrameSlot(idName, null, FrameSlotKind.Illegal);
		lexicalScope.locals.put(idName, idSlot);
		final NablaWriteVariableNode result = getWriteVariableNode(idSlot, itemIdValueNode);
		return result;
	}

	private NablaExpressionNode createItemIdValueNode(ItemIdValue value) {
		switch (value.eClass().getClassifierID()) {
		case IrPackage.ITEM_ID_VALUE_CONTAINER: {
			final ItemIdValueContainer itemIdValueContainer = (ItemIdValueContainer) value;
			final Container container = itemIdValueContainer.getContainer();
			final ConnectivityCall connectivityCall;
			switch (container.eClass().getClassifierID()) {
			case IrPackage.CONNECTIVITY_CALL: {
				connectivityCall = (ConnectivityCall) container;
				break;
			}
			case IrPackage.SET_REF: {
				final SetRef ref = (SetRef) container;
				connectivityCall = ref.getTarget().getValue();
				break;
			}
			default:
				throw new UnsupportedOperationException();
			}
			return createNablaMeshCallNode(connectivityCall);
		}
		case IrPackage.ITEM_ID_VALUE_ITERATOR: {
			final ItemIdValueIterator itemIdValueIterator = (ItemIdValueIterator) value;
			final Container container = itemIdValueIterator.getIterator().getContainer();
			switch (container.eClass().getClassifierID()) {
			case IrPackage.CONNECTIVITY_CALL: {
				final ConnectivityCall connectivityCall = (ConnectivityCall) container;
				final NablaExpressionNode indexValueNode = createGetIndexValueNode(itemIdValueIterator);
				if (connectivityCall.getConnectivity().isIndexEqualId()) {
					return indexValueNode;
				}
				final NablaExpressionNode arrayNode = createNablaMeshCallNode(connectivityCall);
				final NablaExpressionNode[] index = new NablaExpressionNode[] {
						createGetIndexValueNode(itemIdValueIterator) };
				return NablaReadArrayNodeGen.create(index, arrayNode);
			}
			case IrPackage.SET_REF: {
				final SetRef ref = (SetRef) container;
				final NablaExpressionNode[] index = new NablaExpressionNode[] {
						createGetIndexValueNode(itemIdValueIterator) };
				final NablaExpressionNode arrayNode = getReadVariableNode(
						lexicalScope.locals.get(ref.getTarget().getName()));
				return NablaReadArrayNodeGen.create(index, arrayNode);
			}
			}
		}
		}
		throw new UnsupportedOperationException();
	}

	private NablaInstructionNode createItemIndexDefinitionNode(ItemIndexDefinition itemIndexDefinition) {
		final String indexName = itemIndexDefinition.getIndex().getName();
		final FrameSlot indexSlot = lexicalScope.descriptor.findOrAddFrameSlot(indexName, null, FrameSlotKind.Illegal);
		lexicalScope.locals.put(indexName, indexSlot);
		final FrameSlot idSlot = lexicalScope.locals.get(itemIndexDefinition.getValue().getId().getName());
		final NablaExpressionNode idValueNode = getReadVariableNode(idSlot);
		if (itemIndexDefinition.getValue().getContainer().getConnectivity().isIndexEqualId()) {
			return getWriteVariableNode(indexSlot, idValueNode);
		}
		final ConnectivityCall connectivityCall = itemIndexDefinition.getValue().getContainer();
		final NablaExpressionNode arrayNode = createNablaMeshCallNode(connectivityCall);
		final NablaExpressionNode indexOfNode = NablaIndexOfNodeGen.create(arrayNode, idValueNode);
		return getWriteVariableNode(indexSlot, indexOfNode);
	}

	private JsonObject jsonFileContent;

	private JsonObject appJsonOptions;

	private final Map<ExternFunction, NablaProviderObject> functionToProvider = new HashMap<>();

	private final List<Source> sources = new ArrayList<>();

	public NablaRootNode createModule(IrRoot root, JsonObject jsonFileContent, List<Source> nablabSources) {
		assert lexicalScope == null;

		sources.addAll(nablabSources);

		final String rootName = root.getName();

		this.jsonFileContent = jsonFileContent;

		this.appJsonOptions = jsonFileContent.getAsJsonObject(Strings.toFirstLower(rootName));

		final String wd = NablaContext.getCurrent().getStringOption(NablaOptions.WD);

		root.getProviders().stream().filter(p -> p.getProviderName() != null && p.getProviderName().endsWith("JNI"))
				.forEach(p -> {
					final String pName = p.getProviderName();
					final String eName = p.getExtensionName();
					final String outputPath = wd + p.getOutputPath() + '/' + pName.toLowerCase() + '/' + "lib" + '/'
							+ pName.toLowerCase() + ".jar";
					final NablaProviderObject providerObject = new NablaJNIProviderObject(pName, eName, outputPath,
							appJsonOptions.deepCopy());
					p.getFunctions().forEach(f -> functionToProvider.put(f, providerObject));
				});

		lexicalScope = new LexicalScope(lexicalScope);

		moduleFrameDescriptor = lexicalScope.descriptor;

		final NablaWriteVariableNode[] connectivitySizeNodes;
		connectivitySizeNodes = root.getMesh().getConnectivities().stream().filter(c -> c.isMultiple()).map(c -> {
			final String connectivityName = c.getName();
			final FrameSlot frameSlot = moduleFrameDescriptor.findOrAddFrameSlot(connectivityName, null,
					FrameSlotKind.Illegal);
			lexicalScope.locals.put(connectivityName, frameSlot);
			final String ast = (c.getInTypes().isEmpty() ? "get" : "Max") + "Nb"
					+ Strings.toFirstUpper(connectivityName);
			final NablaExpressionNode nbNodes = CreateNablaValueNodeGen.create(int.class,
					NablaMeshWrapperCallNodeGen.create(ast, emptyExpressionArray));
			final NablaWriteVariableNode result = getWriteVariableNode(frameSlot, nbNodes);
			setSourceSection(c, result);
			return result;
		}).collect(Collectors.toList()).toArray(emptyWriteArray);

		final Map<String, JsonObject> moduleNameToOptions = new HashMap<>();
		final Map<String, String> variableNameToModuleName = new HashMap<>();

		// TODO check option changes are taken into account from one execution to the
		// next (beware of ast caching).
		final NablaWriteVariableNode[] optionDefinitions = root.getModules().stream().flatMap(m -> {
			final String moduleName = m.getName();
			moduleNameToOptions.put(moduleName, this.jsonFileContent.getAsJsonObject(moduleName));
			return m.getVariables().stream().filter(v -> v instanceof Variable).map(v -> (Variable) v).filter(v -> {
				if (v.isOption()) {
					variableNameToModuleName.put(v.getName(), moduleName);
					return true;
				}
				return false;
			});
		}).map(o -> {
			final String[] optionPath = new String[] { variableNameToModuleName.get(o.getName()), o.getName() };
			final JsonObject jsonOptions = moduleNameToOptions.get(variableNameToModuleName.get(o.getName()));
			if (jsonOptions != null && jsonOptions.has(o.getName())) {
				return createVariableDeclaration(o, optionPath);
			} else if (o.getDefaultValue() == null) {
				throw new IllegalStateException("Mandatory option missing in Json file: " + o.getName());
			} else {
				return createVariableDeclaration(o);
			}
		}).filter(n -> n != null).collect(Collectors.toList()).toArray(emptyWriteArray);

		final String nodeCoordName = root.getInitNodeCoordVariable().getName();
		final FrameSlot coordinatesSlot = moduleFrameDescriptor.findOrAddFrameSlot(nodeCoordName, null,
				FrameSlotKind.Illegal);
		lexicalScope.locals.put(nodeCoordName, coordinatesSlot);
		final NablaExpressionNode nodes = CreateNablaValueNodeGen.create(double[][].class,
				NablaMeshWrapperCallNodeGen.create("getNodes", emptyExpressionArray));
		final NablaWriteVariableNode nodeCoords = getWriteVariableNode(coordinatesSlot, nodes);

		final List<NablaWriteVariableNode> variableDefinitionList = root.getVariables().stream()
				.filter(v -> !(v instanceof Variable && ((Variable) v).isOption())).map(v -> {
					return createVariableDeclaration(v);
				}).filter(n -> n != null).collect(Collectors.toList());
		variableDefinitionList.add(nodeCoords);
		final NablaWriteVariableNode[] variableDefinitions = variableDefinitionList.toArray(emptyWriteArray);

		Iterators.filter(root.eAllContents(), Function.class)
				.forEachRemaining(f -> functions.put(f, new NablaUndefinedFunctionRootNode(language, f.getName())));
		root.getModules().stream().flatMap(m -> m.getFunctions().stream()).filter(f -> f instanceof InternFunction)
				.map(f -> (InternFunction) f).forEach(f -> functions.computeIfAbsent(f,
						function -> createNablaFunctionNode((InternFunction) function)));

		final NablaRootNode[] jobNodes = root.getMain().getCalls().stream().map(j -> createNablaJobNode(j))
				.collect(Collectors.toList()).toArray(new NablaRootNode[0]);

		final NablaModuleNode moduleNode = new NablaModuleNode(connectivitySizeNodes, optionDefinitions,
				variableDefinitions, jobNodes);

		final NablaRootNode moduleRootNode = new NablaRootNode(language, moduleFrameDescriptor, null, moduleNode,
				rootName);

		setSourceSection(root, moduleNode);
		setRootSourceSection(root, moduleRootNode);

		return moduleRootNode;
	}

	private NablaExpressionNode createNablaBinaryExpressionNode(NablaExpressionNode leftNode, String operator,
			NablaExpressionNode rightNode) {
		switch (operator) {
		case "||":
			return NablaBinaryBooleanNodeGen.create(BooleanOperator.OR, leftNode, rightNode);
		case "&&":
			return NablaBinaryBooleanNodeGen.create(BooleanOperator.AND, leftNode, rightNode);
		case "==":
			return NablaEqNodeGen.create(leftNode, rightNode);
		case "!=":
			return NablaNeqNodeGen.create(leftNode, rightNode);
		case ">=":
			return NablaGeqNodeGen.create(leftNode, rightNode);
		case "<=":
			return NablaLeqNodeGen.create(leftNode, rightNode);
		case ">":
			return NablaGtNodeGen.create(leftNode, rightNode);
		case "<":
			return NablaLtNodeGen.create(leftNode, rightNode);
		case "+":
			return NablaBinaryArithmeticNodeGen.create(ArithmeticOperator.ADD, leftNode, rightNode);
		case "-":
			return NablaBinaryArithmeticNodeGen.create(ArithmeticOperator.SUB, leftNode, rightNode);
		case "*":
			return NablaBinaryArithmeticNodeGen.create(ArithmeticOperator.MUL, leftNode, rightNode);
		case "/":
			return NablaBinaryArithmeticNodeGen.create(ArithmeticOperator.DIV, leftNode, rightNode);
		case "%":
			return NablaModNodeGen.create(leftNode, rightNode);
		}
		throw new UnsupportedOperationException();
	}

	private NablaExpressionNode createNablaBoolConstantNode(boolean value) {
		return NablaBoolConstantNodeGen.create(value);
	}

	private NablaContractedIfNode createNablaContractedIfNode(ContractedIf contractedIf) {
		return new NablaContractedIfNode(createNablaExpressionNode(contractedIf.getCondition()),
				createNablaExpressionNode(contractedIf.getThenExpression()),
				createNablaExpressionNode(contractedIf.getElseExpression()));
	}

	private NablaExpressionNode createNablaDefaultValueNode(BaseType baseType) {
		final NablaExpressionNode[] sizes = baseType.getSizes().stream().map(s -> createNablaExpressionNode(s))
				.collect(Collectors.toList()).toArray(emptyExpressionArray);
		return createNablaDefaultValueNode(baseType.getPrimitive(), sizes);
	}

	private NablaExpressionNode createNablaDefaultValueNode(PrimitiveType primitive, NablaExpressionNode[] sizes) {
		switch (primitive) {
		case BOOL: {
			final NablaExpressionNode value = createNablaBoolConstantNode(false);
			if (sizes == null) {
				return value;
			}
			switch (sizes.length) {
			case 0:
				return value;
			case 1:
				return NablaBool1ConstantNodeGen.create(value, sizes[0]);
			case 2:
				return NablaBool2ConstantNodeGen.create(value, sizes);
			case 3:
				return NablaBool3ConstantNodeGen.create(value, sizes);
			case 4:
				return NablaBool4ConstantNodeGen.create(value, sizes);
			default:
				throw new UnsupportedOperationException();
			}
		}
		case INT: {
			final NablaExpressionNode value = createNablaIntConstantNode(0);
			if (sizes == null) {
				return value;
			}
			switch (sizes.length) {
			case 0:
				return value;
			case 1:
				return NablaInt1ConstantNodeGen.create(value, sizes[0]);
			case 2:
				return NablaInt2ConstantNodeGen.create(value, sizes);
			case 3:
				return NablaInt3ConstantNodeGen.create(value, sizes);
			case 4:
				return NablaInt4ConstantNodeGen.create(value, sizes);
			default:
				throw new UnsupportedOperationException();
			}
		}
		case REAL: {
			final NablaExpressionNode value = createNablaRealConstantNode(0);
			if (sizes == null) {
				return value;
			}
			switch (sizes.length) {
			case 0:
				return value;
			case 1:
				return NablaReal1ConstantNodeGen.create(value, sizes[0]);
			case 2:
				return NablaReal2ConstantNodeGen.create(value, sizes);
			case 3:
				return NablaReal3ConstantNodeGen.create(value, sizes);
			case 4:
				return NablaReal4ConstantNodeGen.create(value, sizes);
			default:
				throw new UnsupportedOperationException();
			}
		}
		default:
			throw new UnsupportedOperationException();
		}
	}

	private NablaExpressionNode createNablaExpressionNode(Expression expression) {
		final NablaExpressionNode expressionNode;
		switch (expression.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE_CONSTANT:
			expressionNode = createBaseTypeConstantNode((BaseTypeConstant) expression);
			break;
		case IrPackage.BOOL_CONSTANT:
			expressionNode = createNablaBoolConstantNode(((BoolConstant) expression).isValue());
			break;
		case IrPackage.INT_CONSTANT:
			expressionNode = createNablaIntConstantNode(((IntConstant) expression).getValue());
			break;
		case IrPackage.REAL_CONSTANT:
			expressionNode = createNablaRealConstantNode(((RealConstant) expression).getValue());
			break;
		case IrPackage.VECTOR_CONSTANT:
			expressionNode = createNablaVectorLiteralNode((VectorConstant) expression);
			break;
		case IrPackage.MAX_CONSTANT:
			expressionNode = createNablaMaxConstant((MaxConstant) expression);
			break;
		case IrPackage.MIN_CONSTANT:
			expressionNode = createNablaMinConstant((MinConstant) expression);
			break;
		case IrPackage.ARG_OR_VAR_REF:
			expressionNode = createNablaReadArgOrVariableNode((ArgOrVarRef) expression);
			break;
		case IrPackage.PARENTHESIS:
			expressionNode = createNablaParenthesisNode((Parenthesis) expression);
			break;
		case IrPackage.CONTRACTED_IF:
			expressionNode = createNablaContractedIfNode((ContractedIf) expression);
			break;
		case IrPackage.FUNCTION_CALL:
			expressionNode = createNablaFunctionCallNode((FunctionCall) expression);
			break;
		case IrPackage.UNARY_EXPRESSION: {
			final UnaryExpression unaryExpression = (UnaryExpression) expression;
			final NablaExpressionNode subNode = createNablaExpressionNode(unaryExpression.getExpression());
			expressionNode = createNablaUnaryExpressionNode(subNode, unaryExpression.getOperator());
		}
			break;
		case IrPackage.BINARY_EXPRESSION: {
			final BinaryExpression binaryExpression = (BinaryExpression) expression;
			final NablaExpressionNode leftNode = createNablaExpressionNode(binaryExpression.getLeft());
			final NablaExpressionNode rightNode = createNablaExpressionNode(binaryExpression.getRight());
			expressionNode = createNablaBinaryExpressionNode(leftNode, binaryExpression.getOperator(), rightNode);
		}
			break;
		case IrPackage.CARDINALITY: {
			final Cardinality cardinality = (Cardinality) expression;
			final Container container = cardinality.getContainer();
			if (ContainerExtensions.getConnectivityCall(container).getConnectivity().isMultiple()) {
				expressionNode = NablaReadArrayDimensionNodeGen.create(0, getContainerElements(container));
			} else {
				expressionNode = createNablaIntConstantNode(1);
			}
		}
			break;
		default:
			throw new UnsupportedOperationException();
		}
		setSourceSection(expression, expressionNode);
		return expressionNode;
	}

	private String getMangledTypeName(IrType irType) {
		final int dimension = IrTypeExtensions.getDimension(irType);
		switch (irType.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE: {
			final BaseType type = (BaseType) irType;
			return type.getPrimitive().name() + dimension;
		}
		case IrPackage.CONNECTIVITY_TYPE: {
			final ConnectivityType type = (ConnectivityType) irType;
			return type.getBase().getPrimitive().name() + dimension;
		}
		case IrPackage.LINEAR_ALGEBRA_TYPE: {
			return PrimitiveType.REAL.name() + dimension;
		}
		default:
			throw new IllegalArgumentException("Unknown IR type.");
		}
	}

	private NablaExpressionNode createNablaExternalCallNode(FunctionCall functionCall) {
		final ExternFunction function = (ExternFunction) functionCall.getFunction();
		final NablaProviderObject providerObj = functionToProvider.get(function);
//		final String providerName = function.getProvider().getProviderName();
//		NablaContext.getCurrent().setProvider(providerName, providerObj.initialize(appJsonOptions));
		final IrType irReturnType = function.getReturnType();
		final Class<?> javaReturnType = FunctionCallHelper.getJavaType(getPrimitiveType(irReturnType),
				IrTypeExtensions.getDimension(irReturnType));
		final NablaExpressionNode[] args = functionCall.getArgs().stream().map(e -> createNablaExpressionNode(e))
				.collect(Collectors.toList()).toArray(emptyExpressionArray);

		return CreateNablaValueNodeGen.create(javaReturnType,
				NablaJNICallNodeGen.create(providerObj, function.getName(), args));
	}

	private NablaExpressionNode createNablaExternalFunctionCallNode(FunctionCall functionCall,
			String receiverClassName) {
		final Function function = functionCall.getFunction();
		final String methodName = function.getName();
		final IrType irReturnType = function.getReturnType();
		final Class<?> javaReturnType = FunctionCallHelper.getJavaType(getPrimitiveType(irReturnType),
				IrTypeExtensions.getDimension(irReturnType));
		final NablaExpressionNode[] argNodes = functionCall.getArgs().stream().map(e -> createNablaExpressionNode(e))
				.collect(Collectors.toList()).toArray(emptyExpressionArray);
		return CreateNablaValueNodeGen.create(javaReturnType,
				new NablaJavaMethodCallNode(receiverClassName, methodName, javaReturnType, argNodes));
	}

	private NablaExpressionNode createNablaMeshCallNode(ConnectivityCall connectivityCall) {
		final Connectivity connectivity = connectivityCall.getConnectivity();
		final String connectivityName = connectivityCall.getConnectivity().getName();
		final String getterName;
		final Class<?> typeClass;
		if (connectivityName.equals("nodes") || connectivityName.equals("cells")) {
			getterName = /* TODO "getNb" + */ connectivityName;
			typeClass = int.class;
		} else {
			getterName = /* TODO "get" + */ Strings.toFirstUpper(connectivityName);
			typeClass = connectivity.isMultiple() ? int[].class : int.class;
		}
		final NablaExpressionNode[] args = connectivityCall.getArgs().stream().map(iId -> iId.getName())
				.map(s -> lexicalScope.locals.get(s)).map(s -> getReadVariableNode(s)).collect(Collectors.toList())
				.toArray(emptyExpressionArray);
		return CreateNablaValueNodeGen.create(typeClass,
				NablaMeshWrapperCallNodeGen.create(getterName, args));
	}

	private NablaExpressionNode createNablaBuiltinOrExternalFunctionCallNode(FunctionCall functionCall) {
		final ExternFunction function = (ExternFunction) functionCall.getFunction();
		final String methodName = function.getName();
		final ExtensionProvider provider = function.getProvider();
		if (provider.getExtensionName().equals("Math")) { // sqrt, min, max, abs
			switch (methodName) {
			case "abs":
				return NablaAbsNodeGen.create(createNablaExpressionNode(functionCall.getArgs().get(0)));
			case "max":
				return NablaMaxNodeGen.create(createNablaExpressionNode(functionCall.getArgs().get(0)),
						createNablaExpressionNode(functionCall.getArgs().get(1)));
			case "min":
				return NablaMinNodeGen.create(createNablaExpressionNode(functionCall.getArgs().get(0)),
						createNablaExpressionNode(functionCall.getArgs().get(1)));
			case "sqrt":
				return NablaSqrtNodeGen.create(createNablaExpressionNode(functionCall.getArgs().get(0)));
			default:
				final String receiverClassName = "java.lang.Math";
				return createNablaExternalFunctionCallNode(functionCall, receiverClassName);
			}
		} else {
			return createNablaExternalCallNode(functionCall);
		}
	}

	private NablaExpressionNode createNablaFunctionCallNode(FunctionCall functionCall) {
		if (functionCall.getFunction() instanceof InternFunction) {
			final NablaRootNode rootNode = functions.compute(functionCall.getFunction(), (f, v) -> {
				if (v != null) {
					if (v instanceof NablaUndefinedFunctionRootNode) {
						return createNablaFunctionNode((InternFunction) f);
					} else {
						return v;
					}
				}
				throw new IllegalStateException();
			});
			final NablaExpressionNode[] argNodes = functionCall.getArgs().stream()
					.map(e -> createNablaExpressionNode(e)).collect(Collectors.toList()).toArray(emptyExpressionArray);
			final NablaFunctionCallNode functionNode = new NablaFunctionCallNode(rootNode, argNodes);
			return functionNode;
		} else {
			return createNablaBuiltinOrExternalFunctionCallNode(functionCall);
		}
	}

	private List<Expression> getSizes(IrType type) {
		switch (type.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE:
			return ((BaseType) type).getSizes();
		case IrPackage.LINEAR_ALGEBRA_TYPE:
			return ((LinearAlgebraType) type).getSizes();
		default:
			throw new UnsupportedOperationException();
		}
	}

	private NablaRootNode createNablaFunctionNode(InternFunction function) {
		lexicalScope = new LexicalScope(lexicalScope, true);
		final List<NablaInstructionNode> functionProlog = new ArrayList<>();
		final Set<String> sizeVarSet = new HashSet<>();
		int nbSizeVars = function.getVariables().size();
		for (int i = 0; i < nbSizeVars; i++) {
			final String varName = function.getVariables().get(i).getName();
			sizeVarSet.add(varName);
			final FrameSlot frameSlot = lexicalScope.descriptor.findOrAddFrameSlot(varName, null,
					FrameSlotKind.Illegal);
			lexicalScope.locals.put(varName, frameSlot);
		}

		int nbArgs = function.getInArgs().size();
		for (int i = 0; i < nbArgs; i++) {
			final Arg arg = function.getInArgs().get(i);
			final String argName = arg.getName();
			final NablaReadArgumentNode readArg1 = NablaReadArgumentNodeGen.create(i);
			final FrameSlot frameSlot = lexicalScope.descriptor.findOrAddFrameSlot(argName, null,
					FrameSlotKind.Illegal);
			lexicalScope.locals.put(argName, frameSlot);
			functionProlog.add(createNablaWriteVariableNode(argName, readArg1, i));
			final List<Expression> sizes = getSizes(arg.getType());
			for (int j = 0; j < sizes.size(); j++) {
				final Variable sizeVariable = collectSizeVariable(sizes.get(j));
				if (sizeVariable != null) {
					final String varName = sizeVariable.getName();
					if (sizeVarSet.remove(varName)) {
						final NablaReadArgumentNode readArg2 = NablaReadArgumentNodeGen.create(i);
						final NablaReadArrayDimensionNode readDimension = NablaReadArrayDimensionNodeGen.create(j,
								readArg2);
						functionProlog.add(createNablaWriteVariableNode(varName, readDimension));
					}
				}
			}
		}
		final NablaInstructionNode[] functionPrologNodes = functionProlog.toArray(emptyInstructionArray);

		final NablaInstructionNode bodyNode = createNablaInstructionNode(function.getBody());
		final NablaPrologBlockNode prologNode = new NablaPrologBlockNode(functionPrologNodes);
		final NablaRootNode functionRootNode = new NablaRootNode(language, lexicalScope.descriptor, prologNode,
				bodyNode, function.getName());
		setRootSourceSection(function, functionRootNode);
		lexicalScope = lexicalScope.outer;
		return functionRootNode;
	}

	private NablaIfNode createNablaIfNode(If ifInstruction) {
		final NablaExpressionNode conditionNode = createNablaExpressionNode(ifInstruction.getCondition());
		final NablaInstructionNode thenNode = createNablaInstructionNode(ifInstruction.getThenInstruction());
		final NablaInstructionNode elseNode = ifInstruction.getElseInstruction() == null ? null
				: createNablaInstructionNode(ifInstruction.getElseInstruction());
		return NablaIfNodeGen.create(conditionNode, thenNode, elseNode);
	}

	private NablaInstructionNode createNablaInstructionBlockNode(InstructionBlock instructionBlock) {
		lexicalScope = new LexicalScope(lexicalScope);
		final NablaInstructionNode[] instructions = instructionBlock.getInstructions().stream()
				.map(i -> createNablaInstructionNode(i)).collect(Collectors.toList()).toArray(emptyInstructionArray);
		lexicalScope = lexicalScope.outer;
		return instructions.length == 1 ? instructions[0] : new NablaInstructionBlockNode(instructions);
	}

	private NablaJobNode createNablaInstructionJobNode(Job job) {
		final NablaInstructionNode body = createNablaInstructionNode(job.getInstruction());
		setSourceSection(job.getInstruction(), body);
		return new NablaInstructionJobNode(job.getName(), body);
	}

	private NablaInstructionNode createNablaInstructionNode(Instruction instruction) {
		final NablaInstructionNode instructionNode;
		switch (instruction.eClass().getClassifierID()) {
		case IrPackage.AFFECTATION: {
			final Affectation affectation = (Affectation) instruction;
			final NablaExpressionNode right = createNablaExpressionNode(affectation.getRight());
			final ArgOrVarRef argOrVarRef = affectation.getLeft();
			final ArgOrVar argOrVar = argOrVarRef.getTarget();
			final String name = argOrVar.getName();
			if (argOrVarRef.getIterators().isEmpty() && argOrVarRef.getIndices().isEmpty()) {
				instructionNode = createNablaWriteVariableNode(name, right);
			} else {
				instructionNode = createNablaWriteArrayNode(name, getBaseType(argOrVar.getType()),
						getDimensions(argOrVar), getArrayIndices(argOrVarRef), right);
			}
		}
			break;
		case IrPackage.IF:
			instructionNode = createNablaIfNode((If) instruction);
			break;
		case IrPackage.INSTRUCTION_BLOCK:
			instructionNode = createNablaInstructionBlockNode((InstructionBlock) instruction);
			break;
		case IrPackage.ITERABLE_INSTRUCTION:
			throw new UnsupportedOperationException();
		case IrPackage.LOOP:
			instructionNode = createNablaLoopNode((Loop) instruction);
			break;
		case IrPackage.WHILE:
			instructionNode = createNablaWhileNode((While) instruction);
			break;
		case IrPackage.RETURN:
			instructionNode = createNablaReturnNode((Return) instruction);
			break;
		case IrPackage.SET_DEFINITION:
			instructionNode = createSetDefinitionNode((SetDefinition) instruction);
			break;
		case IrPackage.VARIABLE_DECLARATION:
			final VariableDeclaration varDeclaration = (VariableDeclaration) instruction;
			instructionNode = createVariableDeclaration(varDeclaration.getVariable());
			break;
		case IrPackage.ITEM_ID_DEFINITION:
			instructionNode = createItemIdDefinitionNode((ItemIdDefinition) instruction);
			break;
		case IrPackage.ITEM_INDEX_DEFINITION:
			instructionNode = createItemIndexDefinitionNode((ItemIndexDefinition) instruction);
			break;
		case IrPackage.EXIT:
			instructionNode = NablaExitNodeGen.create(((Exit) instruction).getMessage());
			break;
		default:
			throw new UnsupportedOperationException();
		}
		setSourceSection(instruction, instructionNode);
		return instructionNode;
	}

	private NablaIntConstantNode createNablaIntConstantNode(int value) {
		return NablaIntConstantNodeGen.create(value);
	}

	private NablaRootNode createNablaJobNode(Job job) {
		final NablaJobNode jobNode;
		lexicalScope = new LexicalScope(lexicalScope, true);
		switch (job.eClass().getClassifierID()) {
		case IrPackage.JOB:
			jobNode = createNablaInstructionJobNode((Job) job);
			break;
		case IrPackage.EXECUTE_TIME_LOOP_JOB:
			jobNode = createNablaExecuteTimeLoopJobNode((ExecuteTimeLoopJob) job);
			break;
		default:
			throw new UnsupportedOperationException();
		}

		final NablaRootNode jobRootNode = new NablaRootNode(language, lexicalScope.descriptor, null, jobNode,
				job.getName());

		setSourceSection(job, jobNode);
		setRootSourceSection(job, jobRootNode);

		lexicalScope = lexicalScope.outer;
		return jobRootNode;
	}

	private NablaInstructionNode createNablaWhileNode(While whyle) {
		final NablaInstructionNode instructionNode = createNablaInstructionNode(whyle.getInstruction());
		final NablaExpressionNode conditionNode = createNablaExpressionNode(whyle.getCondition());
		final NablaWhileRepeatingNode repeatingNode = NablaWhileRepeatingNodeGen.create(instructionNode, conditionNode);
		return new NablaWhileNode(Truffle.getRuntime().createLoopNode(repeatingNode));
	}

	private NablaInstructionNode createNablaLoopNode(Loop loop) {
		final IterationBlock iterationBlock = loop.getIterationBlock();
		switch (iterationBlock.eClass().getClassifierID()) {
		case IrPackage.INTERVAL: {
			lexicalScope = new LexicalScope(lexicalScope);
			final Interval interval = (Interval) iterationBlock;
			final String indexName = interval.getIndex().getName();
			final FrameSlot indexSlot = lexicalScope.descriptor.findOrAddFrameSlot(indexName, null,
					FrameSlotKind.Illegal);
			lexicalScope.locals.put(indexName, indexSlot);
			final NablaInstructionNode bodyNode = createNablaInstructionNode(loop.getBody());
			final NablaExpressionNode iterationCount = createNablaExpressionNode(interval.getNbElems());
			final NablaInstructionNode result = NablaLoopNodeGen.create(indexSlot, null, bodyNode, iterationCount);
			lexicalScope = lexicalScope.outer;
			return result;
		}
		case IrPackage.ITERATOR: {
			lexicalScope = new LexicalScope(lexicalScope);
			final Iterator iterator = (Iterator) iterationBlock;
			final String indexName = iterator.getIndex().getName();
			final FrameSlot indexSlot = lexicalScope.descriptor.findOrAddFrameSlot(indexName, null,
					FrameSlotKind.Illegal);
			lexicalScope.locals.put(indexName, indexSlot);
			final FrameSlot counterSlot;
			if (iterator.getCounter() != null) {
				final Variable counterVariable = iterator.getCounter();
				final String counterName = counterVariable.getName();
				counterSlot = lexicalScope.descriptor.findOrAddFrameSlot(counterName, null, FrameSlotKind.Illegal);
				lexicalScope.locals.put(counterName, counterSlot);
			} else {
				counterSlot = null;
			}
			final NablaInstructionNode bodyNode = createNablaInstructionNode(loop.getBody());

			final Container container = iterator.getContainer();
			switch (container.eClass().getClassifierID()) {
			case IrPackage.CONNECTIVITY_CALL: {
				final ConnectivityCall connectivityCall = (ConnectivityCall) container;
//				final String connectivityName = connectivityCall.getConnectivity().getName();
//				final NablaExpressionNode[] argIds = connectivityCall.getArgs().stream().map(iId -> iId.getName())
//						.map(s -> lexicalScope.locals.get(s)).map(s -> getReadVariableNode(s))
//						.collect(Collectors.toList()).toArray(emptyExpressionArray);
//				final NablaExpressionNode elements = NablaMeshCallNodeGen.create("get" + Strings.toFirstUpper(connectivityName), argIds);
				final NablaExpressionNode elements = createNablaMeshCallNode(connectivityCall);
				final NablaInstructionNode result = NablaLoopNodeGen.create(indexSlot, counterSlot, bodyNode, elements);
				return result;
			}
			case IrPackage.SET_REF: {
				final SetRef ref = (SetRef) container;
				final NablaExpressionNode elements = getReadVariableNode(
						lexicalScope.locals.get(ref.getTarget().getName()));
				final NablaInstructionNode result = NablaLoopNodeGen.create(indexSlot, counterSlot, bodyNode, elements);
				return result;
			}
			}
			lexicalScope = lexicalScope.outer;
		}
		}
		throw new UnsupportedOperationException();
	}

	private NablaExpressionNode createNablaMaxConstant(MaxConstant maxConstant) {
		final IrType type = maxConstant.getType();
		switch (type.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE: {
			final BaseType baseType = (BaseType) type;
			switch (baseType.getPrimitive()) {
			case BOOL:
				throw new UnsupportedOperationException();
			case INT:
				return createNablaIntConstantNode(Integer.MAX_VALUE);
			case REAL:
				return createNablaRealConstantNode(Double.MAX_VALUE);
			}
		}
		case IrPackage.CONNECTIVITY_TYPE:
			throw new UnsupportedOperationException();
		}
		throw new UnsupportedOperationException();
	}

	private NablaExpressionNode createNablaMinConstant(MinConstant minConstant) {
		final IrType type = minConstant.getType();
		switch (type.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE: {
			final BaseType baseType = (BaseType) type;
			switch (baseType.getPrimitive()) {
			case BOOL:
				throw new UnsupportedOperationException();
			case INT:
				return createNablaIntConstantNode(Integer.MIN_VALUE);
			case REAL:
				return createNablaRealConstantNode(Double.MIN_VALUE);
			}
		}
		case IrPackage.CONNECTIVITY_TYPE:
			throw new UnsupportedOperationException();
		}
		throw new UnsupportedOperationException();
	}

	private NablaExpressionNode createNablaParenthesisNode(Parenthesis expression) {
		return NablaParenthesisNodeGen.create(createNablaExpressionNode(expression.getExpression()));
	}

	private NablaExpressionNode createNablaReadArgOrVariableNode(ArgOrVarRef ref) {
		final ArgOrVar target = ref.getTarget();
		final FrameSlot frameSlot = lexicalScope.locals.get(target.getName());
		if (ref.getIterators().isEmpty() && ref.getIndices().isEmpty()) {
			return getReadVariableNode(frameSlot);
		} else {
			return NablaReadArrayNodeGen.create(getArrayIndices(ref), getReadVariableNode(frameSlot));
		}
	}

	private NablaRealConstantNode createNablaRealConstantNode(double value) {
		return NablaRealConstantNodeGen.create(value);
	}

	private NablaInstructionNode createNablaReturnNode(Return ret) {
		return NablaReturnNodeGen.create(createNablaExpressionNode(ret.getExpression()));
	}

	// FIXME: use module frame descriptor or more local one? ->
	private NablaJobNode createNablaExecuteTimeLoopJobNode(ExecuteTimeLoopJob job) {
		final String indexName = job.getIterationCounter().getName();
		final FrameSlot indexSlot;
		if (lexicalScope.locals.containsKey(indexName)) {
			// using slot from closest frame descriptor (i.e., module)
			indexSlot = lexicalScope.locals.get(indexName);
		} else {
			// using slot from own frame descriptor
			indexSlot = lexicalScope.descriptor.findOrAddFrameSlot(indexName, null, FrameSlotKind.Illegal);
			lexicalScope.locals.put(indexName, indexSlot);
		}

		final NablaReadVariableNode read = NablaReadVariableNodeGen.create(indexSlot.getIdentifier().toString(),
				GetFrameNodeGen.create(indexSlot.getIdentifier().toString()));
		final NablaIntConstantNode one = NablaIntConstantNodeGen.create(1);
		final NablaBinaryArithmeticNode add = NablaBinaryArithmeticNodeGen.create(ArithmeticOperator.ADD, read, one);
		final NablaWriteVariableNode indexUpdate = NablaWriteVariableNodeGen.create(indexSlot, add,
				GetFrameNodeGen.create(indexSlot.getIdentifier().toString()));

		final NablaRootNode[] innerJobs = job.getCalls().stream().map(j -> createNablaJobNode(j))
				.collect(Collectors.toList()).toArray(new NablaRootNode[0]);
		final NablaJobBlockNode loopBody = new NablaJobBlockNode(innerJobs);

		final NablaExpressionNode conditionNode = createNablaExpressionNode(job.getWhileCondition());

		final NablaInstructionNode copyInstructionNode = createNablaInstructionNode(job.getInstruction());

		final NablaTimeLoopJobRepeatingNode repeatingNode = NablaTimeLoopJobRepeatingNodeGen.create(copyInstructionNode,
				indexUpdate, loopBody, conditionNode);

		final NablaIntConstantNode zero = NablaIntConstantNodeGen.create(0);
		final NablaWriteVariableNode indexInit = NablaWriteVariableNodeGen.create(indexSlot, zero,
				GetFrameNodeGen.create(indexSlot.getIdentifier().toString()));

		return new NablaTimeLoopJobNode(job.getName(), indexInit, Truffle.getRuntime().createLoopNode(repeatingNode));
	}

	private NablaExpressionNode createNablaUnaryExpressionNode(NablaExpressionNode subNode, String operator) {
		switch (operator) {
		case "!":
			return NablaNotNodeGen.create(subNode);
		case "-":
			return NablaMinusNodeGen.create(subNode);
		}
		throw new UnsupportedOperationException();
	}

	private NablaExpressionNode createNablaVectorLiteralNode(VectorConstant vectorConstant) {
		final IrType type = vectorConstant.getType();
		switch (type.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE: {
			final BaseType baseType = (BaseType) type;
			final NablaExpressionNode[] values = vectorConstant.getValues().stream()
					.map(e -> createNablaExpressionNode(e)).collect(Collectors.toList()).toArray(emptyExpressionArray);
			final NablaExpressionNode[] dimensions = baseType.getSizes().stream().map(s -> createNablaExpressionNode(s))
					.collect(Collectors.toList()).toArray(emptyExpressionArray);

			switch (baseType.getPrimitive()) {
			case BOOL:
				switch (dimensions.length) {
				case 1:
					return NablaBool1NodeGen.create(values);
				case 2:
					return NablaBool2NodeGen.create(values, dimensions);
				default:
					throw new UnsupportedOperationException();
				}
			case INT:
				switch (dimensions.length) {
				case 1:
					return NablaInt1NodeGen.create(values);
				case 2:
					return NablaInt2NodeGen.create(values, dimensions);
				default:
					throw new UnsupportedOperationException();
				}
			case REAL:
				switch (dimensions.length) {
				case 1:
					return NablaReal1NodeGen.create(values);
				case 2:
					return NablaReal2NodeGen.create(values, dimensions);
				default:
					throw new UnsupportedOperationException();
				}
			}
		}
		case IrPackage.CONNECTIVITY_TYPE:
			throw new UnsupportedOperationException();
		}
		throw new UnsupportedOperationException();
	}

	private NablaInstructionNode createNablaWriteArrayNode(String name, Class<?> baseType, int dimensions,
			NablaExpressionNode[] indices, NablaExpressionNode value) {
		final FrameSlot frameSlot = lexicalScope.locals.get(name);
		assert (frameSlot != null);
		return NablaWriteArrayNodeGen.create(frameSlot, baseType, dimensions, indices, value,
				getReadVariableNode(frameSlot));
	}

	private NablaWriteVariableNode createNablaWriteVariableNode(String name, NablaExpressionNode value) {
		return createNablaWriteVariableNode(name, value, null);
	}

	private NablaWriteVariableNode createNablaWriteVariableNode(String name, NablaExpressionNode value,
			Integer paramaterIndex) {
		final FrameSlot frameSlot = lexicalScope.locals.get(name);
		return getWriteVariableNode(frameSlot, value);
	}

	private NablaInstructionNode createSetDefinitionNode(SetDefinition setDefinition) {
		final ConnectivityCall connectivityCall = setDefinition.getValue();
		if (connectivityCall.getConnectivity().isMultiple()) {
//			final String connectivityName = connectivityCall.getConnectivity().getName();
//			final NablaExpressionNode[] argIds = connectivityCall.getArgs().stream().map(iId -> iId.getName())
//					.map(s -> lexicalScope.locals.get(s)).map(s -> getReadVariableNode(s)).collect(Collectors.toList())
//					.toArray(emptyExpressionArray);
//			final NablaExpressionNode elements = NablaMeshCallNodeGen.create("get" + Strings.toFirstUpper(connectivityName), argIds);
			final NablaExpressionNode elements = createNablaMeshCallNode(connectivityCall);
			final FrameSlot frameSlot = lexicalScope.descriptor.findOrAddFrameSlot(setDefinition.getName(), null,
					FrameSlotKind.Illegal);
			lexicalScope.locals.put(setDefinition.getName(), frameSlot);
			final NablaWriteVariableNode result = getWriteVariableNode(frameSlot, elements);
			return result;
		} else {
			return new NablaNopNode();
		}

	}

	private NablaWriteVariableNode createVariableDeclaration(Variable variable) {
		return createVariableDeclaration(variable, null);
	}

//	static def dispatch NablaValue createValue(ConnectivityType type, Expression defaultValue, Context context)
//	{
//		val sizes = getIntSizes(type, context)
//		val p = type.primitive
//		switch sizes.size
//		{
//			case 1: createValue(p, sizes.get(0), null)
//			case 2: createValue(p, sizes.get(0), sizes.get(1), null)
//			case 3: createValue(p, sizes.get(0), sizes.get(1), sizes.get(2))
//			case 4: createValue(p, sizes.get(0), sizes.get(1), sizes.get(2), sizes.get(3))
//			default: throw new RuntimeException("Dimension not yet implemented: " + sizes.size)
//		}
//	}
//
//	static def dispatch NablaValue createValue(LinearAlgebraType type, Expression defaultValue, Context context)
//	{
//		val sizes = getIntSizes(type, context)
//		val p = PrimitiveType.REAL
//		switch sizes.size
//		{
//			case 1: createValue(p, sizes.get(0), context.linearAlgebra)
//			case 2: createValue(p, sizes.get(0), sizes.get(1), context.linearAlgebra)
//			default: throw new RuntimeException("Dimension not yet implemented: " + sizes.size)
//		}
//	}

	private NablaWriteVariableNode createVariableDeclaration(Variable variable, String[] optionPath) {
		final NablaWriteVariableNode result;
		final String name = variable.getName();
		final FrameSlot frameSlot = lexicalScope.descriptor.findOrAddFrameSlot(name, null, FrameSlotKind.Illegal);
		lexicalScope.locals.put(name, frameSlot);
		final Expression defaultValue = variable.getDefaultValue();
		switch (variable.getType().eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE: {
//			TODO
			final BaseType type = (BaseType) variable.getType();
			if (defaultValue == null) {
				final NablaExpressionNode defaultValueExpression;
				if (optionPath == null) {
					defaultValueExpression = createNablaDefaultValueNode(type);
				} else {
					final NablaExpressionNode baseValue = createNablaDefaultValueNode(type);
					defaultValueExpression = getInitializeVariableFromJsonNode(baseValue, optionPath);
				}
				result = getWriteVariableNode(frameSlot, defaultValueExpression);
			} else {
				final NablaExpressionNode defaultValueExpression;
				if (optionPath == null) {
					defaultValueExpression = createNablaExpressionNode(defaultValue);
				} else {
					final NablaExpressionNode baseValue = createNablaExpressionNode(defaultValue);
					defaultValueExpression = getInitializeVariableFromJsonNode(baseValue, optionPath);
				}
				result = getWriteVariableNode(frameSlot, defaultValueExpression);
			}
			break;
		}
		case IrPackage.CONNECTIVITY_TYPE: {
			final ConnectivityType type = (ConnectivityType) variable.getType();
//			TODO: are default values allowed for connectivity types?
//			if (defaultValue == null) {
			final NablaExpressionNode[] sizeNodes = new NablaExpressionNode[type.getConnectivities().size()
					+ type.getBase().getSizes().size()];
			int i = 0;
			for (Connectivity c : type.getConnectivities()) {
				sizeNodes[i] = getReadVariableNode(lexicalScope.locals.get(c.getName()));
				i++;
			}
			for (Expression s : type.getBase().getSizes()) {
				sizeNodes[i] = createNablaExpressionNode(s);
				i++;
			}
			final NablaExpressionNode valueExpression;
			if (optionPath == null) {
				valueExpression = createNablaDefaultValueNode(type.getBase().getPrimitive(), sizeNodes);
			} else {
				final NablaExpressionNode baseValue = createNablaDefaultValueNode(type.getBase().getPrimitive(),
						sizeNodes);
				valueExpression = getInitializeVariableFromJsonNode(baseValue, optionPath);
			}
			result = getWriteVariableNode(frameSlot, valueExpression);
//			} else {
//				final NablaExpressionNode defaultValueExpression;
//				if (jsonValue == null) {
//					defaultValueExpression = createNablaReadArgOrVariableNode(defaultValue);
//				} else {
//					final NablaExpressionNode baseValue = createNablaReadArgOrVariableNode(defaultValue);
//					defaultValueExpression = getInitializeVariableFromJsonNode(baseValue, jsonValue);
//				}
//				result = getWriteVariableNode(frameSlot, defaultValueExpression);
//			}
			break;
		}
		case IrPackage.LINEAR_ALGEBRA_TYPE: {
//			TODO: are default values allowed for linear algebra types?
//			TODO: linear algebra extensions needed
//			val sizes = getIntSizes(type, context)
//			val p = PrimitiveType.REAL
//			switch sizes.size
//			{
//				case 1: createValue(p, sizes.get(0), context.linearAlgebra)
//				case 2: createValue(p, sizes.get(0), sizes.get(1), context.linearAlgebra)
//				default: throw new RuntimeException("Dimension not yet implemented: " + sizes.size)
//			}
			throw new UnsupportedOperationException();
		}
		default:
			throw new UnsupportedOperationException();
		}
		setSourceSection(variable, result);
		return result;
	}

	private Class<?> getBaseType(IrType irType) {
		switch (irType.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE: {
			final BaseType type = (BaseType) irType;
			return getBaseType(type.getPrimitive());
		}
		case IrPackage.CONNECTIVITY_TYPE: {
			final ConnectivityType type = (ConnectivityType) irType;
			return getBaseType(type.getBase().getPrimitive());
		}
		case IrPackage.LINEAR_ALGEBRA_TYPE: {
			return getBaseType(PrimitiveType.REAL);
		}
		default:
			throw new IllegalArgumentException("Unknown IR type.");
		}
	}

	private PrimitiveType getPrimitiveType(IrType irType) {
		switch (irType.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE: {
			final BaseType type = (BaseType) irType;
			return type.getPrimitive();
		}
		case IrPackage.CONNECTIVITY_TYPE: {
			final ConnectivityType type = (ConnectivityType) irType;
			return type.getBase().getPrimitive();
		}
		case IrPackage.LINEAR_ALGEBRA_TYPE: {
			return PrimitiveType.REAL;
		}
		default:
			throw new IllegalArgumentException("Unknown IR type.");
		}
	}

	private Class<?> getBaseType(PrimitiveType primitiveType) {
		switch (primitiveType) {
		case BOOL:
			return boolean.class;
		case INT:
			return int.class;
		case REAL:
			return double.class;
		default:
			throw new IllegalArgumentException();
		}
	}

	private int getDimensions(ArgOrVar argOrVar) {
		final IrType irType = argOrVar.getType();
		switch (irType.eClass().getClassifierID()) {
		case IrPackage.BASE_TYPE: {
			final BaseType type = (BaseType) irType;
			return type.getSizes().size();
		}
		case IrPackage.CONNECTIVITY_TYPE: {
			final ConnectivityType type = (ConnectivityType) irType;
			return type.getBase().getSizes().size() + type.getConnectivities().size();
		}
		case IrPackage.LINEAR_ALGEBRA_TYPE: {
			final LinearAlgebraType type = (LinearAlgebraType) irType;
			return type.getSizes().size();
		}
		default:
			throw new IllegalArgumentException("Unknown IR type.");
		}
	}

	private NablaExpressionNode[] getArrayIndices(ArgOrVarRef ref) {
		final NablaExpressionNode[] indices = new NablaExpressionNode[ref.getIterators().size()
				+ ref.getIndices().size()];
		int i = 0;
		for (ItemIndex item : ref.getIterators()) {
			indices[i] = getReadVariableNode(lexicalScope.locals.get(item.getName()));
			i++;
		}
		for (Expression idx : ref.getIndices()) {
			indices[i] = createNablaExpressionNode(idx);
			i++;
		}

		return indices;
	}

	private NablaExpressionNode getContainerElements(Container container) {
		switch (container.eClass().getClassifierID()) {
		case IrPackage.CONNECTIVITY_CALL: {
			final ConnectivityCall connectivityCall = (ConnectivityCall) container;
//			final String connectivityName = connectivityCall.getConnectivity().getName();
//			final NablaExpressionNode[] argIds = connectivityCall.getArgs().stream().map(iId -> iId.getName())
//					.map(s -> lexicalScope.locals.get(s)).map(s -> getReadVariableNode(s)).collect(Collectors.toList())
//					.toArray(emptyExpressionArray);
//			return NablaMeshCallNodeGen.create("get" + Strings.toFirstUpper(connectivityName), argIds);
			return createNablaMeshCallNode(connectivityCall);
		}
		case IrPackage.SET_REF: {
			final SetRef ref = (SetRef) container;
			return getReadVariableNode(lexicalScope.locals.get(ref.getTarget().getName()));
		}
		}
		throw new UnsupportedOperationException();
	}

	private String getDetail(IrAnnotation annotation, String key) {
		return annotation.getDetails().get(key);
	}

	private NablaReadVariableNode getReadVariableNode(FrameSlot slot) {
		assert (slot != null);
		return NablaReadVariableNodeGen.create(slot.getIdentifier().toString(),
				GetFrameNodeGen.create(slot.getIdentifier().toString()));
	}

	private NablaWriteVariableNode getWriteVariableNode(FrameSlot slot, NablaExpressionNode value) {
		assert (slot != null);
		return NablaWriteVariableNodeGen.create(slot, value, GetFrameNodeGen.create(slot.getIdentifier().toString()));
	}

	private NablaInitializeVariableFromJsonNode getInitializeVariableFromJsonNode(NablaExpressionNode value,
			String[] optionPath) {
		assert (optionPath != null);
		return NablaInitializeVariableFromJsonNodeGen.create(optionPath, value);
	}

	private void setRootSourceSection(IrAnnotable element, NablaRootNode node) {
		element.getAnnotations().stream().filter(a -> a.getSource().equals("nabla-origin")).findFirst()
				.ifPresent(annotation -> {
					final String elementSourceURI = getDetail(annotation, "uri");
					if (elementSourceURI != null) {
						sources.stream().filter(s -> s.getURI().toString().equals(elementSourceURI)).findFirst()
								.ifPresentOrElse(s -> {
									final int offset = Integer.parseInt(getDetail(annotation, "offset"));
									final int length = Integer.parseInt(getDetail(annotation, "length"));
									final int startLine = s.getLineNumber(offset);
									final int startColumn = s.getColumnNumber(offset);
									final int endLine = s.getLineNumber(offset + length);
									final int endColumn = s.getColumnNumber(offset + length);
									node.setSourceSection(s.createSection(startLine, startColumn, endLine, endColumn));
								}, () -> {
									node.setSourceSection(source.createUnavailableSection());
								});
					}
				});
	}

	private void setSourceSection(IrAnnotable element, NablaNode node) {
		element.getAnnotations().stream().filter(a -> a.getSource().equals("nabla-origin")).findFirst()
		.ifPresent(annotation -> {
			final String elementSourceURI = getDetail(annotation, "uri");
			if (elementSourceURI != null) {
				sources.stream().filter(s -> s.getURI().toString().equals(elementSourceURI)).findFirst()
						.ifPresentOrElse(s -> {
							final int offset = Integer.parseInt(getDetail(annotation, "offset"));
							final int length = Integer.parseInt(getDetail(annotation, "length"));
							node.setSourceSection(s, offset, length);
						}, () -> {
							node.setUnavailableSourceSection();
						});
			}
		});
	}
}
