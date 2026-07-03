/**
 * Define a grammar for the CRML language
 */
grammar crml;

definition : definition_type id 'is' dependency* '{'
		(element_def)* 
		'}'  ';' EOF;	

dependency 
	: id 'union' # SingleDependency
	|'flatten' '{' id (',' id)* '}' 'union' # DependencySet
	;

definition_type : 'model' | 'package' | 'library'; // should we keep library?

element_def 
	: comment # CommentElement //TODO
	| template # TemplateElement //TODO
	| class_def # ClassElement
	| uninstantiated_def # UninstantiatedElement //TODO
	| type_def # TypeElement //TODO
	| operator # OperatorElement //TODO
	| var_def # VaraibleElement //TODO
	| category # CategoryElement //TODO
	;
	
class_def : partial='partial'? 'class' id 'is' ('{' class_var_def+ '}' extension? )';' ;

extension 
	: 'extends' type class_params? id?
	;

uninstantiated_def : static_qualifier (type id (',' id)* | structure_type id (',' id)* )';' ; //TODO

static_qualifier : 'parameter' ; //TODO

category : 'Category' id '=' '{' category_pair (',' category_pair)* '}' ';'; //TODO

category_pair : '(' op ',' op ')'; //TODO

//association : 'Category' empty_set c_set=id 'is' 'associate' c_name=id 'with' c_op_name=user_keyword ';';
 
var_def : cnst='constant'? var_qualifier? type id  (arg_list | 'is' (exp | is_external = 'external'))? ';' ;

operator : 'Operator' '[' type ']' operator_def ';' ; //TODO

template : 'Template' (id | user_keyword)+ '=' exp ';' ; //TODO

class_params : '(' (id '=' exp)+ ')'; //TODO

operator_def :  (type id | user_keyword)+ '=' apply_category? exp ; //TODO

apply_category : 'apply' assoc=id 'on'; //TODO
	 
type_def : partial='partial'? 'type' id ('extends' type  arg_list? id?)?  ('{' class_var_def * '}' )? ; //TODO
	 
class_var_def 
	: var_def 
	|'alias' id ';' //TODO
	| comment //TODO
	| 'forbid' (op| op) (',' (op| op))* ';' //TODO
	| uninstantiated_def  //TODO
	;

var_qualifier : 'fixed'; //TODO: is this something that is now the constant?

// Change: at some point, it allowed exp and arg_list, now it is just expt, to make the processing simpler
// WARNING: note that if a single value is provided in parenthesis, it may look like an arg_list, but it is a sub-expression
arg_list : '(' (id ('='| 'is') exp) (',' id ('='| 'is') exp)* ')' ; //TODO

crml_component_reference : '.'? id array_subscripts? ( '.' id array_subscripts? )* ; //TODO ???

type :   (builtin_type | id ) isset=empty_set?;

builtin_type : 'Integer' |'Real' | 'Boolean' | 'String' | 'Clock' | 'Set' | 'Period' |'Periods'| 'Event' | 'Requirement';

structure_type : 'type' | 'class';

external_type : type | structure_type ; //Unused?

boolean_value : 'true' |'false' | 'undecided' | 'undefined' ;

// Can't use named contexts as then no visitExp is generated and processing becomes unnecessary complicated.
constant 
	: boolean_value 
	| string 
	| number 
	| time 
	;

time : 'time';

//TODO: Why is this separed to an 
set_def : '{' (exp (',' exp)*)? '}' ;

empty_set : '{' '}';

sub_exp : '(' exp ')' ;

trim : 'trim' exp 'on' exp;

sum: 'sum' '(' exp (',' exp)+')' ;

when_exp : 'when' when_e=exp 'then' then_e=exp;

integrate : 'integrate' exp 'on' exp;

duration : 'duration' exp 'on' exp;

tick : 'tick' id;

// Can't use named contexts as then no visitExp is generated and processing becomes unnecessary complicated.
// TODO: Operator precendence has to be cleaned up
exp 
 	: sub_exp //# SubExpression
	| constant //# ConstantExpression
	| constructor //# ConstructorExpression
	| sum //# SumExpression
	| trim //# TrimExpression
	| p1=exp 'proj' ('(' opt=exp ')')?  p2=exp 
	| period_op //# PoeriodOperationExpression
	| iterator //# IteratorExpression
	//| 'apply' cat=id 'on' '(' exp ')'
//TODO: with mater on filter pre 
// Unary operations
	| lhs=exp uop0=('start' | 'end')
	| uop1=('-' | '+' | 'par' ) rhs=exp //TODO do we need '+'?
	| uop2=('cos' | 'acos' | 'sin' | 'asin' ) rhs=exp
	| uop3=('exp' | 'log' | 'log10' | 'card') rhs=exp
	| uop4='not' rhs=exp
// Binary operations
	| <assoc=right> lhs=exp bop0='^' rhs=exp
	| lhs=exp bop1=('*'|'/'|'mod') rhs=exp
	| lhs=exp bop2=('+'|'-') rhs=exp
	| lhs=exp bop3=('+'|'-') rhs=exp
	| lhs=exp bop4=('<'|'<='|'>='|'>'|'=='|'<>') rhs=exp
	| lhs=exp bop5=('and'|'or') rhs=exp
	| lhs=exp bop6='at' rhs=exp
//
 	| uright=user_keyword right=exp //# UserRightExpression
	| left=exp ubinary=user_keyword right=exp //# UserLeftBinaryExpression
	| left=exp uleft=user_keyword  //# UserLeftExpression
	| id 
 	| 'element' //# ElementExpression
	| 'terminate' //# TerminateExpression
	| when_exp //# WhenExpression
//	| exp 'at' at=exp // Moved to binary operator. Why was it separate?
 	| integrate //# IntegrateExpression
	| tick //# TickExpression
	| crml_component_reference //# ComponentReferenceExpression
	| if_exp //# IfExpression
	| set_def //# SetDefinitionExpression
	| 'evaluate' exp //# EvaluateExpression
	| duration //# DurationExpression
	;
 	 
iterator : name= ITERATOR;

if_exp : 'if' if_e=exp 'then' then_e=exp ('else' else_e=exp);

constructor : 'new' type (arg_list | exp)?;
	
period_op : lb=('['| ']') exp ',' exp rb=('['| ']') ; 

//right_op : 'start' | 'end';
//

// Needed for forbid and category
op : builtin_op|user_keyword ;

//TODO: Check operator precendence. (BooleanIntegration_no_ext.crml)
// Note: expressions DO NOT use this rule due to operator priority
// ALWAYS sync this with exp_un and exp_bin
builtin_op : 'and' | '*' | '+' | '-' | '/' | 'with' | 'master' | 'on' | 'filter'
				| '<=' | '<' | '>=' | '>' | '<>' | 'par' | '==' |
				'pre' | 'not'| '-' | 'card' | 'or' | '^' |
				'mod' |
				'exp' | 'log' | 'log10' |
				'cos' |'acos' | 'sin' | 'asin' |
				'at' ;





array_subscripts :
  '[' subscript ( ',' subscript )* ']'
  ;

subscript :
  ':' | exp
  ;

id: IDENT;
user_keyword : USER_KEYWORD;
comment : LINE_COMMENT;
number : UNSIGNED_NUMBER;
string : STRING;

IDENT : NONDIGIT ( DIGIT | NONDIGIT )* ;

ITERATOR : NONDIGIT ( DIGIT | NONDIGIT )* '.element';

USER_KEYWORD : '\'' (NONDIGIT|SYMBOL) (NONDIGIT|' '|SYMBOL|DIGIT)* '\'';

fragment CAPS :  'A' .. 'Z' ;
fragment LOWCASE :  'a' .. 'z' ;
fragment NONDIGIT : '_' | 'a' .. 'z' | 'A' .. 'Z' ;
fragment DIGIT : '0' .. '9' ;
fragment SYMBOL : '='|'>'|'<';

// Whitespace and comments

BOM : '\u00EF' '\u00BB' '\u00BF' ;

WS : ( ' ' | '\t' | NL )+ -> channel(HIDDEN)
  ;

LINE_COMMENT
    : '//' ( ~('\r'|'\n')* ) (NL|EOF) -> channel(HIDDEN)
    ;

ML_COMMENT
    :   '/*' (.)*? '*/' -> channel(HIDDEN)
    ;

fragment
NL: '\r\n' | '\n' | '\r';

// Lexical units except for keywords


STRING : '"' ( S_CHAR | S_ESCAPE )* '"' ;

fragment S_CHAR : NL | ~('\r' | '\n' | '\\' | '"'); // Unicode other than " and \

fragment Q_IDENT : '\'' ( Q_CHAR | S_ESCAPE ) ( Q_CHAR | S_ESCAPE | '"' )* '\'' ;

fragment Q_CHAR
   : NONDIGIT | DIGIT | '!' | '#' | '$' | '%' | '&' | '(' | ')' | '*'
   | '+' | ',' | '-' | '.' | '/' | ':' | ';' | '<' | '>' | '=' | '?'
   | '@' | '[' | ']' | '^' | '{' | '}' | '|' | '~' | ' '
   ;
fragment S_ESCAPE : '\\'
  ( '\'' | '"' | '?' | '\\' | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v')
  ;

fragment UNSIGNED_INTEGER : DIGIT+ ;
fragment EXPONENT : ( 'e' | 'E' ) ( '+' | '-' )? DIGIT+ ;

UNSIGNED_NUMBER : DIGIT+ ( '.' (DIGIT)* )? ( EXPONENT )? ;