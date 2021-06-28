import {
  IdentitySerializer,
  JsonSerializer,
  RSocketClient,
} from "rsocket-core";
import RSocketWebSocketClient from "rsocket-websocket-client";
import {
  BaseCommandDTO,
  CommandDTO,
  FlockMonitorCommand,
  MessageDTO,
} from "./StreamDtos";
import { Flowable } from "rsocket-flowable";
import type { ISubscriber, ISubscription } from "rsocket-types";
import { Subscriber } from "rxjs";
import * as uuid from "uuid";

let backendUrl = process.env._HOST;

type OnConnected = () => void;

export const connectClient = (
  route: string,
  messageHandler: MessageHandler,
  onConnected: OnConnected
) => {
  const commandFlow = createCommandsFlow(onConnected);

  const client = createClient(backendUrl + route);
  client.connect().subscribe({
    onSubscribe: (cancel: () => void) => {
      console.debug(`RSocket connection subscription on '${route}'`);
    },
    onError: (error) => {
      console.warn("RSocket connection error", error);
    },
    onComplete: (socket) => {
      socket
        .requestChannel(setupChannel(route, commandFlow.subscriberFlow))
        .subscribe(createMessageFlow(messageHandler));
    },
  });

  return commandFlow.sink;
};

const setupChannel = (
  route: string,
  commands: Flowable<FlockMonitorCommand>
): Flowable<CommandDTO> => {
  return commands.map((command) => {
    console.debug("Process command", command);
    return new BaseCommandDTO(command, getMetadata(route));
  });
};

const getMetadata = (route: string) =>
  String.fromCharCode(route.length) + route;

const createClient = (url: string) => {
  return new RSocketClient({
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
      url: url,
    }),
  });
};

const createMessageFlow = (handler: MessageHandler) => {
  let localSubscription: ISubscription;

  const subscriber: ISubscriber<MessageDTO> = {
    onComplete: () => {
      console.debug("MessageFlow completed");
    },
    onError: (error) => {
      console.debug("MessageFlow error", error);
    },
    onNext: (value) => {
      console.info("MessageFlow message", value);
      handler(value);

      setTimeout(() => {
        localSubscription.request(1);
      }, 10);
    },
    onSubscribe: (subscription) => {
      subscription.request(100);
      localSubscription = subscription;
    },
  };
  return subscriber;
};

const createCommandsFlow = (onConnected: OnConnected) => {
  const subscribers: Map<string, ISubscriber<FlockMonitorCommand>> = new Map();

  const subscriberFlow = new Flowable<FlockMonitorCommand>(
    (subscriber: ISubscriber<FlockMonitorCommand>) => {
      const subscriberId = uuid.v4();
      subscribers.set(subscriberId, subscriber);

      subscriber.onSubscribe({
        cancel(): void {
          subscribers.delete(subscriberId);
        },
        request(n: number): void {
          console.log(`[EventBus] New deviceCommands are requested (#${n}) `);
        },
      });

      onConnected();
    }
  );

  const commandSink: CommandSink = (event: FlockMonitorCommand) => {
    subscribers.forEach(
      (subscriber: ISubscriber<FlockMonitorCommand>, key: string) => {
        subscriber.onNext(event);
      }
    );
  };

  return {
    subscriberFlow: subscriberFlow,
    sink: commandSink,
  };
};

type MessageHandler = {
  (message: MessageDTO);
};

type CommandSink = {
  (command: FlockMonitorCommand);
};
