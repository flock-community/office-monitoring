<script lang="ts">
  import { onMount } from "svelte";
  import * as am4core from "@amcharts/amcharts4/core";
  import * as am4charts from "@amcharts/amcharts4/charts";
  import type { LineChartDateRecord, LineChartRecord } from "./model";
  import type { Writable } from "svelte/store";

  export let chartRecordStore: Writable<any[]>;
  export let tempSensorIds: string[];

  onMount(async () => {
    am4core.ready(onready);

    chartRecordStore.subscribe((records) => {
      chart.data = records;
    });
  });

  let chart: am4charts.XYChart;

  function onready() {
    chart = am4core.create("linechart", am4charts.XYChart);
    chart.logo.disabled = true;

    let dateAxis = chart.xAxes.push(new am4charts.DateAxis());
    dateAxis.renderer.grid.template.location = 0;
    dateAxis.interpolationDuration = 500;
    dateAxis.rangeChangeDuration = 500;
    dateAxis.tooltipDateFormat = "HH:mm:ss";

    let valueAxis = chart.yAxes.push(new am4charts.ValueAxis());

    function addSeries(tempValueField) {
      const series = chart.series.push(new am4charts.LineSeries());
      series.dataFields.valueY = tempValueField;
      series.dataFields.dateX = "date";
      series.strokeWidth = 2;
      series.minBulletDistance = 10;
      series.tooltipText = "{" + tempValueField + "}";
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

    chart.scrollbarX = new am4charts.XYChartScrollbar();
    chart.cursor = new am4charts.XYCursor();
    chart.cursor.xAxis = dateAxis;

    tempSensorIds.forEach((id) => addSeries(id));
  }
</script>

<div class="w-full h-full" id="linechart" />
