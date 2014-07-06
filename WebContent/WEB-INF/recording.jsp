<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<img src="images/zandu.jpeg" alt="Emami Logo" align="center" height="42" width="102">
  <h1>Consultation Recordings </h1>

<div style="width:100%; height:3px; margin:10px 0px 10px; background-color:#bbbbbb">

<head>
</head>
<body>
<br>
<p>Patient Name::${name}</p>
<p>Patient Location::${location}</p>
<p>Patient Disease::${disease}</p>
<p>Patient Vitals::${vitals}</p>
<p>Patient MailId::${mailid}</p>

<c:choose>
  <c:when test="${empty recording}">
      <p>Recording Link::Recording link preparation in progress, kindly wait for a few minutes!</p>
  </c:when>
  <c:otherwise>
      <p>Recording Link::<a href='${recording}'>${recording}</a></p>
      <p><a href='${recording1}'>${recording1}</a></p>
      <p><a href='${recording2}'>${recording2}</a></p>
   </c:otherwise>
</c:choose>

</body>
</html>

