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
    <div class="container" style="width: 800px">
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-info">
                    <div class="panel-heading clearfix">
                        <spring:url value="/container/listAll" var="urlContainerBalances" />
                        Total Balance of Borrowed Containers as of <span class="label label-danger">${dateToday}</span>
                        <a href="${urlContainerBalances}?mode=preview" class="btn btn-default pull-right" role="button">Print Preview</a>
                    </div>
                    <div class="panel-body">
                        <table id="containersTable" class="table table-striped table-bordered table-hover">
                            <thead>
                                <tr>
                                    <th>Customer</th>
                                    <th width="50">Round Total Balance</th>
                                    <th width="50">Slim Total Balance</th>
                                    <th width="50">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${containersList}">
                                    <spring:url value="/customers/view?customerId=${c.customerId}" var="urlViewCustomer" />
                                    <spring:url value="/container/return?cid=${c.customerId}" var="urlReturnContainer" />
                                    <tr>
                                        <td><a href="${urlViewCustomer}">${c.name}</a></td>
                                        <td>${c.totalRound}</td>
                                        <td>${c.totalSlim}</td>
                                        <td>
                                            <a href="${urlReturnContainer}" style="text-align: center;" class="btn btn-default" role="button">Return</a>
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
    
    <div class="container" style="width: 800px">
    	<div class="row">
    		<div class="col-lg-12">
    			<div class="panel panel-info">
                	<div class="panel-heading clearfix">
						Running Total
                	</div>
                	
                	 <div class="panel-body">
                	 	<table id="myTable" class="table table-striped table-bordered table-hover">
                	 		<thead>
	                          <tr>
	                            <th>Total Round</th>
	                            <th>Total Slim</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                        	<tr>
	                        		<td>${runningRound}</td>
	                        		<td>${runningSlim}</td>
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
            $('#containersTable').DataTable({
                fixedHeader: true,
                paging: false,
                scrollY: 350,
                scrollCollapse: true
            })
        })
    </script>
</body>
</html>