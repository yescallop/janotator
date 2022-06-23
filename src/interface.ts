let notated = false;
let req: XMLHttpRequest | null = null;

function buttonClicked(button: HTMLElement) {
    let text = document.getElementById("text")! as HTMLInputElement;
    let result = document.getElementById("result")!;
    if (!notated) {
        notated = true;
        button.innerHTML = "Abort";
        req = notate(text.value, (lines) => {
            if (lines != null) {
                text.style.display = "none";
                button.innerHTML = "Clear";
                lines.forEach((line) => {
                    result.innerHTML += lineToHtml(line);
                });
            } else {
                notated = false;
                button.innerHTML = "Notate";
            }
        });
    } else {
        notated = false;
        req?.abort();
        button.innerHTML = "Notate";
        result.innerHTML = "";
        text.style.display = "inline";
    }
}

function fail<T>(msg: string, onReady: (res: T | null) => void) {
    onReady(null);
    window.alert(`Failed to notate: ${msg}`);
}

function lineToHtml(line: NotatedLine): string {
    let kanaNotated = line.kanaNotated != null ? line.kanaNotated : `<span style=\"color: red\">${line.origin}</span>`;
    return `<p>${kanaNotated}<br/>${line.romaji}</p>`;
}