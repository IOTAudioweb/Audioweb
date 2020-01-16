<%--
  Created by IntelliJ IDEA.
  User: xuan
  Date: 2020/1/3
  Time: 17:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
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
    <%@ include file="../index/top.jsp"%>
    <meta charset="utf-8" />
    <title>访问与操作记录</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <link rel="stylesheet" href="static/layuiadmin/layui/css/layui.css" media="all">
    <link rel="stylesheet" href="static/layuiadmin/style/admin.css" media="all">
    <meta name="description" content="" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />
</head>
<body>


<div class="layui-fluid">
    <div class="layui-row layui-col-space15">
        <form action="sessions.do" method="post" name="Form" id="Form">
            <table id="simple-table"  class="layui-table" style="margin-top:5px;">
                <thead>
                <tr>
                    <th class="center" >终端</th>
                    <th class="center" >时间</th>
                    <th class="center" >用户ID</th>

                </tr>
                </thead>

                <tbody>
                <c:choose>
                    <c:when test="${not empty beans}">
                        <c:forEach items="${beans}" var="tor" varStatus="vs">
                            <tr>
                                <td class='center' style="width: auto;">${tor.cpuRatio}%</td>
                                <td class='center' style="width: auto;">${tor.totalThread}</td>
                                <td class='center' style="width: auto;">
                                        ${tor.usedMemory}MB
                                </td>
                                <td class='center' style="width: auto;">${tor.freePhysicalMemorySize}MB</td>
                                <td class='center' style="width: auto;">${tor.freeMemory}MB</td>
                                <td class='center' style="width: auto;"><fmt:formatDate value="${tor.searchDate}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
                                <td class='center' style="width: auto;">${tor.osName}</td>
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
        </form>
    </div>
</div>
<script type="text/javascript" src="static/js/jquery.min.js"></script>
<!-- basic scripts -->
<!-- 页面底部js¨ -->
<%@ include file="../index/foot.jsp"%>
<script src="static/layuiadmin/layui/layui.js?t=1"></script>
<script>
    layui.config({
        base: 'static/layuiadmin/' //静态资源所在路径
    }).extend({
        index: 'lib/index' //主入口模块
    }).use(['index', 'console']);
    layui.use(['form', 'layedit', 'laydate'], function(){
        var form = layui.form
            ,layer = layui.layer
            ,layedit = layui.layedit
            ,laydate = layui.laydate;
    });
</script>
</body>

