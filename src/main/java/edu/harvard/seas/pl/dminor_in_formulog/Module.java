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
