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
                            X
                        </button>
                        <strong>${msg}</strong>
                    </div>
                </c:if>
                
                <div class="panel panel-info">
                    <div class="panel-body">
                        <spring:url value="/orders/monthly" var="monthlyTotalsUrl"/>
                        <form class="form-horizontal" method="get" action="${monthlyTotalsUrl}">
                            <div class="form-group col-sm-6">
								<div class="col-sm-6 input-append date" id="datepicker" data-date="00-2024"
								     data-date-format="mm-yyyy">
								 	<input class="form-control" type="text" readonly="readonly" name="d" placeholder="select month">
								 	<span class="glyphicon glyphicon-calendar add-on" style="padding-top: 5px;"></span>
<!-- 									 	<button class="btn btn-primary">+</button>     -->
								</div>  
								<div class="col-sm-6">
								    <button type="submit" class="btn btn-primary">Generate Report</button>
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
	                    Summary: ${monthYear}
	                </div>
	                <div class="panel-body">
	                    <table id="myTable" class="table table-striped table-bordered table-hover table-fixed">
	                        <thead>
	                          <tr>
	                            <th width="100">Date</th>
	                            <th>Sum of Cash</th>
	                            <th>Sum of A/R</th> <!-- Debts -->
	                            <th>Sum of Payments</th>
	                            <th>Sum of Expenses</th>
	                            <th>Delivered Round</th>
	                            <th>Delivered Slim</th>
	                            <th>Delivered Total</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                            <c:forEach var="dt" items="${dailyTotals}">
	                                <tr>
	                                   <td>${dt.date}</td>
	                                   <td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${dt.totalCash}"></fmt:formatNumber></td>
	                                   <td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${dt.totalAr}"></fmt:formatNumber></td> <!-- Debts -->
	                                   <td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${dt.totalPayments}"></fmt:formatNumber></td>
	                                   <td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${dt.totalExpenses}"></fmt:formatNumber></td>
	                                   <td>${dt.totalDeliveredRound}</td>
	                                   <td>${dt.totalDeliveredSlim}</td>
	                                   <td>${dt.totalDeliveredContainers}</td>
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
                		Grand Total
                	</div>
                	
                	 <div class="panel-body">
                	 	<table id="myGrandTable" class="table table-striped table-bordered table-hover">
	                        <thead>
	                        	<tr>
									<th>Sum of Cash</th>
									<th>Sum of A/R</th>
									<th>Sum of Expenses</th>
									<th>Delivered Round</th>
									<th>Delivered Slim</th>
									<th>Delivered Total</th>
								</tr>
	                        </thead>
	                        <tbody>
	                        	<tr>
	                        		<td>${grandTotalCash}</td>
	                        		<td>${grandTotalAr}</td>
	                        		<td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${grandTotalExpense}"></fmt:formatNumber></td>
	                        		<td>${grandTotalRound}</td>
	                        		<td>${grandTotalSlim}</td>
	                        		<td>${grandTotalContainers}</td>
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
            	//"pageLength": 50
				fixedHeader: true,
                paging: false,
                scrollY: 350,
                scrollCollapse: true
            } );
            
            $("#datepicker").datepicker( {
                format: "mm-yyyy",
                viewMode: "months", 
                minViewMode: "months"
            });
        })
    </script>

</body>
</html>