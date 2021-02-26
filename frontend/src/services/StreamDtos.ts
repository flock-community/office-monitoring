export interface DeviceDto {
    id: String;
    name: String;
    type: String;
}

export interface DeviceCommandDTO<T>{
    data: T;
    metadata: string;
}

export class BaseDeviceCommandDTO<T> implements  DeviceCommandDTO<T>{
    data: T;
    metadata: string;
    constructor(data: T, metadata: string) {
        this.data = data
        this.metadata = metadata
    }
}

export interface FlockMonitorCommand<T> {
    data: T
}

class BaseCommand<T> implements FlockMonitorCommand<T>{
    data: T;
    constructor(command: T) {
        this.data = command
    }


}

export class DeviceSubscription extends BaseCommand<DeviceX>{}

export class DeviceX{
    id: String
    constructor(id) {
        this.id =id
    }
}
