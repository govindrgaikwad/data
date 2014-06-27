<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!doctype html>
<html>
<head>
<script type="text/javascript"
	src="<c:url value='/css/bootstrap-datetimepicker.min.css' />"></script>

<link rel="stylesheet" type="text/css"
	href="<c:url value='/css/bootstrap.min.css' />" media="screen">

<link rel="stylesheet" type="text/css"
	href="<c:url value='/css/jquery-ui-1.9.2.custom.css' />" media="screen">

<link rel="stylesheet" type="text/css"
	href="<c:url value='/css/ui.jqgrid.css' />" media="screen">



<script type="text/javascript"
	src="<c:url value='/scripts/external/bootstrap.min.js' />"></script>

<script type="text/javascript"
	src="<c:url value='/scripts/external/bootstrap-datetimepicker.js' />"></script>

<script type="text/javascript"
	src="<c:url value='/scripts/external/jquery-1.11.0.min.js' />"></script>

<script type="text/javascript"
	src="<c:url value='/scripts/external/jquery-ui-1.9.2.custom.min.js' />"></script>

<script type="text/javascript"
	src="<c:url value='/scripts/external/jquery.jqGrid.js' />"></script>
</head>
<body>
	<div class="container-fluid">
		<div class="container">
			<tiles:insertAttribute name="header" />
			<tiles:insertAttribute name="body" />
			<tiles:insertAttribute name="footer" />
		</div>
	</div>

</body>
</html>