package org.rhai.refactoring

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
import org.rhai.*
import org.rhai.lang.RhaiFile

/**
 * Handles rename refactoring for Rhai elements.
 * Finds all usages and renames them along with the declaration.
 */
class RhaiRenamePsiElementProcessor : RenamePsiElementProcessor() {

    override fun canProcessElement(element: PsiElement): Boolean {
        if (element.containingFile !is RhaiFile) return false
        return element.elementType == RhaiTypes.IDENTIFIER
    }

    override fun findReferences(
        element: PsiElement,
        searchScope: SearchScope,
        searchInCommentsAndStrings: Boolean
    ): Collection<PsiReference> {
        // First try standard reference search
        val references = ReferencesSearch.search(element, searchScope).findAll().toMutableList()

        // If no references found via standard search, do manual search
        if (references.isEmpty()) {
            val file = element.containingFile as? RhaiFile ?: return references
            val name = element.text

            // Find all identifiers with the same name in the file
            findAllUsages(file, name, element).forEach { usage ->
                references.add(RhaiSimpleReference(usage, element))
            }
        }

        return references
    }

    override fun prepareRenaming(
        element: PsiElement,
        newName: String,
        allRenames: MutableMap<PsiElement, String>
    ) {
        val file = element.containingFile as? RhaiFile ?: return
        val oldName = element.text

        // Find declaration
        val declaration = findDeclaration(file, oldName, element)
        if (declaration != null && declaration != element) {
            allRenames[declaration] = newName
        }

        // Find all usages
        findAllUsages(file, oldName, element).forEach { usage ->
            if (usage != element && !allRenames.containsKey(usage)) {
                allRenames[usage] = newName
            }
        }
    }

    override fun renameElement(
        element: PsiElement,
        newName: String,
        usages: Array<out UsageInfo>,
        listener: RefactoringElementListener?
    ) {
        // Rename the element itself
        renameIdentifier(element, newName)

        // Rename all usages
        usages.forEach { usage ->
            usage.element?.let { usageElement ->
                renameIdentifier(usageElement, newName)
            }
        }

        listener?.elementRenamed(element)
    }

    override fun substituteElementToRename(element: PsiElement, editor: Editor?): PsiElement? {
        // If we're on a usage, find the declaration to rename
        val file = element.containingFile as? RhaiFile ?: return element
        val name = element.text

        return findDeclaration(file, name, element) ?: element
    }

    private fun renameIdentifier(element: PsiElement, newName: String) {
        if (element.elementType != RhaiTypes.IDENTIFIER) return

        val factory = RhaiPsiFactory(element.project)
        val newIdentifier = factory.createIdentifier(newName)
        element.replace(newIdentifier)
    }

    private fun findDeclaration(file: RhaiFile, name: String, sourceElement: PsiElement): PsiElement? {
        val sourceOffset = sourceElement.textOffset

        // Check function definitions
        PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java).forEach { funcDef ->
            val identifier = funcDef.identifier
            if (identifier.text == name) {
                return identifier
            }
        }

        // Check let declarations (find the one that's visible at sourceOffset)
        val letDeclarations = PsiTreeUtil.findChildrenOfType(file, RhaiLetDeclaration::class.java)
            .filter { it.textOffset <= sourceOffset }
            .sortedByDescending { it.textOffset }

        for (letDecl in letDeclarations) {
            val idNode = letDecl.pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
            if (idNode?.text == name) {
                return idNode.psi
            }
        }

        // Check const declarations
        PsiTreeUtil.findChildrenOfType(file, RhaiConstDeclaration::class.java).forEach { constDecl ->
            val idNode = constDecl.pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
            if (idNode?.text == name) {
                return idNode.psi
            }
        }

        // Check parameters in enclosing function
        val funcDef = PsiTreeUtil.getParentOfType(sourceElement, RhaiFunctionDefinition::class.java)
        funcDef?.parameters?.parameterList?.forEach { param ->
            val idNode = param.pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
            if (idNode?.text == name) {
                return idNode.psi
            }
        }

        // Check closure parameters
        var closureParent: PsiElement? = sourceElement
        while (closureParent != null) {
            val closure = PsiTreeUtil.getParentOfType(closureParent, RhaiClosureExpr::class.java, false)
            if (closure != null) {
                closure.closureParams?.closureParamList?.forEach { param ->
                    val idNode = param.pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
                    if (idNode?.text == name) {
                        return idNode.psi
                    }
                }
                closureParent = closure.parent
            } else {
                break
            }
        }

        // Check for loop variables
        var forParent: PsiElement? = sourceElement
        while (forParent != null) {
            val forStmt = PsiTreeUtil.getParentOfType(forParent, RhaiForStatement::class.java, false)
            if (forStmt != null) {
                val idNode = forStmt.pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
                if (idNode?.text == name) {
                    return idNode.psi
                }
                forParent = forStmt.parent
            } else {
                break
            }
        }

        // Check module declarations
        PsiTreeUtil.findChildrenOfType(file, RhaiModuleDeclaration::class.java).forEach { moduleDef ->
            val idNode = moduleDef.node.findChildByType(RhaiTypes.IDENTIFIER)
            if (idNode?.text == name) {
                return idNode.psi
            }
        }

        return null
    }

    private fun findAllUsages(file: RhaiFile, name: String, sourceElement: PsiElement): List<PsiElement> {
        val usages = mutableListOf<PsiElement>()
        val declaration = findDeclaration(file, name, sourceElement)
        val declarationOffset = declaration?.textOffset ?: sourceElement.textOffset

        // Determine scope: if declaration is in a function, only search within that function
        val scope = findScope(declaration ?: sourceElement, file)

        // Find all IDENTIFIER tokens with matching name
        scope.accept(object : com.intellij.psi.PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.elementType == RhaiTypes.IDENTIFIER && element.text == name) {
                    // Skip if this is a different declaration (e.g., shadowing)
                    if (!isShadowedDeclaration(element, declarationOffset)) {
                        // Skip property access (after dot)
                        if (!isPropertyAccess(element)) {
                            // Skip object field names
                            if (!isObjectFieldName(element)) {
                                usages.add(element)
                            }
                        }
                    }
                }
                super.visitElement(element)
            }
        })

        return usages
    }

    @Suppress("UNUSED_PARAMETER")
    private fun findScope(element: PsiElement, file: RhaiFile): PsiElement {
        // If element is inside a function, the scope is that function
        val funcDef = PsiTreeUtil.getParentOfType(element, RhaiFunctionDefinition::class.java)
        if (funcDef != null) {
            // But if it's a global variable/function, scope is the whole file
            val parent = element.parent
            if (parent is RhaiFunctionDefinition || parent is RhaiConstDeclaration) {
                return file
            }
            if (parent is RhaiPattern) {
                val grandparent = parent.parent
                if (grandparent is RhaiLetDeclaration || grandparent is RhaiConstDeclaration) {
                    // Check if this is a top-level declaration
                    if (isTopLevel(grandparent, file)) {
                        return file
                    }
                }
            }
            return funcDef
        }
        return file
    }

    private fun isTopLevel(element: PsiElement, file: RhaiFile): Boolean {
        var parent = element.parent
        while (parent != null && parent !is RhaiFile) {
            if (parent is RhaiFunctionDefinition || parent is RhaiClosureExpr) {
                return false
            }
            parent = parent.parent
        }
        return true
    }

    private fun isShadowedDeclaration(element: PsiElement, originalDeclOffset: Int): Boolean {
        val parent = element.parent
        if (parent is RhaiPattern) {
            val grandparent = parent.parent
            if ((grandparent is RhaiLetDeclaration || grandparent is RhaiConstDeclaration) &&
                element.textOffset != originalDeclOffset) {
                // This is a different declaration with the same name (shadowing)
                return true
            }
        }
        if (parent is RhaiParameter || parent is RhaiClosureParam) {
            if (element.textOffset != originalDeclOffset) {
                return true
            }
        }
        return false
    }

    private fun isPropertyAccess(element: PsiElement): Boolean {
        var sibling = element.prevSibling
        while (sibling != null && sibling.elementType == com.intellij.psi.TokenType.WHITE_SPACE) {
            sibling = sibling.prevSibling
        }
        return sibling?.elementType == RhaiTypes.DOT
    }

    private fun isObjectFieldName(element: PsiElement): Boolean {
        var sibling = element.nextSibling
        while (sibling != null && sibling.elementType == com.intellij.psi.TokenType.WHITE_SPACE) {
            sibling = sibling.nextSibling
        }
        if (sibling?.elementType == RhaiTypes.COLON) {
            var parent = element.parent
            while (parent != null) {
                if (parent is RhaiObjectLiteral || parent is RhaiObjectField) {
                    return true
                }
                if (parent is RhaiBlock || parent is RhaiFunctionDefinition) {
                    break
                }
                parent = parent.parent
            }
        }
        return false
    }
}

/**
 * Simple reference implementation for rename refactoring.
 */
private class RhaiSimpleReference(
    private val element: PsiElement,
    private val target: PsiElement
) : PsiReference {
    override fun getElement(): PsiElement = element
    override fun getRangeInElement() = com.intellij.openapi.util.TextRange(0, element.textLength)
    override fun resolve(): PsiElement = target
    override fun getCanonicalText(): String = element.text
    override fun handleElementRename(newElementName: String): PsiElement {
        val factory = RhaiPsiFactory(element.project)
        val newIdentifier = factory.createIdentifier(newElementName)
        return element.replace(newIdentifier)
    }
    override fun bindToElement(element: PsiElement): PsiElement = this.element
    override fun isReferenceTo(element: PsiElement): Boolean = element == target
    override fun isSoft(): Boolean = false
}
