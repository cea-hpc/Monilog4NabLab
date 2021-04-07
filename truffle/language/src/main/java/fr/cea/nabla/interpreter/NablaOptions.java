package fr.cea.nabla.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionMap;
import org.graalvm.options.OptionStability;
import org.graalvm.options.OptionType;

import com.oracle.truffle.api.Option;

@Option.Group("nabla")
public class NablaOptions {

	static final OptionType<List<String>> STRING_LIST_TYPE = new OptionType<>("String List",
			o -> Arrays.stream(o.split(":")).map(s -> s.trim()).collect(Collectors.toList()));

	@Option(name = "np", help = "Paths to .n/.ngen files and folders containing .n/.ngen files (colon-separated list of paths)", category = OptionCategory.USER, stability = OptionStability.STABLE)
	public static final OptionKey<List<String>> NABLAPATH = new OptionKey<>(new ArrayList<>(), STRING_LIST_TYPE);
	@Option(name = "options", help = "Json file to use for options", category = OptionCategory.USER, stability = OptionStability.STABLE)
	public static final OptionKey<String> OPTIONS = new OptionKey<>("");
	@Option(name = "natlib", help = "External native libraries (comma-separated list of paths)", category = OptionCategory.USER, stability = OptionStability.STABLE)
	public static final OptionKey<OptionMap<String>> NAT_LIBS = OptionKey.mapOf(String.class);
	@Option(name = "mesh", help = "Mesh library", category = OptionCategory.USER, stability = OptionStability.STABLE)
	public static final OptionKey<String> MESH = new OptionKey<String>("");
	@Option(name = "javalib", help = "External java libraries (comma-separated list of paths)", category = OptionCategory.USER, stability = OptionStability.STABLE)
	public static final OptionKey<OptionMap<String>> JAVA_LIBS = OptionKey.mapOf(String.class);
	@Option(name = "wd", help = "Working directory", category = OptionCategory.USER, stability = OptionStability.STABLE)
	public static final OptionKey<String> WD = new OptionKey<String>("");
}
