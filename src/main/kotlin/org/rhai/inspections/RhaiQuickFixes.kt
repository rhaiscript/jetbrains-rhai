package org.rhai.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.rhai.RhaiBlock
import org.rhai.RhaiFunctionDefinition
import org.rhai.RhaiLetDeclaration
import org.rhai.lang.RhaiFile

/**
 * Quick fix that creates a new function for an unresolved function call.
 */
class CreateFunctionQuickFix(
    private val functionName: String,
    private val argumentCount: Int
) : LocalQuickFix {

    override fun getName(): String = "Create function '$functionName'"

    override fun getFamilyName(): String = "Create function"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val file = element.containingFile as? RhaiFile ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

        // Generate parameter names
        val params = if (argumentCount > 0) {
            (1..argumentCount).joinToString(", ") { "arg$it" }
        } else {
            ""
        }

        // Create function template
        val functionTemplate = """

fn $functionName($params) {
    // TODO: Implement $functionName
}
"""

        // Find the best insertion point (end of file or after last function)
        val lastFunction = PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java).lastOrNull()
        val insertOffset = lastFunction?.textRange?.endOffset ?: file.textLength

        document.insertString(insertOffset, functionTemplate)
        PsiDocumentManager.getInstance(project).commitDocument(document)

        // Move cursor to the function body
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor != null && editor.document == document) {
            val todoOffset = insertOffset + functionTemplate.indexOf("// TODO")
            if (todoOffset > insertOffset) {
                editor.caretModel.moveToOffset(todoOffset)
            }
        }
    }
}

/**
 * Quick fix that creates a new variable for an unresolved reference.
 */
class CreateVariableQuickFix(
    private val variableName: String
) : LocalQuickFix {

    override fun getName(): String = "Create variable '$variableName'"

    override fun getFamilyName(): String = "Create variable"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val file = element.containingFile as? RhaiFile ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

        // Find the appropriate insertion point (beginning of enclosing block or before the line)
        val enclosingBlock = PsiTreeUtil.getParentOfType(element, RhaiBlock::class.java)
        val enclosingFunction = PsiTreeUtil.getParentOfType(element, RhaiFunctionDefinition::class.java)

        val insertOffset: Int
        val variableDeclaration: String

        if (enclosingBlock != null) {
            // Insert at the beginning of the block (after the opening brace)
            val lbraceOffset = enclosingBlock.textOffset + 1
            insertOffset = lbraceOffset
            variableDeclaration = "\n    let $variableName = null; // TODO: Initialize"
        } else if (enclosingFunction != null) {
            // Insert at the beginning of the function block
            val funcBlock = enclosingFunction.block
            if (funcBlock != null) {
                insertOffset = funcBlock.textOffset + 1
                variableDeclaration = "\n    let $variableName = null; // TODO: Initialize"
            } else {
                // Fallback: insert before the line containing the reference
                val lineStartOffset = document.getLineStartOffset(document.getLineNumber(element.textOffset))
                insertOffset = lineStartOffset
                variableDeclaration = "let $variableName = null; // TODO: Initialize\n"
            }
        } else {
            // File level - insert at the beginning or after imports
            val firstLetDecl = PsiTreeUtil.findChildOfType(file, RhaiLetDeclaration::class.java)
            insertOffset = firstLetDecl?.textOffset ?: 0
            variableDeclaration = "let $variableName = null; // TODO: Initialize\n"
        }

        document.insertString(insertOffset, variableDeclaration)
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }
}

/**
 * Quick fix that deletes a duplicate function definition.
 */
class DeleteDuplicateFunctionQuickFix : LocalQuickFix {

    override fun getName(): String = "Delete duplicate function"

    override fun getFamilyName(): String = "Delete duplicate function"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val funcDef = PsiTreeUtil.getParentOfType(element, RhaiFunctionDefinition::class.java, false)
            ?: element.parent as? RhaiFunctionDefinition
            ?: return

        // Delete the entire function definition
        funcDef.delete()
    }
}

/**
 * Quick fix that renames a duplicate function to make it unique.
 */
class RenameDuplicateFunctionQuickFix(
    private val currentName: String
) : LocalQuickFix {

    override fun getName(): String = "Rename to '${currentName}_2'"

    override fun getFamilyName(): String = "Rename duplicate function"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val file = element.containingFile as? RhaiFile ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

        // Find a unique name
        var suffix = 2
        var newName = "${currentName}_$suffix"
        val existingNames = PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java)
            .map { it.identifier.text }
            .toSet()

        while (existingNames.contains(newName)) {
            suffix++
            newName = "${currentName}_$suffix"
        }

        // Replace the identifier
        document.replaceString(element.textOffset, element.textOffset + element.textLength, newName)
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }
}

/**
 * Quick fix that adds semicolon at the end of statement.
 */
class AddSemicolonQuickFix : LocalQuickFix {

    override fun getName(): String = "Add missing semicolon"

    override fun getFamilyName(): String = "Add semicolon"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val document = element.containingFile?.viewProvider?.document ?: return

        val endOffset = element.textRange.endOffset
        document.insertString(endOffset, ";")
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }
}

/**
 * Quick fix that removes an unnecessary semicolon.
 */
class RemoveSemicolonQuickFix : LocalQuickFix {

    override fun getName(): String = "Remove unnecessary semicolon"

    override fun getFamilyName(): String = "Remove semicolon"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        element.delete()
    }
}

/**
 * Quick fix that converts a function to a closure.
 */
class ConvertToClosureQuickFix(
    private val functionName: String
) : LocalQuickFix {

    override fun getName(): String = "Convert to closure assigned to '$functionName'"

    override fun getFamilyName(): String = "Convert to closure"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val funcDef = PsiTreeUtil.getParentOfType(element, RhaiFunctionDefinition::class.java, false)
            ?: return
        val file = element.containingFile as? RhaiFile ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

        // Extract parameters
        val params = funcDef.parameters?.parameterList?.joinToString(", ") { it.text } ?: ""

        // Extract body
        val bodyBlock = funcDef.block
        val bodyText = if (bodyBlock != null) {
            // Get content between { and }
            val blockText = bodyBlock.text
            if (blockText.startsWith("{") && blockText.endsWith("}")) {
                blockText.substring(1, blockText.length - 1).trim()
            } else {
                blockText
            }
        } else {
            "// TODO"
        }

        // Build closure
        val closureText = "let $functionName = |$params| {\n    $bodyText\n};"

        // Replace function with closure
        document.replaceString(funcDef.textOffset, funcDef.textRange.endOffset, closureText)
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }
}

/**
 * Quick fix that simplifies a redundant expression (e.g., x == true -> x)
 */
class SimplifyBooleanExpressionQuickFix(
    private val simplifiedExpression: String
) : LocalQuickFix {

    override fun getName(): String = "Simplify to '$simplifiedExpression'"

    override fun getFamilyName(): String = "Simplify boolean expression"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val document = element.containingFile?.viewProvider?.document ?: return

        document.replaceString(element.textOffset, element.textRange.endOffset, simplifiedExpression)
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }
}
