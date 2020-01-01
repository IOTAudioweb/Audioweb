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
<!DOCTYPE html>
<html lang="en">
<head>
<base href="<%=basePath%>">
<!-- jsp文件头和头部 -->
<%@ include file="../index/top.jsp"%>
<!-- layui -->
 <link rel="stylesheet" href="static/layuiadmin/layui/css/layui.css">
 
	<script type="text/javascript" src="static/js/jquery.min.js"></script>
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
								<div class="page-header">
									<h1><small><i class="ace-icon fa fa-angle-double-right"></i>
										终端运行状态</small></h1>
								</div>
								<div class="col-xs-12">
							<div class="layui-form-item">
								 <div class="layui-inline">
									<span class="input-icon">
										<input class="nav-search-input" autocomplete="off" id="namekey" type="text" style="vertical-align:top;width: 160px; height: 38px" name="namekey" value="${namekey}" placeholder="输入终端名称或编号" />
										<i class="ace-icon fa fa-search nav-search-icon"></i>
									</span>
								</div>
								<div class="layui-inline"> 
									<button class="layui-btn layui-btn-primary" onclick="tosearch();"  title="检索">
										<i class="layui-icon">&#xe615;</i>  
										</button>
								</div>
								<div class="layui-inline">
									<button title="刷新" class="layui-btn  layui-btn-radius" onclick="history.go(0)" >刷新
									</button>
								</div>
								<c:if test="${MSG !='off' }">
									<div class="layui-inline">
										<button title="显示离线终端" class="layui-btn  layui-btn-radius layui-btn-normal" onclick="listAll('off')" >显示离线终端
										</button>
									</div>
								</c:if>
								<c:if test="${MSG !='on' }">
									<div class="layui-inline">
										<button title="显示在线终端" class="layui-btn  layui-btn-radius layui-btn-normal" onclick="listAll('on')" >显示在线终端
										</button>
									</div>
								</c:if>
								<c:if test="${MSG !='all' }">
									<div class="layui-inline">
										<button title="显示全部终端" class="layui-btn  layui-btn-radius layui-btn-normal" onclick="listAll('all')" >显示全部终端
										</button>
									</div>
								</c:if>
								<c:if test="${!refresh}">
									<div class="layui-inline">
										<button title="重置终端检测" class="layui-btn  layui-btn-radius layui-btn-danger" onclick="resume()" >重置终端检测
										</button>
									</div>
								</c:if>
								<c:if test="${MSG  =='on'}">
									<div class="layui-inline">
										<button title="设置音量" class="layui-btn  layui-btn-radius layui-btn-normal" onclick="setVol()" >设置音量
										</button>
									</div>
								</c:if>
								 <c:if test="${user  == '1'}">
									<div class="layui-inline">
										<button title="重启全部终端" class="layui-btn  layui-btn-radius layui-btn-normal layui-btn-danger" onclick="Reboot('请确认是否重启全部在线终端，这将会导致终端退出任务等情况',false)" >重启全部终端
										</button>
									</div>
								</c:if>
								<!-- <div class="layui-inline">
									<button type="button" class="layui-btn  demo-active layui-btn-danger" data-type="domainselect">选择终端分组</button> 
								</div> -->
							</div>	
							<form action="termstatus/listTermStatus.do?" method="post" name="Form" id="Form">
								<input type="hidden" id="MSG" name="MSG" value="${MSG}" />
								<input type="hidden" id="namekey" name="namekey" value="${namekey}" />
								<table id="simple-table"  class="layui-table" style="margin-top:5px;">
										<thead>
								  <tr>
								  <th class="center" style="width:30px;">
									<label class="pos-rel"><input type="checkbox" class="ace" id="zcheckbox" /><span class="lbl"></span></label>
									</th>
										<th class="center" >是否在线</th>
										<th class="center" >终端编号</th>
										<th class="center" >终端名称</th>
										<th class="center" >终端IP地址</th>
										<th class="center" >所属分区</th>
										<th class='center'>是否正在广播</th>
										<th class='center'>广播类型</th>
										<c:if test="${MSG  !='on'}"><th class='center'>最近通信时间</th></c:if> 
										<!-- <th class='center'>测试</th> -->
 	 									<c:if test="${MSG  =='on'}"><th class='center' >操作</th></c:if> 
									</tr>
								</thead>
								<tbody>
										<c:choose>
									<c:when test="${not empty terminfos}">
									<c:forEach items="${terminfos}" var="term" varStatus="vs">
									<tr>
									<td class='center' style="width: 30px;">
												<c:if test="${term.terid != pd.currenTid}"><label><input type='checkbox' name='ids' value="${term.tname}" id="${term.terid  }"  class="ace"/><span class="lbl"></span></label></c:if>
												<c:if test="${term.terid  == pd.currentTid}"><label><input type='checkbox' disabled="disabled" class="ace" /><span class="lbl"></span></label></c:if>
											</td>
									
											<td class='center'>${(term.isOnline||term.istrueOnline)?'<i class="layui-icon" style="color:green;">&#xe605;</i>':'<i class="layui-icon" style="color:red;">&#x1006;</i>' }</td>
											<td class='center'>${term.terid }</td>
											<td class='center'>${term.tname }</td>
											<td class='center'>${term.ipAddress.getHostAddress()}</td>
											<td class='center'>${term.domainName}</td>
											<th class='center'>${term.iscast?'<i class="layui-icon" style="color:green;">&#xe605;</i>':'<i class="layui-icon" style="color:red;">&#x1006;</i>' }</th>
											<th class='center'><c:if test="${term.iscast}">编号${term.castid}:${term.castType}</c:if><c:if test="${!term.iscast}">无</c:if></th>
											 <c:if test="${MSG  !='on'}"><td align='center' style="width: auto;">
												<c:if test="${term.lastUseTime.getTime() > time}"><fmt:formatDate value="${term.lastUseTime}" pattern="yyyy-MM-dd HH:mm:ss" /></c:if> 
												<c:if test="${term.lastUseTime.getTime() <= time}">无</c:if> 
											</td></c:if> 
											<%--  <td align='center' style="width: auto;">
												<c:if test="${term.orderCastInfo.size() > 1}">${term.orderCastInfo.get(1).multiCastType}</c:if> 
												<c:if test="${term.orderCastInfo.size() <= 1}">无</c:if> 
											</td> --%>
<!-- 											<div class="action-buttons"> -->
<%-- 												<a class="red" href="javascript:stopcast('${term.castTaskId}');"> --%>
<!-- 													<i class="ace-icon fa fa-stop bigger-130" title="停止"></i> -->
<!-- 												</a> -->
<!-- 											</div> -->
											<c:if test="${MSG  =='on'}">
											<td class='center' style="width: auto;">
											<div class="hidden-sm hidden-xs action-buttons">
												<a class="green" href="javascript:toPostVol('${term.terid}');">
													<i class="ace-icon fa fa-pencil-square-o bigger-130" title="修改音量"></i>
												</a>
											<a class="blue" href="javascript:setTer('${term.terid}');">
													<i class="ace-icon fa fa-refresh bigger-130" title="重启终端"></i>
												</a>
												 <a class="red" href="javascript:deltask('${term.terid}');">
													<i class="ace-icon fa fa-trash-o bigger-130" title="中断全部任务"></i>
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
												
														<li><a href="javascript:toPostVol('${term.terid}');" class="tooltip-success" data-rel="tooltip" title="Edit">
															<span class="green">
																<i class="ace-icon fa fa-pencil-square-o bigger-120" title="修改音量"></i>
															</span>
														</a></li>
												<li><a href="javascript:setTer('${term.terid}');" class="tooltip-success" data-rel="tooltip" title="Read">
															<span class="blue">
																<i class="ace-icon fa fa-refresh bigger-120" title="重启终端"></i>
															</span>
														</a></li>
														<li><a href="javascript:deltask('${term.terid}');" class="tooltip-success" data-rel="tooltip" title="Delete">
															<span class="red"> 
																<i class="ace-icon fa fa-trash-o bigger-120"  title="中断全部任务"></i>
															</span>
														</a></li>
													</ul>
												</div>
											</div>
										</td>
										</c:if> 
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
										<td style="vertical-align:top;"><div class="pagination" style="float: right;padding-top: 0px;margin-top: 0px;">${page.pageStr}</div></td>
									</tr>
							</table>
						</div>
					</form>
						</div>
						<!-- /.col -->
						</div>
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
	<%@ include file="../index/foot.jsp"%>
	<!-- ace scripts -->
	<script src="static/ace/js/ace/ace.js"></script>
	
<!--layui-->
	<script src="static/layuiadmin/layui/layui.js" charset="utf-8"></script>
	<script type="text/javascript">
	$(document).ready(function(){
		top.hangge();
		//复选框全选控制
		var active_class = 'active';
		$('#simple-table > thead > tr > th input[type=checkbox]').eq(0).on('click', function(){
			var th_checked = this.checked;//checkbox inside "TH" table header
			$(this).closest('table').find('tbody > tr').each(function(){
				var row = this;
				if($(row).attr("disabled")!="disabled"){
					if(th_checked) $(row).addClass(active_class).find('input[type=checkbox]').eq(0).prop('checked', true);
					else $(row).removeClass(active_class).find('input[type=checkbox]').eq(0).prop('checked', false);
				}
			});
		});
	});
	layui.use(['layer'], function(){
        var layer = layui.layer;
    });
		function resume(){
			var resume = true;
			var index = layer.load(0);
			<%-- $.post("<%=basePath%>termstatus/listTermStatus.do",{resume:resume},function(data){
				if(data.result=="success"){
					layer.msg('重置终端检测成功！');
					layer.close(index);
					return true;
				}else{
					layer.msg('重置失败！请刷新页面重试！');
					layer.close(index);
					return false;
				}
			}); --%>
			layer.msg('重置中！', {
            	icon :1
            	,time:1000
          	,end: function(){
          		window.location.href="<%=basePath%>termstatus/listTermStatus.do?resume="+resume;
          	}});
		}
		function listAll(msg){
			 if (document.getElementById("namekey").value != "")  {
				 var namekey = document.getElementById("namekey").value;
			 }else{
				 var namekey = ""; 
			 }
			layer.load();
			window.location.href="<%=basePath%>termstatus/listTermStatus.do?MSG="+msg+"&namekey="+namekey;
		}
		function refresh(msg){
			listAll(msg);
		}
		function tosearch(){
			var msg = document.getElementById("MSG").value;
			listAll(msg);
		}
		function setTer(terid){
			layer.confirm("确定要重启此终端吗?", {
				  btn: ['确定','取消'] //按钮
				  ,anim: 6 
				  ,icon: 8
				}, function() {
					$.post("<%=basePath%>termstatus/setTer.do",{Tid:terid},function(data){
						//console.log(data);
						if(data.result=="success"){
							layer.msg('执行成功！');
							//setTimeout("history.go(0)",2000); 
							return true;
						}else{
							layer.msg('执行失败！请刷新页面或确认任务信息！');
							return false;
						}
					});
				});
		}
		function deltask(terid){
			layer.confirm("确定要中断此终端全部任务吗?", {
				  btn: ['确定','取消'] //按钮
				  ,anim: 6 
				  ,icon: 8
				}, function() {
					$.post("<%=basePath%>termstatus/delTask.do",{Tid:terid},function(data){
						//console.log(data);
						if(data.result=="success"){
							layer.msg('执行成功！', {
				            	time:1000
				          	,end: function(){
				          		location.reload(true);
				          	}});
							//setTimeout("history.go(0)",2000); 
							return true;
						}else if(data.result=="nothing"){
							layer.msg('执行失败！无正在执行的任务');
							return false;
						}else{
							layer.msg('执行失败！请刷新页面或确认任务信息！');
							return false;
						}
					});
				});
		}
		//批量设置音量
		function setVol(){
			layer.confirm("确定修改选中终端的音量么", {
				btn: ['确定','取消'] //按钮
			  ,anim: 6 
			  ,icon: 3
			}, function(index) {
					var tidstr = '';
					for(var i=0;i < document.getElementsByName('ids').length;i++)
					{
						  if(document.getElementsByName('ids')[i].checked){
						  	if(tidstr=='') tidstr += document.getElementsByName('ids')[i].id;
						  	else tidstr += ',' + document.getElementsByName('ids')[i].id;
						  }
					}
					if(tidstr==''){
				        layer.msg('无法修改，未选择任何终端！',{
							title: '警告'
							,icon: 2
							,time:1000
							,end: function() {
								layer.tips('点这里全选', '#zcheckbox',{
							          tips: [1,'#FFB800'],
										time:1000
						        });
							}
						});
						return;
					}else{
						layer.close(index);
						toPostVol(tidstr);
					}
			});
		};
		function toPostVol(tidstr){
			layer.open({
				type: 2,
			    title: '音量修改',
			    maxmin: false,
			    btn: ['确定','取消'],
			    btnAlign: 'c',
			    anim:1,
			    area: ['460px', '240px'],
			    content: ["termstatus/toVol.do",'no'],
			    yes: function(index, layero){
			        //按钮【按钮一】的回调
			        var body = layer.getChildFrame('body', index);
						var iframeWin = window[layero.find('iframe')[0]['name']];//得到iframe页的窗口对象，执行iframe页的方法：
			        var datas = iframeWin.back();//调用子页面的方法，得到子页面返回的数据
			        $.post("<%=basePath%>termstatus/setVol.do",{vol:datas.vol,is:datas.is,tids:tidstr},function(data){
						if(data.result=="success"){
							layer.msg('执行成功！');
							//setTimeout("history.go(0)",2000); 
							return true;
						}else{
							layer.msg('执行失败！请刷新页面或确认任务信息！');
							return false;
						}
					});
			        layer.close(index);
			      }
			  });
		}
		<%-- function Reboot(msg,is){
			 layer.confirm(msg, {
				  btn: ['确定','取消'] //按钮
				  ,anim: 6 
				  ,icon: 8
				}, function() {
					if(is){
						layer.load();
						$.post("<%=basePath%>termstatus/Reboot.do",{},function(data){
							layer.closeAll();
	    					if(data.result=="success"){
	    						layer.msg('重启成功！');
	    						return true;
	    					}else{
	    						layer.msg('发送失败！请刷新页面重试！');
	    						return false;
	    					}
	    				});
					}else{
						layer.closeAll();
						msg = "请再确认一次是否重启全部在线终端？";
						is = true;
						Reboot(msg,is);
					}
				},function(){
					layer.tips('已取消', '#updata',{
				          tips: [2,'#3595CC'],
							time:1000
			        });
				});
		} --%>
	</script>
</body>
</html>