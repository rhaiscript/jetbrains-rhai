package org.rhai

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

class RhaiParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(RhaiLanguage.INSTANCE)
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(RhaiTypes.LINE_COMMENT)
        val STRING_LITERALS = TokenSet.create(
            RhaiTypes.STRING_LITERAL,
            RhaiTypes.STRING_START,
            RhaiTypes.STRING_END
        )
    }

    override fun createLexer(project: Project): Lexer = RsLexer()
    override fun createParser(project: Project): PsiParser = RhaiParser()
    override fun getFileNodeType(): IFileElementType = FILE
    override fun getWhitespaceTokens(): TokenSet = WHITE_SPACES
    override fun getCommentTokens(): TokenSet = COMMENTS
    override fun getStringLiteralElements(): TokenSet = STRING_LITERALS
    override fun createElement(node: ASTNode): PsiElement = RhaiTypes.Factory.createElement(node)
    override fun createFile(viewProvider: FileViewProvider): PsiFile = RhaiFile(viewProvider)
}
