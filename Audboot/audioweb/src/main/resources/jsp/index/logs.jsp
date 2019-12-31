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
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <base href="<%=basePath%>">
    <!-- jsp文件头和头部 -->
    <%@ include file="../index/top.jsp" %>
    <meta charset="utf-8"/>
    <title>访问与操作记录</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <link rel="stylesheet" href="static/layuiadmin/layui/css/layui.css" media="all">
    <link rel="stylesheet" href="static/layuiadmin/style/admin.css" media="all">
    <meta name="description" content=""/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0"/>
    <meta http-equiv="refresh" content="60">
</head>
<body>


<div class="layui-fluid">
    <div class="layui-row ">
        <form action="logs.do" method="post" name="Form" id="Form">
            <div class="layui-form-item">
                <div class="layui-inline">
                    <div class="layui-input-inline">
                        <input type="text" class="demo-input" name="StartDateTime" value="${pd.StartDateTime}"
                               placeholder="起始日期" id="StartDateTime">
                    </div>
                </div>
                <div class="layui-inline">
                    <div class="layui-input-inline">
                        <input type="text" class="demo-input" name="EndDateTime" value="${pd.EndDateTime}"
                               placeholder="终止日期" id="EndDateTime">
                    </div>
                </div>
                <div class="layui-inline">
                    <button class="layui-btn layui-btn-primary" title="检索">
                        <i class="layui-icon">&#xe615;</i>
                    </button>
                </div>
            </div>
            <table id="simple-table" class="layui-table" style="margin-top:5px;">
                <thead>
                <tr>
                    <th class="center">记录编号</th>
                    <th class="center">操作用户</th>
                    <th class="center">日志类型</th>
                    <th class="center">日志功能</th>
                    <th class="center">操作内容</th>
                    <th class="center">用户IP</th>
                    <th class="center">备注</th>
                    <th class='center'>记录时间</th>
                </tr>
                </thead>

                <tbody>
                <c:choose>
                    <c:when test="${not empty logs}">
                        <c:forEach items="${logs}" var="tor" varStatus="vs">
                            <tr>
                                <td class='center' style="width: auto;">${tor.lid}</td>
                                <td class='center' style="width: auto;"><c:if
                                        test="${tor.userid eq '0' }">系统</c:if><c:if
                                        test="${tor.userid != '0' }">${tor.username}</c:if></td>
                                <td class='center' style="width: auto;">${tor.logtype}</td>
                                <td class='center' style="width: auto;">${tor.function}</td>
                                <td class='center' style="width: auto;"><c:if
                                        test="${tor.logcontent != null && tor.logcontent != ''}">${tor.logcontent}</c:if><c:if
                                        test="${tor.logcontent == null || tor.logcontent == ''}">无</c:if></td>
                                <td class='center' style="width: auto;">${tor.ip}</td>
                                <td class='center' style="width: auto;">${tor.remark}</td>
                                <td class='center' style="width: auto;"><fmt:formatDate value="${tor.logtime}"
                                                                                        pattern="yyyy-MM-dd HH:mm:ss"/></td>

                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="100" class='center'>没有相关数据</td>
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
</div>
<script type="text/javascript" src="static/js/jquery.min.js"></script>
<!-- basic scripts -->
<!-- 页面底部js¨ -->
<%@ include file="../index/foot.jsp" %>
<script src="static/layuiadmin/layui/layui.js?t=1"></script>
<script>
    $(document).ready(function () {
        top.hangge();
    });
    layui.config({
        base: 'static/layuiadmin/' //静态资源所在路径
    }).extend({
        index: 'lib/index' //主入口模块
    }).use(['index', 'console']);
    layui.use(['form', 'layedit', 'laydate'], function () {
        var form = layui.form
            , layer = layui.layer
            , layedit = layui.layedit
            , laydate = layui.laydate;
        //执行一个laydate实例
        //开始日期
        var insStart = laydate.render({
            elem: '#StartDateTime'
            , min: -65535
            , done: function (value, date) {
                //更新结束日期的最小日期
                insEnd.config.min = lay.extend({}, date, {
                    month: date.month - 1
                });

                //自动弹出结束日期的选择器
                insEnd.config.elem[0].focus();
            }
        });

        //结束日期
        var insEnd = laydate.render({
            elem: '#EndDateTime'
            , min: -65535
            , done: function (value, date) {
                //更新开始日期的最大日期
                insStart.config.max = lay.extend({}, date, {
                    month: date.month - 1
                });
            }
        });
    });
    <%-- //检索
    function tosearch(){
         if (document.getElementById("namekey").value != "")  {
             var namekey = document.getElementById("namekey").value;
         }else{
             var namekey = "";
         }
        top.jzts();
        window.location.href="<%=basePath%>timedcast/listScheTasks.do?namekey="+namekey+"&ScheId="+ScheId;
    } --%>
</script>
</body>
</html>