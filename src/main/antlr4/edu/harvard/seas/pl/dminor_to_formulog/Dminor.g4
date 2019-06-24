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
	| '{' typ '*' '}' # collType
	| '{' expr '}' # singletonType
	| '{' recordEntries '}' # recordType
	| typ '|' typ # unionType
;

recordEntries
:

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
		SUB
		| NOT
	) expr # unopExpr
	| func = ID '(' args ')' # callExpr
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
	| '(' cond = expr ')' '?' thenBranch = expr ':' elseBranch = expr # condExpr
	| 'let' var = ID '=' val = expr 'in' cont = expr # letExpr
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

CCOMMENT
:
	'/*' .*? '*/' -> skip
;

WS
:
	[ \t\r\n]+ -> skip
;