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
				<el-form-item v-hasPerm="['server.list']">
					<el-button type="primary" v-on:click="getServerList(1)" class="el-icon-search"> 查询</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['server.create']">
					<el-button type="primary" @click="$refs.createServer.show()" class="el-icon-edit"> 新增</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="serverList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="ID：">
							<span>{{scope.row.id}}</span>
						</el-form-item>
						<el-form-item label="代理类型：">
							<span>{{scope.row.proxyTypeName}}</span>
						</el-form-item>
						<el-form-item label="监听域名：">
							<span>{{scope.row.serverName}}</span>
						</el-form-item>
						<el-form-item label="是否强制跳转HTTPS：">
							<span v-if="scope.row.ifRedirectHttps == null"></span>
							<span v-else-if="scope.row.ifRedirectHttps == true" class="green">是</span>
							<span v-else class="red">否</span>
						</el-form-item>
						<el-form-item label="SSL证书pem文件：">
							<span>{{scope.row.sslCertPem}}</span>
						</el-form-item>
						<el-form-item label="SSL证书key文件：">
							<span>{{scope.row.sslCertKey}}</span>
						</el-form-item>
						<el-form-item label="SSL协议：">
							<span>{{scope.row.sslProtocols}}</span>
						</el-form-item>
						<el-form-item label="负载均衡：">
							<span>{{scope.row.upstreamName}}</span>
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
						<el-form-item label="备注：">
							<el-input type="textarea" :rows="6" style="width: 600px;" readonly placeholder="" v-model="scope.row.remarks" />
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
						<el-form-item label="监听端口：">
							<div v-if="scope.row.listenList != null && scope.row.listenList.length > 0">
								<el-table :data="scope.row.listenList">
									<el-table-column prop="port" label="端口" width="70px" />
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
								</el-table>
							</div>
							<div v-else>
								<span>无</span>
							</div>
						</el-form-item>
						<el-form-item label="代理目标：">
							<div v-if="scope.row.locationList != null && scope.row.locationList.length > 0">
								<el-table :data="scope.row.locationList">
									<el-table-column prop="path" label="目标路径" width="130px" />
									<el-table-column prop="locTypeName" label="目标类型" width="110px" />
									<el-table-column prop="proxyUpstreamName" label="负载均衡" width="175px" />
									<el-table-column prop="proxyTarget" label="代理目标" width="220px" />
									<el-table-column prop="proxyStaticName" label="静态代理模式" width="95px" />
									<el-table-column prop="proxyIndexPage" label="默认页" width="95px" />
									<el-table-column prop="proxyExtPath" label="扩展代理路径" width="95px" />
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
			<el-table-column prop="proxyTypeName" label="代理类型" width="70" :show-overflow-tooltip="true" />
			<el-table-column prop="serverName" label="监听域名" width="255" :show-overflow-tooltip="true" />
			<el-table-column prop="ifRedirectHttps" label="跳转HTTPS" width="90">
				<template slot-scope="scope">
                    <el-switch v-model="scope.row.ifRedirectHttps" :disabled="!hasPerm(['server.if.redirect.https.post'])"
                            :active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"
                            @change="handleRedirectHttps(scope.row, $event)"/>
				</template>
			</el-table-column>
			<el-table-column prop="upstreamName" label="负载均衡" width="210" :show-overflow-tooltip="true" />
			<el-table-column prop="enabled" label="状态" width="50px">
				<template slot-scope="scope">
                    <el-switch v-model="scope.row.enabled" :disabled="!hasPerm(['server.status.post'])"
                            :active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"
                            @change="handleEnabled(scope.row, $event)"/>
				</template>
			</el-table-column>
			<el-table-column prop="remarks" label="备注" width="200" :show-overflow-tooltip="true" />
			<el-table-column label="操作" width="100" fixed="right">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit" @click="$refs.editServer.show(scope.$index, scope.row)" v-hasPerm="['server.update']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['server.listen.list', 'server.location.list', 'server.param.list', 'server.param.template.list', 'server.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="$refs.serverListen.show(scope.row.id)" v-hasPerm="['server.listen.list']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">监听端口</div>
									<el-button type="text" circle class="el-icon-view"> 监听端口</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="$refs.serverLocation.show(scope.row.id, scope.row.proxyType)" v-hasPerm="['server.location.list']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">代理目标</div>
									<el-button type="text" circle class="el-icon-location"> 代理目标</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="$refs.serverParam.show(scope.row.id, scope.row.serverName)" v-hasPerm="['server.param.list']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">参数列表</div>
									<el-button type="text" circle class="el-icon-s-promotion"> 参数列表</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="$refs.serverParamTemplate.show(scope.row.id, scope.row.serverName)" v-hasPerm="['server.param.template.list']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">参数模板列表</div>
									<el-button type="text" circle class="el-icon-office-building"> 参数模板列表</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['server.delete']">
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
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length===0" class="el-icon-delete" v-hasPerm="['server.delete']"> 批量删除</el-button>
			<el-pagination layout="total, sizes, prev, pager, next, jumper" background
						@size-change="handleSizeChange"
						@current-change="handleCurrentChange"
						:page-sizes="[10,20,50,100]" :current-page="pageNo" :page-size="pageSize" :total="total" style="float:right;">
			</el-pagination>
		</el-col>

		<create-server ref="createServer" v-on:success="getServerList"/>
		<edit-server ref="editServer" v-on:success="getServerList"/>
		<server-listen ref="serverListen"/>
		<server-location ref="serverLocation"/>
		<server-param ref="serverParam"/>
		<server-param-template ref="serverParamTemplate"/>
	</section>
</template>

<script>
import CreateServer from './components/CreateServer';
import EditServer from './components/EditServer';
import ServerListen from "./ServerListen";
import ServerLocation from "./ServerLocation";
import ServerParam from "./ServerParam";
import ServerParamTemplate from "./ServerParamTemplate";
export default {
    components: {
        'create-server': CreateServer,
        'edit-server': EditServer,
        'server-listen': ServerListen,
        'server-location': ServerLocation,
        'server-param': ServerParam,
        'server-param-template': ServerParamTemplate
    },

    data() {
        return {
            filters: {
              keywords: null,
              proxyType: null
            },

            total: 0,
            serverList: [],
            pageNo: 1,
            pageSize: 10,
            listLoading: false,
            sels: [],    //列表选中列

            proxyTypeList: []
        }
    },
    methods: {
        handleSizeChange(psize) {
            this.getServerList(1, psize);
        },
        handleCurrentChange(pno) {
            this.getServerList(pno, null);
        },
        onSelsChanged: function(sels) {
            this.sels = sels;
        },
        getServerList(pno, psize) {
            if (pno != null) {
                this.pageNo = pno;
            }
            if (psize != null) {
                this.pageSize = psize;
            }
            var params = {
                proxyType: this.filters.proxyType ? this.filters.proxyType : null,
                keywords: this.filters.keywords ? this.filters.keywords : null,
                pageNo: this.pageNo,
                pageSize: this.pageSize
            };
            this.listLoading = true;

            this.$axios.get(this.$global.baseUrl + '/server/list', {params: params}).then((res) => {
                if (res.data.code === '0000') {
                    this.total = res.data.data.size;
                    this.serverList = res.data.data.list;
                } else {
                    this.total = 0;
                    this.serverList = [];
                    this.$message.error(res.data.msg);
                }
                this.listLoading = false;
            }).catch((err) => {
                this.listLoading = false;
                this.$message.error(err.message);
            });
        },

        //强制跳转HTTPS/不强制跳转HTTPS
        handleRedirectHttps: function(row, ifRedirectHttps) {
            var tips = ifRedirectHttps ? "确定强制跳转HTTPS吗？" : "确定不强制跳转HTTPS吗？";
            this.$confirm(tips, '提示', {type: 'info'}).then(() => {
                var params = "id=" + row.id + "&ifRedirectHttps=" + ifRedirectHttps;
                this.$axios.post(this.$global.baseUrl + '/server/if-redirect-https', params).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.getServerList();
                }).catch((err) => {
                    this.$message.error(err.message);
                });
            }).catch(() => {
                row.ifRedirectHttps = !ifRedirectHttps;
            });
        },

        //启用/禁用
        handleEnabled: function(row, enabled) {
            var tips = enabled ? "确定启用吗？" : "确定禁用吗？";
            this.$confirm(tips, '提示', {type: 'info'}).then(() => {
                var params = "id=" + row.id + "&enabled=" + enabled;
                this.$axios.post(this.$global.baseUrl + '/server/status', params).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.getServerList();
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

                this.$axios.delete(this.$global.baseUrl + '/server/delete', {params: params}).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getServerList();
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
        }
    },
    mounted() {
        this.initProxyTypeList();
        this.getServerList();
    }
}
</script>

<style lang='scss' scoped>
.table-expand {
    font-size: 0;
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
<style scoped>
.table-expand >>> label {
    color: #99a9bf;
}
</style>

