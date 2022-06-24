const API_URI: string = "https://translate.googleapis.com/translate_a/single?client=t&sl=ja&dt=rm&tk=";

async function transliterate(text: string, signal: AbortSignal): Promise<string> {
    return fetch(API_URI + ticket(text), {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "q=" + encodeURIComponent(text),
        signal
    }).then(resp => {
        if (resp.status != 200) {
            throw `Server returned status code ${resp.status}`;
        }
        return resp.json();
    }).then(json => {
        let elem0 = json[0];
        if (elem0 != null) {
            return elem0[0][3];
        } else {
            throw "Not Japanese";
        }
    });
}