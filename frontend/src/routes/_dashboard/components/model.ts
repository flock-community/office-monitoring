import type { Color } from "@amcharts/amcharts4/core";

export interface TimelineChartRecord {
  category: string;
  start: Date;
  end: Date;
  icon: string;
  text: string;
  color: Color;
  // task: String;
}

export interface LineChartRecord {
  name: string;
  date: Date;
  value: number;
  text: string;
}

export interface LineChartDateRecord {
  date: Date;
  events: DateEvent[];
}

export interface DateEvent {
  name: string;
  value: number;
  text: string;
}
