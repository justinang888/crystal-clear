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
	            <div class="panel panel-default">
	                <div class="panel-heading">
	                    Search Customer
	                </div>
	                <div class="panel-body">
	                    <form action="">
	                        <div class="form-group">
	                            <label for="email">Customer Name:</label>
	                            <input type="text" class="form-control" id="customername">
	                          </div>
	                          <div class="form-group">
	                            <label for="pwd">Delivery Receipt Number:</label>
	                            <input type="text" class="form-control" id="drnumber">
	                          </div>
	                          <button type="submit" class="btn btn-default">Submit</button>
	                    </form>
	                </div>
	            </div>
	        </div>
	    </div>
    </div>
    
    <div class="container">
	    <div class="row">
	        <div class="col-lg-12">
	            <div class="panel panel-default">
	                <div class="panel-heading">
	                    Search Results
	                </div>
	                <div class="panel-body">
	                    <table id="myTable" class="table table-striped table-bordered table-hover">
	                        <thead>
	                          <tr>
	                            <th>Customer Name</th>
	                            <th>Address</th>
	                            <th>Contact Number</th>
	                            <th>Contact Person</th>
	                            <th>Actions</th>
	                          </tr>
	                        </thead>
	                        <tbody>
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
            $('#myTable').DataTable()
        })
    </script>

</body>
</html>
