package com.twingineer.ktemplar

@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
public actual inline fun StandardTemplateType.build(out: Appendable, block: TemplateScope.() -> Unit): Unit =
    buildInternal(out, block)