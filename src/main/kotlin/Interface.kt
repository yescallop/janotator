import org.w3c.dom.Element
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.browser.window

private var noted = false
private var req: XMLHttpRequest? = null

@JsName("buttonClicked")
fun buttonClicked(button: Element) {
    val text = document.getElementById("text")!!
    val result = document.getElementById("result")!!
    if (!noted) {
        noted = true
        button.innerHTML = "Abort"
        req = note(text.asDynamic().value as String) {
            if (it != null) {
                text.asDynamic().style.display = "none"
                button.innerHTML = "Clear"
                it.forEach { line ->
                    result.innerHTML += lineToHtml(line)
                }
            } else {
                noted = false
                button.innerHTML = "Note"
            }
        }
    } else {
        noted = false
        req?.abort()
        button.innerHTML = "Note"
        result.innerHTML = ""
        text.asDynamic().style.display = "inline"
    }
}

fun <T> fail(msg: String, onReadyAction: (T?) -> Unit) {
    onReadyAction(null)
    window.alert("Failed to note: $msg")
}

private fun lineToHtml(line: NotedLine): String {
    val kanaNoted = line.kanaNoted ?: "<span style=\"color: red\">${line.origin}</span>"
    return "<p>$kanaNoted<br/>${line.romaji}</p>"
}