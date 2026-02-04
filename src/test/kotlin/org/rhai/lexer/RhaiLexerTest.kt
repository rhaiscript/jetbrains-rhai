package org.rhai.lexer

import com.intellij.psi.tree.IElementType
import org.junit.Assert.*
import org.junit.Test
import org.rhai.RhaiFlexAdapter
import org.rhai.RhaiTypes

/**
 * Tests for the Rhai lexer.
 * Verifies correct tokenization of Rhai source code.
 */
class RhaiLexerTest {

    private fun tokenize(text: String): List<Pair<IElementType, String>> {
        val lexer = RhaiFlexAdapter()
        lexer.start(text)
        val tokens = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return tokens
    }

    private fun tokenTypes(text: String): List<IElementType> = tokenize(text).map { it.first }

    @Test
    fun `test keywords`() {
        val keywords = listOf(
            "let" to RhaiTypes.LET,
            "const" to RhaiTypes.CONST,
            "fn" to RhaiTypes.FN,
            "if" to RhaiTypes.IF,
            "else" to RhaiTypes.ELSE,
            "while" to RhaiTypes.WHILE,
            "for" to RhaiTypes.FOR,
            "in" to RhaiTypes.IN,
            "return" to RhaiTypes.RETURN,
            "true" to RhaiTypes.TRUE,
            "false" to RhaiTypes.FALSE,
            "null" to RhaiTypes.NULL,
            "pub" to RhaiTypes.PUB,
            "private" to RhaiTypes.PRIVATE,
            "import" to RhaiTypes.IMPORT,
            "export" to RhaiTypes.EXPORT,
            "module" to RhaiTypes.MODULE,
            "throw" to RhaiTypes.THROW,
            "try" to RhaiTypes.TRY,
            "catch" to RhaiTypes.CATCH,
            "switch" to RhaiTypes.SWITCH,
            "loop" to RhaiTypes.LOOP,
            "break" to RhaiTypes.BREAK,
            "continue" to RhaiTypes.CONTINUE
        )

        for ((text, expectedType) in keywords) {
            val types = tokenTypes(text)
            assertEquals("Keyword '$text' should be recognized", listOf(expectedType), types)
        }
    }

    @Test
    fun `test identifiers`() {
        // Note: "_123" is parsed as integer literal in Rhai, not identifier
        val identifiers = listOf("foo", "bar", "_private", "camelCase", "snake_case", "PascalCase", "x1", "_x123")

        for (id in identifiers) {
            val types = tokenTypes(id)
            assertEquals("'$id' should be an identifier", listOf(RhaiTypes.IDENTIFIER), types)
        }
    }

    @Test
    fun `test integer literals`() {
        val integers = listOf("0", "42", "1000", "999999")

        for (num in integers) {
            val types = tokenTypes(num)
            assertEquals("'$num' should be an integer literal", listOf(RhaiTypes.INTEGER_LITERAL), types)
        }
    }

    @Test
    fun `test float literals`() {
        val floats = listOf("0.0", "3.14", "1.5e10", "2.5E-3")

        for (num in floats) {
            val types = tokenTypes(num)
            assertEquals("'$num' should be a float literal", listOf(RhaiTypes.FLOAT_LITERAL), types)
        }
    }

    @Test
    fun `test string literals`() {
        val strings = listOf(
            "\"hello\"",
            "\"hello world\"",
            "\"with \\\"escaped\\\" quotes\"",
            "\"with\\nnewline\""
        )

        for (str in strings) {
            val types = tokenTypes(str)
            assertEquals("'$str' should be a string literal", listOf(RhaiTypes.STRING_LITERAL), types)
        }
    }

    @Test
    fun `test operators`() {
        val operators = listOf(
            "+" to RhaiTypes.PLUS,
            "-" to RhaiTypes.MINUS,
            "*" to RhaiTypes.MUL,
            "/" to RhaiTypes.DIV,
            "%" to RhaiTypes.MOD,
            "=" to RhaiTypes.ASSIGN,
            "==" to RhaiTypes.EQ,
            "!=" to RhaiTypes.NE,
            "<" to RhaiTypes.LT,
            ">" to RhaiTypes.GT,
            "<=" to RhaiTypes.LE,
            ">=" to RhaiTypes.GE,
            "&&" to RhaiTypes.AND,
            "||" to RhaiTypes.OR,
            "!" to RhaiTypes.NOT,
            "+=" to RhaiTypes.PLUS_ASSIGN,
            "-=" to RhaiTypes.MINUS_ASSIGN,
            "*=" to RhaiTypes.MUL_ASSIGN,
            "/=" to RhaiTypes.DIV_ASSIGN,
            "=>" to RhaiTypes.ARROW,
            "::" to RhaiTypes.DOUBLE_COLON,
            ".." to RhaiTypes.RANGE,
            "**" to RhaiTypes.POW
        )

        for ((text, expectedType) in operators) {
            val types = tokenTypes(text)
            assertEquals("Operator '$text' should be recognized", listOf(expectedType), types)
        }
    }

    @Test
    fun `test punctuation`() {
        val punctuation = listOf(
            "(" to RhaiTypes.LPAREN,
            ")" to RhaiTypes.RPAREN,
            "{" to RhaiTypes.LBRACE,
            "}" to RhaiTypes.RBRACE,
            "[" to RhaiTypes.LBRACKET,
            "]" to RhaiTypes.RBRACKET,
            ";" to RhaiTypes.SEMICOLON,
            "," to RhaiTypes.COMMA,
            "." to RhaiTypes.DOT,
            ":" to RhaiTypes.COLON
        )

        for ((text, expectedType) in punctuation) {
            val types = tokenTypes(text)
            assertEquals("Punctuation '$text' should be recognized", listOf(expectedType), types)
        }
    }

    @Test
    fun `test line comment`() {
        val code = "// this is a comment"
        val types = tokenTypes(code)
        assertEquals(listOf(RhaiTypes.LINE_COMMENT), types)
    }

    @Test
    fun `test block comment`() {
        val code = "/* this is a\nmultiline comment */"
        val types = tokenTypes(code)
        assertEquals(listOf(RhaiTypes.BLOCK_COMMENT), types)
    }

    @Test
    fun `test doc comment`() {
        val code = "/// documentation comment"
        val types = tokenTypes(code)
        // Doc comments might be tokenized as DOC_COMMENT or DOC_LINE
        assertTrue("Should be a doc comment type",
            types.contains(RhaiTypes.DOC_COMMENT) || types.contains(RhaiTypes.DOC_LINE))
    }

    @Test
    fun `test function definition tokenization`() {
        val code = "fn add(a, b) { a + b }"
        val types = tokenTypes(code)

        assertTrue("Should contain FN keyword", types.contains(RhaiTypes.FN))
        assertTrue("Should contain IDENTIFIER", types.contains(RhaiTypes.IDENTIFIER))
        assertTrue("Should contain LPAREN", types.contains(RhaiTypes.LPAREN))
        assertTrue("Should contain RPAREN", types.contains(RhaiTypes.RPAREN))
        assertTrue("Should contain LBRACE", types.contains(RhaiTypes.LBRACE))
        assertTrue("Should contain RBRACE", types.contains(RhaiTypes.RBRACE))
        assertTrue("Should contain PLUS", types.contains(RhaiTypes.PLUS))
    }

    @Test
    fun `test let declaration tokenization`() {
        val code = "let x = 42;"
        val types = tokenTypes(code)

        assertEquals(RhaiTypes.LET, types[0])
        assertEquals(RhaiTypes.IDENTIFIER, types[2]) // after whitespace
        assertEquals(RhaiTypes.ASSIGN, types[4])
        assertEquals(RhaiTypes.INTEGER_LITERAL, types[6])
        assertEquals(RhaiTypes.SEMICOLON, types[7])
    }

    @Test
    fun `test closure tokenization`() {
        val code = "|x| x * 2"
        val types = tokenTypes(code)

        assertEquals(RhaiTypes.BOR, types[0]) // | is BOR (bitwise or)
        assertEquals(RhaiTypes.IDENTIFIER, types[1])
        assertEquals(RhaiTypes.BOR, types[2])
    }

    @Test
    fun `test path expression tokenization`() {
        val code = "module::function"
        val types = tokenTypes(code)

        assertTrue("Should contain IDENTIFIER", types.contains(RhaiTypes.IDENTIFIER))
        assertTrue("Should contain DOUBLE_COLON", types.contains(RhaiTypes.DOUBLE_COLON))
    }

    @Test
    fun `test complex expression tokenization`() {
        val code = "arr.map(|x| x + 1).filter(|x| x > 5)"
        val types = tokenTypes(code)

        assertTrue("Should contain multiple IDENTIFIERs", types.count { it == RhaiTypes.IDENTIFIER } >= 4)
        assertTrue("Should contain DOTs", types.contains(RhaiTypes.DOT))
        assertTrue("Should contain LPARENs", types.contains(RhaiTypes.LPAREN))
        assertTrue("Should contain BORs for closure", types.contains(RhaiTypes.BOR))
    }
}
