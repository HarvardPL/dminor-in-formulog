package edu.harvard.seas.pl.dminor_to_formulog;

import java.util.List;

public class Module {

	private final String name;
	private final List<Function> funcs;
	
	public Module(String name, List<Function> funcs) {
		this.name = name;
		this.funcs = funcs;
	}

	public String getName() {
		return name;
	}

	public List<Function> getFuncs() {
		return funcs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((funcs == null) ? 0 : funcs.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Module other = (Module) obj;
		if (funcs == null) {
			if (other.funcs != null)
				return false;
		} else if (!funcs.equals(other.funcs))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}	
	
}
