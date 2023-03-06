grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INTEGER : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;

TRADICIONAL_COMMENT : '/*' .*? '*/' -> channel(HIDDEN);

END_OF_LINE_COMMENT : '//' .*? ('\r' | '\n' | EOF) -> channel(HIDDEN);

WS : [ \t\n\r\f]+ -> skip ;

program : (importDeclaration)* classDeclaration EOF;

importDeclaration : 'import' library=ID ( '.' ID )* ';';

classDeclaration : 'class' name=ID ( 'extends' ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}';

varDeclaration : (accessModifier)? type var=ID ';';

methodDeclaration : instanceDeclaration
    | mainDeclaration ;

instanceDeclaration : ('public')? type instance=ID '(' ( type parameter=ID ( ',' type parameter=ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}';

mainDeclaration : ('public')? 'static' 'void' 'main' '(' type parameter=ID ')' '{' ( varDeclaration )* ( statement )* '}' ;

accessModifier : value='private'
    | value='public'
    | value='protected';

type : value='int' '[' ']'
    | value='String' '[' ']'
    | value='String'
    | value='boolean'
    | value='int'
    | value=ID;

statement : '{' ( statement )* '}' # Stmt
    | 'if' '(' expression ')' statement 'else' statement # CondicionalStmt
    | 'while' '(' expression ')' statement # LoopStmt
    | expression ';' # ExprStmt
    | var=ID '=' value=expression ';' # Assignment
    | ID '[' expression ']' '=' expression ';' # Assignment
    | 'return' expression? ';' # ReturnStmt;

expression : '(' expression ')' #PrecedenceOp
    | op='!' expression #BinaryOp
    | expression op=('*' | '/') expression #BinaryOp
    | expression op=('+' | '-') expression #BinaryOp
    | expression op=('<' | '>') expression #BinaryOp
    | expression op='&&' expression #BinaryOp
    | expression '[' expression ']' #SubscriptOp
    | expression '.' 'length' #DotOp
    | expression '.' method=ID '(' ( expression ( ',' expression )* )? ')' #DotOp
    | 'new' 'int' '[' expression ']' #ArrayDeclaration
    | 'new' objClass=ID '(' ')' #ObjectDeclaration
    | value=INTEGER #Integer
    | value='true' #ReservedExpr
    | value='false' #ReservedExpr
    | value=ID #Identifier
    | value='this' #ReservedExpr;