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
    <title>主页</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <link rel="stylesheet" href="static/layuiadmin/layui/css/layui.css" media="all">
    <link rel="stylesheet" href="static/layuiadmin/style/admin.css" media="all">
    <meta name="description" content=""/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0"/>
    <meta http-equiv="refresh" content="300">
</head>
<body>


<div class="layui-fluid">
    <div class="layui-row layui-col-space15">
        <div class="layui-col-md12">
            <div class="layui-row layui-col-space15">
                <div class="layui-col-md6">
                    <div class="layui-card">
                        <div class="layui-card-header">管理页面</div>
                        <div class="layui-card-body">

                            <div class="layui-carousel layadmin-carousel layadmin-shortcut">
                                <div carousel-item>
                                    <ul class="layui-row layui-col-space10">
                                        <li class="layui-col-xs3">
                                            <a href="javascript:readSysInfo();">
                                                <i class="layui-icon layui-icon-console"></i>
                                                <cite>系统状态</cite>
                                            </a>
                                        </li>
                                        <c:if test="${jurisToLog}">
                                            <li class="layui-col-xs3">
                                                <a href="javascript:readLog();">
                                                    <i class="layui-icon layui-icon-user"></i>
                                                    <cite>操作记录</cite>
                                                </a>
                                            </li>
                                        </c:if>
                                        <%--  <li class="layui-col-xs3">
                                            <a href="javascript:readSessions();">
                                             <i class="layui-icon layui-icon-chat"></i>
                                             <cite>终端通信记录</cite>
                                           </a>
                                         </li>
                                         <c:if test="${!isconnect}">
                                          <li class="layui-col-xs3">
                                           <a href="javascript:Open();">
                                             <i class="layui-icon layui-icon-find-fill"></i>
                                             <cite>连接终端</cite>
                                           </a>
                                         </li>
                                         </c:if>
                                         <c:if test="${jurisToLog}">
                                          <li class="layui-col-xs3">
                                           <a href="javascript:Close();">
                                             <i class="layui-icon layui-icon-find-fill"></i>
                                             <cite>断开连接</cite>
                                           </a>
                                         </li>
                                          </c:if> --%>
                                    </ul>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
                <div class="layui-col-md6">
                    <div class="layui-card">
                        <div class="layui-card-header">登录状态</div>
                        <div class="layui-card-body">

                            <div class="layui-carousel layadmin-carousel layadmin-backlog">
                                <div carousel-item>
                                    <ul class="layui-row layui-col-space10">
                                        <li class="layui-col-xs4">
                                            <a lay-href="app/content/comment.html" class="layadmin-backlog-body">
                                                <h3>终端登录数</h3>
                                                <p><cite>${pass}</cite></p>
                                            </a>
                                        </li>
                                        <li class="layui-col-xs4">
                                            <a lay-href="app/forum/list.html" class="layadmin-backlog-body">
                                                <h3>离线终端数</h3>
                                                <p><cite>${nopass}</cite></p>
                                            </a>
                                        </li>
                                        <li class="layui-col-xs4">
                                            <a lay-href="app/forum/list.html" class="layadmin-backlog-body">
                                                <h3>在线用户</h3>
                                                <p><cite>${online}</cite></p>
                                            </a>
                                        </li>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="layui-col-md12">
                    <div class="layui-card">
                        <div class="layui-card-header">即将执行的定时任务</div>
                        <div class="layui-card-body">
                            <form action="login_default.do" method="post" name="Form" id="Form">
                                <input type="hidden" id="ScheId" name="ScheId" value="${pd.ScheId}"/>
                                <table id="simple-table" class="layui-table" style="margin-top:5px;">
                                    <thead>
                                    <tr>
                                        <th class="center">任务编号</th>
                                        <th class="center">任务名称</th>
                                        <th class="center">运行状态</th>
                                        <th class='center'>下次运行时间</th>
                                        <th class='center'>选中音频</th>
                                        <th class='center'>广播分组</th>
                                        <th class='center'>所属方案</th>
                                    </tr>
                                    </thead>

                                    <tbody>
                                    <c:choose>
                                        <c:when test="${not empty tasks}">
                                            <c:forEach items="${tasks}" var="task" varStatus="vs">
                                                <tr ondblclick="javascript:readtask('${task.taskId }','${pd.ScheId}','${pd.ScheName}');">
                                                        <%-- <td class='center' style="width: 30px;">
                                                                <c:if test="${task.taskId != pd.currenTid}"><label><input type='checkbox' name='ids' value="${task.taskName}" id="${task.taskId }"  class="ace"/><span class="lbl"></span></label></c:if>
                                                                <c:if test="${task.taskId  == pd.currentTid}"><label><input type='checkbox' disabled="disabled" class="ace" /><span class="lbl"></span></label></c:if>
                                                            </td> --%>

                                                    <td class='center' style="width: auto;">${task.taskId}</td>
                                                    <td class='center' style="width: auto;">${task.taskName}</td>
                                                    <td align='center' style="width: auto;">
                                                        <c:if test="${task.jobStatus == '' ||task.jobStatus == null}"><i
                                                                style="color:red;">未成功创建，请删除新建</i></c:if>
                                                        <c:if test="${task.jobStatus == 'NONE'}">未知</c:if>
                                                        <c:if test="${task.jobStatus == 'NORMAL'}">准备就绪</c:if>
                                                        <c:if test="${task.jobStatus == 'PAUSED'}">暂停状态</c:if>
                                                        <c:if test="${task.jobStatus == 'COMPLETE'}"><i
                                                                style="color:green;">完成状态</i></c:if>
                                                        <c:if test="${task.jobStatus == 'ERROR'}"><i style="color:red;">错误状态</i></c:if>
                                                        <c:if test="${task.jobStatus == 'BLOCKED'}"><i
                                                                style="color:yellow;">锁定状态</i></c:if>
                                                        <c:if test="${task.jobStatus == 'RUNNING'}"><i
                                                                style="color:green;">正在运行</i></c:if>
                                                    </td>
                                                    <td align='center' style="width: auto;">
                                                        <c:if test="${task.jobStatus == 'NORMAL'}"><fmt:formatDate
                                                                value="${task.nextFireTime}"
                                                                pattern="yyyy-MM-dd HH:mm:ss"/></c:if>
                                                        <c:if test="${task.jobStatus == 'PAUSED'}">任务暂停中</c:if>
                                                    </td>
                                                    <td class='center' style="width: auto;">${  task.filesInfo }</td>
                                                    <td class='center' style="width: auto;"><a type="button"
                                                                                               href="javascript:lookdomaininfo('${task.domainsId}','${task.taskName}');"
                                                                                               title="查看广播分组"
                                                                                               style='text-decoration:none;'><i
                                                            class="layui-icon" style="color:blue;">&#xe60b;</i>查看</a>
                                                    </td>
                                                    <td class='center' style="width: auto;">${  task.scheName }</td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="100" class='center'>没有相关数据,请选择方案或者刷新页面</td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                    </tbody>
                                </table>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>
<script type="text/javascript" src="static/js/jquery.min.js"></script>
<!-- basic scripts -->
<!-- 页面底部js¨ -->
<%@ include file="../index/foot.jsp" %>
<script src="static/layuiadmin/layui/layui.js?t=1"></script>
<script>
    $(top.hangge());
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
    });

    //双击查看
    function readtask(TaskId, ScheId, ScheName) {
        <%-- top.jzts();
        window.location.href="<%=basePath%>timedcast/toRead.do?TaskId="+TaskId+"&ScheId="+ScheId+"&ScheName="+ScheName; --%>
        layer.open({
            type: 2,
            title: '查看任务',
            maxmin: true,
            btn: ['确定'],
            btnAlign: 'c',
            moveOut: true,
            area: ['840px', '500px'],
            anim: 2,
            content: "timedcast/toRead.do?TaskId=" + TaskId + "&ScheId=" + ScheId + "&ScheName=" + ScheName,
            yes: function (index, layero) {
                //按钮【按钮一】的回调
                layer.close(index);
            },
            success: function (layero, index) {
                //获取当前弹出窗口的索引及初始大小
                layerIndex = index;
                layerInitWidth = $("#layui-layer" + layerIndex).width();
                layerInitHeight = $("#layui-layer" + layerIndex).height();
                resizeLayer(layerIndex, layerInitWidth, layerInitHeight);
            }
        });

    };

    function lookdomaininfo(domainsId, taskName) {
        layer.open({
            type: 2,
            title: '查看"' + taskName + '"广播分组',
            maxmin: true,
            btn: ['确定'],
            btnAlign: 'c',
            moveOut: true,
            area: ['300px', '480px'],
            anim: 1,
            content: "timedcast/finddomids.do?domidInfo=" + domainsId + "&statu=" + "查看",
            yes: function (index, layero) {
                //按钮【按钮一】的回调
                layer.close(index);
            },
            success: function (layero, index) {
                //获取当前弹出窗口的索引及初始大小
                layerIndex = index;
                layerInitWidth = $("#layui-layer" + layerIndex).width();
                layerInitHeight = $("#layui-layer" + layerIndex).height();
                resizeLayer(layerIndex, layerInitWidth, layerInitHeight);
            }
        });
    }

    function resizeLayer(layerIndex, layerInitWidth, layerInitHeight) {
        var docWidth = $(document).width();
        var docHeight = $(document).height();
        var minWidth = layerInitWidth > docWidth ? docWidth : layerInitWidth;
        var minHeight = layerInitHeight > docHeight ? docHeight : layerInitHeight;
        console.log("doc:", docWidth, docHeight);
        console.log("lay:", layerInitWidth, layerInitHeight);
        console.log("min:", minWidth, minHeight);
        if (docWidth < 900 || docHeight < 720)
            layer.style(layerIndex, {
                top: 0,
                width: minWidth,
                height: minHeight - 20
            });
    }

    function readSysInfo() {
        var index = layer.open({
            title: '系统信息',
            type: 2,
            content: 'sysInfo.do',
            anim: 2,
            area: ['1060px', '520px'],
            maxmin: true
        });
        //layer.full(index);
    };

    function readLog() {
        var index = layer.open({
            title: '系统日志',
            type: 2,
            content: 'logs.do',
            anim: 2,
            area: ['1060px', '520px'],
            maxmin: true
        });
        //layer.full(index);
    };
    /* function readTorrent(){
          var index = layer.open({
            title: '数据采集记录',
            type: 2,
            content: 'show/listtorrent.do?',
            area: ['1200px', '600px'],
            maxmin: true
          });
          layer.full(index);
      };
       function readSessions(){
              var index = layer.open({
                title: '终端会话记录',
                type: 2,
                content: 'show/sessions.do?',
                area: ['1200px', '600px'],
                maxmin: true
              });
              layer.full(index);
          };
      function readClient(){
          var index = layer.open({
            title: '终端状态',
            type: 2,
            content: 'show/listClient.do?',
            area: ['1200px', '600px'],
            maxmin: true
          });
          layer.full(index);
      };
  */
</script>
</body>
</html>