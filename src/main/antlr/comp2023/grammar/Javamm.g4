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

importDeclaration : 'import' ID ( '.' ID )* ';';

classDeclaration : 'class' ID ( 'extends' ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}';

varDeclaration : type ID ';';

methodDeclaration : instanceDeclaration
    | mainDeclaration ;

instanceDeclaration : ('public')? type ID '(' ( type ID ( ',' type ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}';

mainDeclaration : ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' ID ')' '{' ( varDeclaration )* ( statement )* '}' ;

type : 'int' '[' ']'
    | 'String'
    | 'boolean'
    | 'int'
    | ID;

statement : '{' ( statement )* '}' # ExprStmt
    | 'if' '(' expression ')' statement 'else' statement # CondicionalStmt
    | 'while' '(' expression ')' statement # LoopStmt
    | expression ';' # ExprStmt
    | var=ID '=' value=expression ';' # Assignment
    | ID '[' expression ']' '=' expression ';' # Assignment;

expression : '(' expression ')' #PrecedenceOp
    | op='!' expression #BinaryOp
    | expression op=('*' | '/') expression #BinaryOp
    | expression op=('+' | '-') expression #BinaryOp
    | expression op=('<' | '>') expression #BinaryOp
    | expression op='&&' expression #BinaryOp
    | expression '[' expression ']' #SubscriptOp
    | expression '.' 'length' #DotOp
    | expression '.' ID '(' ( expression ( ',' expression )* )? ')' #DotOp
    | 'new' 'int' '[' expression ']' #DeclarationOp
    | 'new' ID '(' ')' #DeclarationOp
    | value=INTEGER #Integer
    | 'true' #ReservedExpr
    | 'false' #ReservedExpr
    | value=ID #Identifier
    | 'this' #ReservedExpr;