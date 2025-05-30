let loggedInUser = "";

    document.addEventListener("DOMContentLoaded", () => {
        fetch("/current-user")
            .then(res => res.text())
            .then(username => {
                loggedInUser = username;
                loadRooms();
            });

        document.getElementById("createChatRoomForm").addEventListener("submit", (e) => {
            e.preventDefault();

            const roomName = document.getElementById("roomName").value;
            const isMicOnByDefault = document.getElementById("micDefault").value;
            const isCameraOnByDefault = document.getElementById("cameraDefault").value;

            fetch("/chat-lobby/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    roomName,
                    isMicOnByDefault,
                    isCameraOnByDefault
                })
            })
            .then(res => {
                if (res.ok) {
                    alert("Room created!");
                    loadRooms();
                    document.getElementById("roomName").value = "";
                }
            });
        });

        document.getElementById("joinRoomForm").addEventListener("submit", (e) => {
            e.preventDefault();
            const roomCode = document.getElementById("roomCode").value;
            window.location.href = `/chat-lobby/room/${roomCode}`;
        });
    });

    function formatDateTime(isoString) {
        const date = new Date(isoString);
        const options = {
            weekday: 'short',
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        };
        return date.toLocaleDateString(undefined, options);
    }

    function loadRooms() {
        fetch("/chat-lobby/rooms")
            .then(res => res.json())
            .then(rooms => {
                const roomList = document.getElementById("roomList");
                roomList.innerHTML = "";

                rooms.forEach(room => {
                    const div = document.createElement("div");
                    div.className = "room-card";
                    div.innerHTML = `
                        <div>
                            <h6>${room.roomName}</h6>
                            <div class="room-meta">
                                <div><strong>Code:</strong> ${room.roomCode}</div>
                                <div><strong>Host:</strong> ${room.host}</div>
                                <div><strong>Created:</strong> ${formatDateTime(room.createdAt)}</div>
                                <div><strong>Mic:</strong> ${room.micOnByDefault ? 'On' : 'Off'}</div>
                                <div><strong>Cam:</strong> ${room.cameraOnByDefault ? 'On' : 'Off'}</div>
                            </div>
                        </div>
                        <div class="room-buttons">
                            <a href="/chat-lobby/room/${room.roomCode}" class="btn btn-sm btn-success">Join</a>
                            ${room.host === loggedInUser ? `
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteRoom('${room.roomCode}')">Delete</button>
                            ` : ''}
                        </div>
                    `;
                    roomList.appendChild(div);
                });
            });
    }

    function deleteRoom(roomCode) {
        fetch(`/chat-lobby/delete/${roomCode}`, {
            method: "DELETE"
        })
        .then(res => {
            if (res.ok) {
                loadRooms();
            }
        });
    }