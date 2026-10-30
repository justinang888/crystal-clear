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
                        Return Container/s for: ${customer.name}
                    </div>
                    <div class="panel-body">
                        <spring:url value="/container/save" var="saveContainerUrl"/>
                        <form:form class="form-horizontal" method="post" modelAttribute="container" action="${saveContainerUrl}">
                            <form:hidden path="containerId"/>
                            <form:hidden path="customerId"/>
                            <form:hidden path="status"/>
                            <spring:bind path="slimCount">
                                <div class="form-group ${status.error ? 'has-error' : ''}">
                                    <label class="col-sm-3 control-label"># of Slim Containers: </label>
                                    <div class="col-sm-9">
                                        <form:input path="slimCount" type="text" class="form-control" id="slimCount" placeholder="How many slim containers will be returned?"/>
                                        <form:errors path="slimCount" class="control-label"/>
                                    </div>
                                </div>
                            </spring:bind>
                            
                            <spring:bind path="roundCount">
                                <div class="form-group ${status.error ? 'has-error' : ''}">
                                    <label class="col-sm-3 control-label"># of Round Containers: </label>
                                    <div class="col-sm-9">
                                        <form:input path="roundCount" type="text" class="form-control" id="roundCount" placeholder="How many round containers will be returned?"/>
                                        <form:errors path="roundCount" class="control-label"/>
                                    </div>
                                </div>
                            </spring:bind>

                            <div class="form-group">
		                        <div class="col-sm-offset-3 col-sm-9">
		                      	    <button type="submit" class="btn btn-primary">Return</button>
		                      	     <spring:url value="/customers/view?customerId=${container.customerId}" var="viewCustomerUrl"/>
                            		<a href="${viewCustomerUrl}" class="btn btn-primary">Cancel</a>
		                        </div>
		                    </div>
                        </form:form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="fragments/footer.jsp" />
</body>
</html>