let notated = false;
let controller: AbortController;

function buttonClicked(button: HTMLElement) {
    let text = document.getElementById("text")! as HTMLInputElement;
    let result = document.getElementById("result")!;
    if (!notated) {
        notated = true;
        button.innerHTML = "Abort";
        controller = new AbortController();
        notate(text.value, controller.signal).then(lines => {
            if (lines != null) {
                text.style.display = "none";
                button.innerHTML = "Clear";
                lines.forEach(line => {
                    result.innerHTML += lineToHtml(line);
                });
            } else {
                notated = false;
                button.innerHTML = "Notate";
            }
        }).catch(err => {
            if (!(err instanceof DOMException && err.code == DOMException.ABORT_ERR)) {
                window.alert(`Failed to notate: ${err}`);
            }
            notated = false;
            button.innerHTML = "Notate";
        });
    } else {
        notated = false;
        controller.abort();
        button.innerHTML = "Notate";
        result.innerHTML = "";
        text.style.display = "inline";
    }
}

function lineToHtml(line: NotatedLine): string {
    let kanaNotated = line.kanaNotated != null ? line.kanaNotated : `<span style=\"color: red\">${line.origin}</span>`;
    return `<p>${kanaNotated}<br/>${line.romaji}</p>`;
}