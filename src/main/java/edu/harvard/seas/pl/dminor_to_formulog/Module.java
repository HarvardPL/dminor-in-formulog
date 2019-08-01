package edu.harvard.seas.pl.dminor_to_formulog;

import java.util.List;
import java.util.Map;

public class Module {

	private final String name;
	private final List<Function> funcs;
	private final Map<String, String> typeAliases;
	private final Map<String, String> typeIndicatorFuncs;
	
	public Module(String name, List<Function> funcs, Map<String, String> typeAliases, Map<String, String> typeIndicatorFuncs) {
		this.name = name;
		this.funcs = funcs;
		this.typeAliases = typeAliases;
		this.typeIndicatorFuncs = typeIndicatorFuncs;
	}

	public String getName() {
		return name;
	}

	public List<Function> getFuncs() {
		return funcs;
	}

	public Map<String, String> getTypeAliases() {
		return typeAliases;
	}
	
	public Map<String, String> getTypeIndicatorFuncs() {
		return typeIndicatorFuncs;
	}

}
