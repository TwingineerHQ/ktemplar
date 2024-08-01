package com.twingineer.ktemplar

import kotlinx.serialization.json.JsonPrimitive

public val TemplateScope.json: InterpolatingAppender
    get() = JsonAppender((this as TemplateScopeBase).out)

internal class JsonAppender(out: Appendable) : InterpolatingAppender(out) {

    override fun Appendable.appendParameter(parameter: TemplateParameter<*>) {
        parameter.value.toString()
            .let(::JsonPrimitive)
            .let(JsonPrimitive::toString)
            .let { it.substring(1, it.length - 1) }
            .let { this.append(it) }
    }
}