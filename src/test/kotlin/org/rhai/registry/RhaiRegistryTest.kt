package org.rhai.registry

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Rhai registry parsing.
 */
class RhaiRegistryTest {

    @Test
    fun `test parse register_fn`() {
        val code = """
            engine.register_fn("my_func", my_func);
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)

        assertTrue("Should find one registration", entries.isNotEmpty())
        val entry = entries.find { it.name == "my_func" }
        assertNotNull("Should find my_func", entry)
        assertEquals(RegistrationType.FUNCTION, entry?.type)
    }

    @Test
    fun `test parse register_fn with parameters`() {
        val code = """
            engine.register_fn("add", |a: i64, b: i64| a + b);
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)

        assertTrue("Should find registration", entries.isNotEmpty())
        val entry = entries.find { it.name == "add" }
        assertNotNull("Should find add function", entry)
    }

    @Test
    fun `test parse register_type`() {
        val code = """
            engine.register_type::<MyType>();
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)

        assertTrue("Should find type registration", entries.isNotEmpty())
        val entry = entries.find { it.name == "MyType" }
        assertNotNull("Should find MyType", entry)
        assertEquals(RegistrationType.TYPE, entry?.type)
    }

    @Test
    fun `test parse register_type_with_name`() {
        val code = """
            engine.register_type_with_name::<MyStruct>("MyAlias");
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)

        val entry = entries.find { it.name == "MyAlias" }
        assertNotNull("Should find type with custom name", entry)
    }

    @Test
    fun `test parse register_get`() {
        val code = """
            engine.register_get("value", MyType::get_value);
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)

        val entry = entries.find { it.name == "value" }
        assertNotNull("Should find getter", entry)
        assertEquals(RegistrationType.GETTER, entry?.type)
    }

    @Test
    fun `test parse register_set`() {
        val code = """
            engine.register_set("value", MyType::set_value);
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)

        val entry = entries.find { it.name == "value" }
        assertNotNull("Should find setter", entry)
        assertEquals(RegistrationType.SETTER, entry?.type)
    }

    @Test
    fun `test parse register_get_set`() {
        val code = """
            engine.register_get_set("prop", MyType::get_prop, MyType::set_prop);
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)

        assertTrue("Should find getter and setter", entries.size >= 2)
    }

    @Test
    fun `test parse multiple registrations`() {
        val code = """
            let engine = Engine::new();
            engine.register_fn("func1", func1);
            engine.register_fn("func2", func2);
            engine.register_type::<MyType>();
            engine.register_get("value", MyType::get_value);
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)

        assertTrue("Should find multiple registrations", entries.size >= 4)
        assertNotNull(entries.find { it.name == "func1" })
        assertNotNull(entries.find { it.name == "func2" })
        assertNotNull(entries.find { it.name == "MyType" })
        assertNotNull(entries.find { it.name == "value" })
    }

    @Test
    fun `test parse with comments`() {
        val code = """
            // Register main function
            engine.register_fn("main", main_func);
            /* Multi-line
               comment */
            engine.register_fn("helper", helper_func);
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)

        assertTrue("Should find registrations despite comments", entries.size >= 2)
    }

    @Test
    fun `test empty code`() {
        val entries = RhaiRegistrationParser.parseRustSource("")
        assertTrue("Empty code should return empty list", entries.isEmpty())
    }

    @Test
    fun `test code without registrations`() {
        val code = """
            fn main() {
                println!("Hello");
            }
        """.trimIndent()

        val entries = RhaiRegistrationParser.parseRustSource(code)
        assertTrue("Code without registrations should return empty list", entries.isEmpty())
    }

    @Test
    fun `test parse single line`() {
        val line = """engine.register_fn("test_func", test_func);"""
        val entry = RhaiRegistrationParser.parseSingleLine(line)

        assertNotNull("Should parse single line", entry)
        assertEquals("test_func", entry?.name)
        assertEquals(RegistrationType.FUNCTION, entry?.type)
    }

    @Test
    fun `test valid rhai identifier`() {
        assertTrue(RhaiRegistrationParser.isValidRhaiIdentifier("foo"))
        assertTrue(RhaiRegistrationParser.isValidRhaiIdentifier("_bar"))
        assertTrue(RhaiRegistrationParser.isValidRhaiIdentifier("foo_bar"))
        assertTrue(RhaiRegistrationParser.isValidRhaiIdentifier("foo123"))

        assertFalse(RhaiRegistrationParser.isValidRhaiIdentifier(""))
        assertFalse(RhaiRegistrationParser.isValidRhaiIdentifier("123foo"))
        assertFalse(RhaiRegistrationParser.isValidRhaiIdentifier("foo-bar"))
    }
}
