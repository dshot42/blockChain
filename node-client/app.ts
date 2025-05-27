
import net, { Socket } from 'net';
import { WebSocketServer } from './webSocket';


const wsSender : WebSocketServer = new WebSocketServer(3001); // Initialize WebSocket server on port 8888

const listeningPort = 3000;

const server = net.createServer((socket: Socket) => {
    // Set a large buffer size for this socket
  //  socket.setMaxListeners(1e7); // 10 MB

    console.log("Client connected");
    socket.on("data", async (data: Buffer) => {
        console.log("recive data from server");
      //  console.log(JSON.parse(data.toString()));
         wsSender.emit(data.toString()); // Emit to WebSocket server
    });

    socket.on("end", () => {
        console.log("Client disconnected");
    });

    socket.on("error", (error: Error) => {
        console.log(`Socket Error: ${error.message}`);
    });
});

server.on("error", (error: Error) => {
    console.log(`Server Error: ${error.message}`);
});

server.listen(listeningPort, () => {
    console.log(`TCP socket server is running on port: ${listeningPort}`);
})