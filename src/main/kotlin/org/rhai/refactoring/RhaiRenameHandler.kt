package org.rhai.refactoring

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.refactoring.rename.PsiElementRenameHandler
import com.intellij.refactoring.rename.RenameHandler
import org.rhai.RhaiTypes
import org.rhai.lang.RhaiFile

/**
 * Rename handler for Rhai identifiers.
 * Enables Shift+F6 rename refactoring for variables, functions, constants, etc.
 */
class RhaiRenameHandler : RenameHandler {

    override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
        val element = getTargetElement(dataContext) ?: return false
        return isRenamableElement(element)
    }

    override fun isRenaming(dataContext: DataContext): Boolean {
        return isAvailableOnDataContext(dataContext)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext) {
        val element = getTargetElement(dataContext) ?: return
        if (editor == null || file == null) return

        if (isRenamableElement(element)) {
            // Use built-in rename dialog with our element
            PsiElementRenameHandler.invoke(element, project, element, editor)
        }
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext) {
        if (elements.isNotEmpty()) {
            val editor = CommonDataKeys.EDITOR.getData(dataContext)
            if (editor != null) {
                PsiElementRenameHandler.invoke(elements[0], project, elements[0], editor)
            }
        }
    }

    private fun getTargetElement(dataContext: DataContext): PsiElement? {
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return null
        val file = CommonDataKeys.PSI_FILE.getData(dataContext) ?: return null

        if (file !is RhaiFile) return null

        val offset = editor.caretModel.offset
        return file.findElementAt(offset)
    }

    private fun isRenamableElement(element: PsiElement): Boolean {
        // Only allow renaming IDENTIFIER tokens
        if (element.elementType != RhaiTypes.IDENTIFIER) return false

        val name = element.text

        // Don't allow renaming keywords
        val keywords = setOf(
            "let", "const", "fn", "if", "else", "while", "for", "in",
            "loop", "break", "continue", "return", "throw", "try", "catch",
            "switch", "default", "import", "export", "module", "private", "pub",
            "true", "false", "null", "this", "global", "is", "as"
        )
        if (keywords.contains(name)) return false

        // Don't allow renaming built-in functions
        val builtins = setOf(
            "print", "println", "debug", "type_of", "is_def",
            "to_string", "to_int", "to_float", "to_decimal",
            "abs", "sin", "cos", "tan", "sqrt", "exp", "ln", "log",
            "push", "pop", "shift", "insert", "remove", "reverse", "sort",
            "map", "filter", "reduce", "some", "all", "for_each",
            "keys", "values", "len", "contains", "trim", "replace", "split"
        )
        if (builtins.contains(name)) return false

        return true
    }
}
