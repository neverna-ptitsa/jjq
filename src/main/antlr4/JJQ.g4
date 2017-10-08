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

structureSelectorOrLiteral
    : structureSelector
    | TRUE
    | FALSE
    ;

booleanExpression
    : andExpression
    | orExpression
    | structureSelectorOrLiteral
    ;

andExpression
    : structureSelectorOrLiteral andExpressionR
    ;

andExpressionR
    : (AND booleanExpression andExpressionR)?
    ;

orExpression
    : structureSelectorOrLiteral orExpressionR
    ;

orExpressionR
    : (OR booleanExpression orExpressionR)?
    ;


TRUE       : 'true';
FALSE      : 'false';
OR         : 'or';
AND        : 'and';
FLATMAP    : '[]' ;
DOT        : '.' ;
ID         : [_a-z0-9$]+ ;
WS         : [ \t\r\n]+ -> skip ;
