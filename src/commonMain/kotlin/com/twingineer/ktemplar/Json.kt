package com.twingineer.ktemplar

import io.exoquery.terpal.Messages
import kotlinx.serialization.json.JsonPrimitive
import org.intellij.lang.annotations.Language

public val TemplateScope.json: JsonAppender
    get() = JsonAppender((this as TemplateScopeBase).out)

public class JsonAppender(out: Appendable) : InterpolatingAppender(out) {

    override fun Appendable.appendParameter(parameter: TemplateParameter<*>) {
        parameter.value.toString()
            .let(::JsonPrimitive)
            .let(JsonPrimitive::toString)
            .let { it.substring(1, it.length - 1) }
            .let { this.append(it) }
    }

    override fun invoke(@Language("JSON") fragment: String): Unit = Messages.throwPluginNotExecuted()
}