<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="X-UA-Compatible" content="ie=edge">

<link rel="stylesheet" href="<spring:url value="/resources/css/bootstrap.min.css"/>"/>
<link rel="stylesheet" href="<spring:url value="/resources/css/bootstrap-theme.min.css"/>"/>
<link rel="stylesheet" href="<spring:url value="/resources/css/datatables.min.css"/>"/>
<link rel="stylesheet" href="<spring:url value="/resources/css/fixedHeader.bootstrap.min.css"/>"/>
<link rel="stylesheet" href="<spring:url value="/resources/css/datepicker.css"/>"/>
<link rel="stylesheet" href="<spring:url value="/resources/css/main.css"/>"/>

<script src="<spring:url value="/resources/js/jquery.min.js"/>"></script>
<script src="<spring:url value="/resources/js/bootstrap.min.js"/>"></script>
<script src="<spring:url value="/resources/js/datatables.min.js"/>"></script>
<script src="<spring:url value="/resources/js/dataTables.fixedHeader.min.js"/>"></script>
<script src="<spring:url value="/resources/js/bootstrap-datepicker.js"/>"></script>

<style type="text/css">
     .loader {
     position: fixed;
     left: 0px;
     top: 0px;
     width: 100%;
     height: 100%;
     z-index: 9999;
     background: url('${contextPath}/aurora/resources/images/pageLoader.gif') 50% 50% no-repeat rgb(249,249,249);
     }
 </style> 