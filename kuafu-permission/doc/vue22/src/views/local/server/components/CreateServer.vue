<template>
	<el-dialog title="新增反向代理" :visible.sync="formVisible" width="600px" :close-on-click-modal="true" v-drag>
		<el-form :model="addForm" label-width="130px" :rules="formRules" ref="addForm">
			<el-form-item label="代理类型" prop="proxyType">
                <el-select v-model="addForm.proxyType" placeholder="请选择" @change="onProxyTypeChanged($event)"
                                clearable style="width: 100%;">
                    <el-option v-for="item in proxyTypeList" :key="item.val" :label="item.label" :value="item.val" />
                </el-select>
			</el-form-item>
			<el-form-item label="监听域名" prop="serverName">
				<el-input v-model="addForm.serverName" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="强制跳转HTTPS" prop="ifRedirectHttps">
				<el-switch v-model="addForm.ifRedirectHttps" active-text="是" inactive-text="否"
							    :active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="SSL证书pem文件" prop="sslCertPem">
				<el-input v-model="addForm.sslCertPem" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="SSL证书key文件" prop="sslCertKey">
				<el-input v-model="addForm.sslCertKey" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="SSL协议" prop="sslProtocols">
				<el-input v-model="addForm.sslProtocols" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="负载均衡" prop="proxyUpstreamId">
				<el-select v-model="addForm.proxyUpstreamId" placeholder="请输入" @change="onProxyUpstreamChanged($event)"
							    :remote-method="initProxyUpstreamList" clearable filterable remote style="width: 100%;">
					<el-option v-for="item in proxyUpstreamSlt.result.list" :key="item.val" :label="item.label" :value="item.val" />
					<el-pagination layout="total, prev, pager, next" hide-on-single-page @current-change="onProxyUpstreamPageChanged"
						:current-page="proxyUpstreamSlt.pageNo" :page-size="proxyUpstreamSlt.pageSize" :total="proxyUpstreamSlt.result.size"/>
				</el-select>
			</el-form-item>
			<el-form-item label="状态" prop="enabled">
				<el-switch v-model="addForm.enabled" active-text="启用" inactive-text="禁用"
							    :active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="备注" prop="remarks">
				<el-input type="textarea" :rows="5" placeholder="" v-model="addForm.remarks" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
            <el-button @click.native="formVisible = false">取消</el-button>
            <el-button type="primary" @click.native="addSubmit" :loading="addLoading" v-hasPerm="['server.create']">提交</el-button>
		</div>
	</el-dialog>
</template>

<script>
	export default {
		data() {
			return {
				formVisible: false,//新增界面是否显示
				addLoading: false,
				formRules: {
					proxyType: [
						{required: true, message: '请选择代理类型', trigger: 'blur'}
					]
				},
				//新增界面数据
				addForm: {
					proxyType: null,
					serverName: null,
					ifRedirectHttps: false,
					sslCertPem: null,
					sslCertKey: null,
					sslProtocols: null,
					proxyUpstreamId: null,
					enabled: true,
					remarks: null
				},

                proxyTypeList: [],
                proxyUpstreamSlt: {
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
				this.addForm = {
					proxyType: null,
					serverName: null,
					ifRedirectHttps: false,
					sslCertPem: null,
					sslCertKey: null,
					sslProtocols: null,
					proxyUpstreamId: null,
					enabled: true,
					remarks: null
				};
				this.resetForm("addForm");
                this.initProxyTypeList();
                this.proxyUpstreamSlt.result.size = 0;
				this.proxyUpstreamSlt.result.list = [];
			},
			//新增
			addSubmit: function() {
				this.$refs.addForm.validate((valid) => {
					if (valid) {
						var params = "_fk=u"
								 + (this.addForm.proxyType != null ? "&proxyType=" + this.addForm.proxyType : "")
								 + (this.addForm.serverName != null ? "&serverName=" + this.addForm.serverName : "")
								 + (this.addForm.ifRedirectHttps != null ? "&ifRedirectHttps=" + this.addForm.ifRedirectHttps : "")
								 + (this.addForm.sslCertPem != null ? "&sslCertPem=" + this.addForm.sslCertPem : "")
								 + (this.addForm.sslCertKey != null ? "&sslCertKey=" + this.addForm.sslCertKey : "")
								 + (this.addForm.sslProtocols != null ? "&sslProtocols=" + this.addForm.sslProtocols : "")
								 + (this.addForm.proxyUpstreamId != null ? "&proxyUpstreamId=" + this.addForm.proxyUpstreamId : "")
								 + (this.addForm.enabled != null ? "&enabled=" + this.addForm.enabled : "")
								 + (this.addForm.remarks != null ? "&remarks=" + this.addForm.remarks : "");
						this.addLoading = true;

						this.$axios.post(this.$global.baseUrl + '/server/create', params).then((res) => {
							if (res.data.code === '0000') {
								this.$message({type: "success", message: res.data.msg});
							} else {
								this.$message.error(res.data.msg);
							}
							this.$refs['addForm'].resetFields();
							this.formVisible = false;
							this.addLoading = false;
							this.$emit("success", 1);
						}).catch((err) => {
							this.addLoading = false;
							this.$message.error(err.message);
						});
					}
				});
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
                this.addForm.proxyUpstreamId = null;
                this.initProxyUpstreamList();
                if (proxyType) {
                    return;
                }
                this.initProxyTypeList();
            },

			initProxyUpstreamList(keywords) {
				this.proxyUpstreamSlt.keywords = keywords;
				this.onProxyUpstreamPageChanged(1, 10);
			},
			onProxyUpstreamPageChanged(pno, psize) {
				if (pno != null) {
					this.proxyUpstreamSlt.pageNo = pno;
				}
				if (psize != null) {
					this.proxyUpstreamSlt.pageSize = psize;
				}
                if (!this.addForm.proxyType) {
                    this.proxyUpstreamSlt.result.size = 0;
                    this.proxyUpstreamSlt.result.list = [];
                    return;
                }
				var params = {
                    proxyType: this.addForm.proxyType,
                    keywords: this.proxyUpstreamSlt.keywords,
                    pageNo: this.proxyUpstreamSlt.pageNo,
                    pageSize: this.proxyUpstreamSlt.pageSize
				};
				this.$axios.get(this.$global.baseUrl + '/upstream/drop-down-list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.proxyUpstreamSlt.result.size = res.data.data.size;
						this.proxyUpstreamSlt.result.list = res.data.data.list.map(item => {
							return {val: item.id, label: item.name};
						});
					} else {
						this.proxyUpstreamSlt.result.size = 0;
						this.proxyUpstreamSlt.result.list = [];
						this.$message.error(res.data.msg);
					}
				}).catch((err) => {
					this.$message.error(err.message);
				});
			},
			onProxyUpstreamChanged(proxyUpstreamId) {
				if (proxyUpstreamId) {
					return;
				}
				this.initProxyUpstreamList();
			}
		},
		mounted() {

		}
	}
</script>

<style scoped>

</style>

