package com.twingineer.ktemplar

// should yield equivalently as AppenderBase#interpolate
internal fun CharSequence.trimMarginOrIndent(marginPrefix: String = "|"): String {
    require(marginPrefix.isNotBlank()) { "marginPrefix must be non-blank string." }
    val marginReplace by lazy { " ".repeat(marginPrefix.length) }

    val lines = lines()
    val minCommonIndent by lazy {
        lines
            .filter(String::isNotBlank)
            .minOfOrNull(String::indentWidth)
            ?: 0
    }

    return lines.reindent(length + marginPrefix.length * lines.size) { line ->
        val firstNonWhitespaceIndex = line.indexOfFirst { !it.isWhitespace() }

        when {
            firstNonWhitespaceIndex == -1 -> ""
            line.startsWith(
                marginPrefix,
                firstNonWhitespaceIndex
            ) -> marginReplace + line.substring(firstNonWhitespaceIndex + marginPrefix.length)
            else -> line.substring(minCommonIndent)
        }
    }
}

private inline fun List<String>.reindent(
    resultSizeEstimate: Int,
    indentCutFunction: (String) -> String?
): String {
    val lastIndex = lastIndex
    return mapIndexedNotNull { index, value ->
        if ((index == 0 || index == lastIndex) && value.isBlank())
            null
        else
            indentCutFunction(value) ?: value
    }
        .joinTo(StringBuilder(resultSizeEstimate), "\n")
        .toString()
}

internal fun CharSequence.indentWidth(): Int = indexOfFirst { !it.isWhitespace() }.let { if (it == -1) length else it }