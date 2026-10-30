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
    <div class="container" style="width: 600px">
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-info">
                    <div class="panel-heading">
                        Total Balance of Borrowed Containers as of <span class="label label-danger">${dateToday}</span>
                    </div>
                    <div class="panel-body">
                        <table id="containersTable" class="table table-striped table-bordered table-hover" style="font-size: 10px;">
                            <thead>
                                <tr>
                                    <th style="padding: 8px;">Customer</th>
                                    <th style="padding: 8px;" width="80">Round Total Balance</th>
                                    <th style="padding: 8px;" width="80">Slim Total Balance</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${containersList}">
                                    <tr>
                                        <td style="padding: 8px;">${c.name}</td>
                                        <td style="padding: 8px;">${c.totalRound}</td>
                                        <td style="padding: 8px;">${c.totalSlim}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <div class="container" style="width: 600px">
    	<div class="row">
    		<div class="col-lg-12">
    			<div class="panel panel-info">
                	<div class="panel-heading clearfix">
						Running Total
                	</div>
                	
                	 <div class="panel-body">
                	 	<table id="myTable" class="table table-striped table-bordered table-hover" style="font-size: 10px;">
                	 		<thead>
	                          <tr>
	                            <th style="padding: 8px;">Total Round</th>
	                            <th style="padding: 8px;">Total Slim</th>
	                          </tr>
	                        </thead>
	                        <tbody>
	                        	<tr>
	                        		<td style="padding: 8px;">${runningRound}</td>
	                        		<td style="padding: 8px;">${runningSlim}</td>
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
            	paging: false,
                searching: false
            })
        })
    </script>
</body>
</html>