export interface DeviceDto {
  id: String;
  name: String;
  type: DeviceType;
}

export interface DeviceState<T extends StateBody> {
  id: String;
  type: DeviceType;
  deviceId: String;
  date: Date;
  body: T;
}

export enum DeviceType {
  CONTACT_SENSOR,
}

export interface StateBody {}

export interface ContactSensorState extends StateBody {
  lastSeen: Date;
  battery: Number;
  voltage: Number;
  contact: Boolean;
}

export interface DeviceStateDto {}

export interface DeviceCommandDTO {
  data: FlockMonitorCommand;
  metadata: string;
}

export class BaseDeviceCommandDTO implements DeviceCommandDTO {
  data: FlockMonitorCommand;
  metadata: string;

  constructor(data: FlockMonitorCommand, metadata: string) {
    this.data = data;
    this.metadata = metadata;
  }
}

export interface FlockMonitorCommand {}

enum FlockMonitorCommandType {
  GET_DEVICES_COMMAND = "GET_DEVICES_COMMAND",
  GET_DEVICE_STATE_COMMAND = "GET_DEVICE_STATE_COMMAND",
}
enum FlockMonitorMessageType {
  // Messages
  DEVICE_LIST_MESSAGE = "DEVICE_LIST_MESSAGE",
  DEVICE_STATE = "DEVICE_STATE"
}


class BaseCommand implements FlockMonitorCommand {
  type: FlockMonitorCommandType;
  body: FlockMonitorCommand;

  constructor(type: FlockMonitorCommandType, body: FlockMonitorCommand) {
    this.type = type;
    this.body = body;
  }
}

export class DeviceSubscription extends BaseCommand {
  constructor() {
    super(FlockMonitorCommandType.GET_DEVICES_COMMAND, {});
  }
}

export interface FlockMonitorMessage {
    type: FlockMonitorMessageType,
    body: any //FlockMonitorMessageBody
}
