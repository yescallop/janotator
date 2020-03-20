import org.w3c.dom.Element
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.browser.window

private var notated = false
private var req: XMLHttpRequest? = null

@JsName("buttonClicked")
fun buttonClicked(button: Element) {
    val text = document.getElementById("text")!!
    val result = document.getElementById("result")!!
    if (!notated) {
        notated = true
        button.innerHTML = "Abort"
        req = notate(text.asDynamic().value as String) {
            if (it != null) {
                text.asDynamic().style.display = "none"
                button.innerHTML = "Clear"
                it.forEach { line ->
                    result.innerHTML += lineToHtml(line)
                }
            } else {
                notated = false
                button.innerHTML = "Notate"
            }
        }
    } else {
        notated = false
        req?.abort()
        button.innerHTML = "Notate"
        result.innerHTML = ""
        text.asDynamic().style.display = "inline"
    }
}

fun <T> fail(msg: String, onReadyAction: (T?) -> Unit) {
    onReadyAction(null)
    window.alert("Failed to notate: $msg")
}

private fun lineToHtml(line: NotatedLine): String {
    val kanaNotated = line.kanaNotated ?: "<span style=\"color: red\">${line.origin}</span>"
    return "<p>$kanaNotated<br/>${line.romaji}</p>"
}