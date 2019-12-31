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
                        <div class="layui-card-body">
                            <form action="users/${MSG}.do" name="UserForm" id="UserForm" method="post"
                                  class="layui-form">
                                <input type="hidden" name="userid" id="userid" value="${user.userid}"/>
                                <input type="hidden" name="roleId" id="roleId" value="${user.roleId}"/>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">用户姓名:</label>
                                    </div>
                                    <div class="layui-input-block">
                                        <input type="text" lay-verify="required" name="username" id="username"
                                               autocomplete="off" value="${user.username}" class="layui-input">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">登录账号:</label>
                                    </div>
                                    <div class="layui-input-block">
                                        <input type="text" lay-verify="required" name="loginid" id="loginid"
                                               autocomplete="off" value="${user.loginid}"
                                               <c:if test="${MSG!='addU' }">readonly="readonly"
                                               </c:if>class="layui-input">
                                    </div>
                                </div>
                                <c:if test="${MSG=='addU' }">
                                    <div class="layui-form-item">
                                        <div class="layui-col-md4">
                                            <label class="layui-form-label" style="width: 100px;">登录密码:</label>
                                        </div>
                                        <div class="layui-input-block">
                                            <input type="password" lay-verify="required" name="password" id="password"
                                                   autocomplete="off" class="layui-input">
                                        </div>
                                    </div>
                                    <div class="layui-form-item">
                                        <div class="layui-col-md4">
                                            <label class="layui-form-label" style="width: 100px;">确认密码:</label>
                                        </div>
                                        <div class="layui-input-block">
                                            <input type="password" lay-verify="required" name="passwordcopy"
                                                   id="passwordcopy" autocomplete="off" class="layui-input">
                                        </div>
                                    </div>
                                </c:if>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">用户角色:</label>
                                    </div>
                                    <div class="layui-input-block layui-col-md8">
                                        <select class="form-control" id="roleId" name="roleId"
                                                data-placeholder="请选择角色类型" style="width:98%;">
                                            <c:forEach items="${rolelist}" var="role">
                                                <option value="${role.roleId}"
                                                        <c:if test="${user.roleId==role.roleId}">selected</c:if>>${role.roleName }</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                <div class="layui-form-item ">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">采播模式:</label>
                                    </div>
                                    <div class="layui-input-block layui-col-md8">
                                        <div id="realcasttype" name="typename">
                                            <input type="radio" lay-filter="typeset" name="realcasttype"
                                                   id="realcasttype" value="10" title="本地模式"<c:if
                                                    test="${user.realcasttype == 10 || user.realcasttype == null}">
                                                   checked </c:if>>
                                            <input type="radio" lay-filter="typeset" name="realcasttype"
                                                   id="realcasttype" value="-1" title="急速模式"<c:if
                                                    test="${user.realcasttype == -1}"> checked </c:if>>
                                            <input type="radio" lay-filter="typeset" name="realcasttype"
                                                   id="realcasttype" value="0" title="低延时模式"<c:if
                                                    test="${user.realcasttype == 0}"> checked </c:if>>
                                            <input type="radio" lay-filter="typeset" name="realcasttype"
                                                   id="realcasttype" value="1" title="稳定模式"<c:if
                                                    test="${user.realcasttype == 1}"> checked </c:if>>
                                        </div>
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">备注:</label>
                                    </div>
                                    <div class="layui-input-block layui-col-md8">
                                        <input type="text" lay-verify="required" name="note" id="note"
                                               autocomplete="off" value="${user.note}" class="layui-input">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <div class="layui-input-block" style="margin-top:15px;height: 30px;">
                                        <c:if test="${MSG=='addU' }">
                                            <button type="button" class="layui-btn demo-active" data-type="save">保存
                                            </button>
                                        </c:if>
                                        <c:if test="${MSG=='editU' }">
                                            <button type="button" class="layui-btn demo-active" data-type="edit">保存
                                            </button>
                                        </c:if>
                                        <button type="button" class="layui-btn layui-btn-primary demo-active"
                                                data-type="goback">取消
                                        </button>
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
    layui.use(['form', 'layer'], function () {
        var form = layui.form
        layer = layui.layer;
        var active = {
            menuRights: function () {
                var RoleId = $("#RoleId").val();
                layer.open({
                    type: 2,
                    title: '选择目录分组',
                    maxmin: true,
                    btn: ['确定', '取消'],
                    btnAlign: 'c',
                    area: ['300px', '450px'],
                    anim: 1,
                    content: "users/editRoleRight.do?rid=" + RoleId,
                    yes: function (index, layero) {
                        var body = layer.getChildFrame('body', index);
                        var iframeWin = window[layero.find('iframe')[0]['name']];//得到iframe页的窗口对象，执行iframe页的方法：
                        var ids = iframeWin.save();//调用子页面的方法，得到子页面返回的ids
                        document.RoleForm.menuRights.value = ids;
                        //按钮【按钮一】的回调
                        layer.close(index);
                    }
                });
            },
            save: function () {
                $.post("users/checkloginid.do", {
                    loginid: $("#loginid").val(),
                    userid: $("#user_id").val()
                }, function (data) {
                    if (data.result == "success") {
                        if ($("#username").val() == null || $("#username").val() == "") {
                            layer.tips('请输入用户名称', '#username', {
                                tips: [3, '#3595CC'],
                                time: 1000
                            });
                            return false;
                        } else if (${MSG== 'addU'}&&
                        ($("#loginid").val() == null || $("#loginid").val() == "")
                    )
                        {
                            layer.tips('请输入用户登录名', '#loginid', {
                                tips: [3, '#3595CC'],
                                time: 1000
                            });
                            return false;
                        }
                    else
                        if ("${MSG}" == "addU" && ($("#password").val() == null || $("#password").val() == "")) {
                            layer.tips('请输入用户密码', '#password', {
                                tips: [3, '#3595CC'],
                                time: 1000
                            });
                            return false;
                        } else if ("${MSG}" == "addU" && ($("#password").val() != $("#passwordcopy").val())) {
                            layer.tips('两次密码输入不一致，请检查', '#passwordcopy', {
                                tips: [3, '#3595CC'],
                                time: 1000
                            });
                            return false;
                        } else {
                            var url = "users/${MSG}.do";
                            parent.layer.load();
                            $.post(url, $("#UserForm").serialize(), function (data) {
                                if (data.result == "success") {
                                    var index = parent.layer.getFrameIndex(window.name);
                                    parent.layer.close(index);
                                    window.parent.success();
                                } else {
                                    window.parent.error();
                                }
                            });
                        }
                    } else {
                        layer.tips('登录名已存在', '#loginid', {
                            tips: [3, '#3595CC'],
                            time: 1000
                        });
                        return false;
                    }
                });
            },
            edit: function () {
                if ($("#username").val() == null || $("#username").val() == "") {
                    layer.tips('请输入用户名称', '#username', {
                        tips: [3, '#3595CC'],
                        time: 1000
                    });
                    return false;
                }
                var url = "users/${MSG}.do";
                parent.layer.load();
                $.post(url, $("#UserForm").serialize(), function (data) {
                    if (data.result == "success") {
                        var index = parent.layer.getFrameIndex(window.name);
                        parent.layer.close(index);
                        window.parent.success();
                    } else {
                        window.parent.error();
                    }
                });
            },
            goback: function () {
                var index = parent.layer.getFrameIndex(window.name);
                parent.layer.close(index);
            }
        };
        $('.demo-active').on('click', function () {
            var type = $(this).data('type');
            active[type] ? active[type].call(this) : '';
        });

    });
</script>
</html>