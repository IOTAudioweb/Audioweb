<div id="sidebar" class="sidebar                  responsive">
    <script type="text/javascript">
        try {
            ace.settings.check('sidebar', 'fixed')
        } catch (e) {
        }
    </script>

    <!-- 				<div class="sidebar-shortcuts" id="sidebar-shortcuts"> -->
    <!-- 					<div class="sidebar-shortcuts-large" id="sidebar-shortcuts-large"> -->

    <!-- 						<button class="btn btn-info" onclick="changeMenus();" title="切换菜单"> -->
    <!-- 							<i class="ace-icon fa fa-pencil"></i> -->
    <!-- 						</button> -->

    <!-- #section:basics/sidebar.layout.shortcuts -->
    <!-- 						<button class="btn btn-warning" title="" id="adminzidian"> -->
    <!-- 							<i class="ace-icon fa fa-book"></i> -->
    <!-- 						</button> -->

    <!-- 						<button class="btn btn-danger"> -->
    <!-- 							<i class="ace-icon fa fa-cogs"></i> -->
    <!-- 						</button> -->

    <!-- /section:basics/sidebar.layout.shortcuts -->
    <!-- 					</div> -->

    <!-- 					<div class="sidebar-shortcuts-mini" id="sidebar-shortcuts-mini"> -->
    <!-- 						<span class="btn btn-success"></span> -->

    <!-- 						<span class="btn btn-info"></span> -->

    <!-- 						<span class="btn btn-warning"></span> -->

    <!-- 						<span class="btn btn-danger"></span> -->
    <!-- 					</div> -->
    <!-- 				</div>/.sidebar-shortcuts -->

    <ul class="nav nav-list">
        <li class="">
            <a href="main/index">
                <i class="menu-icon fa fa-tachometer"></i>
                <span class="menu-text">首页</span>
            </a>
            <b class="arrow"></b>
        </li>


        <c:forEach items="${menuList}" var="menu1">
            <c:if test="${menu1.hasMenu }">
                <li class="" id="lm${menu1.mid }">
                    <a style="cursor:pointer;" class="dropdown-toggle">
                        <i class="${menu1.micon == null ? 'menu-icon fa fa-leaf black' : menu1.micon}"></i>
                        <span class="menu-text">
                                ${menu1.mname }
                        </span>
                        <c:if test="${'[]' != menu1.subMenu}"><b class="arrow fa fa-angle-down"></b></c:if>
                    </a>
                    <b class="arrow"></b>
                    <c:if test="${'[]' != menu1.subMenu}">
                        <ul class="submenu">
                            <c:forEach items="${menu1.subMenu}" var="menu2">
                                <c:if test="${menu2.hasMenu }">
                                    <li class="" id="z${menu2.mid }">
                                        <a style="cursor:pointer;"
                                           <c:if test="${not empty menu2.murl && '#' != menu2.murl}">target="mainFrame"
                                           onclick="siMenu('z${menu2.mid }','lm${menu1.mid }','${menu2.mname }','${menu2.murl }','${menu2.refresh}')"</c:if>
                                            <c:if test="${'[]' != menu2.subMenu}"> class="dropdown-toggle"</c:if>>
                                            <i class="${menu2.micon == null ? 'menu-icon fa fa-leaf black' : menu2.micon}"></i>
                                                ${menu2.mname }
                                            <c:if test="${'[]' != menu2.subMenu}"><b
                                                    class="arrow fa fa-angle-down"></b></c:if>
                                        </a>
                                        <b class="arrow"></b>
                                        <c:if test="${'[]' != menu2.subMenu}">
                                            <ul class="submenu">
                                                <c:forEach items="${menu2.subMenu}" var="menu3">
                                                    <c:if test="${menu3.hasMenu}">
                                                        <li class="" id="m${menu3.mid }">
                                                            <a style="cursor:pointer;"
                                                               <c:if test="${not empty menu3.murl && '#' != menu3.murl}">target="mainFrame"
                                                               onclick="siMenu('m${menu3.mid }','z${menu2.mid }','${menu3.mname }','${menu3.murl }','${menu3.refresh}')"</c:if>
                                                                <c:if test="${'[]' != menu3.subMenu}">
                                                               class="dropdown-toggle"</c:if>>
                                                                <i class="${menu3.micon == null ? 'menu-icon fa fa-leaf black' : menu3.micon}"></i>
                                                                    ${menu3.mname }
                                                                <c:if test="${'[]' != menu3.subMenu}"><b
                                                                        class="arrow fa fa-angle-down"></b></c:if>
                                                            </a>
                                                            <b class="arrow"></b>

                                                            <c:if test="${'[]' != menu3.subMenu}">
                                                                <ul class="submenu">
                                                                    <c:forEach items="${menu3.subMenu}" var="menu4">
                                                                        <c:if test="${menu4.hasMenu}">
                                                                            <li class="" id="n${menu4.mid }">
                                                                                <c:if test="${'[]' != menu4.subMenu}">
                                                                                <a style="cursor:pointer;"
                                                                                   target="mainFrame" target="mainFrame"
                                                                                   onclick="siMenu('n${menu4.mid }','m${menu3.mid }','${menu4.mname }','menu/otherlistMenu.do?mid=${menu4.mid }')">
                                                                                    </c:if>
                                                                                    <c:if test="${'[]' == menu4.subMenu}">
                                                                                    <a style="cursor:pointer;"
                                                                                       target="mainFrame"
                                                                                       <c:if test="${not empty menu4.murl && '#' != menu4.murl}">target="mainFrame"
                                                                                       onclick="siMenu('n${menu4.mid }','m${menu3.mid }','${menu4.mname }','${menu4.murl }','${menu4.refresh }')"</c:if>>
                                                                                        </c:if>
                                                                                        <i class="${menu4.micon == null ? 'menu-icon fa fa-leaf black' : menu4.micon}"></i>
                                                                                            ${menu4.mname }
                                                                                    </a>
                                                                                    <b class="arrow"></b>
                                                                            </li>
                                                                        </c:if>
                                                                    </c:forEach>
                                                                </ul>
                                                            </c:if>
                                                        </li>
                                                    </c:if>
                                                </c:forEach>
                                            </ul>
                                        </c:if>
                                    </li>
                                </c:if>
                            </c:forEach>
                        </ul>
                    </c:if>
                </li>
            </c:if>
        </c:forEach>

    </ul><!-- /.nav-list -->

    <!-- #section:basics/sidebar.layout.minimize -->
    <div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
        <i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left"
           data-icon2="ace-icon fa fa-angle-double-right"></i>
    </div>

    <!-- /section:basics/sidebar.layout.minimize -->
    <script type="text/javascript">
        try {
            ace.settings.check('sidebar', 'collapsed')
        } catch (e) {
        }
    </script>
</div>