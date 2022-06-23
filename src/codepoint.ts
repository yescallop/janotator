function forEachCodePoint(s: string, f: (index: number, len: number, c: number) => void) {
    let i = 0;
    while (i < s.length) {
        let c = s.codePointAt(i)!;
        let len = c >= 0x10000 ? 2 : 1;
        f(i, len, c);
        i += len;
    }
}

function isCodePointIdeographic(c: number): boolean {
    return (c >= 0x3400 && c <= 0x4DBF) || (c >= 0x4E00 && c <= 0x9FFF) || (c >= 0xF900 && c <= 0xFAFF) ||
        (c >= 0x20000 && c <= 0x2A6DF) || (c >= 0x2A700 && c <= 0x2B73F) || (c >= 0x2B740 && c <= 0x2B81F) ||
        (c >= 0x2B820 && c <= 0x2CEAF) || (c >= 0x2CEB0 && c <= 0x2EBEF) || (c >= 0x2F800 && c <= 0x2FA1F) || (c >= 0x30000 && c <= 0x3134F) ||
        c == 0x3005 /* 々 */ || c == 0x3007 /* 〇 */ || c == 0x30FB /* ・ */;
}

function isCodePointProhibited(c: number): boolean {
    return c <= 0x20 || (c >= 0x41 /* A */ && c <= 0x5A /* Z */) ||
        (c >= 0x61 /* a */ && c <= 0x7A /* z */) ||
        (c >= 0xFF21 /* Ａ */ && c <= 0xFF3A /* Ｚ */) ||
        (c >= 0xFF41 /* ａ */ && c <= 0xFF5A /* ｚ */) ||
        /* U+3000: Ideographic Space "　".
           U+0304: Combining Macron "◌̄". */
        "\u3000/\\／＼ĀĪŪĒŌāīūēō\u0304".includes(String.fromCodePoint(c));
}
