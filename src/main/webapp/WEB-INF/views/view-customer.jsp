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
                        <span aria-hidden="true">X</span>
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
                    <spring:url value="/customers/edit" var="urlEditCustomer" />
                    <spring:url value="/customers/containerBalances" var="urlUpdateContainerBalances" />
                    <p class="pull-left" style="font-size: 20px"><b>${customer.name}</b></p>
                    <a href="${urlUpdateContainerBalances}?customerId=${customer.customerId}" class="btn btn-default pull-right" role="button">Update Container Balance</a>
                    <a href="${urlEditCustomer}?customerId=${customer.customerId}" class="btn btn-default pull-right" role="button">Edit</a>
                </div>
                <div class="panel-body">
                    <p><b>Type:</b> ${customer.type}</p>
                    <p><b>Address:</b> ${customer.address}</p>
                    <p><b>Contact Person:</b> ${customer.contactName}</p>
                    <p><b>Main number:</b> ${customer.mainNumber}</p>
                    <p><b>Alternate number:</b> ${customer.alternateNumber}</p>
					<p><b>Email Address:</b> ${customer.emailAddress}</p>
					<p><b>Order Interval:</b> ${customer.orderInterval} Days</p>
					<h4><span class="label label-danger pull-right">${dueDateText}</span></h4>
                </div>
            </div>
        </div>
    </div>
    </div>
    
    <div class="container">
    	<div class="row">
    		<div class="col-lg-12">
    			<div class="panel panel-info">
                	<div class="panel-heading clearfix">
						Prices
						<spring:url value="/prices/list/${customer.customerId}" var="listPriceUrl" />
						<a href="${listPriceUrl}" class="btn btn-default pull-right" role="button">Add Product &amp; Price</a>
                	</div>
                	
                	 <div class="panel-body">
                	 	<table id="myPriceTable" class="table table-striped table-bordered table-hover">
                	 		<thead>
	                          <tr>
	                            <th>Product Name</th>
	                            <th>Price</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                        	<c:forEach var="p" items="${prices}">
	                        		<tr>
	                        		<td>${p.product.name}</td>
	                        		<td>${p.sellingPrice}</td>
	                        	</tr>
	                        	</c:forEach>
	                        </tbody>
                	 	</table>
                	 </div>
                </div>
    		</div>
    	</div>
    </div>
    
    <div class="container">
	    <div class="row">
	        <div class="col-lg-12">
	            <div class="panel panel-info">
	                <div class="panel-heading clearfix">
                        <spring:url value="/orders/neworder" var="urlNewOrder" />
	                    Order History
	                    <a href="${urlNewOrder}?customerId=${customer.customerId}" class="btn btn-default pull-right" role="button">New Order</a>
	                </div>
	                <div class="panel-body">
	                    <table id="myTable" class="table table-striped table-bordered table-hover">
	                        <thead>
	                          <tr>
	                            <th>Delivery Receipt #</th>
	                            <th width="50">Status</th>
	                            <th>Amount Paid</th>
	                            <th>Total Amount</th>
	                            <th>Round Refill Only</th>
	                            <th>Slim Refill Only</th>
	                            <th>Round Cont Only</th>
	                            <th>Slim Cont Only</th>
	                            <th>Round Both</th>
	                            <th>Slim Both</th>
	                            <th>Ret Round</th>
	                            <th>Ret Slim</th>
	                            <th>Date</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                            <c:forEach var="o" items="${orders}">
	                                <tr>
	                                    <td>${o.deliveryReceiptNum}</td>
	                                    <td>${o.status}</td>
	                                    <td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${o.amountPaid}"></fmt:formatNumber></td>
	                                    <td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${o.totalAmount}"></fmt:formatNumber></td>
	                                    <td>${o.roundRefillOnlyCount}</td>
	                                    <td>${o.slimRefillOnlyCount}</td>
	                                    <td>${o.roundContainerOnlyCount}</td>
                                    	<td>${o.slimContainerOnlyCount}</td>
                                    	<td>${o.roundRefillWithContainerCount}</td>
                                    	<td>${o.slimRefillWithContainerCount}</td>
	                                    <td>${o.roundReturned}</td>
	                                    <td>${o.slimReturned}</td>
	                                    <td>${o.createdAt}</td>
	                                </tr>
	                            </c:forEach>
	                        </tbody>
	                    </table>
	                </div>
	            </div>
	        </div>
	    </div>
    </div>
    
    <div class="container">
    	<div class="row">
    		<div class="col-lg-12">
    			<div class="panel panel-info">
                	<div class="panel-heading clearfix">
                        <spring:url value="/container/return" var="urlReturnContainer" />
						Container Returns History
						<a href="${urlReturnContainer}?cid=${customer.customerId}" class="btn btn-default pull-right" role="button">Return Container</a>
                	</div>
                	
                	 <div class="panel-body">
                	 	<table id="myContainerTable" class="table table-striped table-bordered table-hover">
                	 		<thead>
	                          <tr>
	                            <th>Round Returned</th>
	                            <th>Slim Returned</th>
	                            <th>Within an Order?</th>
	                            <th>Return Date</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                        	<c:forEach var="ch" items="${containerHistory}">
	                        		<tr>
		                        		<td>${ch.roundCount}</td>
		                        		<td>${ch.slimCount}</td>
		                        		<td>${ch.orderId}</td>
		                        		<td>${ch.createdAt}</td>
	                        		</tr>
	                        	</c:forEach>
	                        </tbody>
                	 	</table>
                	 </div>
                </div>
    		</div>
    	</div>
    </div>
    
    <div class="container">
    	<div class="row">
    		<div class="col-lg-12">
    			<div class="panel panel-info">
                	<div class="panel-heading clearfix">
                        <spring:url value="/payments/new" var="urlNewPayment" />
						Payment History
						<a href="${urlNewPayment}?cid=${customer.customerId}" style="margin-right: 5px" class="btn btn-default pull-right" role="button">New Payment</a>
                	</div>
                	
                	 <div class="panel-body">
                        <table id="myPaymentTable" class="table table-striped table-bordered table-hover">
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
    
    <div class="container">
    	<div class="row">
    		<div class="col-lg-12">
    			<div class="panel panel-info">
                	<div class="panel-heading clearfix">
                        <spring:url value="/debts/new" var="urlNewDebt" />
                		Details
                		<a href="${urlNewDebt}?cid=${customer.customerId}" style="margin-right: 5px" class="btn btn-default pull-right" role="button">New A/R Entry</a>
                	</div>
                	
                	 <div class="panel-body">
                	 	<table id="myTable" class="table table-striped table-bordered table-hover">
                	 		<thead>
	                          <tr>
	                            <th>Last Order Date</th>
	                            <th>Due Date</th>
	                            <th>Total Borrowed Round</th>
	                            <th>Total Borrowed Slim</th>
	                            <th>Total A/R</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                        	<tr>
	                        		<td>${mostRecentOrderDate}</td>
	                        		<td>${dueDate}</td>
	                        		<td>${totalBorrowedRound}</td>
	                        		<td>${totalBorrowedSlim}</td>
	                        		<td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${totalDebt}"></fmt:formatNumber></td>
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
            $('#myTable').DataTable({
            	"order" : [[ 0, "desc"]],
				fixedHeader: true,
				scrollY: 350,
                scrollCollapse: true
            })
            $('#myPriceTable').DataTable({
				fixedHeader: true
            })
            $('#myContainerTable').DataTable({
            	"order" : [[ 3, "desc"]],
				fixedHeader: true
            })
            $('#myPaymentTable').DataTable({
				fixedHeader: true
            })
        })
    </script>

</body>
</html>