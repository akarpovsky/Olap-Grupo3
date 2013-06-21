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
		<form:form name="tableSelectionForm" class="form-horizontal span8"
					action="testing" method="POST"
					commandName="tableSelectionForm" style="margin-top: 30px">
<!-- 			<input path="tableSelectionValues[contamination_fact]" type="text"/> -->
<!-- 			<input path="tableSelectionValues[pepe]" type="text"/> -->
<%-- 		    <c:forEach items="${tableSelectionForm2.tableSelectionValuesMap}" var="entry"> --%>
<!-- 		        <tr> -->
<%-- 		            <td>${entry.key}</td> --%>
<%-- 		            <td><input path="tableSelectionValuesMap['${entry.key}']" value="${entry.value}"/></td> --%>
<!-- 		        </tr> -->
<%-- 		    </c:forEach> --%>
		    <input path="basura" />
			<div class="form-actions">
				<input type="submit" class="btn btn-primary" value="Elegir !!!!!!" />
			</div>
		</form:form>
		
		<h1>Elija las tablas para cada dimensi&oacute;n</h1>
		<span class="" >Conectado a <span class="badge badge-warning">${dburl}</span></span><br />

<%-- 		<form:form name="tableSelectionForm" class="form-horizontal span8" --%>
<%-- 					action="selectTable" method="POST" --%>
<%-- 					commandName="tableSelectionForm" style="margin-top: 30px"> --%>
<%-- 			<c:forEach var="entry" items="${tableSelectionMap}"> --%>
<!-- 				<div class="control-group"> -->
<%-- 		            <label class="control-label" for="input01">${entry.key}</label> --%>
<!-- 		            <div class="controls"> -->
<!-- 			             <select name="table"> -->
<%-- 						    <c:forEach items="${entry.value}" var="tableName"> --%>
<%-- 					            <option value="${tableName.name}">${tableName.name}</option> --%>
<%-- 						    </c:forEach> --%>
<!-- 			    		</select> -->
<!-- 		            </div> -->
<!-- 	        	</div> -->
<%-- 			</c:forEach> --%>
		    
<%-- 		    <input name="currentTable" type="hidden" value="${currentTable}" /> --%>
		    
<!-- 			<div class="form-actions"> -->
<!-- 				<input type="submit" class="btn btn-primary" value="Elegir" /> -->
<!-- 			</div> -->
<%-- 		</form:form> --%>
	    
	</div>
	<%@ include file="/WEB-INF/jsp/footer.jsp"%>
</body>
</html>