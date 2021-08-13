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
import * as uuid from "uuid";

let backendUrl = process.env._HOST;

type OnConnected = () => void;
type MessageSink = (message: MessageDTO) => void;
type CommandSink = (command: FlockMonitorCommand) => void;

export const connectClient = (
  route: string,
  messageSink: MessageSink,
  onConnected: OnConnected
) => {
  const subscribers = setupRSocketSubcribers(onConnected);

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
        .requestChannel(setupChannel(route, subscribers.commandsFlow))
        .subscribe(createMessageFlow(messageSink));
    },
  });

  return subscribers.commandsSink;
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

const createMessageFlow = (handler: MessageSink) => {
  let localSubscription: ISubscription;

  const subscriber: ISubscriber<MessageDTO> = {
    onComplete: () => {
      console.debug("MessageFlow completed");
    },
    onError: (error) => {
      console.debug("MessageFlow error", error);
    },
    onNext: (value) => {
      console.debug("Incoming message", value);
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

const setupRSocketSubcribers = (onConnected: OnConnected) => {
  // TODO: Dit netjes maken
  let subscriberCredits = 0;
  let commandBuffer = [];

  const subscribers: Map<string, ISubscriber<FlockMonitorCommand>> = new Map();

  const commandsFlow = new Flowable<FlockMonitorCommand>(
    (subscriber: ISubscriber<FlockMonitorCommand>) => {
      const subscriberId = uuid.v4();
      subscribers.set(subscriberId, subscriber);

      subscriber.onSubscribe({
        cancel(): void {
          subscribers.delete(subscriberId);
        },
        request(n: number): void {
          console.log(`[EventBus] New deviceCommands are requested (#${n}) `);
          subscriberCredits = subscriberCredits + n;
          clearCommandBuffer(subscriberId);
        },
      });

      onConnected();
    }
  );

  const clearCommandBuffer = (subscriberId) => {
    console.log(commandBuffer);
    commandBuffer = commandBuffer.filter((command) => {
      if (subscriberCredits > 0) {
        const subscriber = subscribers.get(subscriberId);
        subscriber.onNext(command);
        subscriberCredits--;
        return false;
      } else {
        return true;
      }
    });

    console.log("After: " + commandBuffer);
  };

  const commandSink: CommandSink = (command: FlockMonitorCommand) => {
    subscribers.forEach(
      (subscriber: ISubscriber<FlockMonitorCommand>, key: string) => {
        if (subscriberCredits > 0) {
          subscriber.onNext(command);
          subscriberCredits--;
        } else {
          commandBuffer.push(command);
        }
      }
    );
  };

  return {
    commandsFlow: commandsFlow,
    commandsSink: commandSink,
  };
};
