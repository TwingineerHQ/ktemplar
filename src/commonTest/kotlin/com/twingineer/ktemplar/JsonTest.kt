package com.twingineer.ktemplar

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonTest {

    @Test
    fun buildSimpleTemplate() {
        val name = "World"
        val value = buildString {
            appendTemplate {
                json(
                    """
                        { "Hello": "$name" }
                    """
                )
            }
        }

        assertEquals(
            """
                { "Hello": "World" }
            """.trimMarginOrIndent(),
            value
        )
    }

    @Test
    fun buildInjectingTemplate() {
        val name = "\", \"uh\": { \"oh\" }"
        val value = buildString {
            appendTemplate {
                json(
                    """
                        { "Hello": "$name" }
                    """
                )
            }
        }

        assertEquals(
            """
                { "Hello": "\", \"uh\": { \"oh\" }" }
            """.trimMarginOrIndent(),
            value
        )
    }
}