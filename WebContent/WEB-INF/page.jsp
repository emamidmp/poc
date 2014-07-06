<html>
<head>
<style>
br { page-break-after: always}
</style>

<img src="images/zandu.jpeg" alt="Emami Logo" align="center" height="42" width="102"> 
  <h1>Patient Details</h1>

<div style="width:100%; height:3px; margin:10px 0px 10px; background-color:#bbbbbb">
<br>
<b>
<p><font face="Digital, Arial, Helvetica, sans-serif" size="3" color="black">Call coming from             : ${phoneNum}</p>
<p>Name     : ${patientName}</p>
<p>Location : ${patientLocation}</p>
<p>Disease  : ${patientDisease}</p>
<p>Weight   : ${patientVitals}</p>
</b>
<div style="width:100%; height:3px; margin:10px 0px 10px; background-color:#bbbbbb">
<br>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Schedule Appointment </title>
</head>
 <body>
   <form name="EmamiDemo" method="get">

       <h3>Choose the mode of appointment</h3>
       <input type="button" value="Face to face" name="faceToFace" onclick="window.location.href='http://54.255.204.71:8080/EmamiDemo/EmamiDemo?faceToFace=123&phoneNumber=' + ${phoneNum};"/>
       <input type="button" value="Video Consultation" name="video" onclick="window.location.href='http://54.255.204.71:8080/EmamiDemo/EmamiDemo?video=123&phoneNumber=${phoneNum}'"/>

    </form>

<br>

</body>
</html>

