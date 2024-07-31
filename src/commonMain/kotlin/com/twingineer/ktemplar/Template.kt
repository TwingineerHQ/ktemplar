package com.twingineer.ktemplar

import io.exoquery.terpal.Interpolator
import io.exoquery.terpal.Messages
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmInline

private val logger = KotlinLogging.logger {}

public inline fun Appendable.appendTemplate(block: TemplateScope.() -> Unit): Unit =
    CheckedTemplateScope(this).block()

public interface Appender {
    public operator fun invoke(string: String)
}

public abstract class InterpolatingAppender(
    private val out: Appendable,
    private val marginPrefix: String = "|",
) : Interpolator<Any?, Unit>, Appender {

    init {
        require(marginPrefix.isNotBlank()) { "marginPrefix must be non-blank string." }
    }

    private val marginReplace = " ".repeat(marginPrefix.length)

    protected abstract fun Appendable.appendParameter(parameter: TemplateParameter<*>)

    override fun interpolate(parts: () -> List<String>, params: () -> List<Any?>) {
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
                            if (param.isRaw) param.value.toString().let(::append)
                            else appendParameter(param)

                        else -> appendParameter(CheckedTemplateParameter<Any?>(param))
                    }
                } else check(onLastPart)
            }
        }
    }

    override operator fun invoke(string: String): Unit = Messages.throwPluginNotExecuted()
}

public sealed interface TemplateScope {

    public fun <V> V.param(): TemplateParameter<V>

    public fun <V> V.raw(): TemplateParameter<V>

    public operator fun <V> V.not(): TemplateParameter<V> =
        this.raw()

    public operator fun <V> V.unaryMinus(): TemplateParameter<V> =
        this.param()

    public val empty: TemplateParameter<Unit>
}

public abstract class TemplateScopeBase protected constructor(internal val out: Appendable) : TemplateScope {

    internal abstract fun copy(out: Appendable): TemplateScopeBase
}

public class CheckedTemplateScope(out: Appendable) : TemplateScopeBase(out) {

    override fun <V> V.param(): TemplateParameter<V> =
        CheckedTemplateParameter(this)

    override fun <V> V.raw(): TemplateParameter<V> =
        CheckedTemplateParameter(TemplateRaw(this))

    override val empty: TemplateParameter<Unit>
        get() = Companion.empty

    override fun copy(out: Appendable): TemplateScopeBase =
        CheckedTemplateScope(out)

    private companion object {
        private val empty: CheckedTemplateParameter<Unit> =
            CheckedTemplateParameter(TemplateRaw(""))
    }
}

public fun (TemplateScope.() -> Unit).indent(size: Int): (TemplateScope.() -> Unit) = {
    (this as TemplateScopeBase).copy(IndentingAppendable(out, size)).this@indent()
}

public interface TemplateParameter<out V> {
    public val value: V
    public val isRaw: Boolean
}

@JvmInline
private value class CheckedTemplateParameter<out V>(
    private val inlineValue: Any?,
) : TemplateParameter<V> {
    override val value: V
        @Suppress("UNCHECKED_CAST")
        get() = (if (isRaw) (inlineValue as TemplateRaw<*>).value else inlineValue) as V

    override val isRaw: Boolean
        get() = inlineValue is TemplateRaw<*>

    override fun toString(): String =
        throw UnsupportedOperationException()
}

private data class TemplateRaw<out V>(
    val value: V,
)

@Suppress("unused")
internal open class UnsafeAppender(private val out: Appendable) : Appender {
    override operator fun invoke(string: String) {
        out.append(string.trimMarginOrIndent())
    }
}

private class IndentingAppendable(private val out: Appendable, size: Int) : Appendable {
    private val find = '\n'
    private val findRegex = Regex("$find")
    private val indentation = " ".repeat(size)
    private val replace = find + indentation

    private var started: Boolean = false

    override fun append(value: CharSequence?): Appendable {
        checkStarted()
        value?.replace(findRegex, replace).let(out::append)
        return this
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
        checkStarted()
        return value?.subSequence(startIndex, endIndex).let(this::append)
    }

    override fun append(value: Char): Appendable {
        checkStarted()
        if (value == find) out.append(replace)
        else out.append(value)

        return this
    }

    private fun checkStarted() {
        if (started)
            return
        out.append(indentation)
        started = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IndentingAppendable) return false

        if (out != other.out) return false
        if (indentation != other.indentation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = out.hashCode()
        result = 31 * result + indentation.hashCode()
        return result
    }
}

public fun TemplateScope.include(block: TemplateScope.() -> Unit): TemplateParameter<*> =
    TemplateInclusion(this, block).param()

private data class TemplateInclusion(private val builder: TemplateScope, val block: TemplateScope.() -> Unit) {
    override fun toString(): String {
        builder.block()
        return ""
    }
}