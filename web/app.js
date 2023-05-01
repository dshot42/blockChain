const net = require("net");

const port = 3000;


const server = net.createServer((socket) => {
    console.log("Client connected");

    socket.on("data", (data) => {

        console.log(data.toString());
	//	let obj = JSON.parse(data.toString());
		//    console.log(`json: `+obj);
		//io.to('127.0.0.1:8888').emit(data.toString());
       socket.emit('sendFront', data.toString());
    });

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

server.listen(port, () => {
    console.log(`TCP socket server is running on port: ${port}`);
});