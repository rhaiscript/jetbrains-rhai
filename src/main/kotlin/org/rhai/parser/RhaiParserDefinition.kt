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

        val COMMENTS = TokenSet.create(
            RhaiTypes.LINE_COMMENT,
            RhaiTypes.BLOCK_COMMENT,
            RhaiTypes.DOC_COMMENT,
            RhaiTypes.DOC_LINE
        )

        val STRING_LITERALS = TokenSet.create(
            RhaiTypes.CHAR_LITERAL,
            RhaiTypes.REGEX_LITERAL
        )

        val NUMBER_LITERALS = TokenSet.create()

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

        val CONSTANT_LITERALS = TokenSet.create(
            RhaiTypes.TRUE,
            RhaiTypes.FALSE,
            RhaiTypes.NULL,
            RhaiTypes.UNDEF
        )

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

        val IDENTIFIERS = TokenSet.create(
            RhaiTypes.IDENTIFIER
        )
    }

    override fun createLexer(project: Project): Lexer = RhaiFlexAdapter.INSTANCE

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

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
        if (left == null || right == null) {
            return ParserDefinition.SpaceRequirements.MAY
        }

        val leftType = left.elementType
        val rightType = right.elementType

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
