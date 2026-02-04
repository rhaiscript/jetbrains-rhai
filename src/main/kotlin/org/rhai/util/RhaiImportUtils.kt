package org.rhai.util

import com.intellij.psi.util.PsiTreeUtil
import org.rhai.RhaiImportStatement
import org.rhai.RhaiTypes
import org.rhai.lang.RhaiFile

/**
 * Utility functions for working with Rhai imports.
 * Centralizes import-related logic to avoid code duplication.
 */
object RhaiImportUtils {

    /**
     * Find the appropriate offset to insert a new import statement.
     * Imports are inserted after existing imports, or after module-level doc comments.
     */
    fun findImportInsertOffset(file: RhaiFile): Int {
        val imports = PsiTreeUtil.findChildrenOfType(file, RhaiImportStatement::class.java)
        return findImportInsertOffset(file, imports)
    }

    /**
     * Find the appropriate offset to insert a new import statement.
     * Uses pre-fetched imports collection for efficiency.
     */
    fun findImportInsertOffset(file: RhaiFile, imports: Collection<RhaiImportStatement>): Int {
        val lastImport = imports.maxByOrNull { it.textOffset }
        if (lastImport != null) {
            return lastImport.textRange.endOffset + 1
        }

        // Skip module-level doc comments (//!) and empty lines at the start
        val text = file.text
        var offset = 0
        val lines = text.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("//!") || trimmed.isEmpty()) {
                offset += line.length + 1  // +1 for newline
            } else {
                break
            }
        }
        return offset
    }

    /**
     * Build an import statement string.
     * @param importPath The module path (e.g., "utils", "../lib/helpers")
     * @param alias The alias to use (e.g., "utils", "helpers")
     * @return The complete import statement with newline
     */
    fun buildImportStatement(importPath: String, alias: String): String {
        return "import \"$importPath\" as $alias;\n"
    }

    /**
     * Check if an import with the given path or alias already exists.
     * Returns true if there's a conflict (same path OR same alias).
     */
    fun hasImportConflict(file: RhaiFile, importPath: String, alias: String): Boolean {
        val imports = PsiTreeUtil.findChildrenOfType(file, RhaiImportStatement::class.java)
        return imports.any { importStmt ->
            val pathText = importStmt.importPath?.text?.removeSurrounding("\"") ?: ""
            val aliasNode = importStmt.node.findChildByType(RhaiTypes.IDENTIFIER)
            val existingAlias = aliasNode?.text ?: ""

            // Conflict if same path OR same alias (either would cause issues)
            pathText == importPath || existingAlias == alias
        }
    }

    /**
     * Find an existing import for the given path.
     * @return The import statement if found, null otherwise
     */
    fun findImportByPath(file: RhaiFile, importPath: String): RhaiImportStatement? {
        val imports = PsiTreeUtil.findChildrenOfType(file, RhaiImportStatement::class.java)
        return imports.find { importStmt ->
            val pathText = importStmt.importPath?.text?.removeSurrounding("\"") ?: ""
            pathText == importPath
        }
    }

    /**
     * Find an existing import alias for the given path by searching the document text.
     * This is more reliable than PSI-based search when PSI might be out of sync.
     * @return The alias if found, null otherwise
     */
    fun findImportAliasInText(fileText: String, importPath: String): String? {
        // Pattern: import "path" as alias;
        val pattern = Regex("""import\s+"${Regex.escape(importPath)}"\s+as\s+(\w+)\s*;""")
        val match = pattern.find(fileText)
        return match?.groupValues?.getOrNull(1)
    }

    /**
     * Check if an import for the given path exists in the document text.
     */
    fun hasImportInText(fileText: String, importPath: String): Boolean {
        val pattern = Regex("""import\s+"${Regex.escape(importPath)}"\s+as\s+\w+\s*;""")
        return pattern.containsMatchIn(fileText)
    }

    /**
     * Get the alias from an import statement.
     * @return The alias text, or null if not found
     */
    fun getImportAlias(importStmt: RhaiImportStatement): String? {
        return importStmt.node.findChildByType(RhaiTypes.IDENTIFIER)?.text
    }
}
