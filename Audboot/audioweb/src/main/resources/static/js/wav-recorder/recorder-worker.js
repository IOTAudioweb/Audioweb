/*
* recorder-worker.js 实时转码音频格式
*/
(function (undefined) {
    "use strict";
    var size
        , sampleRate
        , bitRate
        , mode
        , mp3
        //,singleOrDouble
        , blockSize = 1152;
    var remainleft = [];
    var ws = null;
    //var remainright = [];
    importScripts('./lame.min.js');

    function init(set) {
        size = set.bufferSize - 1;
        sampleRate = set.sampleRate;
        bitRate = set.bitRate;
        mode = set.mode;
        if (set.mode == '1') {
            blockSize = 4608;
        }
        mp3 = new lamejs.Mp3Encoder(1, sampleRate, bitRate);
        if (!mp3) {
            postMessage("starterror", "开启监听失败,无法加载编码器!");
        }
    }

    //发送 worker 数据
    function postMessage(cmd, data) {
        self.postMessage({
            cmd: cmd,
            data: data
        });
    }

    //https://github.com/wangpengfei15975/recorder.js
    //https://github.com/zhuker/lamejs bug:采样率必须和源一致，不然8k时没有声音，有问题fix：https://github.com/zhuker/lamejs/pull/11
    function encoding(res, time) {
        var data = [];
        /*if(singleOrDouble){
            var idx=0;
            remainleft.push.apply(remainleft,res[0]);
            remainright.push.apply(remainright,res[1]);
            while(idx+blockSize<=remainleft.length){
                var buf=mp3.encodeBuffer(remainleft.slice(idx,idx+blockSize),remainright.slice(idx,idx+blockSize));
                if(buf.length>0){
                    data.push(buf);
                };
                idx+=blockSize;
            }
            remainleft.splice(0,idx);
            remainright.splice(0,idx);
        }else{*/
        /*for(var j=0;j<size;j++){//floatTo16BitPCM 
            var s=Math.max(-1,Math.min(1,res[j]));
            s=s<0?s*0x8000:s*0x7FFF;
            remainleft.push(s);
        };*/
        remainleft.push.apply(remainleft, res);
        while (blockSize <= remainleft.length) {
            var buf = mp3.encodeBuffer(remainleft.splice(0, blockSize));
            if (buf.length > 0) {
                data.push(buf);
            }
            ;
        }
        //remainleft.splice(0,idx);
        /*}*/
        if (data.length > 0) {
            data.push(time);
            if (ws != null) {
                var blob = new Blob(data, {type: "audio/mp3"});
                ws.send(blob);
                blob = null;
            }
        }
        res = null;
        time = null;
        data = null;
    }

    //---------------- WebSocket ----------------
    function connect(target, taskid, sockettype) {
        if (target == '') {
            postMessage("starterror", "开启失败,未获取此浏览器路径!请刷新重试");
            return;
        }
        if (sockettype == 1) {
            ws = new WebSocket(target);
        } else if (sockettype == 2) {
            ws = new MozWebSocket(target);
        } else {
            postMessage("starterror", "开启失败,此浏览器不支持WebSocket!请更换浏览器重试");
            return;
        }
        ws.binaryData = "blob";
        ws.onopen = function () {//连接打开后
            ws.send("start:" + taskid);//发送任务开始指令
            postMessage("started", "成功连接服务器！");
        };
        ws.onmessage = function (event) {
            console.debug("Received: " + event.data);
        };
        ws.onclose = function (event) {
            console.debug("Info: WebSocket connection closed, Code:" + event.code + (event.reason == "" ? "" : ", Reason: " + event.reason));
            postMessage("stop", "断开服务器连接！");
        };
    }

    function stop() {
        if (ws != null) {
            ws.close();
            ws = null;
        } else {
            postMessage("stoped", "已断开服务器连接！");
        }
    }

    //-----------------接收数据--------------------------
    self.onmessage = function (e) {
        var obj = e.data, data = obj.data;
        //console.log(data);
        switch (obj.cmd) {
            case 'init':
                init(data);
                break;
            case 'encoding':
                encoding(data, obj.time);
                break;
            case 'start':
                connect(data.target, data.taskid, data.sockettype);
                break;
            case 'stop':
                stop();
                break;
        }
    };
})();