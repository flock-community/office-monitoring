export interface DeviceDto {
  id: String;
  name: String;
  type: String;
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
