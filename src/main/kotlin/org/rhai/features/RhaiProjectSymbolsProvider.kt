package org.rhai.features

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.rhai.RhaiClosureExpr
import org.rhai.RhaiConstDeclaration
import org.rhai.RhaiFunctionDefinition
import org.rhai.RhaiLetDeclaration
import org.rhai.RhaiTypes
import org.rhai.lang.RhaiFile
import org.rhai.lang.RhaiFileType

/**
 * Provides access to exported symbols (functions, constants, variables) from all Rhai files in the project.
 * Used for cross-file completion and import suggestions.
 */
object RhaiProjectSymbolsProvider {

    data class ExportedSymbol(
        val name: String,
        val kind: SymbolKind,
        val file: VirtualFile,
        val moduleName: String,  // Suggested module alias (file name without extension)
        val isPublic: Boolean
    )

    enum class SymbolKind {
        FUNCTION,
        CONSTANT,
        VARIABLE
    }

    /**
     * Find all exported symbols matching the given name across all project Rhai files.
     * @param project The current project
     * @param name The symbol name to search for
     * @param excludeFile File to exclude from search (usually the current file)
     * @return List of matching exported symbols
     */
    fun findExportedSymbols(project: Project, name: String, excludeFile: VirtualFile?): List<ExportedSymbol> {
        val results = mutableListOf<ExportedSymbol>()
        val psiManager = PsiManager.getInstance(project)

        val rhaiFiles = FileTypeIndex.getFiles(RhaiFileType.INSTANCE, GlobalSearchScope.projectScope(project))

        for (virtualFile in rhaiFiles) {
            if (virtualFile == excludeFile) continue

            val psiFile = psiManager.findFile(virtualFile) as? RhaiFile ?: continue
            val moduleName = virtualFile.nameWithoutExtension

            // Search for functions
            PsiTreeUtil.findChildrenOfType(psiFile, RhaiFunctionDefinition::class.java)
                .filter { it.identifier.text == name }
                .forEach { func ->
                    val isPublic = isPublicFunction(func)
                    results.add(ExportedSymbol(name, SymbolKind.FUNCTION, virtualFile, moduleName, isPublic))
                }

            // Search for constants
            PsiTreeUtil.findChildrenOfType(psiFile, RhaiConstDeclaration::class.java)
                .filter { it.node.findChildByType(RhaiTypes.IDENTIFIER)?.text == name }
                .forEach { constDecl ->
                    val isPublic = isPublicConst(constDecl)
                    results.add(ExportedSymbol(name, SymbolKind.CONSTANT, virtualFile, moduleName, isPublic))
                }

            // Search for top-level let declarations (global variables)
            PsiTreeUtil.findChildrenOfType(psiFile, RhaiLetDeclaration::class.java)
                .filter { isTopLevel(it, psiFile) && it.pattern.text == name }
                .forEach { letDecl ->
                    results.add(ExportedSymbol(name, SymbolKind.VARIABLE, virtualFile, moduleName, false))
                }
        }

        // Sort: public symbols first, then by module name
        return results.sortedWith(compareByDescending<ExportedSymbol> { it.isPublic }.thenBy { it.moduleName })
    }

    /**
     * Get all exported symbols from a specific file.
     */
    fun getFileExports(project: Project, virtualFile: VirtualFile): List<ExportedSymbol> {
        val results = mutableListOf<ExportedSymbol>()
        val psiManager = PsiManager.getInstance(project)
        val psiFile = psiManager.findFile(virtualFile) as? RhaiFile ?: return emptyList()
        val moduleName = virtualFile.nameWithoutExtension

        // Functions
        PsiTreeUtil.findChildrenOfType(psiFile, RhaiFunctionDefinition::class.java)
            .forEach { func ->
                val name = func.identifier.text
                val isPublic = isPublicFunction(func)
                results.add(ExportedSymbol(name, SymbolKind.FUNCTION, virtualFile, moduleName, isPublic))
            }

        // Constants
        PsiTreeUtil.findChildrenOfType(psiFile, RhaiConstDeclaration::class.java)
            .forEach { constDecl ->
                val name = constDecl.node.findChildByType(RhaiTypes.IDENTIFIER)?.text ?: return@forEach
                val isPublic = isPublicConst(constDecl)
                results.add(ExportedSymbol(name, SymbolKind.CONSTANT, virtualFile, moduleName, isPublic))
            }

        return results
    }

    /**
     * Get all Rhai files in the project that export the given symbol.
     */
    fun getFilesExportingSymbol(project: Project, name: String, excludeFile: VirtualFile?): List<VirtualFile> {
        return findExportedSymbols(project, name, excludeFile).map { it.file }.distinct()
    }

    private fun isPublicFunction(func: RhaiFunctionDefinition): Boolean {
        val funcText = func.text
        return funcText.trimStart().startsWith("pub ")
    }

    private fun isPublicConst(constDecl: RhaiConstDeclaration): Boolean {
        val constText = constDecl.text
        return constText.trimStart().startsWith("pub ")
    }

    private fun isTopLevel(letDecl: RhaiLetDeclaration, file: RhaiFile): Boolean {
        var parent = letDecl.parent
        while (parent != null && parent !is RhaiFile) {
            if (parent is RhaiFunctionDefinition || parent is RhaiClosureExpr) {
                return false  // Inside a function or closure - not top-level
            }
            parent = parent.parent
        }
        return true
    }

    /**
     * Calculate relative import path between two files.
     * Returns the path that should be used in import statement.
     */
    fun getRelativeImportPath(fromFile: VirtualFile, toFile: VirtualFile): String {
        val fromDir = fromFile.parent
        val toDir = toFile.parent

        // If in same directory, just use the file name without extension
        if (fromDir == toDir) {
            return toFile.nameWithoutExtension
        }

        // If either file has no parent directory, just return the file name
        if (fromDir == null || toDir == null) {
            return toFile.nameWithoutExtension
        }

        // Try to build relative path using VirtualFile API
        val fromPath = fromDir.path
        val toPath = toDir.path

        // Split paths (handle both / and \ separators)
        val separator = if (fromPath.contains("\\")) "\\" else "/"
        val fromParts = fromPath.split(separator).filter { it.isNotEmpty() }
        val toParts = toPath.split(separator).filter { it.isNotEmpty() }

        // Find common prefix
        var commonIndex = 0
        while (commonIndex < fromParts.size && commonIndex < toParts.size &&
            fromParts[commonIndex] == toParts[commonIndex]) {
            commonIndex++
        }

        val upCount = fromParts.size - commonIndex
        val downPath = toParts.drop(commonIndex)

        val relativePath = StringBuilder()
        repeat(upCount) { relativePath.append("../") }
        if (downPath.isNotEmpty()) {
            relativePath.append(downPath.joinToString("/"))
            relativePath.append("/")
        }
        relativePath.append(toFile.nameWithoutExtension)

        return relativePath.toString()
    }
}
