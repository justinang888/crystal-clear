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
    <div class="container" style="width: 1300px">
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
                        <spring:url value="/orders/daily" var="dailySalesUrl"/>
                        <form class="form-horizontal" method="get" action="${dailySalesUrl}">
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
	                <div class="panel-heading clearfix">
	                    Daily sales for <span class="label label-danger">${datePicked}</span>
	                    <a href="${dailySalesUrl}?d=${dateParam}&mode=preview" class="btn btn-default pull-right" role="button">Print Preview</a>
	                </div>
	                <div class="panel-body">
	                    <table id="myTable" class="table table-striped table-bordered table-hover" style="font-size: 12px;">
	                        <thead>
	                          <tr>
	                          	<th>DR#</th>
	                          	<th width="100">Time</th>
	                            <th width="100">Customer</th>
								<th width="10">ROUND Refill</th>
	                            <th width="10">SLIM Refill</th>
	                            <th width="10">ROUND Cont</th>
	                            <th width="10">SLIM Cont</th>
	                            <th width="10">ROUND Both</th>
	                            <th width="10">SLIM Both</th>
	                            <th width="50">Remarks</th>
	                            <th width="30" bgcolor="F7E8D0">A/R</th>
	                            <th width="30" bgcolor="ECFBEA">Cash</th>
	                            <th width="30" bgcolor="EEE7FB">Check</th>
<!-- 	                            <th width="30" bgcolor="FBEAEA">Expense</th> -->
	                            <th width="10">ROUND Ret</th>
	                            <th width="10">SLIM Ret</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                            <c:forEach var="d" items="${dailySales}">
	                                <tr>
	                                	<td>${d.order.deliveryReceiptNum}</td>
	                                	<td>${d.dateAndTime}</td>
	                                    <td>${d.customerName}</td>
	                                    <td>${d.order.roundRefillOnlyCount}</td>
	                                    <td>${d.order.slimRefillOnlyCount}</td>
	                                    <td>${d.order.roundContainerOnlyCount}</td>
	                                    <td>${d.order.slimContainerOnlyCount}</td>
	                                    <td>${d.order.roundRefillWithContainerCount}</td>
	                                    <td>${d.order.slimRefillWithContainerCount}</td>
	                                    <td>${d.remarks}</td>
	                                    <td bgcolor="F7E8D0"><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${d.balanceAmount}" /></td>
	                                    <td bgcolor="ECFBEA"><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${d.paidCash}" /></td>
	                                    <td bgcolor="EEE7FB"><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${d.paidCheck}" /></td>
<%-- 	                                    <td bgcolor="FBEAEA"><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${d.expenseAmount}" /></td> --%>
	                                    <td>${d.returnedRound}</td>
	                                    <td>${d.returnedSlim}</td>
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
                		Totals
                	</div>
                	
                	 <div class="panel-body">
                	 	<table id="myTable" class="table table-striped table-bordered table-hover">
                	 		<thead>
	                          <tr>
	                            <th>Round Delivered</th>
	                            <th>Slim Delivered</th>
	                            <th>Round Returned</th>
	                            <th>Slim Returned</th>
	                            <th bgcolor="F7E8D0">A/R</th>
	                            <th bgcolor="ECFBEA">Cash Payment</th>
	                            <th bgcolor="EEE7FB">Check Payment</th>
<!-- 	                            <th bgcolor="FBEAEA">Expense</th> -->
	                            <th bgcolor="D0E6F7">Net Cash</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                        	<tr>
	                        		<td>${totalRoundDelivered}</td>
	                        		<td>${totalSlimDelivered}</td>
	                        		<td>${totalRoundReturned}</td>
	                        		<td>${totalSlimReturned}</td>
	                        		<td bgcolor="F7E8D0">${totalDebt}</td>
	                        		<td bgcolor="ECFBEA">${totalCashPayments}</td>
	                        		<td bgcolor="EEE7FB">${totalCheckPayments}</td>
<%-- 	                        		<td bgcolor="FBEAEA">${totalExpenses}</td> --%>
	                        		<td bgcolor="D0E6F7">${netCash}</td>
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
                "order": [[ 0, "desc" ]]
            } );
        })
        
    </script>

</body>
</html>