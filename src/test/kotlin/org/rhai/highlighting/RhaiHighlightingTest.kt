package org.rhai.highlighting

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai syntax highlighting.
 */
class RhaiHighlightingTest : BasePlatformTestCase() {

    fun `test file type is recognized`() {
        val file = myFixture.configureByText(RhaiFileType.INSTANCE, "let x = 1;")
        assertEquals(RhaiFileType.INSTANCE, file.fileType)
    }

    fun `test no errors in valid code`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let x = 42;
            const PI = 3.14;

            fn add(a, b) {
                a + b
            }

            let result = add(1, 2);
            print(result);
        """.trimIndent())

        // This should not throw any exceptions
        val highlights = myFixture.doHighlighting()
        // We're just verifying it doesn't crash, not checking specific highlights
        assertNotNull(highlights)
    }

    fun `test highlighting with comments`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            // Line comment
            /* Block comment */
            /// Doc comment
            //! Module doc

            let x = 1;
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with strings`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let s1 = "hello";
            let s2 = "with \"escaped\" quotes";
            let s3 = "multi\nline";
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with numbers`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let int1 = 42;
            let int2 = 0;
            let int3 = 1_000_000;
            let float1 = 3.14;
            let float2 = 1.0e10;
            let float3 = 2.5E-3;
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with operators`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let a = 1 + 2;
            let b = 3 - 4;
            let c = 5 * 6;
            let d = 7 / 8;
            let e = 9 % 10;
            let f = a == b;
            let g = c != d;
            let h = e < f;
            let i = g > h;
            let j = a && b;
            let k = c || d;
            let l = !e;
            let m = a ** 2;
            let n = 0..10;
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with control flow`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            if x > 0 {
                print("positive");
            } else if x < 0 {
                print("negative");
            } else {
                print("zero");
            }

            while x > 0 {
                x -= 1;
            }

            for i in 0..10 {
                print(i);
            }

            loop {
                if done { break; }
            }

            switch x {
                1 => "one",
                2 => "two",
                _ => "other"
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with closures`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let f = |x| x * 2;
            let g = |a, b| a + b;
            let h = || print("hello");
            let i = |x| {
                let y = x + 1;
                y * 2
            };
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with method chains`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let result = arr
                .map(|x| x * 2)
                .filter(|x| x > 5)
                .reduce(|acc, x| acc + x, 0);
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with imports`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            import "utils" as utils;
            import "math" as m;

            let x = utils::helper();
            let y = m::add(1, 2);
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with modules`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            module math {
                pub fn add(a, b) { a + b }
                private fn helper() { }
                fn internal() { }
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with try-catch`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            try {
                risky_operation();
            } catch (err) {
                print("Error: " + err);
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with arrays and objects`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let arr = [1, 2, 3, 4, 5];
            let obj = #{
                name: "test",
                value: 42,
                nested: #{
                    a: 1,
                    b: 2
                }
            };
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }

    fun `test highlighting with type annotations`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let x: int = 42;
            let s: string = "hello";
            fn add(a: int, b: int) -> int {
                a + b
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        assertNotNull(highlights)
    }
}
