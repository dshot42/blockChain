import { Server, Socket } from 'socket.io';
import { createServer, Server as HttpServer } from 'http';

export class WebSocketServer {
    private io: Server;
    private httpServer: HttpServer;

    constructor(port: number = 3001) { // front need to listen this port
        this.httpServer = createServer();
        
        this.io = new Server(this.httpServer, {
              cors: {
                origin: "*",
                methods: ["GET", "POST"]           
             }
          //  maxHttpBufferSize: 1e7, // 10 MB
        });
        

        this.io.on('connection', (socket: Socket) => {
            console.log('Client connected:', socket.id);

            socket.on('message', (msg: string) => {
                console.log('Received from client:', msg);
                socket.emit('message', `Echo: ${msg}`);
            });

            socket.emit('message', 'Welcome to the Socket.IO server!');
        });

        this.httpServer.listen(port, () => {
            console.log(`WebSocket server running on port ${port}`);
        });
    }


     emit(msg: string){
        this.io.emit('wallet', msg); // sends only to this client
        console.log('Emitting message to client');
    }
}