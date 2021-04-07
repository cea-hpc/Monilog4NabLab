package fr.cea.nabla.interpreter.values;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;

@GenerateLibrary
public abstract class NV1RealLibrary extends Library {

    public boolean isArray(Object receiver) {
        return false;
    }

    public abstract double read(Object receiver, int index);
    
    public abstract int length(Object receiver);
}
