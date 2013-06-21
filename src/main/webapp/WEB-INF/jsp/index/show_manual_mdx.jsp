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
		<h1>XML MDX</h1>
		<a class="btn btn-inverse" href="<c:url value='/bin/index/getoutputxml'/>"><i class="icon-download-alt icon-white"></i> Descargar</a>
		<script type="syntaxhighlighter" class="brush: xml; toolbar: false;"><![CDATA[
			<c:out value="${MDXxml}"/>
		]]></script>
	</div>
</body>
	<%@ include file="/WEB-INF/jsp/footer.jsp"%>
	<!-- Finally, to actually run the highlighter, you need to include this JS on your page -->
	<script type="text/javascript">
	     SyntaxHighlighter.all()
	</script>
</html>