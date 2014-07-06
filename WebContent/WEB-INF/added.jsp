<html>
<body>
<img src="images/zandu.jpeg" alt="Emami Logo" align="center" height="42" width="102">
<div style="width:100%; height:3px; margin:10px 0px 10px; background-color:#bbbbbb">
<br>

<b>
<p> Details for customer ${custName} has been successfully added in the system</p>
<p>Message has been sent with details to ${phoneNum}</p>
<p> Click to book an appointment for this patient. </p>
</b>
       <input type="button" value="Book Appointment" name="bookAppointment" onclick="window.location.href='http://54.255.204.71:8080/EmamiDemo/EmamiDemo?phoneNumber=' + ${phoneNum};"/>


<html>
<body>
