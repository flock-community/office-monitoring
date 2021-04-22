import {
  IdentitySerializer,
  JsonSerializer,
  RSocketClient,
} from "rsocket-core";
import RSocketWebSocketClient from "rsocket-websocket-client";
import { BaseCommandDTO, CommandDTO, FlockMonitorCommand } from "./StreamDtos";
import type { Flowable } from "rsocket-flowable";
import type { ISubscriber } from "rsocket-types";

let backendUrl = process.env._HOST;
export const connectClient = (
  route: string,
  commands: Flowable<FlockMonitorCommand>,
  subscriber: ISubscriber<FlockMonitorCommand>
) => {
  // const store = writable(Array());

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

  client.connect().subscribe({
    onSubscribe: (cancel: () => void) => {
      console.debug(`RSocket connection subscription on '${route}'`);
      // TODO: propagate cancellation back?
    },
    onError: (error) => {
      console.warn("RSocket connection error", error);
    },
    onComplete: (socket) => {
      socket
        .requestChannel(setupChannel2(route, commands))
        .subscribe(subscriber);
    },
  });

  // return cancellation
};
const getMetadata = (route: string) =>
  String.fromCharCode(route.length) + route;

const setupChannel2 = (
  route: string,
  commands: Flowable<FlockMonitorCommand>
): Flowable<CommandDTO> => {
  return commands.map((command) => {
    console.debug("Process command", command);
    return new BaseCommandDTO(command, getMetadata(route));
  });
};
