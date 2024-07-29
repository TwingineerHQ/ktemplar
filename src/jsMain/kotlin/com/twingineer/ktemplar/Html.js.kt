package com.twingineer.ktemplar

public actual typealias Appender<T, R> = IAppender<T, R>

public actual class HtmlAppender(out: Appendable) : UnsafeAppender(out)

internal actual fun htmlAppenderOf(out: Appendable): HtmlAppender =
    HtmlAppender(out)