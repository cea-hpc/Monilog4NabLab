package fr.cea.nabla.interpreter;

import java.io.File;
import java.io.IOException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

class TestMeshWrapper {
	
	public static void main(String[] args) throws IOException {
		Context polyglot = Context.newBuilder("llvm")
				.allowAllAccess(true)
//				.option("inspect", "9229") //Uncomment to debug C++ in chrome
				.build();
		File file = new File("libcppnabla.so");
		Source source = Source.newBuilder("llvm", file).build();
		Value library = polyglot.eval(source);
		Value wrapper = library.getMember("jsonInit").execute("{\n" + 
				"	\"mesh\":\n" + 
				"	{\n" + 
				"		\"nbXQuads\":40,\n" + 
				"		\"nbYQuads\":40,\n" + 
				"		\"xSize\":0.05,\n" + 
				"		\"ySize\":0.05\n" + 
				"	}\n" + 
				"}" + "\0");
		Value v1 = library.getMember("getNodes").execute(wrapper);
		Value v2 = library.getMember("getNbNodes").execute(wrapper);
		Value v3 = library.getMember("getNodesOfCell").execute(wrapper, 0);
		System.out.println("v1 = " + v1 + " ; v2 = " + v2);
		library.getMember("free_wrapper").execute(wrapper);
	}
}
