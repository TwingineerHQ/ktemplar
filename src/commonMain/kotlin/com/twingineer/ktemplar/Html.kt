package com.twingineer.ktemplar

import io.exoquery.terpal.Messages
import kotlinx.html.stream.appendHTML
import org.intellij.lang.annotations.Language

public val TemplateScope.html: HtmlAppender
    get() = HtmlAppender((this as TemplateScopeBase).out)

public class HtmlAppender(out: Appendable) : InterpolatingAppender(out) {

    override fun Appendable.appendParameter(parameter: TemplateParameter<*>) {
        parameter.value.toString().let(appendHTML()::onTagContent)
    }

    override operator fun invoke(@Language("HTML") fragment: String): Unit = Messages.throwPluginNotExecuted()
}