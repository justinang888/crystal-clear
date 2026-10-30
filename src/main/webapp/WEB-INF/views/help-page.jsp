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
            <div class="panel panel-info">
                <div class="panel-heading clearfix">
                    <p class="pull-left" style="font-size: 20px"><b>Coming soon!</b></p>
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