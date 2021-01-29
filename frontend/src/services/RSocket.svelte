<script context="module" lang="ts">
  import {
    IdentitySerializer,
    JsonSerializer,
    RSocketClient,
  } from "rsocket-core";
  import RSocketWebSocketClient from "rsocket-websocket-client";
  import { writable } from "svelte/store";

  let backendUrl = process.env.BACKEND_URL

  export const connectSocket = (route: String) => {

    const store = writable(Array());

    const client = new RSocketClient({
      serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer,
      },
      setup: {
        keepAlive: 60000,
        lifetime: 180000,
        dataMimeType: "application/json",
        metadataMimeType: "message/x.rsocket.routing.v0",
      },
      transport: new RSocketWebSocketClient({
        url: backendUrl + route,
      }),
    });

    let metadata = String.fromCharCode(route.length) + route;

    client.connect().subscribe({
      onComplete: (socket) => {
        socket
          .requestStream({
            data: null,
            metadata: metadata,
          })
          .subscribe({
            onComplete: () => console.log("requestStream done"),
            onError: (error) => {
              console.log("got error with requestStream");
              console.error(error);
            },
            onNext: (value) => {
              console.log("Received: " + value.data);
              store.update((devices) => devices.concat(value.data as DeviceDto));
            },
            // Nothing happens until `request(n)` is called
            onSubscribe: (sub) => {
              console.log("subscribe request Stream!");
              sub.request(7);
            },
          });
      },
      onError: (error) => {
        console.log(error);
      },
      onSubscribe: (cancel) => {
        console.log(`subscribed for`);
      },
    });

    return store
  };
</script>
