<template>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true" :model="filters">
				<el-form-item label="关键字：">
					<el-input v-model="filters.keywords" clearable placeholder="" />
				</el-form-item>
				<el-form-item>
					<el-button type="primary" v-on:click="getTemplateList()" class="el-icon-search" v-hasPerm="['template.list']"> 查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="$refs.createTemplate.show()" class="el-icon-edit" v-hasPerm="['template.create']"> 新增</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="templateList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="ID：">
							<span>{{scope.row.id}}</span>
						</el-form-item>
						<el-form-item label="模板名称：">
							<span>{{scope.row.name}}</span>
						</el-form-item>
						<el-form-item label="参数数量：">
							<span>{{scope.row.recCount}}</span>
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
						<el-form-item label="参数列表：">
							<div v-if="scope.row.recList != null && scope.row.recList.length > 0">
								<el-table :data="scope.row.recList">
                                    <el-table-column prop="name" label="参数名" width="200px" :show-overflow-tooltip="true" />
                                    <el-table-column prop="value" label="参数值" width="350px" :show-overflow-tooltip="true" />
								</el-table>
							</div>
							<div v-else>
								<span>无</span>
							</div>
						</el-form-item>
					</el-form>
				</template>
			</el-table-column>
			<el-table-column type="selection" width="30" />
			<el-table-column prop="name" label="模板名称" width="200px" :show-overflow-tooltip="true" />
			<el-table-column prop="recCount" label="参数数量" width="180px" :show-overflow-tooltip="true" />
			<el-table-column label="操作" width="100">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit" @click="$refs.editTemplate.show(scope.$index, scope.row)" v-hasPerm="['template.update']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['template.rec.list', 'template.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="$refs.templateRec.show(scope.row.id, scope.row.name)">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">参数列表</div>
									<el-button type="text" circle class="el-icon-s-data" v-hasPerm="['template.rec.list']"> 参数列表</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['template.delete']">
							<el-dropdown-item divided>
								<el-tooltip placement="top">
                                    <div slot="content">删除</div>
                                    <el-button type="text" circle class="el-icon-delete" v-hasPerm="['template.delete']"> 删除</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
						</el-dropdown-menu>
					</el-dropdown>
				</template>
			</el-table-column>
			<el-table-column label="" />
		</el-table>

		<!--工具条-->
		<el-col :span="24" class="toolbar2">
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length===0" class="el-icon-delete" v-hasPerm="['template.delete']"> 批量删除</el-button>
			<el-pagination layout="total, sizes, prev, pager, next, jumper" background
						@size-change="handleSizeChange"
						@current-change="handleCurrentChange"
						:page-sizes="[10,20,50,100]" :current-page="pageNo" :page-size="pageSize" :total="total" style="float:right;">
			</el-pagination>
		</el-col>

		<!--新增界面-->
		<create-template ref="createTemplate" v-on:success="getTemplateList"/>

		<!--编辑界面-->
		<edit-template ref="editTemplate" v-on:success="getTemplateList"/>

		<!--模板明细-->
		<template-rec ref="templateRec"/>
	</section>
</template>

<script>
	import CreateTemplate from './components/CreateTemplate';
	import EditTemplate from './components/EditTemplate';
	import TemplateRec from "./TemplateRec";
	export default {
		components: {
			'create-template': CreateTemplate,
			'edit-template': EditTemplate,
			'template-rec': TemplateRec
		},

		data() {
			return {
				filters: {
					keywords: null
				},

				total: 0,
				templateList: [],
				pageNo: 1,
				pageSize: 10,
				listLoading: false,
				sels: []    //列表选中列
			}
		},
		methods: {
			handleSizeChange(psize) {
				this.getTemplateList(1, psize);
			},
			handleCurrentChange(pno) {
				this.getTemplateList(pno, null);
			},
			onSelsChanged: function(sels) {
				this.sels = sels;
			},
			getTemplateList(pno, psize) {
				if (pno != null) {
					this.pageNo = pno;
				}
				if (psize != null) {
					this.pageSize = psize;
				}
				var params = {
					name: this.filters.keywords,
					pageNo: this.pageNo,
					pageSize: this.pageSize
				};
				this.listLoading = true;

				this.$axios.get(this.$global.baseUrl + '/template/list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.total = res.data.data.size;
						this.templateList = res.data.data.list;
					} else {
						this.total = 0;
						this.templateList = [];
						this.$message.error(res.data.msg);
					}
					this.listLoading = false;
				}).catch((err) => {
					this.listLoading = false;
					this.$message.error(err.message);
				});
			},

			//删除
			handleDel: function(index, row) {
				this.sels = [];
				this.sels.push(row);
				this.batchRemove();
			},
			//批量删除
			batchRemove: function() {
				var ids = this.sels.map(item => item.id).toString();
				this.$confirm('确定删除选中记录吗？', '提示', {type: 'warning'}).then(() => {
					var params = {ids: ids};
					this.listLoading = true;

					this.$axios.delete(this.$global.baseUrl + '/template/delete', {params: params}).then((res) => {
						if (res.data.code === '0000') {
							this.$message({type: "success", message: res.data.msg});
						} else {
							this.$message.error(res.data.msg);
						}
						this.listLoading = false;
						this.getTemplateList();
					}).catch((err) => {
						this.listLoading = false;
						this.$message.error(err.message);
					});
				}).catch(() => {

				});
				this.sels = [];
			}
		},
		mounted() {
			this.getTemplateList();
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

