var localVideo;
var testVideo;
var remoteView;
var active1;
var activediv;
var localStream;
var remoteStream;
var channel;
var channelReady = false;
var conf_id; 
var pc;
var socket;
var started = false;
var from_tag = makeid();
var call_id = makeid(); 
var IDLE = 0;
var OFFER_MADE = 1;
var ANSWERED = 2;
var HANGUP = 3;
var TEST = 4;
var RECORD = 5;
var record_admin_email = "";
var record_msg = "";
var call_state = IDLE;
var test_state = IDLE;
var NONE = 0;
var VIDEO = 1;
var VOICE = 2;
var AUDIENCE = 3;
var LISTEN = 4;
var mode = VIDEO;
//role variable should come via db
var role = window.role;
var video_constraints  = { "mandatory":{"maxWidth": "640", "maxHeight":"480", "maxFrameRate":"15"} , "optional": []}
// Set up audio and video regardless of what devices are present.
var sdp_constraints = {'mandatory': {
                      'OfferToReceiveAudio':true, 
                      'OfferToReceiveVideo':true }};
var isVideoMuted = false;
var isAudioMuted = false;
var socket_connection_wait = 10;
var camera_failure = false;

function makeid()
{
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for( var i=0; i < 5; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}

function log_msg(msg) {
    var date = new Date();
    console.log(date.getTime() + " " + msg);
}

function initialize() {
    log_msg("initialize loaded");
    card = document.getElementById("webcam");
    localVideo = document.getElementById("selfView");
    testVideo = document.getElementById("testView");
    remoteView = document.getElementById("remoteView");
    activediv = document.getElementById("activeparticipant");
    active1 = document.getElementById("active1");
    //resetStatus();
    openChannel();
   
    //do_get_user_media(video_constraints, test_view_setup);
    //do_get_user_media(video_constraints, video_call);
    //console
}

function openChannel() {
    log_msg("Opening channel.");
    socket = new WebSocket('wss://oneklikstreet.com/join_conf/'+conf_id);
    socket.binaryType = 'arraybuffer';
    socket.onopen = socketOpen;
    socket.onclose = socketClose;
    socket.onerror = socketError;
    socket.onmessage = socketRecv;
}

function socketPing(){
    var ping = JSON.stringify({"method": "PING"})
    socketSend(ping);
}

function socketOpen() {
    socket.active = 1;
    log_msg('web socket connection is successful\n');
    //30 second heartbeat 
    setInterval(function(){socketPing()}, 30000);
}

function socketError(err) {
    socket.active = 0;
    log_msg('web socket connection error'+err);
}

function socketRecv(msg) {
    processSignalingMessage(msg.data);
}

function socketClose(msg) {
    socket.active = 0;
    log_msg('web socket is disconnected'+msg);
}

function socketSend(msg) {
    if(socket.active){
        log_msg('Browser -> Server' + msg);
        socket.send(msg);        
    } else {
        if(socket_connection_wait){
            socket_connection_wait -= 1;
            //waiting for connection to get ready
            setTimeout(function(){ log_msg("waiting for connection\n"); socketSend(msg);}, 1000);
            return;
        }
        log_msg(' Error: Lost connection with the service. Please check your network setttings.');
        alert(' Error: Lost connection with the service. Please check your network setttings.');
    }
}

function do_get_user_media(constraints, onUserMediaSuccess) {
    // Call into getUserMedia via the polyfill (adapter.js).
    try {
      getUserMedia({'audio':true, 'video':constraints}, onUserMediaSuccess,
                   onUserMediaError);
      log_msg("Requested access to local media with mediaConstraints:\n" +
                  "  \"" + JSON.stringify(constraints) + "\"");
    } catch (e) {
      log_msg("getUserMedia() failed. Is this a WebRTC capable browser?");
      log_msg("getUserMedia failed with exception: " + e.message);
      msg = " Error: The camera/microphone might not work on this browser as it lacks full HTML5 support. Please download and use the most secure and fastest browser: Google Chrome." ;
      call_events( msg + e.message);
      alert(msg);
    }
}

function call_events(msg){
    var events = JSON.stringify({"method": "EVENTS", "fromtag":from_tag, "callid":call_id, "confid":conf_id, "user-agent": "browser", "body": msg})
    socketSend(events);
}

function createPeerConnection() {
    // Force the use of a number IP STUN server for Firefox.
   var pc_constraints = {"optional":
                                [
                                    {'DtlsSrtpKeyAgreement': 'false'},
                                    {'googEnableSdesKeyAgreement': 'true'}
                                ]
                         };
    try {
      pc = new RTCPeerConnection(null, pc_constraints);
      pc.onicecandidate = onIceCandidate;
      log_msg("Created RTCPeerConnnection \n"); 
    } catch (e) {
      log_msg("Failed to create PeerConnection, exception: " + e.message);
      call_events("Failed to create PeerConnection, exception: " + e.message); 
      alert("Cannot create RTCPeerConnection object; WebRTC is not supported by this browser.");
      return;
    }

    pc.onaddstream = onRemoteStreamAdded;
    pc.onremovestream = onRemoteStreamRemoved;
}

function start_call() {
    // remoteView.style.zIndex = -1;
    // testView.style.zIndex = -2; 
    setStatus("Connecting...");
    log_msg("Creating PeerConnection.");
    createPeerConnection();
    log_msg("Adding local stream.");
    if(mode != LISTEN){
        pc.addStream(localStream);
    }
    started = true;
    pc.createOffer(sdpProcessing, onCreateSessionDescriptionError, sdp_constraints);
}

function onCreateSessionDescriptionError(error) {
  log_msg('Failed to create session description: ' + error.toString());
}

function sdpProcessing(sessionDescription){
    log_msg('original sdp\n'+sessionDescription.sdp);
    sessionDescription.sdp = client_sdp_check(sessionDescription.sdp);
    if(sessionDescription.sdp === null){
        log_msg('failed mic case, hence not sending a call.');
        return;
    } else {
        log_msg('sdp parsing is successful');
    }
    if (webrtcDetectedBrowser == "firefox") {
        var invite_msg = JSON.stringify({"method":"INVITE", "role": role, "fromtag":from_tag, "callid":call_id, "confid":conf_id, "user-agent": "browser", "sdp": sessionDescription.sdp});
        call_state = OFFER_MADE;
        socketSend(invite_msg);
    }
    // setLocalDesciption is the one who starts the ice
    //pc.setLocalDescription(sessionDescription);
    log_msg('setLocalDescription');
    pc.setLocalDescription(sessionDescription,
                           onSetSessionDescriptionSuccess,
                           onSetSessionDescriptionError);
}

function onSetSessionDescriptionSuccess() {
  log_msg('Set session description success.');
}

function onSetSessionDescriptionError(error) {
  log_msg('Failed to set session description: ' + error.toString());
}

function test_view_setup(stream) {
    log_msg("User has granted access to local media.");
    // remoteView.style.zIndex = -2;
    // testView.style.zIndex = -1; 

    // Call the polyfill wrapper to attach the media stream to this element.
    attachMediaStream(localVideo, stream);
    attachMediaStream(testVideo, stream);
    localStream = stream;
    test_state = TEST;
}

function call_view_setup(stream) {
    if (webrtcDetectedBrowser != "firefox"){
        var video_tracks = stream.getVideoTracks();
        var audio_tracks = stream.getAudioTracks();
        if(audio_tracks == ""){
            msg = "failed to get mic.";
            call_events(msg);
            alert(msg);
            return;
        }
        if(video_tracks == "" && mode == VIDEO) {
            msg = "Failed to get camera.";
            call_events(msg);
            if(camera_failure === false){
                alert(msg);
                camera_failure = true
            }
        }
    }
    log_msg("got both video and camera.");
    // Call the polyfill wrapper to attach the media stream to this element.
    //attachMediaStream(localVideo, stream);
    //attachMediaStream(remoteView, stream);
    localStream = stream;
    start_call();
}

function onUserMediaError(error) {
    log_msg("Failed to get access to local media. Error code was " + error.code);
    call_events("Failed to get access to local media. Error code was " + error.code);
    alert(" Error: Failed to get access to Camera/Mic. Error code was " + error.code + ".");
}

function incall_cleanup(call_mode){
        hang_up(0);
        call_state == IDLE;
        mode = call_mode;
        send_register();
}

function video_call(){
    // if (test_state != IDLE) {
    //     // localStream.stop();
    //     // localStream = null;
    //     // deletMediaStream(testView);
    //     // test_state = IDLE;
    // }
    // if previously hang_up or call to call transition
    if(call_state != IDLE || mode == NONE) {
        if(mode != VIDEO) {
            incall_cleanup(VIDEO)
        } else {
            console.log("already in video call");
        }
    } else {
        mode = VIDEO;
        if (webrtcDetectedBrowser == "firefox"){
            sdp_constraints = {'mandatory': {"MozDontOfferDataChannel":true, 'OfferToReceiveAudio':true, 'OfferToReceiveVideo':true }};
        } else {
            sdp_constraints = {'mandatory': { 'OfferToReceiveAudio':true, 'OfferToReceiveVideo':true }};
        }
        do_get_user_media(true, call_view_setup);
    }
}

function voice_call(){
    if(call_state != IDLE || mode == NONE) {
        if(mode != VOICE) {
            incall_cleanup(VOICE)
        } else {
            console.log("already in voice call");
        }
    } else {
        mode = VOICE;
        // do getUserMedia again with video: false
        // pc.createOffer with new sdp_constraints
        if(webrtcDetectedBrowser == "firefox"){
            sdp_constraints = {'mandatory': {"MozDontOfferDataChannel":true, 'OfferToReceiveAudio':true, 'OfferToReceiveVideo':false }};
        } else {
            sdp_constraints = {'mandatory': { 'OfferToReceiveAudio':true, 'OfferToReceiveVideo':false }};
        }
        do_get_user_media(false, call_view_setup);
    }
}

function audience_call(){
    if(call_state != IDLE || mode == NONE) {
        if(mode != AUDIENCE) {
            incall_cleanup(AUDIENCE)
        } else {
            console.log("already in audience call");
        }
    } else {
        mode = AUDIENCE;
        // do getUserMedia again with video: false
        if(webrtcDetectedBrowser == "firefox"){
            sdp_constraints = {'mandatory': {"MozDontOfferDataChannel":true, 'OfferToReceiveAudio':true, 'OfferToReceiveVideo':true }};
        } else {
            sdp_constraints = {'mandatory': { 'OfferToReceiveAudio':true, 'OfferToReceiveVideo':true }};
        }
        do_get_user_media(false, call_view_setup);
    }
}

function listener_call(){
    if(call_state != IDLE || mode == NONE) {
        if(mode != LISTEN) {
            incall_cleanup(LISTEN)
        } else {
            console.log("already in listen call");
        }
    } else {
        mode = LISTEN;
        // do getUserMedia again with video: false
        if(webrtcDetectedBrowser == "firefox"){
            sdp_constraints = {'mandatory': {"MozDontOfferDataChannel":true, 'OfferToReceiveAudio':true, 'OfferToReceiveVideo':true }};
        } else {
            sdp_constraints = {'mandatory': { 'OfferToReceiveAudio':true, 'OfferToReceiveVideo':true }};
        }
        start_call();
    }
}

function record(record_name) {
    record_admin_email = "" ; 
    var record_name_alter = record_name.replace(/ /g,"");
    if(record_name_alter == ""){
        msg = "please provide recording name for session";
        call_events(msg);
        alert(msg);
        return;
    }
    mode = RECORD;
    record_msg = JSON.stringify({"method":"RECORD", "fromtag":from_tag, "callid":call_id, "confid":conf_id, "user-agent": "browser", "record": record_name, "admin_email" :  record_admin_email});
    if (call_state != IDLE) {//call is already in progress
        socketSend(record_msg);
    } else {
        // career vita automatically calls record msg after some timeout
        // this can result in multiple get user media
        //sdp_constraints = {'mandatory': { 'OfferToReceiveAudio':true, 'OfferToReceiveVideo':true }};
        //do_get_user_media(true, call_view_setup);
    }
}

function hang_up(full) {
    if (call_state != IDLE && call_state != HANGUP) {
        log_msg("Hanging up.");
        if(full == 2) {
            var bye_msg = JSON.stringify({"method":"BYE", "fromtag":from_tag, "callid":call_id, "confid":conf_id, "user-agent": "browser", "admin": true});
        } else {
            var bye_msg = JSON.stringify({"method":"BYE", "fromtag":from_tag, "callid":call_id, "confid":conf_id, "user-agent": "browser"});
        }
        socketSend(bye_msg);
        call_state = HANGUP;
        //go back to test view
        if (mode != LISTEN) {
            localStream.stop();
        }
        pc.close();
        pc = null;
        localStream = null;
        remoteStream = null;
        // deletMediaStream(remoteView);
        deletMediaStream(remoteView);
        mode = NONE;
        //deletMediaStream(localVideo);
        //do_get_user_media(video_constraints, test_view_setup);
    } 
    if (test_state != IDLE) {
        localStream.stop();
        localStream = null;
        deletMediaStream(remoteView);
        test_state = IDLE;
    }
}

function peer_hang_up(){
    call_state = IDLE;
    localStream.stop();
    pc.close();
    pc = null;
    localStream = null;
    remoteStream = null;
    deletMediaStream(remoteView);
}

function setStatus(state) {
    log_msg(state)
}

function send_register(){
        var register_msg = JSON.stringify({"method":"REGISTER", "fromtag":from_tag, "callid":call_id, "confid":conf_id, "user-agent": "browser"});
        socketSend(register_msg);
}

function processSignalingMessage(message) {
    var msg = JSON.parse(message);

    if (msg.code == 100) {
        log_msg("server->client: websocket connection successful\n")
        //register msg
        var register_msg = JSON.stringify({"method":"REGISTER", "fromtag":from_tag, "callid":call_id, "confid":conf_id, "user-agent": "browser"});
        socketSend(register_msg);
    } else if(msg.code == 150) {
        log_msg("server->client: registration successful\n")
    } else if(msg.code == 155) {
        if (mode == VIDEO) {
            video_call()
        } else if (mode == VOICE) {
            voice_call()
        } else if (mode == AUDIENCE) {
            audience_call()
        } else {
            listener_call()
        } 
    } else if(msg.code == 156) {
            listener_call()
    } else if(msg.code == 200) {
        if(call_state == OFFER_MADE) {
            call_state = ANSWERED;
            //pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp: msg.body }));    
            pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp: msg.body }),
                                    onSetSessionDescriptionSuccess,
                                    onSetSessionDescriptionError);
            if(mode == RECORD){
                socketSend(record_msg);
            }
        }else if(call_state == HANGUP) {
            call_state = IDLE;
            log_msg("server->client:" + msg.body)
        }
    } else if (msg.code == 201) {
        hang_up(0);
        call_state = IDLE;
        alert('admin has terminated the conference')
    } else if (msg.code == 481 || msg.code == 125) {
        alert('your session has ended. Please refresh the browser.')
    } else if (msg.code == 488) {
        alert('call failed due to bad sdp.')
    } else if (msg.code == 502) {
        log_msg(" Error: Unable to connect video/voice. Please check firewall policy on the system or with network service provider");
        hang_up(0);
        call_state = IDLE;
        alert(" Error: Unable to connect video/voice. Please check firewall policy on the system or with network service provider");
    } else if(msg.code == 503) {
        hang_up (0);
        alert("Your bandwidth is low for a video call, hanging up the call");
        call_state = IDLE;
    } else if(msg.code == 505) {
        hang_up(0);
        alert("Client has stopped sending audio packets. Please rejoin the conference");
        call_state = IDLE;
    }else if(msg.code == 430){
        var size = 0;
        for (i=0;i<4;i++){       
            if( msg.body[i] != 0){ 
                size = size +1 ;
                if(msg.body[i].indexOf(msg.callid) != -1 && msg.body[i].indexOf(msg.fromtag)){
                    var selfview_id = i;
                    log_msg("self :"+ i);
               }
            }
            else{
                log_msg("hide :"+ i);
            }
        }
    } else if(msg.code == 490) {
        alert("recording is already in progess");
    } else if(msg.code == 491) {
        alert("record name is missing");
    } else if(msg.code == 492) {
        alert("recording started");
    } else if(msg.code == 493)  {
        alert("recording failed due to internal error"); 
    } else if(msg.code == 500) {
        alert("Invalid conference identifier!!");
        hang_up(0);
        call_state = IDLE;
    } else if(msg.code == 510) {
        peer_hang_up();
        log_msg(msg.body)
        alert(msg.body)
    } else if(msg.code == 550) {
        audio_loss = msg.body
    } else if(msg.code == 551) {
        video_loss = msg.body
    }
    log_msg("server->client:"+ msg)
}

function onIceCandidate(event) {
    if (event.candidate) {
        if(call_state == IDLE){
            call_state = OFFER_MADE;
            log_msg(pc.localDescription.sdp);
            var invite_msg = JSON.stringify({"method":"INVITE", "role": role, "fromtag":from_tag, "callid":call_id, "confid":conf_id, "user-agent": "browser", "sdp": pc.localDescription.sdp});
            socketSend(invite_msg);
        } else {
            log_msg('onIceCandidate call state ='+ call_state)
        }
    } else {
      log_msg("End of candidates.");
    }
}

function onRemoteStreamAdded(event) {
    log_msg("Remote stream added."); 
    call_events("call connected."); 
    //reattachMediaStream(testVideo, localVideo);
    attachMediaStream(remoteView, event.stream);
    remoteStream = event.stream;
}

function onRemoteStreamRemoved(event) {
    log_msg("Remote stream removed.");
}

function toggleVideoMute() {
    // Call the getVideoTracks method via adapter.js.
    videoTracks = localStream.getVideoTracks();

    if (videoTracks.length === 0) {
      log_msg("No local video available.");
      return;
    }

    if (isVideoMuted) {
      for (i = 0; i < videoTracks.length; i++) {
        videoTracks[i].enabled = true;
      }
      log_msg("Video unmuted.");
    } else {
      for (i = 0; i < videoTracks.length; i++) {
        videoTracks[i].enabled = false;
      }
      log_msg("Video muted.");
    }

    isVideoMuted = !isVideoMuted;    
}

function toggleAudioMute() {
    // Call the getAudioTracks method via adapter.js.
    audioTracks = localStream.getAudioTracks();

    if (audioTracks.length === 0) {
      log_msg("No local audio available.");
      return;
    }

    if (isAudioMuted) {
      for (i = 0; i < audioTracks.length; i++) {
        audioTracks[i].enabled = true;
      }
      log_msg("Audio unmuted.");
    } else {
      for (i = 0; i < audioTracks.length; i++){
        audioTracks[i].enabled = false;
      }
      log_msg("Audio muted.");
    }

    isAudioMuted = !isAudioMuted;  
}

$(document).ready(function(){
    log_msg("document is ready");
    conf_id = window.identity;
    initialize();
    $('.button').click (function  (obj) {

        var label = $(this).attr('id');
        log_msg(label);
        switch (label) {
            case "Video":
                //video_call();
            break;
            case "Voice":
                //voice_call();
            break;
            case "Audience":
                //audience_call();
            break;
            case "Hangup":
                hang_up(1);
            break;
            case "help":
            case "Feedback":
            case "upload":
            case "Invite":
                //do nothing
            break;
            default:
                log_msg('Wrong click');
            break;
        };
    });

});

// Ctrl-D: toggle audio mute; Ctrl-E: toggle video mute.
// On Mac, Command key is instead of Ctrl.
// Return false to screen out original Chrome shortcuts.
document.onkeydown = function() {
    if (navigator.appVersion.indexOf("Mac") != -1) {
      if (event.metaKey && event.keyCode === 68) {
        toggleAudioMute();
        return false;
      }
      if (event.metaKey && event.keyCode === 69) {
        toggleVideoMute();
        return false;
      }
    } else {
      if (event.ctrlKey && event.keyCode === 68) {
        toggleAudioMute();
        return false;
      }
      if (event.ctrlKey && event.keyCode === 69) {
        toggleVideoMute();
        return false;
      }
    }
}

// Set Opus as the default audio codec if it's present.
function preferOpus(sdp) {
    var sdpLines = sdp.split('\r\n');

    // Search for m line.
    for (var i = 0; i < sdpLines.length; i++) {
        if (sdpLines[i].search('m=audio') !== -1) {
          var mLineIndex = i;
          break;
        } 
    }
    if (mLineIndex === null)
      return sdp;

    // If Opus is available, set it as the default in m line.
    for (var i = 0; i < sdpLines.length; i++) {
      if (sdpLines[i].search('opus/48000') !== -1) {        
        var opusPayload = extractSdp(sdpLines[i], /:(\d+) opus\/48000/i);
        if (opusPayload)
          sdpLines[mLineIndex] = setDefaultCodec(sdpLines[mLineIndex], opusPayload);
        break;
      }
    }

    sdp = sdpLines.join('\r\n');
    return sdp;
}

function extractSdp(sdpLine, pattern) {
    var result = sdpLine.match(pattern);
    return (result && result.length == 2)? result[1]: null;
}

// Set the selected codec to the first in m line.
function setDefaultCodec(mLine, payload) {
    var elements = mLine.split(' ');
    var newLine = new Array();
    var index = 0;
    for (var i = 0; i < elements.length; i++) {
      if (index === 3) // Format of media starts from the fourth.
        newLine[index++] = payload; // Put target payload to the first.
      if (elements[i] !== payload)
        newLine[index++] = elements[i];
    }
    return newLine.join(' ');
}

function check_mic_capture(sdp){
    var sdpLines = sdp.split('\r\n');
    // Search for m audio line.
    for (var i = 0; i < sdpLines.length; i++) {
        if (sdpLines[i].search('m=audio') !== -1) {
          var audioLineIndex = i;
          break;
        }
    }
    if (audioLineIndex === null){
        call_events('m=audio line didnt found. Failed to get microphone.'+sdp) 
        return 0;
    }
    // search for a=sendrecv for audio line,
    // if not found call will not proceed.
    var direction_pattern = /a=sendrecv*/
    var mic_capture = false;
    for(var i= audioLineIndex; i<=sdpLines.length-1; i++){
        var sdpLine = sdpLines[i];
        var direction_result = sdpLine.match(direction_pattern);
        if (direction_result){
            mic_capture = true;
            break;
        }
        if(sdpLine.search('m=video') !== -1) {
            //if we dont find before video line. mic_capture failed.
            break;
        }
    }
    if(mic_capture === false){
        call_events('a=sendrecv not found for m=audio line.Failed to get microphone.'+sdp)
        alert('Failed to get microphone');
        return 0;
    }
    return audioLineIndex;
}

function process_audio_lines(audioLineIndex, sdp) {
    // Strip CN from sdp before CN constraints is ready.
    var sdpLines = sdp.split('\r\n');

    var mLineElements = sdpLines[audioLineIndex].split(' ');
    // Scan from end for the convenience of removing an item.
    for (var i = sdpLines.length-1; i >= 0; i--) {
      var payload = extractSdp(sdpLines[i], /a=rtpmap:(\d+) CN\/\d+/i);
      if (payload) {
        var cnPos = mLineElements.indexOf(payload);
        if (cnPos !== -1) {
          // Remove CN payload from m line.
          mLineElements.splice(cnPos, 1);
        }
        // Remove CN line in sdp
        sdpLines.splice(i, 1);
      }
    }
    //adding nack line for opus regardless of type of call
    var opus_pattern = /a=rtpmap:(\d+) opus\/48000\/2/
    for(var i = 0; i < sdpLines.length; i++){
        var sdpLine = sdpLines[i];
        var result = sdpLine.match(opus_pattern);
        if(result){
            // log_msg('opus match'+sdpLine);
            var str = sdpLine.split(" ");
            // this will produce a=rtpmap:(\d+), opus/48000/2
            var payload = str[0].split(":");
            // this will produce a=rtpmap, \d+
            var rtcp_fb = "a=rtcp-fb:";
            var rtcp_fb_payload = rtcp_fb.concat(payload[1]);
            var rtcp_fb_nack = rtcp_fb_payload.concat(" nack");
            sdpLines.splice(i, 0, rtcp_fb_nack);
            break;
        }
    }
    sdpLines[audioLineIndex] = mLineElements.join(' ');
    sdp = sdpLines.join('\r\n');
    return sdp;
}

function audience_video_ssrc_add(sdp){
    var sdpLines = sdp.split('\r\n');
    // copy ssrc strings of audio
    // audio and video line will have same cname,mslabel,label value
    // so copy them and increment ssrc by 1
    log_msg('video_ssrc_add called');
    var cname = null,mslabel = null ,label = null;
    var cname_pattern = /a=ssrc:(\d+) cname:*/
    var mslabel_pattern = /a=ssrc:(\d+) mslabel:*/
    var label_pattern = /a=ssrc:(\d+) label:*/
    var video_index = 0;
    for(var i = 0; i < sdpLines.length; i++){
        var sdpLine = sdpLines[i];
        var result = sdpLine.match(cname_pattern);
        if(result){
            log_msg('cname match'+sdpLine);
            audio_ssrc = Number(result[1]);
            video_ssrc = audio_ssrc + 1;
            split_array = sdpLine.split(" ");
            cname = 'a=ssrc:'+String(video_ssrc)+" "+split_array[1];
            continue;
        }
        result = sdpLine.match(mslabel_pattern);
        if(result) {
            log_msg('mslabel match'+sdpLine);
            audio_ssrc = Number(result[1]);
            video_ssrc = audio_ssrc + 1;
            split_array = sdpLine.split(" ");
            mslabel = 'a=ssrc:'+String(video_ssrc)+" "+split_array[1];
            continue;
        }
        result = sdpLine.match(label_pattern);
        if(result) {
            log_msg('label match'+sdpLine);
            audio_ssrc = Number(result[1]);
            video_ssrc = audio_ssrc + 1;
            split_array = sdpLine.split(" ");
            label = 'a=ssrc:'+String(video_ssrc)+" "+split_array[1];
            continue;
        }
        if(sdpLine.search('m=video') !== -1){
            log_msg('video line match')
            video_index = i;
            continue;
        }
        if(video_index && (sdpLine.search('a=sendrecv') !== -1) ){
            log_msg('replace sendrecv with recvonly');
            sdpLines.splice(i, 1);
            sdpLines.splice(i, 0, 'a=recvonly');
        }
    }
    var last_index = sdpLines.length - 1;
    sdpLines.splice(last_index,0,cname);
    sdpLines.splice(last_index+1,0,mslabel);
    sdpLines.splice(last_index+2,0,label);

    sdp = sdpLines.join('\r\n');
    return sdp;
}

function listen_video_ssrc_add(sdp){
    var sdpLines = sdp.split('\r\n');
    audio_ssrc = Math.floor(Math.random() * 1000000001)
    video_ssrc = audio_ssrc + 1
    audio_cname = 'a=ssrc:' + String(audio_ssrc) + ' cname:wjkd3ycwsniqfhcq'
    audio_mslabel = 'a=ssrc:' + String(audio_ssrc) + ' mslabel:kpjjz1x0zndw0fsxr32tnylpbdlwgbivsgvy'
    audio_label = 'a=ssrc:' + String(audio_ssrc) + ' label:kpjjz1x0zndw0fsxr32tnylpbdlwgbivsgvya0'
    for(var i = 0; i < sdpLines.length; i++){
        var sdpLine = sdpLines[i];
        if(sdpLine.search('m=video') !== -1){
            log_msg('video line match')
            break;
        }
    }
    sdpLines.splice(i-1, 0, audio_cname);
    sdpLines.splice(i, 0, audio_mslabel);
    sdpLines.splice(i+1, 0, audio_label);
    video_cname = 'a=ssrc:' + String(video_ssrc) + ' cname:wjkd3ycwsniqfhcq'
    video_mslabel = 'a=ssrc:' + String(video_ssrc) + ' mslabel:kpjjz1x0zndw0fsxr32tnylpbdlwgbivsgvy'
    video_label = 'a=ssrc:' + String(video_ssrc) + ' label:kpjjz1x0zndw0fsxr32tnylpbdlwgbivsgvya0'
    var last_index = sdpLines.length - 1;
    sdpLines.splice(last_index, 0, video_cname);
    sdpLines.splice(last_index+1, 0, video_mslabel);
    sdpLines.splice(last_index+2, 0, video_label);

    sdp = sdpLines.join('\r\n');
    return sdp;
}

function check_cam_capture(sdp){
    var sdpLines = sdp.split('\r\n');
    for (var i = 0; i < sdpLines.length; i++) {
        if (sdpLines[i].search('m=video') !== -1) {
          var videoLineIndex = i;
          break;
        }
    }
    if (videoLineIndex === null){
        call_events('m=video line didnt found. Failed to get camera.'+sdp) 
        if(camera_failure === false){
            alert('Failed to get camera.');
            camera_failure = true
        }
        return sdp;
    }
    var direction_pattern = /a=sendrecv*/
    var cam_capture = false;
    for(var i= videoLineIndex; i<=sdpLines.length-1; i++){
        var sdpLine = sdpLines[i];
        var direction_result = sdpLine.match(direction_pattern);
        if (direction_result){
            cam_capture = true;
            break;
        }
    }
    if(cam_capture === false){
        call_events('a=sendrecv not found for m=video line.Failed to get camera.'+sdp)
        // mask multiple alerts
        if(camera_failure === false){
            alert('Failed to get camera.');
            camera_failure = true
        }
        //mode = video to audience
        sdp = audience_video_ssrc_add(sdp)
    }
    return sdp;
}

function client_sdp_check(sdp){
    if( mode === LISTEN) {
        sdp = listen_video_ssrc_add(sdp)
    } else {
        /* 1) failed to get microphone.
           2) process_audio_lines i.e remove cn, add opus nack
           3) add ssrc for video elements if needed. 
        */
        var audioLineIndex = check_mic_capture(sdp)    
        if (audioLineIndex === 0) {
            alert('Failed to get microphone');
            return null;
        }
        sdp = process_audio_lines(audioLineIndex, sdp)
     
        if (mode == VIDEO || mode == RECORD) {
            sdp = check_cam_capture(sdp)
        } else if (mode == AUDIENCE) {
            sdp = audience_video_ssrc_add(sdp)
        }
    }
    return sdp;
}
