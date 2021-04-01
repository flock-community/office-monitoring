import type { Color } from "@amcharts/amcharts4/core";

export interface TimelineChartRecord {
  category: string;
  start: Date;
  end: Date;
  icon: String;
  text: String;
  color: Color;
  // task: String;
}
