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
                        <form name="Form" id="Form" method="post">
                            <input type="hidden" name="RoleId" id="RoleId" value="${RoleId}"/>
                            <div id="suopu" style="padding-top: 13px;">
                                <table id="table_report" class="table table-striped table-bordered table-hover">
                                    <tr>
                                        <td style="width:90px;text-align: right;padding-top: 13px;">角色名称:</td>
                                        <td><input type="text" value="${RoleName}" id="RoleName" name="RoleName"
                                                   title="角色名称" style="width:98%;"/></td>
                                    </tr>
                                    <tr>
                                        <td style="width:90px;text-align: right;padding-top: 13px;">角色类型:</td>
                                        <td>
                                            <select class="form-control" id="RoleLevel" name="RoleLevel"
                                                    data-placeholder="请选择角色类型" style="width:98%;">
                                                <c:if test="${RoleLevels < 3 }">
                                                    <option value="3" <c:if test="${RoleLevel== 3}">selected</c:if>>
                                                        普通组
                                                    </option>
                                                </c:if>
                                                <c:if test="${RoleLevels < 2 }">
                                                    <option value="2" <c:if test="${RoleLevel== 2 }">selected</c:if>>
                                                        高级用户组
                                                    </option>
                                                </c:if>
                                                <c:if test="${RoleLevels < 1 }">
                                                    <option value="1" <c:if test="${RoleLevel== 1 }">selected</c:if>>
                                                        管理员组
                                                    </option>
                                                </c:if>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="width:90px;text-align: right;padding-top: 13px;">备注:</td>
                                        <td><textarea name="Description" id="Description" wrap="soft" title="备注"
                                                      style="width:98%;">${pd.Description }</textarea></td>
                                    </tr>
                                    <tr>
                                        <td style="text-align: center;" colspan="10">
                                            <a class="btn btn-sm btn-primary" onclick="save();">保存</a>
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
        if ($("#RoleName").val() == null || $("#RoleName").val() == "") {
            $("#RoleName").tips({
                side: 3,
                msg: '请输入角色名称',
                bg: '#AE81FF',
                time: 3
            });
            $("#RoleName").focus();
            return false;
        } else {

            var url = "users/${MSG}.do";
            $.post(url, $("#Form").serialize(), function (data) {
                if ("success" == data.result) {
                    $("#suopu").hide();
                    $("#suopu2").show();
                    top.Dialog.close();
                }
            });
        }
    }
</script>
</html>