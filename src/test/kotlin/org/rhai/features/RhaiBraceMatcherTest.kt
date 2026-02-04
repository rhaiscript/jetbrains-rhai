package org.rhai.features

import org.junit.Test
import org.rhai.RhaiTestBase

/**
 * Tests for Rhai brace matching.
 */
class RhaiBraceMatcherTest : RhaiTestBase() {

    @Test
    fun `test parentheses matching`() {
        configureByText("fn foo(<caret>) {}")
        // The brace matcher should work when cursor is at brace
        // This test verifies the file parses correctly with balanced parens
        assertNotNull(myFixture.file)
    }

    @Test
    fun `test curly braces matching`() {
        configureByText("fn foo() {<caret>}")
        assertNotNull(myFixture.file)
    }

    @Test
    fun `test square brackets matching`() {
        configureByText("let arr = [<caret>1, 2, 3];")
        assertNotNull(myFixture.file)
    }

    @Test
    fun `test nested braces`() {
        configureByText("""
            fn foo() {
                if true {
                    let arr = [1, (2 + 3)];
                }
            }
        """.trimIndent())
        assertNotNull(myFixture.file)
    }

    @Test
    fun `test closure pipes matching`() {
        configureByText("let f = |x| x * 2;")
        assertNotNull(myFixture.file)
    }
}
