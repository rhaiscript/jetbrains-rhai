package org.rhai.features

import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.rhai.RhaiStatement
import org.rhai.RhaiExpression
import org.rhai.lang.RhaiFile

/**
 * Descriptor for "Surround with" functionality in Rhai
 */
class RhaiSurroundDescriptor : SurroundDescriptor {

    override fun getElementsToSurround(
        file: PsiFile,
        startOffset: Int,
        endOffset: Int
    ): Array<PsiElement> {
        if (file !is RhaiFile) return PsiElement.EMPTY_ARRAY

        val startElement = file.findElementAt(startOffset) ?: return PsiElement.EMPTY_ARRAY
        file.findElementAt(endOffset - 1) ?: return PsiElement.EMPTY_ARRAY

        // Find statements or expressions in the range
        val statement = PsiTreeUtil.getParentOfType(startElement, RhaiStatement::class.java)
        if (statement != null && statement.textRange.endOffset <= endOffset) {
            return arrayOf(statement)
        }

        val expression = PsiTreeUtil.getParentOfType(startElement, RhaiExpression::class.java)
        if (expression != null && expression.textRange.endOffset <= endOffset) {
            return arrayOf(expression)
        }

        return PsiElement.EMPTY_ARRAY
    }

    override fun getSurrounders(): Array<Surrounder> = SURROUNDERS

    override fun isExclusive(): Boolean = false

    companion object {
        private val SURROUNDERS: Array<Surrounder> = arrayOf(
            RhaiIfSurrounder(),
            RhaiIfElseSurrounder(),
            RhaiWhileSurrounder(),
            RhaiForSurrounder(),
            RhaiLoopSurrounder(),
            RhaiTryCatchSurrounder(),
            RhaiBlockSurrounder(),
            RhaiParenthesesSurrounder()
        )
    }
}

/**
 * Base class for Rhai surrounders
 */
abstract class RhaiSurrounderBase : Surrounder {

    abstract val templateText: String
    abstract val caretOffset: Int

    override fun isApplicable(elements: Array<out PsiElement>): Boolean {
        return elements.isNotEmpty()
    }

    override fun surroundElements(
        project: Project,
        editor: Editor,
        elements: Array<out PsiElement>
    ): TextRange? {
        if (elements.isEmpty()) return null

        val first = elements.first()
        val last = elements.last()
        val startOffset = first.textRange.startOffset
        val endOffset = last.textRange.endOffset
        val selectedText = editor.document.getText(TextRange(startOffset, endOffset))

        val indent = getIndent(editor, startOffset)
        val newText = buildSurroundText(selectedText, indent)

        editor.document.replaceString(startOffset, endOffset, newText)

        return TextRange(startOffset + caretOffset, startOffset + caretOffset)
    }

    protected open fun buildSurroundText(content: String, indent: String): String {
        val indentedContent = content.lines().joinToString("\n") { "$indent    $it" }
        return templateText.replace("\$CONTENT\$", indentedContent).replace("\$INDENT\$", indent)
    }

    private fun getIndent(editor: Editor, offset: Int): String {
        val lineNumber = editor.document.getLineNumber(offset)
        val lineStart = editor.document.getLineStartOffset(lineNumber)
        val text = editor.document.getText(TextRange(lineStart, offset))
        return text.takeWhile { it.isWhitespace() }
    }
}

class RhaiIfSurrounder : RhaiSurrounderBase() {
    override fun getTemplateDescription(): String = "if { ... }"
    override val templateText = "if  {\n\$CONTENT\$\n\$INDENT\$}"
    override val caretOffset = 3
}

class RhaiIfElseSurrounder : RhaiSurrounderBase() {
    override fun getTemplateDescription(): String = "if { ... } else { ... }"
    override val templateText = "if  {\n\$CONTENT\$\n\$INDENT\$} else {\n\$INDENT\$    \n\$INDENT\$}"
    override val caretOffset = 3
}

class RhaiWhileSurrounder : RhaiSurrounderBase() {
    override fun getTemplateDescription(): String = "while { ... }"
    override val templateText = "while  {\n\$CONTENT\$\n\$INDENT\$}"
    override val caretOffset = 6
}

class RhaiForSurrounder : RhaiSurrounderBase() {
    override fun getTemplateDescription(): String = "for in { ... }"
    override val templateText = "for item in  {\n\$CONTENT\$\n\$INDENT\$}"
    override val caretOffset = 12
}

class RhaiLoopSurrounder : RhaiSurrounderBase() {
    override fun getTemplateDescription(): String = "loop { ... }"
    override val templateText = "loop {\n\$CONTENT\$\n\$INDENT\$}"
    override val caretOffset = 6
}

class RhaiTryCatchSurrounder : RhaiSurrounderBase() {
    override fun getTemplateDescription(): String = "try { ... } catch { ... }"
    override val templateText = "try {\n\$CONTENT\$\n\$INDENT\$} catch (err) {\n\$INDENT\$    print(err);\n\$INDENT\$}"
    override val caretOffset = 5
}

class RhaiBlockSurrounder : RhaiSurrounderBase() {
    override fun getTemplateDescription(): String = "{ ... }"
    override val templateText = "{\n\$CONTENT\$\n\$INDENT\$}"
    override val caretOffset = 1
}

class RhaiParenthesesSurrounder : RhaiSurrounderBase() {
    override fun getTemplateDescription(): String = "( ... )"
    override val templateText = "(\$CONTENT\$)"
    override val caretOffset = 1

    override fun buildSurroundText(content: String, indent: String): String {
        return "($content)"
    }
}
