let stompClient = null;
let peerConnection = null;
let localStream = null;
let targetUser = '';
let mediaRecorder;
let recordedChunks = [];
let startTime;
let endTime;
let duration;
let hostUrl;
let guestUrl;
let flag = '0';
let candidateQueue = [];
let lat1;


const config = {
    iceServers: [
        { urls: 'stun:stun.l.google.com:19302' },
        {
            urls: 'turn:openrelay.metered.ca:80',
            username: 'openrelayproject',
            credential: 'openrelayproject'
        }
    ]
};

const localVideo = document.getElementById('localVideo');
const remoteVideo = document.getElementById('remoteVideo');
const endCallBtn = document.getElementById('endCall');

connectSocket();

function connectSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        ['call', 'offer', 'answer', 'candidate', 'end'].forEach(type => {
            stompClient.subscribe(`/user/queue/${roomCode}/${type}`, msg => {

                const data = type === 'call' ? msg.body : JSON.parse(msg.body);
                handleSignal(type, data);
            });
        });

        stompClient.subscribe("/queue/pong", (message) => {
            console.log(message.body)
            const lat2 = Date.now();
            const latency = lat2 - lat1;
            console.log(`WebSocket signaling latency: ${latency}ms`);
        });
        lat1 = Date.now();
                stompClient.send("/app/ping", {}, "ping");

        stompClient.subscribe(`/queue/onlineUsers/${roomCode}`, msg => {
            const users = JSON.parse(msg.body);
            updateUserList(users);
        });

        stompClient.subscribe(`/queue/${roomCode}/guestUrl`, message => {
            guestUrl = message.body;
        });

        stompClient.subscribe(`/queue/${roomCode}/hostUrl`, message => {
            hostUrl = message.body;
        });

        stompClient.subscribe(`/queue/${roomCode}/flag`, message => {
            flag = message.body;
        });

        stompClient.subscribe(`/queue/${roomCode}/startTime`, message => {
            startTime = message.body;
            console.log("Received startTime:", startTime);
        });

        stompClient.subscribe(`/queue/${roomCode}/endTime`, message => {

            endTime = message.body;
            console.log("Received endTime:", endTime);
        });

        stompClient.send("/app/joinRoom", {}, JSON.stringify({ roomCode }));
    }, error => {
        console.error('STOMP connection error:', error);
    });

    console.log("Fetching!");
}



function updateUserList(users) {
    const userList = document.getElementById('userList');
    userList.innerHTML = '';
    const h2 = document.querySelector("h2");

    users.forEach(user => {
        if (user !== username) {
            h2.innerHTML = '';
            const btn = document.createElement('button');
            btn.textContent = user;
            if (targetUser === '') targetUser = user;
            btn.onclick = () => {
                startCall();
            };
            userList.appendChild(btn);
        }
    });
}


function handleCandidate(candidate) {
    if (peerConnection && peerConnection.remoteDescription && peerConnection.remoteDescription.type) {
        peerConnection.addIceCandidate(new RTCIceCandidate(candidate)).catch(console.error);
    } else {
        candidateQueue.push(candidate);
    }
}

function flushCandidates() {
    if (!peerConnection) return;
    console.log("Flushing queued ICE candidates:", candidateQueue.length);
    candidateQueue.forEach(candidate => {
        peerConnection.addIceCandidate(new RTCIceCandidate(candidate))
            .catch(err => console.error("Error adding queued candidate:", err));
    });
    candidateQueue = [];
}


function startCall() {
    if (!targetUser) return alert("Enter a username to call");


    sendSignal("call", null);
    stompClient.send(`/app/${roomCode}/flag`, {}, '0');


}


function acceptCall() {
    document.getElementById("incomingCall").style.display = "none";
    navigator.mediaDevices.getUserMedia({ video: true, audio: true }).then(stream => {
            setupPeerConnection(stream, true); // ✅ caller = true
        });
//    navigator.mediaDevices.getUserMedia({ video: true, audio: true }).then(stream => {
//        setupPeerConnection(stream);
//    });
}

function setupPeerConnection(stream, isCaller) {
    peerConnection = new RTCPeerConnection(config);
    localStream = stream;
    localVideo.srcObject = stream;

    stream.getTracks().forEach(track => peerConnection.addTrack(track, stream));

    peerConnection.onicecandidate = e => {
        if (e.candidate) sendSignal("candidate", e.candidate);
    };

    peerConnection.ontrack = e => {
        remoteVideo.srcObject = e.streams[0];
    };

    if (isCaller) {
        peerConnection.createOffer()
            .then(offer => peerConnection.setLocalDescription(offer))
            .then(() => sendSignal("offer", peerConnection.localDescription));
    }

    endCallBtn.style.display = 'inline-block';
}

function handleSignal(type, data) {
    if (type === 'call') {
        document.getElementById("incomingCall").style.display = "block";
        document.getElementById("callerName").innerText = data;
    } else if (type === 'offer') {
         handleOffer(data);
    } else if (type === 'answer') {

        peerConnection.setRemoteDescription(new RTCSessionDescription(data))
        .then(() => {
                    flushCandidates(); // <--- flush here
                });
        startTime = new Date();
        startTime = getLocalFormattedTime(startTime);
        stompClient.send(`/app/${roomCode}/startTime`, {}, startTime);
        startRecording();
    } else if (type === 'candidate') {
//        peerConnection.addIceCandidate(new RTCIceCandidate(data));
          handleCandidate(data);

    } else if (type === 'end') {
        endCall(true);
    }
}

async function handleOffer(data) {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
        setupPeerConnection(stream, false); // ✅ callee = false
        await peerConnection.setRemoteDescription(new RTCSessionDescription(data));
        flushCandidates();
        const answer = await peerConnection.createAnswer();
        await peerConnection.setLocalDescription(answer);
        sendSignal("answer", peerConnection.localDescription);
        startRecording();
    } catch (err) {
        console.error("Error handling offer:", err);
    }
}
function sendSignal(type, data) {
    stompClient.send(`/app/signal/${roomCode}`, {}, JSON.stringify({
        toUser: targetUser,
        type: type,
        data: data ? JSON.stringify(data) : null
    }));
}

function getLocalFormattedTime(dateObj) {
    return dateObj.getFullYear() + '-' +
        String(dateObj.getMonth() + 1).padStart(2, '0') + '-' +
        String(dateObj.getDate()).padStart(2, '0') + ' ' +
        String(dateObj.getHours()).padStart(2, '0') + ':' +
        String(dateObj.getMinutes()).padStart(2, '0') + ':' +
        String(dateObj.getSeconds()).padStart(2, '0');
}

function saveVideoCall({ targetUser, startTime, duration, hostUrl, guestUrl }) {
    console.log("Saving video call...");
    console.log("Host: ", hostUrl);
    console.log("Guest: ", guestUrl);
    console.log("Start Time:", startTime);

    fetch('/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ roomCode, targetUser, startTime, duration, hostUrl, guestUrl })
    })
    .then(response => {
        if (!response.ok) throw new Error("Failed to save video call");
        return response.text();
    })
    .then(data => {
        console.log("Video call saved:", data);

    });
}

function endCall(fromRemote = false) {
    if (peerConnection) peerConnection.close();

    if (!fromRemote) {
        endTime = new Date();
        endTime = getLocalFormattedTime(endTime);

        stompClient.send(`/app/${roomCode}/endTime`, {}, endTime);
        sendSignal("end", "end-call");
    }

    peerConnection = null;

    if (localStream) {
        localStream.getTracks().forEach(t => t.stop());
        localStream = null;
    }

    localVideo.srcObject = null;
    remoteVideo.srcObject = null;
    endCallBtn.style.display = 'none';

    alert("Call Ended");
}

function startRecording() {
    if (!localStream) return alert("Start a call first to record.");
    if (mediaRecorder && mediaRecorder.state === "recording") return;

    mediaRecorder = new MediaRecorder(localStream);
    recordedChunks = [];

    mediaRecorder.ondataavailable = event => {
        if (event.data.size > 0) {
            recordedChunks.push(event.data);
        }
    };

    mediaRecorder.onstop = () => {
        const fileName = "recording-" + crypto.randomUUID() + ".webm";
        const blob = new Blob(recordedChunks, { type: 'video/webm' });
        const file = new File([blob], fileName, { type: 'video/webm' });

        const formData = new FormData();
        formData.append('file', file);

        fetch('/uploadVideo', {
            method: 'POST',
            body: formData
        })
        .then(res => res.text())
        .then(videoUrl => {
            const isHost = username === host;
            if (isHost) {
                console.log("lokkkkkkkkkkkkkkkkkkk", videoUrl);
                hostUrl = videoUrl;
            } else {
                console.log("lokkkkkkkkkkkkkkkkkkk", videoUrl);
                guestUrl = videoUrl;
            }

            if (guestUrl) stompClient.send(`/app/${roomCode}/guestUrl`, {}, guestUrl);
            if (hostUrl) stompClient.send(`/app/${roomCode}/hostUrl`, {}, hostUrl);

            console.log("hostUrl: ",hostUrl);
            console.log("guestUrl: ",guestUrl);
            if (hostUrl && guestUrl && flag == '0') {
                stompClient.send(`/app/${roomCode}/flag`, {}, '1');

                if (startTime && endTime) {
                    duration = Math.floor((new Date(endTime) - new Date(startTime)) / 1000);
                } else {
                    duration = 0;
                }
                console.log("startTime: ",startTime);
                console.log("endTime: ",endTime);

                saveVideoCall({ targetUser, startTime, duration, hostUrl, guestUrl });
            }
        })
        .catch(error => {
            console.error("Video upload failed:", error);
        });
    };

    mediaRecorder.start();
}
