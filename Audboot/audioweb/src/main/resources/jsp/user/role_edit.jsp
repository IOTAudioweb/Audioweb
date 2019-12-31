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
                            <form action="users/${MSG}.do" name="RoleForm" id="RoleForm" method="post"
                                  class="layui-form">
                                <input type="hidden" name="RoleId" id="RoleId" value="${RoleId}"/>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">角色名称:</label>
                                    </div>
                                    <div class="layui-input-block">
                                        <input type="text" lay-verify="required" name="RoleName" id="RoleName"
                                               autocomplete="off" value="${RoleName}" class="layui-input">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">角色类型:</label>
                                    </div>
                                    <div class="layui-input-block layui-col-md8">
                                        <select class="form-control" id="RoleLevel" name="RoleLevel"
                                                data-placeholder="请选择角色类型" style="width:98%;">
                                            <c:if test="${UserRoleLevel < 3 }">
                                                <option value="3" <c:if test="${RoleLevel== 3}">selected</c:if>>普通组
                                                </option>
                                            </c:if>
                                            <c:if test="${UserRoleLevel < 2 }">
                                                <option value="2" <c:if test="${RoleLevel== 2 }">selected</c:if>>高级用户组
                                                </option>
                                            </c:if>
                                            <c:if test="${UserRoleLevel < 1 }">
                                                <option value="1" <c:if test="${RoleLevel== 1 }">selected</c:if>>管理员组
                                                </option>
                                            </c:if>
                                        </select>
                                    </div>
                                </div>
                                <c:if test="${MSG != 'editR'}">
                                    <div class="layui-form-item">
                                        <div class="layui-col-md4">
                                            <label class="layui-form-label" style="width: 100px;">权限设置:</label>
                                        </div>
                                        <div class="layui-input-block layui-col-md8">
                                            <div class="layui-inline">
                                                <input type="text" lay-verify="required" name="menuRights"
                                                       readonly="readonly" id="menuRights" autocomplete="off"
                                                       value="${menuRights}" class="layui-input">
                                            </div>
                                            <div class="layui-inline">
                                                <button type="button" class="layui-btn  layui-btn-normal demo-active"
                                                        data-type="menuRights">选择
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>
                                <div class="layui-form-item">
                                    <div class="layui-col-md4">
                                        <label class="layui-form-label" style="width: 100px;">备注:</label>
                                    </div>
                                    <div class="layui-input-block layui-col-md8">
                                        <input type="text" lay-verify="required" name="Description" id="Description"
                                               autocomplete="off" value="${Description}" class="layui-input">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <div class="layui-input-block" style="margin-top:15px;height: 30px;">
                                        <button type="button" class="layui-btn demo-active" data-type="save">保存</button>
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
                if ($("#RoleName").val() == null || $("#RoleName").val() == "") {
                    layer.tips('请输入角色名称', '#RoleName', {
                        tips: [1, '#3595CC'],
                        time: 1000
                    });
                    return false;
                } else if ($("#RoleLevel").val() == null || $("#RoleLevel").val() == "") {
                    layer.tips('请选择角色类型', '#RoleLevel', {
                        tips: [1, '#3595CC'],
                        time: 1000
                    });
                    return false;
                } else if (${MSG== 'addR'}&&
                ($("#menuRights").val() == null || $("#menuRights").val() == "")
            )
                {
                    layer.tips('请输入角色权限', '#menuRights', {
                        tips: [1, '#3595CC'],
                        time: 1000
                    });
                    return false;
                }
            else
                {
                    var url = "users/${MSG}.do";
                    $.post(url, $("#RoleForm").serialize(), function (data) {
                        if ("success" == data.result) {
                            var index = parent.layer.getFrameIndex(window.name);
                            parent.layer.close(index);
                            window.parent.location.reload();
                        }
                    });
                }
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