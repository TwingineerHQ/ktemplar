package com.twingineer.ktemplar

import io.exoquery.terpal.InterpolatorFunction
import io.exoquery.terpal.Messages
import io.exoquery.terpal.ProtoInterpolator
import kotlinx.html.stream.appendHTML
import org.intellij.lang.annotations.Language

@InterpolatorFunction<HtmlAppender>(HtmlAppender::class, customReciever = true)
public fun TemplateScope.html(@Language("HTML") string: String): Unit =
    Messages.throwPluginNotExecuted()

// would ideally be private, but doing so causes the following K/JS compilation error
// IrLinkageError: Can not get instance of singleton 'HtmlAppender': No class found for symbol '[ File '/home/i/ktemplar/src/commonTest/kotlin/com/twingineer/ktemplar/HtmlTest.kt' <- com.twingineer.ktemplar/HtmlAppender|null[0] ]'
internal object HtmlAppender : ProtoInterpolator<Any?, Unit>, InterpolatingAppender() {

    override fun Appendable.appendParameter(parameter: TemplateParameter<*>) {
        parameter.value.toString().let(appendHTML()::onTagContent)
    }
}