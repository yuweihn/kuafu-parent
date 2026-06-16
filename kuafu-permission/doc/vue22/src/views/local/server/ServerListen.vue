<template>
<el-dialog title="监听端口" :visible.sync="formVisible" width="900px":close-on-click-modal="true" v-drag>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true">
				<el-form-item v-hasPerm="['server.listen.list']">
					<el-button type="primary" v-on:click="getServerListenList()" class="el-icon-refresh"> 刷新</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['server.listen.create']">
					<el-button type="primary" @click="$refs.createServerListen.show(serverId)" class="el-icon-edit"> 新增</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="serverListenList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="ID：">
							<span>{{scope.row.id}}</span>
						</el-form-item>
						<el-form-item label="监听端口：">
							<span>{{scope.row.port}}</span>
						</el-form-item>
						<el-form-item label="是否为默认Server：">
							<span v-if="scope.row.ifDefault == null"></span>
							<span v-else-if="scope.row.ifDefault == true" class="green">是</span>
							<span v-else class="red">否</span>
						</el-form-item>
						<el-form-item label="是否开启Proxy Protocol：">
							<span v-if="scope.row.ifProxyProtocol == null"></span>
							<span v-else-if="scope.row.ifProxyProtocol == true" class="green">是</span>
							<span v-else class="red">否</span>
						</el-form-item>
						<el-form-item label="是否开启SSL：">
							<span v-if="scope.row.ifSsl == null"></span>
							<span v-else-if="scope.row.ifSsl == true" class="green">是</span>
							<span v-else class="red">否</span>
						</el-form-item>
						<el-form-item label="是否开启Http2：">
							<span v-if="scope.row.ifHttp2 == null"></span>
							<span v-else-if="scope.row.ifHttp2 == true" class="green">是</span>
							<span v-else class="red">否</span>
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
			<el-table-column prop="port" label="监听端口" width="70px" />
			<el-table-column prop="ifDefault" label="是否为默认Server" width="120px">
				<template slot-scope="scope">
					<el-tooltip placement="right">
						<span slot="content">
							<span v-if="scope.row.ifDefault == null"></span>
							<span v-else-if="scope.row.ifDefault == true">是</span>
							<span v-else>否</span>
						</span>
						<span v-if="scope.row.ifDefault == null"></span>
						<font v-else :class="scope.row.ifDefault == true ? 'el-icon-success green' : 'el-icon-error red'" />
					</el-tooltip>
				</template>
			</el-table-column>
			<el-table-column prop="ifProxyProtocol" label="是否开启Proxy Protocol" width="160px">
				<template slot-scope="scope">
					<el-tooltip placement="right">
						<span slot="content">
							<span v-if="scope.row.ifProxyProtocol == null"></span>
							<span v-else-if="scope.row.ifProxyProtocol == true">是</span>
							<span v-else>否</span>
						</span>
						<span v-if="scope.row.ifProxyProtocol == null"></span>
						<font v-else :class="scope.row.ifProxyProtocol == true ? 'el-icon-success green' : 'el-icon-error red'" />
					</el-tooltip>
				</template>
			</el-table-column>
			<el-table-column prop="ifSsl" label="是否开启SSL" width="120px">
				<template slot-scope="scope">
					<el-tooltip placement="right">
						<span slot="content">
							<span v-if="scope.row.ifSsl == null"></span>
							<span v-else-if="scope.row.ifSsl == true">是</span>
							<span v-else>否</span>
						</span>
						<span v-if="scope.row.ifSsl == null"></span>
						<font v-else :class="scope.row.ifSsl == true ? 'el-icon-success green' : 'el-icon-error red'" />
					</el-tooltip>
				</template>
			</el-table-column>
			<el-table-column prop="ifHttp2" label="是否开启Http2" width="120px">
				<template slot-scope="scope">
					<el-tooltip placement="right">
						<span slot="content">
							<span v-if="scope.row.ifHttp2 == null"></span>
							<span v-else-if="scope.row.ifHttp2 == true">是</span>
							<span v-else>否</span>
						</span>
						<span v-if="scope.row.ifHttp2 == null"></span>
						<font v-else :class="scope.row.ifHttp2 == true ? 'el-icon-success green' : 'el-icon-error red'" />
					</el-tooltip>
				</template>
			</el-table-column>
			<el-table-column label="操作" width="100">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit"
						        @click="$refs.editServerListen.show(serverId, scope.$index, scope.row)" v-hasPerm="['server.listen.update']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['server.listen.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['server.listen.delete']">
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
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length === 0" class="el-icon-delete" v-hasPerm="['server.listen.delete']"> 批量删除</el-button>
		</el-col>
		<!--新增界面-->
		<create-server-listen ref="createServerListen" v-on:success="getServerListenList"/>
		<!--编辑界面-->
		<edit-server-listen ref="editServerListen" v-on:success="getServerListenList"/>
	</section>
</el-dialog>
</template>

<script>
	import CreateServerListen from './components/CreateServerListen';
	import EditServerListen from './components/EditServerListen';
	export default {
		components: {
			'create-server-listen': CreateServerListen,
			'edit-server-listen': EditServerListen
		},

		data() {
			return {
				formVisible: false,
				serverId: null,

				serverListenList: [],
				listLoading: false,
				sels: []    //列表选中列
			}
		},
		methods: {
			show: function(serverId) {
                this.formVisible = true;
                this.serverId = serverId;
                this.getServerListenList();
			},
			onSelsChanged: function(sels) {
				this.sels = sels;
			},
			getServerListenList() {
				var params = {
					serverId: this.serverId
				};
				this.listLoading = true;

				this.$axios.get(this.$global.baseUrl + '/server/listen/list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.serverListenList = res.data.data;
					} else {
						this.serverListenList = [];
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

					this.$axios.delete(this.$global.baseUrl + '/server/listen/delete', {params: params}).then((res) => {
						if (res.data.code === '0000') {
							this.$message({type: "success", message: res.data.msg});
						} else {
							this.$message.error(res.data.msg);
						}
						this.listLoading = false;
						this.getServerListenList();
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

