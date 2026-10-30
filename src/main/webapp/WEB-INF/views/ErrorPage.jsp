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
		             <div class="panel-heading">
		                  Error Encountered
		             </div>
		             <div class="panel-body">
		              	<p>
							Sorry! The system encountered an error. Please contact the developers for assistance.
						</p>
		             </div>
	       		</div>
	       	</div>
	     </div>
    </div>
    
</body>
</html>