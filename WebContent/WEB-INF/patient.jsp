<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<img src="images/zandu.jpeg" alt="Emami Logo" align="center" height="42" width="102">
  <h1>Patient Details</h1>

<div style="width:100%; height:3px; margin:10px 0px 10px; background-color:#bbbbbb">
<br>

<head>
    <title></title>
      <script type="text/javascript" src="https://1click.io/static/js/jquery.min.js"></script>
    <script type="text/javascript" src="https://1click.io/static/js/adapter.js"></script>
    <script type="text/javascript" src="https://1click.io/static/js/oks.js"></script>
     <script type="text/javascript">
         //The virtual room title. This room is allocated using the REST API documented below.
         //var identity = "scanbuild040603";
         //var identity = "1402479910305";
         var identity = "${confid}";
    </script>
</head>
<body>

<p>Patient Name::${name}</p>
<p>Patient Location::${location}</p>
<p>Patient Disease::${disease}</p>
<p>Patient Vitals::${vitals}</p>
<p>Patient MailId::${mailid}</p>

        <script type = "text/javascript">

        function show_from() {
            document.getElementById("record-session").style.display = "block";
        }

        function start_record_session() {
            var record_name = document.getElementById("record_name").value;
            record(record_name);
        }
        </script>

<c:choose>
  <c:when test="${empty confid}">
  <p>Consultation Link::No consultation scheduled for this patient.</p>
  </c:when>
  <c:otherwise>

        <video id="remoteView" autoplay></video>
        <button class="" onclick="video_call();"> Video </button>
        <button class="" onclick="hang_up(1);"> End Call </button>
        <button id="record" onclick="show_from()"> Record </button>
        <button id="view" onclick="window.location.href='http://54.255.204.71:8080/EmamiDemo/EmamiDemo?viewRecording=yes&phoneNumber=${phoneNum}'"> View </button>

        <div id = "record-session" style="display:none">
        <input type="text" id="record_name">
        <br>
        <button id="start_record" onclick="start_record_session()"> submit </button>
        </div>

  </c:otherwise>
  </c:choose>
<br>
</body>
</html>
