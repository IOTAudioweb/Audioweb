<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://"
            + request.getServerName() + ":" + request.getServerPort()
            + path + "/";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <base href="<%=basePath%>">
    <!-- 下拉框 -->
    <link rel="stylesheet" href="static/ace/css/chosen.css"/>
    <!-- layui -->
    <link rel="stylesheet" href="static/layuiadmin/layui/css/layui.css">
    <!-- jsp文件头和头部 -->
    <%@ include file="../index/top.jsp" %>
</head>
<body class="no-skin">
<!-- /section:basics/navbar.layout -->
<div class="main-container" id="main-container">
    <!-- /section:basics/sidebar -->
    <div class="main-content">
        <div class="main-content-inner">
            <div class="page-content">
                <div class="row">
                    <div class="layui-card">
                        <div class="layui-card-header">修改密码</div>
                        <div class="layui-card-body">
                            <form action="users/editUserPwd.do" name="userForm" id="userForm" method="post">
                                <input type="hidden" name="userid" id="userid" value="${user.userid }"/>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">登录账号：</label>
                                    </div>
                                    <div class="layui-input-block">
                                        <input type="text" lay-verify="required" autocomplete="off" readonly="readonly"
                                               value="${user.loginid }" class="layui-input">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">用户名:</label>
                                    </div>
                                    <div class="layui-input-block layui-col-md8">
                                        <input type="text" name="username" id="username" lay-verify="title"
                                               autocomplete="off" placeholder="请输入用户名" value="${user.username }"
                                               class="layui-input" onchange="save(this.id,this.value)">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">原密码:</label>
                                    </div>
                                    <div class="layui-input-block layui-col-md8">
                                        <input type="password" name="oldpwd" id="oldpwd" placeholder="请输入密码"
                                               autocomplete="off" class="layui-input"
                                               onchange="save(this.id,this.value)">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">新密码:</label>
                                    </div>
                                    <div class="layui-input-block layui-col-md8">
                                        <input type="password" name="newpwd" id="newpwd" placeholder="请输入密码"
                                               autocomplete="off" class="layui-input"
                                               onchange="save(this.id,this.value)">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">确认密码:</label>
                                    </div>
                                    <div class="layui-input-block layui-col-md8">
                                        <input type="password" name="chkpwd" id="chkpwd" placeholder="请输入密码"
                                               autocomplete="off" class="layui-input"
                                               onchange="save(this.id,this.value)">
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
                <!-- /.col -->
            </div>
            <!-- /.row -->
        </div>
        <!-- /.page-content -->
    </div>
</div>
<!-- /.main-content -->
</div>
<!-- /.main-container -->
<!-- basic scripts -->
<!-- 页面底部js¨ -->
<%@ include file="../index/foot.jsp" %>
<!-- ace scripts -->
<script src="static/ace/js/ace/ace.js"></script>
<!--layui-->
<script src="static/layuiadmin/layui/layui.js" charset="utf-8"></script>
</body>
<script type="text/javascript">
    $(top.hangge());//关闭加载状态
    var is = false, ispwd = false, check = false, issame = false;
    layui.use(['form', 'layer'], function () {
        var form = layui.form
        layer = layui.layer;
    });

    //保存
    function save(id, value) {
        console.log(id);
        is = checks(id, value);
    }

    //保存
    function saveAll() {
        if (is && ispwd && check && issame) {
            return $("#userForm").serializeArray();
        }
    }

    function checks(id, value) {
        if (value == "") {
            layer.tips('请输入正确的数据', '#' + id, {
                tips: [3, '#3595CC'],
                time: 1000
            });
            return false;
        }
        if (id == "oldpwd") {
            $.ajax({
                type: "post",
                url: "users/judgePwd",
                data: "password=" + value,
                async: true,
                success: function (data) {
                    if ("success" == data.result) {
                        layer.tips('密码正确!', '#' + id, {
                            tips: [1, '#3595CC'],
                            time: 1000
                        });
                        ispwd = true;
                    } else {
                        layer.tips('密码错误!', '#' + id, {
                            tips: [1, '#3595CC'],
                            time: 1000
                        });
                        ispwd = false;
                    }
                }
            });
            if ($("#newpwd").val() != "" && $("#newpwd").val() == $("#oldpwd").val()) {
                layer.tips("与新密码相同", '#newpwd', {
                    tips: [1, '#3595CC'],
                    time: 1000
                });
                issame = false;
                return false;
            } else if ($("#newpwd").val() != "") {
                issame = true;
            }
        } else if (id == "newpwd") {
            if ($("#newpwd").val() == $("#oldpwd").val()) {
                layer.tips("与原密码相同", '#newpwd', {
                    tips: [1, '#3595CC'],
                    time: 1000
                });
                issame = false;
                return false;
            } else if ($("#oldpwd").val() != "") {
                issame = true;
            }
            if ($("#chkpwd").val() != "" && $("#newpwd").val() != $("#chkpwd").val()) {
                layer.tips("两次密码不一致", '#newpwd', {
                    tips: [1, '#3595CC'],
                    time: 1000
                });
                check = false;
                return false;
            } else if ($("#chkpwd").val() != "") {
                layer.tips("校验正确", '#newpwd', {
                    tips: [1, '#3595CC'],
                    time: 1000
                });
                check = true;
            }
        } else if (id == "chkpwd") {
            if ($("#newpwd").val() != $("#chkpwd").val()) {
                layer.tips("两次密码不一致", '#chkpwd', {
                    tips: [1, '#3595CC'],
                    time: 1000
                });
                check = false;
                return false;
            } else {
                layer.tips("校验正确", '#chkpwd', {
                    tips: [1, '#3595CC'],
                    time: 1000
                });
                check = true;
            }
        }
        return true;
    }
</script>
</html>