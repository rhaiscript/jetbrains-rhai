package org.rhai.features

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rhai.lang.RhaiFileType

/**
 * Tests for Rhai commenter functionality.
 */
class RhaiCommenterTest : BasePlatformTestCase() {

    fun `test commenter is registered`() {
        val commenter = RhaiCommenter()
        assertEquals("//", commenter.lineCommentPrefix)
        assertEquals("/*", commenter.blockCommentPrefix)
        assertEquals("*/", commenter.blockCommentSuffix)
    }

    fun `test line comment format`() {
        val commenter = RhaiCommenter()
        assertNotNull(commenter.lineCommentPrefix)
        assertTrue(commenter.lineCommentPrefix!!.startsWith("//"))
    }

    fun `test block comment format`() {
        val commenter = RhaiCommenter()
        assertNotNull(commenter.blockCommentPrefix)
        assertNotNull(commenter.blockCommentSuffix)
        assertEquals("/*", commenter.blockCommentPrefix)
        assertEquals("*/", commenter.blockCommentSuffix)
    }

    fun `test file with comments parses`() {
        myFixture.configureByText(RhaiFileType.INSTANCE, """
            // Line comment
            let x = 1;
            /* Block comment */
            let y = 2;
        """.trimIndent())
        assertNotNull(myFixture.file)
    }
}
