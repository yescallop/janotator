fun String.forEachCodePoint(action: (index: Int, len: Int, Int) -> Unit) {
    var i = 0
    while (i < length) {
        val c = get(i)
        if (c.isHighSurrogate() && i != length - 1) {
            val lc = get(i + 1)
            if (lc.isLowSurrogate()) {
                action(
                    i, 2, ((c.toInt() - 0xD800) shl 10) +
                            lc.toInt() + 0x2400
                )
                i += 2
                continue
            }
        }
        action(i, 1, c.toInt())
        i++
    }
}

fun isCodePointIdeographic(c: Int): Boolean {
    return c in 0x3400..0x4DBF || c in 0x4E00..0x9FFF || c in 0xF900..0xFAFF ||
            c in 0x20000..0x2A6DF || c in 0x2A700..0x2B73F || c in 0x2B740..0x2B81F ||
            c in 0x2B820..0x2CEAF || c in 0x2CEB0..0x2EBEF || c in 0x2F800..0x2FA1F || c in 0x30000..0x3134F ||
            c == '々'.toInt() || c == '〇'.toInt() || c == '・'.toInt()
}

fun isCodePointProhibited(c: Int): Boolean {
    val ch = c.toChar()
    return c <= 0x20 || ch in 'A'..'Z' || ch in 'a'..'z' ||
            ch in 'Ａ'..'Ｚ' || ch in 'ａ'..'ｚ' ||
            "　/\\／＼ĀĪŪĒŌāīūēō\u0304".contains(ch)
}