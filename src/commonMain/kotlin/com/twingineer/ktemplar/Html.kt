package com.twingineer.ktemplar

internal expect fun htmlAppenderOf(out: Appendable): HtmlAppender

public expect class HtmlAppender: Appender<Any, Unit> {
    override operator fun invoke(string: String)
}

public val TemplateScope.html: HtmlAppender
    get() = (this as TemplateScopeBase).appenders.getOrPut(HtmlAppender::class) { htmlAppenderOf(out) } as HtmlAppender