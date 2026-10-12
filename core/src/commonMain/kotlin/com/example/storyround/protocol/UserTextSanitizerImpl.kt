package com.example.storyround.protocol

private const val ZERO_WIDTH_JOINER = '‍'

class UserTextSanitizerImpl : UserTextSanitizer {
    override fun sanitize(raw: String): String =
        buildString(raw.length) {
            var pendingSpace = false
            var index = 0
            while (index < raw.length) {
                val char = raw[index]
                when {
                    char.isWhitespace() -> pendingSpace = isNotEmpty()
                    char.isHighSurrogate() && raw.getOrNull(index + 1)?.isLowSurrogate() == true -> {
                        appendPending(pendingSpace).also { pendingSpace = false }
                        append(char).append(raw[index + 1])
                        index++
                    }
                    char.isKept() -> {
                        appendPending(pendingSpace).also { pendingSpace = false }
                        append(char)
                    }
                }
                index++
            }
        }
}

private fun StringBuilder.appendPending(pendingSpace: Boolean) {
    if (pendingSpace) append(' ')
}

private fun Char.isKept(): Boolean =
    when (category) {
        CharCategory.CONTROL, CharCategory.SURROGATE, CharCategory.UNASSIGNED, CharCategory.PRIVATE_USE -> false
        CharCategory.FORMAT -> this == ZERO_WIDTH_JOINER
        else -> true
    }
