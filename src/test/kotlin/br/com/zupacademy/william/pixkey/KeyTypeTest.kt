package br.com.zupacademy.william.pixkey

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class KeyTypeTest {

    @Nested
    inner class RANDOM {
        @Test
        fun `should be valid when it's null or empty`() {
            with(KeyType.RANDOM) {
                assertTrue(validate(null))
                assertTrue(validate(""))
            }
        }

        @Test
        fun `should be invalid when it's not null or empty`() {
            with(KeyType.RANDOM) {
                assertFalse(validate("random_value"))
            }
        }
    }

    @Nested
    inner class CPF {
        @Test
        fun `should be valid when it's a valid CPF`() {
            with(KeyType.CPF) {
                assertTrue(validate("89364705076"))
            }
        }

        @Test
        fun `should be invalid when it's not a valid CPF`() {
            with(KeyType.CPF) {
                assertFalse(validate("89364712076"))
            }
        }

        @Test
        fun `should be invalid when it has characters`() {
            with(KeyType.CPF) {
                assertFalse(validate("8936470507A"))
            }
        }

        @Test
        fun `should be invalid when it's blank`() {
            with(KeyType.CPF) {
                assertFalse(validate(""))
            }
        }
    }

    @Nested
    inner class PHONE {
        @Test
        fun `should be valid when it's a valid phone number`() {
            with(KeyType.PHONE) {
                assertTrue(validate("+5515999998888"))
            }
        }

        @Test
        fun `should be invalid when it's a invalid phone number`() {
            with(KeyType.PHONE) {
                assertFalse(validate("15999998888"))
            }
        }

        @Test
        fun `should be invalid when it's blank`() {
            with(KeyType.PHONE) {
                assertFalse(validate(""))
            }
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        fun `should be valid when it's a valid email`() {
            with(KeyType.EMAIL) {
                assertTrue(validate("test@test.com"))
            }
        }

        @Test
        fun `should be invalid when it's blank`() {
            with(KeyType.EMAIL) {
                assertFalse(validate(""))
            }
        }
    }
}