package org.rhai.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.rhai.RhaiTypes
import org.rhai.lang.RhaiLanguage
import org.rhai.lexer.RhaiHighlightingLexer

class RhaiSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val KEYWORD = TextAttributesKey.createTextAttributesKey(
            "RHAI_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD
        )
        val STRING = TextAttributesKey.createTextAttributesKey(
            "RHAI_STRING", DefaultLanguageHighlighterColors.STRING
        )
        val NUMBER = TextAttributesKey.createTextAttributesKey(
            "RHAI_NUMBER", DefaultLanguageHighlighterColors.NUMBER
        )
        val COMMENT = TextAttributesKey.createTextAttributesKey(
            "RHAI_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT
        )
        val DOC_COMMENT = TextAttributesKey.createTextAttributesKey(
            "RHAI_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT
        )
        val IDENTIFIER = TextAttributesKey.createTextAttributesKey(
            "RHAI_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER
        )
        val OPERATOR = TextAttributesKey.createTextAttributesKey(
            "RHAI_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
        val BRACES = TextAttributesKey.createTextAttributesKey(
            "RHAI_BRACES", DefaultLanguageHighlighterColors.BRACES
        )
        val BRACKETS = TextAttributesKey.createTextAttributesKey(
            "RHAI_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS
        )
        val PARENTHESES = TextAttributesKey.createTextAttributesKey(
            "RHAI_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES
        )
        val BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
            "RHAI_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER
        )
        val FUNCTION_DECLARATION = TextAttributesKey.createTextAttributesKey(
            "RHAI_FUNCTION_DECLARATION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
        )
        val FUNCTION_CALL = TextAttributesKey.createTextAttributesKey(
            "RHAI_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL
        )
        val TYPE = TextAttributesKey.createTextAttributesKey(
            "RHAI_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME
        )
        val CONSTANT = TextAttributesKey.createTextAttributesKey(
            "RHAI_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT
        )
        val ATTRIBUTE = TextAttributesKey.createTextAttributesKey(
            "RHAI_ATTRIBUTE", DefaultLanguageHighlighterColors.METADATA
        )
        val INTERPOLATED_STRING = TextAttributesKey.createTextAttributesKey(
            "RHAI_INTERPOLATED_STRING", DefaultLanguageHighlighterColors.STRING
        )
        val REGEX = TextAttributesKey.createTextAttributesKey(
            "RHAI_REGEX", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
        )
        val CHAR = TextAttributesKey.createTextAttributesKey(
            "RHAI_CHAR", DefaultLanguageHighlighterColors.STRING
        )
        val BUILTIN_FUNCTION = TextAttributesKey.createTextAttributesKey(
            "RHAI_BUILTIN_FUNCTION", DefaultLanguageHighlighterColors.STATIC_METHOD
        )
//        val MACRO = TextAttributesKey.createTextAttributesKey(
//            "RHAI_MACRO", DefaultLanguageHighlighterColors.MACRO
//        )
    }

    override fun getHighlightingLexer(): Lexer = RhaiHighlightingLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            // Комментарии
            RhaiTypes.LINE_COMMENT, RhaiTypes.BLOCK_COMMENT, RhaiTypes.DOC_COMMENT -> arrayOf(COMMENT)
            RhaiTypes.DOC_COMMENT, RhaiTypes.DOC_LINE -> arrayOf(DOC_COMMENT)

            // Строки и символы
            RhaiTypes.STRING_EXPR, RhaiTypes.STRING_LITERAL, RhaiTypes.RAW_STRING_LITERAL, RhaiTypes.MULTILINE_STRING_LITERAL, RhaiTypes.INTERPOLATED_STRING -> arrayOf(
                STRING
            )

            RhaiTypes.CHAR_LITERAL -> arrayOf(CHAR)
            RhaiTypes.REGEX_LITERAL -> arrayOf(REGEX)

            // Интерполированные строки
//            RhaiTypes.INTERPOLATED_START, RhaiTypes.INTERPOLATED_END,
//            RhaiTypes.INTERPOLATED_TEXT -> arrayOf(INTERPOLATED_STRING)

            RhaiTypes.INTERPOLATED_EXPR_START, RhaiTypes.INTERPOLATED_EXPR_END -> arrayOf(OPERATOR)

            // Числа
//            RhaiTypes.INTEGER, RhaiTypes.FLOAT -> arrayOf(NUMBER)

            // Ключевые слова
            RhaiTypes.FN, RhaiTypes.LET, RhaiTypes.CONST, RhaiTypes.IF, RhaiTypes.ELSE,
            RhaiTypes.SWITCH, RhaiTypes.DEFAULT, RhaiTypes.RETURN, RhaiTypes.THROW,
            RhaiTypes.TRY, RhaiTypes.CATCH, RhaiTypes.FOR, RhaiTypes.IN, RhaiTypes.LOOP,
            RhaiTypes.DO, RhaiTypes.WHILE, RhaiTypes.UNTIL, RhaiTypes.BREAK, RhaiTypes.CONTINUE,
            RhaiTypes.IMPORT, RhaiTypes.EXPORT, RhaiTypes.MODULE, RhaiTypes.THIS,
            RhaiTypes.GLOBAL, RhaiTypes.PRIVATE, RhaiTypes.PUB, RhaiTypes.AS, RhaiTypes.IS -> arrayOf(KEYWORD)

            // Булевы литералы
            RhaiTypes.TRUE, RhaiTypes.FALSE -> arrayOf(CONSTANT)

            // Null и undefined
            RhaiTypes.NULL, RhaiTypes.UNDEF -> arrayOf(CONSTANT)

            // Операторы
            RhaiTypes.PLUS, RhaiTypes.MINUS, RhaiTypes.MUL, RhaiTypes.DIV, RhaiTypes.MOD,
            RhaiTypes.ASSIGN, RhaiTypes.NOT, RhaiTypes.BAND, RhaiTypes.BOR, RhaiTypes.BXOR,
            RhaiTypes.LT, RhaiTypes.GT, RhaiTypes.DOT, RhaiTypes.COLON, RhaiTypes.SEMICOLON,
            RhaiTypes.COMMA, RhaiTypes.QUESTION, RhaiTypes.AT,
            RhaiTypes.ARROW, RhaiTypes.DOUBLE_COLON, RhaiTypes.POW, RhaiTypes.RANGE,
            RhaiTypes.NULL_COALESCING, RhaiTypes.SHL_ASSIGN, RhaiTypes.SHR_ASSIGN,
            RhaiTypes.SHL, RhaiTypes.SHR, RhaiTypes.LE, RhaiTypes.GE, RhaiTypes.EQ,
            RhaiTypes.NE, RhaiTypes.PLUS_ASSIGN, RhaiTypes.MINUS_ASSIGN,
            RhaiTypes.MUL_ASSIGN, RhaiTypes.DIV_ASSIGN, RhaiTypes.MOD_ASSIGN,
            RhaiTypes.AND_ASSIGN, RhaiTypes.OR_ASSIGN, RhaiTypes.XOR_ASSIGN,
            RhaiTypes.AND, RhaiTypes.OR -> arrayOf(OPERATOR)

            // Скобки
            RhaiTypes.LPAREN, RhaiTypes.RPAREN -> arrayOf(PARENTHESES)
            RhaiTypes.LBRACE, RhaiTypes.RBRACE -> arrayOf(BRACES)
            RhaiTypes.LBRACKET, RhaiTypes.RBRACKET -> arrayOf(BRACKETS)

            // Атрибуты
            RhaiTypes.HASH -> arrayOf(ATTRIBUTE)

            // Макросы
//            RhaiTypes.NOT -> {
//                // Проверяем контекст: если это макрос (например, println! или vec!)
//                // нужно определить в контексте парсера, но для простоты
//                // будем считать все "!" как часть макроса
//                arrayOf(MACRO)
//            }

            // Идентификаторы и встроенные функции
            RhaiTypes.IDENTIFIER -> {
                // Здесь можно добавить логику для определения типа идентификатора
                // (функция, переменная, тип и т.д.)
                arrayOf(IDENTIFIER)
            }

            RhaiTypes.BUILTIN -> arrayOf(BUILTIN_FUNCTION)

            // Типы
            in typeKeywords -> arrayOf(TYPE)

            // Shebang
//            RhaiTypes.SHEBANG_TOKEN -> arrayOf(COMMENT)

            // Ошибки
//            RhaiTypes.INVALID_OP, RhaiTypes.INVALID_ESCAPE -> arrayOf(BAD_CHARACTER)
            TokenType.BAD_CHARACTER -> arrayOf(BAD_CHARACTER)

            else -> emptyArray()
        }
    }

    // Вспомогательный набор ключевых слов типов
    private val typeKeywords = setOf(
        "int", "float", "bool", "string", "Array", "Map", "Fn"
    ).map { IElementType(it, RhaiLanguage.INSTANCE) }.toSet()
}
