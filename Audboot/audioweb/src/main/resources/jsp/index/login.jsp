<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String path = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">

<head>
    <title>${pd.SYSNAME}</title>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <link rel="stylesheet" href="static/login/bootstrap.min.css"/>
    <!-- <link rel="stylesheet" href="static/login/css/camera.css" />
    <link rel="stylesheet" href="static/login/bootstrap-responsive.min.css" /> -->
    <link rel="stylesheet" href="static/login/matrix-login.css"/>
    <link rel="stylesheet" href="static/login/css/style.css">
    <link href="static/login/font-awesome.css" rel="stylesheet"/>
    <script type="text/javascript" src="static/login/js/jquery-1.9.1.min.js"></script>
    <style type="text/css">
        /*
       body{
        -webkit-transform: rotate(-3deg);
        -moz-transform: rotate(-3deg);
        -o-transform: rotate(-3deg);
        padding-top:20px;
        }
        */
    </style>
    <script>
        //window.setTimeout(showfh,3000);
        var timer;

        function showfh() {
            fhi = 1;
            //关闭提示晃动屏幕，注释掉这句话即可
            timer = setInterval(xzfh2, 10);
        };
        var current = 0;

        function xzfh() {
            current = (current) % 360;
            document.body.style.transform = 'rotate(' + current + 'deg)';
            current++;
            if (current > 360) {
                current = 0;
            }
        };
        var fhi = 1;
        var current2 = 1;

        function xzfh2() {
            if (fhi > 50) {
                document.body.style.transform = 'rotate(0deg)';
                clearInterval(timer);
                return;
            }
            current = (current2) % 360;
            document.body.style.transform = 'rotate(' + current + 'deg)';
            current++;
            if (current2 == 1) {
                current2 = -1;
            } else {
                current2 = 1;
            }
            fhi++;
        };
    </script>
</head>
<body>
<div class="bg"></div>
<div style="width:100%;text-align: center;margin: 0 auto;position: absolute;">
    <div id="loginbox">
        <form action="" method="post" name="loginForm"
              id="loginForm">
            <div class="control-group normal_text">
                <h3>
                    <img src="static/login/logo.png" alt="Logo"/>
                </h3>
            </div>
            <div class="control-group">
                <div class="controls">
                    <div class="main_input_box">
							<span class="add-on bg_lg">
							<i><img height="37" src="static/login/user.png"/></i>
							</span><input type="text" name="loginname" id="loginname" value="" placeholder="请输入用户名"/>
                    </div>
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <div class="main_input_box">
							<span class="add-on bg_ly">
							<i><img height="37" src="static/login/suo.png"/></i>
							</span><input type="password" name="password" id="password" placeholder="请输入密码" value=""/>
                    </div>
                </div>
            </div>
            <div style="float:right;padding-right:10%;">
                <div style="float: left;margin-top:3px;margin-right:2px;">
                    <font color="white">记住密码</font>
                </div>
                <div style="float: left;">
                    <input name="form-field-checkbox" id="saveid" type="checkbox"
                           onclick="savePaw();" style="padding-top:0px;"/>
                </div>
            </div>
            <div class="form-actions">
                <div style="width:86%;padding-left:8%;">

                    <div style="float: left;padding-top:2px;">
                        <i><img src="static/login/yan.png"/></i>
                    </div>
                    <div style="float: left;" class="codediv">
                        <input type="text" name="code" id="code" class="login_code"
                               style="height:16px; padding-top:4px;"/>
                    </div>
                    <div style="float: left;">
                        <i><img style="height:22px;" id="codeImg" alt="点击更换"
                                title="点击更换" src=""/></i>
                    </div>
                    <span class="pull-right" style="padding-right:3%;"><a href="javascript:quxiao();"
                                                                          class="btn btn-success">取消</a></span>
                    <span class="pull-right"><a onclick="severCheck();" class="flip-link btn btn-info" id="to-recover">登录</a></span>
                </div>
            </div>
        </form>
        <div class="controls">
            <div class="main_input_box">
                <font color="white"><span id="nameerr">${pd.SYSOWNER}</span></font>
                <input type="hidden" name="MSG" id="MSG" value="${pd.MSG}"/>
            </div>
        </div>
    </div>
</div>
<!-- <div id="templatemo_banner_slide" class="container_wapper">
    <div class=" " id="camera_slide">
        <div data-src="static/login/images/banner_slide_01.jpg"></div>
        <div data-src="static/login/images/banner_slide_01.jpg"></div>
    </div>
    #camera_wrap_3
</div> -->
<div class="container">
    <div data-src="static/login/images/banner_slide_01.jpg"></div>
</div>
<script type="text/javascript">
    //服务器校验
    function severCheck() {
        if (check()) {

            var loginname = $("#loginname").val();
            var password = $("#password").val();
            var code = "infopublic" + loginname + ",info," + password + "infopublic" + ",info," + $("#code").val();
            $.ajax({
                type: "POST",
                url: 'login_login',
                data: {KEYDATA: code, tm: new Date().getTime()},
                dataType: 'json',
                cache: false,
                success: function (data) {
                    if ("success" == data.result) {
                        saveCookie();
                        window.location.href = "main/index";
                    } else if ("usererror" == data.result) {
                        $("#loginname").tips({
                            side: 1,
                            msg: "用户名或密码有误",
                            bg: '#FF5080',
                            time: 15
                        });
                        showfh();
                        $("#loginname").focus();
                    } else if ("codeerror" == data.result) {
                        $("#code").tips({
                            side: 1,
                            msg: "验证码输入有误",
                            bg: '#FF5080',
                            time: 15
                        });
                        showfh();
                        $("#code").focus();
                    } else {
                        $("#loginname").tips({
                            side: 1,
                            msg: "缺少参数",
                            bg: '#FF5080',
                            time: 15
                        });
                        showfh();
                        $("#loginname").focus();
                    }
                }
            });
        }
    }

    $(document).ready(function () {
        changeCode();
        $("#codeImg").bind("click", changeCode);
        $(function () {
            var msg = $("#MSG").val();
            console.log(msg);
            if (msg == '1') {
                alert('连接丢失或此账户已被其他用户登录，请稍后重试');
            }
        });
    });

    $(document).keyup(function (event) {
        if (event.keyCode == 13) {
            $("#to-recover").trigger("click");
        }
    });

    function genTimestamp() {
        var time = new Date();
        return time.getTime();
    }

    function changeCode() {
        $("#codeImg").attr("src", "code.do?t=" + genTimestamp());
    }

    //客户端校验
    function check() {

        if ($("#loginname").val() == "") {

            $("#loginname").tips({
                side: 2,
                msg: '用户名不得为空',
                bg: '#AE81FF',
                time: 3
            });
            showfh();
            $("#loginname").focus();
            return false;
        } else {
            $("#loginname").val(jQuery.trim($('#loginname').val()));
        }

        if ($("#password").val() == "") {

            $("#password").tips({
                side: 2,
                msg: '密码不得为空',
                bg: '#AE81FF',
                time: 3
            });
            showfh();
            $("#password").focus();
            return false;
        }
        if ($("#code").val() == "") {

            $("#code").tips({
                side: 1,
                msg: '验证码不得为空',
                bg: '#AE81FF',
                time: 3
            });
            showfh();
            $("#code").focus();
            return false;
        }

        $("#loginbox").tips({
            side: 1,
            msg: '正在登录 , 请稍后 ...',
            bg: '#68B500',
            time: 10
        });

        return true;
    }

    function savePaw() {
        if (!$("#saveid").attr("checked")) {
            $.cookie('loginname', '', {
                expires: -1
            });
            $.cookie('password', '', {
                expires: -1
            });
            $("#loginname").val('');
            $("#password").val('');
        }
    }

    function saveCookie() {
        if ($("#saveid").attr("checked")) {
            $.cookie('loginname', $("#loginname").val(), {
                expires: 7
            });
            $.cookie('password', $("#password").val(), {
                expires: 7
            });
        }
    }

    function quxiao() {
        $("#loginname").val('');
        $("#password").val('');
    }

    jQuery(function () {
        var loginname = $.cookie('loginname');
        var password = $.cookie('password');
        if (typeof(loginname) != "undefined"
            && typeof(password) != "undefined") {
            $("#loginname").val(loginname);
            $("#password").val(password);
            $("#saveid").attr("checked", true);
            $("#code").focus();
        }
    });

</script>
<%-- <c:if test="${'1' == pd.msg}">
    <script type="text/javascript">
    $(tsMsg());
    function tsMsg(){
        alert('此用户在其它终端已经早于您登录,您暂时无法登录');
    }
    </script>
</c:if>
<c:if test="${'2' == pd.msg}">
    <script type="text/javascript">
        $(tsMsg());
        function tsMsg(){
            alert('您被系统管理员强制下线');
        }
    </script>
</c:if> --%>
<%-- <c:if test="${pd.msg != null}">
    <script type="text/javascript">
        $(tsMsg());
        function tsMsg(){
            alert(${pd.msg});
        }
    </script>
</c:if> --%>
<script>
    //TOCMAT重启之后 点击左侧列表跳转登录首页
    if (window != top) {
        top.location.href = '';
    }
</script>
<script src="static/login/js/bootstrap.min.js"></script>
<script src="static/js/jquery-1.7.2.js"></script>
<script src="static/login/js/jquery.easing.1.3.js"></script>
<script src="static/login/js/jquery.mobile.customized.min.js"></script>
<script src="static/login/js/camera.min.js"></script>
<script src="static/login/js/templatemo_script.js"></script>
<script type="text/javascript" src="static/js/jquery.tips.js"></script>
<script type="text/javascript" src="static/js/jquery.cookie.js"></script>
</body>

</html>