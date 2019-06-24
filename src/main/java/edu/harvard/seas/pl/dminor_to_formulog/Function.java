package edu.harvard.seas.pl.dminor_to_formulog;

import java.util.List;

import org.antlr.v4.runtime.misc.Pair;

public class Function {

	private final String name;
	private final List<Pair<String, String>> paramsAndTypes;
	private final String retType;
	private final String body;
	
	public Function(String name, List<Pair<String, String>> paramsAndTypes, String retType, String body) {
		this.name = name;
		this.paramsAndTypes = paramsAndTypes;
		this.retType = retType;
		this.body = body;
	}

	public String getName() {
		return name;
	}

	public List<Pair<String, String>> getParamsAndTypes() {
		return paramsAndTypes;
	}

	public String getRetType() {
		return retType;
	}
	
	public String getBody() {
		return body;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((paramsAndTypes == null) ? 0 : paramsAndTypes.hashCode());
		result = prime * result + ((retType == null) ? 0 : retType.hashCode());
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
		Function other = (Function) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (paramsAndTypes == null) {
			if (other.paramsAndTypes != null)
				return false;
		} else if (!paramsAndTypes.equals(other.paramsAndTypes))
			return false;
		if (retType == null) {
			if (other.retType != null)
				return false;
		} else if (!retType.equals(other.retType))
			return false;
		return true;
	}

}
