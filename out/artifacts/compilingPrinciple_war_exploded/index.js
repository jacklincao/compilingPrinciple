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