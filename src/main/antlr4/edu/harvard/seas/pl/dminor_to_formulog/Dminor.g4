grammar Dminor;

module
:
	'module' name = ID '{'
	(
		typeDef
		| funcDef
	)* '}'
;

typeDef
:
	'type' name = ID ':' typ ';'
;

typ
:
	ID # namedType
;

funcDef
:
	name = ID '(' params ')' ':' retType = typ '{' expr '}'
;

params
:
	(
		param
		(
			',' param
		)*
	)?
;

param
:
	name = ID ':' type = typ
;

expr
:
	'(' expr ')' # parenExpr
	| NUM # numExpr
	| STR # strExpr
	| ID # varExpr
	| unop =
	(
		NEG
		| NOT
	) expr # unopExpr
	| '(' cond = expr ')' '?' thenBranch = expr ':' elseBranch = expr # condExpr
	| func = ID '(' args ')' # callExpr
	| 'let' var = ID '=' val = expr 'in' cont = expr # letExpr
	| lhs = expr binop =
	(
		MUL
		| DIV
	) rhs = expr # binopExpr
	| lhs = expr binop =
	(
		ADD
		| SUB
	) rhs = expr # binopExpr
	| lhs = expr binop =
	(
		CMPGT
		| CMPLT
	) rhs = expr # binopExpr
	| lhs = expr binop = CMPEQ rhs = expr # binopExpr
	| lhs = expr binop = AND rhs = expr # binopExpr
	| lhs = expr binop = OR rhs = expr # binopExpr
;

args
:
	(
		expr
		(
			',' expr
		)*
	)?
;

NEG
:
	'-'
;

NOT
:
	'!'
;

ADD
:
	'+'
;

SUB
:
	'-'
;

MUL
:
	'*'
;

DIV
:
	'/'
;

CMPEQ
:
	'=='
;

CMPGT
:
	'>'
;

CMPLT
:
	'<'
;

AND
:
	'&&'
;

OR
:
	'||'
;

NUM
:
	'-'? [0-9]+
;

STR
:
	'"' ~( '"' )* '"'
;

ID
:
	[a-zA-Z] [a-zA-Z0-9_]*
;

COMMENT
:
	'//' ~[\n\r]* -> skip
;

WS
:
	[ \t\r\n]+ -> skip
;