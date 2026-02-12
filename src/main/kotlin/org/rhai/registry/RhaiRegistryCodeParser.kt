package org.rhai.registry

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.rhai.RhaiConstDeclaration
import org.rhai.RhaiFunctionDefinition
import org.rhai.RhaiLetDeclaration
import org.rhai.RhaiTypes
import org.rhai.lang.RhaiFile
import org.rhai.lang.RhaiFileType
import java.util.concurrent.ConcurrentHashMap

/**
 * Data class for parsed registry entry with metadata
 */
data class ParsedRegistryEntry(
    val name: String,
    val kind: RegistryEntryKind,
    val signature: String? = null,
    val documentation: String? = null
)

enum class RegistryEntryKind {
    FUNCTION,
    VARIABLE,
    CONSTANT,
    TYPE
}

/**
 * Cached parse result
 */
data class ParsedRegistryCode(
    val functions: Set<ParsedRegistryEntry>,
    val variables: Set<ParsedRegistryEntry>,
    val constants: Set<ParsedRegistryEntry>,
    val types: Set<String>,
    val sourceHash: Int
)

/**
 * Service for parsing Rhai code strings and extracting declarations.
 * Used to parse registry code (global and project scope) into functions/variables/types.
 */
@Service(Service.Level.PROJECT)
class RhaiRegistryCodeParser(private val project: Project) {

    private val cache = ConcurrentHashMap<Int, ParsedRegistryCode>()

    /**
     * Parse Rhai code string and extract all declarations
     */
    fun parseRhaiCode(code: String): ParsedRegistryCode {
        if (code.isBlank()) {
            return ParsedRegistryCode(emptySet(), emptySet(), emptySet(), emptySet(), 0)
        }

        val hash = code.hashCode()

        // Check cache
        cache[hash]?.let { return it }

        val functions = mutableSetOf<ParsedRegistryEntry>()
        val variables = mutableSetOf<ParsedRegistryEntry>()
        val constants = mutableSetOf<ParsedRegistryEntry>()
        val types = mutableSetOf<String>()

        try {
            // Create PSI file from code string
            val psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText("registry.rhai", RhaiFileType.INSTANCE, code) as? RhaiFile

            if (psiFile != null) {
                // Extract function definitions
                PsiTreeUtil.findChildrenOfType(psiFile, RhaiFunctionDefinition::class.java).forEach { func ->
                    val name = func.identifier.text
                    val params = func.parameters?.parameterList?.joinToString(", ") { param ->
                        val paramName = param.pattern.text
                        val typeAnnotation = param.typeAnnotation?.text?.removePrefix(":")?.trim()
                        if (typeAnnotation != null) "$paramName: $typeAnnotation" else paramName
                    } ?: ""
                    val returnType = func.returnType?.text?.removePrefix("->")?.trim()

                    val signature = "($params)${returnType?.let { " -> $it" } ?: ""}"
                    val doc = extractDocComment(func)

                    functions.add(ParsedRegistryEntry(name, RegistryEntryKind.FUNCTION, signature, doc))
                }

                // Extract let declarations (variables)
                PsiTreeUtil.findChildrenOfType(psiFile, RhaiLetDeclaration::class.java).forEach { letDecl ->
                    val idNode = letDecl.pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
                    val name = idNode?.text ?: letDecl.pattern.text
                    val typeAnnotation = letDecl.typeAnnotation?.text?.removePrefix(":")?.trim()
                    variables.add(ParsedRegistryEntry(name, RegistryEntryKind.VARIABLE, typeAnnotation))
                }

                // Extract const declarations
                PsiTreeUtil.findChildrenOfType(psiFile, RhaiConstDeclaration::class.java).forEach { constDecl ->
                    val idNode = constDecl.pattern.node.findChildByType(RhaiTypes.IDENTIFIER)
                    val name = idNode?.text ?: constDecl.pattern.text
                    val typeAnnotation = constDecl.typeAnnotation?.text?.removePrefix(":")?.trim()
                    constants.add(ParsedRegistryEntry(name, RegistryEntryKind.CONSTANT, typeAnnotation))
                }
            }
        } catch (e: Exception) {
            // Ignore parsing errors - just return empty results for unparseable code
        }

        // Extract type declarations from special comments: // type: TypeName
        val typePattern = Regex("""//\s*type:\s*(\w+)""")
        typePattern.findAll(code).forEach { match ->
            types.add(match.groupValues[1])
        }

        val result = ParsedRegistryCode(functions, variables, constants, types, hash)
        cache[hash] = result
        return result
    }

    /**
     * Extract documentation comment from a PSI element
     */
    private fun extractDocComment(element: com.intellij.psi.PsiElement): String? {
        var sibling = element.prevSibling
        val docLines = mutableListOf<String>()

        while (sibling != null) {
            val elementType = sibling.node.elementType
            when {
                elementType == RhaiTypes.DOC_LINE || elementType == RhaiTypes.DOC_COMMENT -> {
                    docLines.add(0, sibling.text.removePrefix("///").removePrefix("//!").trim())
                }
                elementType == com.intellij.psi.TokenType.WHITE_SPACE -> {
                    // Continue looking
                }
                else -> break
            }
            sibling = sibling.prevSibling
        }

        return if (docLines.isNotEmpty()) docLines.joinToString("\n") else null
    }

    /**
     * Invalidate all cached results
     */
    fun invalidateCache() {
        cache.clear()
    }

    companion object {
        fun getInstance(project: Project): RhaiRegistryCodeParser {
            return project.getService(RhaiRegistryCodeParser::class.java)
        }
    }
}
