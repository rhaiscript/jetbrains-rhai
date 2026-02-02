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
import org.rhai.lang.RhaiLanguage
import org.rhai.lexer.RhaiHighlightingLexer


class RhaiSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = RhaiFlexAdapter.INSTANCE

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        return when (tokenType) {
            // === Комментарии ===
            RhaiTypes.LINE_COMMENT, RhaiTypes.DOC_LINE ->
                LINE_COMMENT_KEYS

            RhaiTypes.BLOCK_COMMENT, RhaiTypes.DOC_COMMENT ->
                BLOCK_COMMENT_KEYS

            // === Строки и символы ===
            RhaiTypes.STRING_LITERAL,
            RhaiTypes.RAW_STRING_LITERAL,
            RhaiTypes.MULTILINE_STRING_LITERAL,
            RhaiTypes.CHAR_LITERAL,
            RhaiTypes.INTERPOLATED_START,
            RhaiTypes.INTERPOLATED_END,
            RhaiTypes.INTERPOLATED_TEXT ->
                STRING_KEYS

            // === Регулярные выражения (отдельный стиль) ===
            RhaiTypes.REGEX_LITERAL ->
                REGEX_KEYS

            // === Числовые литералы ===
            RhaiTypes.INTEGER_LITERAL,
            RhaiTypes.FLOAT_LITERAL,
            RhaiTypes.INF,
            RhaiTypes.NEG_INF,
            RhaiTypes.NAN ->
                NUMBER_KEYS

            // === Ключевые слова ===
            RhaiTypes.FN, RhaiTypes.LET, RhaiTypes.CONST,
            RhaiTypes.IF, RhaiTypes.ELSE,
            RhaiTypes.WHILE, RhaiTypes.FOR, RhaiTypes.IN,
            RhaiTypes.DO, RhaiTypes.LOOP,
            RhaiTypes.BREAK, RhaiTypes.CONTINUE,
            RhaiTypes.RETURN, RhaiTypes.THROW,
            RhaiTypes.SWITCH, RhaiTypes.DEFAULT,
            RhaiTypes.TRY, RhaiTypes.CATCH,
            RhaiTypes.IMPORT, RhaiTypes.EXPORT,
            RhaiTypes.AS, RhaiTypes.MODULE,
            RhaiTypes.PRIVATE, RhaiTypes.PUB,
            RhaiTypes.THIS, RhaiTypes.GLOBAL,
            RhaiTypes.IS, RhaiTypes.UNTIL,
            RhaiTypes.SHARED, RhaiTypes.SYNC,
            RhaiTypes.ASYNC, RhaiTypes.AWAIT,
            RhaiTypes.UNDEF ->
                KEYWORD_KEYS

            // === Булевы значения и null (как ключевые слова) ===
            RhaiTypes.TRUE, RhaiTypes.FALSE, RhaiTypes.NULL ->
                KEYWORD_KEYS

            // === Встроенные функции (type_of, print, и т.д.) ===
            RhaiTypes.BUILTIN ->
                BUILTIN_KEYS

            // === Операторы ===
            // Арифметические
            RhaiTypes.PLUS, RhaiTypes.MINUS, RhaiTypes.MUL,
            RhaiTypes.DIV, RhaiTypes.MOD, RhaiTypes.POW,
                // Сравнения
            RhaiTypes.EQ, RhaiTypes.NE, RhaiTypes.LT,
            RhaiTypes.GT, RhaiTypes.LE, RhaiTypes.GE,
            RhaiTypes.SPACESHIP,
                // Логические
            RhaiTypes.AND, RhaiTypes.OR, RhaiTypes.NOT,
                // Битовые
            RhaiTypes.BAND, RhaiTypes.BOR, RhaiTypes.BXOR,
            RhaiTypes.SHL, RhaiTypes.SHR,
                // Присваивания
            RhaiTypes.ASSIGN, RhaiTypes.PLUS_ASSIGN,
            RhaiTypes.MINUS_ASSIGN, RhaiTypes.MUL_ASSIGN,
            RhaiTypes.DIV_ASSIGN, RhaiTypes.MOD_ASSIGN,
            RhaiTypes.AND_ASSIGN, RhaiTypes.OR_ASSIGN,
            RhaiTypes.XOR_ASSIGN, RhaiTypes.SHL_ASSIGN,
            RhaiTypes.SHR_ASSIGN, RhaiTypes.POW_ASSIGN,
                // Специальные
            RhaiTypes.ARROW, RhaiTypes.THIN_ARROW,
            RhaiTypes.LEFT_ARROW, RhaiTypes.DOUBLE_COLON,
            RhaiTypes.RANGE, RhaiTypes.DOT_DOT_EQ,
            RhaiTypes.NULL_COALESCING,
                // Разделители/пунктуация как операторы
            RhaiTypes.DOT, RhaiTypes.COLON, RhaiTypes.QUESTION,
            RhaiTypes.AT, RhaiTypes.HASH, RhaiTypes.DOLLAR,
            RhaiTypes.TILDE ->
                OPERATOR_KEYS

            // === Скобки ===
            RhaiTypes.LPAREN, RhaiTypes.RPAREN ->
                PARENTHESIS_KEYS

            RhaiTypes.LBRACE, RhaiTypes.RBRACE ->
                BRACES_KEYS

            RhaiTypes.LBRACKET, RhaiTypes.RBRACKET ->
                BRACKETS_KEYS

            // === Разделители ===
            RhaiTypes.COMMA ->
                COMMA_KEYS

            RhaiTypes.SEMICOLON ->
                SEMICOLON_KEYS

            // === Shebang (как комментарий) ===
            RhaiTypes.SHEBANG ->
                LINE_COMMENT_KEYS

            // === Ошибочные символы ===
            TokenType.BAD_CHARACTER ->
                BAD_CHARACTER_KEYS

            GeneratedParserUtilBase.DUMMY_BLOCK -> FUNCTION_KEYS

            // === Идентификаторы и прочее - без подсветки ===
            else ->
                emptyArray()
        }
    }

    companion object {
        // --- Определения атрибутов ---

        val INSTANCE = RhaiSyntaxHighlighter()

        val LINE_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_LINE_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )

        val BLOCK_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_BLOCK_COMMENT",
            DefaultLanguageHighlighterColors.BLOCK_COMMENT
        )

        val STRING: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_STRING",
            DefaultLanguageHighlighterColors.STRING
        )

        val REGEX: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_REGEX",
            DefaultLanguageHighlighterColors.STRING  // Можно изменить на свой цвет
        )

        val NUMBER: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )

        val KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        val BUILTIN: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_BUILTIN",
            DefaultLanguageHighlighterColors.NUMBER
        )

        val OPERATOR: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_OPERATOR",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

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

        val COMMA: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_COMMA",
            DefaultLanguageHighlighterColors.COMMA
        )

        val SEMICOLON: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_SEMICOLON",
            DefaultLanguageHighlighterColors.SEMICOLON
        )

        val BAD_CHARACTER: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_BAD_CHARACTER",
            HighlighterColors.BAD_CHARACTER
        )

        val FUNCTION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_FUNCTION",
            DefaultLanguageHighlighterColors.STATIC_METHOD
        )

        // --- Массивы для возврата (оптимизация) ---
        private val LINE_COMMENT_KEYS = arrayOf(LINE_COMMENT)
        private val BLOCK_COMMENT_KEYS = arrayOf(BLOCK_COMMENT)
        private val STRING_KEYS = arrayOf(STRING)
        private val REGEX_KEYS = arrayOf(REGEX)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val BUILTIN_KEYS = arrayOf(BUILTIN)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val PARENTHESIS_KEYS = arrayOf(PARENTHESES)
        private val BRACES_KEYS = arrayOf(BRACES)
        private val BRACKETS_KEYS = arrayOf(BRACKETS)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val SEMICOLON_KEYS = arrayOf(SEMICOLON)
        private val BAD_CHARACTER_KEYS = arrayOf(BAD_CHARACTER)
        private val FUNCTION_KEYS = arrayOf(FUNCTION)
    }
}
