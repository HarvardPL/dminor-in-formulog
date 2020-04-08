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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Pair;

import edu.harvard.seas.pl.dminor_in_formulog.DminorBaseVisitor;
import edu.harvard.seas.pl.dminor_in_formulog.DminorLexer;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.AccumExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.ArgsContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.AscribeExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.BinopExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.CallExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.CollExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.CollTypeContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.CondExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.ExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.FromSelectExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.FromWhereSelectExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.FuncDefContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.LetExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.ModuleContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.NamedTypeContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.NumExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.ParamContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.ParenExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.RecordDefEntryContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.RecordEntryContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.RecordGetExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.RecordMakeExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.RecordTypeContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.RefinementTypeContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.SingletonTypeContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.StrExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.TypeDefContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.UnionTypeContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.UnopExprContext;
import edu.harvard.seas.pl.dminor_in_formulog.DminorParser.VarExprContext;

public final class Extractor {

	private Extractor() {
		throw new AssertionError("impossible");
	}

	public static List<Module> extract(Reader r) throws IOException {
		CharStream chars = CharStreams.fromReader(r);
		DminorLexer lexer = new DminorLexer(chars);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		DminorParser parser = new DminorParser(tokens);
		List<Module> modules = new ArrayList<>();
		for (ModuleContext modCtx : parser.prog().module()) {
			modules.add(modCtx.accept(new ModuleExtractor()));
		}
		return modules;
	}

	private static class ModuleExtractor extends DminorBaseVisitor<Module> {

		private final Map<String, String> typeAlias = new HashMap<>();

		private final List<Function> funcs = new ArrayList<>();

		private final Map<String, String> typeIndicatorFuncs = new HashMap<>();

		private String modName;

		public ModuleExtractor() {
			typeAlias.put("Any", "t_any");
			typeAlias.put("Integer32", "t_int");
			typeAlias.put("Logical", "t_bool");
			typeAlias.put("Text", "t_str");
		}

		private String qualifyId(String id) {
			switch (id) {
			case "Any":
			case "Integer32":
			case "Logical":
			case "Text":
				return id;
			default:
				return modName + "." + id;
			}
		}

		@Override
		public Module visitModule(ModuleContext ctx) {
			modName = ctx.name.getText();
			ctx.typeDef().forEach(typeDef -> handleTypeDefinition(typeDef));
			ctx.funcDef().forEach(func -> func.accept(funcExtractor));
			return new Module(modName, funcs, typeAlias, typeIndicatorFuncs);
		}

		private void handleTypeDefinition(TypeDefContext ctx) {
			String typeName = qualifyId(ctx.name.getText());
			TypeNameExtractor tne = new TypeNameExtractor();
			ctx.typ().accept(tne);
			if (tne.getTypeNames().contains(typeName)) {
				String funcName = "$" + typeName;
				String x = freshVar();
				String type = "t_refine(" + x + ", " + "t_any, " + "e_app(\"" + funcName + "\", [e_var(" + x + ")]))";
				typeAlias.put(typeName, type);
				String recType = ctx.typ().accept(typeExtractor);
				x = freshVar();
				String body = "e_type_test(e_var(" + x + "), " + recType + ")";
				List<Pair<String, String>> params = Collections.singletonList(new Pair<>(x, "t_any"));
				Function func = new Function(funcName, params, "t_bool", body, true);
				funcs.add(func);
				typeIndicatorFuncs.put(funcName, recType);
			} else {
				String type = ctx.typ().accept(typeExtractor);
				typeAlias.put(typeName, type);
			}
		}

		private class TypeNameExtractor extends DminorBaseVisitor<Void> {

			private final Set<String> typeNames = new HashSet<>();

			public Set<String> getTypeNames() {
				return typeNames;
			}

			@Override
			public Void visitNamedType(NamedTypeContext ctx) {
				String name = qualifyId(ctx.ID().getText());
				typeNames.add(name);
				return null;
			}

		}

		private final DminorBaseVisitor<String> typeExtractor = new DminorBaseVisitor<String>() {

			@Override
			public String visitNamedType(NamedTypeContext ctx) {
				String name = qualifyId(ctx.ID().getText());
				String type = typeAlias.get(name);
				if (type == null) {
					throw new AssertionError("Unrecognized type: " + name + "/" + qualifyId(name));
				}
				return type;
			}

			@Override
			public String visitRecordType(RecordTypeContext ctx) {
				List<RecordDefEntryContext> rectxs = new ArrayList<>(ctx.recordDefEntries().recordDefEntry());
				String type = makeRecordType(rectxs.remove(rectxs.size() - 1));
				for (RecordDefEntryContext rectx : rectxs) {
					type = "intersection_type(" + makeRecordType(rectx) + ", " + type + ")";
				}
				return type;
			}

			private String makeRecordType(RecordDefEntryContext ctx) {
				String label = "\"" + qualifyId(ctx.ID().getText()) + "\"";
				String entryType = ctx.typ().accept(this);
				return "t_entity(" + label + ", " + entryType + ")";
			}

			@Override
			public String visitRefinementType(RefinementTypeContext ctx) {
				String val = incrValueCnt();
				String type = ctx.typ().accept(this);
				String expr = ctx.expr().accept(exprExtractor);
				decrValueCnt();
				return "t_refine(" + val + ", " + type + ", " + expr + ")";
			}

			@Override
			public String visitSingletonType(SingletonTypeContext ctx) {
				String expr = ctx.expr().accept(exprExtractor);
				// XXX Not sure if t_any is right or if there is something more exact...
				return "singleton_type(" + expr + ", t_any)";
			}

			@Override
			public String visitUnionType(UnionTypeContext ctx) {
				String type1 = ctx.typ(0).accept(this);
				String type2 = ctx.typ(1).accept(this);
				return "union_type(" + type1 + ", " + type2 + ")";
			}

			@Override
			public String visitCollType(CollTypeContext ctx) {
				String type = ctx.typ().accept(this);
				return "t_coll(" + type + ")";
			}

		};

		private final DminorBaseVisitor<Void> funcExtractor = new DminorBaseVisitor<Void>() {

			@Override
			public Void visitFuncDef(FuncDefContext ctx) {
				String name = qualifyId(ctx.name.getText());
				List<Pair<String, String>> paramsAndTypes = new ArrayList<>();
				for (ParamContext pctx : ctx.params().param()) {
					String param = toVar(pctx.name.getText());
					String type = pctx.typ().accept(typeExtractor);
					paramsAndTypes.add(new Pair<>(param, type));
				}
				String retType = ctx.typ().accept(typeExtractor);
				String body = ctx.expr().accept(exprExtractor);
				funcs.add(new Function(name, paramsAndTypes, retType, body, false));
				return null;
			}

		};

		private final DminorBaseVisitor<String> exprExtractor = new DminorBaseVisitor<String>() {

			@Override
			public String visitStrExpr(StrExprContext ctx) {
				return "e_str(" + ctx.getText() + ")";
			}

			@Override
			public String visitVarExpr(VarExprContext ctx) {
				String text = ctx.ID().getText();
				switch (text) {
				case "false":
					return "e_bool(false)";
				case "true":
					return "e_bool(true)";
				default:
					return "e_var(" + toVar(ctx.ID().getText()) + ")";
				}
			}

			@Override
			public String visitAscribeExpr(AscribeExprContext ctx) {
				String expr = ctx.expr().accept(this);
				String type = ctx.typ().accept(typeExtractor);
				return "e_ascribe(" + expr + ", " + type + ")";
			}

			@Override
			public String visitUnopExpr(UnopExprContext ctx) {
				String s = "e_unop(";
				switch (ctx.unop.getType()) {
				case DminorParser.SUB:
					s += "u_neg, ";
					break;
				case DminorParser.NOT:
					s += "u_not, ";
					break;
				default:
					throw new AssertionError("Unexpected operator: " + ctx.unop.getText());
				}
				s += ctx.expr().accept(this) + ")";
				return s;
			}

			@Override
			public String visitCondExpr(CondExprContext ctx) {
				String condExpr = ctx.cond.accept(this);
				String thenBranch = ctx.thenBranch.accept(this);
				String elseBranch = ctx.elseBranch.accept(this);
				return "e_cond(" + condExpr + ", " + thenBranch + ", " + elseBranch + ")";
			}

			@Override
			public String visitCallExpr(CallExprContext ctx) {
				String args = ctx.args().accept(this);
				return "e_app(\"" + qualifyId(ctx.func.getText()) + "\", " + args + ")";
			}

			@Override
			public String visitArgs(ArgsContext ctx) {
				List<String> args = ctx.expr().stream().map(e -> e.accept(this)).collect(Collectors.toList());
				String s = "[";
				for (Iterator<String> it = args.iterator(); it.hasNext();) {
					s += it.next();
					if (it.hasNext()) {
						s += ", ";
					}
				}
				s += "]";
				return s;
			}

			@Override
			public String visitCollExpr(CollExprContext ctx) {
				String s = "e_coll([])";
				for (ExprContext ectx : ctx.args().expr()) {
					String e = ectx.accept(this);
					s = "e_add(" + e + ", " + s + ")";
				}
				return s;
			}

			@Override
			public String visitRecordGetExpr(RecordGetExprContext ctx) {
				String expr = ctx.expr().accept(this);
				String label = ctx.ID().getText();
				if (label.equals("Count")) {
					return makeCount(expr);
				} else {
					return "e_select(" + expr + ", \"" + qualifyId(label) + "\")";
				}
			}

			private String makeCount(String expr) {
				String x = freshVar();
				String y = freshVar();
				return makeAccum(x, expr, y, "e_ascribe(e_int(0), t_int)",
						"e_binop(b_add, e_var(" + y + "), e_int(1))");
			}

			@Override
			public String visitRecordMakeExpr(RecordMakeExprContext ctx) {
				String s = "e_entity([";
				for (Iterator<RecordEntryContext> it = ctx.recordEntries().recordEntry().iterator(); it.hasNext();) {
					RecordEntryContext rectx = it.next();
					s += "(\"";
					s += qualifyId(rectx.ID().getText());
					s += "\", ";
					s += rectx.expr().accept(this);
					s += ")";
					if (it.hasNext()) {
						s += ", ";
					}
				}
				s += "])";
				return s;
			}

			private String fromWhereSelect(String x, String from, String where, String select) {
				return "e_query(" + x + ", " + from + ", " + where + ", " + select + ")";
			}

			@Override
			public String visitFromSelectExpr(FromSelectExprContext ctx) {
				String var = toVar(ctx.ID().toString());
				String from = ctx.from.accept(this);
				String select = ctx.select.accept(this);
				return fromWhereSelect(var, from, "e_bool(true)", select);
			}

			@Override
			public String visitFromWhereSelectExpr(FromWhereSelectExprContext ctx) {
				String var = toVar(ctx.ID().toString());
				String from = ctx.from.accept(this);
				String select = ctx.select.accept(this);
				String where = ctx.where.accept(this);
				return fromWhereSelect(var, from, where, select);
			}

			@Override
			public String visitBinopExpr(BinopExprContext ctx) {
				String e1 = ctx.lhs.accept(this);
				String e2 = ctx.rhs.accept(this);
				String op = null;
				switch (ctx.binop.getType()) {
				case DminorParser.ADD:
					op = "b_add";
					break;
				case DminorParser.SUB:
					op = "b_sub";
					break;
				case DminorParser.MUL:
					op = "b_mul";
					break;
				case DminorParser.DIV:
					op = "b_div";
					break;
				case DminorParser.CMPEQ:
					op = "b_eq";
					break;
				case DminorParser.CMPGT:
					op = "b_gt";
					break;
				case DminorParser.CMPLT:
					op = "b_lt";
					break;
				case DminorParser.AND:
					op = "b_and";
					break;
				case DminorParser.OR:
					op = "b_or";
					break;
				case DminorParser.CMPNE:
					return "e_unop(u_not, e_binop(b_eq, " + e1 + ", " + e2 + "))";
				case DminorParser.UNION:
					return "e_union(" + e1 + ", " + e2 + ")";
				default:
					throw new AssertionError("Unexpected operator: " + ctx.binop.getText());
				}
				return "e_binop(" + op + ", " + e1 + ", " + e2 + ")";
			}

			@Override
			public String visitParenExpr(ParenExprContext ctx) {
				return ctx.expr().accept(this);
			}

			@Override
			public String visitNumExpr(NumExprContext ctx) {
				return "e_int(" + ctx.getText() + ")";
			}

			@Override
			public String visitLetExpr(LetExprContext ctx) {
				String var = toVar(ctx.var.getText());
				String val = ctx.val.accept(this);
				String cont = ctx.cont.accept(this);
				return "e_let(" + var + ", " + val + ", " + cont + ")";
			}

			@Override
			public String visitAccumExpr(AccumExprContext ctx) {
				String x = toVar(ctx.x.getText());
				String from = ctx.from.accept(this);
				String y = toVar(ctx.y.getText());
				String init = ctx.init.accept(this);
				String accum = ctx.accum.accept(this);
				return makeAccum(x, from, y, init, accum);
			}

			private String makeAccum(String x, String from, String y, String init, String accum) {
				return "e_accum(" + x + ", " + from + ", " + y + ", " + init + ", " + accum + ")";
			}

		};

		private int varCnt;

		private int valueCnt;

		private String incrValueCnt() {
			valueCnt++;
			String s = toVar("value");
			return s;
		}

		private void decrValueCnt() {
			valueCnt--;
		}

		private String freshVar() {
			String x = toVar("x" + varCnt);
			varCnt++;
			return x;
		}

		private String toVar(String name) {
			if (name.equals("value")) {
				name = "value" + valueCnt;
			}
			name = qualifyId(name);
			return "#{\"" + name + "\"}[value]";
		}

	}

}
