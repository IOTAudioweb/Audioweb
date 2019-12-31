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
    <!-- jsp文件头和头部 -->
    <%@ include file="../index/top.jsp" %>
    <!-- layui -->
    <link rel="stylesheet" href="static/layuiadmin/layui/css/layui.css">
</head>
<body class="no-skin">

<!-- /section:basics/navbar.layout -->
<div class="main-container" id="main-container">
    <!-- /section:basics/sidebar -->
    <div class="main-content">
        <div class="main-content-inner">

            <div class="page-content">
                <div class="page-header">


                    <h1>
                        <small>
                            <i class="ace-icon fa fa-angle-double-right"></i>
                            用户列表
                        </small>

                    </h1>

                </div>
                <div class="row">
                    <div class="col-xs-12">
                        <div class="layui-form-item">
                            <!-- 检索  -->
                            <div class="layui-inline">
									<span class="input-icon">
										<input class="nav-search-input" autocomplete="off" id="namekey" type="text"
                                               style="vertical-align:top;width: 160px; height: 38px" name="namekey"
                                               value="${pd.namekey}" placeholder="输入用户名"/>
										<i class="ace-icon fa fa-search nav-search-icon"></i>
									</span>
                            </div>
                            <div class="layui-inline">
                                <button class="layui-btn layui-btn-primary" onclick="tosearch();" title="检索">
                                    <i class="layui-icon">&#xe615;</i>
                                </button>
                            </div>
                            <div class="layui-inline">
                                <button class="layui-btn  layui-btn-radius layui-btn-normal" onclick="addUser();"
                                        title="添加"><i class="layui-icon">&#xe654;</i>添加
                                </button>
                            </div>
                        </div>
                        <form action="users/listAllUsers.do" method="post" name="Form" id="Form">
                            <!-- 检索  -->
                            <table id="simple-table" class="table table-striped table-bordered table-hover"
                                   style="margin-top:5px;">
                                <thead>
                                <tr>
                                    <th class="center" style="width:35px;">
                                        <label class="pos-rel"><input type="checkbox" class="ace" id="zcheckbox"/><span
                                                class="lbl"></span></label>
                                    </th>
                                    <th class="center" style="width:50px;">序号</th>
                                    <th class="center">用户姓名</th>
                                    <th class="center">用户角色</th>
                                    <th class="center">登录账号</th>
                                    <!-- <th class="center">管理区域</th> -->
                                    <th class="center">账号状态</th>
                                    <th class="center">备注</th>
                                    <th class="center">操作</th>
                                </tr>
                                </thead>

                                <tbody>
                                <!-- 开始循环 -->
                                <c:choose>
                                    <c:when test="${not empty userlist}">
                                        <c:forEach items="${userlist}" var="user" varStatus="vs">
                                            <tr ondblclick="javascript:editUser('${user.userid}');">
                                                <td class='center' style="width: 30px;">
                                                    <label><input type='checkbox' name='ids' value="${user.roleId}"
                                                                  id="${user.userid }" class="ace"/><span
                                                            class="lbl"></span></label>
                                                </td>
                                                <td class='center' style="width: 30px;">${vs.index+1}</td>
                                                <td class='center'>${user.username}</td>
                                                <td class='center'>${user.roleName}</td>
                                                <td class='center'>${user.loginid}</td>
                                                    <%-- <td class='center'>${user.note} </td> --%>
                                                <td class='center'>
                                                    <label id="isuse">
                                                        <input name="switch-field-1"
                                                               onclick="setenable('${user.userid}','${user.username}',this)"
                                                               class="ace ace-switch ace-switch-3" type="checkbox"
                                                               <c:if test="${user.isuse }">checked="checked"</c:if> >
                                                        <span class="lbl"></span>
                                                    </label>
                                                </td>
                                                <td class='center'>${user.note}</td>

                                                <td class="center" style="width: auto;">
                                                    <div class="hidden-sm hidden-xs action-buttons">
                                                        <a class="green" href="javascript:editUser('${user.userid}');">
                                                            <i class="ace-icon fa fa-pencil-square-o bigger-130"
                                                               title="修改"></i>
                                                        </a>
                                                        <a class="red"
                                                           href="javascript:delUser('${user.userid }','${user.username }');">
                                                            <i class="ace-icon fa fa-trash-o bigger-130" title="删除"></i>
                                                        </a>
                                                    </div>
                                                    <div class="hidden-md hidden-lg">
                                                        <div class="inline pos-rel">
                                                            <button
                                                                    class="btn btn-minier btn-yellow dropdown-toggle"
                                                                    data-toggle="dropdown" data-position="auto">
                                                                <i
                                                                        class="ace-icon fa fa-caret-down icon-only bigger-120"></i>
                                                            </button>
                                                            <ul class="dropdown-menu dropdown-only-icon dropdown-yellow dropdown-menu-right dropdown-caret dropdown-close">
                                                                <li><a href="javascript:editUser('${user.userid}');"
                                                                       class="tooltip-success" data-rel="tooltip"
                                                                       title="Edit">
															<span class="green">
																<i class="ace-icon fa fa-pencil-square-o bigger-120"
                                                                   title="修改"></i>
															</span>
                                                                </a></li>
                                                                <li>
                                                                    <a href="javascript:delUser('${user.userid }','${user.username }');"
                                                                       class="tooltip-success" data-rel="tooltip"
                                                                       title="Delete">
															<span class="red"> 
																<i class="ace-icon fa fa-trash-o bigger-120"
                                                                   title="删除"></i>
															</span>
                                                                    </a>
                                                                </li>
                                                            </ul>
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr class="main_info">
                                            <td colspan="100" class="center">没有相关数据</td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                                </tbody>
                            </table>
                            <div class="page-header position-relative">
                                <table style="width:100%;">
                                    <tr>
                                        <td style="vertical-align:top;">
                                            <div class="pagination"
                                                 style="float: right;padding-top: 0px;margin-top: 0px;">${page.pageStr}</div>
                                        </td>
                                    </tr>
                                </table>
                            </div>
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

    <!-- 返回顶部 -->
    <a href="#" id="btn-scroll-up" class="btn-scroll-up btn btn-sm btn-inverse">
        <i class="ace-icon fa fa-angle-double-up icon-only bigger-110"></i>
    </a>

</div>
<!-- /.main-container -->

<!-- basic scripts -->
<!-- 页面底部js¨ -->
<%@ include file="../index/foot.jsp" %>
<!-- 下拉框 -->
<script src="static/ace/js/chosen.jquery.js"></script>
<!-- ace scripts -->
<script src="static/ace/js/ace/ace.js"></script>
<!--layui-->
<script src="static/layuiadmin/layui/layui.js" charset="utf-8"></script>
<script type="text/javascript">
    $(top.hangge());//关闭加载状态
    layui.use(['form', 'layer'], function () {
        var form = layui.form
        layer = layui.layer;
    });

    //检索
    function tosearch() {
        layer.load();
        if (document.getElementById("namekey").value != "") {
            var namekey = document.getElementById("namekey").value;
        } else {
            var namekey = "";
        }
        window.location.href = "<%=basePath%>users/listAllUsers.do?namekey=" + namekey;
    }

    //删除
    function delUser(userId, msg) {
        layer.confirm("确定要删除[" + msg + "]吗?", {
            btn: ['确定', '取消'] //按钮
            , anim: 6
            , icon: 8
        }, function () {
            layer.load();
            var url = "<%=basePath%>users/deleteU.do?userid=" + userId + "&uname=" + msg;
            $.get(url, function (data) {
                layer.closeAll();
                if (data.result == "success") {
                    layer.msg('删除成功！', {
                        icon: 1
                        , time: 1000
                        , end: function () {
                            location.reload(true);
                        }
                    });
                } else {
                    layer.msg('删除失败！', {
                        icon: 2
                        , time: 1000
                        , end: function () {
                        }
                    });
                }
            });
        });
    }

    //修改用户
    function editUser(userid) {
        layer.open({
            type: 2,
            title: '添加角色',
            maxmin: true,
            area: ['840px', '500px'],
            anim: 2,
            content: 'users/editUser.do?userid=' + userid,
            end: function () {

            }
        });
    }

    //添加
    function addUser() {
        //layer.load();
        layer.open({
            type: 2,
            title: '添加用户',
            maxmin: true,
            area: ['840px', '520px'],
            anim: 2,
            content: 'users/addUser.do',
            end: function () {

            }
        });
    }

    //处理按钮点击,修改用户启用性
    function setenable(userid, uname, click) {
        var Status = $(click).is(':checked');
        layer.load();
        $.post("<%=basePath%>users/upEnable.do", {userid: userid, uname: uname, isuse: Status}, function (data) {
            layer.closeAll();
            var str = '修改出错,请刷新页面重试！';
            if (data.result == "success") {
                str = '修改成功';
                layer.msg(str, {
                    icon: 1
                    , time: 1000
                });
            } else {
                layer.msg(str, {
                    icon: 2
                    , time: 1000
                    , end: function () {
                        location.reload(true);
                    }
                });
            }
        });
    }

    function success() {
        layer.closeAll();
        layer.msg('保存成功', {
            icon: 1
            , time: 1000
            , end: function () {
                location.reload(true);
            }
        });
    }

    function error() {
        layer.closeAll();
        layer.msg('保存失败', {
            icon: 2
            , time: 1000
            , end: function () {
                location.reload(true);
            }
        });
    }
</script>
</body>
</html>