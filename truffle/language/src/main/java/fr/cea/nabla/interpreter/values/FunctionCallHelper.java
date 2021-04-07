/**
 * Copyright (c) 2020 CEA
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * Contributors: see AUTHORS file
 */
package fr.cea.nabla.interpreter.values;

import java.io.Serializable;

import fr.cea.nabla.ir.ir.PrimitiveType;

@SuppressWarnings("all")
public class FunctionCallHelper {
	public static Class<?> getJavaType(final PrimitiveType primitive, final int dimension) {
		Class<? extends Serializable> _switchResult = null;
		if (primitive != null) {
			switch (primitive) {
			case BOOL:
				Class<? extends Serializable> _switchResult_1 = null;
				switch (dimension) {
				case 0:
					_switchResult_1 = boolean.class;
					break;
				case 1:
					_switchResult_1 = boolean[].class;
					break;
				case 2:
					_switchResult_1 = boolean[][].class;
					break;
				default:
					throw new RuntimeException(("Dimension not implemented: " + Integer.valueOf(dimension)));
				}
				_switchResult = _switchResult_1;
				break;
			case INT:
				Class<? extends Serializable> _switchResult_2 = null;
				switch (dimension) {
				case 0:
					_switchResult_2 = int.class;
					break;
				case 1:
					_switchResult_2 = int[].class;
					break;
				case 2:
					_switchResult_2 = int[][].class;
					break;
				default:
					throw new RuntimeException(("Dimension not implemented: " + Integer.valueOf(dimension)));
				}
				_switchResult = _switchResult_2;
				break;
			case REAL:
				Class<? extends Serializable> _switchResult_3 = null;
				switch (dimension) {
				case 0:
					_switchResult_3 = double.class;
					break;
				case 1:
					_switchResult_3 = double[].class;
					break;
				case 2:
					_switchResult_3 = double[][].class;
					break;
				default:
					throw new RuntimeException(("Dimension not implemented: " + Integer.valueOf(dimension)));
				}
				_switchResult = _switchResult_3;
				break;
			default:
				break;
			}
		}
		return _switchResult;
	}
}
