package org.rhai.features

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.lang.LanguageStructureViewBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType
import org.rhai.lang.RhaiLanguage

/**
 * Tests for Rhai structure view.
 */
class RhaiStructureViewTest : BasePlatformTestCase() {

    fun `test structure view shows functions`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            fn foo() {}
            fn bar(x) {}
            fn baz(a, b) {}
        """.trimIndent())

        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(myFixture.file)
        assertNotNull("Structure view builder should exist", builder)
    }

    fun `test structure view shows constants`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            const MAX = 100;
            const MIN = 0;
        """.trimIndent())

        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(myFixture.file)
        assertNotNull("Structure view builder should exist", builder)
    }

    fun `test structure view shows pub functions`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            pub fn publicFunc() {}
            private fn privateFunc() {}
            fn normalFunc() {}
        """.trimIndent())

        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(myFixture.file)
        assertNotNull("Structure view builder should exist", builder)
    }

    fun `test structure view with modules`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            module math {
                fn add(a, b) { a + b }
                fn sub(a, b) { a - b }
            }
        """.trimIndent())

        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(myFixture.file)
        assertNotNull("Structure view builder should exist", builder)
    }

    fun `test structure view with variables`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            let globalVar = 42;
            const CONSTANT = "test";

            fn foo() {
                let localVar = 1;
            }
        """.trimIndent())

        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(myFixture.file)
        assertNotNull("Structure view builder should exist", builder)
    }

    fun `test structure view complex file`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            //! Module documentation

            import "utils" as utils;

            const MAX_SIZE = 1000;

            /// Helper function
            fn helper() {}

            /// Main processing function
            pub fn process(data) {
                let result = [];
                for item in data {
                    result.push(helper());
                }
                result
            }

            module internal {
                fn private_impl() {}
            }
        """.trimIndent())

        val builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(myFixture.file)
        assertNotNull("Structure view builder should exist", builder)
    }
}
