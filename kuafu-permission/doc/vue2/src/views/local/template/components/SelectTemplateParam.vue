<template>
<el-dialog title="选择模板参数" :visible.sync="formVisible" width="750px" :close-on-click-modal="true"
	      append-to-body v-drag>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true" :model="filters">
				<el-form-item label="模板名称：">
                    <el-select v-model="filters.templateId" placeholder="请输入" @change="onTemplateChanged($event)"
                                    :remote-method="initTemplateList" clearable filterable remote>
                        <el-option v-for="item in templateSlt.result.list" :key="item.val" :label="item.label" :value="item.val" />
                        <el-pagination layout="total, prev, pager, next" hide-on-single-page @current-change="onTemplatePageChanged"
                                    :current-page="templateSlt.pageNo" :page-size="templateSlt.pageSize" :total="templateSlt.result.size"/>
                    </el-select>
				</el-form-item>
				<el-form-item v-hasPerm="['template.rec.list']">
					<el-button type="primary" v-on:click="getTemplateRecList()" class="el-icon-search"> 查询</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="templateRecList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="ID：">
							<span>{{scope.row.id}}</span>
						</el-form-item>
						<el-form-item label="参数名：">
							<span>{{scope.row.name}}</span>
						</el-form-item>
						<el-form-item label="参数值：">
							<el-input type="textarea" :rows="5" style="width: 450px;" readonly placeholder="" v-model="scope.row.value"></el-input>
						</el-form-item>
						<el-form-item label="创建人：">
							<span>{{scope.row.creator}}</span>
						</el-form-item>
						<el-form-item label="创建时间：">
							<span>{{scope.row.createTime}}</span>
						</el-form-item>
						<el-form-item label="修改人：">
							<span>{{scope.row.modifier}}</span>
						</el-form-item>
						<el-form-item label="修改时间：">
							<span>{{scope.row.modifyTime}}</span>
						</el-form-item>
					</el-form>
				</template>
			</el-table-column>
			<el-table-column type="selection" width="30" />
			<el-table-column prop="name" label="参数名" width="240px" :show-overflow-tooltip="true" />
			<el-table-column prop="value" label="参数值" width="330px" :show-overflow-tooltip="true" />
			<el-table-column label="" />
		</el-table>

		<!--工具条-->
		<el-col :span="24" class="toolbar2">
			<el-button type="primary" @click.native="doSelect" class="el-icon-circle-check"> 确认选择</el-button>
		</el-col>
	</section>
</el-dialog>
</template>

<script>
	export default {
		data() {
			return {
				formVisible: false,

				filters: {
				  templateId: null
				},

				templateRecList: [],
				listLoading: false,
				sels: [],    //列表选中列

				templateSlt: {
					result: {
						size: 0,
						list: []
					},
					keywords: "",
					pageNo: 1,
					pageSize: 10
				}
			}
		},
		methods: {
			show: function() {
				this.formVisible = true;
				this.filters.templateId = null;
				this.templateRecList = [];
				this.listLoading = false;
				this.sels = [];
				this.templateSlt.result.size = 0;
				this.templateSlt.result.list = [];
				this.initTemplateList();
			},
			onSelsChanged: function(sels) {
				this.sels = sels;
			},
			getTemplateRecList() {
				if (!this.filters.templateId) {
					this.$message.error("请选择模板");
					return;
				}
				var params = {
					templateId: this.filters.templateId
				};
				this.listLoading = true;

				this.$axios.get(this.$global.baseUrl + '/template/rec/list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.templateRecList = res.data.data;
					} else {
						this.templateRecList = [];
						this.$message.error(res.data.msg);
					}
					this.listLoading = false;
				}).catch((err) => {
					this.listLoading = false;
					this.$message.error(err.message);
				});
			},

			doSelect: function() {
				var data = this.sels.map(item => {
					return {name: item.name, value: item.value};
				});
				if (data == null || data.length <= 0) {
					this.$message.error("请选择参数");
					return;
				}
				this.formVisible = false;
				this.$emit("success", data);
			},

			initTemplateList(keywords) {
				this.templateSlt.keywords = keywords;
				this.onTemplatePageChanged(1, 10);
			},
			onTemplatePageChanged(pno, psize) {
				if (pno != null) {
					this.templateSlt.pageNo = pno;
				}
				if (psize != null) {
					this.templateSlt.pageSize = psize;
				}
				var params = {
                    name: this.templateSlt.keywords,
                    pageNo: this.templateSlt.pageNo,
                    pageSize: this.templateSlt.pageSize
				};
				this.$axios.get(this.$global.baseUrl + '/template/drop-down-list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.templateSlt.result.size = res.data.data.size;
						this.templateSlt.result.list = res.data.data.list.map(item => {
							return {val: item.id, label: item.name};
						});
					} else {
						this.templateSlt.result.size = 0;
						this.templateSlt.result.list = [];
						this.$message.error(res.data.msg);
					}
				}).catch((err) => {
					this.$message.error(err.message);
				});
			},
			onTemplateChanged(templateId) {
                this.templateRecList = [];
                if (templateId) {
                    return;
                }
                this.initTemplateList();
			}
		},
		mounted() {

		}
	}
</script>

<style scoped>
	.table-expand {
		font-size: 0;
	}
	.table-expand >>> label {
		color: #99a9bf;
	}
	.table-expand .el-form-item {
		margin-right: 0;
		margin-bottom: 0;
		width: 90%;
	}
</style>

