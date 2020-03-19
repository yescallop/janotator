import org.w3c.xhr.XMLHttpRequest

private val REGEX_SYLLABLE =
    Regex("n(?![aiueoāīūēō])|[wrtypsdfghjkzcvbnm~]*[aiueoāīūēō]|\\b \\b| ?\\u0304")
private const val HIRAGANA_START = 0x3041
private const val HIRAGANA_END = 0x3094
private const val KATAKANA_START = 0x30A1
private const val KATAKANA_END = 0x30F4
private const val HIRA_KATA_OFFSET = KATAKANA_START - HIRAGANA_START
private val ROMAJI = arrayOf(
    "~a", "a", "~i", "i", "~u", "u", "~e", "e", "~o", "o",
    "ka", "ga", "ki", "gi", "ku", "gu", "ke", "ge", "ko", "go",
    "sa", "za", "shi", "ji", "su", "zu", "se", "ze", "so", "zo",
    "ta", "da", "chi", "dji", "~tsu", "tsu", "dzu", "te", "de", "to", "do",
    "na", "ni", "nu", "ne", "no",
    "ha", "ba", "pa", "hi", "bi", "pi", "fu", "bu", "pu", "he", "be", "pe", "ho", "bo", "po",
    "ma", "mi", "mu", "me", "mo",
    "~ya", "ya", "~yu", "yu", "~yo", "yo",
    "ra", "ri", "ru", "re", "ro",
    "~wa", "wa", "wi", "we", "wo",
    "n", "vu"
)
private const val VA_LINE_START = 0x30F7
private const val VA_LINE_END = 0x30FA
private val VA_LINE = arrayOf("va", "vi", "ve", "vo")
private val ROMAJI_TO_KANA =
    HashMap<String, Char>(ROMAJI.size + VA_LINE.size).apply {
        for (i in ROMAJI.indices) {
            put(ROMAJI[i], (HIRAGANA_START + i).toChar())
        }
        for (i in VA_LINE.indices) {
            put(VA_LINE[i], (VA_LINE_START + i).toChar())
        }
    }

private fun noteKana(line: String, kana: String, regex: String, indexes: List<Int>): String? {
    val size = indexes.size
    if (size == 0) return line

    val values = Regex(regex).matchEntire(kana)?.groupValues ?: return null
    val sb = StringBuilder()
    var last = 0
    for (i in 0 until size) {
        val cur = indexes[i]
        var res = values[i + 1].trim()
        sb.append(line, last, cur)
        if (res.isNotEmpty()) {
            if (res.indexOf('/') >= 0) res = "<span style=\"color: red\">ERROR<span>"
            sb.append("(<b>").append(res).append("</b>)")
        }
        last = cur
    }
    sb.append(line, last, line.length)
    return sb.toString()
}

private fun romajiToKana(romaji: String): String {
    val sb = StringBuilder()
    var lastEnd = 0
    REGEX_SYLLABLE.findAll(romaji).forEach {
        val diff = it.range.first - lastEnd
        val c = romaji[lastEnd]
        if (diff == 1 && c == ' ') {
            sb.append(' ')
        } else if (diff != 0 &&
            !(lastEnd != 0 && diff == 1 && (c == '\'' || c == '-'))
        ) {
            sb.append('/')
        }
        lastEnd = it.range.last + 1
        var syllable = it.groupValues[0]
        var len = syllable.length
        if (syllable == " ") {
            sb.append(' ')
            return@forEach
        } else if (syllable[len - 1] == '\u0304') {
            sb.append('ー')
            return@forEach
        }
        if (len > 2) {
            val c0 = syllable[0]
            val c1 = syllable[1]
            if (c0 == c1 || c0 == 't' && c1 == 'c') {
                syllable = syllable.substring(1)
                len--
                sb.append('っ')
            }
        }
        val last = syllable[len - 1]
        val i = "āīūēō".indexOf(last)
        if (i >= 0) {
            syllable = syllable.substring(0, len - 1) + "aiueo"[i]
            romajiSyllableToKana(syllable, sb)
            sb.append("あいうえう"[i])
        } else {
            romajiSyllableToKana(syllable, sb)
        }
    }
    if (lastEnd != romaji.length) sb.append('/')
    return sb.toString()
}

private val YOUON_1 = Regex("[kgnhbpmr]y[aueo]")
private val YOUON_2 = Regex("(?:sh|j|ch|dj)[aueo]")
private val YOUON_3 = Regex("f[aieo]")
private fun romajiSyllableToKana(s: String, sb: StringBuilder) {
    val kana = ROMAJI_TO_KANA[s]
    if (kana == null) when {
        YOUON_1.matches(s) -> {
            sb.append(ROMAJI_TO_KANA[s[0] + "i"])
            val c = s[2]
            sb.append(
                if (c == 'e') 'ぇ' else ROMAJI_TO_KANA["~y$c"]
            )
        }
        YOUON_2.matches(s) -> {
            sb.append(ROMAJI_TO_KANA[s.substring(0, s.length - 1) + 'i'])
            val c = s[s.length - 1]
            sb.append(
                if (c == 'e') 'ぇ' else ROMAJI_TO_KANA["~y$c"]
            )
        }
        YOUON_3.matches(s) -> {
            sb.append('ふ')
            sb.append(ROMAJI_TO_KANA["~" + s[1]])
        }
        else -> throw IllegalArgumentException("No such kana: $s")
    } else {
        sb.append(kana)
    }
}

private fun jaToRegex(s: String, indexes: MutableList<Int>): String {
    var res = ""
    val len = s.length
    var last = 0
    var flag = -1
    s.forEachCodePoint { i, clen, c ->
        val vaLine = isInVaLine(c)
        val kata = vaLine || isKatakana(c)
        val hira = isHiragana(c)
        if (!kata && !hira && c != 'ー'.toInt()) {
            if (!isCodePointIdeographic(c)) {
                if (flag == 0) indexes.add(i)
                if (flag != 1) {
                    res += "/ ?"
                    flag = 1
                }
            } else if (flag != 0) {
                res += "( .+?|.+? |.+?)"
                flag = 0
            }
        } else {
            if (flag == 0) indexes.add(i)
            val ch = c.toChar()
            // Without toString(), Char would be unexpectedly casted to Int
            res += when {
                kata -> when (ch) {
                    'オ' -> "[おう]"
                    'ァ', 'ィ', 'ゥ', 'ェ', 'ォ' -> {
                        val ck = ch - HIRA_KATA_OFFSET
                        "[$ck${ck + 1}]"
                    }
                    else -> (if (vaLine) ch else ch - HIRA_KATA_OFFSET).toString()
                }
                ch == 'ー' -> when {
                    isKatakana(last) -> {
                        val r = ROMAJI[last - KATAKANA_START]
                        "あいうえう"["aiueo".indexOf(r.last())].toString()
                    }
                    isInVaLine(last) -> "あいえう"[last - VA_LINE_START].toString()
                    else -> "ー"
                }
                else -> when (ch) {
                    'お' -> "[おう]"
                    'は' -> "[はわ]"
                    'へ' -> "[へえ]"
                    'を' -> "[をお]"
                    'ぁ', 'ぃ', 'ぅ', 'ぇ', 'ぉ' -> "[$ch${ch + 1}]"
                    else -> s.substring(i, i + clen)
                }
            }

            last = c
            flag = -1
            res += " ?"
        }
    }
    if (flag == 0) indexes.add(len)
    return res
}

private fun isHiragana(c: Int): Boolean {
    return c in HIRAGANA_START..HIRAGANA_END
}

private fun isKatakana(c: Int): Boolean {
    return c in KATAKANA_START..KATAKANA_END
}

private fun isInVaLine(c: Int): Boolean {
    return c in VA_LINE_START..VA_LINE_END
}

private fun merge(s: String, lines: MutableList<String>): String {
    var res = ""
    var last = 0
    for (i in 0..s.length) {
        if (i == s.length || s[i] == '\n') {
            if (last == i) {
                last = i + 1
                continue
            }
            val line = s.substring(last, i).trim()
            if (line.isEmpty()) {
                last = i + 1
                continue
            }
            lines.add(line)
            res += mergeLine(line)
            if (i != s.length) res += '\\'
            last = i + 1
        }
    }
    return res
}

private fun mergeLine(s: String): String {
    var res: String? = null
    var lastPermitted = true
    s.forEachCodePoint { i, len, c ->
        if (!isCodePointProhibited(c)) {
            if (res != null) {
                if (!lastPermitted) res += '/'
                res += s.substring(i, i + len)
                lastPermitted = true
            }
        } else {
            if (res == null) res = s.substring(0, i)
            lastPermitted = false
        }
    }
    if (!lastPermitted) res += '/'
    return res ?: s
}

fun note(s: String, onReadyAction: (List<NotedLine>?) -> Unit): XMLHttpRequest? {
    val lines = ArrayList<String>()
    val merged = merge(s, lines)
    if (merged.isEmpty()) {
        fail("Empty text", onReadyAction)
        return null
    }
    if (merged.length > 5000) {
        fail("Merged text length > 5000", onReadyAction)
        return null
    }
    return transliterate(merged) {
        if (it == null) {
            onReadyAction(null)
            return@transliterate
        }
        val resLines = it.split('\\')
        val res = ArrayList<NotedLine>()
        for (i in lines.indices) {
            val line = lines[i]
            val romaji = resLines[i].trim(' ', '-').toLowerCase()
            val kana = romajiToKana(romaji)
            val indexes = ArrayList<Int>()
            val regex = jaToRegex(line, indexes)
            val kanaNoted = noteKana(line, kana, regex, indexes)
            res.add(NotedLine(line, romaji, kanaNoted))
        }
        onReadyAction(res)
    }
}

class NotedLine(
    var origin: String,
    var romaji: String,
    var kanaNoted: String?
)