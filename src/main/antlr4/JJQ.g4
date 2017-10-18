grammar JJQ;
@header {
package com.github.nevernaptitsa;
}

jjqExpression
    : stageExpression ('|' stageExpression)* EOF
    ;

stageExpression
    : arrayFlatMap
    | structureSelector arrayFlatMap?
    | booleanExpression
    ;

arrayFlatMap
    : DOT ? FLATMAP
    ;

structureSelector
    : DOT
    | DOT ID structureSelector?
    ;

structureSelectorOrBooleanLiteral
    : structureSelector
    | BOOLEAN_LITERAL
    ;

booleanExpression
    : structureSelectorOrBooleanLiteral booleanExpressionR
    ;

booleanExpressionR
    : (BOOLEAN_OP booleanExpression booleanExpressionR)?
    ;


BOOLEAN_LITERAL : 'true' | 'false';
BOOLEAN_OP      : '==' | '!=' | 'and' | 'or' ;
FLATMAP         : '[]' ;
DOT             : '.' ;
ID              : [_a-z0-9$]+ ;
WS              : [ \t\r\n]+ -> skip ;
