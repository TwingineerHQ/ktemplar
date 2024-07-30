package com.twingineer.ktemplar

import kotlinx.html.stream.appendHTML

internal fun htmlAppenderOf(out: Appendable): HtmlAppender =
    HtmlAppender(out)

public val TemplateScope.html: HtmlAppender
    get() = (this as TemplateScopeBase).appenders.getOrPut(HtmlAppender::class) { htmlAppenderOf(out) } as HtmlAppender

public class HtmlAppender(out: Appendable) : InterpolatingAppender(out) {

    override fun Appendable.appendParameter(parameter: TemplateParameter<*>) {
        parameter.value.toString().let(appendHTML()::onTagContent)
    }
}