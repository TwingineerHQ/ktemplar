package com.twingineer.ktemplar

import io.exoquery.terpal.InterpolatorFunction
import io.exoquery.terpal.Messages
import io.exoquery.terpal.ProtoInterpolator
import kotlinx.serialization.json.JsonPrimitive
import org.intellij.lang.annotations.Language

@InterpolatorFunction<JsonAppender>(JsonAppender::class, customReciever = true)
public fun TemplateScope.json(@Language("JSON") string: String): Unit =
    Messages.throwPluginNotExecuted()

// would ideally be private, but doing so causes the following K/JS compilation error
// IrLinkageError: Can not get instance of singleton 'JsonAppender': No class found for symbol '[ File '/home/i/ktemplar/src/commonTest/kotlin/com/twingineer/ktemplar/JsonTest.kt' <- com.twingineer.ktemplar/JsonAppender|null[0] ]'
internal object JsonAppender : ProtoInterpolator<Any?, Unit>, InterpolatingAppender() {

    override fun Appendable.appendParameter(parameter: TemplateParameter<*>) {
        parameter.value.toString()
            .let(::JsonPrimitive)
            .let(JsonPrimitive::toString)
            .let { it.substring(1, it.length - 1) }
            .let { this.append(it) }
    }
}