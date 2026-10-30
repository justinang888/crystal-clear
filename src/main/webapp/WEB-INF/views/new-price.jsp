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
                        <spring:url value="/orders/neworder" var="urlNewOrder" />
                        Add new product price for ${customer.name}
                        <a href="${urlNewOrder}?customerId=${customer.customerId}" class="btn btn-default pull-right" role="button">New Order</a>
                    </div>
                    <div class="panel-body">
                        <spring:url value="/prices/save" var="savePriceUrl"/>
                        <form:form class="form-horizontal" method="post" modelAttribute="price" action="${savePriceUrl}">
                            <form:hidden path="customerId" />
                            <div class="form-group">
                                <label class="col-sm-3 control-label">Customer name: </label>
                                <div class="col-sm-9">
                                    <input type="text" class="form-control" id="name" placeholder="${customer.name}" disabled="disabled"/>
                                </div>
                            </div>
                            <spring:bind path="productId">
                                <div class="form-group ${status.error ? 'has-error' : ''}">
                                    <label class="col-sm-3 control-label">Product: </label>
                                    <div class="col-sm-9">
                                        <form:select path="productId" class="form-control" id="productId">
                                            <form:options items="${products}" itemValue="productId" itemLabel="name" />    
                                        </form:select>
                                        <form:errors path="productId" class="control-label"/>
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
		                      	    <button type="submit" class="btn btn-primary">Set Price</button>
		                        </div>
		                    </div>
                        </form:form>
                    </div>
                </div>
            </div>
        </div>
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
                    <div class="panel-heading">
                        List of Products and prices for customer: ${customer.name}
                    </div>
                    <div class="panel-body">
                        <table id="productsTable" class="table table-striped table-bordered table-hover">
                            <thead>
                                <tr>
                                    <th>Product name</th>
                                    <th>Selling Price</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="price" items="${prices}">
                                    <tr>
                                        <td>${price.product.name}</td>
                                        <td>${price.sellingPrice}</td>
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
    
</body>
</html>