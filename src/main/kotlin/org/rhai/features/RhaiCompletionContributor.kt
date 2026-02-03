package org.rhai.features

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.rhai.*
import org.rhai.lang.RhaiFile
import org.rhai.settings.RhaiCustomRegistrySettings

class RhaiCompletionContributor : CompletionContributor() {

    init {
        // Basic completion for identifiers
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(RhaiTypes.IDENTIFIER),
            RhaiCompletionProvider()
        )

        // Completion after dot
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().afterLeaf("."),
            RhaiMethodCompletionProvider()
        )
    }

    private class RhaiCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val position = parameters.position
            val file = parameters.originalFile as? RhaiFile ?: return

            // Add keywords
            addKeywords(result)

            // Add builtin functions
            addBuiltinFunctions(result)

            // Add custom registered functions/variables from Rust
            addCustomRegistryItems(parameters, result)

            // Add user-defined functions
            addUserFunctions(file, result)

            // Add variables in scope
            addVariablesInScope(position, file, result)

            // Add constants
            addConstants(file, result)

            // Add snippets/templates
            addSnippets(result)
        }

        private fun addKeywords(result: CompletionResultSet) {
            KEYWORDS.forEach { (keyword, description) ->
                result.addElement(
                    LookupElementBuilder.create(keyword)
                        .withTypeText(description)
                        .withIcon(AllIcons.Nodes.Tag)
                        .bold()
                        .withInsertHandler(KeywordInsertHandler(keyword))
                )
            }
        }

        private fun addBuiltinFunctions(result: CompletionResultSet) {
            BUILTIN_FUNCTIONS.forEach { (name, info) ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText(info.returnType)
                        .withTailText("(${info.params})", true)
                        .withIcon(AllIcons.Nodes.Method)
                        .withInsertHandler(FunctionInsertHandler(info.params.isNotEmpty()))
                )
            }
        }

        private fun addCustomRegistryItems(parameters: CompletionParameters, result: CompletionResultSet) {
            val project = parameters.position.project
            val registry = RhaiCustomRegistrySettings.getInstance(project)

            // Add custom functions
            registry.getCustomFunctionList().forEach { name ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText("Rust function")
                        .withTailText("(...)", true)
                        .withIcon(AllIcons.Nodes.Method)
                        .withInsertHandler(FunctionInsertHandler(true))
                )
            }

            // Add custom variables
            registry.getCustomVariableList().forEach { name ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText("Rust variable")
                        .withIcon(AllIcons.Nodes.Variable)
                )
            }

            // Add custom types
            registry.getCustomTypeList().forEach { name ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText("Rust type")
                        .withIcon(AllIcons.Nodes.Class)
                )
            }
        }

        private fun addUserFunctions(file: RhaiFile, result: CompletionResultSet) {
            PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java).forEach { func ->
                val name = func.identifier.text
                val params = func.parameters?.parameterList?.joinToString(", ") { it.pattern.text } ?: ""
                val returnType = func.returnType?.text?.removePrefix("->")?.trim() ?: "Dynamic"

                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText(returnType)
                        .withTailText("($params)", true)
                        .withIcon(AllIcons.Nodes.Function)
                        .withInsertHandler(FunctionInsertHandler(params.isNotEmpty()))
                )
            }
        }

        private fun addVariablesInScope(position: PsiElement, file: RhaiFile, result: CompletionResultSet) {
            val offset = position.textOffset

            // Add let declarations
            PsiTreeUtil.findChildrenOfType(file, RhaiLetDeclaration::class.java)
                .filter { it.textOffset < offset }
                .forEach { letDecl ->
                    val name = letDecl.pattern.text
                    val type = letDecl.typeAnnotation?.text?.removePrefix(":")?.trim()
                        ?: inferType(letDecl.expression)

                    result.addElement(
                        LookupElementBuilder.create(name)
                            .withTypeText(type)
                            .withIcon(AllIcons.Nodes.Variable)
                    )
                }

            // Add parameters if inside a function
            val func = PsiTreeUtil.getParentOfType(position, RhaiFunctionDefinition::class.java)
            func?.parameters?.parameterList?.forEach { param ->
                val name = param.pattern.text
                val type = param.typeAnnotation?.text?.removePrefix(":")?.trim() ?: "Dynamic"

                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText(type)
                        .withIcon(AllIcons.Nodes.Parameter)
                )
            }

            // Add closure parameters if inside a closure
            val closure = PsiTreeUtil.getParentOfType(position, RhaiClosureExpr::class.java)
            closure?.closureParams?.closureParamList?.forEach { param ->
                result.addElement(
                    LookupElementBuilder.create(param.text)
                        .withTypeText("Dynamic")
                        .withIcon(AllIcons.Nodes.Parameter)
                )
            }

            // Add for loop variables
            val forStmt = PsiTreeUtil.getParentOfType(position, RhaiForStatement::class.java)
            forStmt?.let {
                it.node.findChildByType(RhaiTypes.IDENTIFIER)?.let { idNode ->
                    result.addElement(
                        LookupElementBuilder.create(idNode.text)
                            .withTypeText("Dynamic")
                            .withIcon(AllIcons.Nodes.Variable)
                    )
                }
            }
        }

        private fun addConstants(file: RhaiFile, result: CompletionResultSet) {
            PsiTreeUtil.findChildrenOfType(file, RhaiConstDeclaration::class.java).forEach { constDecl ->
                constDecl.node.findChildByType(RhaiTypes.IDENTIFIER)?.let { idNode ->
                    result.addElement(
                        LookupElementBuilder.create(idNode.text)
                            .withTypeText("const")
                            .withIcon(AllIcons.Nodes.Constant)
                    )
                }
            }
        }

        private fun addSnippets(result: CompletionResultSet) {
            SNIPPETS.forEach { (name, snippet) ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText("snippet")
                        .withIcon(AllIcons.Nodes.Template)
                        .withInsertHandler { ctx, _ ->
                            val editor = ctx.editor
                            val document = editor.document
                            val startOffset = ctx.startOffset
                            val tailOffset = ctx.tailOffset

                            document.replaceString(startOffset, tailOffset, snippet.template)

                            // Move caret to first placeholder
                            val caretOffset = startOffset + snippet.template.indexOf("\$END\$").let {
                                if (it >= 0) it else snippet.template.length
                            }
                            editor.caretModel.moveToOffset(caretOffset)

                            // Remove $END$ marker
                            val text = document.text
                            val endMarker = text.indexOf("\$END\$", startOffset)
                            if (endMarker >= 0) {
                                document.deleteString(endMarker, endMarker + 5)
                            }
                        }
                )
            }
        }

        private fun inferType(expression: RhaiExpression?): String {
            if (expression == null) return "Dynamic"

            val text = expression.text.trim()
            return when {
                text.startsWith("\"") || text.startsWith("`") -> "String"
                text.startsWith("[") -> "Array"
                text.startsWith("#{") -> "Map"
                text == "true" || text == "false" -> "bool"
                text == "null" -> "null"
                text.toIntOrNull() != null -> "int"
                text.toFloatOrNull() != null -> "float"
                else -> "Dynamic"
            }
        }
    }

    private class RhaiMethodCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            // Add common methods for all types
            COMMON_METHODS.forEach { (name, info) ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText(info.returnType)
                        .withTailText("(${info.params})", true)
                        .withIcon(AllIcons.Nodes.Method)
                        .withInsertHandler(FunctionInsertHandler(info.params.isNotEmpty()))
                )
            }

            // Add array methods
            ARRAY_METHODS.forEach { (name, info) ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText(info.returnType)
                        .withTailText("(${info.params})", true)
                        .withIcon(AllIcons.Nodes.Method)
                        .withInsertHandler(FunctionInsertHandler(info.params.isNotEmpty()))
                )
            }

            // Add string methods
            STRING_METHODS.forEach { (name, info) ->
                result.addElement(
                    LookupElementBuilder.create(name)
                        .withTypeText(info.returnType)
                        .withTailText("(${info.params})", true)
                        .withIcon(AllIcons.Nodes.Method)
                        .withInsertHandler(FunctionInsertHandler(info.params.isNotEmpty()))
                )
            }
        }
    }

    private class KeywordInsertHandler(private val keyword: String) : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, item: LookupElement) {
            val editor = context.editor
            val document = editor.document

            when (keyword) {
                "if", "while", "for", "switch" -> {
                    document.insertString(context.tailOffset, " ")
                    editor.caretModel.moveToOffset(context.tailOffset + 1)
                }
                "fn" -> {
                    document.insertString(context.tailOffset, " () {\n    \n}")
                    editor.caretModel.moveToOffset(context.tailOffset + 2)
                }
                "let", "const" -> {
                    document.insertString(context.tailOffset, " ")
                    editor.caretModel.moveToOffset(context.tailOffset + 1)
                }
                "return", "throw" -> {
                    document.insertString(context.tailOffset, " ")
                    editor.caretModel.moveToOffset(context.tailOffset + 1)
                }
                "import" -> {
                    document.insertString(context.tailOffset, " \"\"")
                    editor.caretModel.moveToOffset(context.tailOffset + 2)
                }
            }
        }
    }

    private class FunctionInsertHandler(private val hasParams: Boolean) : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, item: LookupElement) {
            val editor = context.editor
            val document = editor.document

            if (hasParams) {
                document.insertString(context.tailOffset, "()")
                editor.caretModel.moveToOffset(context.tailOffset + 1)
            } else {
                document.insertString(context.tailOffset, "()")
                editor.caretModel.moveToOffset(context.tailOffset + 2)
            }
        }
    }

    data class FunctionInfo(val params: String, val returnType: String)
    data class Snippet(val template: String)

    companion object {
        private val KEYWORDS = mapOf(
            "let" to "variable declaration",
            "const" to "constant declaration",
            "fn" to "function definition",
            "if" to "conditional statement",
            "else" to "else branch",
            "while" to "while loop",
            "for" to "for loop",
            "in" to "iterator",
            "loop" to "infinite loop",
            "break" to "break from loop",
            "continue" to "continue loop",
            "return" to "return from function",
            "throw" to "throw exception",
            "try" to "try block",
            "catch" to "catch exception",
            "switch" to "switch expression",
            "import" to "import module",
            "export" to "export item",
            "module" to "module declaration",
            "private" to "private visibility",
            "pub" to "public visibility",
            "true" to "boolean literal",
            "false" to "boolean literal",
            "null" to "null value",
            "this" to "current object",
            "global" to "global scope"
        )

        private val BUILTIN_FUNCTIONS = mapOf(
            "print" to FunctionInfo("value", "()"),
            "println" to FunctionInfo("value", "()"),
            "debug" to FunctionInfo("value", "()"),
            "type_of" to FunctionInfo("value", "String"),
            "is_def" to FunctionInfo("name", "bool"),
            "to_string" to FunctionInfo("value", "String"),
            "to_int" to FunctionInfo("value", "int"),
            "to_float" to FunctionInfo("value", "float"),
            "len" to FunctionInfo("value", "int"),
            "abs" to FunctionInfo("x", "int|float"),
            "sin" to FunctionInfo("x", "float"),
            "cos" to FunctionInfo("x", "float"),
            "tan" to FunctionInfo("x", "float"),
            "sqrt" to FunctionInfo("x", "float"),
            "exp" to FunctionInfo("x", "float"),
            "ln" to FunctionInfo("x", "float"),
            "log" to FunctionInfo("x, base", "float"),
            "floor" to FunctionInfo("x", "float"),
            "ceiling" to FunctionInfo("x", "float"),
            "round" to FunctionInfo("x", "float"),
            "push" to FunctionInfo("array, value", "()"),
            "pop" to FunctionInfo("array", "Dynamic"),
            "shift" to FunctionInfo("array", "Dynamic"),
            "insert" to FunctionInfo("array, index, value", "()"),
            "remove" to FunctionInfo("array, index", "Dynamic"),
            "reverse" to FunctionInfo("array", "()"),
            "sort" to FunctionInfo("array", "()"),
            "keys" to FunctionInfo("map", "Array"),
            "values" to FunctionInfo("map", "Array"),
            "contains" to FunctionInfo("container, value", "bool"),
            "split" to FunctionInfo("string, delimiter", "Array"),
            "trim" to FunctionInfo("string", "String"),
            "replace" to FunctionInfo("string, from, to", "String"),
            "sub_string" to FunctionInfo("string, start, len", "String"),
            "timestamp" to FunctionInfo("", "Timestamp"),
            "range" to FunctionInfo("start, end", "Range"),
            "eval" to FunctionInfo("script", "Dynamic"),
            "call" to FunctionInfo("fn_ptr, args...", "Dynamic")
        )

        private val ARRAY_METHODS = mapOf(
            "len" to FunctionInfo("", "int"),
            "push" to FunctionInfo("value", "()"),
            "pop" to FunctionInfo("", "Dynamic"),
            "shift" to FunctionInfo("", "Dynamic"),
            "insert" to FunctionInfo("index, value", "()"),
            "remove" to FunctionInfo("index", "Dynamic"),
            "reverse" to FunctionInfo("", "()"),
            "sort" to FunctionInfo("", "()"),
            "map" to FunctionInfo("closure", "Array"),
            "filter" to FunctionInfo("predicate", "Array"),
            "reduce" to FunctionInfo("initial, reducer", "Dynamic"),
            "reduce_rev" to FunctionInfo("initial, reducer", "Dynamic"),
            "some" to FunctionInfo("predicate", "bool"),
            "all" to FunctionInfo("predicate", "bool"),
            "find" to FunctionInfo("predicate", "Dynamic"),
            "find_map" to FunctionInfo("mapper", "Dynamic"),
            "for_each" to FunctionInfo("action", "()"),
            "index_of" to FunctionInfo("value", "int"),
            "contains" to FunctionInfo("value", "bool"),
            "drain" to FunctionInfo("start, end", "Array"),
            "retain" to FunctionInfo("predicate", "()"),
            "splice" to FunctionInfo("start, count, items", "()"),
            "extract" to FunctionInfo("start, end", "Array"),
            "chop" to FunctionInfo("length", "Array")
        )

        private val STRING_METHODS = mapOf(
            "len" to FunctionInfo("", "int"),
            "to_upper" to FunctionInfo("", "String"),
            "to_lower" to FunctionInfo("", "String"),
            "trim" to FunctionInfo("", "String"),
            "trim_start" to FunctionInfo("", "String"),
            "trim_end" to FunctionInfo("", "String"),
            "contains" to FunctionInfo("substring", "bool"),
            "starts_with" to FunctionInfo("prefix", "bool"),
            "ends_with" to FunctionInfo("suffix", "bool"),
            "replace" to FunctionInfo("from, to", "String"),
            "split" to FunctionInfo("delimiter", "Array"),
            "sub_string" to FunctionInfo("start, length", "String"),
            "index_of" to FunctionInfo("substring", "int"),
            "chars" to FunctionInfo("", "Array"),
            "pad" to FunctionInfo("length, fill", "String")
        )

        private val COMMON_METHODS = mapOf(
            "to_string" to FunctionInfo("", "String"),
            "to_debug" to FunctionInfo("", "String")
        )

        private val SNIPPETS = mapOf(
            "fori" to Snippet("for i in 0..\$END\$ {\n    \n}"),
            "forr" to Snippet("for item in \$END\$ {\n    \n}"),
            "ife" to Snippet("if \$END\$ {\n    \n} else {\n    \n}"),
            "match" to Snippet("switch \$END\$ {\n    _ => ()\n}"),
            "func" to Snippet("fn \$END\$() {\n    \n}"),
            "closure" to Snippet("|x| \$END\$"),
            "trye" to Snippet("try {\n    \$END\$\n} catch (err) {\n    \n}"),
            "mape" to Snippet(".map(|x| \$END\$)"),
            "filtere" to Snippet(".filter(|x| \$END\$)"),
            "reducee" to Snippet(".reduce(0, |acc, x| \$END\$)")
        )
    }
}
