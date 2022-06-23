const API_URI: string = "https://translate.googleapis.com/translate_a/single?client=t&sl=ja&dt=rm&tk=";

function transliterate(s: string, onReady: (res: string | null) => void): XMLHttpRequest {
    let tk = ticket(s);
    let req = new XMLHttpRequest();
    req.open("POST", API_URI + tk);
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    req.onreadystatechange = () => {
        if (req.readyState == XMLHttpRequest.DONE) {
            if (req.status == 200) {
                let json = JSON.parse(req.responseText);
                let elem0 = json[0];
                if (elem0 != null) {
                    onReady(elem0[0][3]);
                } else {
                    fail("Not Japanese", onReady);
                }
            } else if (req.status != 0) {
                fail(`Server returned status code ${req.status}`, onReady);
            }
        } else if (req.readyState == XMLHttpRequest.UNSENT) {
            fail("Request unsent", onReady);
        }
    };
    req.send("q=" + encodeURIComponent(s));
    return req;
}