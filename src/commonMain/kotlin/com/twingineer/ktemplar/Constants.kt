package com.twingineer.ktemplar

public val TemplateScope.empty: TemplateParameter<Nothing>
    get() = EMPTY

public val TemplateScope.ln: TemplateParameter<String>
    get() = LN

private val EMPTY = CheckedTemplateParameter<Nothing>(TemplateRaw(""))
private val LN = CheckedTemplateParameter<String>(TemplateRaw("\n"))