package com.twingineer.ktemplar

import com.twingineer.ktemplar.StandardTemplateType.HTML
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTest {

    @Test
    fun buildSimpleTemplate() {
        val name = "World"
        val value = HTML.build {
            html(
                """
                    Hello, ${!name}!
                """
            )
        }

        assertEquals("Hello, $name!", value)
    }
}