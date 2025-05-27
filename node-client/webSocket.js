"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.WebSocketServer = void 0;
const socket_io_1 = require("socket.io");
const http_1 = require("http");
class WebSocketServer {
    constructor(port = 3001) {
        this.httpServer = (0, http_1.createServer)();
        this.io = new socket_io_1.Server(this.httpServer, {
            cors: {
                origin: "*",
                methods: ["GET", "POST"]
            }
            //  maxHttpBufferSize: 1e7, // 10 MB
        });
        this.io.on('connection', (socket) => {
            console.log('Client connected:', socket.id);
            socket.on('message', (msg) => {
                console.log('Received from client:', msg);
                socket.emit('message', `Echo: ${msg}`);
            });
            socket.emit('message', 'Welcome to the Socket.IO server!');
        });
        this.httpServer.listen(port, () => {
            console.log(`WebSocket server running on port ${port}`);
        });
    }
    emit(msg) {
        this.io.emit('wallet', msg); // sends only to this client
        console.log('Emitting message to client');
    }
}
exports.WebSocketServer = WebSocketServer;
