package org.rhai.refactoring

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project

/**
 * Validates identifier names in Rhai.
 * Used by rename refactoring to check if new names are valid.
 */
class RhaiNamesValidator : NamesValidator {

    private val keywords = setOf(
        "let", "const", "fn", "if", "else", "while", "for", "in",
        "loop", "break", "continue", "return", "throw", "try", "catch",
        "switch", "default", "import", "export", "module", "private", "pub",
        "true", "false", "null", "this", "global", "is", "as", "do", "until",
        "shared", "sync", "async", "await", "inf", "NaN", "undefined"
    )

    override fun isKeyword(name: String, project: Project?): Boolean {
        return keywords.contains(name)
    }

    override fun isIdentifier(name: String, project: Project?): Boolean {
        if (name.isEmpty()) return false

        // Rhai identifiers: start with letter or underscore, followed by letters, digits, or underscores
        val firstChar = name[0]
        if (!firstChar.isLetter() && firstChar != '_') return false

        return name.all { it.isLetterOrDigit() || it == '_' }
    }
}
