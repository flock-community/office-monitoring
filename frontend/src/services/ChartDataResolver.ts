import type { TimelineChartRecord } from "../routes/_components/modelboard/components/model";
import { getColor, getDeviceName } from "./DeviceUtil";
import type {
  ContactSensorState,
  DeviceState,
  StateBody,
  SwitchState,
  TemperatureSensorState,
} from "./StreamDtos";
import { DeviceType } from "./StreamDtos";

const door = "/icons/door.svg";
const unknown = "/icons/shrug.svg";
const socket = "/icons/socket.svg";
const thermometer = "/icons/thermometer.svg";

export const resolveChartData: (
  deviceStates: Map<string, DeviceState<StateBody>[]>
) => TimelineChartRecord[] = (
  deviceStates: Map<string, DeviceState<StateBody>[]>
) => {
  let allChartData: TimelineChartRecord[] = [];
  for (let [_, deviceStateArray] of deviceStates.entries()) {
    if (deviceStateArray.length > 0) {
      let chartData = [];
      switch (deviceStateArray[0].type) {
        case DeviceType.CONTACT_SENSOR:
          chartData = convertToChartDataContact(
            deviceStateArray as DeviceState<ContactSensorState>[]
          );
          break;
        case DeviceType.TEMPERATURE_SENSOR:
          chartData = convertToChartDataTemp(
            deviceStateArray as DeviceState<TemperatureSensorState>[]
          );
          break;
        case DeviceType.SWITCH:
          chartData = convertToChartDataSwitch(
            deviceStateArray as DeviceState<SwitchState>[]
          );
          break;
      }

      allChartData = [...allChartData, ...chartData];
    }
  }

  return allChartData;
};

const convertToChartDataTemp = (
  deviceStates: DeviceState<TemperatureSensorState>[]
) => {
  let last: DeviceState<TemperatureSensorState>;
  return deviceStates
    .map((state: DeviceState<TemperatureSensorState>) => {
      if (
        !!last &&
        Math.abs(last.state.temperature - state.state.temperature) < 2 &&
        state !== deviceStates[deviceStates.length - 1]
      ) {
        return undefined;
      }
      last = state;

      let record: TimelineChartRecord = {
        category: getDeviceName(state.deviceId),
        start: state.date,
        end: state.date,
        icon: thermometer,
        text: `${getDeviceName(
          state.deviceId
        )} temperatuur veranderd naar [bold]${state.state.temperature.toFixed(
          1
        )}°C[/]`,
        color: getColor(state.deviceId),
      };
      return record;
    })
    .filter((s) => s !== undefined);
};

const convertToChartDataSwitch = (deviceStates: DeviceState<SwitchState>[]) => {
  return deviceStates
    .map((state: DeviceState<SwitchState>, index: number) => {
      if (state.state.state === "ON") {
        let openedOnDate = state.date;

        // The states are per device so the next one must be the close state, if it's not found the sensor is still open
        let closedOnDate =
          deviceStates
            .slice(index)
            .find((s) => s.date > state.date && s.state.state === "OFF")
            ?.date || new Date();

        const localizedDate = new Date(closedOnDate).toLocaleString("nl-NL", {
          timeZone: "Europe/Amsterdam",
        });
        const record: TimelineChartRecord = {
          category: getDeviceName(state.deviceId),
          start: openedOnDate,
          end: closedOnDate,
          icon: socket,
          text: `${getDeviceName(
            state.deviceId
          )} aangezet tot [bold]${localizedDate}[/]`,
          color: getColor(state.deviceId),
        };
        return record;
      }
    })
    .filter((s) => s !== undefined);
};

const convertToChartDataContact = (
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

        const localizedDate = new Date(closedOnDate).toLocaleString("nl-NL", {
          timeZone: "Europe/Amsterdam",
        });
        let record: TimelineChartRecord = {
          category: getDeviceName(state.deviceId),
          start: openedOnDate,
          end: closedOnDate,
          icon: door,
          text: `${getDeviceName(
            state.deviceId
          )} geopend tot [bold]${localizedDate}[/]`,
          color: getColor(state.deviceId),
        };
        return record;
      }
    })
    .filter((s) => s !== undefined);
};
