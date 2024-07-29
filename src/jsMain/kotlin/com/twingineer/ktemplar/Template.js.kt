package com.twingineer.ktemplar

import com.twingineer.ktemplar.StandardTemplateType.*

public actual inline fun StandardTemplateType.build(out: Appendable, block: TemplateScope.() -> Unit) {
    val scope = when (this) {
        CHECKED -> CheckedTemplateScope(out)
        UNCHECKED, HTML -> UncheckedTemplateScope(out)
    }
    scope.block()
}