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
      const currentDeviceStates: DeviceState<StateBody>[] | undefined =
        map.get(deviceId);
      if (!!currentDeviceStates) {
        currentDeviceStates.push(message);
        currentDeviceStates.sort((a, b) => {
          return new Date(a.date).getTime() - new Date(b.date).getTime();
        });
        map.set(deviceId, currentDeviceStates);
      } else {
        map.set(deviceId, [message]);
      }

      return map;
    });
  }

  private handleDeviceListMessage(newDevices: DeviceDto[]) {
    devices.update((_) => {
      return [...newDevices];
    });
  }
}
