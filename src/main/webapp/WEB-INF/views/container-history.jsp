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
                
                <div class="panel panel-info">
                    <div class="panel-body">
                        <spring:url value="/container/history" var="containerHistoryUrl"/>
                        <form class="form-horizontal" method="get" action="${containerHistoryUrl}">
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
	                    Container history for ${datePicked}.
	                </div>
	                <div class="panel-body">
	                    <table id="myTable" class="table table-striped table-bordered table-hover">
	                        <thead>
	                          <tr>
	                          	<th width=300>Customer</th>
	                          	<th width=150>Address</th>
	                          	<th>Delivered Round</th>
	                          	<th>Delivered Slim</th>
	                          	<th>Returned Round</th>
	                          	<th>Returned Slim</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                            <c:forEach var="cd" items="${containerDaily}">
	                            	<spring:url value="/customers/view?customerId=${c.key}" var="urlViewCustomer" />
	                                <tr>
	                                	<td><a href="${urlViewCustomer}">${cd.customer.name}</a></td>
	                                	<td>${cd.customer.address}</td>
	                                	<td>${cd.roundTotalDelivered}</td>
	                                	<td>${cd.slimTotalDelivered}</td>
	                                	<td>${cd.roundTotalReturned}</td>
	                                	<td>${cd.slimTotalReturned}</td>
	                                </tr>
	                            </c:forEach>
	                        </tbody>
	                    </table>
	                </div>
	            </div>
	        </div>
	    </div>
	    
	    <div class="row">
    		<div class="col-lg-12">
    			<div class="panel panel-info">
                	<div class="panel-heading clearfix">
                		Daily Totals
                	</div>
                	
                	 <div class="panel-body">
                	 	<table id="myTable" class="table table-striped table-bordered table-hover">
                	 		<thead>
	                          <tr>
								<th>Delivered Round</th>
	                            <th>Delivered Slim</th>
	                            <th>Returned Round</th>
	                            <th>Returned Slim</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                        	<tr>
	                        		<td>${totalRoundDelivered}</td>
	                        		<td>${totalSlimDelivered}</td>
	                        		<td>${totalRoundReturned}</td>
	                        		<td>${totalSlimReturned}</td>
	                        	</tr>
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
            $('#myTable').DataTable( {
                "order": [[ 0, "asc" ]],
				fixedHeader: true,
                paging: false,
                scrollY: 350,
                scrollCollapse: true
            } );
        })
        
    </script>

</body>
</html>