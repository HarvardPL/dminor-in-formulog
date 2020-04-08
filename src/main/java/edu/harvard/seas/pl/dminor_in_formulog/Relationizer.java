package edu.harvard.seas.pl.dminor_in_formulog;

import static edu.harvard.seas.pl.dminor_in_formulog.Relation.FUNC_SIG;
import static edu.harvard.seas.pl.dminor_in_formulog.Relation.LABELED_PURE;
import static edu.harvard.seas.pl.dminor_in_formulog.Relation.TYPE_ALIAS;
import static edu.harvard.seas.pl.dminor_in_formulog.Relation.TYPE_INDICATOR_FUNC;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.misc.Pair;

public final class Relationizer {

	private Relationizer() {
		throw new AssertionError("impossible");
	}

	public static Map<Relation, List<String[]>> relationize(Module mod) {
		return (new Impl(mod)).run();
	}

	private static class Impl {

		private final Module mod;
		private final Map<Relation, List<String[]>> db = new HashMap<>();

		public Impl(Module mod) {
			this.mod = mod;
			db.put(FUNC_SIG, new ArrayList<>());
			db.put(LABELED_PURE, new ArrayList<>());
			db.put(TYPE_ALIAS, new ArrayList<>());
			db.put(TYPE_INDICATOR_FUNC, new ArrayList<>());
		}

		public Map<Relation, List<String[]>> run() {
			for (Function func : mod.getFuncs()) {
				processFunc(func);
			}
			for (Map.Entry<String, String> e : mod.getTypeIndicatorFuncs().entrySet()) {
				db.get(TYPE_INDICATOR_FUNC).add(new String[] { "\"" + e.getKey() + "\"", e.getValue() });
			}
			for (Map.Entry<String, String> e : mod.getTypeAliases().entrySet()) {
				db.get(TYPE_ALIAS).add(new String[] { "\"" + e.getKey() + "\"", e.getValue() });
			}
			return db;
		}

		private void processFunc(Function func) {
			String[] ss = new String[4];
			String params = "[";
			List<Pair<String, String>> paramsAndTypes = func.getParamsAndTypes();
			int numParams = paramsAndTypes.size();
			int i = 0;
			for (Pair<String, String> pt : paramsAndTypes) {
				params += "(" + pt.a + ", " + pt.b + ")";
				if (++i < numParams) {
					params += ", ";
				}
			}
			params += "]";
			ss[0] = "\"" + func.getName() + "\"";
			ss[1] = params;
			ss[2] = func.getRetType();
			ss[3] = func.getBody();
			db.get(FUNC_SIG).add(ss);
			if (func.isPure()) {
				db.get(LABELED_PURE).add(new String[] { ss[0] });
			}
		}

	}

}
