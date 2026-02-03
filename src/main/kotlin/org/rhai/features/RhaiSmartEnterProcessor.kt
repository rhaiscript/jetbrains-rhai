package org.rhai.features

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.rhai.*
import org.rhai.lang.RhaiFile

/**
 * Smart Enter processor for Rhai.
 * Handles automatic completion of statements when pressing Ctrl+Shift+Enter.
 */
class RhaiSmartEnterProcessor : SmartEnterProcessor() {

    override fun process(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        if (psiFile !is RhaiFile) return false

        val caretOffset = editor.caretModel.offset
        val element = psiFile.findElementAt(caretOffset) ?: return false

        // Try to complete different statement types
        return completeLetDeclaration(editor, element) ||
                completeConstDeclaration(editor, element) ||
                addSemicolon(editor, element)
    }

    private fun completeLetDeclaration(editor: Editor, element: PsiElement): Boolean {
        val letDecl = PsiTreeUtil.getParentOfType(element, RhaiLetDeclaration::class.java)
            ?: return false

        val text = letDecl.text
        if (!text.endsWith(";")) {
            val insertOffset = letDecl.textRange.endOffset
            editor.document.insertString(insertOffset, ";")
            editor.caretModel.moveToOffset(insertOffset + 1)
            return true
        }

        return false
    }

    private fun completeConstDeclaration(editor: Editor, element: PsiElement): Boolean {
        val constDecl = PsiTreeUtil.getParentOfType(element, RhaiConstDeclaration::class.java)
            ?: return false

        val text = constDecl.text
        if (!text.endsWith(";")) {
            val insertOffset = constDecl.textRange.endOffset
            editor.document.insertString(insertOffset, ";")
            editor.caretModel.moveToOffset(insertOffset + 1)
            return true
        }

        return false
    }

    private fun addSemicolon(editor: Editor, element: PsiElement): Boolean {
        val exprStmt = PsiTreeUtil.getParentOfType(element, RhaiExpressionStatement::class.java)
            ?: return false

        val text = exprStmt.text
        if (!text.endsWith(";")) {
            val insertOffset = exprStmt.textRange.endOffset
            editor.document.insertString(insertOffset, ";")
            editor.caretModel.moveToOffset(insertOffset + 1)
            return true
        }

        return false
    }
}
