package org.rhai.settings

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.rhai.features.RhaiAnnotator
import org.rhai.highlighting.RhaiSyntaxHighlighter
import org.rhai.lang.RhaiIcons
import javax.swing.Icon

class RhaiColorSettingsPage : ColorSettingsPage {

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Rhai"

    override fun getIcon(): Icon? = RhaiIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = RhaiSyntaxHighlighter.INSTANCE

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey> {
        return mutableMapOf(
            "func_decl" to RhaiAnnotator.FUNCTION_DECLARATION,
            "func_call" to RhaiAnnotator.FUNCTION_CALL,
            "param" to RhaiAnnotator.PARAMETER,
            "var" to RhaiAnnotator.LOCAL_VARIABLE,
            "const" to RhaiAnnotator.CONSTANT,
            "global" to RhaiAnnotator.GLOBAL_VARIABLE,
            "prop" to RhaiAnnotator.PROPERTY,
            "module" to RhaiAnnotator.MODULE,
            "label" to RhaiAnnotator.LABEL,
            "attr" to RhaiAnnotator.ATTRIBUTE,
            "doc_tag" to RhaiAnnotator.DOC_COMMENT_TAG
        )
    }

    companion object {
        private val DESCRIPTORS = arrayOf(
            // === Keywords ===
            AttributesDescriptor("Keywords//Keyword", RhaiSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Keywords//Control flow keyword", RhaiSyntaxHighlighter.CONTROL_KEYWORD),
            AttributesDescriptor("Keywords//Built-in function", RhaiSyntaxHighlighter.BUILTIN),

            // === Identifiers ===
            AttributesDescriptor("Identifiers//Identifier", RhaiSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("Identifiers//Function declaration", RhaiAnnotator.FUNCTION_DECLARATION),
            AttributesDescriptor("Identifiers//Function call", RhaiAnnotator.FUNCTION_CALL),
            AttributesDescriptor("Identifiers//Parameter", RhaiAnnotator.PARAMETER),
            AttributesDescriptor("Identifiers//Local variable", RhaiAnnotator.LOCAL_VARIABLE),
            AttributesDescriptor("Identifiers//Constant", RhaiAnnotator.CONSTANT),
            AttributesDescriptor("Identifiers//Global variable", RhaiAnnotator.GLOBAL_VARIABLE),
            AttributesDescriptor("Identifiers//Property", RhaiAnnotator.PROPERTY),
            AttributesDescriptor("Identifiers//Module", RhaiAnnotator.MODULE),
            AttributesDescriptor("Identifiers//Label", RhaiAnnotator.LABEL),
            AttributesDescriptor("Identifiers//Attribute", RhaiAnnotator.ATTRIBUTE),

            // === Literals ===
            AttributesDescriptor("Literals//String", RhaiSyntaxHighlighter.STRING),
            AttributesDescriptor("Literals//Raw string", RhaiSyntaxHighlighter.RAW_STRING),
            AttributesDescriptor("Literals//Character", RhaiSyntaxHighlighter.CHAR),
            AttributesDescriptor("Literals//Number", RhaiSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Literals//Regex", RhaiSyntaxHighlighter.REGEX),

            // === Interpolation ===
            AttributesDescriptor("Interpolation//Interpolated string", RhaiSyntaxHighlighter.INTERPOLATION),
            AttributesDescriptor("Interpolation//Interpolation marker", RhaiSyntaxHighlighter.INTERPOLATION_MARKER),

            // === Comments ===
            AttributesDescriptor("Comments//Line comment", RhaiSyntaxHighlighter.LINE_COMMENT),
            AttributesDescriptor("Comments//Block comment", RhaiSyntaxHighlighter.BLOCK_COMMENT),
            AttributesDescriptor("Comments//Documentation comment", RhaiSyntaxHighlighter.DOC_COMMENT),
            AttributesDescriptor("Comments//Documentation tag", RhaiAnnotator.DOC_COMMENT_TAG),

            // === Operators ===
            AttributesDescriptor("Operators//Operator", RhaiSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Operators//Arrow (=> ->)", RhaiSyntaxHighlighter.ARROW),
            AttributesDescriptor("Operators//Range (..)", RhaiSyntaxHighlighter.RANGE),

            // === Punctuation ===
            AttributesDescriptor("Punctuation//Parentheses", RhaiSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Punctuation//Braces", RhaiSyntaxHighlighter.BRACES),
            AttributesDescriptor("Punctuation//Brackets", RhaiSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Punctuation//Comma", RhaiSyntaxHighlighter.COMMA),
            AttributesDescriptor("Punctuation//Semicolon", RhaiSyntaxHighlighter.SEMICOLON),
            AttributesDescriptor("Punctuation//Dot", RhaiSyntaxHighlighter.DOT),
            AttributesDescriptor("Punctuation//Colon", RhaiSyntaxHighlighter.COLON),

            // === Errors ===
            AttributesDescriptor("Errors//Bad character", RhaiSyntaxHighlighter.BAD_CHARACTER)
        )

        private val DEMO_TEXT = """
//! Module documentation comment
//! This module demonstrates Rhai syntax highlighting

/// Documentation for the greeting function
/// @param name - The name to greet
/// @return A greeting message
fn <func_decl>greet</func_decl>(<param>name</param>) {
    let <var>message</var> = "Hello, " + <param>name</param> + "!";
    <func_call>println</func_call>(<var>message</var>);
    return <var>message</var>;
}

// Import a module
import "utils" as <module>utils</module>;

// Constants and globals
const <const>MAX_VALUE</const> = 100;
const <const>PI</const> = 3.14159;

// Private helper function
private fn <func_decl>calculate</func_decl>(<param>x</param>, <param>y</param>) -> int {
    let <var>result</var> = <param>x</param> * <param>y</param> + <const>PI</const>;
    return <var>result</var>;
}

// Array and closure operations
let <var>arr</var> = [1, 2, 3, 4, 5];
let <var>doubled</var> = <var>arr</var>.<func_call>map</func_call>(|<param>x</param>| <param>x</param> * 2);
let <var>sum</var> = <var>doubled</var>.<func_call>reduce</func_call>(0, |<param>acc</param>, <param>x</param>| <param>acc</param> + <param>x</param>);

// Interpolated string
let <var>output</var> = `The sum is ${'$'}{<var>sum</var>} and max is ${'$'}{<const>MAX_VALUE</const>}`;

// Object with properties
let <var>person</var> = #{
    name: "Alice",
    age: 30,
    greet: |<param>msg</param>| <func_call>print</func_call>(`${'$'}{this.<prop>name</prop>}: ${'$'}{<param>msg</param>}`)
};
<var>person</var>.<prop>greet</prop>("Hello!");

// Control flow
for <var>i</var> in 0..<const>MAX_VALUE</const> {
    if <var>i</var> > 10 {
        break;
    }
    <func_call>debug</func_call>(<var>i</var>);
}

// Switch expression
let <var>type_name</var> = switch <func_call>type_of</func_call>(<var>sum</var>) {
    "i64" => "integer",
    "f64" => "float",
    _ => "unknown"
};

// Labeled loop with range
'<label>outer</label>: for <var>x</var> in 0..=5 {
    for <var>y</var> in 0..5 {
        if <var>x</var> * <var>y</var> > 10 {
            break '<label>outer</label>;
        }
    }
}

// Try-catch error handling
try {
    let <var>value</var> = <func_call>eval</func_call>("40 + 2");
    <func_call>println</func_call>(<var>value</var>);
} catch (<var>err</var>) {
    <func_call>print</func_call>("Error: " + <var>err</var>);
}

// Raw string and regex
let <var>raw</var> = r#"raw string with "quotes""#;
let <var>pattern</var> = /\d+/g;

// Character literal
let <var>char</var> = 'A';

/*
 * Block comment
 * Multiple lines of documentation
 */

// Call our functions
let <var>greeting</var> = <func_call>greet</func_call>("World");
let <var>result</var> = <func_call>calculate</func_call>(10, 20);

#[<attr>test</attr>]
fn <func_decl>test_example</func_decl>() {
    assert(<func_call>calculate</func_call>(2, 3) == 6 + <const>PI</const>);
}
""".trimIndent()
    }
}
