package org.rhai;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import org.rhai.RhaiTypes;

%%

%class RhaiLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{
    return;
%eof}

// Состояния для строк и комментариев
%state IN_STRING, IN_RAW_STRING, IN_MULTI_STRING
%state IN_BLOCK_COMMENT, IN_DOC_COMMENT

// Состояния для интерполированных строк - без стеков!
// IN_INTERPOLATED_STRING - внутри `...`
// IN_INTERP_EXPR_N - внутри ${...}, N = глубина вложенных скобок
%state IN_INTERPOLATED_STRING
%state IN_INTERP_EXPR_0, IN_INTERP_EXPR_1, IN_INTERP_EXPR_2, IN_INTERP_EXPR_3, IN_INTERP_EXPR_4

// Java-код
%{
    private int rawStringHashes = 0;
%}

// === Макросы ===

// Пробельные символы
WHITE_SPACE     = [ \t\f]+

// Идентификаторы
IDENTIFIER      = [_a-zA-Z][_a-zA-Z0-9]*

// Числа
DIGIT_OR_UNDER  = [_0-9]
HEX_DIGIT       = [_0-9a-fA-F]
BIN_DIGIT       = [_01]
OCT_DIGIT       = [_0-7]

INTEGER_LITERAL = {DIGIT_OR_UNDER}+
BINARY_LITERAL  = 0[bB]{BIN_DIGIT}+
OCTAL_LITERAL   = 0[oO]{OCT_DIGIT}+
HEX_LITERAL     = 0[xX]{HEX_DIGIT}+
FLOAT_LITERAL   = {DIGIT_OR_UNDER}*\.{DIGIT_OR_UNDER}+([eE][+-]?{DIGIT_OR_UNDER}+)?|{DIGIT_OR_UNDER}+[eE][+-]?{DIGIT_OR_UNDER}+

// Строки
ESCAPE_SEQUENCE = \\[tnr\"\\'0]|\\x{HEX_DIGIT}{2}|\\u\{{HEX_DIGIT}{1,6}\}|\\u{HEX_DIGIT}{4}|\\U{HEX_DIGIT}{8}
CHAR_LITERAL    = \'({ESCAPE_SEQUENCE}|[^\'\\\r\n])\'

// Raw strings: r"..." или r#"..."#
RAW_STRING_START = r(\"|\#+\")

// Multi-line strings: """
MULTI_STRING_START = \"\"\"
MULTI_STRING_END   = \"\"\"

// Комментарии
LINE_COMMENT    = "//"[^\r\n]*
DOC_COMMENT_LINE= "///"[^\r\n]*
SHEBANG         = "#!"[^\r\n]*

%%

// === Начальное состояние ===
<YYINITIAL> {
    // Пробелы
    {WHITE_SPACE}       { return TokenType.WHITE_SPACE; }
    \r\n|\n|\r          { return TokenType.WHITE_SPACE; }

    // Shebang
    ^ {SHEBANG}         { return RhaiTypes.SHEBANG; }

    // Комментарии
    {DOC_COMMENT_LINE}  { return RhaiTypes.DOC_LINE; }
    {LINE_COMMENT}      { return RhaiTypes.LINE_COMMENT; }
    "/**"               { yybegin(IN_DOC_COMMENT); }
    "/*"                { yybegin(IN_BLOCK_COMMENT); }

    // Raw strings
    {RAW_STRING_START}  {
                         String text = yytext().toString();
                         rawStringHashes = text.indexOf('"') - 1;
                         if (rawStringHashes < 0) rawStringHashes = 0;
                         yybegin(IN_RAW_STRING);
                       }

    // Multi-line strings
    {MULTI_STRING_START} { yybegin(IN_MULTI_STRING); }

    // Обычные строки
    \"                  { yybegin(IN_STRING); }

    // Интерполированные строки
    "`"                 { yybegin(IN_INTERPOLATED_STRING); return RhaiTypes.INTERPOLATED_START; }

    // Char
    {CHAR_LITERAL}      { return RhaiTypes.CHAR_LITERAL; }

    // Числа
    {BINARY_LITERAL}    { return RhaiTypes.INTEGER_LITERAL; }
    {OCTAL_LITERAL}     { return RhaiTypes.INTEGER_LITERAL; }
    {HEX_LITERAL}       { return RhaiTypes.INTEGER_LITERAL; }
    {FLOAT_LITERAL}     { return RhaiTypes.FLOAT_LITERAL; }
    {INTEGER_LITERAL}   { return RhaiTypes.INTEGER_LITERAL; }

    // Ключевые слова
    "fn"                { return RhaiTypes.FN; }
    "let"               { return RhaiTypes.LET; }
    "const"             { return RhaiTypes.CONST; }
    "if"                { return RhaiTypes.IF; }
    "else"              { return RhaiTypes.ELSE; }
    "return"            { return RhaiTypes.RETURN; }
    "while"             { return RhaiTypes.WHILE; }
    "for"               { return RhaiTypes.FOR; }
    "in"                { return RhaiTypes.IN; }
    "do"                { return RhaiTypes.DO; }
    "loop"              { return RhaiTypes.LOOP; }
    "break"             { return RhaiTypes.BREAK; }
    "continue"          { return RhaiTypes.CONTINUE; }
    "switch"            { return RhaiTypes.SWITCH; }
    "default"           { return RhaiTypes.DEFAULT; }
    "true"              { return RhaiTypes.TRUE; }
    "false"             { return RhaiTypes.FALSE; }
    "null"              { return RhaiTypes.NULL; }
    "import"            { return RhaiTypes.IMPORT; }
    "export"            { return RhaiTypes.EXPORT; }
    "as"                { return RhaiTypes.AS; }
    "try"               { return RhaiTypes.TRY; }
    "catch"             { return RhaiTypes.CATCH; }
    "throw"             { return RhaiTypes.THROW; }
    "until"             { return RhaiTypes.UNTIL; }
    "private"           { return RhaiTypes.PRIVATE; }
    "pub"               { return RhaiTypes.PUB; }
    "module"            { return RhaiTypes.MODULE; }
    "this"              { return RhaiTypes.THIS; }
    "global"            { return RhaiTypes.GLOBAL; }
    "is"                { return RhaiTypes.IS; }
    "shared"            { return RhaiTypes.SHARED; }
    "sync"              { return RhaiTypes.SYNC; }
    "async"             { return RhaiTypes.ASYNC; }
    "await"             { return RhaiTypes.AWAIT; }
    "undefined"         { return RhaiTypes.UNDEF; }
    "inf"               { return RhaiTypes.INF; }
    "-inf"              { return RhaiTypes.NEG_INF; }
    "NaN"               { return RhaiTypes.NAN; }

    // Идентификаторы
    {IDENTIFIER}        { return RhaiTypes.IDENTIFIER; }

    // Операторы (от длинных к коротким)
    "**="               { return RhaiTypes.POW_ASSIGN; }
    "**"                { return RhaiTypes.POW; }
    "<<="               { return RhaiTypes.SHL_ASSIGN; }
    ">>="               { return RhaiTypes.SHR_ASSIGN; }
    "<<"                { return RhaiTypes.SHL; }
    ">>"                { return RhaiTypes.SHR; }
    "=="                { return RhaiTypes.EQ; }
    "!="                { return RhaiTypes.NE; }
    "<=>"               { return RhaiTypes.SPACESHIP; }
    "<="                { return RhaiTypes.LE; }
    ">="                { return RhaiTypes.GE; }
    "&&"                { return RhaiTypes.AND; }
    "||"                { return RhaiTypes.OR; }
    "??"                { return RhaiTypes.NULL_COALESCING; }
    "..="               { return RhaiTypes.DOT_DOT_EQ; }
    ".."                { return RhaiTypes.RANGE; }
    "=>"                { return RhaiTypes.ARROW; }
    "->"                { return RhaiTypes.THIN_ARROW; }
    "<-"                { return RhaiTypes.LEFT_ARROW; }
    "::"                { return RhaiTypes.DOUBLE_COLON; }
    "+="                { return RhaiTypes.PLUS_ASSIGN; }
    "-="                { return RhaiTypes.MINUS_ASSIGN; }
    "*="                { return RhaiTypes.MUL_ASSIGN; }
    "/="                { return RhaiTypes.DIV_ASSIGN; }
    "%="                { return RhaiTypes.MOD_ASSIGN; }
    "&="                { return RhaiTypes.AND_ASSIGN; }
    "|="                { return RhaiTypes.OR_ASSIGN; }
    "^="                { return RhaiTypes.XOR_ASSIGN; }

    // Одиночные символы
    "+"                 { return RhaiTypes.PLUS; }
    "-"                 { return RhaiTypes.MINUS; }
    "*"                 { return RhaiTypes.MUL; }
    "/"                 { return RhaiTypes.DIV; }
    "%"                 { return RhaiTypes.MOD; }
    "!"                 { return RhaiTypes.NOT; }
    "&"                 { return RhaiTypes.BAND; }
    "|"                 { return RhaiTypes.BOR; }
    "^"                 { return RhaiTypes.BXOR; }
    "="                 { return RhaiTypes.ASSIGN; }
    "<"                 { return RhaiTypes.LT; }
    ">"                 { return RhaiTypes.GT; }
    "."                 { return RhaiTypes.DOT; }
    ","                 { return RhaiTypes.COMMA; }
    ";"                 { return RhaiTypes.SEMICOLON; }
    ":"                 { return RhaiTypes.COLON; }
    "?"                 { return RhaiTypes.QUESTION; }
    "@"                 { return RhaiTypes.AT; }
    "$"                 { return RhaiTypes.DOLLAR; }
    "~"                 { return RhaiTypes.TILDE; }
    "#"                 { return RhaiTypes.HASH; }

    // Скобки
    "("                 { return RhaiTypes.LPAREN; }
    ")"                 { return RhaiTypes.RPAREN; }
    "{"                 { return RhaiTypes.LBRACE; }
    "}"                 { return RhaiTypes.RBRACE; }
    "["                 { return RhaiTypes.LBRACKET; }
    "]"                 { return RhaiTypes.RBRACKET; }

    // Ошибки
    [^]                 { return TokenType.BAD_CHARACTER; }
}

// === Обычные строки ===
<IN_STRING> {
    \"                  { yybegin(YYINITIAL); return RhaiTypes.STRING_LITERAL; }
    {ESCAPE_SEQUENCE}   {  }
    "\\\$"              {  }
    "\\`"               {  }
    \\\r\n              {  }
    \\\n                {  }
    \\\r                {  }
    \\[^]               { return TokenType.BAD_CHARACTER; }
    [^\"\\\r\n]+        {  }
    \r\n|\n|\r          { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
    <<EOF>>             { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
}

// === Raw strings ===
<IN_RAW_STRING> {
    \"                  {
                          if (rawStringHashes == 0) {
                              yybegin(YYINITIAL);
                              return RhaiTypes.RAW_STRING_LITERAL;
                          }
                        }
    \"\#+               {
                          String hashes = yytext().toString().substring(1);
                          if (hashes.length() == rawStringHashes) {
                              yybegin(YYINITIAL);
                              return RhaiTypes.RAW_STRING_LITERAL;
                          }
                        }
    [^\"]+              {  }
    \"                  {  }
    <<EOF>>             { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
}

// === Multi-line strings ===
<IN_MULTI_STRING> {
    {MULTI_STRING_END}  { yybegin(YYINITIAL); return RhaiTypes.MULTILINE_STRING_LITERAL; }
    [^\"]+              {  }
    \"                  {  }
    <<EOF>>             { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
}

// === Интерполированные строки ===
<IN_INTERPOLATED_STRING> {
    "`"                 { yybegin(YYINITIAL); return RhaiTypes.INTERPOLATED_END; }
    "${"                { yybegin(IN_INTERP_EXPR_0); return RhaiTypes.INTERPOLATED_EXPR_START; }
    {ESCAPE_SEQUENCE}   { return RhaiTypes.INTERPOLATED_TEXT; }
    "\\`"               { return RhaiTypes.INTERPOLATED_TEXT; }
    "\\$"               { return RhaiTypes.INTERPOLATED_TEXT; }
    "$"/[^\{]           { return RhaiTypes.INTERPOLATED_TEXT; }
    "$"                 { return RhaiTypes.INTERPOLATED_TEXT; }
    [^`\\\$\r\n]+       { return RhaiTypes.INTERPOLATED_TEXT; }
    \r\n|\n|\r          { return RhaiTypes.INTERPOLATED_TEXT; }
    \\[^`\$]            { return TokenType.BAD_CHARACTER; }
    \\                  { return TokenType.BAD_CHARACTER; }
    <<EOF>>             { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
}

// === Выражения внутри ${} - глубина 0 ===
<IN_INTERP_EXPR_0> {
    "}"                 { yybegin(IN_INTERPOLATED_STRING); return RhaiTypes.INTERPOLATED_EXPR_END; }
    "(" | "{" | "["     { yybegin(IN_INTERP_EXPR_1); return RhaiTypes.LPAREN; }

    // Всё остальное как в YYINITIAL
    {WHITE_SPACE}       { return TokenType.WHITE_SPACE; }
    \r\n|\n|\r          { return TokenType.WHITE_SPACE; }
    {LINE_COMMENT}      { return RhaiTypes.LINE_COMMENT; }
    \"                  { yybegin(IN_STRING); }
    "`"                 { yybegin(IN_INTERPOLATED_STRING); return RhaiTypes.INTERPOLATED_START; }
    {CHAR_LITERAL}      { return RhaiTypes.CHAR_LITERAL; }
    {BINARY_LITERAL}    { return RhaiTypes.INTEGER_LITERAL; }
    {OCTAL_LITERAL}     { return RhaiTypes.INTEGER_LITERAL; }
    {HEX_LITERAL}       { return RhaiTypes.INTEGER_LITERAL; }
    {FLOAT_LITERAL}     { return RhaiTypes.FLOAT_LITERAL; }
    {INTEGER_LITERAL}   { return RhaiTypes.INTEGER_LITERAL; }
    "true"              { return RhaiTypes.TRUE; }
    "false"             { return RhaiTypes.FALSE; }
    "null"              { return RhaiTypes.NULL; }
    "if"                { return RhaiTypes.IF; }
    "else"              { return RhaiTypes.ELSE; }
    {IDENTIFIER}        { return RhaiTypes.IDENTIFIER; }
    "+"                 { return RhaiTypes.PLUS; }
    "-"                 { return RhaiTypes.MINUS; }
    "*"                 { return RhaiTypes.MUL; }
    "/"                 { return RhaiTypes.DIV; }
    "%"                 { return RhaiTypes.MOD; }
    "!"                 { return RhaiTypes.NOT; }
    "&&"                { return RhaiTypes.AND; }
    "||"                { return RhaiTypes.OR; }
    "=="                { return RhaiTypes.EQ; }
    "!="                { return RhaiTypes.NE; }
    "<="                { return RhaiTypes.LE; }
    ">="                { return RhaiTypes.GE; }
    "<"                 { return RhaiTypes.LT; }
    ">"                 { return RhaiTypes.GT; }
    "."                 { return RhaiTypes.DOT; }
    ","                 { return RhaiTypes.COMMA; }
    ":"                 { return RhaiTypes.COLON; }
    "::"                { return RhaiTypes.DOUBLE_COLON; }
    "?"                 { return RhaiTypes.QUESTION; }
    "??"                { return RhaiTypes.NULL_COALESCING; }
    ".."                { return RhaiTypes.RANGE; }
    "=>"                { return RhaiTypes.ARROW; }
    [^]                 { return TokenType.BAD_CHARACTER; }
}

// === Выражения внутри ${} - глубина 1 ===
<IN_INTERP_EXPR_1> {
    ")" | "}" | "]"     { yybegin(IN_INTERP_EXPR_0); return RhaiTypes.RPAREN; }
    "(" | "{" | "["     { yybegin(IN_INTERP_EXPR_2); return RhaiTypes.LPAREN; }

    {WHITE_SPACE}       { return TokenType.WHITE_SPACE; }
    \r\n|\n|\r          { return TokenType.WHITE_SPACE; }
    {LINE_COMMENT}      { return RhaiTypes.LINE_COMMENT; }
    \"                  { yybegin(IN_STRING); }
    "`"                 { yybegin(IN_INTERPOLATED_STRING); return RhaiTypes.INTERPOLATED_START; }
    {CHAR_LITERAL}      { return RhaiTypes.CHAR_LITERAL; }
    {BINARY_LITERAL}    { return RhaiTypes.INTEGER_LITERAL; }
    {OCTAL_LITERAL}     { return RhaiTypes.INTEGER_LITERAL; }
    {HEX_LITERAL}       { return RhaiTypes.INTEGER_LITERAL; }
    {FLOAT_LITERAL}     { return RhaiTypes.FLOAT_LITERAL; }
    {INTEGER_LITERAL}   { return RhaiTypes.INTEGER_LITERAL; }
    "true"              { return RhaiTypes.TRUE; }
    "false"             { return RhaiTypes.FALSE; }
    "null"              { return RhaiTypes.NULL; }
    "if"                { return RhaiTypes.IF; }
    "else"              { return RhaiTypes.ELSE; }
    {IDENTIFIER}        { return RhaiTypes.IDENTIFIER; }
    "+"                 { return RhaiTypes.PLUS; }
    "-"                 { return RhaiTypes.MINUS; }
    "*"                 { return RhaiTypes.MUL; }
    "/"                 { return RhaiTypes.DIV; }
    "%"                 { return RhaiTypes.MOD; }
    "!"                 { return RhaiTypes.NOT; }
    "&&"                { return RhaiTypes.AND; }
    "||"                { return RhaiTypes.OR; }
    "=="                { return RhaiTypes.EQ; }
    "!="                { return RhaiTypes.NE; }
    "<="                { return RhaiTypes.LE; }
    ">="                { return RhaiTypes.GE; }
    "<"                 { return RhaiTypes.LT; }
    ">"                 { return RhaiTypes.GT; }
    "."                 { return RhaiTypes.DOT; }
    ","                 { return RhaiTypes.COMMA; }
    ":"                 { return RhaiTypes.COLON; }
    "::"                { return RhaiTypes.DOUBLE_COLON; }
    "?"                 { return RhaiTypes.QUESTION; }
    "??"                { return RhaiTypes.NULL_COALESCING; }
    ".."                { return RhaiTypes.RANGE; }
    "=>"                { return RhaiTypes.ARROW; }
    [^]                 { return TokenType.BAD_CHARACTER; }
}

// === Выражения внутри ${} - глубина 2 ===
<IN_INTERP_EXPR_2> {
    ")" | "}" | "]"     { yybegin(IN_INTERP_EXPR_1); return RhaiTypes.RPAREN; }
    "(" | "{" | "["     { yybegin(IN_INTERP_EXPR_3); return RhaiTypes.LPAREN; }

    {WHITE_SPACE}       { return TokenType.WHITE_SPACE; }
    \r\n|\n|\r          { return TokenType.WHITE_SPACE; }
    {LINE_COMMENT}      { return RhaiTypes.LINE_COMMENT; }
    \"                  { yybegin(IN_STRING); }
    "`"                 { yybegin(IN_INTERPOLATED_STRING); return RhaiTypes.INTERPOLATED_START; }
    {CHAR_LITERAL}      { return RhaiTypes.CHAR_LITERAL; }
    {BINARY_LITERAL}    { return RhaiTypes.INTEGER_LITERAL; }
    {OCTAL_LITERAL}     { return RhaiTypes.INTEGER_LITERAL; }
    {HEX_LITERAL}       { return RhaiTypes.INTEGER_LITERAL; }
    {FLOAT_LITERAL}     { return RhaiTypes.FLOAT_LITERAL; }
    {INTEGER_LITERAL}   { return RhaiTypes.INTEGER_LITERAL; }
    "true"              { return RhaiTypes.TRUE; }
    "false"             { return RhaiTypes.FALSE; }
    "null"              { return RhaiTypes.NULL; }
    "if"                { return RhaiTypes.IF; }
    "else"              { return RhaiTypes.ELSE; }
    {IDENTIFIER}        { return RhaiTypes.IDENTIFIER; }
    "+"                 { return RhaiTypes.PLUS; }
    "-"                 { return RhaiTypes.MINUS; }
    "*"                 { return RhaiTypes.MUL; }
    "/"                 { return RhaiTypes.DIV; }
    "%"                 { return RhaiTypes.MOD; }
    "!"                 { return RhaiTypes.NOT; }
    "&&"                { return RhaiTypes.AND; }
    "||"                { return RhaiTypes.OR; }
    "=="                { return RhaiTypes.EQ; }
    "!="                { return RhaiTypes.NE; }
    "<="                { return RhaiTypes.LE; }
    ">="                { return RhaiTypes.GE; }
    "<"                 { return RhaiTypes.LT; }
    ">"                 { return RhaiTypes.GT; }
    "."                 { return RhaiTypes.DOT; }
    ","                 { return RhaiTypes.COMMA; }
    ":"                 { return RhaiTypes.COLON; }
    "::"                { return RhaiTypes.DOUBLE_COLON; }
    "?"                 { return RhaiTypes.QUESTION; }
    "??"                { return RhaiTypes.NULL_COALESCING; }
    ".."                { return RhaiTypes.RANGE; }
    "=>"                { return RhaiTypes.ARROW; }
    [^]                 { return TokenType.BAD_CHARACTER; }
}

// === Выражения внутри ${} - глубина 3 ===
<IN_INTERP_EXPR_3> {
    ")" | "}" | "]"     { yybegin(IN_INTERP_EXPR_2); return RhaiTypes.RPAREN; }
    "(" | "{" | "["     { yybegin(IN_INTERP_EXPR_4); return RhaiTypes.LPAREN; }

    {WHITE_SPACE}       { return TokenType.WHITE_SPACE; }
    \r\n|\n|\r          { return TokenType.WHITE_SPACE; }
    {LINE_COMMENT}      { return RhaiTypes.LINE_COMMENT; }
    \"                  { yybegin(IN_STRING); }
    "`"                 { yybegin(IN_INTERPOLATED_STRING); return RhaiTypes.INTERPOLATED_START; }
    {CHAR_LITERAL}      { return RhaiTypes.CHAR_LITERAL; }
    {BINARY_LITERAL}    { return RhaiTypes.INTEGER_LITERAL; }
    {OCTAL_LITERAL}     { return RhaiTypes.INTEGER_LITERAL; }
    {HEX_LITERAL}       { return RhaiTypes.INTEGER_LITERAL; }
    {FLOAT_LITERAL}     { return RhaiTypes.FLOAT_LITERAL; }
    {INTEGER_LITERAL}   { return RhaiTypes.INTEGER_LITERAL; }
    "true"              { return RhaiTypes.TRUE; }
    "false"             { return RhaiTypes.FALSE; }
    "null"              { return RhaiTypes.NULL; }
    "if"                { return RhaiTypes.IF; }
    "else"              { return RhaiTypes.ELSE; }
    {IDENTIFIER}        { return RhaiTypes.IDENTIFIER; }
    "+"                 { return RhaiTypes.PLUS; }
    "-"                 { return RhaiTypes.MINUS; }
    "*"                 { return RhaiTypes.MUL; }
    "/"                 { return RhaiTypes.DIV; }
    "%"                 { return RhaiTypes.MOD; }
    "!"                 { return RhaiTypes.NOT; }
    "&&"                { return RhaiTypes.AND; }
    "||"                { return RhaiTypes.OR; }
    "=="                { return RhaiTypes.EQ; }
    "!="                { return RhaiTypes.NE; }
    "<="                { return RhaiTypes.LE; }
    ">="                { return RhaiTypes.GE; }
    "<"                 { return RhaiTypes.LT; }
    ">"                 { return RhaiTypes.GT; }
    "."                 { return RhaiTypes.DOT; }
    ","                 { return RhaiTypes.COMMA; }
    ":"                 { return RhaiTypes.COLON; }
    "::"                { return RhaiTypes.DOUBLE_COLON; }
    "?"                 { return RhaiTypes.QUESTION; }
    "??"                { return RhaiTypes.NULL_COALESCING; }
    ".."                { return RhaiTypes.RANGE; }
    "=>"                { return RhaiTypes.ARROW; }
    [^]                 { return TokenType.BAD_CHARACTER; }
}

// === Выражения внутри ${} - глубина 4 (максимум, дальше не увеличиваем) ===
<IN_INTERP_EXPR_4> {
    ")" | "}" | "]"     { yybegin(IN_INTERP_EXPR_3); return RhaiTypes.RPAREN; }
    "(" | "{" | "["     { return RhaiTypes.LPAREN; }

    {WHITE_SPACE}       { return TokenType.WHITE_SPACE; }
    \r\n|\n|\r          { return TokenType.WHITE_SPACE; }
    {LINE_COMMENT}      { return RhaiTypes.LINE_COMMENT; }
    \"                  { yybegin(IN_STRING); }
    "`"                 { yybegin(IN_INTERPOLATED_STRING); return RhaiTypes.INTERPOLATED_START; }
    {CHAR_LITERAL}      { return RhaiTypes.CHAR_LITERAL; }
    {BINARY_LITERAL}    { return RhaiTypes.INTEGER_LITERAL; }
    {OCTAL_LITERAL}     { return RhaiTypes.INTEGER_LITERAL; }
    {HEX_LITERAL}       { return RhaiTypes.INTEGER_LITERAL; }
    {FLOAT_LITERAL}     { return RhaiTypes.FLOAT_LITERAL; }
    {INTEGER_LITERAL}   { return RhaiTypes.INTEGER_LITERAL; }
    "true"              { return RhaiTypes.TRUE; }
    "false"             { return RhaiTypes.FALSE; }
    "null"              { return RhaiTypes.NULL; }
    "if"                { return RhaiTypes.IF; }
    "else"              { return RhaiTypes.ELSE; }
    {IDENTIFIER}        { return RhaiTypes.IDENTIFIER; }
    "+"                 { return RhaiTypes.PLUS; }
    "-"                 { return RhaiTypes.MINUS; }
    "*"                 { return RhaiTypes.MUL; }
    "/"                 { return RhaiTypes.DIV; }
    "%"                 { return RhaiTypes.MOD; }
    "!"                 { return RhaiTypes.NOT; }
    "&&"                { return RhaiTypes.AND; }
    "||"                { return RhaiTypes.OR; }
    "=="                { return RhaiTypes.EQ; }
    "!="                { return RhaiTypes.NE; }
    "<="                { return RhaiTypes.LE; }
    ">="                { return RhaiTypes.GE; }
    "<"                 { return RhaiTypes.LT; }
    ">"                 { return RhaiTypes.GT; }
    "."                 { return RhaiTypes.DOT; }
    ","                 { return RhaiTypes.COMMA; }
    ":"                 { return RhaiTypes.COLON; }
    "::"                { return RhaiTypes.DOUBLE_COLON; }
    "?"                 { return RhaiTypes.QUESTION; }
    "??"                { return RhaiTypes.NULL_COALESCING; }
    ".."                { return RhaiTypes.RANGE; }
    "=>"                { return RhaiTypes.ARROW; }
    [^]                 { return TokenType.BAD_CHARACTER; }
}

// === Блочные комментарии ===
<IN_BLOCK_COMMENT> {
    "*/"                { yybegin(YYINITIAL); return RhaiTypes.BLOCK_COMMENT; }
    [^*]+               {  }
    "*"                 {  }
    <<EOF>>             { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
}

// === Документационные комментарии ===
<IN_DOC_COMMENT> {
    "*/"                { yybegin(YYINITIAL); return RhaiTypes.DOC_COMMENT; }
    [^*]+               {  }
    "*"                 {  }
    <<EOF>>             { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
}
