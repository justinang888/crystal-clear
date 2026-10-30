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
                        <spring:url value="/expenses/list" var="listExpensesUrl"/>
                        <form class="form-horizontal" method="get" action="${listExpensesUrl}">
                            <div class="form-group">
                                <label class="col-sm-1 control-label">Date: </label>
                                <div class="col-sm-3">
                                    <input type="date" class="form-control" name="d" placeholder="Date"/>
                                </div>
   								<div class="col-sm-5">
								    <button type="submit" class="btn btn-primary">List Expenses</button>
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
                    <div class="panel-heading" style="padding-bottom: 20px;">
                        <p style="margin-bottom: 0px;">List of expenses for ${date}.</p>
                        <spring:url value="/expenses/new" var="newExpenseUrl"/>
                            <a href="${newExpenseUrl}" class="btn btn-default pull-right" role="button">Add Expense</a>
                        <div class="label label-danger" >Running Total: ${expensesTotal}</div>
                    </div>
                    <div class="panel-body">
                        <table id="expensesTable" class="table table-striped table-bordered table-hover">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Description</th>
                                    <th>Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="exp" items="${expenses}">
                                    <tr>
                                        <td>${exp.createdAt}</td>
                                        <td>${exp.description}</td>
                                        <td>${exp.amount}</td>
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
            $('#expensesTable').DataTable({
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