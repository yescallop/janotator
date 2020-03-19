import org.w3c.xhr.XMLHttpRequest

private const val API_URI =
    "https://translate.googleapis.com/translate_a/single?client=t&sl=ja&dt=rm&tk="

fun transliterate(s: String, onReadyAction: (String?) -> Unit): XMLHttpRequest {
    val tk = ticket(s)
    val req = XMLHttpRequest()
    req.open("POST", API_URI + tk)
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded")
    req.onreadystatechange = {
        if (req.readyState == XMLHttpRequest.DONE) {
            if (req.status == 200.toShort()) {
                val json: dynamic = JSON.parse(req.responseText)
                val elem0 = json[0]
                if (elem0 != null) {
                    onReadyAction(elem0[0][3] as String)
                } else {
                    fail("Not Japanese", onReadyAction)
                }
            } else if (req.status != 0.toShort()) {
                fail("Server returned status code ${req.status}", onReadyAction)
            }
        } else if (req.readyState == XMLHttpRequest.UNSENT) {
            fail("Request unsent", onReadyAction)
        }
    }
    req.send("q=" + js("encodeURIComponent")(s) as String)
    return req
}

private external fun ticket(s: String): String