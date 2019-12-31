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
                <div class="page-header">


                    <h1>
                        <small>
                            <i class="ace-icon fa fa-angle-double-right"></i>
                            角色列表
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
                                               value="${pd.namekey}" placeholder="输入角色名称"/>
										<i class="ace-icon fa fa-search nav-search-icon"></i>
									</span>
                            </div>
                            <div class="layui-inline">
                                <button class="layui-btn layui-btn-primary" onclick="tosearch();" title="检索">
                                    <i class="layui-icon">&#xe615;</i>
                                </button>
                            </div>
                            <div class="layui-inline">
                                <button class="layui-btn  layui-btn-radius layui-btn-normal" onclick="addRole();"
                                        title="添加"><i class="layui-icon">&#xe654;</i>添加
                                </button>
                            </div>
                        </div>
                        <form action="users/listRole.do" method="post" name="Form" id="Form">
                            <table id="simple-table" class="table table-striped table-bordered table-hover"
                                   style="margin-top:5px;">
                                <thead>
                                <tr>
                                    <!-- 								<th class="center" style="width:35px;"> -->
                                    <!-- 									<label class="pos-rel"><input type="checkbox" class="ace" id="zcheckbox" /><span class="lbl"></span></label> -->
                                    <!-- 									</th> -->
                                    <th class="center" style="width:50px;">序号</th>
                                    <th class="center">角色名称</th>
                                    <th class="center">角色类型</th>
                                    <th class="center">授权状态</th>
                                    <th class="center">备注</th>
                                    <th class="center">操作</th>
                                </tr>
                                </thead>

                                <tbody>
                                <!-- 开始循环 -->
                                <c:choose>
                                    <c:when test="${not empty rolelist}">
                                        <c:forEach items="${rolelist}" var="role" varStatus="vs">
                                            <tr ondblclick="javascript:editRoleRight('${role.roleId}','${role.roleLevel}','${role.menuRights}');">
                                                <td class='center' style="width: 30px;">${vs.index+1}</td>
                                                <td class='center'>${role.roleName}</td>
                                                <td class='center'>
                                                    <c:if test="${role.roleLevel=='0'}">总管理员</c:if>
                                                    <c:if test="${role.roleLevel=='1'}">管理员</c:if>
                                                    <c:if test="${role.roleLevel=='2'}">高级用户组</c:if>
                                                    <c:if test="${role.roleLevel=='3'}">普通组</c:if>
                                                </td>
                                                <td class='center'>${(role.menuRights==null||role.menuRights=='')?'未授权':'已授权'}</td>
                                                <td class='center'>${role.description}</td>
                                                <td class="center">
                                                    <div class="hidden-sm hidden-xs action-buttons">
                                                        <c:if test="${role.roleLevel > pd.RoleLevel }">
                                                            <a class="blue"
                                                               href="javascript:editRoleRight('${role.roleId}','${role.roleLevel}');">
                                                                <i class="ace-icon fa fa-key bigger-130" title="授权"></i>
                                                            </a>
                                                            <a class="green"
                                                               href="javascript:editRole('${role.roleId}');">
                                                                <i class="ace-icon fa fa-pencil-square-o bigger-130"
                                                                   title="修改"></i>
                                                            </a>
                                                            <a class="red"
                                                               href="javascript:delRole('${role.roleId }','${role.roleName }');">
                                                                <i class="ace-icon fa fa-trash-o bigger-130"
                                                                   title="删除"></i>
                                                            </a>
                                                        </c:if>
                                                    </div>
                                                    <div class="hidden-md hidden-lg">
                                                        <div class="inline pos-rel">
                                                            <button class="btn btn-minier btn-primary dropdown-toggle"
                                                                    data-toggle="dropdown" data-position="auto">
                                                                <i class="ace-icon fa fa-cog icon-only bigger-110"></i>
                                                            </button>
                                                            <ul class="dropdown-menu dropdown-only-icon dropdown-yellow dropdown-menu-right dropdown-caret dropdown-close">
                                                                <li>
                                                                    <a style="cursor:pointer;"
                                                                       onclick="editRoleRight('${role.roleId}','${role.roleLevel}');"
                                                                       class="tooltip-success" data-rel="tooltip"
                                                                       title="修改">
																	<span class="blue">
																		<i class="ace-icon fa fa-key bigger-120"></i>
																	</span>
                                                                    </a>
                                                                </li>
                                                                <li>
                                                                    <a style="cursor:pointer;"
                                                                       onclick="editRole('${role.roleId}');"
                                                                       class="tooltip-success" data-rel="tooltip"
                                                                       title="修改">
																	<span class="green">
																		<i class="ace-icon fa fa-pencil-square-o bigger-120"></i>
																	</span>
                                                                    </a>
                                                                </li>
                                                                <c:if test="${role.roleId != 1}">
                                                                    <li>
                                                                        <a style="cursor:pointer;"
                                                                           onclick="delRole('${role.roleId }','${role.roleName }');"
                                                                           class="tooltip-error" data-rel="tooltip"
                                                                           title="删除">
																		<span class="red">
																			<i class="ace-icon fa fa-trash-o bigger-120"></i>
																		</span>
                                                                        </a>
                                                                    </li>
                                                                </c:if>
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
        //$("#Form").submit();
        if (document.getElementById("namekey").value != "") {
            var namekey = document.getElementById("namekey").value;
        } else {
            var namekey = "";
        }
        window.location.href = "<%=basePath%>users/listRole.do?namekey=" + namekey;
    }

    //下拉框
    // 	复选框全选控制
    // 	var active_class = 'active';
    // 	$('#simple-table > thead > tr > th input[type=checkbox]').eq(0).on('click', function(){
    // 		var th_checked = this.checked;//checkbox inside "TH" table header
    // 		$(this).closest('table').find('tbody > tr').each(function(){
    // 			var row = this;
    // 			if($(row).attr("disabled")!="disabled"){
    // 				if(th_checked) $(row).addClass(active_class).find('input[type=checkbox]').eq(0).prop('checked', true);
    // 				else $(row).removeClass(active_class).find('input[type=checkbox]').eq(0).prop('checked', false);
    // 			}
    // 		});
    // 	});
    //删除
    function delRole(rid, msg) {
        layer.confirm("确定要删除[" + msg + "]吗?删除此角色后，属于此角色的所有用户都将被删除！", {
            btn: ['确定', '取消'] //按钮
            , anim: 6
            , icon: 8
        }, function (index) {
            layer.load();
            var url = "<%=basePath%>users/deleteRole.do?rid=" + rid;
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

    //授权
    function editRoleRight(RoleId, rlevel) {
        var role = ${pd.RoleLevel};
        if (rlevel > role) {
            layer.open({
                type: 2,
                title: '选择授权目录',
                maxmin: true,
                btn: ['确定', '取消'],
                btnAlign: 'c',
                area: ['300px', '480px'],
                anim: 1,
                content: "users/editRoleRight.do?rid=" + RoleId,
                yes: function (index, layero) {
                    var body = layer.getChildFrame('body', index);
                    var iframeWin = window[layero.find('iframe')[0]['name']];//得到iframe页的窗口对象，执行iframe页的方法：
                    var ids = iframeWin.save();//调用子页面的方法，得到子页面返回的ids
                    console.log(ids);
                    $.post("<%=basePath%>users/accredit.do", {RoleId: RoleId, menuIds: ids}, function (data) {
                        if (data.result == "success") {
                            layer.msg('授权成功！');
                            return true;
                        } else {
                            layer.msg('授权失败！请刷新页面重试！');
                            return false;
                        }
                    });
                    //按钮【按钮一】的回调
                    layer.close(index);
                }
            });
        }
    }

    //修改
    function editRole(RoleId) {
        var index = layer.open({
            type: 2,
            title: '添加角色',
            maxmin: true,
            area: ['840px', '500px'],
            anim: 2,
            content: 'users/editRole.do?RoleId=' + RoleId,
            end: function () {

            }
        });
        <%-- var diag = new top.Dialog();
        diag.Drag=true;
        diag.Title ="修改";
        diag.URL = "<%=basePath%>users/editRole.do?RoleId="+RoleId+"&RoleName="+RoleName;
        diag.Width = 400;
        diag.Height = 300;
        diag.CancelEvent = function(){ //关闭事件
        if(diag.innerFrame.contentWindow.document.getElementById('suopu').style.display == 'none'){
               nextPage(${page.currentPage});
           }
           diag.close();
        };
        diag.show(); --%>
    }

    //添加
    function addRole() {
        var index = layer.open({
            type: 2,
            title: '添加角色',
            maxmin: true,
            area: ['840px', '500px'],
            anim: 2,
            content: 'users/addRole.do',
            end: function () {

            }
        });
        //layer.full(index);
    }
</script>
</body>
</html>