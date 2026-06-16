<template>
<el-dialog title="负载节点" :visible.sync="formVisible" width="900px":close-on-click-modal="true"
	      append-to-body v-drag>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true">
				<el-form-item>
					<el-button type="primary" v-on:click="getUpstreamServerList()" class="el-icon-refresh" v-hasPerm="['upstream.server.list']"> 刷新</el-button>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="$refs.createUpstreamServer.show(upstreamId)" class="el-icon-edit" v-hasPerm="['upstream.server.create']"> 新增</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="upstreamServerList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="ID：">
							<span>{{scope.row.id}}</span>
						</el-form-item>
						<el-form-item label="IP：">
							<span>{{scope.row.ip}}</span>
						</el-form-item>
						<el-form-item label="端口：">
							<span>{{scope.row.port}}</span>
						</el-form-item>
						<el-form-item label="权重：">
							<span>{{scope.row.weight}}</span>
						</el-form-item>
						<el-form-item label="失败等待时间(秒)：">
							<span>{{scope.row.failTimeout}}</span>
						</el-form-item>
						<el-form-item label="最大失败次数：">
							<span>{{scope.row.maxFails}}</span>
						</el-form-item>
						<el-form-item label="状态：">
							<el-tooltip placement="right">
								<span slot="content">
									<span v-if="scope.row.enabled">启用</span>
									<span v-else>禁用</span>
								</span>
								<font :class="scope.row.enabled ? 'el-icon-success green' : 'el-icon-error red'" />
							</el-tooltip>
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
			<el-table-column prop="ip" label="IP" width="150px" :show-overflow-tooltip="true" />
			<el-table-column prop="port" label="端口" width="70px" :show-overflow-tooltip="true" />
			<el-table-column prop="weight" label="权重" width="70px" :show-overflow-tooltip="true" />
			<el-table-column prop="failTimeout" label="失败等待时间(秒)" width="120px" :show-overflow-tooltip="true" />
			<el-table-column prop="maxFails" label="最大失败次数" width="120px" :show-overflow-tooltip="true" />
			<el-table-column prop="enabled" label="状态" width="75px">
				<template slot-scope="scope">
                    <el-switch v-model="scope.row.enabled" :disabled="!hasPerm(['upstream.server.status.post'])"
                        :active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"
                        @change="handleEnabled(scope.row, $event)"/>
				</template>
			</el-table-column>
			<el-table-column label="操作" width="100">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit"
								 @click="$refs.editUpstreamServer.show(upstreamId, scope.$index, scope.row)" v-hasPerm="['upstream.server.update']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['upstream.server.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['upstream.server.delete']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">删除</div>
									<el-button type="text" circle class="el-icon-delete" v-hasPerm="['upstream.server.delete']"> 删除</el-button>
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
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length === 0" class="el-icon-delete" v-hasPerm="['upstream.server.delete']"> 批量删除</el-button>
		</el-col>
		<!--新增界面-->
		<create-upstream-server ref="createUpstreamServer" v-on:success="getUpstreamServerList"/>
		<!--编辑界面-->
		<edit-upstream-server ref="editUpstreamServer" v-on:success="getUpstreamServerList"/>
	</section>
</el-dialog>
</template>

<script>
	import CreateUpstreamServer from './components/CreateUpstreamServer';
	import EditUpstreamServer from './components/EditUpstreamServer';
	export default {
		components: {
			'create-upstream-server': CreateUpstreamServer,
			'edit-upstream-server': EditUpstreamServer
		},

		data() {
			return {
				formVisible: false,
				upstreamId: null,

				upstreamServerList: [],
				listLoading: false,
				sels: []    //列表选中列
			}
		},
		methods: {
			show: function(upstreamId) {
				this.formVisible = true;
				this.upstreamId = upstreamId;
				this.getUpstreamServerList();
			},
			onSelsChanged: function(sels) {
				this.sels = sels;
			},
			getUpstreamServerList() {
				var params = {
					upstreamId: this.upstreamId
				};
				this.listLoading = true;

				this.$axios.get(this.$global.baseUrl + '/upstream/server/list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.upstreamServerList = res.data.data;
					} else {
						this.upstreamServerList = [];
						this.$message.error(res.data.msg);
					}
					this.listLoading = false;
				}).catch((err) => {
					this.listLoading = false;
					this.$message.error(err.message);
				});
			},

			//启用/禁用
			handleEnabled: function(row, enabled) {
				var tips = enabled ? "确定启用吗？" : "确定禁用吗？";
				this.$confirm(tips, '提示', {type: 'info'}).then(() => {
					var params = "id=" + row.id + "&enabled=" + enabled;
					this.$axios.post(this.$global.baseUrl + '/upstream/server/status', params).then((res) => {
						if (res.data.code === '0000') {
							this.$message({type: "success", message: res.data.msg});
						} else {
							this.$message.error(res.data.msg);
						}
						this.getUpstreamServerList();
					}).catch((err) => {
						this.$message.error(err.message);
					});
				}).catch(() => {
                    row.enabled = !enabled;
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

					this.$axios.delete(this.$global.baseUrl + '/upstream/server/delete', {params: params}).then((res) => {
						if (res.data.code === '0000') {
							this.$message({type: "success", message: res.data.msg});
						} else {
							this.$message.error(res.data.msg);
						}
						this.listLoading = false;
						this.getUpstreamServerList();
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
	.green {
		color: green;
	}
	.red {
		color: red;
	}
</style>

