<template>
	<div>
		<div class="echarts" :id="id" v-loading="chartData.loading" :style="{'height': chartHeight}"></div>
		<el-row :gutter="20">
			<el-col :span="16">&nbsp;</el-col>
			<el-col :span="8" >
				<div class="chart-title">{{maxLabel}}</div>
			</el-col>
		</el-row>
		<el-row :gutter="20" class="details-content">
			<el-col :span="16">
				<div class="line-grid-content">
					<div class="item" v-for="(yaxisItem) in yaxisList">
						<span class="item-head" :style="{'background': yaxisItem.color}"></span><span>{{yaxisItem.alias}}</span>
					</div>
				</div>
			</el-col>
			<el-col :span="8">
				<div class="line-grid-content">
					<div class="item" v-for="(yaxisItem) in yaxisList">
						<span v-if="yaxisItem.max != null">{{yaxisItem.max}} {{yunit}}</span>
					</div>
				</div>
			</el-col>
		</el-row>
	</div>
</template>

<script>
import * as echarts from 'echarts'

export default {
	data() {
		return {
			chart: null,
			title: null,
			yunit: null,
			maxLabel: null,
			yaxisList: []
		}
	},
	props: {
		id: {
			default: 'echart'
		},
		chartHeight: {
			default: '350px'
		},
		chartData: Object
	},
	mounted() {
		this.initChart();
		this.drawChart();
	},
	watch: {
		chartData: function(val, oldVal) {
			this.drawChart();
		}
	},
	methods: {
		initChart() {
			this.chart && this.chart.dispose();
			this.chart = echarts.init(document.getElementById(this.id));
		},
		drawChart() {
			var _this = this;
			_this.chart.clear();
			if (!_this.chartData || !_this.chartData.xaxis) {
				_this.maxLabel = null;
				_this.yaxisList = [];
				return;
			}
			_this.yunit = _this.chartData.yunit;
			_this.title = _this.chartData.title + "(" + _this.yunit + ")";
			_this.maxLabel = _this.chartData.maxLabel;

			var _xDatalist = _this.chartData.xaxis.dataList;
			_this.yaxisList = _this.chartData.yaxisList;

			var _yDataList = [];
			for (let i = 0; i <= _this.yaxisList.length - 1; i++) {
				var _yAxis = _this.yaxisList[i];
				_yDataList.push({
					name: _yAxis.alias,
					type: 'line',
					smooth: true,
					symbol: 'none',
					areaStyle: {},
					lineStyle:{
						width: 0.3,
						color: _yAxis.color
					},
					itemStyle:{
						color: _yAxis.color
					},
					data: _yAxis.dataList
				});
			}

			var option = {
				tooltip: {
					trigger: 'axis',
					formatter: function(params) {
						var str = "<div style='padding: 0 10px;'>";
						str += "<div>" + params[0].axisValue + "</div>";
						for (var i = 0; i <= params.length - 1; i++) {
							str += "<div><span style='width:45%;text-align:left;display:inline-block;margin-right:10px;'><span style='background:" + params[i].color + ";width:10px;height:10px;display:inline-block;border-radius:10px;'></span> " + params[i].seriesName + "</span><span style='width:55%;text-align:right;display:inline-block;'><span style='font-weight:bold;'>" + params[i].value + "</span> " + _this.yunit + "</span></div>";
						}
						str += "</div>";
						return str;
					}
				},
				title: {
					left: 'center',
					text: _this.title,
				},
				toolbox: {
					feature: {
						/**
						dataZoom: {
							yAxisIndex: 'none'
						},
						restore: {
							show: false
						},
						**/
						saveAsImage: {}
					}
				},
				dataZoom: [
					{
						type: 'inside',
						start: 0,
						end: 100
					},
					{
						start: 0,
						end: 20
					}
				],
				xAxis: {
					type: 'category',
					boundaryGap: false,
					data: _xDatalist
				},
				yAxis: {
					type: 'value',
					axisLabel: {
						formatter: function(val, idx) {
							return val;
						}
					}
				},
				series: _yDataList
			};
			_this.chart.setOption(option);
			_this.chart.resize();
		}
	}
}
</script>
<style scoped>
.echarts {
	width: 100%;
}

.line-grid-content {
	border-radius: 5px;
	min-height: 20px;
	color: #ffffff;
	display: flex;
	justify-content: center;
	align-items: flex-start;
	flex-direction: column;
	padding: 5px 0 5px 15px;
	box-sizing: border-box;
	background: #172b4d;
}

.details-content {
	padding-bottom: 10px;
}
.item-head {
	height: 10px;
	width: 40px;
	display: inline-block;
	margin-right: 4px;
}
.item {
	height: 20px;
	display: flex;
	justify-content: flex-start;
	align-items: center;
}
.chart-title {
	color: #172b4d;
	padding-left: 15px;
	font-size: 16px;
}
</style>
