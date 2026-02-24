package org.rhai.features

import com.intellij.lang.parameterInfo.*
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.rhai.*
import org.rhai.lang.RhaiFile

class RhaiParameterInfoHandler : ParameterInfoHandler<RhaiArgumentList, RhaiParameterInfoHandler.FunctionInfo> {

    data class FunctionInfo(
        val name: String,
        val parameters: List<ParameterDescriptor>,
        val isBuiltin: Boolean = false
    )

    data class ParameterDescriptor(
        val name: String,
        val type: String? = null,
        val defaultValue: String? = null
    )

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): RhaiArgumentList? {
        return findArgumentList(context.file, context.offset)
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): RhaiArgumentList? {
        return findArgumentList(context.file, context.offset)
    }

    private fun findArgumentList(file: PsiFile, offset: Int): RhaiArgumentList? {
        val element = file.findElementAt(offset) ?: return null
        return PsiTreeUtil.getParentOfType(element, RhaiArgumentList::class.java)
    }

    override fun showParameterInfo(element: RhaiArgumentList, context: CreateParameterInfoContext) {
        val functionInfo = getFunctionInfo(element)
        if (functionInfo != null) {
            context.itemsToShow = arrayOf(functionInfo)
            context.showHint(element, element.textRange.startOffset, this)
        }
    }

    private fun getFunctionInfo(argumentList: RhaiArgumentList): FunctionInfo? {
        val postfixExpr = argumentList.parent as? RhaiPostfixExpr ?: return null

        // Find the function name
        val funcName = findFunctionName(postfixExpr) ?: return null

        // Check if it's a builtin function
        val builtinInfo = getBuiltinFunctionInfo(funcName)
        if (builtinInfo != null) {
            return builtinInfo
        }

        // Find user-defined function
        val file = argumentList.containingFile as? RhaiFile ?: return null
        val funcDef = findFunctionDefinition(file, funcName)

        return funcDef?.let { createFunctionInfo(it) }
    }

    private fun findFunctionName(postfixExpr: RhaiPostfixExpr): String? {
        // Find the identifier before the argument list
        val children = postfixExpr.children.toList()
        for (i in children.indices) {
            if (children[i] is RhaiArgumentList && i > 0) {
                val prev = children[i - 1]
                if (prev.elementType == RhaiTypes.IDENTIFIER) {
                    return prev.text
                }
                // Could be a primary expression with identifier
                if (prev is RhaiPrimaryExpr) {
                    return prev.text
                }
            }
        }
        return postfixExpr.firstChild?.text
    }

    private fun findFunctionDefinition(file: RhaiFile, name: String): RhaiFunctionDefinition? {
        return PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java)
            .firstOrNull { it.identifier.text == name }
    }

    private fun createFunctionInfo(func: RhaiFunctionDefinition): FunctionInfo {
        val params = func.parameters?.parameterList?.map { param ->
            ParameterDescriptor(
                name = param.pattern.text,
                type = param.typeAnnotation?.text?.removePrefix(":")?.trim()
            )
        } ?: emptyList()

        return FunctionInfo(
            name = func.identifier.text,
            parameters = params
        )
    }

    private fun getBuiltinFunctionInfo(name: String): FunctionInfo? {
        return BUILTIN_FUNCTIONS[name]
    }

    override fun updateParameterInfo(parameterOwner: RhaiArgumentList, context: UpdateParameterInfoContext) {
        val offset = context.offset
        val text = parameterOwner.text
        val relativeOffset = offset - parameterOwner.textRange.startOffset

        var currentIndex = 0
        var depth = 0
        var inString = false
        var stringChar: Char? = null

        for (i in 0 until minOf(relativeOffset, text.length)) {
            val c = text[i]

            if (inString) {
                if (c == stringChar && (i == 0 || text[i - 1] != '\\')) {
                    inString = false
                    stringChar = null
                }
            } else {
                when (c) {
                    '"', '\'' -> {
                        inString = true
                        stringChar = c
                    }
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                    ',' -> if (depth == 1) currentIndex++
                }
            }
        }

        context.setCurrentParameter(currentIndex)
    }

    override fun updateUI(info: FunctionInfo?, context: ParameterInfoUIContext) {
        if (info == null) {
            context.isUIComponentEnabled = false
            return
        }

        val currentIndex = context.currentParameterIndex

        if (info.parameters.isEmpty()) {
            context.setupUIComponentPresentation(
                "${info.name}()",
                -1, -1,
                false,
                false,
                false,
                context.defaultParameterColor
            )
            return
        }

        val builder = StringBuilder()
        var highlightStart = -1
        var highlightEnd = -1

        info.parameters.forEachIndexed { index, param ->
            if (index > 0) {
                builder.append(", ")
            }

            if (index == currentIndex) {
                highlightStart = builder.length
            }

            builder.append(param.name)
            param.type?.let { builder.append(": $it") }
            param.defaultValue?.let { builder.append(" = $it") }

            if (index == currentIndex) {
                highlightEnd = builder.length
            }
        }

        context.setupUIComponentPresentation(
            builder.toString(),
            highlightStart,
            highlightEnd,
            false,
            false,
            false,
            context.defaultParameterColor
        )
    }


    companion object {
        private val BUILTIN_FUNCTIONS = mapOf(
            // I/O Functions
            "print" to FunctionInfo("print", listOf(ParameterDescriptor("value", "Dynamic")), true),
            "println" to FunctionInfo("println", listOf(ParameterDescriptor("value", "Dynamic")), true),
            "debug" to FunctionInfo("debug", listOf(ParameterDescriptor("value", "Dynamic")), true),

            // Type Functions
            "type_of" to FunctionInfo("type_of", listOf(ParameterDescriptor("value", "Dynamic")), true),
            "is_def" to FunctionInfo("is_def", listOf(ParameterDescriptor("variable", "String")), true),

            // String Functions
            "to_string" to FunctionInfo("to_string", listOf(ParameterDescriptor("value", "Dynamic")), true),
            "to_chars" to FunctionInfo("to_chars", listOf(ParameterDescriptor("string", "String")), true),
            "len" to FunctionInfo("len", listOf(ParameterDescriptor("value", "String|Array")), true),
            "pad" to FunctionInfo("pad", listOf(
                ParameterDescriptor("string", "String"),
                ParameterDescriptor("length", "int"),
                ParameterDescriptor("fill", "char", "' '")
            ), true),
            "trim" to FunctionInfo("trim", listOf(ParameterDescriptor("string", "String")), true),
            "contains" to FunctionInfo("contains", listOf(
                ParameterDescriptor("string", "String"),
                ParameterDescriptor("substring", "String")
            ), true),
            "replace" to FunctionInfo("replace", listOf(
                ParameterDescriptor("string", "String"),
                ParameterDescriptor("from", "String"),
                ParameterDescriptor("to", "String")
            ), true),
            "split" to FunctionInfo("split", listOf(
                ParameterDescriptor("string", "String"),
                ParameterDescriptor("delimiter", "String")
            ), true),
            "sub_string" to FunctionInfo("sub_string", listOf(
                ParameterDescriptor("string", "String"),
                ParameterDescriptor("start", "int"),
                ParameterDescriptor("length", "int")
            ), true),

            // Number Functions
            "to_int" to FunctionInfo("to_int", listOf(ParameterDescriptor("value", "Dynamic")), true),
            "to_float" to FunctionInfo("to_float", listOf(ParameterDescriptor("value", "Dynamic")), true),
            "to_decimal" to FunctionInfo("to_decimal", listOf(ParameterDescriptor("value", "Dynamic")), true),

            // Math Functions
            "abs" to FunctionInfo("abs", listOf(ParameterDescriptor("x", "int|float")), true),
            "sin" to FunctionInfo("sin", listOf(ParameterDescriptor("x", "float")), true),
            "cos" to FunctionInfo("cos", listOf(ParameterDescriptor("x", "float")), true),
            "tan" to FunctionInfo("tan", listOf(ParameterDescriptor("x", "float")), true),
            "asin" to FunctionInfo("asin", listOf(ParameterDescriptor("x", "float")), true),
            "acos" to FunctionInfo("acos", listOf(ParameterDescriptor("x", "float")), true),
            "atan" to FunctionInfo("atan", listOf(ParameterDescriptor("x", "float")), true),
            "sinh" to FunctionInfo("sinh", listOf(ParameterDescriptor("x", "float")), true),
            "cosh" to FunctionInfo("cosh", listOf(ParameterDescriptor("x", "float")), true),
            "tanh" to FunctionInfo("tanh", listOf(ParameterDescriptor("x", "float")), true),
            "sqrt" to FunctionInfo("sqrt", listOf(ParameterDescriptor("x", "float")), true),
            "exp" to FunctionInfo("exp", listOf(ParameterDescriptor("x", "float")), true),
            "ln" to FunctionInfo("ln", listOf(ParameterDescriptor("x", "float")), true),
            "log" to FunctionInfo("log", listOf(
                ParameterDescriptor("x", "float"),
                ParameterDescriptor("base", "float")
            ), true),
            "log10" to FunctionInfo("log10", listOf(ParameterDescriptor("x", "float")), true),
            "floor" to FunctionInfo("floor", listOf(ParameterDescriptor("x", "float")), true),
            "ceiling" to FunctionInfo("ceiling", listOf(ParameterDescriptor("x", "float")), true),
            "round" to FunctionInfo("round", listOf(ParameterDescriptor("x", "float")), true),
            "int" to FunctionInfo("int", listOf(ParameterDescriptor("x", "float")), true),
            "fraction" to FunctionInfo("fraction", listOf(ParameterDescriptor("x", "float")), true),

            // Array Functions
            "push" to FunctionInfo("push", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("value", "Dynamic")
            ), true),
            "pop" to FunctionInfo("pop", listOf(ParameterDescriptor("array", "Array")), true),
            "shift" to FunctionInfo("shift", listOf(ParameterDescriptor("array", "Array")), true),
            "insert" to FunctionInfo("insert", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("index", "int"),
                ParameterDescriptor("value", "Dynamic")
            ), true),
            "remove" to FunctionInfo("remove", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("index", "int")
            ), true),
            "reverse" to FunctionInfo("reverse", listOf(ParameterDescriptor("array", "Array")), true),
            "sort" to FunctionInfo("sort", listOf(ParameterDescriptor("array", "Array")), true),
            "drain" to FunctionInfo("drain", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("start", "int"),
                ParameterDescriptor("end", "int")
            ), true),
            "retain" to FunctionInfo("retain", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("filter", "FnPtr")
            ), true),
            "splice" to FunctionInfo("splice", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("start", "int"),
                ParameterDescriptor("deleteCount", "int"),
                ParameterDescriptor("items", "Array")
            ), true),
            "extract" to FunctionInfo("extract", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("start", "int"),
                ParameterDescriptor("end", "int")
            ), true),
            "chop" to FunctionInfo("chop", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("length", "int")
            ), true),

            // Functional Array Methods
            "map" to FunctionInfo("map", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("mapper", "FnPtr")
            ), true),
            "filter" to FunctionInfo("filter", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("predicate", "FnPtr")
            ), true),
            "reduce" to FunctionInfo("reduce", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("initial", "Dynamic"),
                ParameterDescriptor("reducer", "FnPtr")
            ), true),
            "reduce_rev" to FunctionInfo("reduce_rev", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("initial", "Dynamic"),
                ParameterDescriptor("reducer", "FnPtr")
            ), true),
            "some" to FunctionInfo("some", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("predicate", "FnPtr")
            ), true),
            "all" to FunctionInfo("all", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("predicate", "FnPtr")
            ), true),
            "for_each" to FunctionInfo("for_each", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("action", "FnPtr")
            ), true),
            "index_of" to FunctionInfo("index_of", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("value", "Dynamic")
            ), true),
            "find" to FunctionInfo("find", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("predicate", "FnPtr")
            ), true),
            "find_map" to FunctionInfo("find_map", listOf(
                ParameterDescriptor("array", "Array"),
                ParameterDescriptor("mapper", "FnPtr")
            ), true),

            // Object Map Functions
            "keys" to FunctionInfo("keys", listOf(ParameterDescriptor("map", "Map")), true),
            "values" to FunctionInfo("values", listOf(ParameterDescriptor("map", "Map")), true),

            // Timestamp Functions
            "timestamp" to FunctionInfo("timestamp", emptyList(), true),

            // Range Functions
            "range" to FunctionInfo("range", listOf(
                ParameterDescriptor("start", "int"),
                ParameterDescriptor("end", "int"),
                ParameterDescriptor("step", "int", "1")
            ), true),

            // Eval Functions
            "eval" to FunctionInfo("eval", listOf(ParameterDescriptor("script", "String")), true),

            // Call Functions
            "call" to FunctionInfo("call", listOf(
                ParameterDescriptor("fn_ptr", "FnPtr"),
                ParameterDescriptor("args...", "Dynamic")
            ), true)
        )
    }
}
