grammar Dminor;

module
:
	'module' ID '{'
	(
		typeDef
		| funcDef
	)* '}'
;

typeDef
:
	'type'
;

funcDef
:
	ID
;

ID
:
	[a-zA-Z] [a-zA-Z0-9_]*
;