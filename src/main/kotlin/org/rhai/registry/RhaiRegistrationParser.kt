package org.rhai.registry

/**
 * Data class representing a parsed Rhai registration
 */
data class RhaiRegistration(
    val name: String,
    val type: RegistrationType,
    val signature: String? = null,
    val sourceFile: String? = null,
    val lineNumber: Int? = null
)

enum class RegistrationType {
    FUNCTION,
    VARIABLE,
    TYPE,
    MODULE,
    GETTER,
    SETTER,
    INDEXER,
    OPERATOR
}

/**
 * Parser for Rhai function/variable registrations in Rust code.
 *
 * Supports patterns like:
 * - engine.register_fn("name", function)
 * - engine.register_fn(NAME_CONST, function)
 * - engine.register_type::<Type>()
 * - engine.register_get("prop", getter)
 * - engine.register_set("prop", setter)
 * - module.set_fn("name", function)
 */
object RhaiRegistrationParser {

    // Regex patterns for different registration methods
    private val REGISTER_FN_PATTERN = Regex(
        """\.register_fn\s*\(\s*"([^"]+)"\s*,""",
        RegexOption.MULTILINE
    )

    private val REGISTER_FN_VAR_PATTERN = Regex(
        """\.register_fn\s*\(\s*([A-Z_][A-Z0-9_]*)\s*,""",
        RegexOption.MULTILINE
    )

    private val REGISTER_TYPE_PATTERN = Regex(
        """\.register_type(?:_with_name)?\s*::\s*<\s*(\w+)\s*>\s*\(\s*\)""",
        RegexOption.MULTILINE
    )

    private val REGISTER_TYPE_WITH_NAME_PATTERN = Regex(
        """\.register_type_with_name\s*::\s*<\s*\w+\s*>\s*\(\s*"([^"]+)"\s*\)""",
        RegexOption.MULTILINE
    )

    private val REGISTER_GET_PATTERN = Regex(
        """\.register_get\s*\(\s*"([^"]+)"\s*,""",
        RegexOption.MULTILINE
    )

    private val REGISTER_SET_PATTERN = Regex(
        """\.register_set\s*\(\s*"([^"]+)"\s*,""",
        RegexOption.MULTILINE
    )

    private val REGISTER_GET_SET_PATTERN = Regex(
        """\.register_get_set\s*\(\s*"([^"]+)"\s*,""",
        RegexOption.MULTILINE
    )

    private val MODULE_SET_FN_PATTERN = Regex(
        """\.set_fn\s*\(\s*"([^"]+)"\s*,""",
        RegexOption.MULTILINE
    )

    private val MODULE_SET_VAR_PATTERN = Regex(
        """\.set_var\s*\(\s*"([^"]+)"\s*,""",
        RegexOption.MULTILINE
    )

    private val CONST_DEFINITION_PATTERN = Regex(
        """(?:const|static)\s+([A-Z_][A-Z0-9_]*)\s*:\s*&?\s*str\s*=\s*"([^"]+)"""",
        RegexOption.MULTILINE
    )

    private val LET_STRING_PATTERN = Regex(
        """let\s+(\w+)\s*(?::\s*&?\s*str)?\s*=\s*"([^"]+)"""",
        RegexOption.MULTILINE
    )

    /**
     * Parse Rust source code and extract Rhai registrations
     */
    fun parseRustSource(
        source: String,
        fileName: String? = null
    ): List<RhaiRegistration> {
        val registrations = mutableListOf<RhaiRegistration>()

        // First, extract constant definitions for variable resolution
        val constants = extractConstants(source)

        // Parse register_fn with string literal
        REGISTER_FN_PATTERN.findAll(source).forEach { match ->
            val name = match.groupValues[1]
            val lineNumber = source.substring(0, match.range.first).count { it == '\n' } + 1
            registrations.add(
                RhaiRegistration(
                    name = name,
                    type = RegistrationType.FUNCTION,
                    sourceFile = fileName,
                    lineNumber = lineNumber
                )
            )
        }

        // Parse register_fn with constant variable
        REGISTER_FN_VAR_PATTERN.findAll(source).forEach { match ->
            val constName = match.groupValues[1]
            val resolvedName = constants[constName]
            if (resolvedName != null) {
                val lineNumber = source.substring(0, match.range.first).count { it == '\n' } + 1
                registrations.add(
                    RhaiRegistration(
                        name = resolvedName,
                        type = RegistrationType.FUNCTION,
                        sourceFile = fileName,
                        lineNumber = lineNumber
                    )
                )
            }
        }

        // Parse register_type
        REGISTER_TYPE_PATTERN.findAll(source).forEach { match ->
            val typeName = match.groupValues[1]
            val lineNumber = source.substring(0, match.range.first).count { it == '\n' } + 1
            registrations.add(
                RhaiRegistration(
                    name = typeName,
                    type = RegistrationType.TYPE,
                    sourceFile = fileName,
                    lineNumber = lineNumber
                )
            )
        }

        // Parse register_type_with_name
        REGISTER_TYPE_WITH_NAME_PATTERN.findAll(source).forEach { match ->
            val typeName = match.groupValues[1]
            val lineNumber = source.substring(0, match.range.first).count { it == '\n' } + 1
            registrations.add(
                RhaiRegistration(
                    name = typeName,
                    type = RegistrationType.TYPE,
                    sourceFile = fileName,
                    lineNumber = lineNumber
                )
            )
        }

        // Parse register_get (property getter)
        REGISTER_GET_PATTERN.findAll(source).forEach { match ->
            val propName = match.groupValues[1]
            val lineNumber = source.substring(0, match.range.first).count { it == '\n' } + 1
            registrations.add(
                RhaiRegistration(
                    name = propName,
                    type = RegistrationType.GETTER,
                    sourceFile = fileName,
                    lineNumber = lineNumber
                )
            )
        }

        // Parse register_set (property setter)
        REGISTER_SET_PATTERN.findAll(source).forEach { match ->
            val propName = match.groupValues[1]
            val lineNumber = source.substring(0, match.range.first).count { it == '\n' } + 1
            registrations.add(
                RhaiRegistration(
                    name = propName,
                    type = RegistrationType.SETTER,
                    sourceFile = fileName,
                    lineNumber = lineNumber
                )
            )
        }

        // Parse register_get_set (both getter and setter)
        REGISTER_GET_SET_PATTERN.findAll(source).forEach { match ->
            val propName = match.groupValues[1]
            val lineNumber = source.substring(0, match.range.first).count { it == '\n' } + 1
            registrations.add(
                RhaiRegistration(
                    name = propName,
                    type = RegistrationType.GETTER,
                    sourceFile = fileName,
                    lineNumber = lineNumber
                )
            )
            registrations.add(
                RhaiRegistration(
                    name = propName,
                    type = RegistrationType.SETTER,
                    sourceFile = fileName,
                    lineNumber = lineNumber
                )
            )
        }

        // Parse module.set_fn
        MODULE_SET_FN_PATTERN.findAll(source).forEach { match ->
            val name = match.groupValues[1]
            val lineNumber = source.substring(0, match.range.first).count { it == '\n' } + 1
            registrations.add(
                RhaiRegistration(
                    name = name,
                    type = RegistrationType.FUNCTION,
                    sourceFile = fileName,
                    lineNumber = lineNumber
                )
            )
        }

        // Parse module.set_var
        MODULE_SET_VAR_PATTERN.findAll(source).forEach { match ->
            val name = match.groupValues[1]
            val lineNumber = source.substring(0, match.range.first).count { it == '\n' } + 1
            registrations.add(
                RhaiRegistration(
                    name = name,
                    type = RegistrationType.VARIABLE,
                    sourceFile = fileName,
                    lineNumber = lineNumber
                )
            )
        }

        return registrations
    }

    /**
     * Extract constant and let definitions for variable resolution
     */
    private fun extractConstants(source: String): Map<String, String> {
        val constants = mutableMapOf<String, String>()

        // Extract const/static definitions
        CONST_DEFINITION_PATTERN.findAll(source).forEach { match ->
            val constName = match.groupValues[1]
            val value = match.groupValues[2]
            constants[constName] = value
        }

        // Extract let definitions (for local variables used in registration)
        LET_STRING_PATTERN.findAll(source).forEach { match ->
            val varName = match.groupValues[1]
            val value = match.groupValues[2]
            // Only add if it looks like a constant (all caps with underscores)
            if (varName.matches(Regex("[a-z_][a-z0-9_]*"))) {
                constants[varName] = value
            }
        }

        return constants
    }

    /**
     * Parse a single line and try to extract registration
     */
    fun parseSingleLine(line: String): RhaiRegistration? {
        // Try register_fn pattern
        REGISTER_FN_PATTERN.find(line)?.let { match ->
            return RhaiRegistration(
                name = match.groupValues[1],
                type = RegistrationType.FUNCTION
            )
        }

        // Try module.set_fn pattern
        MODULE_SET_FN_PATTERN.find(line)?.let { match ->
            return RhaiRegistration(
                name = match.groupValues[1],
                type = RegistrationType.FUNCTION
            )
        }

        // Try register_type pattern
        REGISTER_TYPE_PATTERN.find(line)?.let { match ->
            return RhaiRegistration(
                name = match.groupValues[1],
                type = RegistrationType.TYPE
            )
        }

        // Try register_type_with_name pattern
        REGISTER_TYPE_WITH_NAME_PATTERN.find(line)?.let { match ->
            return RhaiRegistration(
                name = match.groupValues[1],
                type = RegistrationType.TYPE
            )
        }

        // Try register_get pattern
        REGISTER_GET_PATTERN.find(line)?.let { match ->
            return RhaiRegistration(
                name = match.groupValues[1],
                type = RegistrationType.GETTER
            )
        }

        // Try register_set pattern
        REGISTER_SET_PATTERN.find(line)?.let { match ->
            return RhaiRegistration(
                name = match.groupValues[1],
                type = RegistrationType.SETTER
            )
        }

        // Try module.set_var pattern
        MODULE_SET_VAR_PATTERN.find(line)?.let { match ->
            return RhaiRegistration(
                name = match.groupValues[1],
                type = RegistrationType.VARIABLE
            )
        }

        return null
    }

    /**
     * Validate if a name is a valid Rhai identifier
     */
    fun isValidRhaiIdentifier(name: String): Boolean {
        if (name.isEmpty()) return false
        if (name.first().isDigit()) return false
        return name.all { it.isLetterOrDigit() || it == '_' }
    }
}
