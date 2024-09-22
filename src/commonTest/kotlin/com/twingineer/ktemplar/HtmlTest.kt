package com.twingineer.ktemplar

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTest {

    @Test
    fun buildSimpleTemplate() {
        val name = "World"
        val value = buildString {
            appendTemplate {
                html(
                    """
                    Hello, $name!
                """
                )
            }
        }

        assertEquals("Hello, $name!", value)
    }

    @Test
    fun buildInjectingTemplate() {
        val name = "<script>console.log('Uh oh');</script>"
        val value = buildString {
            appendTemplate {
                html.invoke(
                    """
                    Hello, $name!
                """
                )
            }
        }

        assertEquals("Hello, &lt;script&gt;console.log('Uh oh');&lt;/script&gt;!", value)
    }
}