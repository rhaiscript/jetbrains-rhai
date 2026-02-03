package org.rhai.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.startOffset
import org.rhai.*
import org.rhai.lang.RhaiFile


class RhaiGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (element == null) return null

        // Check that this is an IDENTIFIER token
        if (element.elementType != RhaiTypes.IDENTIFIER) return null

        val name = element.text
        val file = element.containingFile as? RhaiFile ?: return null

        val targets = mutableListOf<PsiElement>()

        // 0. Check if this is part of a path expression (e.g., cfg::APP_NAME)
        if (isPartOfPathExpression(element)) {
            findPathExpressionTargets(element, file, targets)
            if (targets.isNotEmpty()) {
                return targets.toTypedArray()
            }
        }

        // 0.5. Check if this is an import alias usage
        findImportAliasTargets(file, name, targets)
        if (targets.isNotEmpty()) {
            return targets.toTypedArray()
        }

        // 1. Find functions
        findFunctionDefinitions(file, name, element, targets)

        // 2. Find variables (let/const declarations)
        if (targets.isEmpty()) {
            findVariableDeclarations(file, name, element, targets)
        }

        // 3. Find parameters
        if (targets.isEmpty()) {
            findParameters(element, name, targets)
        }

        // 4. Find for loop variables
        if (targets.isEmpty()) {
            findForLoopVariables(element, name, targets)
        }

        // 5. Find closure parameters
        if (targets.isEmpty()) {
            findClosureParameters(element, name, targets)
        }

        // 6. Find module declarations
        if (targets.isEmpty()) {
            findModuleDeclarations(file, name, targets)
        }

        return targets.takeIf { it.isNotEmpty() }?.toTypedArray()
    }

    private fun isPartOfPathExpression(element: PsiElement): Boolean {
        // Check if preceded or followed by ::
        var prev = element.prevSibling
        while (prev != null && prev.elementType == TokenType.WHITE_SPACE) {
            prev = prev.prevSibling
        }
        if (prev?.elementType == RhaiTypes.DOUBLE_COLON) return true

        var next = element.nextSibling
        while (next != null && next.elementType == TokenType.WHITE_SPACE) {
            next = next.nextSibling
        }
        if (next?.elementType == RhaiTypes.DOUBLE_COLON) return true

        // Check parent
        return element.parent is RhaiPathExpression
    }

    private fun findPathExpressionTargets(
        element: PsiElement,
        file: RhaiFile,
        targets: MutableList<PsiElement>
    ) {
        // Find the full path expression
        val pathExpr = PsiTreeUtil.getParentOfType(element, RhaiPathExpression::class.java)
            ?: return

        // Get all identifiers in the path
        val identifiers = pathExpr.text.split("::")
        if (identifiers.size < 2) return

        val moduleAlias = identifiers[0].trim()
        val memberName = identifiers.drop(1).joinToString("::").trim()

        // Find the import statement for this alias
        val importedFile = findImportedFile(file, moduleAlias) ?: return

        // Find the declaration in the imported file
        findDeclarationInFile(importedFile, memberName, targets)
    }

    private fun findImportAliasTargets(
        file: RhaiFile,
        name: String,
        targets: MutableList<PsiElement>
    ) {
        // Check if 'name' is an import alias and navigate to the import statement
        PsiTreeUtil.findChildrenOfType(file, RhaiImportStatement::class.java).forEach { importStmt ->
            // Look for 'as NAME' pattern
            val text = importStmt.text
            val asMatch = Regex("""as\s+(\w+)""").find(text)
            if (asMatch != null && asMatch.groupValues[1] == name) {
                // Return the alias identifier in the import statement
                importStmt.node.getChildren(null).forEach { child ->
                    if (child.elementType == RhaiTypes.IDENTIFIER && child.text == name) {
                        targets.add(child.psi)
                    }
                }
            }
        }
    }

    private fun findImportedFile(file: RhaiFile, alias: String): RhaiFile? {
        // Find import statement with this alias
        PsiTreeUtil.findChildrenOfType(file, RhaiImportStatement::class.java).forEach { importStmt ->
            val text = importStmt.text
            // Match: import "filename" as alias
            val match = Regex("""import\s+"([^"]+)"\s+as\s+(\w+)""").find(text)
            if (match != null && match.groupValues[2] == alias) {
                val importPath = match.groupValues[1]
                // Find the file
                val currentDir = file.virtualFile?.parent ?: return@forEach
                val targetFile = currentDir.findChild("$importPath.rhai")
                    ?: currentDir.findChild(importPath)
                if (targetFile != null) {
                    val psiFile = PsiManager.getInstance(file.project).findFile(targetFile)
                    if (psiFile is RhaiFile) {
                        return psiFile
                    }
                }
            }
        }
        return null
    }

    private fun findDeclarationInFile(
        file: RhaiFile,
        name: String,
        targets: MutableList<PsiElement>
    ) {
        // Find function with this name
        PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java).forEach { funcDef ->
            if (funcDef.identifier.text == name) {
                targets.add(funcDef.identifier)
            }
        }

        // Find const with this name
        if (targets.isEmpty()) {
            PsiTreeUtil.findChildrenOfType(file, RhaiConstDeclaration::class.java).forEach { constDecl ->
                val pattern = constDecl.pattern
                // Try node first, then identifier property
                val astNode = pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
                if (astNode != null && astNode.text == name) {
                    targets.add(astNode.psi)
                } else {
                    val idElement = pattern.identifier
                    if (idElement != null && idElement.text == name) {
                        targets.add(idElement)
                    }
                }
            }
        }

        // Find let with this name (for exported variables)
        if (targets.isEmpty()) {
            PsiTreeUtil.findChildrenOfType(file, RhaiLetDeclaration::class.java).forEach { letDecl ->
                val pattern = letDecl.pattern
                val astNode = pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
                if (astNode != null && astNode.text == name) {
                    targets.add(astNode.psi)
                } else {
                    val idElement = pattern.identifier
                    if (idElement != null && idElement.text == name) {
                        targets.add(idElement)
                    }
                }
            }
        }
    }

    private fun findFunctionDefinitions(
        file: RhaiFile,
        name: String,
        sourceElement: PsiElement,
        targets: MutableList<PsiElement>
    ) {
        PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java).forEach { funcDef ->
            val identifier = funcDef.identifier
            if (identifier.text == name && identifier.startOffset != sourceElement.startOffset) {
                targets.add(identifier)
            }
        }
    }

    private fun findVariableDeclarations(
        file: RhaiFile,
        name: String,
        sourceElement: PsiElement,
        targets: MutableList<PsiElement>
    ) {
        val sourceOffset = sourceElement.startOffset

        // Find let declarations
        PsiTreeUtil.findChildrenOfType(file, RhaiLetDeclaration::class.java)
            .filter { it.textOffset < sourceOffset }
            .forEach { letDecl ->
                val pattern = letDecl.pattern
                val idNode = pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
                if (idNode?.text == name) {
                    targets.add(idNode.psi)
                }
            }

        // Find const declarations
        PsiTreeUtil.findChildrenOfType(file, RhaiConstDeclaration::class.java)
            .filter { it.textOffset < sourceOffset }
            .forEach { constDecl ->
                val idNode = constDecl.node.findChildByType(RhaiTypes.IDENTIFIER)
                if (idNode?.text == name) {
                    targets.add(idNode.psi)
                }
            }
    }

    private fun findParameters(
        sourceElement: PsiElement,
        name: String,
        targets: MutableList<PsiElement>
    ) {
        // Check if inside a function
        val funcDef = PsiTreeUtil.getParentOfType(sourceElement, RhaiFunctionDefinition::class.java)
        funcDef?.parameters?.parameterList?.forEach { param ->
            val pattern = param.pattern
            val idNode = pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
            if (idNode?.text == name) {
                targets.add(idNode.psi)
            }
        }
    }

    private fun findForLoopVariables(
        sourceElement: PsiElement,
        name: String,
        targets: MutableList<PsiElement>
    ) {
        val forStmt = PsiTreeUtil.getParentOfType(sourceElement, RhaiForStatement::class.java)
        forStmt?.let {
            val idNode = it.node.findChildByType(RhaiTypes.IDENTIFIER)
            if (idNode?.text == name && idNode.startOffset != sourceElement.startOffset) {
                targets.add(idNode.psi)
            }
        }
    }

    private fun findClosureParameters(
        sourceElement: PsiElement,
        name: String,
        targets: MutableList<PsiElement>
    ) {
        // Find all parent closures (can be nested)
        var current: PsiElement? = sourceElement
        while (current != null) {
            val closure = PsiTreeUtil.getParentOfType(current, RhaiClosureExpr::class.java, false)
            if (closure != null) {
                closure.closureParams?.closureParamList?.forEach { param ->
                    val pattern = param.pattern
                    val idNode = pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
                    if (idNode?.text == name && idNode.startOffset != sourceElement.startOffset) {
                        targets.add(idNode.psi)
                    }
                }
                current = closure.parent
            } else {
                break
            }
        }
    }

    private fun findModuleDeclarations(
        file: RhaiFile,
        name: String,
        targets: MutableList<PsiElement>
    ) {
        PsiTreeUtil.findChildrenOfType(file, RhaiModuleDeclaration::class.java).forEach { moduleDef ->
            val idNode = moduleDef.node.findChildByType(RhaiTypes.IDENTIFIER)
            if (idNode?.text == name) {
                targets.add(idNode.psi)
            }
        }
    }
}
