package org.rhai.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Rhai import utilities.
 */
class RhaiImportUtilsTest {

    @Test
    fun `test build import statement`() {
        val result = RhaiImportUtils.buildImportStatement("utils", "utils")
        assertEquals("import \"utils\" as utils;\n", result)
    }

    @Test
    fun `test build import statement with path`() {
        val result = RhaiImportUtils.buildImportStatement("../lib/helpers", "helpers")
        assertEquals("import \"../lib/helpers\" as helpers;\n", result)
    }

    @Test
    fun `test find import alias in text`() {
        val fileText = """
            import "utils" as utils;
            import "math" as m;

            let x = utils::func();
        """.trimIndent()

        val alias = RhaiImportUtils.findImportAliasInText(fileText, "utils")
        assertEquals("utils", alias)

        val mathAlias = RhaiImportUtils.findImportAliasInText(fileText, "math")
        assertEquals("m", mathAlias)
    }

    @Test
    fun `test find import alias not found`() {
        val fileText = """
            import "utils" as utils;
        """.trimIndent()

        val alias = RhaiImportUtils.findImportAliasInText(fileText, "nonexistent")
        assertNull(alias)
    }

    @Test
    fun `test has import in text`() {
        val fileText = """
            import "utils" as utils;
            let x = 1;
        """.trimIndent()

        assertTrue(RhaiImportUtils.hasImportInText(fileText, "utils"))
        assertFalse(RhaiImportUtils.hasImportInText(fileText, "other"))
    }

    @Test
    fun `test find import alias with special characters in path`() {
        val fileText = """
            import "../lib/my-utils" as myUtils;
        """.trimIndent()

        val alias = RhaiImportUtils.findImportAliasInText(fileText, "../lib/my-utils")
        assertEquals("myUtils", alias)
    }

    @Test
    fun `test find import alias with dots in path`() {
        val fileText = """
            import "path.to.module" as mod;
        """.trimIndent()

        val alias = RhaiImportUtils.findImportAliasInText(fileText, "path.to.module")
        assertEquals("mod", alias)
    }

    @Test
    fun `test multiple imports same prefix`() {
        val fileText = """
            import "utils/string" as strUtils;
            import "utils/array" as arrUtils;
            import "utils" as utils;
        """.trimIndent()

        assertEquals("strUtils", RhaiImportUtils.findImportAliasInText(fileText, "utils/string"))
        assertEquals("arrUtils", RhaiImportUtils.findImportAliasInText(fileText, "utils/array"))
        assertEquals("utils", RhaiImportUtils.findImportAliasInText(fileText, "utils"))
    }
}
