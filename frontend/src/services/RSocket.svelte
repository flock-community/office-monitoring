<script context="module" lang="ts">
  import {
    IdentitySerializer,
    JsonSerializer,
    RSocketClient,
  } from "rsocket-core";
  import RSocketWebSocketClient from "rsocket-websocket-client";
  import { writable } from "svelte/store";

  export interface DeviceDto {
    id: String;
    name: String;
    type: String;
  }

  export const connectSocket = (route: String) => {
    const { subscribe, set, update } = writable(Array<DeviceDto>());

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
        url: "ws://localhost:9000/ws/" + route,
      }),
    });
    let onNext = (payload) => {
      console.log(payload);
    };

    let onSubscribe = (sub) => {
      sub.request(2147483647);
    };

    const requestParams = {
      deviceId: "1234abc",
      deviceType: "CONTACT_SENSOR",
      since: new Date().toISOString(),
    };
    let metadata = String.fromCharCode(route.length) + route;

    client.connect().subscribe({
      onComplete: (socket) => {
        socket
          .requestStream({
            data: requestParams,
            metadata: metadata,
          })
          .subscribe({
            onComplete: () => console.log("requestStream done"),
            onError: (error) => {
              console.log("got error with requestStream");
              console.error(error);
            },
            onNext: (value) => {
              console.log(value.data);

              update((devices) => devices.concat(value.data as DeviceDto));
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

    console.log(client);

    return {
      subscribe,
    };
  };
</script>
