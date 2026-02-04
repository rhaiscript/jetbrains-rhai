package org.rhai.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.rhai.features.RhaiProjectSymbolsProvider
import org.rhai.lang.RhaiFile
import org.rhai.util.RhaiImportUtils

/**
 * Quick fix that adds an import statement for an unresolved symbol.
 * Triggered via Alt+Enter (Cmd+Enter on macOS).
 */
class RhaiImportQuickFix(
    private val symbolName: String,
    private val sourceFile: VirtualFile,
    private val moduleName: String,
    private val isFunction: Boolean
) : LocalQuickFix {

    override fun getName(): String {
        val symbolType = if (isFunction) "function" else "symbol"
        return "Import $symbolType '$symbolName' from '$moduleName'"
    }

    override fun getFamilyName(): String = "Import symbol"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val file = element.containingFile as? RhaiFile ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
        val currentFile = file.virtualFile ?: return

        // Calculate relative import path
        val importPath = RhaiProjectSymbolsProvider.getRelativeImportPath(currentFile, sourceFile)

        // Check if this import already exists
        if (RhaiImportUtils.hasImportConflict(file, importPath, moduleName)) {
            return  // Import already exists or conflicts
        }

        // Find the appropriate position for the import
        val insertOffset = RhaiImportUtils.findImportInsertOffset(file)

        // Build and insert the import statement
        val importStatement = RhaiImportUtils.buildImportStatement(importPath, moduleName)
        document.insertString(insertOffset, importStatement)
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }

    companion object {
        /**
         * Create quick fixes for importing a symbol from available sources.
         */
        fun createQuickFixes(
            element: PsiElement,
            symbolName: String,
            isFunction: Boolean
        ): List<RhaiImportQuickFix> {
            val file = element.containingFile as? RhaiFile ?: return emptyList()
            val virtualFile = file.virtualFile ?: return emptyList()
            val project = element.project

            val symbols = RhaiProjectSymbolsProvider.findExportedSymbols(project, symbolName, virtualFile)

            return symbols.map { symbol ->
                RhaiImportQuickFix(
                    symbolName,
                    symbol.file,
                    symbol.moduleName,
                    symbol.kind == RhaiProjectSymbolsProvider.SymbolKind.FUNCTION
                )
            }
        }
    }
}

/**
 * Quick fix that adds an import and updates the reference to use module prefix.
 */
class RhaiImportAndPrefixQuickFix(
    private val symbolName: String,
    private val sourceFile: VirtualFile,
    private val moduleName: String,
    private val isFunction: Boolean
) : LocalQuickFix {

    override fun getName(): String {
        return "Import and use as '$moduleName::$symbolName'"
    }

    override fun getFamilyName(): String = "Import and prefix symbol"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val file = element.containingFile as? RhaiFile ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
        val currentFile = file.virtualFile ?: return

        // Calculate relative import path
        val importPath = RhaiProjectSymbolsProvider.getRelativeImportPath(currentFile, sourceFile)

        // Find existing import
        val existingImport = RhaiImportUtils.findImportByPath(file, importPath)

        // Save element position BEFORE any modifications
        val elementOffset = element.textOffset
        val elementLength = element.textLength

        var adjustment = 0
        val actualModuleName = if (existingImport != null) {
            // Get the alias from existing import
            RhaiImportUtils.getImportAlias(existingImport) ?: moduleName
        } else {
            // Add new import
            val insertOffset = RhaiImportUtils.findImportInsertOffset(file)
            val importStatement = RhaiImportUtils.buildImportStatement(importPath, moduleName)
            document.insertString(insertOffset, importStatement)

            // Only adjust if import was inserted BEFORE the element
            if (insertOffset <= elementOffset) {
                adjustment = importStatement.length
            }
            moduleName
        }

        // Replace the identifier with module::identifier
        val newReference = "$actualModuleName::$symbolName"
        document.replaceString(elementOffset + adjustment, elementOffset + adjustment + elementLength, newReference)

        PsiDocumentManager.getInstance(project).commitDocument(document)
    }

    companion object {
        fun createQuickFixes(
            element: PsiElement,
            symbolName: String,
            isFunction: Boolean
        ): List<RhaiImportAndPrefixQuickFix> {
            val file = element.containingFile as? RhaiFile ?: return emptyList()
            val virtualFile = file.virtualFile ?: return emptyList()
            val project = element.project

            val symbols = RhaiProjectSymbolsProvider.findExportedSymbols(project, symbolName, virtualFile)

            return symbols.map { symbol ->
                RhaiImportAndPrefixQuickFix(
                    symbolName,
                    symbol.file,
                    symbol.moduleName,
                    symbol.kind == RhaiProjectSymbolsProvider.SymbolKind.FUNCTION
                )
            }
        }
    }
}
