package edu.harvard.seas.pl.dminor_in_formulog;

/*-
 * #%L
 * Formulog
 * %%
 * Copyright (C) 2018 - 2019 President and Fellows of Harvard College
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;

import org.antlr.v4.runtime.misc.Pair;

public class Function {

	private final String name;
	private final List<Pair<String, String>> paramsAndTypes;
	private final String retType;
	private final String body;
	private final boolean isPure;
	
	public Function(String name, List<Pair<String, String>> paramsAndTypes, String retType, String body, boolean isPure) {
		this.name = name;
		this.paramsAndTypes = paramsAndTypes;
		this.retType = retType;
		this.body = body;
		this.isPure = isPure;
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
	
	public boolean isPure() {
		return isPure;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + (isPure ? 1231 : 1237);
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
		if (isPure != other.isPure)
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
