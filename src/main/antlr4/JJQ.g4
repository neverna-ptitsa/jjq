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
    | TRUE
    | FALSE
    ;

booleanExpression
    : structureSelectorOrBooleanLiteral booleanExpressionR
    ;

booleanExpressionR
    : (AND booleanExpression booleanExpressionR)?
    | (OR booleanExpression booleanExpressionR)?
    ;

TRUE       : 'true';
FALSE      : 'false';
OR         : 'or';
AND        : 'and';
FLATMAP    : '[]' ;
DOT        : '.' ;
ID         : [_a-z0-9$]+ ;
WS         : [ \t\r\n]+ -> skip ;
