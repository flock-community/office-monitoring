import type { ISubscriber } from "rsocket-types";
import type { DeviceDto, FlockMonitorCommand } from "./StreamDtos";
import { Flowable } from "rsocket-flowable";
import * as uuid from "uuid";
import { connectClient } from "./RSocket";
import { devicesStore } from "./stores";

const subscribers: Map<string, ISubscriber<FlockMonitorCommand>> = new Map();

let subscriptionX;
const messagesFlow: ISubscriber<any> = {
  onComplete: () => {
    console.debug("MessageFlow completed");
    // subscribers.forEach(x => x.onComplete())
  },
  onError: (error) => {
    console.debug("MessageFlow error", error);
    // subscribers.forEach(x => x.onError(error))
  },
  onNext: (value) => {
    console.debug("MessageFlow onNext:", value);
    // TODO: unpack value and store in correct store
    devicesStore.update((devices) =>
      devices.concat(value.data.body.devices as DeviceDto)
    );

    setTimeout(subscriptionX.request(1));
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

const request = (event: FlockMonitorCommand) => {
  if (subscribers.size < 1) {
    console.log("[EventBus] No subscribers yet, trying again to publish command in one second", event);
    setTimeout(() => request(event), 1500);
  } else {
    console.log("[Event] Publishing command: ", event);
    subscribers.forEach(
      (subscriber: ISubscriber<FlockMonitorCommand>, key: string) => {
        subscriber.onNext(event);
      }
    );
  }

  return;
};

// On start
connectClient("devices", commandsFlow, messagesFlow);

const eventBus = {
  request,
};
export default eventBus;
