import {IdentitySerializer, JsonSerializer, RSocketClient,} from "rsocket-core";
import RSocketWebSocketClient from "rsocket-websocket-client";
import {
  BaseCommandDTO,
  CommandDTO,
  FlockMonitorCommand,
} from "./StreamDtos";
import { Flowable } from "rsocket-flowable";
import * as rxjsop from "rxjs/operators";

import type * as rxjs from "rxjs";
import type { Observable, Subscription } from "rxjs";
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

export const connectSocket = (
  route: string,
  commands: rxjs.Observable<FlockMonitorCommand>
) => {
  const store = []; // writable(Array());

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
    onComplete: (socket) => {
      socket
        .requestChannel(setupChannel(route, commands))
        // .requestStream({
        //     data: {},
        //     metadata: getMetadata(route),
        // })
        .subscribe({
          onComplete: () => console.debug("RSocket requestStream completed"),
          onError: (error) => {
            console.debug("RSocket requestStream error", error);
          },
          onNext: (value) => {
            // console.debug("RSocket requestStream onNext:", value);
            // store.update((devices) =>
            //   devices.concat(value.data.body.devices as DeviceDto)
            // );
          },
          // Nothing happens until `request(n)` is called
          onSubscribe: (subscription) => {
            console.debug(
              "RSocket requestStream received onSubscribed (from server)"
            );
          },
        });
    },
    onError: (error) => {
      console.warn("RSocket connection error", error);
    },
    onSubscribe: (cancel) => {
      console.debug(`RSocket connection subscription on '${route}'`);
    },
  });

  return store;
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

const setupChannel = (
  route: string,
  commands: Observable<FlockMonitorCommand>
) => {
  console.debug("Setting up subscription for commands from application");
  let subscription: Subscription | undefined = undefined;

  return new Flowable<CommandDTO>((subscriber) => {
    const processCommand = (command: FlockMonitorCommand) => {
      console.debug("Process command", command);

      let deviceCommandDto = new BaseCommandDTO(
        command,
        getMetadata(route)
      );
      subscriber.onNext(deviceCommandDto);
      return deviceCommandDto;
    };

    const processError = (err) => {
      console.warn("Processing error", err);
      subscriber.onError(err);
      throw err;
    };

    const something = commands.pipe(
      rxjsop.map((command) => processCommand(command)),
      rxjsop.catchError((err, _) => processError(err))
    );

    subscriber.onSubscribe({
      cancel(): void {
        console.warn("Cancel is called");
        // Our subscriber send us a cancel signal
        // 1. ensure request: n are stopped.
        // ?? (cancel subscription to `commands`)
        // console.warn("Not unsubscribing from subscription to backend")
        console.info("Unsubscribing from application observable..");
        subscription.unsubscribe();

        // 2. Send completion signal (cause convention?)
        // subscriber.onComplete()
      },
      request: (n) => {
        console.log(`Request '${n}' is called`);

        // TODO:  Deal with request `n` (backpressure) properly
        // for(let i = 0; i < n; i++) {
        // }

        if (!subscription) {
          subscription = something.subscribe(
            // Added these logs for debug purposes
            (command) => console.debug(`Received another command:`, command),
            (error) => console.error(`Error occurred`, error),
            () => console.log("Subscription is finished")
          );
        }
      },
    });
  });
};
