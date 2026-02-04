package org.rhai.highlighting

import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.rhai.RhaiFlexAdapter
import org.rhai.RhaiTypes


class RhaiSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = RhaiFlexAdapter.createLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        return when (tokenType) {
            // Comments
            RhaiTypes.LINE_COMMENT -> LINE_COMMENT_KEYS
            RhaiTypes.DOC_LINE, RhaiTypes.DOC_COMMENT -> DOC_COMMENT_KEYS
            RhaiTypes.BLOCK_COMMENT -> BLOCK_COMMENT_KEYS

            // Strings and characters
            RhaiTypes.STRING_LITERAL, RhaiTypes.MULTILINE_STRING_LITERAL -> STRING_KEYS
            RhaiTypes.RAW_STRING_LITERAL -> RAW_STRING_KEYS
            RhaiTypes.CHAR_LITERAL -> CHAR_KEYS
            RhaiTypes.INTERPOLATED_TEXT -> INTERPOLATION_KEYS
            RhaiTypes.INTERPOLATED_START, RhaiTypes.INTERPOLATED_END -> INTERPOLATION_KEYS
            RhaiTypes.INTERPOLATED_EXPR_START, RhaiTypes.INTERPOLATED_EXPR_END -> INTERPOLATION_MARKER_KEYS

            // Regex literals
            RhaiTypes.REGEX_LITERAL -> REGEX_KEYS

            // Numeric literals
            RhaiTypes.INTEGER_LITERAL,
            RhaiTypes.FLOAT_LITERAL,
            RhaiTypes.INF,
            RhaiTypes.NEG_INF,
            RhaiTypes.NAN -> NUMBER_KEYS

            // Control flow keywords
            RhaiTypes.IF, RhaiTypes.ELSE,
            RhaiTypes.WHILE, RhaiTypes.FOR,
            RhaiTypes.DO, RhaiTypes.LOOP,
            RhaiTypes.BREAK, RhaiTypes.CONTINUE,
            RhaiTypes.RETURN, RhaiTypes.THROW,
            RhaiTypes.SWITCH, RhaiTypes.DEFAULT,
            RhaiTypes.TRY, RhaiTypes.CATCH,
            RhaiTypes.UNTIL -> CONTROL_KEYWORD_KEYS

            // Other keywords
            RhaiTypes.FN, RhaiTypes.LET, RhaiTypes.CONST,
            RhaiTypes.IN, RhaiTypes.IMPORT, RhaiTypes.EXPORT,
            RhaiTypes.AS, RhaiTypes.MODULE,
            RhaiTypes.PRIVATE, RhaiTypes.PUB,
            RhaiTypes.THIS, RhaiTypes.GLOBAL,
            RhaiTypes.IS, RhaiTypes.SHARED, RhaiTypes.SYNC,
            RhaiTypes.ASYNC, RhaiTypes.AWAIT,
            RhaiTypes.UNDEF -> KEYWORD_KEYS

            // Boolean values and null
            RhaiTypes.TRUE, RhaiTypes.FALSE, RhaiTypes.NULL -> KEYWORD_KEYS

            // Arrows
            RhaiTypes.ARROW, RhaiTypes.THIN_ARROW, RhaiTypes.LEFT_ARROW -> ARROW_KEYS

            // Ranges
            RhaiTypes.RANGE, RhaiTypes.DOT_DOT_EQ -> RANGE_KEYS

            // Operators
            RhaiTypes.PLUS, RhaiTypes.MINUS, RhaiTypes.MUL,
            RhaiTypes.DIV, RhaiTypes.MOD, RhaiTypes.POW,
            RhaiTypes.EQ, RhaiTypes.NE, RhaiTypes.LT,
            RhaiTypes.GT, RhaiTypes.LE, RhaiTypes.GE,
            RhaiTypes.SPACESHIP,
            RhaiTypes.AND, RhaiTypes.OR, RhaiTypes.NOT,
            RhaiTypes.BAND, RhaiTypes.BOR, RhaiTypes.BXOR,
            RhaiTypes.SHL, RhaiTypes.SHR,
            RhaiTypes.ASSIGN, RhaiTypes.PLUS_ASSIGN,
            RhaiTypes.MINUS_ASSIGN, RhaiTypes.MUL_ASSIGN,
            RhaiTypes.DIV_ASSIGN, RhaiTypes.MOD_ASSIGN,
            RhaiTypes.AND_ASSIGN, RhaiTypes.OR_ASSIGN,
            RhaiTypes.XOR_ASSIGN, RhaiTypes.SHL_ASSIGN,
            RhaiTypes.SHR_ASSIGN, RhaiTypes.POW_ASSIGN,
            RhaiTypes.DOUBLE_COLON, RhaiTypes.NULL_COALESCING,
            RhaiTypes.QUESTION, RhaiTypes.AT, RhaiTypes.HASH,
            RhaiTypes.DOLLAR, RhaiTypes.TILDE -> OPERATOR_KEYS

            // Dot
            RhaiTypes.DOT -> DOT_KEYS

            // Colon
            RhaiTypes.COLON -> COLON_KEYS

            // Parentheses
            RhaiTypes.LPAREN, RhaiTypes.RPAREN -> PARENTHESIS_KEYS

            // Braces
            RhaiTypes.LBRACE, RhaiTypes.RBRACE -> BRACES_KEYS

            // Brackets
            RhaiTypes.LBRACKET, RhaiTypes.RBRACKET -> BRACKETS_KEYS

            // Delimiters
            RhaiTypes.COMMA -> COMMA_KEYS
            RhaiTypes.SEMICOLON -> SEMICOLON_KEYS

            // Shebang (treated as comment)
            RhaiTypes.SHEBANG -> LINE_COMMENT_KEYS

            // Bad characters
            TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS

            GeneratedParserUtilBase.DUMMY_BLOCK -> FUNCTION_KEYS

            // Identifiers and other tokens - no highlighting
            else -> emptyArray()
        }
    }

    companion object {
        val INSTANCE = RhaiSyntaxHighlighter()

        // Comments
        val LINE_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_LINE_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )

        val BLOCK_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_BLOCK_COMMENT",
            DefaultLanguageHighlighterColors.BLOCK_COMMENT
        )

        val DOC_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_DOC_COMMENT",
            DefaultLanguageHighlighterColors.DOC_COMMENT
        )

        // Strings
        val STRING: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_STRING",
            DefaultLanguageHighlighterColors.STRING
        )

        val RAW_STRING: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_RAW_STRING",
            DefaultLanguageHighlighterColors.STRING
        )

        val CHAR: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_CHAR",
            DefaultLanguageHighlighterColors.STRING
        )

        val INTERPOLATION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_INTERPOLATION",
            DefaultLanguageHighlighterColors.STRING
        )

        val INTERPOLATION_MARKER: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_INTERPOLATION_MARKER",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        val REGEX: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_REGEX",
            DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
        )

        // Numbers
        val NUMBER: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )

        // Keywords
        val KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        val CONTROL_KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_CONTROL_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        // Built-ins
        val BUILTIN: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_BUILTIN",
            DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL
        )

        // Operators
        val OPERATOR: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_OPERATOR",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        val ARROW: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_ARROW",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        val RANGE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_RANGE",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        // Brackets
        val PARENTHESES: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_PARENTHESES",
            DefaultLanguageHighlighterColors.PARENTHESES
        )

        val BRACES: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_BRACES",
            DefaultLanguageHighlighterColors.BRACES
        )

        val BRACKETS: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_BRACKETS",
            DefaultLanguageHighlighterColors.BRACKETS
        )

        // Punctuation
        val COMMA: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_COMMA",
            DefaultLanguageHighlighterColors.COMMA
        )

        val SEMICOLON: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_SEMICOLON",
            DefaultLanguageHighlighterColors.SEMICOLON
        )

        val DOT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_DOT",
            DefaultLanguageHighlighterColors.DOT
        )

        val COLON: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_COLON",
            DefaultLanguageHighlighterColors.SEMICOLON
        )

        // Special
        val IDENTIFIER: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_IDENTIFIER",
            DefaultLanguageHighlighterColors.IDENTIFIER
        )

        val BAD_CHARACTER: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_BAD_CHARACTER",
            HighlighterColors.BAD_CHARACTER
        )

        val FUNCTION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_FUNCTION",
            DefaultLanguageHighlighterColors.STATIC_METHOD
        )

        private val LINE_COMMENT_KEYS = arrayOf(LINE_COMMENT)
        private val BLOCK_COMMENT_KEYS = arrayOf(BLOCK_COMMENT)
        private val DOC_COMMENT_KEYS = arrayOf(DOC_COMMENT)
        private val STRING_KEYS = arrayOf(STRING)
        private val RAW_STRING_KEYS = arrayOf(RAW_STRING)
        private val CHAR_KEYS = arrayOf(CHAR)
        private val INTERPOLATION_KEYS = arrayOf(INTERPOLATION)
        private val INTERPOLATION_MARKER_KEYS = arrayOf(INTERPOLATION_MARKER)
        private val REGEX_KEYS = arrayOf(REGEX)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val CONTROL_KEYWORD_KEYS = arrayOf(CONTROL_KEYWORD)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val ARROW_KEYS = arrayOf(ARROW)
        private val RANGE_KEYS = arrayOf(RANGE)
        private val PARENTHESIS_KEYS = arrayOf(PARENTHESES)
        private val BRACES_KEYS = arrayOf(BRACES)
        private val BRACKETS_KEYS = arrayOf(BRACKETS)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val SEMICOLON_KEYS = arrayOf(SEMICOLON)
        private val DOT_KEYS = arrayOf(DOT)
        private val COLON_KEYS = arrayOf(COLON)
        private val BAD_CHARACTER_KEYS = arrayOf(BAD_CHARACTER)
        private val FUNCTION_KEYS = arrayOf(FUNCTION)
    }
}
