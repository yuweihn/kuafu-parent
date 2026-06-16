<template>
<el-dialog title="反向代理参数模板" :visible.sync="formVisible" width="750px":close-on-click-modal="true"
	      append-to-body v-drag>
    <el-row :gutter="20">
        <el-col>server_name：{{serverName}}</el-col>
    </el-row>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true">
				<el-form-item v-hasPerm="['server.param.template.list']">
					<el-button type="primary" v-on:click="getParamTemplateList()" class="el-icon-refresh"> 刷新</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['server.param.template.create']">
					<el-button type="primary" @click="$refs.createServerParamTemplate.show(serverId)" class="el-icon-edit"> 新增</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="paramTemplateList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="ID：">
							<span>{{scope.row.id}}</span>
						</el-form-item>
						<el-form-item label="模板名称：">
							<span>{{scope.row.templateName}}</span>
						</el-form-item>
						<el-form-item label="参数数量：">
							<span>{{scope.row.templateRecCount}}</span>
						</el-form-item>
						<el-form-item label="顺序：">
							<span>{{scope.row.orderBy}}</span>
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
							<div v-if="scope.row.templateRecList != null && scope.row.templateRecList.length > 0">
								<el-table :data="scope.row.templateRecList">
                                    <el-table-column prop="name" label="参数名" width="180px" :show-overflow-tooltip="true" />
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
			<el-table-column prop="templateName" label="模板名称" width="180px" :show-overflow-tooltip="true" />
			<el-table-column prop="templateRecCount" label="参数数量" width="120px" :show-overflow-tooltip="true" />
			<el-table-column prop="orderBy" label="顺序" width="80" />
			<el-table-column label="操作" width="180px">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit"
								 @click="$refs.editServerParamTemplate.show(serverId, scope.$index, scope.row)" v-hasPerm="['server.param.template.update']" />
					</el-tooltip>
					<el-tooltip placement="top">
						<div slot="content">上移</div>
						<el-button type="text" circle class="el-icon-arrow-up" @click="handleUp(scope.$index, scope.row)" v-hasPerm="['server.param.template.move.up']" />
					</el-tooltip>
					<el-tooltip placement="top">
						<div slot="content">下移</div>
						<el-button type="text" circle class="el-icon-arrow-down" @click="handleDown(scope.$index, scope.row)" v-hasPerm="['server.param.template.move.down']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['server.param.template.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['server.param.template.delete']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">删除</div>
									<el-button type="text" circle class="el-icon-delete"> 删除</el-button>
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
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length === 0" class="el-icon-delete" v-hasPerm="['server.param.template.delete']"> 批量删除</el-button>
		</el-col>
		<!--新增界面-->
		<create-server-param-template ref="createServerParamTemplate" v-on:success="getParamTemplateList"/>
		<!--编辑界面-->
		<edit-server-param-template ref="editServerParamTemplate" v-on:success="getParamTemplateList"/>
	</section>
</el-dialog>
</template>

<script>
	import CreateServerParamTemplate from './components/CreateServerParamTemplate';
	import EditServerParamTemplate from './components/EditServerParamTemplate';
	export default {
		components: {
			'create-server-param-template': CreateServerParamTemplate,
			'edit-server-param-template': EditServerParamTemplate
		},

		data() {
			return {
				formVisible: false,
				serverId: null,
				serverName: null,

				paramTemplateList: [],
				listLoading: false,
				sels: []    //列表选中列
			}
		},
		methods: {
			show: function(serverId, serverName) {
				this.formVisible = true;
				this.serverId = serverId;
				this.serverName = serverName;
				this.getParamTemplateList();
			},
			onSelsChanged: function(sels) {
				this.sels = sels;
			},
			getParamTemplateList() {
				var params = {
					serverId: this.serverId
				};
				this.listLoading = true;

				this.$axios.get(this.$global.baseUrl + '/server/param/template/list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.paramTemplateList = res.data.data;
					} else {
						this.paramTemplateList = [];
						this.$message.error(res.data.msg);
					}
					this.listLoading = false;
				}).catch((err) => {
					this.listLoading = false;
					this.$message.error(err.message);
				});
			},

			handleUp: function(index, row) {
                var params = "id=" + row.id;
                this.listLoading = true;
                this.$axios.post(this.$global.baseUrl + '/server/param/template/move-up', params).then((res) => {
                    if (res.data.code === '0000') {
                        //DO NOTHING
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getParamTemplateList();
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
			},

			handleDown: function(index, row) {
                var params = "id=" + row.id;
                this.listLoading = true;
                this.$axios.post(this.$global.baseUrl + '/server/param/template/move-down', params).then((res) => {
                    if (res.data.code === '0000') {
                        //DO NOTHING
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getParamTemplateList();
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

					this.$axios.delete(this.$global.baseUrl + '/server/param/template/delete', {params: params}).then((res) => {
						if (res.data.code === '0000') {
							this.$message({type: "success", message: res.data.msg});
						} else {
							this.$message.error(res.data.msg);
						}
						this.listLoading = false;
						this.getParamTemplateList();
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

