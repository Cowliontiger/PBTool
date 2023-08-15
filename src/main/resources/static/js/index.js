var socketIsConnected=false;
var checkSocketTimer;
var getRaidoTimer;
window.onload = function () {
    $("#otherContainer").addClass("divHide")
    $("#data_container").addClass("divHide")

}
/**
 * 获取protocolId
 * @returns {*|jQuery}
 */
function protocolIdChange() {
    var objS = document.getElementById("protocolIdName");
    var val = objS.options[objS.selectedIndex].value;
    getJsonTemplate(val.replace(";","").split("=")[0]);
    //document.getElementById("paramData").value ="{ \"protocolId\":\""+val.replace(";","").split("=")[1]+"\"}";
}

/**
 * 请求类型变化
 */
function methodTypeChange() {
    if($("#methodType option:selected").val()=="SOCKET"){
        $("#socketContainer").removeClass("divHide")
        $("#otherContainer").addClass("divHide")

    }
    else {
        $("#socketContainer").addClass("divHide")
        $("#otherContainer").removeClass("divHide")

    }

}/**
 * 数据格式变化
 */
function dataTypeChange() {
    if($("#protocolType option:selected").val()=="application/json"){
        $("#protoContainer").addClass("divHide")
        $("#data_container").removeClass("divHide")


    }
    else {
        $("#protoContainer").removeClass("divHide")
        $("#data_container").addClass("divHide")

    }

}
/**
 * 文件上传
 */
function uploadProtoFile() {
    var formData = new FormData($("#protoFileForm")[0]);
    var protoFile = getFileName();
    $.ajax({
        url : '/uploadProtoFile',
        type : 'POST',
        data : formData,
        // 告诉jQuery不要去处理发送的数据
        processData : false,
        // 告诉jQuery不要去设置Content-Type请求头
        contentType : false,
        success : function(responseStr) {
            $("#uploadResult").html(responseStr);
            addProtoFileOption(protoFile);
        }
    });
}

/**
 * 动态添加 protoFile
 * @param protoFile
 */
function addProtoFileOption(protoFile) {
    var flag = true;
    $("#protoFileName option").each(function (){
        var txt = $(this).text();
        if (txt == protoFile) {
            flag = false;
        }
    });
    if (flag) {
        $("#protoFileName").append("<option value=" + protoFile + ">" + protoFile + "</option>");
    }
}

/**
 * 获取protocolId
 * @returns {*|jQuery}
 */
/*
function protocolIdChange() {
    inputDataChange();
}
*/
/**
 * 添加参数
 * @returns {*|jQuery}
 */
function AddData() {
    let inputstr="<div class=\"dataLine\" id=\"input_template\">参数名: <input type=\"text\" name=\"keyname\" class=\"input_data\" onchange=\"inputDataChange();\"/> 参数值: <input type=\"text\" name=\"keynvalue\" class=\"input_data\" onchange=\"inputDataChange();\"/>   <button onclick=\"DelData(this);\";>删除</button></div>"
    $("#data_container").append(inputstr);
    inputDataChange();

}
/**
 * 删除参数
 * @returns {*|jQuery}
 */
function DelData(btn){
    let div=btn.parentElement;
    div.parentNode.removeChild(div);
    inputDataChange();
}
/**
 * 参数改变
 * @returns {*|jQuery}
 */
function inputDataChange() {
    console.log("inputDataChange");
    let protocolIdName = document.getElementById("protocolIdName");
    let protocolIdNameVal = protocolIdName.options[protocolIdName.selectedIndex].value;

    let dataLines= document.getElementsByClassName("dataLine");

   // let dataStr="{ \"protocolId\""+":"+protocolIdNameVal.replace(";","").replace("\"","").split("=")[1];
    let dataStr="{";
    console.log(protocolIdNameVal)
    for(let i=0;i<dataLines.length;i++){
        let element=dataLines[i];
        let name="\""+element.children[0].value+"\"";
        let val="\""+element.children[1].value+"\"";
        if(i==0){
            dataStr+=name+":"+val;
        }
        else{
            dataStr+=","+name+":"+val;

        }
    }
    dataStr+="}";
    console.log(dataStr);
    console.log("dataLines"+dataLines);
    document.getElementById("paramData").value =dataStr;
}

/**
 * 获取上传文件的名称
 * @returns {*|jQuery}
 */
function getFileName() {
    var protoFile = $("#protoFile").val();
    var index = protoFile.lastIndexOf("\\");
    protoFile = protoFile.substring(index + 1);
    return protoFile;
}

/**
 * 发送请求
 */
// function executeHttpRequest() {
//     console.log("开始执行请求！");
//     var rspStr;
//     var interfaceUrl = $("#interfaceUrl").val();
//     if (interfaceUrl == '') {
//         $("#errorMsg").html("请输入接口地址！");
//         $("#errorMsg").show();
//         return;
//     } else {
//         $("#errorMsg").hide();
//     }
//     // 如果数据格式为：application/x-protobuf，则需要选择或上传proto文件
//     var protocolType = $("#protocolType").val();
//     var protoFileName = $("#protoFileName").val();
//     console.log("protocolType", protocolType);
//     console.log("protoFileName", protoFileName);
//     if (protocolType == 'application/x-protobuf') {
//         if (protoFileName == '') {
//             var protoFile = getFileName();
//             if (protoFile == '') {
//                 $("#errorMsg").html("请选择或上传proto文件！");
//                 $("#errorMsg").show();
//                 return;
//             } else {
//                 protoFileName = protoFile;
//                 $("#errorMsg").hide();
//             }
//         } else {
//             $("#errorMsg").hide();
//         }
//     }
//
//     var data = {
//         "interfaceUrl" : interfaceUrl,
//         "methodType" : $("#methodType").val(),
//         "protocolType" : protocolType,
//         "protoFileName" : protoFileName,
//         "paramData" : $("#paramData").val(),
//         "protocolIdName" : document.getElementById("protocolIdName").
//             options[document.getElementById("protocolIdName").selectedIndex].value.replace(";","")
//     }
//
//     $.ajax({
//         type: "POST",
//         url: '/executeHttpRequest',
//         data: data,
//         dataType: 'json',
//         success: function (result) {
//             console.log(result)
//             addDebug(data,result);
//
//             $("#requestHeader").html(result.requestHeader);
//             $("#requestBody").html(result.requestBody);
//             $("#responseHeader").html(result.responseHeader);
//             $("#responseBody").html(result.responseBody);
//
//         },
//         reject(reason) {
//             addDebug(data,"请求失败"+reason);
//
//         }
//     });
//
//
// }
function executeHttpRequest() {
    console.log("开始执行请求！");
    console.log("socketIsConnected:"+socketIsConnected);
    var sendUrl="";

    if($("#methodType option:selected").val()=="SOCKET"){
        //console.log("socket")
        var host = $("#host").val();
        var port = $("#port").val();
        var url="http://"+host+":"+port;
        checkSocket();
        if (host==""||port=="") {
            //console.log(url);
            $("#errorMsg").html("请输入接口地址！");
            $("#errorMsg").show();
            return;
        }

        else if(!socketIsConnected){
            //console.log(socketIsConnected);
            $("#errorMsg").html("socket服务器未连接！");
            $("#errorMsg").show();

            return;

        }
        else {
            sendUrl=url;
            $("#errorMsg").hide();
        }
    }
    else {
        console.log("other")

        var interfaceUrl = $("#interfaceUrl").val();
        if (interfaceUrl == '') {
            $("#errorMsg").html("请输入接口地址！");
            $("#errorMsg").show();
            return;
        } else {
            sendUrl=interfaceUrl;
            $("#errorMsg").hide();
        }
    }

    // 如果数据格式为：application/x-protobuf，则需要选择或上传proto文件
    var protocolType = $("#protocolType").val();
    var protoFileName = $("#protoFileName").val();
    console.log("protocolType", protocolType);
    console.log("protoFileName", protoFileName);
    if (protocolType == 'application/x-protobuf') {
        if (protoFileName == '') {
            var protoFile = getFileName();
            if (protoFile == '') {
                $("#errorMsg").html("请选择或上传proto文件！");
                $("#errorMsg").show();
                return;
            } else {
                protoFileName = protoFile;
                $("#errorMsg").hide();
            }
        } else {
            $("#errorMsg").hide();
        }
    }

    console.log(sendUrl)
    let data = {
        "interfaceUrl" : sendUrl,
        "methodType" : $("#methodType").val(),
        "protocolType" : protocolType,
        "contentType" : protocolType,
        "protoFileName" : protoFileName,
        "paramData" : $("#paramData").val(),
        "protocolIdName" : document.getElementById("protocolIdName").
            options[document.getElementById("protocolIdName").selectedIndex].value.replace(";","")
    }

    $.ajax({
        type: "POST",
        url: '/executeHttpRequest',
        data: data,
        dataType: 'json',
        success: function (result) {
            console.log(result)
            addDebug(true,data,result);
            $("#requestHeader").html(result.requestHeader);
            $("#requestBody").html(result.requestBody);
            $("#responseHeader").html(result.responseHeader);
            $("#responseBody").html(result.responseBody);
        },
        error:function (e) {
            //返回500错误 或者其他 http状态码错误时 需要在error 回调函数中处理了 并且返回的数据还不能直接alert，需要使用
            //$.parseJSON 进行转译    res.msg 是自己组装的错误信息通用变量
            var res = $.parseJSON(e.responseText);
            console.log(res.status);
            addDebug(false,data,"请求失败"+res.status+"    error:"+res.error);

        },


    });


}
/**
 * 添加debug信息
 * @param protoFile
 */
function addDebug(isSucess,data,rspStr){
    var myDate=new Date()
    debugStr= $("#debug").text();

    let paramData=data.paramData;
    let responseBody="";
    if(isSucess==true){
    responseBody=rspStr.responseBody;
    paramData = paramData.replace(/[\r]/g, "");
    paramData = paramData.replace(/[\n]/g, "");
    paramData=paramData.replace(/\s/g,"");

    responseBody=responseBody.replace(/[\r\n]/g, "");
    responseBody=responseBody.replace(/\s/g,"");
    responseBody=responseBody.replace(/\x00/g,"");
    }
    else {
        responseBody=rspStr;
    }
    console.log(paramData)
    debugAddStr=myDate.toLocaleString()+ " 执行请求 "+data.protocolIdName+" \n "+"           req:"+paramData+" \n "+"           rsp:"+responseBody;
    $("#debug").text(debugStr+" \n "+debugAddStr);
    $('#debug').scrollTop($('#debug')[0].scrollHeight);
    $('#debug').resize().scrollTop($('#debug')[0].scrollHeight);
}

/**
 * 添加广播信息
 * @param protoFile
 */
function getRaido(){
    // console.log("getradio请求");
    var rspStr="";
    let data = {
    }

    $.ajax({
        type: "POST",
        url: '/getRadio',
        data: data,
        dataType: 'text',
        success: function (result) {
            rspStr=result.toString();

           // console.log("Raido:"+rspStr);
           if (rspStr!=""){
            var myDate=new Date()
            debugStr= $("#radio").text();
            let responseBody=rspStr;
            // responseBody=responseBody.replace(/[\r\n]/g, "");
            // responseBody=responseBody.replace(/\s/g,"");
            // responseBody=responseBody.replace(/\x00/g,"");
            let reg=new RegExp("<br>","g");
            responseBody=responseBody.replace(reg," \n ");
            console.log(responseBody);
            debugAddStr="------------------------"+" \n "+myDate.toLocaleString()+ " 收到广播 "+" \n "+responseBody.toString();
            $("#radio").text(debugStr+" \n "+debugAddStr);
            if($('#autoScroll').text()=="自动滚动"){
                $('#radio').scrollTop($('#radio')[0].scrollHeight);
                $('#radio').resize().scrollTop($('#radio')[0].scrollHeight);
            }

            }
        }


    });

}
/**
 * 添加广播信息
 * @param protoFile
 */
function autoScrollSwitch(){
    console.log($('#autoScroll').text());
    if($('#autoScroll').text()=="自动滚动"){
        $('#autoScroll').html('手动滚动');
    }
    else {
        $('#autoScroll').html('自动滚动');


    }
}
/**
 * 连接socket
 */
function initSocket(){
    console.log("开始执行请求！initSocket");

    let data = {
        "host" : $("#host").val(),
        "port" : $("#port").val()
    }

    $.ajax({
        type: "POST",
        url: '/initSocket',
        data: data,
        dataType: 'json',
        success: function (result) {
            console.log(result)
            $("#socketState").html(result.toString());
            socketIsConnected=result;

        }
    });
     checkSocketTimer = window.setInterval(function () {
        checkSocket();
    }, 3000);
     getRaidoTimer= window.setInterval(function() {
        getRaido();
    },3000);
}

/**
 * 断开socket
 */
function closeSocket(){
    console.log("开始执行请求！closeSocket");

    let data = {
    }

    $.ajax({
        type: "POST",
        url: '/closeSocket',
        data: data,
        dataType: 'json',
        success: function (result) {
            console.log(result);
            $("#socketState").html(result.toString());
            clearInterval(checkSocketTimer);
            clearInterval(getRaidoTimer);
        }
    });
}

/**
 *检查socket
 */
// function checkSocket(){
//     //console.log("开始执行请求！checkSocket");
//
//     let data = {
//     }
//
//     $.ajax({
//         type: "POST",
//         url: '/checkSocket',
//         data: data,
//         dataType: 'json',
//         success: function (result) {
//             if(result.toString()=="false"){
//                 $("#socketState").html("socket断线"+result.toString());
//             }
//             $("#socketState").html(result.toString());
//
//         }
//     });
// }
function checkSocket(){
    //console.log("开始执行请求！checkSocket");

    let data = {
    }

    $.ajax({
        type: "POST",
        url: '/checkSocket',
        data: data,
        dataType: 'json',
        success: function (result) {
            // console.log(result)
            if(result.toString()=="false"){
                $("#socketState").html("socket断线"+result.toString());
            }
            $("#socketState").html(result.toString());
            socketIsConnected=result;
        }
    });
}
/**
 * 取得json模板
 */
function getJsonTemplate(){
    var data = {
       // "interfaceUrl" : interfaceUrl,
       // "methodType" : $("#methodType").val(),
       // "protocolType" : protocolType,
       // "protoFileName" : protoFileName,
       // "paramData" : $("#paramData").val(),
        "protocolIdName" : document.getElementById("protocolIdName").
            options[document.getElementById("protocolIdName").selectedIndex].value.replace(";","")
    }
    $.ajax({
        type: "POST",
        url: '/getJsonTemplate',
        data: data,
        dataType: 'text',
        success: function (result) {
            console.log("getJsonTemplate result:"+result);
            document.getElementById("paramData").value = result;
            //$("#socketState").html(result);
            //$("paramData").value = result;
        }
    });
}
