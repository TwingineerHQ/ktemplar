package com.twingineer.ktemplar

import kotlinx.html.stream.appendHTML

internal actual fun htmlAppenderOf(out: Appendable): HtmlAppender =
    HtmlAppender(out)

public actual class HtmlAppender(out: Appendable) : AppenderBase<Any>(out), Appender<Any, Unit> {

    override fun Appendable.appendParameter(parameter: TemplateParameter<*>) {
        parameter.value.toString().let(appendHTML()::onTagContent)
    }
}