package org.rhai.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.rhai.*
import org.rhai.lang.RhaiFile
import org.rhai.settings.RhaiCustomRegistrySettings

class RhaiUnresolvedReferenceInspection : RhaiInspectionBase() {

    override fun getDisplayName(): String = "Unresolved reference"

    override fun getStaticDescription(): String = "Reports references to undefined variables or functions."

    override fun buildRhaiVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.elementType == RhaiTypes.IDENTIFIER) {
                    checkReference(element, holder)
                }
            }
        }
    }

    private fun checkReference(element: PsiElement, holder: ProblemsHolder) {
        val file = element.containingFile as? RhaiFile ?: return
        val name = element.text
        val parent = element.parent

        // Skip declarations
        if (parent is RhaiLetDeclaration ||
            parent is RhaiConstDeclaration ||
            parent is RhaiFunctionDefinition ||
            parent is RhaiParameter ||
            parent is RhaiPattern ||
            parent is RhaiClosureParam ||
            parent is RhaiModuleDeclaration ||
            parent is RhaiImportPath ||
            parent is RhaiImportStatement ||
            parent is RhaiLabel ||
            parent is RhaiObjectField ||
            parent is RhaiPathExpression) {  // Skip path expressions (module::item)
            return
        }

        // Skip import aliases (identifier after 'as' in import statement)
        if (isImportAlias(element)) return

        // Skip path expression parts (identifier before or after ::)
        if (isPathExpressionPart(element)) return

        // Skip property access (after dot)
        if (isPropertyAccess(element)) return

        // Skip object field names (identifier followed by colon in object literal)
        if (isObjectFieldName(element)) return

        // Skip if it's a builtin
        if (BUILTINS.contains(name)) return

        // Skip keywords that might appear as identifiers
        if (KEYWORDS.contains(name)) return

        // Skip if it's a custom registered function/variable from Rust
        val project = element.project
        val customRegistry = RhaiCustomRegistrySettings.getInstance(project)
        if (customRegistry.getCustomFunctionList().contains(name)) return
        if (customRegistry.getCustomVariableList().contains(name)) return
        if (customRegistry.getCustomTypeList().contains(name)) return

        // Check if it's a function call
        val isFunctionCall = isFunctionCall(element)

        if (isFunctionCall) {
            // Check if function is defined
            if (!isFunctionDefined(file, name) && !BUILTIN_FUNCTIONS.contains(name)) {
                holder.registerProblem(
                    element,
                    "Unresolved function '$name'",
                    ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
                )
            }
        } else {
            // Check if variable is defined
            if (!isVariableDefined(file, name, element.textOffset)) {
                holder.registerProblem(
                    element,
                    "Unresolved reference '$name'",
                    ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
                )
            }
        }
    }

    private fun isPropertyAccess(element: PsiElement): Boolean {
        var sibling = element.prevSibling
        while (sibling != null && sibling.elementType == TokenType.WHITE_SPACE) {
            sibling = sibling.prevSibling
        }
        return sibling?.elementType == RhaiTypes.DOT
    }

    private fun isObjectFieldName(element: PsiElement): Boolean {
        // Check if identifier is followed by colon (field name pattern: "name: value")
        var sibling = element.nextSibling
        while (sibling != null && sibling.elementType == TokenType.WHITE_SPACE) {
            sibling = sibling.nextSibling
        }
        if (sibling?.elementType == RhaiTypes.COLON) {
            // Make sure we're inside an object literal context
            var parent = element.parent
            while (parent != null) {
                if (parent is RhaiObjectLiteral || parent is RhaiObjectField) {
                    return true
                }
                if (parent is RhaiBlock || parent is RhaiFunctionDefinition) {
                    // Stop searching if we hit a block boundary without finding object context
                    break
                }
                parent = parent.parent
            }
        }
        return false
    }

    private fun isImportAlias(element: PsiElement): Boolean {
        // Check if identifier is preceded by 'as' keyword in import context
        var sibling = element.prevSibling
        while (sibling != null && sibling.elementType == TokenType.WHITE_SPACE) {
            sibling = sibling.prevSibling
        }
        if (sibling?.elementType == RhaiTypes.AS) {
            // Check if we're inside an import statement
            var parent = element.parent
            while (parent != null) {
                if (parent is RhaiImportStatement) {
                    return true
                }
                parent = parent.parent
            }
        }
        return false
    }

    private fun isPathExpressionPart(element: PsiElement): Boolean {
        // Check if identifier is preceded by :: or followed by ::
        var prevSibling = element.prevSibling
        while (prevSibling != null && prevSibling.elementType == TokenType.WHITE_SPACE) {
            prevSibling = prevSibling.prevSibling
        }
        if (prevSibling?.elementType == RhaiTypes.DOUBLE_COLON) {
            return true
        }

        var nextSibling = element.nextSibling
        while (nextSibling != null && nextSibling.elementType == TokenType.WHITE_SPACE) {
            nextSibling = nextSibling.nextSibling
        }
        if (nextSibling?.elementType == RhaiTypes.DOUBLE_COLON) {
            return true
        }

        // Also check if parent is a path expression
        var parent = element.parent
        while (parent != null) {
            if (parent is RhaiPathExpression) {
                return true
            }
            if (parent is RhaiPrimaryExpr || parent is RhaiPostfixExpr) {
                parent = parent.parent
                continue
            }
            break
        }

        return false
    }

    private fun isFunctionCall(element: PsiElement): Boolean {
        // Method 1: Check direct sibling (for simple cases)
        var sibling = element.nextSibling
        while (sibling != null && sibling.elementType == TokenType.WHITE_SPACE) {
            sibling = sibling.nextSibling
        }
        if (sibling?.elementType == RhaiTypes.LPAREN) {
            return true
        }

        // Method 2: Check if sibling is a postfix_op starting with LPAREN
        if (sibling is RhaiPostfixOp) {
            val firstChild = sibling.node.firstChildNode
            if (firstChild?.elementType == RhaiTypes.LPAREN) {
                return true
            }
        }

        // Method 3: Check if the identifier's parent is primary_expr
        // and next sibling of primary_expr is postfix_op with LPAREN
        val parent = element.parent
        if (parent?.elementType == RhaiTypes.PRIMARY_EXPR) {
            var parentSibling = parent.nextSibling
            while (parentSibling != null && parentSibling.node.elementType == TokenType.WHITE_SPACE) {
                parentSibling = parentSibling.nextSibling
            }
            if (parentSibling is RhaiPostfixOp) {
                val firstChild = parentSibling.node.firstChildNode
                if (firstChild?.elementType == RhaiTypes.LPAREN) {
                    return true
                }
            }
        }

        return false
    }

    private fun isFunctionDefined(file: RhaiFile, name: String): Boolean {
        // Method 1: Search through PSI tree for function definitions
        val functions = PsiTreeUtil.findChildrenOfType(file, RhaiFunctionDefinition::class.java)
        if (functions.any { it.identifier.text == name }) {
            return true
        }

        // Method 2: Fallback - search for "fn name(" pattern in text
        // This handles cases where PSI tree might be incomplete
        val text = file.text
        val pattern = Regex("""(?:pub\s+)?(?:private\s+)?fn\s+${Regex.escape(name)}\s*\(""")
        return pattern.containsMatchIn(text)
    }

    private fun getPatternName(pattern: RhaiPattern): String? {
        // Method 1: Try the generated getter
        pattern.identifier?.let { return it.text }

        // Method 2: Search for IDENTIFIER token in the pattern node
        pattern.node.findChildByType(RhaiTypes.IDENTIFIER)?.let { return it.text }

        // Method 3: Use full text if it looks like a simple identifier
        val text = pattern.text.trim()
        if (text.isNotBlank() && text.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) {
            return text
        }

        return null
    }

    private fun isVariableDefined(file: RhaiFile, name: String, offset: Int): Boolean {
        val elementAtOffset = file.findElementAt(offset) ?: return false

        // Check function parameters (find all enclosing functions)
        var funcParent: PsiElement? = elementAtOffset
        while (funcParent != null) {
            val func = PsiTreeUtil.getParentOfType(funcParent, RhaiFunctionDefinition::class.java, false)
            if (func != null) {
                if (func.parameters?.parameterList?.any { getPatternName(it.pattern) == name } == true) {
                    return true
                }
                funcParent = func.parent
            } else {
                break
            }
        }

        // Check closure parameters (including nested closures)
        var closureParent: PsiElement? = elementAtOffset
        while (closureParent != null) {
            val closure = PsiTreeUtil.getParentOfType(closureParent, RhaiClosureExpr::class.java, false)
            if (closure != null) {
                if (closure.closureParams?.closureParamList?.any { getPatternName(it.pattern) == name } == true) {
                    return true
                }
                closureParent = closure.parent
            } else {
                break
            }
        }

        // Check for loop variables
        var forParent: PsiElement? = elementAtOffset
        while (forParent != null) {
            val forStmt = PsiTreeUtil.getParentOfType(forParent, RhaiForStatement::class.java, false)
            if (forStmt != null) {
                if (getPatternName(forStmt.pattern) == name) {
                    return true
                }
                forParent = forStmt.parent
            } else {
                break
            }
        }

        // Check let declarations within the enclosing scope
        // First, find the enclosing function or file scope
        val enclosingScope = PsiTreeUtil.getParentOfType(elementAtOffset, RhaiFunctionDefinition::class.java)
            ?: file

        val letDecls = PsiTreeUtil.findChildrenOfType(enclosingScope, RhaiLetDeclaration::class.java)
        if (letDecls.any { getPatternName(it.pattern) == name && it.textOffset < offset }) {
            return true
        }

        // Check const declarations (visible from anywhere in file)
        val constDecls = PsiTreeUtil.findChildrenOfType(file, RhaiConstDeclaration::class.java)
        if (constDecls.any { getPatternName(it.pattern) == name }) {
            return true
        }

        // Fallback: search for "let name" or "const name" pattern in enclosing scope text
        val scopeText = enclosingScope.text
        val scopeStartOffset = enclosingScope.textOffset
        val relativeOffset = offset - scopeStartOffset

        // Check for let declaration before current position
        val letPattern = Regex("""let\s+${Regex.escape(name)}\s*[=;:\[]""")
        letPattern.find(scopeText)?.let { match ->
            if (match.range.first < relativeOffset) {
                return true
            }
        }

        // Check for const declaration anywhere in file
        val constPattern = Regex("""const\s+${Regex.escape(name)}\s*[=;:\[]""")
        if (constPattern.containsMatchIn(file.text)) {
            return true
        }

        return false
    }

    companion object {
        private val BUILTINS = setOf(
            "print", "println", "debug", "type_of", "is_def",
            "to_string", "to_int", "to_float", "to_decimal",
            "abs", "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "sqrt", "exp", "ln", "log", "log10",
            "floor", "ceiling", "round", "int", "fraction",
            "push", "pop", "shift", "insert", "remove", "reverse", "sort",
            "drain", "retain", "splice", "extract", "chop",
            "map", "filter", "reduce", "reduce_rev", "some", "all",
            "for_each", "index_of", "find", "find_map",
            "keys", "values", "len", "contains",
            "trim", "replace", "split", "sub_string", "pad",
            "timestamp", "range", "eval", "call"
        )

        private val BUILTIN_FUNCTIONS = BUILTINS

        private val KEYWORDS = setOf(
            "let", "const", "fn", "if", "else", "while", "for", "in",
            "loop", "break", "continue", "return", "throw", "try", "catch",
            "switch", "default", "import", "export", "module", "private", "pub",
            "true", "false", "null", "this", "global", "is", "as"
        )
    }
}
