<p>conference id : ${confid}</p>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title></title>
      <script type="text/javascript" src="https://1click.io/static/js/jquery.min.js"></script>
    <script type="text/javascript" src="https://1click.io/static/js/adapter.js"></script>
    <script type="text/javascript" src="https://1click.io/static/js/oks.js"></script>
     <script type="text/javascript">
         //The virtual room title. This room is allocated using the REST API documented below.
         var identity = ${confid};
    </script>
</head>
<body>
        <script type = "text/javascript">

        function show_from() {
            document.getElementById("record-session").style.display = "block";
        }

        function start_record_session() {
            var record_name = document.getElementById("record_name").value;
            record(record_name);
        }

        </script>

        <video id="remoteView" autoplay></video>
        <button class="" onclick="video_call();"> Video </button>
        <button class="" onclick="hang_up(1);"> End Call </button>
        <button class="" onclick="voice_call();"> Audio </button>
        <button id="record" onclick="show_from()"> Record </button>

        <div id = "record-session" style="display:none">
        <input type="text" id="record_name">
        <br>
        <button id="start_record" onclick="start_record_session()"> submit </button>
        </div>

<br>
</body>
</html>

