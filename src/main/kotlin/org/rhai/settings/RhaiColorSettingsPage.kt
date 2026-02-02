package org.rhai.settings

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.rhai.features.RhaiAnnotator
import org.rhai.features.RhaiAnnotator.Companion.FUNCTION_DECLARATION
import org.rhai.highlighting.RhaiSyntaxHighlighter
import org.rhai.lang.RhaiIcons
import javax.swing.Icon

class RhaiColorSettingsPage : ColorSettingsPage {
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = arrayOf(
        AttributesDescriptor("Keywords//Keywords", RhaiSyntaxHighlighter.KEYWORD),
        AttributesDescriptor("Keywords//Built-in functions", RhaiSyntaxHighlighter.BUILTIN),
        AttributesDescriptor("Literals//Strings", RhaiSyntaxHighlighter.STRING),
        AttributesDescriptor("Literals//Numbers", RhaiSyntaxHighlighter.NUMBER),
        AttributesDescriptor("Literals//Regex", RhaiSyntaxHighlighter.REGEX),
        AttributesDescriptor("Comments//Line comment", RhaiSyntaxHighlighter.LINE_COMMENT),
        AttributesDescriptor("Comments//Block comment", RhaiSyntaxHighlighter.BLOCK_COMMENT),
        AttributesDescriptor("Comments//Documentation", RhaiSyntaxHighlighter.BLOCK_COMMENT),

        AttributesDescriptor("Functions//Declaration", RhaiAnnotator.FUNCTION_DECLARATION),
        AttributesDescriptor("Functions//Call", RhaiAnnotator.FUNCTION_CALL),
//        AttributesDescriptor("Functions//Built-in", RhaiAnnotator.MODULE_NAME),
//        AttributesDescriptor("Semantic//Parameter", RhaiAnnotator.PARAMETER),
//        AttributesDescriptor("Semantic//Field", RhaiAnnotator.FIELD),
//        AttributesDescriptor("Semantic//Macro", RhaiAnnotator.MACRO),

        AttributesDescriptor("Errors//Invalid character", RhaiSyntaxHighlighter.BAD_CHARACTER)
    )

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Rhai"

    override fun getIcon(): Icon? = RhaiIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = RhaiSyntaxHighlighter.INSTANCE

    override fun getDemoText(): String {
        return """
    //! This script runs for-loops.

let arr = [1, true, 123.456, "hello", 3, 42];

// Loop over array with counter
for (a, i) in arr {
    for (b, j) in ['x', 42, (), 123, 99, 0.5] {
        if b > 100 { continue; }

        print(`(${'$'}{i}, ${'$'}{j}) = (${'$'}{a}, ${'$'}{b})`);
    }

    if a == 3 { break; }
}
//print(a);                 // <- if you uncomment this line, the script will fail to compile
                            //    because 'a' is not defined here

for i in range(5, 0, -1) {  // runs from 5 down to 1
    print(i);
}
""".trimIndent()
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? = null
}