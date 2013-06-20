<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<html>
<%@ include file="/WEB-INF/jsp/head.jsp"%>
<style>
.caption {
    float:left;
    width: 270px;
}
.span8{
    width:350px;
}
.thumb_image {
    width:187x;
    height:187px;
    float: left;
}
.clear {
    clear:both;
}
</style>
<body>

	<%@ include file="/WEB-INF/jsp/header.jsp"%>
	<div class="container well">
		<h1>Tablas MDX</h1>
		<span class="">Conectado a <span class="badge badge-warning">${dburl}</span></span><br />
		${MDXtables}
		<h1>XML MDX</h1>
		<xmp>
			${MDXxml}
		</xmp>
	</div>
	<%@ include file="/WEB-INF/jsp/footer.jsp"%>
</body>
</html>