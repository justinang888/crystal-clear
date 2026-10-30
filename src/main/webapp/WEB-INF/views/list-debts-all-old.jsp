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
                    <div class="panel-heading clearfix">
                        <spring:url value="/debts/listAll" var="urlListDebts" />
                        List of A/Rs for each customer as of <span class="label label-danger">${dateToday}</span>
                        <p>Total of all Accounts Receivables: <fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${arTotal}"></fmt:formatNumber></p>
                        <a href="${urlListDebts}?mode=preview" class="btn btn-default pull-right" role="button">Print Preview</a>
                    </div>
                    <div class="panel-body">
                        <table id="debtsTable" class="table table-striped table-bordered table-hover">
                            <thead>
                                <tr>
                                    <th>Customer</th>
                                    <th width="160" bgcolor="F7E8D0">Total A/R</th>
                                    <!--<th width="220">Actions</th>-->
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${debtsMap}">
                                    <spring:url value="/customers/view?customerId=${entry.key}" var="urlViewCustomer" />
                                    <spring:url value="/payments/new?cid=${entry.key}" var="urlNewPayment" />
                                    <spring:url value="/payments/list?cid=${entry.key}" var="urlListPayment" />
                                    <spring:url value="/debts/list?cid=${entry.key}" var="urlListDebts" />
                                    <spring:url value="/debts/new?cid=${entry.key}" var="urlNewArEntry" />
                                    <tr>
                                        <td><a href="${urlViewCustomer}">${entry.value.customerName}</a></td>
                                        <td bgcolor="F7E8D0"><a href="${urlListDebts}"><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${entry.value.debtsTotal}"></fmt:formatNumber></a></td>
                                        <!--<td>
                                            <a href="${urlNewPayment}" style="margin-right: 5px" class="btn btn-default pull-right" role="button">New Payment</a>
                                            <a href="${urlListPayment}" style="margin-right: 5px" class="btn btn-info pull-right" role="button">List Payments</a>
                                        </td>-->
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
                fixedHeader: true,
                paging: false,
                scrollY: 350,
                scrollCollapse: true
            })
        })
    </script>
</body>
</html>