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
                <div class="panel-heading clearfix">
                    <p class="pull-left">List of Customers</p> 
                    <spring:url value="/customers/new" var="urlAddCustomer" />
                    <spring:url value="/customers/list" var="urlListCustomers" />
                    <a href="${urlAddCustomer}" class="btn btn-info pull-right" role="button" style="margin-left: 5px;">New Customer</a>
                    <a href="${urlListCustomers}?mode=preview" class="btn btn-default pull-right" role="button">Print Preview</a>
                </div>
                <div class="panel-body">
                    <table id="myTable" class="table table-striped table-bordered table-hover">
                        <thead>
                          <tr>
                            <th>Type</th>
                            <th>Customer Name</th>
                            <th>Refill Price</th>
                            <th>Address</th>
                            <th>Contact Person</th>
                            <th>Contact Number</th>
                            <th width="100">Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="cp" items="${customerPrices}">
                                <tr>
                                    <td>${cp.customer.type}</td>
                                    <td>${cp.customer.name}</td>
                                    <td>${cp.refillPrice}</td>
                                    <td>${cp.customer.address}</td>
									<td>${cp.customer.contactName}</td>
                                    <td>${cp.customer.mainNumber}</td>
                                    <td nowrap>
<!-- 										<p><button class="btn btn-default">New Order</button></p>  -->
										<!--                                         <button class="btn btn-info">View</button> -->
										
										<form action="view" method="get" style="display: inline-block;" >
											<%-- <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /> --%>
											<input type="hidden" name=customerId value="${cp.customer.customerId}"> 
											<input class="btn btn-info" type="submit" value="View">
										</form>
										<spring:url value="/orders/neworder" var="newOrderUrl"/>
										<form action="${newOrderUrl}" method="get" style="display: inline-block;">
											<%-- <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /> --%> 
											<input type="hidden" name="customerId" value="${cp.customer.customerId}"/> 
											<input class="btn btn-default" type="submit" value="New Order"/>
										</form>
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
            $('#myTable').DataTable({
            	"columns": [
            	    null,
            	    null,
            	    null,
            	    null,
            	    null,
            	    null,
            	    { "orderable": false }
            	  ]
            	
            });
            
            $('#headerNav a').click(function(e) {
    			$('#headerNav a').removeClass('active');
    			$(this).addClass('active');
    		});
        });
    </script>

</body>
</html>