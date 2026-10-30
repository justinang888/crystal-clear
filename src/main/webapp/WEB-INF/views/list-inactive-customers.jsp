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
                    <h3 class="pull-left">List of Inactive Customers</h3> 
                    <spring:url value="/customers/new" var="urlAddCustomer" />
                    <spring:url value="/customers/list" var="urlListCustomers" />
                    <a href="${urlAddCustomer}" class="btn btn-info pull-right" role="button" style="margin-left: 5px;">New Customer</a>
                    <a href="${urlListCustomers}?mode=preview" class="btn btn-default pull-right" role="button">Print Preview</a>
                </div>
                <div class="panel-body">
                	<p>These are customers that have not had any purchases for a period of more than one month.</p>
                    <table id="myTable" class="table table-striped table-bordered table-hover">
                        <thead>
                          <tr>
                            <th>Type</th>
                            <th>Customer Name</th>
                            <th>Address</th>
                            <th>Contact Person</th>
                            <th>Contact Number</th>
                            <th>Action</th>
                          </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${customerList}">
                                <tr>
                                    <td>${c.type}</td>
                                    <td>${c.name}</td>
                                    <td>${c.address}</td>
									<td>${c.contactName}</td>
                                    <td>${c.mainNumber}</td>
                                    <td>
                                    	<form action="view" method="get" style="display: inline-block;" >
											<input type="hidden" name=customerId value="${c.customerId}"> 
											<input class="btn btn-info" type="submit" value="View">
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
            	    null
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