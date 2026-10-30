<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="fragments/header.jsp" />
    <title>Search</title>
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
                            <label for="customername">Customer Name:</label>
                            <input type="text" class="form-control" id="customername">
                          </div>
                          <div class="form-group">
                            <label for="drnumber">Delivery Receipt Number:</label>
                            <input type="text" class="form-control" id="drnumber">
                          </div>
                          <button type="submit" class="btn btn-default">Submit</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
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
    
    <script type="text/javascript">
        $(document).ready(() => {
            $('#myTable').DataTable()
        })
    </script>

</body>
</html>
