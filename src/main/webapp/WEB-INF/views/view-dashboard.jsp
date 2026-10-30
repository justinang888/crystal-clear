<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="fragments/header.jsp" />
</head>
<body>
    <jsp:include page="fragments/nav.jsp" />
    <div class="container">
    
    <div class="row">
        <div class="col-lg-12">
            <c:if test="${not empty msg}">
                <div class="alert alert-success alert-dismissible" role="alert">
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                        <span aria-hidden="true">X</span>
                    </button>
                    <strong>${msg}</strong>
                </div>
            </c:if>
        </div>
    </div>
    <div class="row">
        <div class="col-lg-12">
            <div class="panel panel-info">
                <div class="panel-heading clearfix">
                    <p class="pull-left" style="font-size: 20px"><b>Dashboard</b></p>
                </div>
                <div class="panel-body">
                    <p><b>Pending Orders:</b> ${pendingCount}</p>
                    <p><b>Borrowed round containers:</b> ${runningRound}</p>
                    <p><b>Borrowed slim containers:</b> ${runningSlim}</p>
                    <p><b>New customers as of this month:</b> </p>
                    <p><b>New orders as of this month:</b> </p>
                </div>
            </div>
        </div>
    </div>
    </div>
    
    <jsp:include page="fragments/footer.jsp" />
    <script src="<c:url value="/resources/js/jquery.min.js"/>"></script>
    <script src="<c:url value="/resources/js/bootstrap.min.js"/>"></script>
	<script src="<c:url value="/resources/js/datatables.min.js"/>"></script>
	<script src="<c:url value="/resources/js/dataTables.fixedHeader.min.js"/>"></script>
    <link rel="stylesheet" href="<c:url value="/resources/css/bootstrap.css"/>"/>
	<link rel="stylesheet" href="<c:url value="/resources/css/datatables.min.css"/>"/>
	<link rel="stylesheet" href="<c:url value="/resources/css/fixedHeader.bootstrap.min.css"/>"/>
    <script type="text/javascript">
        $(document).ready(() => {
            $('#myTable').DataTable({
            	"order" : [[ 0, "desc"]],
				fixedHeader: true,
				scrollY: 350,
                scrollCollapse: true
            })
            $('#myPriceTable').DataTable({
				fixedHeader: true
            })
            $('#myContainerTable').DataTable({
				fixedHeader: true
            })
            $('#myPaymentTable').DataTable({
				fixedHeader: true
            })
        })
    </script>

</body>
</html>