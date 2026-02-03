package org.rhai.settings

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

/**
 * Custom code style settings for Rhai language
 */
class RhaiCodeStyleSettings(container: CodeStyleSettings) :
    CustomCodeStyleSettings("RhaiCodeStyleSettings", container) {

    // Spacing options
    @JvmField var SPACE_AROUND_ASSIGNMENT_OPERATORS = true
    @JvmField var SPACE_AROUND_LOGICAL_OPERATORS = true
    @JvmField var SPACE_AROUND_EQUALITY_OPERATORS = true
    @JvmField var SPACE_AROUND_RELATIONAL_OPERATORS = true
    @JvmField var SPACE_AROUND_ADDITIVE_OPERATORS = true
    @JvmField var SPACE_AROUND_MULTIPLICATIVE_OPERATORS = true
    @JvmField var SPACE_AROUND_RANGE_OPERATOR = false
    @JvmField var SPACE_AROUND_ARROW_OPERATOR = true

    @JvmField var SPACE_AFTER_COMMA = true
    @JvmField var SPACE_BEFORE_COMMA = false
    @JvmField var SPACE_AFTER_COLON = true
    @JvmField var SPACE_BEFORE_COLON = false
    @JvmField var SPACE_AFTER_SEMICOLON = true
    @JvmField var SPACE_BEFORE_SEMICOLON = false

    // Brace placement
    @JvmField var SPACE_BEFORE_FUNCTION_LBRACE = true
    @JvmField var SPACE_BEFORE_IF_LBRACE = true
    @JvmField var SPACE_BEFORE_LOOP_LBRACE = true
    @JvmField var SPACE_BEFORE_SWITCH_LBRACE = true

    // Parentheses
    @JvmField var SPACE_WITHIN_PARENTHESES = false
    @JvmField var SPACE_WITHIN_BRACKETS = false
    @JvmField var SPACE_WITHIN_BRACES = false
    @JvmField var SPACE_BEFORE_FUNCTION_PARENTHESES = false
    @JvmField var SPACE_BEFORE_IF_PARENTHESES = true

    // Closure
    @JvmField var SPACE_WITHIN_CLOSURE_PIPES = false
    @JvmField var SPACE_AROUND_CLOSURE_ARROW = true

    // Blank lines
    @JvmField var BLANK_LINES_AFTER_IMPORTS = 1
    @JvmField var BLANK_LINES_AROUND_FUNCTION = 1
    @JvmField var BLANK_LINES_AROUND_MODULE = 1

    // Wrapping options
    @JvmField var ALIGN_MULTILINE_PARAMETERS = true
    @JvmField var ALIGN_MULTILINE_ARGUMENTS = true
    @JvmField var ALIGN_MULTILINE_ARRAY_ELEMENTS = true
    @JvmField var ALIGN_MULTILINE_OBJECT_FIELDS = true

    // Other
    @JvmField var KEEP_TRAILING_COMMA = true
    @JvmField var INSERT_SEMICOLONS = true
}
