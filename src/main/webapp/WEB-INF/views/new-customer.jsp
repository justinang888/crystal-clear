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
                    Create New Customer
                </div>
                <div class="panel-body">
                    <spring:url value="/customers/save" var="saveCustomerUrl"/>
                    <form:form class="form-horizontal" action="${saveCustomerUrl}" method="post" modelAttribute="customerForm">
                        <form:hidden path="customerId"/>
                        <spring:bind path="name">
                            <div class="form-group ${status.error ? 'has-error' : ''}">
                                <label class="col-sm-3 control-label" for="name">Customer name:</label>
                                <div class="col-sm-9">
                                    <form:input path="name" type="text" class="form-control" id="name" placeholder="Customer name" />
                                    <form:errors path="name" class="control-label"/>
                                </div>
                            </div>
                        </spring:bind>
                        <spring:bind path="type">
                            <div class="form-group">
                                <label class="col-sm-3 control-label" for="type">Customer type:</label>
                                <div class="col-sm-9">
                                    <form:radiobuttons path="type" items="${types}"/>
                                </div>
                            </div>
                        </spring:bind>
                        <spring:bind path="address">
                            <div class="form-group ${status.error ? 'has-error' : ''}">
                                <label class="col-sm-3 control-label">Address: </label>
                                <div class="col-sm-9">
                                    <form:input path="address" type="text" class="form-control" id="address" placeholder="Address"/>
                                    <form:errors path="address" class="control-label"/>
                                </div>
                            </div>
                        </spring:bind>
                        <spring:bind path="mainNumber">
                            <div class="form-group ${status.error ? 'has-error' : ''}">
                                <label class="col-sm-3 control-label">Main number: </label>
                                <div class="col-sm-9">
                                    <form:input path="mainNumber" type="text" class="form-control" id="mainNumber" placeholder="Main number"/>
                                    <form:errors path="mainNumber" class="control-label"/>
                                </div>
                            </div>
                        </spring:bind>
                        <spring:bind path="contactName">
                            <div class="form-group ${status.error ? 'has-error' : ''}">
                                <label class="col-sm-3 control-label">Contact person: </label>
                                <div class="col-sm-9">
                                    <form:input path="contactName" type="text" class="form-control" id="contactName" placeholder="Contact person"/>
                                    <form:errors path="contactName" class="control-label"/>
                                </div>
                            </div>
                        </spring:bind>
                        <spring:bind path="alternateNumber">
                            <div class="form-group ${status.error ? 'has-error' : ''}">
                                <label class="col-sm-3 control-label">Alternate number: </label>
                                <div class="col-sm-9">
                                    <form:input path="alternateNumber" type="text" class="form-control" id="alternateNumber" placeholder="Alternate number"/>
                                    <form:errors path="alternateNumber" class="control-label"/>
                                </div>
                            </div>
                        </spring:bind>
                        <spring:bind path="emailAddress">
                            <div class="form-group ${status.error ? 'has-error' : ''}">
                                <label class="col-sm-3 control-label">Email address: </label>
                                <div class="col-sm-9">
                                    <form:input path="emailAddress" type="email" class="form-control" id="emailAddress" placeholder="Email address"/>
                                    <form:errors path="emailAddress" class="control-label"/>
                                </div>
                            </div>
                        </spring:bind>
                        <spring:bind path="orderInterval">
                            <div class="form-group ${status.error ? 'has-error' : ''}">
                                <label class="col-sm-3 control-label">Order Interval: </label>
                                <div class="col-sm-9">
                                    <form:input path="orderInterval" type="text" class="form-control" id="orderInterval" placeholder="Number of days"/>
                                    <form:errors path="orderInterval" class="control-label"/>
                                </div>
                            </div>
                        </spring:bind>
                        <div class="col-sm-offset-3 col-sm-9">
                        	<button onclick="this.disabled=true;this.value='Submitting...'; this.form.submit();" type="submit" class="btn btn-primary">Submit</button>
                        	<spring:url value="/customers/list" var="listCustomersUrl"/>
                            <a href="${listCustomersUrl}" class="btn btn-primary">Cancel</a>
                        </div>
                    </form:form>
                </div>
            </div>
        </div>
    </div>
    
    
<!--     <div class="row"> -->
<!--         <div class="col-lg-12"> -->
<!--             <div class="panel panel-default"> -->
<!--                 <div class="panel-heading"> -->
<!--                     Search Results -->
<!--                 </div> -->
<!--                 <div class="panel-body"> -->
<!--                     <table id="myTable" class="table table-striped table-bordered table-hover"> -->
<!--                         <thead> -->
<!--                           <tr> -->
<!--                             <th>Customer Name</th> -->
<!--                             <th>Address</th> -->
<!--                             <th>Contact Number</th> -->
<!--                             <th>Contact Person</th> -->
<!--                             <th>Actions</th> -->
<!--                           </tr> -->
<!--                         </thead> -->
<!--                         <tbody> -->
<!--                         </tbody> -->
<!--                     </table> -->
<!--                 </div> -->
<!--             </div> -->
<!--         </div> -->
<!--     </div> -->
    </div>

    <jsp:include page="fragments/footer.jsp" />
    
    <script type="text/javascript">
        $(document).ready(() => {
            $('#myTable').DataTable()
        })
    </script>

</body>
</html>