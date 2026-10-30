<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/" var="urlHome" />
<spring:url value="/search" var="urlSearch" />
<spring:url value="/orders/list" var="urlOrders" />
<spring:url value="/reports" var="urlReports" />
<spring:url value="/customers/list" var="urlCustomers" />
<spring:url value="/customers/duedates" var="urlDueDates" />
<spring:url value="/customers/dailyduedates" var="urlDailyDueDates" />
<spring:url value="/debts/listAll" var="urlDebt" />
<spring:url value="/orders/daily?d=today" var="urlDaily" />
<spring:url value="/orders/daily?d=today" var="urlDaily" />
<spring:url value="/orders/monthly?d=today" var="urlMonthly" />
<spring:url value="/container/listAll" var="urlContainerBalance" />
<spring:url value="/container/history?d=today" var="urlContainerHistory" />
<spring:url value="/help" var="urlHelp" />
<spring:url value="/expenses/list?d=today" var="urlListExpenses" />
<spring:url value="/customers/listinactive" var="urlInactive" />
<spring:url value="/products/list" var="urlListProducts"/>
<spring:url value="/dashboard" var="urlDashboard"/>
<spring:url value="/customers/listactive?d=today" var="urlMonthlyActiveCustomers" />

<div class="loader"></div>
<nav class="navbar navbar-inverse">
  <div class="container">
    <div class="navbar-header">
      <a class="navbar-brand" href="${urlDashboard}">Crystal Clear</a>
    </div>
    <div id="navbar">
      <ul class="nav navbar-nav navbar-right" id="headerNav">
        <li><a href="${urlCustomers}">Customers</a></li>
        <li><a href="${urlOrders}">Orders</a></li>
        <li><a href="${urlListProducts}">Products</a></li>
        <li><a href="${urlListExpenses}">Expenses</a></li>
        <li><a href="${urlDebt}" id="arSummaryLink">A/R Summary</a></li>
        <li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="">Reports</a>
          <ul class="dropdown-menu">
            <li><a href="${urlDaily}">Daily Sales</a></li>
            <li><a href="${urlMonthly}" id="monthlySummaryLink">Monthly Summary</a></li>
            <li><a href="${urlDueDates}">Due Dates Summary</a></li>
            <li><a href="${urlDailyDueDates}" id="dailyDueLink">Daily Due Dates</a></li>
            <li><a href="${urlInactive}">Inactive Customers</a></li>
			<li><a href="${urlMonthlyActiveCustomers}" id="monthlyActiveLink">Monthly Active Customers</a></li>
          </ul>
        </li>
        <li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="">Containers</a>
          <ul class="dropdown-menu">
            <li><a href="${urlContainerBalance}">Total Balances</a></li>
            <li><a href="${urlContainerHistory}">History</a></li>
          </ul>
        </li>
<!--        <li class="dropdown"> -->
<!--          <a href="#">Reports</a> -->
<!--          <span class="caret"></span> -->
<!--          <ul class="dropdown-menu"> -->
<%--                  <li><a href="${urlDaily}">Daily Sales</a></li> --%>
<%--                  <li><a href="${urlMonthly}">Monthly Sales</a></li> --%>
<%--                  <li><a href="${urlSearch}">Containers Summary</a></li> --%>
<!--              </ul> -->
<!--        </li> -->
      </ul>
    </div>
  </div>
</nav>