package org.rhai.refactoring

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.rhai.RhaiLetDeclaration
import org.rhai.RhaiTypes
import org.rhai.lang.RhaiFile
import org.rhai.lang.RhaiFileType

/**
 * Factory for creating Rhai PSI elements.
 * Used by refactoring operations to create new elements.
 */
class RhaiPsiFactory(private val project: Project) {

    /**
     * Create an identifier element with the given name.
     */
    fun createIdentifier(name: String): PsiElement {
        // Create a simple let declaration to extract the identifier
        val file = createFile("let $name = 0;")
        val letDecl = PsiTreeUtil.findChildOfType(file, RhaiLetDeclaration::class.java)
            ?: error("Failed to create identifier PSI element")

        val identifierNode = letDecl.pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
            ?: error("Failed to find identifier in created element")

        return identifierNode.psi
    }

    /**
     * Create a Rhai file with the given content.
     */
    fun createFile(content: String): RhaiFile {
        return PsiFileFactory.getInstance(project)
            .createFileFromText("dummy.rhai", RhaiFileType.INSTANCE, content) as RhaiFile
    }
}
