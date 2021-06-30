import { deviceStates, devices } from "./stores";
import {
  DeviceDto,
  DeviceState,
  FlockMonitorMessageType,
  MessageDTO,
  StateBody,
} from "./StreamDtos";

export class MessageHandler {
  handleMessage(message: MessageDTO) {
    let data = message.data;
    switch (data.type) {
      case FlockMonitorMessageType.DEVICE_STATE:
        this.handleDeviceStateMessage(data.body.state);
        break;
      case FlockMonitorMessageType.DEVICE_LIST_MESSAGE:
        this.handleDeviceListMessage(data.body.devices);
        break;
      default:
        console.error(
          `Received unsupported message type: ${message.data.type}`
        );
    }
  }

  private handleDeviceStateMessage(message: DeviceState<StateBody>) {
    deviceStates.update((map) => {
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

  private handleDeviceListMessage(newDevices: DeviceDto[]) {
    devices.update((savedDevices) => {
      return [...savedDevices, ...newDevices];
    });
  }
}
