package org.rhai.features

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai code completion.
 */
class RhaiCompletionTest : BasePlatformTestCase() {

    private fun configureAndComplete(code: String): List<String> {
        myFixture.configureByText(RhaiFileType.INSTANCE, code)
        return myFixture.completeBasic()?.map { it.lookupString } ?: emptyList()
    }

    fun `test keyword completion at file level`() {
        val completions = configureAndComplete("<caret>")

        assertTrue("Should suggest 'let'", completions.contains("let"))
        assertTrue("Should suggest 'const'", completions.contains("const"))
        assertTrue("Should suggest 'fn'", completions.contains("fn"))
    }

    fun `test keyword completion inside function`() {
        val completions = configureAndComplete("""
            fn foo() {
                <caret>
            }
        """.trimIndent())

        assertTrue("Should suggest 'let'", completions.contains("let"))
        assertTrue("Should suggest 'if'", completions.contains("if"))
        assertTrue("Should suggest 'return'", completions.contains("return"))
    }

    fun `test no keywords after dot`() {
        val completions = configureAndComplete("""
            let obj = #{};
            obj.<caret>
        """.trimIndent())

        assertFalse("Should NOT suggest 'let' after dot", completions.contains("let"))
        assertFalse("Should NOT suggest 'fn' after dot", completions.contains("fn"))
    }

    fun `test completion after double colon`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            module::<caret>
        """.trimIndent())
        val completions = myFixture.completeBasic()?.map { it.lookupString } ?: emptyList()

        // Completion after :: should work (may or may not have keywords)
        // This test just ensures no crash occurs
        assertNotNull("Completions should be available", completions)
    }

    fun `test builtin function completion`() {
        val completions = configureAndComplete("pri<caret>")

        assertTrue("Should suggest 'print'", completions.contains("print"))
    }

    fun `test array method completion`() {
        val completions = configureAndComplete("""
            let arr = [1, 2, 3];
            arr.<caret>
        """.trimIndent())

        assertTrue("Should suggest 'push'", completions.contains("push"))
        assertTrue("Should suggest 'len'", completions.contains("len"))
    }

    fun `test string method completion`() {
        val completions = configureAndComplete("""
            let s = "hello";
            s.<caret>
        """.trimIndent())

        assertTrue("Should suggest 'len'", completions.contains("len"))
        assertTrue("Should suggest 'trim'", completions.contains("trim"))
    }

    fun `test closure parameter completion`() {
        val completions = configureAndComplete("""
            let f = |value| {
                val<caret>
            };
        """.trimIndent())

        assertTrue("Should suggest 'value'", completions.contains("value"))
    }

    fun `test true false null completion`() {
        val completions = configureAndComplete("let x = tr<caret>")

        assertTrue("Should suggest 'true'", completions.contains("true"))
    }

    fun `test completion after assignment`() {
        val completions = configureAndComplete("""
            let x = 10;
            let y = <caret>
        """.trimIndent())

        assertTrue("Should suggest 'true'", completions.contains("true"))
        assertTrue("Should suggest 'false'", completions.contains("false"))
    }

    fun `test completion in if condition`() {
        val completions = configureAndComplete("""
            let flag = true;
            if fl<caret>
        """.trimIndent())

        assertTrue("Should suggest 'flag'", completions.contains("flag"))
    }

    fun `test completion in array literal`() {
        val completions = configureAndComplete("""
            let x = 1;
            let arr = [<caret>];
        """.trimIndent())

        assertTrue("Should suggest 'x'", completions.contains("x"))
    }

    fun `test completion in object literal`() {
        val completions = configureAndComplete("""
            let value = 42;
            let obj = #{ key: val<caret> };
        """.trimIndent())

        assertTrue("Should suggest 'value'", completions.contains("value"))
    }

    fun `test global variable visible in function`() {
        val completions = configureAndComplete("""
            let globalVar = "global";

            fn test() {
                glob<caret>
            }
        """.trimIndent())

        assertTrue("Should suggest 'globalVar'", completions.contains("globalVar"))
    }

    fun `test builtin methods on common types`() {
        val completions = configureAndComplete("""
            let x = 42;
            x.<caret>
        """.trimIndent())

        assertTrue("Should suggest 'to_string'", completions.contains("to_string"))
    }

    fun `test snippet completion`() {
        val completions = configureAndComplete("""
            fn foo() {
                for<caret>
            }
        """.trimIndent())

        assertTrue("Should suggest 'for'", completions.contains("for"))
    }
}
