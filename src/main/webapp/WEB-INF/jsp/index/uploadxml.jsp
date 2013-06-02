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
	<div class="container">

		<div class="row-fluid">
			<div class="span6">
				<h3>OLAP_</h3>
				<div class="thumb_image">
					<img alt="Logo OLAP" src="<c:url value='/img/Cube_OLAP.png'/>" style=" height: 200; width: 180; ">
				</div>
           		<div class="caption pull-left">
           			<p>Nullam quis risus eget urna mollis ornare vel eu leo. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nullam id dolor id nibh ultricies vehicula ut id elit.</p>
					<p>Vivamus sagittis lacus vel augue laoreet rutrum faucibus dolor auctor. Duis mollis, est non commodo luctus, nisi erat porttitor ligula, eget lacinia odio sem nec elit. Donec sed odio dui.</p>
  			  	</div>
         	   	<div class="clear"></div>
         	   	
				<p>Nullam quis risus eget urna mollis ornare vel eu leo. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nullam id dolor id nibh ultricies vehicula ut id elit.</p>
				<p>Vivamus sagittis lacus vel augue laoreet rutrum faucibus dolor auctor. Duis mollis, est non commodo luctus, nisi erat porttitor ligula, eget lacinia odio sem nec elit. Donec sed odio dui.</p>
		    </div>
			<div class="span6">
				<h1>Cargar archivo XML_</h1>
				<div class="well">
					<form:form name="uploadxmlform" class="form-horizontal"
						action="uploadxml" method="POST" enctype="multipart/form-data"
						commandName="uploadxmlform">
						<form:errors path="*" />
						<fieldset>
							<legend>Documento XML Multidim</legend>
							<form:input type="file" class="input-xlarge" id="photo"
										name="xml_doc" path="file" />
							<div class="form-actions">
								<input type="submit" class="btn btn-primary" value="Subir documento " />
							</div>
						</fieldset>
					</form:form>
				</div>
				<span class="">Conectado a <span class="badge badge-warning">${dburl}</span></span>
			</div>
		</div>
	</div>
	<%@ include file="/WEB-INF/jsp/footer.jsp"%>
</body>
</html>