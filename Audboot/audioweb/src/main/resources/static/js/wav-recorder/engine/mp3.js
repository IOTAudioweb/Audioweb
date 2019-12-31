/*
mp3编码器，需带上mp3-engine.js引擎使用
https://github.com/xiangyuecn/Recorder

当然最佳推荐使用mp3、wav格式，代码也是优先照顾这两种格式
浏览器支持情况
https://developer.mozilla.org/en-US/docs/Web/HTML/Supported_media_formats
*/
(function () {
    "use strict";
    var mp3;
    var remain = [];//存余数据
    var blockSize = 1152; //帧大小
    Recorder.prototype.enc_mp3 = {
        stable: true
        , testmsg: "采样率范围48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000"
    };

    Recorder.prototype.mp3_init = function (set, True) {
        //var This=this,set=This.set;
        mp3 = new Recorder.lamejs.Mp3Encoder(1, set.sampleRate, set.bitRate);
        True(mp3);
    };


    Recorder.prototype.mp3 = function (res, time, True, False) {
        //var This=this,set=This.set,size=res.length;
        //https://github.com/wangpengfei15975/recorder.js
        //https://github.com/zhuker/lamejs bug:采样率必须和源一致，不然8k时没有声音，有问题fix：https://github.com/zhuker/lamejs/pull/11
        //var mp3=new Recorder.lamejs.Mp3Encoder(1,set.sampleRate,set.bitRate);
        var data = [];
        //var blockSize=1152;
        remain.push.apply(remain, res);
        while (blockSize <= remain.length) {
            var buf = mp3.encodeBuffer(remain.splice(0, blockSize));
            if (buf.length > 0) {
                data.push(buf);
            }
            ;
        }
        //remain.splice(0,idx);
        if (data.length > 0) {
            data.push(time);
            True(new Blob(data, {type: "audio/mp3"}));
        }
        /*var data=[];
        
        var idx=0;
        var run=function(){
            if(idx<size){
                var buf=mp3.encodeBuffer(res.subarray(idx,idx+blockSize));
                if(buf.length>0){
                    data.push(buf);
                };
                idx+=blockSize;
                setTimeout(run);//Worker? 复杂了
            }else{
                var buf=mp3.flush();
                if(buf.length>0){
                    data.push(buf);
                };
                
                True(new Blob(data,{type:"audio/mp3"}));
            };
        };
        run();*/
    }

})();