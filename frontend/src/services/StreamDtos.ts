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
