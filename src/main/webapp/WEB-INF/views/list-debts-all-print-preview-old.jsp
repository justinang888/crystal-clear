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
    <div class="container" style="width: 700px">
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-info">
                    <div class="panel-heading" style="font-size: 10px;">
                       List of A/Rs for each customer as of <span class="label label-danger">${dateToday}</span>
                        <p>Total of all Accounts Receivables: <fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${arTotal}"></fmt:formatNumber></p>
                    </div>
                    <div class="panel-body">
                        <table id="debtsTable" class="table table-striped table-bordered table-hover" style="font-size: 10px;">
                            <thead>
                                <tr>
                                    <th style="padding: 8px;">Customer</th>
                                    <th style="padding: 8px;" width="100">Total A/R</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${debtsMap}">
                                    <tr>
                                        <td style="padding: 8px;">${entry.value.customerName}</td>
                                        <td style="padding: 8px;"><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${entry.value.debtsTotal}"></fmt:formatNumber></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="fragments/footer.jsp" />
    
    <script type="text/javascript">
        $(document).ready(() => {
            $('#debtsTable').DataTable({
                paging: false,
                searching: false
            })
        })
    </script>
</body>
</html>