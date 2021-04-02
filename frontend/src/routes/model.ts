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
