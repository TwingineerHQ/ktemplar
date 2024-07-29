package com.twingineer.ktemplar

import kotlinx.html.stream.appendHTML
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

public inline fun template(block: TemplateScope.() -> Unit): String =
    StandardTemplateType.CHECKED.build(block)

public inline fun template(out: Appendable, block: TemplateScope.() -> Unit): Unit =
    StandardTemplateType.CHECKED.build(out, block)

public inline fun StandardTemplateType.build(block: TemplateScope.() -> Unit): String =
    buildString { build(this, block) }

public expect inline fun StandardTemplateType.build(out: Appendable, block: TemplateScope.() -> Unit)

public expect interface Appender<T, R> {
    public open operator fun invoke(string: String): R
}

public enum class StandardTemplateType {
    HTML,
    CHECKED,
    UNCHECKED,
}

public sealed interface TemplateScope {

    public fun <V> V.param(): TemplateParameter<V>

    public fun <V> V.raw(): TemplateParameter<V>

    // aligns params (more common than raws) with an operator for booleans (less common than numbers)
    public operator fun <V> V.not(): TemplateParameter<V> =
        this.param()

    public operator fun <V> V.unaryMinus(): TemplateParameter<V> =
        this.raw()

    public val empty: TemplateParameter<Unit>
}

public abstract class TemplateScopeBase protected constructor(internal val out: Appendable) : TemplateScope {
    internal val appenders: MutableMap<KClass<*>, Appender<*, *>> = mutableMapOf()

    internal abstract fun copy(out: Appendable): TemplateScopeBase
}

public class CheckedTemplateScope(out: Appendable) : TemplateScopeBase(out) {

    override fun <V> V.param(): TemplateParameter<V> =
        paramOf(this)

    override fun <V> V.raw(): TemplateParameter<V> =
        rawOf(this)

    override val empty: TemplateParameter<Unit>
        get() = com.twingineer.ktemplar.empty

    override fun copy(out: Appendable): TemplateScopeBase =
        CheckedTemplateScope(out)
}

public class UncheckedTemplateScope(out: Appendable) : TemplateScopeBase(out) {

    override fun <V> V.param(): TemplateParameter<V> =
        uncheckedParamOf(this) {
            it.toString().let(appendHTML()::onTagContent)
        }

    override fun <V> V.raw(): TemplateParameter<V> =
        uncheckedRawOf(this)

    override val empty: TemplateParameter<Unit>
        get() = uncheckedEmpty

    override fun copy(out: Appendable): TemplateScopeBase =
        UncheckedTemplateScope(out)
}

private fun <V> uncheckedParamOf(value: V, block: Appendable.(V) -> Unit): TemplateParameter<V> =
    UncheckedTemplateParameter(value, block)

private fun <V> uncheckedRawOf(value: V): TemplateParameter<V> =
    UncheckedTemplateParameter(TemplateRaw(value), null)

private val uncheckedEmpty: TemplateParameter<Unit> =
    UncheckedTemplateParameter(TemplateRaw(""), null)

private fun <V> paramOf(value: V): TemplateParameter<V> =
    SafeTemplateParameter(value)

private fun <V> rawOf(value: V): TemplateParameter<V> =
    SafeTemplateParameter(TemplateRaw(value))

private val empty: TemplateParameter<Unit> =
    SafeTemplateParameter(TemplateRaw(""))

public fun (TemplateScope.() -> Unit).indent(size: Int): (TemplateScope.() -> Unit) = {
    (this as TemplateScopeBase).copy(IndentingAppendable(out, size)).this@indent()
}

public interface TemplateParameter<out V> {
    public val value: V
    public val isRaw: Boolean
}

@Suppress("UNCHECKED_CAST")
private fun <V> TemplateParameter<V>.valueOf(value: Any?): V =
    (if (isRaw) (value as TemplateRaw<*>).value else value) as V

@JvmInline
private value class SafeTemplateParameter<out V>(
    private val inlineValue: Any?,
) : TemplateParameter<V> {
    override val value: V
        get() = this.valueOf(inlineValue)

    override val isRaw: Boolean
        get() = inlineValue is TemplateRaw<*>

    override fun toString(): String =
        throw UnsupportedOperationException()
}

private class UncheckedTemplateParameter<out V>(
    private val inlineValue: Any?,
    private val block: (Appendable.(V) -> Unit)?
) : TemplateParameter<V> {
    override val value: V
        get() = this.valueOf(inlineValue)

    override val isRaw: Boolean
        get() = inlineValue is TemplateRaw<*>

    override fun toString(): String =
        if (isRaw) value.toString()
        else buildString {
            block!!(value)
        }
}

private data class TemplateRaw<out V>(
    val value: V,
)

@Suppress("unused")
public interface IAppender<T, R> {
    public operator fun invoke(string: String): R =
        throw UnsupportedOperationException()
}

public open class UnsafeAppender(private val out: Appendable) : IAppender<Any, Unit> {
    override operator fun invoke(string: String) {
        out.append(string.trimMarginOrIndent())
    }
}

internal class IndentingAppendable(private val out: Appendable, size: Int) : Appendable {
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
    -TemplateInclusion(this, block)

internal data class TemplateInclusion(private val builder: TemplateScope, val block: TemplateScope.() -> Unit) {
    override fun toString(): String {
        builder.block()
        return ""
    }
}