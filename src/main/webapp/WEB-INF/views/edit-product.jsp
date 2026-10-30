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
                        Add new product
                    </div>
                    <div class="panel-body">
                        <spring:url value="/products/save" var="saveProductUrl"/>
                        <form:form class="form-horizontal" method="post" modelAttribute="product" action="${saveProductUrl}">
                            <form:input path="productId" type="hidden"/>
                            <form:input path="createdAt" type="hidden"/>
                            <spring:bind path="name">
                                <div class="form-group ${status.error ? 'has-error' : ''}">
                                    <label class="col-sm-3 control-label">Product name: </label>
                                    <div class="col-sm-9">
                                        <form:input path="name" type="text" class="form-control" id="name" placeholder="Product name"/>
                                        <form:errors path="name" class="control-label"/>
                                    </div>
                                </div>
                            </spring:bind>
                            <spring:bind path="description">
                                <div class="form-group ${status.error ? 'has-error' : ''}">
                                    <label class="col-sm-3 control-label">Description: </label>
                                    <div class="col-sm-9">
                                        <form:input path="description" type="text" class="form-control" id="description" placeholder="Description"/>
                                        <form:errors path="description" class="control-label"/>
                                    </div>
                                </div>
                            </spring:bind>
                            <spring:bind path="sellingPrice">
                                <div class="form-group ${status.error ? 'has-error' : ''}">
                                    <label class="col-sm-3 control-label">Selling price in Php: </label>
                                    <div class="col-sm-9">
                                        <form:input path="sellingPrice" type="text" class="form-control" id="sellingPrice" placeholder="0.00"/>
                                        <form:errors path="sellingPrice" class="control-label"/>
                                    </div>
                                </div>
                            </spring:bind>
                            <div class="form-group">
		                        <div class="col-sm-offset-3 col-sm-9">
		                      	    <button type="submit" class="btn btn-primary">Save Product</button>
		                      	    <spring:url value="/products/list" var="listProductUrl"/>
		                      	    <a href="${listProductUrl}" class="btn btn-primary">Cancel</a>
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