package org.rhai.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.rhai.RhaiTypes
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
        val IDENTIFIER = TextAttributesKey.createTextAttributesKey(
            "RHAI_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER
        )
        val OPERATOR = TextAttributesKey.createTextAttributesKey(
            "RHAI_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
        val BRACES = TextAttributesKey.createTextAttributesKey(
            "RHAI_BRACES", DefaultLanguageHighlighterColors.BRACES
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
    }

    override fun getHighlightingLexer(): Lexer = RhaiHighlightingLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            RhaiTypes.LINE_COMMENT, RhaiTypes.BLOCK_COMMENT -> arrayOf(COMMENT)
            RhaiTypes.STRING_LITERAL, RhaiTypes.STRING_START, RhaiTypes.STRING_END,
            RhaiTypes.STRING_CONTENT, RhaiTypes.STRING_ESCAPE, RhaiTypes.STRING_ESCAPE_NEWLINE -> arrayOf(STRING)
            RhaiTypes.NUMBER -> arrayOf(NUMBER)

            RhaiTypes.LET, RhaiTypes.IF, RhaiTypes.ELSE, RhaiTypes.WHILE,
            RhaiTypes.TRUE, RhaiTypes.FALSE -> arrayOf(KEYWORD)

            RhaiTypes.PLUS, RhaiTypes.MINUS, RhaiTypes.MUL,
            RhaiTypes.DIV, RhaiTypes.MOD, RhaiTypes.EQ,
            RhaiTypes.EQ, RhaiTypes.NE, RhaiTypes.LT,
            RhaiTypes.LE, RhaiTypes.GT, RhaiTypes.GE,
            RhaiTypes.AND, RhaiTypes.OR, RhaiTypes.NOT -> arrayOf(OPERATOR)

            RhaiTypes.LPAREN, RhaiTypes.RPAREN,
            RhaiTypes.LBRACE, RhaiTypes.RBRACE -> arrayOf(BRACES)

            RhaiTypes.IDENTIFIER, RhaiTypes.BUILTIN -> arrayOf(IDENTIFIER)
            TokenType.BAD_CHARACTER -> arrayOf(BAD_CHARACTER)
            else -> emptyArray()
        }
    }
}
