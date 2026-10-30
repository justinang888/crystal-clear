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
    <div class="container" style="width: 900px">
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
                
                <div class="panel panel-info">
                    <div class="panel-body">
                        <spring:url value="/customers/dailyduedates" var="dailyDueDatesUrl"/>
                        <form class="form-horizontal" method="get" action="${dailyDueDatesUrl}">
                            <div class="form-group">
                                <label class="col-sm-1 control-label">Date: </label>
                                <div class="col-sm-3">
                                    <input type="date" class="form-control" name="d" placeholder="Date"/>
                                </div>
   								<div class="col-sm-5">
								    <button type="submit" class="btn btn-primary">Refresh</button>
								</div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    
	    <div class="row">
	        <div class="col-lg-12">
	            <div class="panel panel-info">
	                <div class="panel-heading">
	                    Customers due on <span class="label label-danger">${datePicked}</span>
	                </div>
	                <div class="panel-body">
	                    <table id="dueDatesTable" class="table table-striped table-bordered table-hover">
	                        <thead>
	                          <tr>
	                            <th width="200">Customer</th>
	                            <th width="180">Address</th>
								<th>Contact Number</th>
                           		<th width="120">Last Order Date</th>
                           		<th width="50">Action</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                            <c:forEach var="c" items="${dueDates}">
                                    <spring:url value="/customers/view?customerId=${c.customer.customerId}" var="urlViewCustomer" />
                                    <spring:url value="/orders/neworder?customerId=${c.customer.customerId}" var="newOrderUrl"/>
                                    <tr>
                                        <td><a href="${urlViewCustomer}">${c.customer.name}</a></td>
                                        <td>${c.customer.address}</td>
                                        <td>${c.customer.mainNumber}</td>
                                        <td>${c.lastOrderDate}</td>
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
            $('#dueDatesTable').DataTable( {
                "order": [[ 0, "desc" ]],
            	"columns": [
	        	    null,
	        	    null,
	        	    null,
	        	    null,
	        	    { "orderable": false }
	        	]
            } );
        })
        
    </script>

</body>
</html>