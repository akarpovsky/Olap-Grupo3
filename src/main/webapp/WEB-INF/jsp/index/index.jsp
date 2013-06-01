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
		<h1>Conexi&oacute;n con la base de datos</h1>
		<c:if test="${couldNotConnectToDB }">
					<h3>No se pudo conectar a la base de datos.</h3>
				</c:if>
				<form:form name="dbcredentialsform" class="form-horizontal span8 offset2"
					action="connectToDB" method="POST"
					commandName="dbcredentialsform">
					<form:errors path="*" />
					<fieldset>
						<legend> </legend>
						<div class="control-group">
							<label class="control-label" for="xml_doc">URL DB </label>
							<div class="controls">
								<form:input type="text" class="input-xlarge" id="url_db"
									name="url_db" path="url_db" />
								<span>localhost:5432/postgres</span>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="xml_doc">User </label>
							<div class="controls">
								<form:input type="text" class="input-xlarge" id="user_db"
									name="user_db" path="user_db" />
								<span>postgres</span>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="xml_doc">Password</label>
							<div class="controls">
								<form:input type="text" class="input-xlarge" id="password_db"
									name="password_db" path="password_db" />
								<span>postgres</span>
							</div>
						</div>												
						<div class="form-actions">
							<input type="submit" class="btn btn-primary" value="Conectar" />
						</div>
					</fieldset>
				</form:form>
	</div>
	<%@ include file="/WEB-INF/jsp/footer.jsp"%>
</body>
</html>