package org.rhai.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.parser.GeneratedParserUtilBase.DUMMY_BLOCK
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.startOffset
import org.rhai.*
import org.rhai.lang.RhaiFile
import org.rhai.psi.*


class RhaiGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (element == null) return null

        // Проверяем, что это идентификатор (LeafPsiElement с типом IDENTIFIER)
        if (element.elementType != RhaiTypes.IDENTIFIER) return null

        val name = element.text
        val file = element.containingFile as? RhaiFile ?: return null

        val targets = mutableListOf<PsiElement>()

        // 1. Ищем функции
        findAllFunctions(file, element, targets)

        // 2. Ищем переменные (если не функция)
        if (targets.isEmpty()) {
            findAllVariables(file, element, name, targets)
        }

        // 3. Ищем модули
        if (targets.isEmpty()) {
            findAllModules(file, name, targets)
        }

        return targets.takeIf { it.isNotEmpty() }?.toTypedArray()
    }

    private fun findAllFunctions(file: RhaiFile, targetElement: PsiElement, targets: MutableList<PsiElement>) {
        // Обходим все элементы файла
        file.accept(object : com.intellij.psi.PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)

                if (element.elementType == DUMMY_BLOCK) {
                    val idNode = element.node.findChildByType(RhaiTypes.IDENTIFIER)
                    if (idNode?.text == targetElement.text) {
                        if (idNode != null) {
                            if (targetElement.startOffset != idNode.startOffset) {
                                targets.add(idNode.psi)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun findAllVariables(
        file: RhaiFile,
        reference: PsiElement,
        name: String,
        targets: MutableList<PsiElement>
    ) {
        file.accept(object : com.intellij.psi.PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)

                val typeName = element.elementType?.toString()?.lowercase() ?: ""

                if (typeName.contains("let_declaration") || typeName.contains("const_declaration") ||
                    element.javaClass.simpleName.contains("LetDeclaration") ||
                    element.javaClass.simpleName.contains("ConstDeclaration")
                ) {
                    if (PsiTreeUtil.isAncestor(element, reference, false)) return

                    val pattern = element.node.findChildByType(RhaiTypes.PATTERN)
                    val idNode = pattern?.findChildByType(RhaiTypes.IDENTIFIER)

                    if (idNode?.text == name) {
                        targets.add(idNode.psi)
                    }
                }

                if (typeName.contains("parameter") || element.javaClass.simpleName.contains("Parameter")) {
                    val idNode = element.node.findChildByType(RhaiTypes.IDENTIFIER)
                    if (idNode?.text == name) {
                        targets.add(idNode.psi)
                    }
                }
            }
        })
    }

    private fun findAllModules(file: RhaiFile, name: String, targets: MutableList<PsiElement>) {
        file.accept(object : com.intellij.psi.PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)

                if (element.elementType?.toString()?.lowercase()?.contains("module_declaration") == true ||
                    element.javaClass.simpleName.contains("ModuleDeclaration")
                ) {
                    val idNode = element.node.findChildByType(RhaiTypes.IDENTIFIER)
                    if (idNode?.text == name) {
                        targets.add(idNode.psi)
                    }
                }
            }
        })
    }
}