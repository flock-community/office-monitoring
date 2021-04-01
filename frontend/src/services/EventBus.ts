import type { ISubscriber } from "rsocket-types";
import {
  DeviceDto,
  DeviceState,
  FlockMonitorCommand,
  FlockMonitorMessage,
  FlockMonitorMessageType,
  MessageDTO,
  StateBody,
} from "./StreamDtos";
import { Flowable } from "rsocket-flowable";
import * as uuid from "uuid";
import { connectClient } from "./RSocket";
import { devicesStore, deviceStateStore } from "./stores";

const subscribers: Map<string, ISubscriber<FlockMonitorCommand>> = new Map();

let subscriptionX;

const handleMessage = (value: MessageDTO) => {
  let data: FlockMonitorMessage = value.data as FlockMonitorMessage;
  switch (data.type) {
    case FlockMonitorMessageType.DEVICE_STATE:
      let state = data.body.state as DeviceState<any>;

      deviceStateStore.update((map) => {
        const deviceId = state.deviceId;
        const savedStates: DeviceState<StateBody>[] | undefined = map.get(deviceId);
        if (!!savedStates) {
          const safeSavedStates = savedStates as DeviceState<StateBody>[];
          safeSavedStates.push(state);
          map.set(deviceId, safeSavedStates);
        } else {
          map.set(deviceId, [state]);
        }

        return map
      })
      break;
    case FlockMonitorMessageType.DEVICE_LIST_MESSAGE:
      let newDevices = data.body.devices as DeviceDto[];
      devicesStore.update((devices) => {
            // console.debug("upserting devicesStore with: ", newDevices);
            return [...devices, ...newDevices]
      }
      );
      break;
    default:
      console.error(`Received unsupported message type: ${value.data.type}`)
  }
};

const messagesFlow: ISubscriber<MessageDTO> = {
  onComplete: () => {
    console.debug("MessageFlow completed");
  },
  onError: (error) => {
    console.debug("MessageFlow error", error);
  },
  onNext: (value ) => {
    // console.debug("MessageFlow onNext:", value);
    handleMessage(value);

    // console.debug("Requesting another message");
    setTimeout(subscriptionX.request(1),50);
  },
  // Nothing happens until `request(n)` is called
  onSubscribe: (subscription) => {
    console.debug("MessageFlow received onSubscribed (from server)");
    // TODO: backpressure and stuff!
    subscription.request(10);
    subscriptionX = subscription;
  },
};

const commandsFlow = new Flowable<FlockMonitorCommand>(
  (subscriber: ISubscriber<FlockMonitorCommand>) => {
    const myId = uuid.v4();
    subscribers.set(myId, subscriber);

    subscriber.onSubscribe({
      cancel(): void {
        subscribers.delete(myId);
      },
      request(n: number): void {
        console.log(`[EventBus] New deviceCommands are requested (#${n}) `);
      },
    });
  }
);

const request = (event: FlockMonitorCommand, tries = 3) => {
  if (subscribers.size < 1) {
    console.log("[EventBus] No subscribers yet, trying again to publish command in one second", event);
    if (tries-- > 1 )setTimeout(() => request(event, tries), 1500);
  } else {
    console.log("[Event] Publishing command: ", event);
    subscribers.forEach(
      (subscriber: ISubscriber<FlockMonitorCommand>, key: string) => {
        subscriber.onNext(event);
      }
    );
  }
};

// On start
connectClient("devices", commandsFlow, messagesFlow);

const eventBus = {
  request,
};
export default eventBus;
