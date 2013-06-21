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
		
		<h1>Elija las tablas para cada dimensi&oacute;n</h1>
		<span class="" >Conectado a <span class="badge badge-warning">${dburl}</span></span><br />
			<form:form name="tableSelectForm" class="form-horizontal span8"
					action="select_db_table" method="POST"
					commandName="tableSelectForm"  style="margin-top: 30px">
				<fieldset>
				    	<c:forEach items="${userSelectedFieldList}" var="entry">
							<div class="control-group">
					            <label class="control-label" for="input01">${entry.name}</label>
					            <div class="controls">
										<form:select path="tablesMap['${entry.name}']">
											<form:options items="${existingDBTablesList}" itemValue="name" itemLabel="name"   />
										</form:select>
					            </div>
				        	</div>
						</c:forEach>
						
						<div class="form-actions">
							<input type="submit" class="btn btn-primary" value="Continuar..." />
						</div>
				</fieldset>
		</form:form>
	    
	</div>
	<%@ include file="/WEB-INF/jsp/footer.jsp"%>
</body>
</html>