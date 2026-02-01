package org.rhai;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import static org.rhai.RhaiTypes.*;
import com.intellij.psi.TokenType;
import java.util.Stack;

%%

%class RhaiLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{
    return;
%eof}
%line
%column
%char

// Состояния
%state IN_STRING, IN_INTERPOLATED_STRING
%xstate IN_BLOCK_COMMENT, IN_DOC_COMMENT

// Стек состояний для вложенных интерполяций и строк
%{
    private final Stack<Integer> stateStack = new Stack<>();
    private final Stack<Integer> braceStack = new Stack<>();

    private void pushState(int state) {
        stateStack.push(yystate());
        yybegin(state);
    }

    private void popState() {
        if (!stateStack.isEmpty()) {
            yybegin(stateStack.pop());
        } else {
            yybegin(YYINITIAL);
        }
    }

    private void pushBrace() {
        if (!braceStack.isEmpty()) {
            braceStack.push(braceStack.pop() + 1);
        } else {
            braceStack.push(1);
        }
    }

    private void popBrace() {
        if (!braceStack.isEmpty()) {
            int depth = braceStack.pop() - 1;
            if (depth > 0) {
                braceStack.push(depth);
            }
        }
    }

    private boolean inInterpolation() {
        return !braceStack.isEmpty() && braceStack.peek() > 0;
    }
%}

// === Макросы ===

// Пробельные символы
WHITE_SPACE     = [ \t\f]+
LINE_TERMINATOR = \r|\n|\r\n

// Идентификаторы
IDENTIFIER      = [_a-zA-Z][_a-zA-Z0-9]*

// Числа с разделителями _
DIGIT           = [0-9]
DIGIT_OR_UNDER  = [_0-9]
HEX_DIGIT       = [_0-9a-fA-F]
BIN_DIGIT       = [_01]
OCT_DIGIT       = [_0-7]

// Целочисленные литералы
INTEGER_LITERAL = {DIGIT_OR_UNDER}+
BINARY_LITERAL  = 0[bB]{BIN_DIGIT}+
OCTAL_LITERAL   = 0[oO]{OCT_DIGIT}+
HEX_LITERAL     = 0[xX]{HEX_DIGIT}+

// Вещественные литералы
FLOAT_LITERAL   = {DIGIT_OR_UNDER}*\.{DIGIT_OR_UNDER}+|{DIGIT_OR_UNDER}+\.|{DIGIT_OR_UNDER}+[eE][+-]?{DIGIT_OR_UNDER}+|{DIGIT_OR_UNDER}*\.{DIGIT_OR_UNDER}+[eE][+-]?{DIGIT_OR_UNDER}+

NUMBER_LITERAL  = {BINARY_LITERAL}|{OCTAL_LITERAL}|{HEX_LITERAL}|{FLOAT_LITERAL}|{INTEGER_LITERAL}

// Строки и символы
STRING_CHAR     = [^\"\\\r\n]
CHAR_CHAR       = [^\'\\\r\n]
// Экранированные последовательности должны быть в одной строке
ESCAPE_SEQUENCE = \\[tnr\"\\'0]|\\x{HEX_DIGIT}{2}|\\u\{ {HEX_DIGIT}{1,6} \}|\\u{HEX_DIGIT}{4}|\\U{HEX_DIGIT}{8}

CHAR_LITERAL    = \'({ESCAPE_SEQUENCE}|{CHAR_CHAR})\'

// Комментарии
LINE_COMMENT    = "//"[^\r\n]*
DOC_COMMENT_LINE= "///"[^\r\n]*
SHEBANG         = "#!"[^\r\n]*

// === Правила ===
%%

// === Начальное состояние ===
<YYINITIAL> {
    // Пробелы и переводы строк
    {WHITE_SPACE}       { return TokenType.WHITE_SPACE; }
    {LINE_TERMINATOR}   { return TokenType.WHITE_SPACE; }

    // Shebang (только в начале файла)
    ^ {SHEBANG}         { return SHEBANG; }

    // Комментарии
    {DOC_COMMENT_LINE}  { return DOC_COMMENT; }
    {LINE_COMMENT}      { return LINE_COMMENT; }
    "/*"                { pushState(IN_BLOCK_COMMENT); return BLOCK_COMMENT; }
    "/**"               { pushState(IN_DOC_COMMENT); return DOC_COMMENT; }

    // Строки и символы
    \"                  { pushState(IN_STRING); return STRING_START; }
    "`"                 { pushState(IN_INTERPOLATED_STRING); return INTERPOLATED_START; }
    {CHAR_LITERAL}      { return CHAR_LITERAL; }

    // Числа
    {NUMBER_LITERAL}    { return NUMBER; }

    // Ключевые слова (соответствуют официальной грамматике Rhai)
    "if"                { return IF; }
    "else"              { return ELSE; }
    "switch"            { return SWITCH; }
    "while"             { return WHILE; }
    "until"             { return UNTIL; }
    "loop"              { return LOOP; }
    "for"               { return FOR; }
    "in"                { return IN; }
    "do"                { return DO; }
    "break"             { return BREAK; }
    "continue"          { return CONTINUE; }
    "return"            { return RETURN; }
    "throw"             { return THROW; }
    "try"               { return TRY; }
    "catch"             { return CATCH; }
    "import"            { return IMPORT; }
    "export"            { return EXPORT; }
    "as"                { return AS; }
    "fn"                { return FN; }
    "let"               { return LET; }
    "const"             { return CONST; }
    "private"           { return PRIVATE; }
    "global"            { return GLOBAL; }
    "shared"            { return SHARED; }
    "sync"              { return SYNC; }
    "async"             { return ASYNC; }
    "await"             { return AWAIT; }
    "true"              { return TRUE; }
    "false"             { return FALSE; }
    "null"              { return NULL; }
    "this"              { return THIS; }

    // Литералы и специальные значения
    "inf"               { return INF; }
    "-inf"              { return NEG_INF; }
    "NaN"               { return NAN; }

    // Идентификаторы (все остальные)
    {IDENTIFIER}        { return IDENTIFIER; }

    // Составные операторы (длинные первыми)
    "**="               { return POW_ASSIGN; }
    "**"                { return POW; }
    "=="                { return EQ; }
    "!="                { return NE; }
    "<="                { return LE; }
    ">="                { return GE; }
    "<=>"               { return SPACESHIP; }
    "&&"                { return AND; }
    "||"                { return OR; }
    "??"                { return NULL_COALESCING; }
    "..="               { return DOT_DOT_EQ; }
    ".."                { return DOT_DOT; }
    "=>"                { return ARROW; }
    "->"                { return THIN_ARROW; }
    "<-"                { return LEFT_ARROW; }
    "::"                { return DOUBLE_COLON; }

    // Составные операторы присваивания
    "+="                { return PLUS_ASSIGN; }
    "-="                { return MINUS_ASSIGN; }
    "*="                { return MUL_ASSIGN; }
    "/="                { return DIV_ASSIGN; }
    "%="                { return MOD_ASSIGN; }
    "&="                { return AND_ASSIGN; }
    "|="                { return OR_ASSIGN; }
    "^="                { return XOR_ASSIGN; }
    "<<="               { return SHL_ASSIGN; }
    ">>="               { return SHR_ASSIGN; }

    // Побитовые операторы
    "<<"                { return SHL; }
    ">>"                { return SHR; }
    "&"                 { return BAND; }
    "|"                 { return BOR; }
    "^"                 { return BXOR; }

    // Простые операторы
    "+"                 { return PLUS; }
    "-"                 { return MINUS; }
    "*"                 { return MUL; }
    "/"                 { return DIV; }
    "%"                 { return MOD; }
    "!"                 { return NOT; }
    "="                 { return ASSIGN; }
    "<"                 { return LT; }
    ">"                 { return GT; }

    // Разделители
    "."                 { return DOT; }
    ","                 { return COMMA; }
    ";"                 { return SEMICOLON; }
    ":"                 { return COLON; }
    "?"                 { return QUESTION; }
    "@"                 { return AT; }
    "$"                 { return DOLLAR; }
    "~"                 { return TILDE; }
    "#"                 { return HASH; }

    // Скобки
    "("                 {
                         if (inInterpolation()) pushBrace();
                         return LPAREN;
                       }
    ")"                 {
                         if (inInterpolation()) popBrace();
                         return RPAREN;
                       }
    "{"                 {
                         if (inInterpolation()) pushBrace();
                         return LBRACE;
                       }
    "}"                 {
                         if (inInterpolation()) {
                             popBrace();
                             if (!inInterpolation()) {
                                 yybegin(IN_INTERPOLATED_STRING);
                                 return INTERPOLATED_EXPR_END;
                             }
                         }
                         return RBRACE;
                       }
    "["                 {
                         if (inInterpolation()) pushBrace();
                         return LBRACKET;
                       }
    "]"                 {
                         if (inInterpolation()) popBrace();
                         return RBRACKET;
                       }

    // Неожиданный символ
    [^]                 { return TokenType.BAD_CHARACTER; }
}

// === Строки в двойных кавычках ===
<IN_STRING> {
    \"                  { popState(); return STRING_END; }

    // Экранированные последовательности
    {ESCAPE_SEQUENCE}   { return STRING_ESCAPE; }

    // Продолжение строки на новой строке
    \\{LINE_TERMINATOR} { return STRING_ESCAPE_NEWLINE; }

    // Некорректное экранирование
    \\[^]               { return INVALID_ESCAPE; }

    // Содержимое строки
    {STRING_CHAR}+      { return STRING_CONTENT; }

    // Конец файла внутри строки
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}

// === Интерполированные строки (обратные кавычки) ===
<IN_INTERPOLATED_STRING> {
    "`"                 { popState(); return INTERPOLATED_END; }

    // Начало интерполированного выражения
    "${"                {
                         pushState(YYINITIAL);
                         braceStack.push(1);
                         return INTERPOLATED_EXPR_START;
                       }

    // Экранированные последовательности
    {ESCAPE_SEQUENCE}   { return INTERPOLATED_ESCAPE; }

    // Продолжение строки на новой строке
    \\{LINE_TERMINATOR} { return INTERPOLATED_ESCAPE_NEWLINE; }

    // Доллар без фигурных скобок
    "$"/[^\{]           { return INTERPOLATED_CONTENT; }

    // Некорректное экранирование
    \\[^]               { return INVALID_ESCAPE; }

    // Содержимое интерполированной строки
    [^`\\\$\r\n]+       { return INTERPOLATED_CONTENT; }

    // Конец файла внутри интерполированной строки
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}

// === Многострочные комментарии ===
<IN_BLOCK_COMMENT> {
    "*/"                { popState(); return BLOCK_COMMENT; }
    [^*]+               { /* потребляем содержимое */ }
    "*"                 { /* потребляем одиночную звёздочку */ }
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}

// === Документационные комментарии ===
<IN_DOC_COMMENT> {
    "*/"                { popState(); return DOC_COMMENT; }
    [^*]+               { /* потребляем содержимое */ }
    "*"                 { /* потребляем одиночную звёздочку */ }
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}