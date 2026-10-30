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

		<div class="panel panel-info">
			<div class="panel-body">
                <spring:url value="/customers/listactive" var="urlListCustomersMonthlyActive" />
				<form class="form-horizontal" method="get"
					action="${urlListCustomersMonthlyActive}">
					<div class="form-group col-sm-6">
						<div class="col-sm-6 input-append date" id="datepicker"
							data-date="00-2022" data-date-format="mm-yyyy">
							<input class="form-control" type="text" readonly="readonly"
								name="d" placeholder="select month"> <span
								class="glyphicon glyphicon-calendar add-on"
								style="padding-top: 5px;"></span>
						</div>
						<div class="col-sm-6">
							<button type="submit" id="generateMonthlyActiveReport" class="btn btn-primary">Generate
								Report</button>
						</div>
					</div>
				</form>
			</div>
		</div>

		<div class="row">
        <div class="col-lg-12">
            <div class="panel panel-info">
            	<div class="panel-heading clearfix">
                    <h2>Active customers for ${monthYear}</h2>
                    <a href="${urlListCustomersMonthlyActive}?d=${dateParam}&mode=preview" class="btn btn-primary pull-right" id="printMonthlyActiveReport" role="button">Print Preview</a>
                </div>
                <div class="panel-body">
                    <table id="myTable" class="table table-striped table-bordered table-hover">
                        <thead>
                          <tr>
                            <th>Type</th>
                            <th>Customer Name</th>
                            <th>Address</th>
                            <th>Contact Number</th>
<!--                             <th style="padding: 8px;">Order Count</th> -->
                            <th width="100">Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="co" items="${cwoList}">
                                <tr>
                                    <td>${co.type}</td>
                                    <td>${co.name}</td>
                                    <td>${co.address}</td>
                                    <td>${co.mainNumber}</td>
<%--                                     <td>${co.numberOfOrdersForTheMonth}</td> --%>
                                    <td nowrap>
<!-- 										<p><button class="btn btn-default">New Order</button></p>  -->
										<!--                                         <button class="btn btn-info">View</button> -->
										
										<form action="view" method="get" style="display: inline-block;" >
											<%-- <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /> --%> 
											<input type="hidden" name=customerId value="${co.customerId}"> 
											<input class="btn btn-info" type="submit" value="View">
										</form>
										<spring:url value="/orders/neworder" var="newOrderUrl"/>
										<form action="${newOrderUrl}" method="get" style="display: inline-block;">
											<%-- <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /> --%> 
											<input type="hidden" name="customerId" value="${co.customerId}"/> 
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
            	    { "orderable": false }
            	  ]
            	
            });
            
            $('#headerNav a').click(function(e) {
    			$('#headerNav a').removeClass('active');
    			$(this).addClass('active');
    		});
            
            $("#datepicker").datepicker( {
                format: "mm-yyyy",
                viewMode: "months", 
                minViewMode: "months"
            });
        });
    </script>

</body>
</html>