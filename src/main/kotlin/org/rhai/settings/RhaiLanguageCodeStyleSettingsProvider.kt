package org.rhai.settings

import com.intellij.application.options.IndentOptionsEditor
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import org.rhai.lang.RhaiLanguage

class RhaiLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun getLanguage(): Language = RhaiLanguage.INSTANCE

    override fun getCodeSample(settingsType: SettingsType): String {
        return when (settingsType) {
            SettingsType.INDENT_SETTINGS -> INDENT_SAMPLE
            SettingsType.SPACING_SETTINGS -> SPACING_SAMPLE
            SettingsType.WRAPPING_AND_BRACES_SETTINGS -> WRAPPING_SAMPLE
            SettingsType.BLANK_LINES_SETTINGS -> BLANK_LINES_SAMPLE
            else -> DEFAULT_SAMPLE
        }
    }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> {
                consumer.showStandardOptions(
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_AROUND_ASSIGNMENT_OPERATORS.name,
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_AROUND_LOGICAL_OPERATORS.name,
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_AROUND_EQUALITY_OPERATORS.name,
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_AROUND_RELATIONAL_OPERATORS.name,
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_AROUND_ADDITIVE_OPERATORS.name,
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_AROUND_MULTIPLICATIVE_OPERATORS.name,
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_AFTER_COMMA.name,
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_BEFORE_COMMA.name,
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_AFTER_COLON.name,
                    CodeStyleSettingsCustomizable.SpacingOption.SPACE_BEFORE_COLON.name
                )

                consumer.showCustomOption(
                    RhaiCodeStyleSettings::class.java,
                    "SPACE_AROUND_RANGE_OPERATOR",
                    "Around range operator (..)",
                    CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS
                )

                consumer.showCustomOption(
                    RhaiCodeStyleSettings::class.java,
                    "SPACE_AROUND_ARROW_OPERATOR",
                    "Around arrow operator (=>)",
                    CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS
                )

                consumer.showCustomOption(
                    RhaiCodeStyleSettings::class.java,
                    "SPACE_WITHIN_CLOSURE_PIPES",
                    "Within closure pipes (|x|)",
                    CodeStyleSettingsCustomizableOptions.getInstance().SPACES_OTHER
                )
            }

            SettingsType.WRAPPING_AND_BRACES_SETTINGS -> {
                consumer.showStandardOptions(
                    CodeStyleSettingsCustomizable.WrappingOrBraceOption.KEEP_LINE_BREAKS.name,
                    CodeStyleSettingsCustomizable.WrappingOrBraceOption.KEEP_FIRST_COLUMN_COMMENT.name
                )

                consumer.showCustomOption(
                    RhaiCodeStyleSettings::class.java,
                    "ALIGN_MULTILINE_PARAMETERS",
                    "Align multiline parameters",
                    CodeStyleSettingsCustomizableOptions.getInstance().WRAPPING_METHOD_PARAMETERS
                )

                consumer.showCustomOption(
                    RhaiCodeStyleSettings::class.java,
                    "ALIGN_MULTILINE_ARGUMENTS",
                    "Align multiline arguments",
                    CodeStyleSettingsCustomizableOptions.getInstance().WRAPPING_METHOD_ARGUMENTS_WRAPPING
                )
            }

            SettingsType.BLANK_LINES_SETTINGS -> {
                consumer.showCustomOption(
                    RhaiCodeStyleSettings::class.java,
                    "BLANK_LINES_AFTER_IMPORTS",
                    "After imports",
                    CodeStyleSettingsCustomizableOptions.getInstance().BLANK_LINES
                )

                consumer.showCustomOption(
                    RhaiCodeStyleSettings::class.java,
                    "BLANK_LINES_AROUND_FUNCTION",
                    "Around function",
                    CodeStyleSettingsCustomizableOptions.getInstance().BLANK_LINES
                )
            }

            else -> {}
        }
    }

    override fun getIndentOptionsEditor(): IndentOptionsEditor {
        return SmartIndentOptionsEditor()
    }

    override fun customizeDefaults(
        commonSettings: CommonCodeStyleSettings,
        indentOptions: CommonCodeStyleSettings.IndentOptions
    ) {
        indentOptions.INDENT_SIZE = 4
        indentOptions.CONTINUATION_INDENT_SIZE = 4
        indentOptions.TAB_SIZE = 4
        indentOptions.USE_TAB_CHARACTER = false

        commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS = true
        commonSettings.SPACE_AROUND_LOGICAL_OPERATORS = true
        commonSettings.SPACE_AROUND_EQUALITY_OPERATORS = true
        commonSettings.SPACE_AROUND_RELATIONAL_OPERATORS = true
        commonSettings.SPACE_AROUND_ADDITIVE_OPERATORS = true
        commonSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = true
        commonSettings.SPACE_AFTER_COMMA = true
        commonSettings.SPACE_BEFORE_COMMA = false
        commonSettings.SPACE_AFTER_COLON = true
        commonSettings.SPACE_BEFORE_COLON = false
    }

    companion object {
        private val DEFAULT_SAMPLE = """
            // Rhai code sample
            import "utils" as utils;

            const MAX = 100;

            fn greet(name) {
                let message = "Hello, " + name;
                println(message);
            }

            let arr = [1, 2, 3];
            let doubled = arr.map(|x| x * 2);

            for i in 0..10 {
                if i > 5 {
                    break;
                }
            }
        """.trimIndent()

        private val INDENT_SAMPLE = """
            fn example() {
                let x = 1;
                if x > 0 {
                    for i in 0..10 {
                        print(i);
                    }
                }
            }
        """.trimIndent()

        private val SPACING_SAMPLE = """
            let x = 1 + 2;
            let y = a && b || c;
            let range = 0..10;
            let arr = [1, 2, 3];
            let obj = #{ key: value };
            let closure = |x| x * 2;
            fn call(a, b, c) {}
        """.trimIndent()

        private val WRAPPING_SAMPLE = """
            fn function_with_many_parameters(
                parameter_one,
                parameter_two,
                parameter_three
            ) {
                let result = long_function_call(
                    argument_one,
                    argument_two,
                    argument_three
                );
            }

            let array = [
                element_one,
                element_two,
                element_three
            ];
        """.trimIndent()

        private val BLANK_LINES_SAMPLE = """
            import "module_a";
            import "module_b";

            const CONSTANT = 42;

            fn function_one() {
                // ...
            }

            fn function_two() {
                // ...
            }
        """.trimIndent()
    }
}
