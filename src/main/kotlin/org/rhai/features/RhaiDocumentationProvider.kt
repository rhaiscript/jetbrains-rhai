package org.rhai.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.rhai.*
import org.rhai.lang.RhaiFile

class RhaiDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        return when (element) {
            is RhaiFunctionDefinition -> generateFunctionDoc(element)
            is RhaiLetDeclaration -> generateLetDoc(element)
            is RhaiConstDeclaration -> generateConstDoc(element)
            is RhaiParameter -> generateParameterDoc(element)
            is RhaiModuleDeclaration -> generateModuleDoc(element)
            is RhaiForStatement -> generateForDoc(element)
            is RhaiClosureExpr -> generateClosureDoc(element)
            else -> {
                // Check for builtin function
                if (element.elementType == RhaiTypes.IDENTIFIER) {
                    val name = element.text
                    BUILTIN_DOCS[name]?.let { return generateBuiltinDoc(name, it) }
                }
                null
            }
        }
    }

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        if (contextElement == null) return null

        // Check if it's a builtin
        if (contextElement.elementType == RhaiTypes.IDENTIFIER) {
            val name = contextElement.text
            if (BUILTIN_DOCS.containsKey(name)) {
                return contextElement
            }
        }

        // Navigate to declaration
        return when (val parent = contextElement.parent) {
            is RhaiFunctionDefinition -> parent
            is RhaiLetDeclaration -> parent
            is RhaiConstDeclaration -> parent
            is RhaiParameter -> parent
            is RhaiModuleDeclaration -> parent
            else -> {
                // Try to find referenced element
                findReferencedElement(contextElement, file)
            }
        }
    }

    private fun findReferencedElement(element: PsiElement, file: PsiFile): PsiElement? {
        val name = element.text
        val rhaiFile = file as? RhaiFile ?: return null

        // Look for function
        PsiTreeUtil.findChildrenOfType(rhaiFile, RhaiFunctionDefinition::class.java)
            .firstOrNull { it.identifier.text == name }
            ?.let { return it }

        // Look for const
        PsiTreeUtil.findChildrenOfType(rhaiFile, RhaiConstDeclaration::class.java)
            .firstOrNull { it.node.findChildByType(RhaiTypes.IDENTIFIER)?.text == name }
            ?.let { return it }

        // Look for let
        PsiTreeUtil.findChildrenOfType(rhaiFile, RhaiLetDeclaration::class.java)
            .firstOrNull { it.pattern.text == name && it.textOffset < element.textOffset }
            ?.let { return it }

        return null
    }

    private fun generateFunctionDoc(func: RhaiFunctionDefinition): String {
        val sb = StringBuilder()

        // Definition section
        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("<code>")

        // Visibility
        func.visibility?.let { sb.append("${it.text} ") }

        sb.append("fn <b>${func.identifier.text}</b>")

        // Parameters
        sb.append("(")
        func.parameters?.parameterList?.forEachIndexed { index, param ->
            if (index > 0) sb.append(", ")
            sb.append(param.pattern.text)
            param.typeAnnotation?.let { sb.append(it.text) }
        }
        sb.append(")")

        // Return type
        func.returnType?.let { sb.append(" ${it.text}") }

        sb.append("</code>")
        sb.append(DocumentationMarkup.DEFINITION_END)

        // Content section
        sb.append(DocumentationMarkup.CONTENT_START)

        // Extract doc comment if present
        val docComment = findDocComment(func)
        if (docComment != null) {
            sb.append(formatDocComment(docComment))
        } else {
            sb.append("Function <code>${func.identifier.text}</code>")
        }

        sb.append(DocumentationMarkup.CONTENT_END)

        // Sections
        sb.append(DocumentationMarkup.SECTIONS_START)

        // Parameters section
        val params = func.parameters?.parameterList
        if (!params.isNullOrEmpty()) {
            sb.append(DocumentationMarkup.SECTION_HEADER_START)
            sb.append("Parameters:")
            sb.append(DocumentationMarkup.SECTION_SEPARATOR)
            sb.append("<p>")
            params.forEach { param ->
                sb.append("<code>${param.pattern.text}</code>")
                param.typeAnnotation?.let { sb.append(" ${it.text}") }
                sb.append("<br/>")
            }
            sb.append("</p>")
            sb.append(DocumentationMarkup.SECTION_END)
        }

        // Return type section
        func.returnType?.let { returnType ->
            sb.append(DocumentationMarkup.SECTION_HEADER_START)
            sb.append("Returns:")
            sb.append(DocumentationMarkup.SECTION_SEPARATOR)
            sb.append("<code>${returnType.text}</code>")
            sb.append(DocumentationMarkup.SECTION_END)
        }

        // Location
        sb.append(DocumentationMarkup.SECTION_HEADER_START)
        sb.append("Defined in:")
        sb.append(DocumentationMarkup.SECTION_SEPARATOR)
        sb.append(func.containingFile?.name ?: "unknown")
        sb.append(DocumentationMarkup.SECTION_END)

        sb.append(DocumentationMarkup.SECTIONS_END)

        return sb.toString()
    }

    private fun generateLetDoc(letDecl: RhaiLetDeclaration): String {
        val sb = StringBuilder()

        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("<code>let <b>${letDecl.pattern.text}</b>")
        letDecl.typeAnnotation?.let { sb.append(it.text) }
        letDecl.expression?.let { sb.append(" = ${truncateExpression(it.text)}") }
        sb.append("</code>")
        sb.append(DocumentationMarkup.DEFINITION_END)

        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append("Variable <code>${letDecl.pattern.text}</code>")
        sb.append(DocumentationMarkup.CONTENT_END)

        sb.append(DocumentationMarkup.SECTIONS_START)
        sb.append(DocumentationMarkup.SECTION_HEADER_START)
        sb.append("Scope:")
        sb.append(DocumentationMarkup.SECTION_SEPARATOR)
        sb.append(letDecl.containingFile?.name ?: "local")
        sb.append(DocumentationMarkup.SECTION_END)
        sb.append(DocumentationMarkup.SECTIONS_END)

        return sb.toString()
    }

    private fun generateConstDoc(constDecl: RhaiConstDeclaration): String {
        val sb = StringBuilder()
        val name = constDecl.node.findChildByType(RhaiTypes.IDENTIFIER)?.text ?: "unknown"

        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("<code>const <b>$name</b></code>")
        sb.append(DocumentationMarkup.DEFINITION_END)

        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append("Constant <code>$name</code>")
        sb.append(DocumentationMarkup.CONTENT_END)

        sb.append(DocumentationMarkup.SECTIONS_START)
        sb.append(DocumentationMarkup.SECTION_HEADER_START)
        sb.append("Type:")
        sb.append(DocumentationMarkup.SECTION_SEPARATOR)
        sb.append("immutable")
        sb.append(DocumentationMarkup.SECTION_END)
        sb.append(DocumentationMarkup.SECTIONS_END)

        return sb.toString()
    }

    private fun generateParameterDoc(param: RhaiParameter): String {
        val sb = StringBuilder()

        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("<code>${param.pattern.text}")
        param.typeAnnotation?.let { sb.append(it.text) }
        sb.append("</code>")
        sb.append(DocumentationMarkup.DEFINITION_END)

        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append("Parameter <code>${param.pattern.text}</code>")
        sb.append(DocumentationMarkup.CONTENT_END)

        // Find enclosing function
        val func = PsiTreeUtil.getParentOfType(param, RhaiFunctionDefinition::class.java)
        if (func != null) {
            sb.append(DocumentationMarkup.SECTIONS_START)
            sb.append(DocumentationMarkup.SECTION_HEADER_START)
            sb.append("Function:")
            sb.append(DocumentationMarkup.SECTION_SEPARATOR)
            sb.append("<code>${func.identifier.text}</code>")
            sb.append(DocumentationMarkup.SECTION_END)
            sb.append(DocumentationMarkup.SECTIONS_END)
        }

        return sb.toString()
    }

    private fun generateModuleDoc(module: RhaiModuleDeclaration): String {
        val sb = StringBuilder()
        val name = module.node.findChildByType(RhaiTypes.IDENTIFIER)?.text ?: "unknown"

        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("<code>module <b>$name</b></code>")
        sb.append(DocumentationMarkup.DEFINITION_END)

        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append("Module <code>$name</code>")
        sb.append(DocumentationMarkup.CONTENT_END)

        return sb.toString()
    }

    private fun generateForDoc(forStmt: RhaiForStatement): String {
        val sb = StringBuilder()

        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("<code>for ... in ...</code>")
        sb.append(DocumentationMarkup.DEFINITION_END)

        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append("For loop iterating over a collection or range")
        sb.append(DocumentationMarkup.CONTENT_END)

        return sb.toString()
    }

    private fun generateClosureDoc(closure: RhaiClosureExpr): String {
        val sb = StringBuilder()

        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("<code>|")
        closure.closureParams?.closureParamList?.forEachIndexed { index, param ->
            if (index > 0) sb.append(", ")
            sb.append(param.text)
        }
        sb.append("| { ... }</code>")
        sb.append(DocumentationMarkup.DEFINITION_END)

        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append("Anonymous closure/lambda function")
        sb.append(DocumentationMarkup.CONTENT_END)

        return sb.toString()
    }

    private fun generateBuiltinDoc(name: String, info: BuiltinDocInfo): String {
        val sb = StringBuilder()

        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append("<code><b>$name</b>(${info.params})</code>")
        sb.append(DocumentationMarkup.DEFINITION_END)

        sb.append(DocumentationMarkup.CONTENT_START)
        sb.append(info.description)
        sb.append(DocumentationMarkup.CONTENT_END)

        sb.append(DocumentationMarkup.SECTIONS_START)

        // Return type
        sb.append(DocumentationMarkup.SECTION_HEADER_START)
        sb.append("Returns:")
        sb.append(DocumentationMarkup.SECTION_SEPARATOR)
        sb.append("<code>${info.returnType}</code>")
        sb.append(DocumentationMarkup.SECTION_END)

        // Example if available
        info.example?.let { example ->
            sb.append(DocumentationMarkup.SECTION_HEADER_START)
            sb.append("Example:")
            sb.append(DocumentationMarkup.SECTION_SEPARATOR)
            sb.append("<pre><code>$example</code></pre>")
            sb.append(DocumentationMarkup.SECTION_END)
        }

        sb.append(DocumentationMarkup.SECTION_HEADER_START)
        sb.append("Type:")
        sb.append(DocumentationMarkup.SECTION_SEPARATOR)
        sb.append("Built-in function")
        sb.append(DocumentationMarkup.SECTION_END)

        sb.append(DocumentationMarkup.SECTIONS_END)

        return sb.toString()
    }

    private fun findDocComment(element: PsiElement): String? {
        var prev = element.prevSibling
        while (prev != null) {
            when (prev.elementType) {
                RhaiTypes.DOC_COMMENT, RhaiTypes.DOC_LINE -> {
                    return prev.text
                }
                RhaiTypes.LINE_COMMENT -> {
                    // Continue looking for doc comments
                }
                else -> {
                    if (prev.text.isNotBlank()) return null
                }
            }
            prev = prev.prevSibling
        }
        return null
    }

    private fun formatDocComment(comment: String): String {
        return comment
            .lines()
            .map { line ->
                line.trim()
                    .removePrefix("///")
                    .removePrefix("/**")
                    .removeSuffix("*/")
                    .removePrefix("*")
                    .trim()
            }
            .filter { it.isNotEmpty() }
            .joinToString("<br/>")
    }

    private fun truncateExpression(text: String, maxLength: Int = 50): String {
        val singleLine = text.replace("\n", " ").trim()
        return if (singleLine.length > maxLength) {
            singleLine.take(maxLength) + "..."
        } else {
            singleLine
        }
    }

    data class BuiltinDocInfo(
        val params: String,
        val returnType: String,
        val description: String,
        val example: String? = null
    )

    companion object {
        private val BUILTIN_DOCS = mapOf(
            // I/O
            "print" to BuiltinDocInfo(
                "value",
                "()",
                "Prints a value to the standard output without a newline.",
                """print("Hello");"""
            ),
            "println" to BuiltinDocInfo(
                "value",
                "()",
                "Prints a value to the standard output with a newline.",
                """println("Hello, World!");"""
            ),
            "debug" to BuiltinDocInfo(
                "value",
                "()",
                "Prints a value for debugging purposes with type information.",
                """debug(my_object);"""
            ),

            // Type Functions
            "type_of" to BuiltinDocInfo(
                "value",
                "String",
                "Returns the type name of a value as a string.",
                """let t = type_of(42);  // "i64""""
            ),
            "is_def" to BuiltinDocInfo(
                "variable_name",
                "bool",
                "Checks if a variable is defined in the current scope.",
                """if is_def("x") { print(x); }"""
            ),

            // String Functions
            "to_string" to BuiltinDocInfo(
                "value",
                "String",
                "Converts a value to its string representation.",
                """let s = to_string(123);  // "123""""
            ),
            "len" to BuiltinDocInfo(
                "string_or_array",
                "int",
                "Returns the length of a string or array.",
                """let n = len("hello");  // 5"""
            ),
            "trim" to BuiltinDocInfo(
                "string",
                "String",
                "Removes leading and trailing whitespace from a string.",
                """let s = trim("  hello  ");  // "hello""""
            ),
            "contains" to BuiltinDocInfo(
                "string, substring",
                "bool",
                "Checks if a string contains a substring.",
                """let b = contains("hello", "ell");  // true"""
            ),
            "replace" to BuiltinDocInfo(
                "string, from, to",
                "String",
                "Replaces all occurrences of a substring with another.",
                """let s = replace("hello", "l", "L");  // "heLLo""""
            ),
            "split" to BuiltinDocInfo(
                "string, delimiter",
                "Array",
                "Splits a string by a delimiter into an array.",
                """let arr = split("a,b,c", ",");  // ["a", "b", "c"]"""
            ),
            "sub_string" to BuiltinDocInfo(
                "string, start, length",
                "String",
                "Extracts a substring from the given position with the specified length.",
                """let s = sub_string("hello", 1, 3);  // "ell""""
            ),

            // Number Functions
            "to_int" to BuiltinDocInfo(
                "value",
                "int",
                "Converts a value to an integer.",
                """let n = to_int("42");  // 42"""
            ),
            "to_float" to BuiltinDocInfo(
                "value",
                "float",
                "Converts a value to a floating-point number.",
                """let f = to_float(42);  // 42.0"""
            ),

            // Math Functions
            "abs" to BuiltinDocInfo(
                "x",
                "int|float",
                "Returns the absolute value of a number.",
                """let n = abs(-42);  // 42"""
            ),
            "sin" to BuiltinDocInfo(
                "x",
                "float",
                "Returns the sine of an angle in radians.",
                """let s = sin(0.0);  // 0.0"""
            ),
            "cos" to BuiltinDocInfo(
                "x",
                "float",
                "Returns the cosine of an angle in radians.",
                """let c = cos(0.0);  // 1.0"""
            ),
            "tan" to BuiltinDocInfo(
                "x",
                "float",
                "Returns the tangent of an angle in radians.",
                """let t = tan(0.0);  // 0.0"""
            ),
            "sqrt" to BuiltinDocInfo(
                "x",
                "float",
                "Returns the square root of a number.",
                """let r = sqrt(16.0);  // 4.0"""
            ),
            "exp" to BuiltinDocInfo(
                "x",
                "float",
                "Returns e raised to the power of x.",
                """let e = exp(1.0);  // ~2.718"""
            ),
            "ln" to BuiltinDocInfo(
                "x",
                "float",
                "Returns the natural logarithm of x.",
                """let l = ln(2.718);  // ~1.0"""
            ),
            "log" to BuiltinDocInfo(
                "x, base",
                "float",
                "Returns the logarithm of x with the specified base.",
                """let l = log(8.0, 2.0);  // 3.0"""
            ),
            "floor" to BuiltinDocInfo(
                "x",
                "float",
                "Returns the largest integer less than or equal to x.",
                """let f = floor(3.7);  // 3.0"""
            ),
            "ceiling" to BuiltinDocInfo(
                "x",
                "float",
                "Returns the smallest integer greater than or equal to x.",
                """let c = ceiling(3.2);  // 4.0"""
            ),
            "round" to BuiltinDocInfo(
                "x",
                "float",
                "Rounds a number to the nearest integer.",
                """let r = round(3.5);  // 4.0"""
            ),

            // Array Functions
            "push" to BuiltinDocInfo(
                "array, value",
                "()",
                "Appends a value to the end of an array.",
                """let arr = [1, 2]; push(arr, 3);  // [1, 2, 3]"""
            ),
            "pop" to BuiltinDocInfo(
                "array",
                "Dynamic",
                "Removes and returns the last element of an array.",
                """let arr = [1, 2, 3]; let x = pop(arr);  // x = 3"""
            ),
            "shift" to BuiltinDocInfo(
                "array",
                "Dynamic",
                "Removes and returns the first element of an array.",
                """let arr = [1, 2, 3]; let x = shift(arr);  // x = 1"""
            ),
            "insert" to BuiltinDocInfo(
                "array, index, value",
                "()",
                "Inserts a value at the specified index in an array.",
                """let arr = [1, 3]; insert(arr, 1, 2);  // [1, 2, 3]"""
            ),
            "remove" to BuiltinDocInfo(
                "array, index",
                "Dynamic",
                "Removes and returns the element at the specified index.",
                """let arr = [1, 2, 3]; let x = remove(arr, 1);  // x = 2"""
            ),
            "reverse" to BuiltinDocInfo(
                "array",
                "()",
                "Reverses the order of elements in an array in place.",
                """let arr = [1, 2, 3]; reverse(arr);  // [3, 2, 1]"""
            ),
            "sort" to BuiltinDocInfo(
                "array",
                "()",
                "Sorts the elements of an array in place.",
                """let arr = [3, 1, 2]; sort(arr);  // [1, 2, 3]"""
            ),

            // Functional Array Methods
            "map" to BuiltinDocInfo(
                "array, mapper",
                "Array",
                "Creates a new array with the results of calling a function on every element.",
                """let arr = [1, 2, 3].map(|x| x * 2);  // [2, 4, 6]"""
            ),
            "filter" to BuiltinDocInfo(
                "array, predicate",
                "Array",
                "Creates a new array with elements that pass the test.",
                """let arr = [1, 2, 3, 4].filter(|x| x > 2);  // [3, 4]"""
            ),
            "reduce" to BuiltinDocInfo(
                "array, initial, reducer",
                "Dynamic",
                "Reduces an array to a single value using an accumulator function.",
                """let sum = [1, 2, 3].reduce(0, |acc, x| acc + x);  // 6"""
            ),
            "some" to BuiltinDocInfo(
                "array, predicate",
                "bool",
                "Tests whether at least one element passes the test.",
                """let b = [1, 2, 3].some(|x| x > 2);  // true"""
            ),
            "all" to BuiltinDocInfo(
                "array, predicate",
                "bool",
                "Tests whether all elements pass the test.",
                """let b = [1, 2, 3].all(|x| x > 0);  // true"""
            ),
            "find" to BuiltinDocInfo(
                "array, predicate",
                "Dynamic",
                "Returns the first element that satisfies the predicate.",
                """let x = [1, 2, 3].find(|x| x > 1);  // 2"""
            ),
            "index_of" to BuiltinDocInfo(
                "array, value",
                "int",
                "Returns the index of the first occurrence of a value, or -1 if not found.",
                """let i = [1, 2, 3].index_of(2);  // 1"""
            ),
            "for_each" to BuiltinDocInfo(
                "array, action",
                "()",
                "Executes a function for each element in the array.",
                """[1, 2, 3].for_each(|x| print(x));"""
            ),

            // Object Map Functions
            "keys" to BuiltinDocInfo(
                "map",
                "Array",
                "Returns an array of all keys in the object map.",
                """let k = keys(#{ a: 1, b: 2 });  // ["a", "b"]"""
            ),
            "values" to BuiltinDocInfo(
                "map",
                "Array",
                "Returns an array of all values in the object map.",
                """let v = values(#{ a: 1, b: 2 });  // [1, 2]"""
            ),

            // Utility Functions
            "timestamp" to BuiltinDocInfo(
                "",
                "Timestamp",
                "Returns the current timestamp.",
                """let now = timestamp();"""
            ),
            "range" to BuiltinDocInfo(
                "start, end, step?",
                "Range",
                "Creates a range from start (inclusive) to end (exclusive) with optional step.",
                """for i in range(0, 10, 2) { print(i); }  // 0, 2, 4, 6, 8"""
            ),
            "eval" to BuiltinDocInfo(
                "script",
                "Dynamic",
                "Evaluates a string as a Rhai script and returns the result.",
                """let result = eval("40 + 2");  // 42"""
            ),
            "call" to BuiltinDocInfo(
                "fn_ptr, args...",
                "Dynamic",
                "Calls a function pointer with the given arguments.",
                """let f = Fn("add"); call(f, 1, 2);  // 3"""
            )
        )
    }
}
