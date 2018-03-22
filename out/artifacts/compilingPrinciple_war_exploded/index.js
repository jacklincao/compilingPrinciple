function openFile() {
    var dom = document.getElementById('openFile');
    dom.click()
    console.log(dom.value)
}

function getFile(files) {
    console.log(files[0])
    var reader = new FileReader();
    reader.onload = function (ev) {
        document.getElementById("display").value = this.result;
    }
    reader.readAsText(files[0]);
}

function uploadFile() {
    var form = new FormData();
    form.append("file", document.getElementById("display").value);

    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if(xhr.readyState == 4) {
            if((xhr.status >= 200 && xhr.status < 300) || xhr.status == 304){
                console.log(xhr.responseText);
            }
            else {
                alert("请求失败:" + xhr.status)
            }
        }
    }
    xhr.open("POST", "/CompilingServlet", true);
    xhr.send(form);
}

function saveAnother() {
    exportRaw('test.txt', JSON.stringify(document.querySelector('#display').value));
}

function fakeClick(obj) {
    var ev = document.createEvent("MouseEvents");
    ev.initMouseEvent("click", true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
    obj.dispatchEvent(ev);
}

function exportRaw(name, data) {
    var urlObject = window.URL || window.webkitURL || window;
    var export_blob = new Blob([data], {type: 'text/plain'});
    var save_link = document.createElementNS("http://www.w3.org/1999/xhtml", "a")
    save_link.href = urlObject.createObjectURL(export_blob);
    save_link.download = name;
    fakeClick(save_link);
}