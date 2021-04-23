export interface DeviceDto {
  id: string;
  name: string;
  type: DeviceType;
}

export interface DeviceState<T extends StateBody> {
  id: string;
  type: DeviceType;
  deviceId: string;
  date: string;
  state: T;
}

export enum DeviceType {
  CONTACT_SENSOR = "CONTACT_SENSOR",
  TEMPERATURE_SENSOR = "TEMPERATURE_SENSOR",
  SWITCH = "SWITCH",
}

export interface StateBody {}

export interface ContactSensorState extends StateBody {
  lastSeen: Date;
  battery: number;
  voltage: number;
  contact: boolean;
}

export interface TemperatureSensorState {
  lastSeen: Date;
  battery: number;
  voltage: number;
  humidity: number;
  pressure: number;
  temperature: number;
}

export interface SwitchState {
  lastSeen: Date;
  state: string;
}

export interface DeviceStateDto {}

export interface MessageDTO {
  data: FlockMonitorMessage;
  metadata: string;
}

export interface CommandDTO {
  data: FlockMonitorCommand;
  metadata: string;
}

export class BaseCommandDTO implements CommandDTO {
  data: FlockMonitorCommand;
  metadata: string;

  constructor(data: FlockMonitorCommand, metadata: string) {
    this.data = data;
    this.metadata = metadata;
  }
}

export interface FlockMonitorCommand {}

export enum FlockMonitorCommandType {
  GET_DEVICES_COMMAND = "GET_DEVICES_COMMAND",
  GET_DEVICE_STATE_COMMAND = "GET_DEVICE_STATE_COMMAND",
}

export enum FlockMonitorMessageType {
  // Messages
  DEVICE_LIST_MESSAGE = "DEVICE_LIST_MESSAGE",
  DEVICE_STATE = "DEVICE_STATE",
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

export class DeviceStateSubscription extends BaseCommand {
  constructor(deviceId: string, from: Date) {
    super(FlockMonitorCommandType.GET_DEVICE_STATE_COMMAND, {
      deviceId: deviceId,
      from: from,
    });
  }
}

export interface FlockMonitorMessage {
  type: FlockMonitorMessageType;
  body: any; //FlockMonitorMessageBody
}
