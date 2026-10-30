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
    <div class="container" style="width: 900px">
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-info">
                    <div class="panel-heading">
                        <p>Due Dates</p>
                    </div>
                    <div class="panel-body">
                        <table id="containersTable" class="table table-striped table-bordered table-hover">
                            <thead>
                                <tr>
                                    <th>Customer</th>
                                    <th>Contact Number</th>
                                    <th width="120">Last Order Date</th>
                                    <th width="120">Due Date</th>
                                    <th width="60" bgcolor="FBEAEA">Days Remaining</th>
                                    <th width="50">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${dueDates}">
                                    <spring:url value="/customers/view?customerId=${c.customer.customerId}" var="urlViewCustomer" />
                                    <spring:url value="/orders/neworder?customerId=${c.customer.customerId}" var="newOrderUrl"/>
                                    <tr>
                                        <td><a href="${urlViewCustomer}">${c.customer.name}</a></td>
                                        <td>${c.customer.mainNumber}</td>
                                        <td>${c.lastOrderDate}</td>
                                        <td>${c.dueDate}</td>
                                        <td bgcolor="FBEAEA">${c.daysRemaining}</td>
                                        <td>
                                            <a href="${newOrderUrl}" style="text-align: center;" class="btn btn-default" role="button">New Order</a>
                                        </td>
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
            $('#containersTable').DataTable({
            	"columns": [
            	    null,
            	    null,
            	    null,
            	    null,
            	    null,
            	    { "orderable": false }
            	  ],
                fixedHeader: true,
                paging: false,
                scrollY: 350,
                scrollCollapse: true
            })
        })
    </script>
</body>
</html>