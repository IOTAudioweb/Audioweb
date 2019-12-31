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
    <!-- jsp文件头和头部 -->
    <%@ include file="../index/top.jsp" %>
    <script type="text/javascript" src="static/js/jquery.min.js"></script>
    <link type="text/css" rel="stylesheet" href="plugins/zTree/3.5/zTreeStyle.css"/>
    <script type="text/javascript" src="plugins/zTree/3.5/jquery.ztree.core-3.5.js"></script>
</head>
<body class="no-skin">
<!-- /section:basics/navbar.layout -->
<div class="main-container" id="main-container">
    <!-- /section:basics/sidebar -->
    <div class="main-content">
        <div class="main-content-inner">
            <div class="page-content">
                <div class="row">
                    <div class="col-xs-12">
                        <form action="users/${MSG}.do" name="userForm" id="userForm" method="post">
                            <input type="hidden" name="userid" id="userid" value="${user.userid}"/>
                            <input type="hidden" name="roleId" id="roleId" value="${user.roleId}"/>
                            <div id="suopu" style="padding-top: 13px;">
                                <table id="table_report" class="table table-striped table-bordered table-hover">
                                    <tr>
                                        <td style="width:90px;text-align: right;padding-top: 13px;">用户姓名:</td>
                                        <td><input type="text" value="${user.username}" id="username" name="username"
                                                   title="用户姓名:" style="width:98%;"/></td>
                                    </tr>
                                    <c:if test="${MSG=='addU' }">
                                        <tr>
                                            <td style="width:90px;text-align: right;padding-top: 13px;">登录账号:</td>
                                            <td><input type="text" value="${user.loginid }" id="loginid" name="loginid"
                                                       title="登录账号" style="width:98%;"/></td>
                                        </tr>
                                        <tr>
                                            <td style="width:90px;text-align: right;padding-top: 13px;">登录密码:</td>
                                            <td><input type="password" id="password" name="password" title="登录密码"
                                                       style="width:98%;"/></td>
                                        </tr>
                                        <tr>
                                            <td style="width:90px;text-align: right;padding-top: 13px;">确认密码:</td>
                                            <td><input type="password" id="passwordcopy" name="passwordcopy"
                                                       title="登录密码" style="width:98%;"/></td>
                                        </tr>
                                    </c:if>
                                    <tr>
                                        <td style="width:90px;text-align: right;padding-top: 13px;">用户角色:</td>
                                        <td>
                                            <select class="chosen-select form-control" id="roleId" name="roleId"
                                                    value="${user.roleId}" data-placeholder="请分配角色" style="width:98%;">
                                                <c:forEach items="${rolelist}" var="role">
                                                    <option value="${role.roleId}"
                                                            <c:if test="${user.roleId==role.roleId}">selected</c:if>>${role.roleName }</option>
                                                </c:forEach>
                                            </select>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="width:90px;text-align: right;padding-top: 13px;">备注:</td>
                                        <td><textarea name="note" id="note" wrap="soft" value="${user.note}" title="备注"
                                                      style="width:98%;"></textarea></td>
                                    </tr>
                                    <tr>
                                        <td style="text-align: center;" colspan="10">
                                            <c:if test="${MSG=='addU' }">
                                                <a class="btn btn-sm btn-primary" onclick="save();">保存</a>
                                            </c:if>
                                            <c:if test="${MSG=='editU' }">
                                                <a class="btn btn-sm btn-primary" onclick="edit();">保存</a>
                                            </c:if>
                                            <a class="btn btn-sm btn-danger" onclick="top.Dialog.close();">取消</a>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                            <div id="suopu2" class="center" style="display:none"><br/><br/><br/><br/><img
                                    src="static/images/jiazai.gif"/><br/><h4 class="lighter block green"></h4></div>
                        </form>
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
<!-- inline scripts related to this page -->
<!-- 下拉框 -->
<script src="static/ace/js/chosen.jquery.js"></script>
<!--提示框-->
<script type="text/javascript" src="static/js/jquery.tips.js"></script>
</body>
<script type="text/javascript">
    $(top.hangge());
    $(document).ready(function () {
        //下拉框
        if (!ace.vars['touch']) {
            $('.chosen-select').chosen({allow_single_deselect: true});
            $(window)
                .off('resize.chosen')
                .on('resize.chosen', function () {
                    $('.chosen-select').each(function () {
                        var $this = $(this);
                        $this.next().css({'width': $this.parent().width()});
                    });
                }).trigger('resize.chosen');
            $(document).on('settings.ace.chosen', function (e, event_name, event_val) {
                if (event_name != 'sidebar_collapsed') return;
                $('.chosen-select').each(function () {
                    var $this = $(this);
                    $this.next().css({'width': $this.parent().width()});
                });
            });
            $('#chosen-multiple-style .btn').on('click', function (e) {
                var target = $(this).find('input[type=radio]');
                var which = parseInt(target.val());
                if (which == 2) $('#form-field-select-4').addClass('tag-input-style');
                else $('#form-field-select-4').removeClass('tag-input-style');
            });
        }
    });

    function save() {
        if ($("#loginid").val() == null || $("#loginid").val() == "") {
            $("#loginid").tips({
                side: 3,
                msg: '请输入登录账号',
                bg: '#AE81FF',
                time: 3
            });
            $("#loginid").focus();
            return false;
        } else {
            $.post("users/checkloginid.do", {
                loginid: $("#loginid").val(),
                userid: $("#user_id").val()
            }, function (data) {
                if (data.result == "success") {
                    if ($("#username").val() == null || $("#username").val() == "") {
                        $("#username").tips({
                            side: 3,
                            msg: '请输入用户姓名',
                            bg: '#AE81FF',
                            time: 3
                        });
                        $("#username").focus();
                        return false;
                    } else if ("${MSG}" == "addU" && ($("#password").val() == null || $("#password").val() == "")) {
                        $("#password").tips({
                            side: 3,
                            msg: '请输入登录密码',
                            bg: '#AE81FF',
                            time: 3
                        });
                        $("#password").focus();
                        return false;
                    } else if ("${MSG}" == "addU" && ($("#password").val() != $("#passwordcopy").val())) {
                        $("#password").tips({
                            side: 3,
                            msg: '两次密码输入不一致，请检查',
                            bg: '#AE81FF',
                            time: 3
                        });
                        $("#password").focus();
                        return false;
                    } else {
                        var url = "users/${MSG}.do";
                        $.post(url, $("#userForm").serialize(), function (data) {
                            if ("success" == data.result) {
                                $("#suopu").hide();
                                $("#suopu2").show();
                                top.Dialog.close();
                            }
                        });
                    }
                } else {
                    $("#loginid").tips({
                        side: 3,
                        msg: '登陆名已存在',
                        bg: '#AE81FF',
                        time: 3
                    });
                    $("#loginid").focus();
                    return false;
                }
            });
        }

    }

    function edit() {
        if ($("#username").val() == "") {
            $("#username").tips({
                side: 3,
                msg: '请输入用户姓名',
                bg: '#AE81FF',
                time: 2
            });
            $("#username").focus();
            return false;
        }
        var url = "users/${MSG}.do";
        $.post(url, $("#userForm").serialize(), function (data) {
            if ("success" == data.result) {
                $("#suopu").hide();
                $("#suopu2").show();
                top.Dialog.close();
            }
        });
    }
</script>
</html>