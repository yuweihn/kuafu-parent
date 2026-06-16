<template>
	<section>
	<div class='monitor-box'>
		<div class='content-summary'>
			<div class='s-item'>
				<div class="itm-header">CPU使用率 <span>(核心数: {{summary.cpuCount}})</span></div>
				<div class="body"><el-progress :text-inside="true" :stroke-width="15" :percentage="summary.formatCpuUsage" :color="formatColor" /></div>
			</div>
			<div class='s-item'>
				<div class="itm-header">内存使用 <span>(已用: {{summary.formatMemUsed}} / 总量: {{summary.formatMemTotal}})</span></div>
				<div class="body"><el-progress :text-inside="true" :stroke-width="15" :percentage="summary.formatMemUsage" :color="formatColor" /></div>
			</div>
			<div class='s-item disk'>
				<div class="itm-header">磁盘使用</div>
				<div class="body" v-for = "(item, index) in summary.diskList" :key = "item.path">
					<div class="title">{{item.path}}&nbsp;&nbsp;({{item.formatUsed}} / {{item.formatTotal}})</div>
					<el-progress :text-inside="false" :stroke-width="8" :percentage="item.formatUsage" :color="formatColor"/>
				</div>
			</div>
		</div>
		<div class='content-network'>
			<area-chart :chartData="chartData" :chartHeight="'380px'"/>
		</div>
	</div>
	</section>
</template>

<script>
import areaChart from '@/views/components/echart/AreaChart';
export default {
	components: {
		"area-chart": areaChart
	},
	data () {
		return {
			summary: {},
			chartData: {},
			summaryInterval: null,
			networkInterval: null
		}
	},
	methods: {
		getMonitorSummary() {
			let _this = this;
			_this.$axios.get(_this.$global.baseUrl + '/monitor/summary', {}).then((res) => {
				if (res.data.code === '0000') {
					_this.summary = res.data.data;
				} else {
					_this.summary = {};
					_this.$message.error(res.data.msg);
				}
			}).catch((err) => {
				_this.$message.error(err.message);
			});
		},
		formatColor: function(percentage) {
			if (percentage < 70) {
				return '#67C23A';
			} else if (percentage < 90) {
				return '#E6A23C';
			} else {
				return '#F56C6C';
			}
		},
		queryNetworkChartDataList: function() {
			var _this = this;
			_this.$axios.get(_this.$global.baseUrl + '/monitor/network/data/list', {}).then((res) => {
				if (res.data.code === '0000') {
					var _data = res.data.data;
					_this.chartData = _data;
				} else {
					_this.$message.error(res.data.msg);
				}
			}).catch((err) => {
				_this.$message.error(err.message);
			});
		}
	},
	mounted() {
		this.getMonitorSummary();
		this.queryNetworkChartDataList();
		this.summaryInterval = setInterval(() => {
			this.getMonitorSummary();
		}, 2000);
		this.networkInterval = setInterval(() => {
			this.queryNetworkChartDataList();
		}, 3000);
	},
	destroyed() {
		clearInterval(this.summaryInterval);
		clearInterval(this.networkInterval);
	}
}
</script>

<style lang='scss' scoped>
	.monitor-box {
		background-color: #f2f2f2;
		margin: 30px 0;
		padding: 20px;
	}
	.monitor-box .content-summary, .monitor-box .content-network {
		background-color: #ffffff;
		display: inline-block;
		vertical-align: top;
	}
	.monitor-box .content-summary {
		width: 32%;
		margin-right: 15px;
		padding: 10px;
	}
	.monitor-box .content-network {
		width: 60%;
		padding: 10px 15px 10px 20px;
	}
	.s-item {
		margin-bottom: 10px;
	}
	.itm-header {
		border-bottom: 1px solid #dadada;
		padding: 5px;
	}
	.body {
		margin-top: 10px;
	}
	.title {
		color: #999999;
	}
</style>
<style scoped>
	>>> .el-progress__text {
		font-size: 12.5px !important;
	}
</style>
