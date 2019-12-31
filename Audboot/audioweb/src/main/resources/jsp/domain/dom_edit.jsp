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
                            <c:if test="${MSG=='edit' }">编辑区域</c:if>
                            <c:if test="${MSG=='add' }">添加区域</c:if>
                        </small>
                    </h1>
                </div><!-- /.page-header -->

                <div class="row">
                    <div class="col-xs-12">

                        <form action="domain/${MSG }.do" name="domainForm" id="domainForm" method="post"
                              class="form-horizontal">
                            <input type="hidden" name="ParentDomainId" id="ParentDomainId"
                                   value="${null == pd.Domains.parentDomainId ? DomainId:pd.Domains.parentDomainId}"/>
                            <div class="form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-field-1"> 上级 :</label>
                                <div class="col-sm-9">
                                    <div style="padding-top:5px;">
                                        <div class="col-xs-4 label label-lg label-light arrowed-in arrowed-right">
                                            <b>${null == pd.pareDomain.domainName ?'(无) 此项为顶级区域':pd.pareDomain.domainName}</b>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <c:if test="${MSG=='edit' }">
                                <div class="form-group">
                                    <label class="col-sm-3 control-label no-padding-right" for="form-field-1"> 编号
                                        : </label>
                                    <div class="col-sm-9">
                                        <input type="number" name="DomainId" id="DomainId"
                                               value="${pd.Domains.domainId}" placeholder="这里输入分组编号" title="请输入正整数"
                                               readonly="readonly" class="col-xs-10 col-sm-5"/>
                                    </div>
                                </div>
                            </c:if>
                            <div class="form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-field-1"> 名称 :</label>
                                <div class="col-sm-9">
                                    <input type="text" name=DomainName id="DomainName" value="${pd.Domains.domainName }"
                                           placeholder="这里输入分组名称" class="col-xs-10 col-sm-5"/>
                                </div>
                            </div>
                            <div class="clearfix form-actions">
                                <div class="col-md-offset-3 col-md-9">
                                    <a class="btn btn-mini btn-primary" onclick="save();">保存</a>&nbsp;&nbsp;
                                    <a class="btn btn-mini btn-danger" onclick="goback('${DomainId}');">取消</a>
                                </div>
                            </div>
                            <div class="hr hr-18 dotted hr-double"></div>
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
    <a href="#" id="btn-scroll-up"
       class="btn-scroll-up btn btn-sm btn-inverse"> <i
            class="ace-icon fa fa-angle-double-up icon-only bigger-110"></i>
    </a>

</div>
<!-- /.main-container -->

<!-- basic scripts -->
<!-- 页面底部js¨ -->
<%@ include file="../index/foot.jsp" %>
<!--提示框-->
<script type="text/javascript" src="static/js/jquery.tips.js"></script>
<!-- inline scripts related to this page -->
<script type="text/javascript">
    $(top.hangge());
    console.info("${MSG}")

    //返回
    function goback(DomainId) {
        top.jzts();
        window.location.href = "<%=basePath%>domain/list.do?DomainId=" + DomainId;
    }

    //保存
    function save() {
        if ($("#DomainName").val() == "") {
            $("#DomainName").tips({
                side: 3,
                msg: '请输入区域名称',
                bg: '#AE81FF',
                time: 2
            });
            $("#DomainName").focus();
            return false;
        }

// 		if ("${MSG}" == "add") {
// 			$("#areaForm").submit();
// 				checkAndSubmit();
// 		} else {
// 				if ($("#aid").val() == null || $("#aid").val() == "") {
// 					$("#aid").tips({
// 						side : 3,
// 						msg : '区域编号不能为空',
// 						bg : '#AE81FF',
// 						time : 2
// 					});
// 					$("#aid").focus();
// 				} else 
// 				if ($("#aid").val() == "${pd.area.aid}") {
        $("#domainForm").submit();
// 				} else {
// 					checkAndSubmit();
// 				}
    }

    // 		}

    // 		function checkAndSubmit() {
    // 			$.ajax({
    // 				type : "POST",
    // 				url : 'area/checkAid',
    // 				data : {
    // 					aid : $("#aid").val()
    // 				},
    // 				dataType : 'json',
    // 				cache : false,
    // 				success : function(data) {
    // 					if (data.result == "success") {
    // 						$("#areaForm").submit();
    // 					} else {
    // 						$("#aid").tips({
    // 							side : 3,
    // 							msg : '该区域编号已存在',
    // 							bg : '#AE81FF',
    // 							time : 2
    // 						});
    // 						$("#aid").focus();
    // 					}
    // 				}
    // 			});
    // 		}
    // 	选择用户

</script>
</body>
</html>