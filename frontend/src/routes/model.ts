export interface DeviceState {
  id: String;
  type: DeviceType;
  deviceId: String;
  date: Date;
  body: StateBody;
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

export interface TimelineChartRecord {
  category: String;
  start: Date;
  end: Date;
  //color: colorSet.getIndex(0),
  task: String;
}
