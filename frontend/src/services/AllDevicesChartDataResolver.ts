import type { TimelineChartRecord } from "../routes/_dashboard/components/model";
import { getColor, getDeviceName } from "./DeviceUtil";
import { devices, deviceStates } from "./stores";
import type {
  ContactSensorState,
  DeviceState,
  StateBody,
  SwitchState,
  TemperatureSensorState,
} from "./StreamDtos";
import { DeviceType } from "./StreamDtos";

const door = "/icons/door.svg";
const socket = "/icons/socket.svg";
const thermometer = "/icons/thermometer.svg";

export const createChartRecords = (
  statesPerDevice: Map<string, DeviceState<StateBody>[]>
) => {
  let chartRecords: TimelineChartRecord[] = [];

  for (let [_, deviceStates] of statesPerDevice.entries()) {
    if (deviceStates.length > 0) {
      let newChartRecords = [];
      switch (deviceStates[0].type) {
        case DeviceType.CONTACT_SENSOR:
          newChartRecords = createContactDeviceRecords(
            deviceStates as DeviceState<ContactSensorState>[]
          );
          break;
        case DeviceType.TEMPERATURE_SENSOR:
          newChartRecords = createTemperatureDeviceRecords(
            deviceStates as DeviceState<TemperatureSensorState>[]
          );
          break;
        case DeviceType.SWITCH:
          newChartRecords = createSwitchDeviceRecords(
            deviceStates as DeviceState<SwitchState>[]
          );
          break;
      }

      chartRecords = [...chartRecords, ...newChartRecords];
    }
  }

  return chartRecords;
};

const createTemperatureDeviceRecords = (
  deviceStates: DeviceState<TemperatureSensorState>[]
) => {
  const filtered = deviceStates.reduce((acc, curr) => {
    if (acc.length == 0) return [...acc, curr];

    const lastTempState = acc.slice(-1)[0];

    const tempDifference = Math.abs(
      lastTempState.state.temperature - curr.state.temperature
    );

    if (tempDifference > 1) {
      return [...acc, curr];
    } else {
      return acc;
    }
  }, [] as DeviceState<TemperatureSensorState>[]);

  return filtered.map((state) => {
    return {
      category: getDeviceName(state.deviceId),
      start: state.date,
      end: state.date,
      icon: thermometer,
      text: `${getDeviceName(
        state.deviceId
      )} temperatuur veranderd naar [bold]${getTemperatureString(
        state.state.temperature
      )}[/]`,
      color: getColor(state.deviceId),
    } as TimelineChartRecord;
  });
};

const createSwitchDeviceRecords = (
  deviceStates: DeviceState<SwitchState>[]
) => {
  return deviceStates.map((state: DeviceState<SwitchState>, index: number) => {
    if (state.state.state === "ON") {
      let openedOnDate = state.date;

      // The states are per device so the next one must be the close state, if it's not found the sensor is still open
      let closedOnDate =
        deviceStates
          .slice(index)
          .find((s) => s.date > state.date && s.state.state === "OFF")?.date ||
        new Date();

      const record: TimelineChartRecord = {
        category: getDeviceName(state.deviceId),
        start: openedOnDate,
        end: closedOnDate,
        icon: socket,
        text: `${getDeviceName(
          state.deviceId
        )} aangezet tot [bold]${getLocalizedDateString(closedOnDate)}[/]`,
        color: getColor(state.deviceId),
      };
      return record;
    }
  });
};

const createContactDeviceRecords = (
  deviceStates: DeviceState<ContactSensorState>[]
) => {
  return deviceStates
    .map((state: DeviceState<ContactSensorState>, index) => {
      if (state.state.contact === false) {
        let openedOnDate = state.date;

        // The states are per device so the next one must be the close state, if it's not found the sensor is still open
        let closedOnDate =
          deviceStates
            .slice(index)
            .find((s) => s.date > state.date && s.state.contact)?.date ||
          new Date();

        let record: TimelineChartRecord = {
          category: getDeviceName(state.deviceId),
          start: openedOnDate,
          end: closedOnDate,
          icon: door,
          text: `${getDeviceName(
            state.deviceId
          )} geopend tot [bold]${getLocalizedDateString(closedOnDate)}[/]`,
          color: getColor(state.deviceId),
        };
        return record;
      }
    })
    .filter((s) => s !== undefined);
};

const getLocalizedDateString = (date: Date) => {
  return new Date(date).toLocaleString("nl-NL", {
    timeZone: "Europe/Amsterdam",
  });
};

const getTemperatureString = (temp: number) => {
  return temp.toFixed(1) + "°C";
};
