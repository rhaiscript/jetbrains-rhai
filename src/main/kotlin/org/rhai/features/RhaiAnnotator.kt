package org.rhai.features

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.rhai.*
import org.rhai.lang.RhaiFile

class RhaiAnnotator : Annotator {

    companion object {
        val FUNCTION_DECLARATION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_FUNCTION_DECLARATION",
            DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
        )

        val FUNCTION_CALL: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_FUNCTION_CALL",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
        )

        val PARAMETER: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_PARAMETER",
            DefaultLanguageHighlighterColors.PARAMETER
        )

        val LOCAL_VARIABLE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_LOCAL_VARIABLE",
            DefaultLanguageHighlighterColors.LOCAL_VARIABLE
        )

        val CONSTANT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_CONSTANT",
            DefaultLanguageHighlighterColors.CONSTANT
        )

        val GLOBAL_VARIABLE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_GLOBAL_VARIABLE",
            DefaultLanguageHighlighterColors.GLOBAL_VARIABLE
        )

        val PROPERTY: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_PROPERTY",
            DefaultLanguageHighlighterColors.INSTANCE_FIELD
        )

        val MODULE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_MODULE",
            DefaultLanguageHighlighterColors.CLASS_NAME
        )

        val LABEL: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_LABEL",
            DefaultLanguageHighlighterColors.LABEL
        )

        val DOC_COMMENT_TAG: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_DOC_COMMENT_TAG",
            DefaultLanguageHighlighterColors.DOC_COMMENT_TAG
        )

        val ATTRIBUTE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "RHAI_ATTRIBUTE",
            DefaultLanguageHighlighterColors.METADATA
        )
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when {
            // Function definitions
            element is RhaiFunctionDefinition -> annotateFunctionDefinition(element, holder)

            // Function parameters
            element is RhaiParameter -> annotateParameter(element, holder)

            // Closure parameters
            element is RhaiClosureParam -> annotateClosureParam(element, holder)

            // Let declarations
            element is RhaiLetDeclaration -> annotateLetDeclaration(element, holder)

            // Const declarations
            element is RhaiConstDeclaration -> annotateConstDeclaration(element, holder)

            // Module declarations
            element is RhaiModuleDeclaration -> annotateModuleDeclaration(element, holder)

            // Labels
            element is RhaiLabel -> annotateLabel(element, holder)

            // Attributes
            element is RhaiAttribute -> annotateAttribute(element, holder)

            // Import statements
            element is RhaiImportStatement -> annotateImportStatement(element, holder)

            // Identifiers (for calls, references, etc.)
            element.elementType == RhaiTypes.IDENTIFIER -> annotateIdentifier(element, holder)

            // Bad characters
            element.elementType == TokenType.BAD_CHARACTER -> annotateBadCharacter(element, holder)
        }
    }

    private fun annotateFunctionDefinition(func: RhaiFunctionDefinition, holder: AnnotationHolder) {
        val nameElement = func.identifier

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(nameElement.textRange)
            .textAttributes(FUNCTION_DECLARATION)
            .create()
    }

    private fun annotateParameter(param: RhaiParameter, holder: AnnotationHolder) {
        val pattern = param.pattern

        // Highlight the pattern (parameter name)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(pattern.textRange)
            .textAttributes(PARAMETER)
            .create()
    }

    private fun annotateClosureParam(param: RhaiClosureParam, holder: AnnotationHolder) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(param.textRange)
            .textAttributes(PARAMETER)
            .create()
    }

    private fun annotateLetDeclaration(letDecl: RhaiLetDeclaration, holder: AnnotationHolder) {
        val pattern = letDecl.pattern

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(pattern.textRange)
            .textAttributes(LOCAL_VARIABLE)
            .create()
    }

    private fun annotateConstDeclaration(constDecl: RhaiConstDeclaration, holder: AnnotationHolder) {
        // Find the identifier in const declaration
        constDecl.node.findChildByType(RhaiTypes.IDENTIFIER)?.let { idNode ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(idNode.textRange)
                .textAttributes(CONSTANT)
                .create()
        }
    }

    private fun annotateModuleDeclaration(module: RhaiModuleDeclaration, holder: AnnotationHolder) {
        module.node.findChildByType(RhaiTypes.IDENTIFIER)?.let { idNode ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(idNode.textRange)
                .textAttributes(MODULE)
                .create()
        }
    }

    private fun annotateLabel(label: RhaiLabel, holder: AnnotationHolder) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(label.textRange)
            .textAttributes(LABEL)
            .create()
    }

    private fun annotateAttribute(attr: RhaiAttribute, holder: AnnotationHolder) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(attr.textRange)
            .textAttributes(ATTRIBUTE)
            .create()
    }

    private fun annotateImportStatement(importStmt: RhaiImportStatement, holder: AnnotationHolder) {
        // Highlight the module path
        importStmt.importPath?.let { path ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(path.textRange)
                .textAttributes(MODULE)
                .create()
        }
    }

    private fun annotateIdentifier(identifier: PsiElement, holder: AnnotationHolder) {
        val parent = identifier.parent
        val text = identifier.text

        when {
            // Skip if it's already handled (function definition name)
            parent is RhaiFunctionDefinition && identifier == parent.identifier -> return

            // Skip if it's a parameter
            parent is RhaiParameter || parent is RhaiPattern -> return

            // Check if it's a function call
            isFunctionCall(identifier) -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(identifier.textRange)
                    .textAttributes(FUNCTION_CALL)
                    .create()
            }

            // Check if it's a property access (after dot)
            isPropertyAccess(identifier) -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(identifier.textRange)
                    .textAttributes(PROPERTY)
                    .create()
            }

            // Check if it's 'this' keyword
            text == "this" -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(identifier.textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.KEYWORD)
                    .create()
            }

            // Check if it references a constant
            isConstantReference(identifier) -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(identifier.textRange)
                    .textAttributes(CONSTANT)
                    .create()
            }

            // Check if it references a parameter
            isParameterReference(identifier) -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(identifier.textRange)
                    .textAttributes(PARAMETER)
                    .create()
            }

            // Local variable reference
            isLocalVariableReference(identifier) -> {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(identifier.textRange)
                    .textAttributes(LOCAL_VARIABLE)
                    .create()
            }
        }
    }

    private fun isFunctionCall(identifier: PsiElement): Boolean {
        var sibling = identifier.nextSibling

        // Skip whitespace
        while (sibling != null && sibling.elementType == TokenType.WHITE_SPACE) {
            sibling = sibling.nextSibling
        }

        // Check if followed by opening parenthesis
        return sibling?.elementType == RhaiTypes.LPAREN
    }

    private fun isPropertyAccess(identifier: PsiElement): Boolean {
        var sibling = identifier.prevSibling

        // Skip whitespace
        while (sibling != null && sibling.elementType == TokenType.WHITE_SPACE) {
            sibling = sibling.prevSibling
        }

        // Check if preceded by dot
        return sibling?.elementType == RhaiTypes.DOT
    }

    private fun isConstantReference(identifier: PsiElement): Boolean {
        val file = identifier.containingFile as? RhaiFile ?: return false
        val name = identifier.text

        // Check if there's a const declaration with this name
        return PsiTreeUtil.findChildrenOfType(file, RhaiConstDeclaration::class.java)
            .any { it.node.findChildByType(RhaiTypes.IDENTIFIER)?.text == name }
    }

    private fun isParameterReference(identifier: PsiElement): Boolean {
        // Find enclosing function
        val func = PsiTreeUtil.getParentOfType(identifier, RhaiFunctionDefinition::class.java)
            ?: return false

        val name = identifier.text

        // Check if this name matches a parameter
        return func.parameters?.parameterList?.any { param ->
            param.pattern.text == name
        } ?: false
    }

    private fun isLocalVariableReference(identifier: PsiElement): Boolean {
        val file = identifier.containingFile as? RhaiFile ?: return false
        val name = identifier.text

        // Find all let declarations before this identifier
        val declarations = PsiTreeUtil.findChildrenOfType(file, RhaiLetDeclaration::class.java)

        return declarations.any { letDecl ->
            letDecl.textOffset < identifier.textOffset &&
                    letDecl.pattern.text == name
        }
    }

    private fun annotateBadCharacter(element: PsiElement, holder: AnnotationHolder) {
        holder.newAnnotation(HighlightSeverity.ERROR, "Invalid character: '${element.text}'")
            .range(element.textRange)
            .highlightType(ProblemHighlightType.ERROR)
            .create()
    }
}
