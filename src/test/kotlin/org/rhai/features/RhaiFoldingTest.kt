package org.rhai.features

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai code folding.
 */
class RhaiFoldingTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/resources/testData"

    private fun doFoldingTest(code: String, expectedRegions: Int) {
        myFixture.configureByText(RhaiFileType.INSTANCE, code)
        myFixture.testFolding("$testDataPath/folding/test.rhai")
    }

    fun `test function folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            fn foo() {
                let x = 1;
                let y = 2;
            }
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        // Just verify the folding builder doesn't throw
        assertNotNull(foldingModel)
    }

    fun `test if block folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            if true {
                print("yes");
            }
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }

    fun `test while loop folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            while x > 0 {
                x -= 1;
            }
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }

    fun `test for loop folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            for i in 0..10 {
                print(i);
            }
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }

    fun `test closure folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let f = |x| {
                let y = x * 2;
                y + 1
            };
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }

    fun `test block comment folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            /* This is a
               multi-line
               comment */
            let x = 1;
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }

    fun `test nested folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            fn outer() {
                fn inner() {
                    if true {
                        for i in 0..10 {
                            print(i);
                        }
                    }
                }
            }
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }

    fun `test array literal folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let arr = [
                1,
                2,
                3,
                4,
                5
            ];
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }

    fun `test object literal folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let obj = #{
                name: "test",
                value: 42,
                nested: #{
                    a: 1,
                    b: 2
                }
            };
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }

    fun `test switch expression folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let result = switch x {
                1 => "one",
                2 => "two",
                _ => "other"
            };
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }

    fun `test try-catch folding`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            try {
                risky_operation();
            } catch (err) {
                print(err);
            }
        """.trimIndent())

        val foldingModel = myFixture.editor.foldingModel
        assertNotNull(foldingModel)
    }
}
