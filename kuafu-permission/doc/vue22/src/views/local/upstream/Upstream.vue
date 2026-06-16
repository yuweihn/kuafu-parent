<template>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true" :model="filters">
				<el-form-item label="关键字：">
					<el-input v-model="filters.keywords" clearable placeholder="" />
				</el-form-item>
				<el-form-item label="代理类型：">
					<el-select v-model="filters.proxyType" placeholder="请选择" @change="onProxyTypeChanged($event)" clearable>
						<el-option v-for="item in proxyTypeList" :key="item.val" :label="item.label" :value="item.val" />
					</el-select>
				</el-form-item>
				<el-form-item label="负载策略：">
					<el-select v-model="filters.loadPolicy" placeholder="请选择" @change="onLoadPolicyChanged($event)" clearable>
						<el-option v-for="item in loadPolicyList" :key="item.val" :label="item.label" :value="item.val">
							<span style="float:left">{{item.label}}</span>
							<span style="float:right; color:#8492a6; font-size:13px; margin-left:20px;">{{$util.slice(item.desc, 100)}}</span>
						</el-option>
					</el-select>
				</el-form-item>
				<el-form-item v-hasPerm="['upstream.list']">
					<el-button type="primary" v-on:click="getUpstreamList(1)" class="el-icon-search"> 查询</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['upstream.create']">
					<el-button type="primary" @click="$refs.createUpstream.show()" class="el-icon-edit"> 新增</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="upstreamList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="ID：">
							<span>{{scope.row.id}}</span>
						</el-form-item>
						<el-form-item label="名称：">
							<span>{{scope.row.name}}</span>
						</el-form-item>
						<el-form-item label="代理类型：">
							<span>{{scope.row.proxyTypeName}}</span>
						</el-form-item>
						<el-form-item label="负载策略：">
							<span>{{scope.row.loadPolicyName}}</span>
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
						<el-form-item label="负载节点：">
							<div v-if="scope.row.serverList != null && scope.row.serverList.length > 0">
								<el-table :data="scope.row.serverList">
									<el-table-column prop="ip" label="IP" width="150px" :show-overflow-tooltip="true" />
									<el-table-column prop="port" label="端口" width="70px" :show-overflow-tooltip="true" />
									<el-table-column prop="weight" label="权重" width="70px" :show-overflow-tooltip="true" />
									<el-table-column prop="failTimeout" label="失败等待时间(秒)" width="120px" :show-overflow-tooltip="true" />
									<el-table-column prop="maxFails" label="最大失败次数" width="120px" :show-overflow-tooltip="true" />
									<el-table-column prop="enabled" label="状态" width="75px">
										<template slot-scope="scope">
											<el-tooltip placement="right">
												<span slot="content">
													<span v-if="scope.row.enabled">启用</span>
													<span v-else>禁用</span>
												</span>
												<font :class="scope.row.enabled ? 'el-icon-success green' : 'el-icon-error red'" />
											</el-tooltip>
										</template>
									</el-table-column>
								</el-table>
							</div>
							<div v-else>
								<span>无</span>
							</div>
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
			<el-table-column type="selection" width="30" />
			<el-table-column prop="name" label="名称" width="200" :show-overflow-tooltip="true" />
			<el-table-column prop="proxyTypeName" label="代理类型" width="160" :show-overflow-tooltip="true" />
			<el-table-column prop="loadPolicyName" label="负载策略" width="200" :show-overflow-tooltip="true" />
			<el-table-column prop="orderBy" label="顺序" width="80" />
			<el-table-column label="操作" width="200">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit" @click="$refs.editUpstream.show(scope.$index, scope.row)" v-hasPerm="['upstream.update']"/>
					</el-tooltip>
					<el-tooltip placement="top">
						<div slot="content">上移</div>
						<el-button type="text" circle class="el-icon-arrow-up" @click="handleUp(scope.$index, scope.row)" v-hasPerm="['upstream.move.up']" />
					</el-tooltip>
					<el-tooltip placement="top">
						<div slot="content">下移</div>
						<el-button type="text" circle class="el-icon-arrow-down" @click="handleDown(scope.$index, scope.row)" v-hasPerm="['upstream.move.down']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['upstream.server.list', 'upstream.param.list', 'upstream.param.template.list', 'upstream.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="$refs.upstreamServer.show(scope.row.id)" v-hasPerm="['upstream.server.list']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">负载节点</div>
									<el-button type="text" circle class="el-icon-s-data"> 负载节点</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="$refs.upstreamParam.show(scope.row.id, scope.row.name)" v-hasPerm="['upstream.param.list']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">参数列表</div>
									<el-button type="text" circle class="el-icon-s-promotion"> 参数列表</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="$refs.upstreamParamTemplate.show(scope.row.id, scope.row.name)" v-hasPerm="['upstream.param.template.list']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">参数模板列表</div>
									<el-button type="text" circle class="el-icon-office-building"> 参数模板列表</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['upstream.delete']">
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
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length===0" class="el-icon-delete" v-hasPerm="['upstream.delete']"> 批量删除</el-button>
			<el-pagination layout="total, sizes, prev, pager, next, jumper" background
						@size-change="handleSizeChange"
						@current-change="handleCurrentChange"
						:page-sizes="[10,20,50,100]" :current-page="pageNo" :page-size="pageSize" :total="total" style="float:right;">
			</el-pagination>
		</el-col>

		<create-upstream ref="createUpstream" v-on:success="getUpstreamList"/>
		<edit-upstream ref="editUpstream" v-on:success="getUpstreamList"/>
		<upstream-server ref="upstreamServer"/>
		<upstream-param ref="upstreamParam"/>
		<upstream-param-template ref="upstreamParamTemplate"/>
	</section>
</template>

<script>
	import CreateUpstream from './components/CreateUpstream';
	import EditUpstream from './components/EditUpstream';
	import UpstreamServer from "./UpstreamServer";
	import UpstreamParam from "./UpstreamParam";
	import UpstreamParamTemplate from "./UpstreamParamTemplate";
	export default {
		components: {
			'create-upstream': CreateUpstream,
			'edit-upstream': EditUpstream,
			'upstream-server': UpstreamServer,
			'upstream-param': UpstreamParam,
			'upstream-param-template': UpstreamParamTemplate
		},

		data() {
			return {
				filters: {
                    keywords: null,
                    proxyType: null,
                    loadPolicy: null
				},

				total: 0,
				upstreamList: [],
				pageNo: 1,
				pageSize: 10,
				listLoading: false,
				sels: [],    //列表选中列

				proxyTypeList: [],
				loadPolicyList: []
			}
		},
		methods: {
			handleSizeChange(psize) {
				this.getUpstreamList(1, psize);
			},
			handleCurrentChange(pno) {
				this.getUpstreamList(pno, null);
			},
			onSelsChanged: function(sels) {
				this.sels = sels;
			},
			getUpstreamList(pno, psize) {
				if (pno != null) {
					this.pageNo = pno;
				}
				if (psize != null) {
					this.pageSize = psize;
				}
				var params = {
					proxyType: this.filters.proxyType ? this.filters.proxyType : null,
					loadPolicy: this.filters.loadPolicy ? this.filters.loadPolicy : null,
					keywords: this.filters.keywords ? this.filters.keywords : null,
					pageNo: this.pageNo,
					pageSize: this.pageSize
				};
				this.listLoading = true;

				this.$axios.get(this.$global.baseUrl + '/upstream/list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.total = res.data.data.size;
						this.upstreamList = res.data.data.list;
					} else {
						this.total = 0;
						this.upstreamList = [];
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
				this.$axios.post(this.$global.baseUrl + '/upstream/move-up', params).then((res) => {
					if (res.data.code === '0000') {
						//DO NOTHING
					} else {
						this.$message.error(res.data.msg);
					}
					this.listLoading = false;
					this.getUpstreamList();
				}).catch((err) => {
					this.listLoading = false;
					this.$message.error(err.message);
				});
			},

			handleDown: function(index, row) {
				var params = "id=" + row.id;
				this.listLoading = true;
				this.$axios.post(this.$global.baseUrl + '/upstream/move-down', params).then((res) => {
					if (res.data.code === '0000') {
						//DO NOTHING
					} else {
						this.$message.error(res.data.msg);
					}
					this.listLoading = false;
					this.getUpstreamList();
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

					this.$axios.delete(this.$global.baseUrl + '/upstream/delete', {params: params}).then((res) => {
						if (res.data.code === '0000') {
							this.$message({type: "success", message: res.data.msg});
						} else {
							this.$message.error(res.data.msg);
						}
						this.listLoading = false;
						this.getUpstreamList();
					}).catch((err) => {
						this.listLoading = false;
						this.$message.error(err.message);
					});
				}).catch(() => {

				});
				this.sels = [];
			},

			initProxyTypeList() {
				let me = this;
				this.$axios.get(this.$global.baseUrl + '/upstream/proxy-type/drop-down-list', {}).then((res) => {
					if (res.data.code === '0000') {
						me.proxyTypeList = res.data.data.map(item => {
							return {val: item.id, label: item.name};
						});
					} else {
						me.proxyTypeList = [];
						me.$message.error(res.data.msg);
					}
				}).catch((err) => {
					me.$message.error(err.message);
				});
			},
			onProxyTypeChanged(proxyType) {
				if (proxyType) {
					return;
				}
				this.initProxyTypeList();
			},

			initLoadPolicyList() {
				let me = this;
				this.$axios.get(this.$global.baseUrl + '/upstream/load-policy/drop-down-list', {}).then((res) => {
					if (res.data.code === '0000') {
						me.loadPolicyList = res.data.data.map(item => {
							return {val: item.id, label: item.name, desc: item.desc};
						});
					} else {
						me.loadPolicyList = [];
						me.$message.error(res.data.msg);
					}
				}).catch((err) => {
					me.$message.error(err.message);
				});
			},
			onLoadPolicyChanged(loadPolicy) {
				if (loadPolicy) {
					return;
				}
				this.initLoadPolicyList();
			}
		},
		mounted() {
			this.initProxyTypeList();
			this.initLoadPolicyList();
			this.getUpstreamList();
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

