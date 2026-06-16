<template>
<el-dialog title="代理目标" :visible.sync="formVisible" width="1200px":close-on-click-modal="true" v-drag>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true">
				<el-form-item v-hasPerm="['server.location.list']">
					<el-button type="primary" v-on:click="getServerLocationList()" class="el-icon-refresh"> 刷新</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['server.location.create']">
					<el-button type="primary" @click="$refs.createServerLocation.show(serverId, proxyType)" class="el-icon-edit"> 新增</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="serverLocationList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="ID：">
							<span>{{scope.row.id}}</span>
						</el-form-item>
						<el-form-item label="目标路径：">
							<span>{{scope.row.path}}</span>
						</el-form-item>
						<el-form-item label="目标类型：">
							<span>{{scope.row.locTypeName}}</span>
						</el-form-item>
						<el-form-item label="负载均衡：">
							<span>{{scope.row.proxyUpstreamName}}</span>
						</el-form-item>
						<el-form-item label="目标地址：">
							<span>{{scope.row.proxyTarget}}</span>
						</el-form-item>
						<el-form-item label="静态代理模式：">
							<span>{{scope.row.proxyStaticName}}</span>
						</el-form-item>
						<el-form-item label="默认页：">
							<span>{{scope.row.proxyIndexPage}}</span>
						</el-form-item>
						<el-form-item label="扩展代理路径：">
							<span>{{scope.row.proxyExtPath}}</span>
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
							<div v-if="scope.row.paramList != null && scope.row.paramList.length > 0">
								<el-table :data="scope.row.paramList">
									<el-table-column prop="name" label="参数名" width="200px" />
                                    <el-table-column prop="value" label="参数值" width="350px">
                                        <template slot-scope="scope2">{{$util.slice(scope2.row.value, 100)}}</template>
                                    </el-table-column>
								</el-table>
							</div>
							<div v-else>
								<span>无</span>
							</div>
						</el-form-item>
						<el-form-item label="参数模板列表：">
							<div v-if="scope.row.paramTemplateList != null && scope.row.paramTemplateList.length > 0">
								<el-table :data="scope.row.paramTemplateList">
                                    <el-table-column prop="templateName" label="模板名称" width="200px" />
                                    <el-table-column prop="templateRecCount" label="参数数量" width="200px" />
								</el-table>
							</div>
							<div v-else>
								<span>无</span>
                            </div>
						</el-form-item>
					</el-form>
				</template>
			</el-table-column>
			<el-table-column type="selection" width="30"></el-table-column>
			<!--<el-table-column prop="id" label="ID" width="80" sortable></el-table-column>-->
			<el-table-column prop="path" label="目标路径" width="130px" />
			<el-table-column prop="locTypeName" label="目标类型" width="110px" />
			<el-table-column prop="proxyUpstreamName" label="负载均衡" width="175px" />
			<el-table-column prop="proxyTarget" label="目标地址" width="220px" />
			<el-table-column prop="proxyStaticName" label="静态代理模式" width="95px" />
			<el-table-column prop="proxyIndexPage" label="默认页" width="95px" />
			<el-table-column prop="proxyExtPath" label="扩展代理路径" width="95px" />
			<el-table-column label="操作" width="100">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit"
						        @click="$refs.editServerLocation.show(serverId, proxyType, scope.$index, scope.row)" v-hasPerm="['server.location.update']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['server.location.param.list', 'server.location.param.template.list', 'server.location.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="$refs.serverLocationParam.show(scope.row.id)" v-hasPerm="['server.location.param.list']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">参数列表</div>
									<el-button type="text" circle class="el-icon-s-promotion"> 参数列表</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="$refs.serverLocationParamTemplate.show(scope.row.id)" v-hasPerm="['server.location.param.template.list']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">参数模板列表</div>
									<el-button type="text" circle class="el-icon-office-building"> 参数模板列表</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['server.location.delete']">
							<el-dropdown-item divided>
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
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length === 0" class="el-icon-delete" v-hasPerm="['server.location.delete']"> 批量删除</el-button>
		</el-col>
		<!--新增界面-->
		<create-server-location ref="createServerLocation" v-on:success="getServerLocationList"/>
		<!--编辑界面-->
		<edit-server-location ref="editServerLocation" v-on:success="getServerLocationList"/>
		<server-location-param ref="serverLocationParam"/>
		<server-location-param-template ref="serverLocationParamTemplate"/>
	</section>
</el-dialog>
</template>

<script>
	import CreateServerLocation from './components/CreateServerLocation';
	import EditServerLocation from './components/EditServerLocation';
	import ServerLocationParam from "./ServerLocationParam";
	import ServerLocationParamTemplate from "./ServerLocationParamTemplate";
	export default {
		components: {
			'create-server-location': CreateServerLocation,
			'edit-server-location': EditServerLocation,
			'server-location-param': ServerLocationParam,
			'server-location-param-template': ServerLocationParamTemplate
		},

		data() {
			return {
				formVisible: false,
				serverId: null,
				proxyType: null,

				serverLocationList: [],
				listLoading: false,
				sels: []    //列表选中列
			}
		},
		methods: {
			show: function(serverId, proxyType) {
				this.formVisible = true;
				this.serverId = serverId;
				this.proxyType = proxyType;
				this.getServerLocationList();
			},
			onSelsChanged: function(sels) {
				this.sels = sels;
			},
			getServerLocationList() {
				var params = {
					serverId: this.serverId
				};
				this.listLoading = true;

				this.$axios.get(this.$global.baseUrl + '/server/location/list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.serverLocationList = res.data.data;
					} else {
						this.serverLocationList = [];
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

					this.$axios.delete(this.$global.baseUrl + '/server/location/delete', {params: params}).then((res) => {
						if (res.data.code === '0000') {
							this.$message({type: "success", message: res.data.msg});
						} else {
							this.$message.error(res.data.msg);
						}
						this.listLoading = false;
						this.getServerLocationList();
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

