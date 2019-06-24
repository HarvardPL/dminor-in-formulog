package edu.harvard.seas.pl.dminor_to_formulog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.misc.Pair;

public final class Relationizer {

	private Relationizer() {
		throw new AssertionError("impossible");
	}
	
	public static Map<String, List<String[]>> relationize(Module mod) {
		return (new Impl(mod)).run();
	}

	private static final String funcSig = "func_sig";
	private static final String labeledPure = "labeled_pure";
	
	private static class Impl {
	
		private final Module mod;
		private final Map<String, List<String[]>> db = new HashMap<>();
		
		public Impl(Module mod) {
			this.mod = mod;
			db.put(funcSig, new ArrayList<>());
			db.put(labeledPure, new ArrayList<>());
		}
		
		public Map<String, List<String[]>> run() {
			for (Function func : mod.getFuncs()) {
				processFunc(func);
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
			db.get(funcSig).add(ss);
		}
		
	}
	
}
