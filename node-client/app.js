"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const net_1 = __importDefault(require("net"));
const webSocket_1 = require("./webSocket");
const wsSender = new webSocket_1.WebSocketServer(3001); // Initialize WebSocket server on port 8888
const listeningPort = 3000;
const server = net_1.default.createServer((socket) => {
    // Set a large buffer size for this socket
    //  socket.setMaxListeners(1e7); // 10 MB
    console.log("Client connected");
    socket.on("data", (data) => __awaiter(void 0, void 0, void 0, function* () {
        console.log("recive data from server");
        //  console.log(JSON.parse(data.toString()));
        wsSender.emit(data.toString()); // Emit to WebSocket server
    }));
    socket.on("end", () => {
        console.log("Client disconnected");
    });
    socket.on("error", (error) => {
        console.log(`Socket Error: ${error.message}`);
    });
});
server.on("error", (error) => {
    console.log(`Server Error: ${error.message}`);
});
server.listen(listeningPort, () => {
    console.log(`TCP socket server is running on port: ${listeningPort}`);
});
