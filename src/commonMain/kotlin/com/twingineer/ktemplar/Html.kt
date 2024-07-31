package com.twingineer.ktemplar

import kotlinx.html.stream.appendHTML

public val TemplateScope.html: InterpolatingAppender
    get() = HtmlAppender((this as TemplateScopeBase).out)

internal class HtmlAppender(out: Appendable) : InterpolatingAppender(out) {

    override fun Appendable.appendParameter(parameter: TemplateParameter<*>) {
        parameter.value.toString().let(appendHTML()::onTagContent)
    }
}