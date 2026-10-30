<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ page session="false" %>
<html>
<head>
	<title>System User Management</title>
	<style type="text/css">
		.tg  {border-collapse:collapse;border-spacing:0;border-color:#ccc;}
		.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#ccc;color:#333;background-color:#fff;}
		.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#ccc;color:#333;background-color:#f0f0f0;}
		.tg .tg-4eph{background-color:#f9f9f9}
	</style>
</head>
<body>
<c:if test="${pageContext.request.userPrincipal.name != null}">
    <h2>Welcome user: ${pageContext.request.userPrincipal.name}</h2> 
</c:if>
<h1>
	Add a System User
</h1>

<c:url var="formActionUrl" value="${formAction}" ></c:url>
<form:form action="${formActionUrl}" modelAttribute="user">
<table>
	<tr>
		<td>
			<form:label path="username">
				<spring:message text="Username"/>
			</form:label>
		</td>
		<td>
			<form:input path="username" />
		</td> 
	</tr>
	<tr>
		<td>
			<form:label path="password">
				<spring:message text="Password"/>
			</form:label>
		</td>
		<td>
			<form:password path="password" />
		</td>
	</tr>
	<tr>
		<td>
			<form:label path="enabled">
				<spring:message text="Active"/>
			</form:label>
		</td>
		<td>
			<form:checkbox path="enabled" id="enabled" />
		</td>
	</tr>
	<tr>
		<td>
			<form:label path="roles">
				<spring:message text="Select Roles"/>
			</form:label>
		</td>
		<td>
			<form:select path="roles" items="${user.roles}" multiple="true" size="5" class="form-control" />
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<c:if test="${!empty user.username}">
				<input type="submit"
					value="<spring:message text="Update User"/>" />
			</c:if>
			<c:if test="${empty user.username}">
				<input type="submit"
					value="<spring:message text="Add User"/>" />
			</c:if>
		</td>
	</tr>
</table>
<form:hidden path="enabled"/>
</form:form>
<br>
<h3>List of System Users</h3>
<c:if test="${!empty users}">
	<table class="tg">
	<tr>
		<th width="80">Active</th>
		<th width="120">Username</th>
		<th width="120">Roles</th>
		<th width="60">Edit</th>
		<th width="60">Delete</th>
	</tr>
	<c:forEach items="${users}" var="person">
		<tr>
			<td>${person.enabled}</td>
			<td>${person.username}</td>
			<td><ul>
			<c:forEach var="role" items="${person.userAuthority}">
			    <li>${role.authority}</li>
			</c:forEach>
			</ul></td>
			<td><a href="<c:url value='/user/edit/${person.username}' />" >Edit</a></td>
			<td><a href="<c:url value='/user/delete/${person.username}' />" >Delete</a></td>
		</tr>
	</c:forEach>
	</table>
</c:if>
</body>
</html>
