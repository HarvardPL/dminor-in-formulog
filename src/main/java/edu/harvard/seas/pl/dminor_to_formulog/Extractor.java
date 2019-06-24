package edu.harvard.seas.pl.dminor_to_formulog;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Pair;

import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.BinopExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.CallExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.CondExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.FuncDefContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.LetExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.ModuleContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.NamedTypeContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.NumExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.ParamContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.ParenExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.RecordDefEntryContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.RecordEntryContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.RecordGetExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.RecordMakeExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.RecordTypeContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.RefinementTypeContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.StrExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.UnopExprContext;
import edu.harvard.seas.pl.dminor_to_formulog.DminorParser.VarExprContext;

public final class Extractor {

	private Extractor() {
		throw new AssertionError("impossible");
	}

	public static Module extract(Reader r) throws IOException {
		CharStream chars = CharStreams.fromReader(r);
		DminorLexer lexer = new DminorLexer(chars);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		DminorParser parser = new DminorParser(tokens);
		return parser.module().accept(new ModuleExtractor());
	}

	private static class ModuleExtractor extends DminorBaseVisitor<Module> {

		private final Map<String, String> typeAlias = new HashMap<>();

		private final List<Function> funcs = new ArrayList<>();

		public ModuleExtractor() {
			typeAlias.put("Any", "t_any");
			typeAlias.put("Integer32", "t_int");
			typeAlias.put("Logical", "t_bool");
			typeAlias.put("Text", "t_str");
		}

		@Override
		public Module visitModule(ModuleContext ctx) {
			String name = ctx.name.getText();
			ctx.typeDef().forEach(typeDef -> {
				String typeName = typeDef.name.getText();
				String type = typeDef.typ().accept(typeExtractor);
				typeAlias.put(typeName, type);
			});
			ctx.funcDef().forEach(func -> func.accept(funcExtractor));
			return new Module(name, funcs);
		}

		private final DminorBaseVisitor<String> typeExtractor = new DminorBaseVisitor<String>() {

			@Override
			public String visitNamedType(NamedTypeContext ctx) {
				String name = ctx.ID().getText();
				String type = typeAlias.get(name);
				if (type == null) {
					throw new AssertionError("Unrecognized type: " + name);
				}
				return type;
			}

			@Override
			public String visitRecordType(RecordTypeContext ctx) {
				String type = "t_any";
				for (RecordDefEntryContext rectx : ctx.recordDefEntries().recordDefEntry()) {
					String label = "\"" + rectx.ID().getText() + "\"";
					String entryType = rectx.typ().accept(this);
					type = "intersection_type(t_entity(" + label + ", " + entryType + "), " + type + ")";
				}
				return type;
			}
			
			@Override
			public String visitRefinementType(RefinementTypeContext ctx) {
				bumpValueCnt();
				String val = toVar("value");
				String type = ctx.typ().accept(this);
				String expr = ctx.expr().accept(exprExtractor);
				// Bump value again, just to be safe.
				bumpValueCnt();
				return "t_refine(" + val + ", " + type + ", " + expr + ")";
			}

		};

		private final DminorBaseVisitor<Void> funcExtractor = new DminorBaseVisitor<Void>() {

			@Override
			public Void visitFuncDef(FuncDefContext ctx) {
				String name = ctx.name.getText();
				List<Pair<String, String>> paramsAndTypes = new ArrayList<>();
				for (ParamContext pctx : ctx.params().param()) {
					String param = toVar(pctx.name.getText());
					String type = pctx.typ().accept(typeExtractor);
					paramsAndTypes.add(new Pair<>(param, type));
				}
				String retType = ctx.typ().accept(typeExtractor);
				String body = ctx.expr().accept(exprExtractor);
				funcs.add(new Function(name, paramsAndTypes, retType, body));
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
				return "e_var(" + toVar(ctx.ID().getText()) + ")";
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
				List<String> args = ctx.args().expr().stream().map(e -> e.accept(this)).collect(Collectors.toList());
				String s = "e_app(\"" + ctx.func.getText() + "\", [";
				for (Iterator<String> it = args.iterator(); it.hasNext();) {
					s += it.next();
					if (it.hasNext()) {
						s += ", ";
					}
				}
				s += "])";
				return s;
			}
			
			@Override
			public String visitRecordGetExpr(RecordGetExprContext ctx) {
				String expr = ctx.expr().accept(this);
				String label = "\"" + ctx.ID().getText() + "\"";
				return "e_select(" + expr + ", " + label + ")";
			}
			
			@Override
			public String visitRecordMakeExpr(RecordMakeExprContext ctx) {
				String s = "e_entity([";
				for (Iterator<RecordEntryContext> it = ctx.recordEntries().recordEntry().iterator(); it.hasNext();) {
					RecordEntryContext rectx = it.next();
					s += "(\"";
					s += rectx.ID().getText();
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

			@Override
			public String visitBinopExpr(BinopExprContext ctx) {
				String s = "e_binop(";
				switch (ctx.binop.getType()) {
				case DminorParser.ADD:
					s += "b_add, ";
					break;
				case DminorParser.SUB:
					s += "b_sub, ";
					break;
				case DminorParser.MUL:
					s += "b_mul, ";
					break;
				case DminorParser.DIV:
					s += "b_div, ";
					break;
				case DminorParser.CMPEQ:
					s += "b_eq, ";
					break;
				case DminorParser.CMPGT:
					s += "b_gt, ";
					break;
				case DminorParser.CMPLT:
					s += "b_lt, ";
					break;
				case DminorParser.AND:
					s += "b_and, ";
					break;
				case DminorParser.OR:
					s += "b_or, ";
					break;
				default:
					throw new AssertionError("Unexpected operator: " + ctx.binop.getText());
				}
				s += ctx.lhs.accept(this) + ", ";
				s += ctx.rhs.accept(this) + ")";
				return s;
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

		};
		
		private int valueCnt = 0;

		private void bumpValueCnt() {
			++valueCnt;
		}
		
		private String toVar(String name) {
			if (name.equals("value")) {
				name = "value" + valueCnt;
			}
			return "#" + name + "[value]";
		}

	}

}
