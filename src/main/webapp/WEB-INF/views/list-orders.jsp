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
    <jsp:include page="fragments/nav.jsp" />    <div class="container" style="width: 1480px">
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
            
            <c:if test="${not empty failmsg}">
                <div class="alert alert-danger alert-dismissible" role="alert">
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                        X
                    </button>
                    <strong>${failmsg}</strong>
                </div>
            </c:if>

            <div class="panel panel-info">
                <div class="panel-body">
                    <spring:url value="/orders/list" var="listOrdersByDateUrl"/>
                    <form class="form-horizontal" method="get" action="${listOrdersByDateUrl}">
                        <div class="form-group">
                            <label class="col-sm-1 control-label">Date: </label>
                            <div class="col-sm-3">
                                <input type="date" class="form-control" name="d" placeholder="Date"/>
                            </div>
                               <div class="col-sm-1">
                                <button type="submit" class="btn btn-primary">List Orders</button>
                            </div>
                            <c:if test="${pendingCount > 0}">
                                <spring:url value="/orders/pending" var="urlShowPending" />
                    			<a href="${urlShowPending}" class="btn btn-danger" style="margin-bottom: 10px;" role="button" title="Pending for Delivery">${pendingCount}</a>
                    		</c:if>
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
                    List of Orders for <span class="label label-danger">${datePicked}</span>
                </div>
                <div class="panel-body">
                    <table id="myTable" class="table table-striped table-bordered table-hover" style="font-size: 12px;">
                        <thead>
                          <tr>
                          	<th>Actions</th>
                          	<th>Status</th>
                            <th>Customer Name</th>
                            <th>DR #</th>
                            <th>Amount Paid</th>
                            <th>Total Amount</th>
                            <th bgcolor="F6ECD5">ROUND Refill</th>
                            <th bgcolor="D5E8F6">SLIM Refill</th>
                            <th bgcolor="F6ECD5">ROUND Cont</th>
                            <th bgcolor="D5E8F6">SLIM Cont</th>
                            <th bgcolor="F6ECD5">ROUND Both</th>
                            <th bgcolor="D5E8F6">SLIM Both</th>
                            <th bgcolor="F6ECD5">ROUND Ret</th>
                            <th bgcolor="D5E8F6">SLIM Ret</th>
                            <th>Date</th>
                            <th>Edit</th>
                          </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="o" items="${orders}">
                            	<spring:url value="/customers/view?customerId=${o.order.customerId}" var="urlViewCustomer" />
                                <tr>
                                	<td nowrap>
<%--                                     	<form action="edit" method="get" style="display: inline-block;"> --%>
<%-- 											<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />  --%>
<!-- 											<input type="hidden" name=orderId value=${o.order.orderId}>  -->
<%-- 											<input class="btn btn-default" type="submit" value="Edit" <c:if test="${o.order.status == 'Delivered'}"><c:out value="disabled='disabled'"/></c:if>> --%>
<%-- 										</form> --%>
										<form action="deliver" method="get" style="display: inline-block;">
											<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /> 
											<input type="hidden" name="orderId" value="${o.order.orderId}" /> 
											<input class="btn btn-success" type="submit" value="Deliver"
												<c:if test="${o.order.status == 'Delivered' || o.order.status == 'Cancelled'}">
												<c:out value="disabled='disabled'"/></c:if>>
										</form>
										<form action="cancel" method="get" style="display: inline-block;">
											<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /> 
											<input type="hidden" name="orderId" value="${o.order.orderId}" /> 
											<input class="btn btn-danger" type="submit" value="Cancel" <c:if test="${o.order.status == 'Delivered' || o.order.status == 'Cancelled'}">
												<c:out value="disabled='disabled'"/></c:if>>
										</form>
									</td>
									<td>${o.order.status}</td>
                                    <td><a href="${urlViewCustomer}">${o.customerName}</a></td>
                                    <td>${o.order.deliveryReceiptNum}</td>
                                    <td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${o.order.amountPaid}"></fmt:formatNumber></td>
                                    <td><fmt:formatNumber type = "currency" pattern = "#,##0.00" value = "${o.order.totalAmount}"></fmt:formatNumber></td>
                                    <td bgcolor="F6ECD5">${o.order.roundRefillOnlyCount}</td>
                                    <td bgcolor="D5E8F6">${o.order.slimRefillOnlyCount}</td>
                                    <td bgcolor="F6ECD5">${o.order.roundContainerOnlyCount}</td>
                                    <td bgcolor="D5E8F6">${o.order.slimContainerOnlyCount}</td>
                                    <td bgcolor="F6ECD5">${o.order.roundRefillWithContainerCount}</td>
                                    <td bgcolor="D5E8F6">${o.order.slimRefillWithContainerCount}</td>
                                    <td bgcolor="F6ECD5">${o.order.roundReturned}</td>
                                    <td bgcolor="D5E8F6">${o.order.slimReturned}</td>
                                    <td>${o.formattedDate}</td>
                                    <td>
                                    	<spring:url value="/orders/edit" var="editOrderUrl"/>
										<form action="${editOrderUrl}" method="get" style="display: inline-block;" >
											<%-- <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /> --%> 
											<input type="hidden" name="orderId" value="${o.order.orderId}" /> 
											<input class="btn btn-default" type="submit" value="Edit">
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
            	"order" : [[ 3, "desc"]],
            	"columns": [
            	    { "orderable": false },
            	    null,
            	    null,
            	    null,
            	    null,
            	    null,
            	    null,
            	    null,
            	    null,
            	    null,
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
            });
            
            $('#headerNav a').click(function(e) {
    			$('#headerNav a').removeClass('active');
    			$(this).addClass('active');
    		});
        });
    </script>

</body>
</html>