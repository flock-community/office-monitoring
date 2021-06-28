import { deviceStateStore, devicesStore } from "./stores";
import {
  DeviceDto,
  DeviceState,
  FlockMonitorMessage,
  FlockMonitorMessageType,
  MessageDTO,
  StateBody,
} from "./StreamDtos";

export class MessageHandler {
  handleMessage(message: MessageDTO) {
    let data = message.data;
    switch (data.type) {
      case FlockMonitorMessageType.DEVICE_STATE:
        //let state = data.body.state as DeviceState<StateBody>;
        this.handleDeviceStateMessage(data.body.state);
        break;
      case FlockMonitorMessageType.DEVICE_LIST_MESSAGE:
        let newDevices = data.body.devices as DeviceDto[];
        devicesStore.update((devices) => {
          return [...devices, ...newDevices];
        });
        break;
      default:
        console.error(
          `Received unsupported message type: ${message.data.type}`
        );
    }
  }

  handleDeviceStateMessage(message: DeviceState<StateBody>) {
    deviceStateStore.update((map) => {
      const deviceId = message.deviceId;
      const deviceStates: DeviceState<StateBody>[] | undefined =
        map.get(deviceId);
      if (!!deviceStates) {
        const deviceStatesMap = deviceStates as DeviceState<StateBody>[];
        deviceStatesMap.push(message);
        map.set(deviceId, deviceStatesMap);
      } else {
        map.set(deviceId, [message]);
      }

      return map;
    });
  }
}
