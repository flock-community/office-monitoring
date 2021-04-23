<script lang="ts">
  import { onMount } from "svelte";
  import * as am4core from "@amcharts/amcharts4/core";
  import * as am4charts from "@amcharts/amcharts4/charts";
  import type { LineChartDateRecord, LineChartRecord } from "../model";
  import type { Writable } from "svelte/store";

  export let chartRecordStore: Writable<LineChartRecord[]>;

  onMount(async () => {
    am4core.ready(onready);

    chartRecordStore.subscribe((records) => {
      //console.log("ChartData", records);
      chart.data = records;
      //chart.addData(records.slice(-1)[0]);
    });
  });

  let chart: am4charts.XYChart;

  // let testData: LineChartDateRecord[] = [
  //     {
  //         date: new Date(new Date().getTime() - 1 * 60000),
  //         events: [
  //             {
  //                 name: "Koelkast",
  //                 text: "text",
  //                 value: 21,
  //             },
  //         ],
  //     },
  //     {
  //         date: new Date(new Date().getTime() - 2 * 60000),
  //         events: [
  //             {
  //                 name: "Koelkast",
  //                 text: "text",
  //                 value: 20,
  //             },
  //         ],
  //     },
  //     {
  //         date: new Date(new Date().getTime() - 3 * 60000),
  //         events: [
  //             {
  //                 name: "Koelkast",
  //                 text: "text",
  //                 value: 19,
  //             },
  //         ],
  //     },
  // ];

  function onready() {
    chart = am4core.create("linechart", am4charts.XYChart);

    let dateAxis = chart.xAxes.push(new am4charts.DateAxis());
    dateAxis.renderer.grid.template.location = 0.5;
    dateAxis.interpolationDuration = 500;
    dateAxis.rangeChangeDuration = 500;

    let valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
    // Add scrollbar
    chart.scrollbarX = new am4charts.XYChartScrollbar();

    function addSeries(tempValueField) {
      const series = chart.series.push(new am4charts.LineSeries());
      series.dataFields.valueY = tempValueField;
      series.dataFields.dateX = "date";
      series.strokeWidth = 2;
      series.minBulletDistance = 10;
      series.tooltipText = "{text}";
      series.tooltip.pointerOrientation = "vertical";
      series.tooltip.background.cornerRadius = 20;
      series.tooltip.background.fillOpacity = 0.5;
      series.tooltip.label.padding(12, 12, 12, 12);
      series.tensionX = 0.8;
      series.interpolationDuration = 500;
      series.defaultState.transitionDuration = 0;

      const bullet = series.createChild(am4charts.CircleBullet);
      bullet.circle.radius = 5;
      bullet.fillOpacity = 1;
      bullet.fill = chart.colors.getIndex(0);
      bullet.isMeasured = false;

      series.events.on("validated", function () {
        bullet.moveTo(series.dataItems.last.point);
        bullet.validatePosition();
      });
    }

    addSeries("value");

    // Add cursor
    chart.cursor = new am4charts.XYCursor();
    chart.cursor.xAxis = dateAxis;
    chart.cursor.dateFormatter = new am4core.DateFormatter();
    chart.cursor.dateFormatter.dateFormat = "MM-dd";
  }
</script>

<div class="w-full h-full" id="linechart" />
