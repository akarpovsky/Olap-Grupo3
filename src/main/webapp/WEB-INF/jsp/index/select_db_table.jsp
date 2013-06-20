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
		<h1>Elija la tabla para ${currentTable}</h1>
		<span class="" >Conectado a <span class="badge badge-warning">${dburl}</span></span><br />

		<form:form name="tableSelectionForm" class="form-horizontal span8"
					action="selectTable" method="POST"
					commandName="tableSelectionForm" style="margin-top: 30px">
			
			<select name="table">
			    <c:forEach items="${dbtables}" var="tableName">
		            <option value="${tableName.name}">${tableName.name}</option>
			    </c:forEach>
		    </select>
		    
		    <input name="currentTable" type="hidden" value="${currentTable}" />
		    
			<div class="form-actions">
				<input type="submit" class="btn btn-primary" value="Elegir" />
			</div>
		</form:form>
	    
	</div>
	<%@ include file="/WEB-INF/jsp/footer.jsp"%>
</body>
</html>