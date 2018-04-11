function openFile() {
    var dom = document.getElementById('openFile');
    dom.click()
    console.log(dom.value)
}

function getFile(files) {
    console.log(files[0])
    var reader = new FileReader();
    reader.onload = function (ev) {
        var code = this.result;
        var highCode = hljs.highlightAuto(code).value;
        // document.getElementById('preCode').innerHTML = '<code id="display"></code>';
        document.getElementById('preCode').style.visibility = 'visible';
        document.getElementById("display").innerHTML =highCode
        // document.getElementById("display").innerText = this.result;
    }
    reader.readAsText(files[0]);
}

function uploadFile() {
    var form = new FormData();
    form.append("file", document.getElementById("display").innerText);

    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if(xhr.readyState == 4) {
            if((xhr.status >= 200 && xhr.status < 300) || xhr.status == 304){
                dealResponse(xhr.responseText);
            }
            else {
                alert("请求失败:" + xhr.status)
            }
        }
    }
    xhr.open("POST", "/CompilingServlet", true);
    xhr.send(form);
}

function dealResponse(data) {
    var tokenFrameElement = document.createDocumentFragment();
    var errorFrameElement = document.createDocumentFragment();
    var symbolFrameElement = document.createDocumentFragment();

    var tokenParentDiv = document.createElement('div');
    var errorParentDiv = document.createElement('div');
    var symbolParentDiv = document.createElement('div');
    tokenParentDiv.style.marginTop = '46px'
    errorParentDiv.style.marginTop = '46px'
    symbolParentDiv.style.marginTop = '46px'

    var tokenArray = JSON.parse(data).token;
    var errorArray = JSON.parse(data).error;
    var symbolArray = JSON.parse(data).symbol;

    tokenArray.forEach(function (item) {
        var div = document.createElement("div");
        var word = document.createElement("span");
        word.style.width = "200px";
        word.style.display = "inline-block";
        word.innerText = item.word;
        var token = document.createElement("span");
        token.style.marginLeft = "20px"
        token.innerText = item.token;
        div.appendChild(word);
        div.appendChild(token);
        tokenParentDiv.appendChild(div);
    });
    errorArray.forEach(function (item) {
        var div = document.createElement("div");
        var word = document.createElement("span");
        word.style.width = "200px";
        word.style.display = "inline-block";
        word.innerText = item.index;
        var token = document.createElement("span");
        token.style.marginLeft = "20px"
        token.innerText = item.word;
        div.appendChild(word);
        div.appendChild(token);
        errorParentDiv.appendChild(div);
    });
    symbolArray.forEach(function (item) {
        var div = document.createElement("div");
        var word = document.createElement("span");
        word.style.width = "20px";
        word.style.display = "inline-block";
        word.innerText = item.addr;
        var token = document.createElement("span");
        token.style.marginLeft = "45px"
        token.style.display = 'inline-block';
        token.style.width = '230px';
        token.innerText = item.name;
        var length = document.createElement("span");
        length.style.marginLeft = "1px"
        length.innerText = item.length;
        div.appendChild(word);
        div.appendChild(token);
        div.appendChild(length);
        symbolParentDiv.appendChild(div);
    })
    tokenFrameElement.appendChild(tokenParentDiv);
    errorFrameElement.appendChild(errorParentDiv);
    symbolFrameElement.appendChild(symbolParentDiv);
    document.getElementById("token").appendChild(tokenFrameElement);
    document.getElementById("error").appendChild(errorFrameElement);
    document.getElementById("symbol").appendChild(symbolFrameElement);
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

(function() {
    var subTitle = document.getElementById('subTitle');
    var subTitleParent = document.getElementById('subTitleParent');
    var parentWidth = subTitle.parentElement.offsetWidth;

    subTitle.style.width = parentWidth + 'px';
    subTitleParent.style.width = parentWidth + 'px';

    window.onresize = function (ev) {
        var subTitle = document.getElementById('subTitle');
        var parentWidth = subTitle.parentElement.offsetWidth;

        subTitle.style.width = parentWidth + 'px';
        subTitleParent.style.width = parentWidth + 'px';
    }
})()