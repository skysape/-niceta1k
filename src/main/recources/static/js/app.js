// Real-time Chat & Voice RTC Simulation Engine
let stompClient = null;
let currentChannelId = 'general';
let clientUser = {
    login: '',
    nickname: '',
    email: '',
    avatar: 'https://picsum.photos/40'
};

// Simulated Cooldown limits (in memory for local check)
const limitTimers = {
    nickname: 0,
    login: 0,
    emailOrPassword: 0
};

// WebRTC Audio nodes for individual volume regulation (0-200%)
const participantAudioNodes = {};
let audioCtx = null;

// Ringtones & Notifications Audio triggers
const ringtoneNode = document.getElementById('sound-ringtone');
const notifyNode = document.getElementById('sound-notification');

// Init WebSocket Connection
function connectWebSocket() {
    const socket = new SockJS('/discord-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected WebSocket: ' + frame);
        
        // Subscribe to text channel
        stompClient.subscribe('/topic/channel/' + currentChannelId, function (messageOutput) {
            appendMessage(JSON.parse(messageOutput.body));
        });

        // Subscribe to reaction changes
        stompClient.subscribe('/topic/channel/' + currentChannelId + '/reactions', function (reactionOutput) {
            handleReceivedReaction(JSON.parse(reactionOutput.body));
        });

        // Subscribe to incoming call invitations specifically for this user
        stompClient.subscribe('/user/queue/call-incoming', function (callData) {
            triggerIncomingCall(JSON.parse(callData.body));
        });
    }, function(error) {
        console.log('WebSocket Error (running mock simulator mode): ' + error);
        mockWebSocketFallback();
    });
}

function mockWebSocketFallback() {
    console.warn("Running in local mock mode.");
}

// App Registration flow
document.getElementById('btn-register').addEventListener('click', function() {
    const login = document.getElementById('reg-login').value;
    const nickname = document.getElementById('reg-nickname').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const errorBox = document.getElementById('reg-error');

    // Alphanumeric custom regex check (Letters, Cyrillic, Polish characters & numbers)
    const passRegex = /^[a-zA-Z0-9а-яА-ЯёЁąęćłńóśźżĄĆĘŁŃÓŚŹŻ]{8,}$/;
    if (!passRegex.test(password)) {
        errorBox.textContent = "Password must be at least 8 characters, only letters (Latin, Cyrillic, Polish) and digits.";
        return;
    }

    // Show loading spinner
    document.getElementById('btn-reg-text').classList.add('hidden');
    document.getElementById('btn-reg-spinner').classList.remove('hidden');

    // AJAX to Spring Boot Authentication REST Controller
    fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login, nickname, email, password })
    })
    .then(res => res.json())
    .then(data => {
        if (data.error) {
            errorBox.textContent = data.error;
            resetRegButton();
        } else {
            errorBox.textContent = "";
            document.getElementById('verify-section').classList.remove('hidden');
            clientUser.login = login;
            clientUser.nickname = nickname || login;
            clientUser.email = email;
            alert("Success! Check console / server output for your mock SMTP verification code.");
        }
    })
    .catch(err => {
        console.log("Offline or server not running. Bypassing auth for demo purposes.");
        clientUser.login = login;
        clientUser.nickname = nickname || login;
        clientUser.email = email;
        document.getElementById('my-name').textContent = clientUser.nickname;
        document.getElementById('auth-overlay').classList.add('hidden');
        connectWebSocket();
    });
});

function resetRegButton() {
    document.getElementById('btn-reg-text').classList.remove('hidden');
    document.getElementById('btn-reg-spinner').classList.add('hidden');
}

// Email Verification
document.getElementById('btn-verify').addEventListener('click', function() {
    const code = document.getElementById('verify-code').value;
    fetch('/api/auth/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: clientUser.email, code: code })
    })
    .then(res => res.json())
    .then(data => {
        if (data.error) {
            document.getElementById('verify-error').textContent = data.error;
        } else {
            document.getElementById('auth-overlay').classList.add('hidden');
            document.getElementById('my-name').textContent = clientUser.nickname;
            connectWebSocket();
        }
    })
    .catch(err => {
        document.getElementById('auth-overlay').classList.add('hidden');
        document.getElementById('my-name').textContent = clientUser.nickname;
        connectWebSocket();
    });
});

// Sound Settings
document.getElementById('effects-volume-range').addEventListener('input', function(e) {
    const vol = e.target.value / 100;
    ringtoneNode.volume = vol;
    notifyNode.volume = vol;
});

// Incoming Call signaling triggers
function triggerIncomingCall(payload) {
    document.getElementById('call-caller-name').textContent = payload.caller + " is calling you...";
    document.getElementById('incoming-call-overlay').classList.remove('hidden');
    ringtoneNode.play().catch(e => console.log("Audio needs interaction: " + e));
}

document.getElementById('btn-call-accept').addEventListener('click', function() {
    ringtoneNode.pause();
    document.getElementById('incoming-call-overlay').classList.add('hidden');
    document.getElementById('voice-sidebar').classList.remove('hidden');
    playNotificationSound();
    simulateRtcAudio();
});

document.getElementById('btn-call-reject').addEventListener('click', function() {
    ringtoneNode.pause();
    document.getElementById('incoming-call-overlay').classList.add('hidden');
});

function playNotificationSound() {
    notifyNode.currentTime = 0;
    notifyNode.play().catch(e => console.log(e));
}

// Dynamic Channels, Message sending, slow down chat
let spamCooldown = false;
document.getElementById('chat-input-field').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        const text = e.target.value;
        if (!text) return;

        if (spamCooldown) {
            const warning = document.getElementById('spam-warning');
            warning.classList.remove('hidden');
            setTimeout(() => warning.classList.add('hidden'), 2000);
            return;
        }

        sendMessage(text);
        e.target.value = '';

        spamCooldown = true;
        setTimeout(() => { spamCooldown = false; }, 3000);
    }
});

function sendMessage(content) {
    const msg = {
        channelId: currentChannelId,
        sender: clientUser.nickname,
        content: content,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    if (stompClient && stompClient.connected) {
        stompClient.send("/app/chat/send", {}, JSON.stringify(msg));
    } else {
        appendMessage(msg);
    }
}

function appendMessage(msg) {
    const box = document.getElementById('chat-messages');
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message';
    msgDiv.innerHTML = `
        <img src="https://picsum.photos/35?random=${Math.random()}" class="avatar-sm">
        <div class="message-content">
            <div class="msg-header">
                <span class="sender-name">${msg.sender}</span>
                <span class="msg-time">${msg.timestamp}</span>
            </div>
            <div class="msg-body">${msg.content}</div>
            <div class="reactions-box" id="react-box-${msg.timestamp.replace(/[: ]/g, '')}">
                <button class="reaction-btn" onclick="addLocalReaction('${msg.timestamp.replace(/[: ]/g, '')}', '❤️')">❤️ <span class="count">0</span></button>
                <button class="reaction-btn" onclick="addLocalReaction('${msg.timestamp.replace(/[: ]/g, '')}', '🔥')">🔥 <span class="count">0</span></button>
            </div>
        </div>
    `;
    box.appendChild(msgDiv);
    box.scrollTop = box.scrollHeight;
    playNotificationSound();
}

function addLocalReaction(msgId, emoji) {
    const container = document.getElementById('react-box-' + msgId);
    if (!container) return;
    
    const btns = container.querySelectorAll('.reaction-btn');
    btns.forEach(btn => {
        if (btn.textContent.includes(emoji)) {
            const countSpan = btn.querySelector('.count');
            countSpan.textContent = parseInt(countSpan.textContent) + 1;
        }
    });
}

// 0-200% Web Audio GainNode individual volume regulation
function simulateRtcAudio() {
    if (!audioCtx) {
        const AudioContext = window.AudioContext || window.webkitAudioContext;
        audioCtx = new AudioContext();
    }
    
    const oscillator = audioCtx.createOscillator();
    oscillator.type = 'sine';
    oscillator.frequency.setValueAtTime(300, audioCtx.currentTime); // low hum
    
    const gainNode = audioCtx.createGain();
    gainNode.gain.value = 1.0; // default 100%
    
    oscillator.connect(gainNode);
    gainNode.connect(audioCtx.destination);
    oscillator.start();
    
    participantAudioNodes['Artyom'] = { osc: oscillator, gain: gainNode };
}

// Context Menu for remote peer
const participantEl = document.querySelector('.participant');
const contextMenu = document.getElementById('custom-context-menu');
const volumeSlider = document.getElementById('peer-volume-slider');
const volumePercent = document.getElementById('peer-volume-percent');

participantEl.addEventListener('contextmenu', function(e) {
    e.preventDefault();
    contextMenu.style.top = `${e.clientY}px`;
    contextMenu.style.left = `${e.clientX}px`;
    contextMenu.classList.remove('hidden');
});

document.addEventListener('click', function(e) {
    if (!contextMenu.contains(e.target)) {
        contextMenu.classList.add('hidden');
    }
});

volumeSlider.addEventListener('input', function(e) {
    const vol = e.target.value;
    volumePercent.textContent = `${vol}%`;
    
    const nodeObj = participantAudioNodes['Artyom'];
    if (nodeObj) {
        nodeObj.gain.gain.setValueAtTime(vol / 100, audioCtx.currentTime);
    }
});

function hangUpCall() {
    const nodeObj = participantAudioNodes['Artyom'];
    if (nodeObj) {
        nodeObj.osc.stop();
        delete participantAudioNodes['Artyom'];
    }
    document.getElementById('voice-sidebar').classList.add('hidden');
}

// Settings modal
function switchSettingsTab(tabName) {
    const panels = document.querySelectorAll('.settings-panel');
    panels.forEach(p => p.classList.add('hidden'));
    
    const buttons = document.querySelectorAll('.settings-tab-btn');
    buttons.forEach(b => b.classList.remove('active'));

    document.getElementById(`settings-tab-${tabName}`).classList.remove('hidden');
    event.currentTarget.classList.add('active');
}

document.getElementById('btn-open-settings').addEventListener('click', () => {
    document.getElementById('settings-modal').classList.remove('hidden');
});

function closeSettings() {
    document.getElementById('settings-modal').classList.add('hidden');
}

// Nickname change cooldown (3 days rule)
function saveNickname() {
    const nick = document.getElementById('settings-nickname').value;
    const now = Date.now();
    const cooldown = 3 * 24 * 60 * 60 * 1000;
    
    if (now - limitTimers.nickname < cooldown) {
        alert("You can change your nickname only once every 3 days!");
        return;
    }
    
    limitTimers.nickname = now;
    clientUser.nickname = nick;
    document.getElementById('my-name').textContent = nick;
    alert("Nickname updated successfully!");
}

function saveLogin() {
    const login = document.getElementById('settings-login').value;
    const now = Date.now();
    const cooldown = 7 * 24 * 60 * 60 * 1000;
    
    if (now - limitTimers.login < cooldown) {
        alert("You can change your login only once every 7 days!");
        return;
    }
    
    limitTimers.login = now;
    clientUser.login = login;
    alert("Login updated successfully!");
}

function saveEmailPass() {
    const email = document.getElementById('settings-email').value;
    const now = Date.now();
    const cooldown = 30 * 24 * 60 * 60 * 1000;
    
    if (now - limitTimers.emailOrPassword < cooldown) {
        alert("You can change your email/password only once every 30 days!");
        return;
    }
    
    limitTimers.emailOrPassword = now;
    if (email) clientUser.email = email;
    alert("Profile settings saved.");
}

// Localization dictionary (Russian, Belarusian, Polish, English)
const locales = {
    en: { brand: "DiscordClone", friends: "Friends", channels: "CHANNELS & GROUPS", guest: "Guest", theme: "Theme", lang: "Interface Language" },
    ru: { brand: "ДискордКлон", friends: "Друзья", channels: "КАНАЛЫ И ГРУППЫ", guest: "Гость", theme: "Тема", lang: "Язык интерфейса" },
    be: { brand: "ДыскардКлон", friends: "Сябры", channels: "КАНАЛЫ І ГРУПУ", guest: "Госць", theme: "Тэма", lang: "Мова інтэрфейсу" },
    pl: { brand: "KlonDiscorda", friends: "Znajomi", channels: "KANAŁY I GRUPY", guest: "Gość", theme: "Motyw", lang: "Język interfejsu" }
};

function changeLanguage(lang) {
    const dict = locales[lang] || locales.en;
    document.querySelector('.brand').textContent = dict.brand;
    document.getElementById('tab-friends-btn').innerHTML = `<span class="icon">👥</span> ${dict.friends}`;
    document.querySelector('.section-title').textContent = dict.channels;
}

function changeTheme(theme) {
    const body = document.getElementById('body-app');
    if (theme === 'light') {
        body.className = 'theme-light';
    } else {
        body.className = 'theme-dark';
    }
}

// Direct Call simulation
function startDirectCall(name) {
    const payload = {
        caller: clientUser.nickname || "User",
        target: name,
        groupCall: false
    };
    if (stompClient && stompClient.connected) {
        stompClient.send("/app/call/invite", {}, JSON.stringify(payload));
    } else {
        triggerIncomingCall(payload);
    }
}