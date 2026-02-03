package org.rhai.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.rhai.*
import org.rhai.lang.RhaiFile

/**
 * Convert 'let' to 'const' intention
 */
class ConvertLetToConstIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = "Rhai"
    override fun getText(): String = "Convert 'let' to 'const'"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.containingFile !is RhaiFile) return false

        val letDecl = PsiTreeUtil.getParentOfType(element, RhaiLetDeclaration::class.java)
        return letDecl != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val letDecl = PsiTreeUtil.getParentOfType(element, RhaiLetDeclaration::class.java)
            ?: return

        val letKeyword = letDecl.node.findChildByType(RhaiTypes.LET) ?: return
        val document = element.containingFile.viewProvider.document ?: return

        document.replaceString(
            letKeyword.startOffset,
            letKeyword.startOffset + letKeyword.textLength,
            "const"
        )
    }
}

/**
 * Convert 'const' to 'let' intention
 */
class ConvertConstToLetIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = "Rhai"
    override fun getText(): String = "Convert 'const' to 'let'"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.containingFile !is RhaiFile) return false

        val constDecl = PsiTreeUtil.getParentOfType(element, RhaiConstDeclaration::class.java)
        return constDecl != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val constDecl = PsiTreeUtil.getParentOfType(element, RhaiConstDeclaration::class.java)
            ?: return

        val constKeyword = constDecl.node.findChildByType(RhaiTypes.CONST) ?: return
        val document = element.containingFile.viewProvider.document ?: return

        document.replaceString(
            constKeyword.startOffset,
            constKeyword.startOffset + constKeyword.textLength,
            "let"
        )
    }
}

/**
 * Add type annotation intention
 */
class AddTypeAnnotationIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = "Rhai"
    override fun getText(): String = "Add type annotation"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.containingFile !is RhaiFile) return false

        val letDecl = PsiTreeUtil.getParentOfType(element, RhaiLetDeclaration::class.java)
        if (letDecl != null && letDecl.typeAnnotation == null) {
            return true
        }

        val param = PsiTreeUtil.getParentOfType(element, RhaiParameter::class.java)
        if (param != null && param.typeAnnotation == null) {
            return true
        }

        return false
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val document = element.containingFile.viewProvider.document ?: return

        val letDecl = PsiTreeUtil.getParentOfType(element, RhaiLetDeclaration::class.java)
        if (letDecl != null) {
            val pattern = letDecl.pattern
            val insertOffset = pattern.textRange.endOffset
            document.insertString(insertOffset, ": ")
            editor?.caretModel?.moveToOffset(insertOffset + 2)
            return
        }

        val param = PsiTreeUtil.getParentOfType(element, RhaiParameter::class.java)
        if (param != null) {
            val pattern = param.pattern
            val insertOffset = pattern.textRange.endOffset
            document.insertString(insertOffset, ": ")
            editor?.caretModel?.moveToOffset(insertOffset + 2)
        }
    }
}

/**
 * Convert if-else to switch expression
 */
class ConvertIfToSwitchIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = "Rhai"
    override fun getText(): String = "Convert to switch expression"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.containingFile !is RhaiFile) return false

        val ifStmt = PsiTreeUtil.getParentOfType(element, RhaiIfStatement::class.java)
        return ifStmt != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        // This is a complex refactoring - placeholder implementation
        val ifStmt = PsiTreeUtil.getParentOfType(element, RhaiIfStatement::class.java)
            ?: return

        // For now, just add a comment suggesting the conversion
        val document = element.containingFile.viewProvider.document ?: return
        val insertOffset = ifStmt.textRange.startOffset
        document.insertString(insertOffset, "// TODO: Convert to switch\n")
    }
}

/**
 * Extract closure to function
 */
class ExtractClosureToFunctionIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = "Rhai"
    override fun getText(): String = "Extract closure to function"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.containingFile !is RhaiFile) return false

        val closure = PsiTreeUtil.getParentOfType(element, RhaiClosureExpr::class.java)
        return closure != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val closure = PsiTreeUtil.getParentOfType(element, RhaiClosureExpr::class.java)
            ?: return

        val document = element.containingFile.viewProvider.document ?: return

        // Get closure parameters
        val params = closure.closureParams?.closureParamList?.joinToString(", ") {
            it.pattern.text
        } ?: ""

        // Get closure body
        val body = closure.block?.text ?: closure.expression?.text ?: "// body"

        // Create function text
        val functionText = "\nfn extracted_function($params) $body\n"

        // Find file start to insert function
        val file = element.containingFile
        val insertOffset = file.textRange.endOffset

        document.insertString(insertOffset, functionText)

        // Replace closure with function reference
        val closureRange = closure.textRange
        document.replaceString(closureRange.startOffset, closureRange.endOffset, "extracted_function")
    }
}

/**
 * Add return type to function
 */
class AddReturnTypeIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = "Rhai"
    override fun getText(): String = "Add return type"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.containingFile !is RhaiFile) return false

        val funcDef = PsiTreeUtil.getParentOfType(element, RhaiFunctionDefinition::class.java)
        return funcDef != null && funcDef.returnType == null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val funcDef = PsiTreeUtil.getParentOfType(element, RhaiFunctionDefinition::class.java)
            ?: return

        val document = element.containingFile.viewProvider.document ?: return

        // Find the position after the closing parenthesis
        val params = funcDef.parameters
        val insertOffset = if (params != null) {
            params.textRange.endOffset + 1 // After RPAREN
        } else {
            val lparen = funcDef.node.findChildByType(RhaiTypes.LPAREN)
            val rparen = funcDef.node.findChildByType(RhaiTypes.RPAREN)
            rparen?.textRange?.endOffset ?: return
        }

        document.insertString(insertOffset, " -> ")
        editor?.caretModel?.moveToOffset(insertOffset + 4)
    }
}

/**
 * Toggle function visibility (private/pub)
 */
class ToggleFunctionVisibilityIntention : PsiElementBaseIntentionAction(), IntentionAction {

    override fun getFamilyName(): String = "Rhai"
    override fun getText(): String = "Toggle visibility (private/public)"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.containingFile !is RhaiFile) return false

        val funcDef = PsiTreeUtil.getParentOfType(element, RhaiFunctionDefinition::class.java)
        return funcDef != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val funcDef = PsiTreeUtil.getParentOfType(element, RhaiFunctionDefinition::class.java)
            ?: return

        val document = element.containingFile.viewProvider.document ?: return
        val visibility = funcDef.visibility

        if (visibility != null) {
            // Remove visibility modifier
            document.deleteString(visibility.textRange.startOffset, visibility.textRange.endOffset + 1)
        } else {
            // Add 'pub' modifier
            val fnKeyword = funcDef.node.findChildByType(RhaiTypes.FN) ?: return
            document.insertString(fnKeyword.startOffset, "pub ")
        }
    }
}
