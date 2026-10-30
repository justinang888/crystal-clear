<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                            X
                        </button>
                        <strong>${msg}</strong>
                    </div>
                </c:if>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <p>
                            <spring:url value="/customers/view?customerId=${customer.customerId}" var="urlViewCustomer" />
                            List of payments for <a href="${urlViewCustomer}">${customer.name}.</a>

                        </p>
                        <p>Running Total: ${paymentsTotal}.
                            <spring:url value="/payments/new?cid=${customer.customerId}" var="newPaymentUrl" />
                            <a href="${newPaymentUrl}" class="btn btn-default pull-right" role="button">New Payment</a>
                        </p>
                    </div>
                    <div class="panel-body">
                        <table id="paymentsTable" class="table table-striped table-bordered table-hover">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Remarks</th>
                                    <th>Amount</th>
                                    <th>Withholding Tax</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="payment" items="${payments}">
                                    <tr>
                                        <td>${payment.createdAt}</td>
                                        <td>${payment.remarks}</td>
                                        <td>${payment.amount}</td>
                                        <td>${payment.withholdingTax}</td>
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
            $('#paymentsTable').DataTable({
                fixedHeader: true,
                paging: false,
                scrollY: 350,
                scrollCollapse: true
            })
        })
    </script>
</body>
</html>