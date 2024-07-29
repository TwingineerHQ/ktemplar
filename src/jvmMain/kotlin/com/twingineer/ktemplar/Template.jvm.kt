package com.twingineer.ktemplar

import com.twingineer.ktemplar.StandardTemplateType.*
import io.exoquery.terpal.Interpolator
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

public actual inline fun StandardTemplateType.build(out: Appendable, block: TemplateScope.() -> Unit) {
    val scope = when (this) {
        CHECKED, HTML -> CheckedTemplateScope(out)
        UNCHECKED -> UncheckedTemplateScope(out)
    }
    scope.block()
}

public actual typealias Appender<T, R> = Interpolator<T, R>

public abstract class AppenderBase<T>(
    private val out: Appendable,
    private val marginPrefix: String = "|",
) : Appender<T, Unit> {

    init {
        require(marginPrefix.isNotBlank()) { "marginPrefix must be non-blank string." }
    }

    private val marginReplace = " ".repeat(marginPrefix.length)

    protected abstract fun Appendable.appendParameter(parameter: TemplateParameter<*>)

    public override fun interpolate(parts: () -> List<String>, params: () -> List<T>) {
        with(out) {
            var firstLineSkipped = false
            var indentTrim: Int? = null
            fun appendTrimmed(value: CharSequence, skipLast: Boolean = false) {
                val linesIter = value.lineSequence().iterator()
                var line = linesIter.next()
                var indentWidth = line.indentWidth()
                var doTrim = false
                var doBreak = false

                if (indentTrim == null) {
                    if (!firstLineSkipped && indentWidth == line.length) {
                        firstLineSkipped = true
                        if (!linesIter.hasNext())
                            return

                        line = linesIter.next()
                        indentWidth = line.indentWidth()
                    }
                    indentTrim = indentWidth
                    doTrim = true
                }

                do {
                    val hasNext = linesIter.hasNext()
                    if (doTrim) {
                        val startIndex = if (indentWidth < indentTrim!!) {
                            if (!skipLast || hasNext)
                                logger.debug { "Tried to trim indentation of width $indentTrim but only $indentWidth present." }
                            indentWidth
                        } else indentTrim!!

                        val prefixed = startIndex < line.length &&
                                line.subSequence(startIndex, startIndex + marginPrefix.length) == marginPrefix

                        if (prefixed) {
                            if (doBreak)
                                out.append('\n')
                            out.append(marginReplace)
                            out.append(line.subSequence(startIndex + marginPrefix.length, line.length))
                        } else if (skipLast && !hasNext && line.subSequence(startIndex, line.length).isBlank()) {
                            // no-op
                        } else {
                            if (doBreak)
                                out.append('\n')
                            out.append(line.subSequence(startIndex, line.length))
                        }
                    } else {
                        out.append(line)
                    }

                    if (!hasNext)
                        break
                    line = linesIter.next()
                    indentWidth = line.indentWidth()
                    doBreak = true
                    doTrim = true
                } while (true)
            }

            val partsIter = parts().iterator()
            val paramsIter = params().iterator()
            while (partsIter.hasNext()) {
                val part = partsIter.next()
                val onLastPart = !partsIter.hasNext()
                appendTrimmed(part, skipLast = onLastPart)
                if (paramsIter.hasNext()) {
                    when (val param = paramsIter.next()) {
                        is TemplateParameter<*> ->
                            if (param.isRaw) param.value.toString()
                            else appendParameter(param)

                        else -> throw IllegalArgumentException("Non-parametrized string after '${part.takeLast(100)}'")
                    }
                } else assert(onLastPart)
            }
        }
    }
}