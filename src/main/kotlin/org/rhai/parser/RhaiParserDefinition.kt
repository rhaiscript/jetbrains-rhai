package org.rhai.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.rhai.RhaiTypes
import org.rhai.RhaiParser
import org.rhai.lang.RhaiLanguage
import org.rhai.lang.RhaiFile
import org.rhai.RhaiFlexAdapter

class RhaiParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(RhaiLanguage.INSTANCE)
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)

        // Комментарии
        val COMMENTS = TokenSet.create(
//            RhaiTypes.LINE_COMMENT,
//            RhaiTypes.BLOCK_COMMENT,
//            RhaiTypes.DOC_COMMENT,
//            RhaiTypes.DOC_LINE
        )

        // Строковые литералы
        val STRING_LITERALS = TokenSet.create(
//            RhaiTypes.STRING,
//            RhaiTypes.RAW_STRING,
//            RhaiTypes.MULTILINE_STRING,
            RhaiTypes.CHAR_LITERAL,
            RhaiTypes.REGEX_LITERAL,
//            RhaiTypes.INTERPOLATED_START,
//            RhaiTypes.INTERPOLATED_END,
//            RhaiTypes.INTERPOLATED_TEXT
        )

        // Числовые литералы
        val NUMBER_LITERALS = TokenSet.create(
//            RhaiTypes.INTEGER,
//            RhaiTypes.FLOAT
        )

        // Ключевые слова
        val KEYWORDS = TokenSet.create(
            RhaiTypes.FN,
            RhaiTypes.LET,
            RhaiTypes.CONST,
            RhaiTypes.IF,
            RhaiTypes.ELSE,
            RhaiTypes.SWITCH,
            RhaiTypes.DEFAULT,
            RhaiTypes.RETURN,
            RhaiTypes.THROW,
            RhaiTypes.TRY,
            RhaiTypes.CATCH,
            RhaiTypes.FOR,
            RhaiTypes.IN,
            RhaiTypes.IS,
            RhaiTypes.AS,
            RhaiTypes.LOOP,
            RhaiTypes.DO,
            RhaiTypes.WHILE,
            RhaiTypes.UNTIL,
            RhaiTypes.BREAK,
            RhaiTypes.CONTINUE,
            RhaiTypes.IMPORT,
            RhaiTypes.EXPORT,
            RhaiTypes.MODULE,
            RhaiTypes.THIS,
            RhaiTypes.GLOBAL,
            RhaiTypes.PRIVATE,
            RhaiTypes.PUB
        )

        // Литералы констант
        val CONSTANT_LITERALS = TokenSet.create(
            RhaiTypes.TRUE,
            RhaiTypes.FALSE,
            RhaiTypes.NULL,
            RhaiTypes.UNDEF
        )

        // Операторы
        val OPERATORS = TokenSet.create(
            RhaiTypes.PLUS,
            RhaiTypes.MINUS,
            RhaiTypes.MUL,
            RhaiTypes.DIV,
            RhaiTypes.MOD,
            RhaiTypes.ASSIGN,
            RhaiTypes.NOT,
            RhaiTypes.BAND,
            RhaiTypes.BOR,
            RhaiTypes.BXOR,
            RhaiTypes.LT,
            RhaiTypes.GT,
            RhaiTypes.DOT,
            RhaiTypes.COLON,
            RhaiTypes.SEMICOLON,
            RhaiTypes.COMMA,
            RhaiTypes.QUESTION,
            RhaiTypes.AT,
            RhaiTypes.ARROW,
            RhaiTypes.DOUBLE_COLON,
            RhaiTypes.POW,
            RhaiTypes.RANGE,
            RhaiTypes.NULL_COALESCING,
            RhaiTypes.SHL_ASSIGN,
            RhaiTypes.SHR_ASSIGN,
            RhaiTypes.SHL,
            RhaiTypes.SHR,
            RhaiTypes.LE,
            RhaiTypes.GE,
            RhaiTypes.EQ,
            RhaiTypes.NE,
            RhaiTypes.PLUS_ASSIGN,
            RhaiTypes.MINUS_ASSIGN,
            RhaiTypes.MUL_ASSIGN,
            RhaiTypes.DIV_ASSIGN,
            RhaiTypes.MOD_ASSIGN,
            RhaiTypes.AND_ASSIGN,
            RhaiTypes.OR_ASSIGN,
            RhaiTypes.XOR_ASSIGN,
            RhaiTypes.AND,
            RhaiTypes.OR
        )

        // Скобки
        val BRACES = TokenSet.create(
            RhaiTypes.LBRACE,
            RhaiTypes.RBRACE
        )

        val BRACKETS = TokenSet.create(
            RhaiTypes.LBRACKET,
            RhaiTypes.RBRACKET
        )

        val PARENTHESES = TokenSet.create(
            RhaiTypes.LPAREN,
            RhaiTypes.RPAREN
        )

        // Идентификаторы
        val IDENTIFIERS = TokenSet.create(
            RhaiTypes.IDENTIFIER,
            RhaiTypes.BUILTIN
        )

        // Прочие токены
//        val OTHER_TOKENS = TokenSet.create(
//            RhaiTypes.HASH,
//            RhaiTypes.SHEBANG_TOKEN,
//            RhaiTypes.INTERPOLATED_EXPR_START,
//            RhaiTypes.INTERPOLATED_EXPR_END
//        )
//
//        // Ошибочные токены
//        val ERROR_TOKENS = TokenSet.create(
//            RhaiTypes.INVALID_OP,
//            RhaiTypes.INVALID_ESCAPE
//        )
    }

    override fun createLexer(project: Project): Lexer = RhaiFlexAdapter()

    override fun createParser(project: Project): PsiParser = RhaiParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getWhitespaceTokens(): TokenSet = WHITE_SPACES

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRING_LITERALS

    override fun createElement(node: ASTNode): PsiElement {
        return RhaiTypes.Factory.createElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return RhaiFile(viewProvider)
    }

    // Опциональные методы для лучшей интеграции

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
        // Определяем, нужен ли пробел между токенами
        if (left == null || right == null) {
            return ParserDefinition.SpaceRequirements.MAY
        }

        val leftType = left.elementType
        val rightType = right.elementType

        // Не нужен пробел между этими парами токенов:
        if (leftType == RhaiTypes.DOT && rightType == RhaiTypes.IDENTIFIER) {
            return ParserDefinition.SpaceRequirements.MUST_NOT
        }
        if (leftType == RhaiTypes.IDENTIFIER && rightType == RhaiTypes.LPAREN) {
            return ParserDefinition.SpaceRequirements.MUST_NOT
        }
        if (leftType == RhaiTypes.DOUBLE_COLON && rightType == RhaiTypes.IDENTIFIER) {
            return ParserDefinition.SpaceRequirements.MUST_NOT
        }
        if (leftType == RhaiTypes.IDENTIFIER && rightType == RhaiTypes.DOUBLE_COLON) {
            return ParserDefinition.SpaceRequirements.MUST_NOT
        }
        if (OPERATORS.contains(leftType) && OPERATORS.contains(rightType)) {
            return ParserDefinition.SpaceRequirements.MUST
        }

        return ParserDefinition.SpaceRequirements.MAY
    }
}
