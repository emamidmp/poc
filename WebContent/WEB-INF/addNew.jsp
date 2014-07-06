
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org   /TR/html4/loose.dtd">
<html>
<img src="images/zandu.jpeg" alt="Emami Logo" align="center" height="42" width="102">
  <h1>New Patient Registration </h1>

<div style="width:100%; height:3px; margin:10px 0px 10px; background-color:#bbbbbb">
<br>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Insert title here</title>
    </head>
<b>
<p>This is a new customer </p>
<p>Patient Calling From : ${phoneNum}</p>
</b>
    <body>
        <form action="EmamiDemo" method="post">
            <p><strong>Patient's Name: </strong>
            <input type="text" name="Name"/><br>
            <p><strong>Patient's Location: </strong>
            <input type="text" name="Location"><br>
            <p><strong>Patient's Disease: </strong>
            <input type="text" name="Disease"><br>
            <p><strong>Patient's Weight: </strong>
            <input type="text" name="Weight"><br>
            <p><strong>Patient's Mail Id: </strong>
            <input type="text" name="Mail"><br><br><br>
            <input type="hidden" name="PhoneNum" value=${phoneNum}>
            <input type="submit" value="submit"/><br>
        </form>
    </body>
</html>

