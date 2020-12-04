const {
    RSocketClient,
    JsonSerializer,
    IdentitySerializer
} = require('rsocket-core');

const RSocketWebSocketClient = require('rsocket-websocket-client').default;


export const createRSocketClient = () => {
    // Create an instance of a client
    return new RSocketClient({
        serializers: {
            data: JsonSerializer,
            metadata: IdentitySerializer
        },
        setup: {
            // ms btw sending keepalive to server
            keepAlive: 60000,
            // ms timeout if no keepalive response
            lifetime: 180000,
            // format of `data`
            dataMimeType: 'application/json',
            // format of `metadata`
            metadataMimeType: 'message/x.rsocket.routing.v0',
        },
        transport: new RSocketWebSocketClient({
            url: `ws://${process.env.HOST}/ws`
        }),
    });
};

export const connectAndSubscribeToEndpoint = (client, route, onNext, onSubscribe, onComplete= () => {}, onError = (e)=> {}) => {
    let metadata = String.fromCharCode(route.length) + route;

    return  client.connect().subscribe({
        onComplete: socket => {
            // socket provides the rsocket interactions fire/forget, request/response,
            // request/stream, etc as well as methods to close the socket.
            socket.requestStream({
                data: null,
                metadata: metadata,
            }).subscribe({
                onComplete: () => {
                    console.log('complete')
                    onComplete()
                },
                onError: error => {
                    console.log(error);
                    onError(error)
                },
                onNext: onNext,
                onSubscribe: onSubscribe,

            });
        },
        onError: error => {
            console.log(error);
            onError(error)

        },
        onSubscribe: cancel => {
            console.log(`subscribed for ${route}`)
        }
    });

};