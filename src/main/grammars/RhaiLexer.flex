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

// Состояния
%state IN_STRING, IN_INTERPOLATED_STRING, IN_RAW_STRING, IN_MULTI_STRING
%xstate IN_BLOCK_COMMENT, IN_DOC_COMMENT

// Java-код
%{
    private final java.util.Stack<Integer> stateStack = new java.util.Stack<>();
    private final java.util.Stack<Integer> braceStack = new java.util.Stack<>();
    private int rawStringHashes = 0;

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
        braceStack.push(braceStack.isEmpty() ? 1 : braceStack.peek() + 1);
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

    private IElementType finishString() {
        popState();
        return RhaiTypes.STRING_LITERAL;
    }

    private IElementType finishRawString() {
        popState();
        return RhaiTypes.RAW_STRING_LITERAL;
    }

    private IElementType finishMultiString() {
        popState();
        return RhaiTypes.MULTILINE_STRING_LITERAL;
    }
%}

// === Макросы ===

// Пробельные символы
WHITE_SPACE     = [ \t\f]+

// Идентификаторы
IDENTIFIER      = [_a-zA-Z][_a-zA-Z0-9]*

// Встроенные функции Rhai
BUILTIN_FUNC    = (print|debug|dump|eval|call|curry|compose|time|timestamp|type_of|is_def|is_shared|is_sync|is_async|len|push|pop|shift|unshift|clear|truncate|remove|contains|keys|values|index_of|split|join|trim|trim_start|trim_end|to_lower|to_upper|replace|starts_with|ends_with|find|find_rev|pad_start|pad_end|repeat|parse_int|parse_float|to_int|to_float|to_string|to_char|to_bool|sqrt|abs|sin|cos|tan|asin|acos|atan|atan2|floor|ceil|round|pow|exp|ln|log|log10|random|rand|seed)

// Числа
DIGIT           = [0-9]
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

// Regex: /pattern/ или /pattern/flags
REGEX_LITERAL   = \/([^\/\\\r\n]|\\.)+\/[a-z]*

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

    // Комментарии (все возвращают свои типы)
    {DOC_COMMENT_LINE}  { return RhaiTypes.DOC_LINE; }
    {LINE_COMMENT}      { return RhaiTypes.LINE_COMMENT; }
    "/*"                { pushState(IN_BLOCK_COMMENT); }
    "/**"               { pushState(IN_DOC_COMMENT); }

    // Raw strings
    {RAW_STRING_START}  {
                         String text = yytext().toString();
                         rawStringHashes = text.indexOf('"') - 1;
                         if (rawStringHashes < 0) rawStringHashes = 0;
                         pushState(IN_RAW_STRING);
                         return RhaiTypes.RAW_STRING_LITERAL;
                       }

    // Multi-line strings
    {MULTI_STRING_START} { pushState(IN_MULTI_STRING); return RhaiTypes.MULTILINE_STRING_LITERAL; }

    // Обычные строки
    \"                  { pushState(IN_STRING); return RhaiTypes.STRING_LITERAL; }

    // Интерполированные строки
    "`"                 { pushState(IN_INTERPOLATED_STRING); return RhaiTypes.INTERPOLATED_START; }

    // Char
    {CHAR_LITERAL}      { return RhaiTypes.CHAR_LITERAL; }

    // Regex
    {REGEX_LITERAL}     { return RhaiTypes.REGEX_LITERAL; }

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
    {BUILTIN_FUNC}      { return RhaiTypes.BUILTIN; }
    {IDENTIFIER}        { return RhaiTypes.IDENTIFIER; }

    // Операторы (от длинных к коротким)
    "**="               { return RhaiTypes.POW_ASSIGN; }
    "**"                { return RhaiTypes.POW; }
    "<<"                { return RhaiTypes.SHL; }
    ">>"                { return RhaiTypes.SHR; }
    "<<="               { return RhaiTypes.SHL_ASSIGN; }
    ">>="               { return RhaiTypes.SHR_ASSIGN; }
    "=="                { return RhaiTypes.EQ; }
    "!="                { return RhaiTypes.NE; }
    "<="                { return RhaiTypes.LE; }
    ">="                { return RhaiTypes.GE; }
    "<=>"               { return RhaiTypes.SPACESHIP; }
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

    // Скобки с учетом интерполяций
    "("                 { if (inInterpolation()) pushBrace(); return RhaiTypes.LPAREN; }
    ")"                 { if (inInterpolation()) popBrace(); return RhaiTypes.RPAREN; }
    "{"                 { if (inInterpolation()) pushBrace(); return RhaiTypes.LBRACE; }
    "}"                 {
                          if (inInterpolation()) {
                              popBrace();
                              if (!inInterpolation()) {
                                  yybegin(IN_INTERPOLATED_STRING);
                                  return RhaiTypes.INTERPOLATED_EXPR_END;
                              }
                          }
                          return RhaiTypes.RBRACE;
                        }
    "["                 { if (inInterpolation()) pushBrace(); return RhaiTypes.LBRACKET; }
    "]"                 { if (inInterpolation()) popBrace(); return RhaiTypes.RBRACKET; }

    // Ошибки
    [^]                 { return TokenType.BAD_CHARACTER; }
}

// === Обычные строки ===
<IN_STRING> {
    \"                  { return finishString(); }
    {ESCAPE_SEQUENCE}   {  }
    \\\r\n              {  }
    \\\n                {  }
    \\\r                {  }
    \\[^]               { return TokenType.BAD_CHARACTER; }
    [^\"\\\r\n]+        {  }
    \r\n|\n|\r          { popState(); return TokenType.BAD_CHARACTER; }
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}

// === Raw strings ===
<IN_RAW_STRING> {
    \"                  {
                          if (rawStringHashes == 0) {
                              return finishRawString();
                          }
                        }
    \"\#+               {
                          String hashes = yytext().toString().substring(1);
                          if (hashes.length() == rawStringHashes) {
                              return finishRawString();
                          }
                        }
    [^\"]+              {  }
    \"                  {  }
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}

// === Multi-line strings ===
<IN_MULTI_STRING> {
    {MULTI_STRING_END}  { return finishMultiString(); }
    [^\"]+              {  }
    \"                  {  }
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}

// === Интерполированные строки ===
<IN_INTERPOLATED_STRING> {
    "`"                 { popState(); return RhaiTypes.INTERPOLATED_END; }
    "${"                {
                          pushState(YYINITIAL);
                          braceStack.push(1);
                          return RhaiTypes.INTERPOLATED_EXPR_START;
                        }
    {ESCAPE_SEQUENCE}   { return RhaiTypes.INTERPOLATED_TEXT; }
    "$"/[^\{]           { return RhaiTypes.INTERPOLATED_TEXT; }
    [^`\\\$\r\n]+       { return RhaiTypes.INTERPOLATED_TEXT; }
    \r\n|\n|\r          { return RhaiTypes.INTERPOLATED_TEXT; }
    \\[^]               { return TokenType.BAD_CHARACTER; }
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}

// === Блочные комментарии ===
<IN_BLOCK_COMMENT> {
    "*/"                { popState(); return RhaiTypes.BLOCK_COMMENT; }
    [^*]+               {  }
    "*"                 {  }
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}

// === Документационные комментарии ===
<IN_DOC_COMMENT> {
    "*/"                { popState(); return RhaiTypes.DOC_COMMENT; }
    [^*]+               {  }
    "*"                 {  }
    <<EOF>>             { popState(); return TokenType.BAD_CHARACTER; }
}