<template>
<el-dialog title="反向代理目标参数" :visible.sync="formVisible" width="800px":close-on-click-modal="true"
	      append-to-body v-drag>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true">
				<el-form-item v-hasPerm="['server.location.param.list']">
					<el-button type="primary" v-on:click="getParamList()" class="el-icon-refresh"> 刷新</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['server.location.param.create']">
					<el-button type="primary" @click="$refs.createServerLocationParam.show(locationId)" class="el-icon-edit"> 新增</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['server.location.param.batch.create']">
					<el-button type="primary" @click="$refs.selectTemplateParam.show()" class="el-icon-plus"> 添加模板参数</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="serverLocationParamList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
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
							<el-input type="textarea" :rows="5" style="width: 550px;" readonly placeholder="" v-model="scope.row.value"></el-input>
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
					</el-form>
				</template>
			</el-table-column>
			<el-table-column type="selection" width="30" />
			<el-table-column prop="name" label="参数名" width="170px" :show-overflow-tooltip="true" />
			<el-table-column prop="value" label="参数值" width="200px" :show-overflow-tooltip="true" />
			<el-table-column prop="orderBy" label="顺序" width="80" />
			<el-table-column label="操作" width="180px">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit"
								 @click="$refs.editServerLocationParam.show(locationId, scope.$index, scope.row)" v-hasPerm="['server.location.param.update']" />
					</el-tooltip>
					<el-tooltip placement="top">
						<div slot="content">上移</div>
						<el-button type="text" circle class="el-icon-arrow-up" @click="handleUp(scope.$index, scope.row)" v-hasPerm="['server.location.param.move.up']" />
					</el-tooltip>
					<el-tooltip placement="top">
						<div slot="content">下移</div>
						<el-button type="text" circle class="el-icon-arrow-down" @click="handleDown(scope.$index, scope.row)" v-hasPerm="['server.location.param.move.down']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['server.location.param.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['server.location.param.delete']">
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
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length === 0" class="el-icon-delete" v-hasPerm="['server.location.param.delete']"> 批量删除</el-button>
		</el-col>
		<!--新增界面-->
		<create-server-location-param ref="createServerLocationParam" v-on:success="getParamList"/>
		<!--编辑界面-->
		<edit-server-location-param ref="editServerLocationParam" v-on:success="getParamList"/>
		<!--选择参数模板-->
		<select-template-param ref="selectTemplateParam" v-on:success="selectTemplateCallback"/>
	</section>
</el-dialog>
</template>

<script>
	import CreateServerLocationParam from './components/CreateServerLocationParam';
	import EditServerLocationParam from './components/EditServerLocationParam';
	import SelectTemplateParam from '@/views/local/template/components/SelectTemplateParam';
	export default {
		components: {
			'create-server-location-param': CreateServerLocationParam,
			'edit-server-location-param': EditServerLocationParam,
			'select-template-param': SelectTemplateParam
		},

		data() {
			return {
				formVisible: false,
				locationId: null,

				serverLocationParamList: [],
				listLoading: false,
				sels: []    //列表选中列
			}
		},
		methods: {
			show: function(locationId) {
				this.formVisible = true;
				this.locationId = locationId;
				this.getParamList();
			},
			onSelsChanged: function(sels) {
				this.sels = sels;
			},
			getParamList() {
				var params = {
					locationId: this.locationId
				};
				this.listLoading = true;

				this.$axios.get(this.$global.baseUrl + '/server/location/param/list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.serverLocationParamList = res.data.data;
					} else {
						this.serverLocationParamList = [];
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
                this.$axios.post(this.$global.baseUrl + '/server/location/param/move-up', params).then((res) => {
                    if (res.data.code === '0000') {
                        //DO NOTHING
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getParamList();
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
			},

			handleDown: function(index, row) {
                var params = "id=" + row.id;
                this.listLoading = true;
                this.$axios.post(this.$global.baseUrl + '/server/location/param/move-down', params).then((res) => {
                    if (res.data.code === '0000') {
                        //DO NOTHING
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getParamList();
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
			},

			selectTemplateCallback: function(data) {
                if (data == null || data.length <= 0) {
                    return;
                }
                var params = {
                    locationId: this.locationId,
                    dataList: data
                };
                this.listLoading = true;

                this.$axios.post(this.$global.baseUrl + '/server/location/param/batch-create', params).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getParamList();
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

					this.$axios.delete(this.$global.baseUrl + '/server/location/param/delete', {params: params}).then((res) => {
						if (res.data.code === '0000') {
							this.$message({type: "success", message: res.data.msg});
						} else {
							this.$message.error(res.data.msg);
						}
						this.listLoading = false;
						this.getParamList();
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

