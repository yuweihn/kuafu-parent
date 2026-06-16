<template>
	<el-dialog title="编辑反向代理" :visible.sync="formVisible" width="600px" :close-on-click-modal="true" v-drag>
		<el-form :model="editForm" label-width="130px" :rules="formRules" ref="editForm">
			<el-form-item label="代理类型" prop="proxyType">
                <el-select v-model="editForm.proxyType" placeholder="请选择" @change="onProxyTypeChanged($event)"
                            clearable style="width: 100%;">
                    <el-option v-for="item in proxyTypeList" :key="item.val" :label="item.label" :value="item.val" />
                </el-select>
			</el-form-item>
			<el-form-item label="监听域名" prop="serverName">
				<el-input v-model="editForm.serverName" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="强制跳转HTTPS" prop="ifRedirectHttps">
				<el-switch v-model="editForm.ifRedirectHttps" active-text="是" inactive-text="否"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="SSL证书pem文件" prop="sslCertPem">
				<el-input v-model="editForm.sslCertPem" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="SSL证书key文件" prop="sslCertKey">
				<el-input v-model="editForm.sslCertKey" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="SSL协议" prop="sslProtocols">
				<el-input v-model="editForm.sslProtocols" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="负载均衡" prop="proxyUpstreamId">
				<el-select v-model="editForm.proxyUpstreamId" placeholder="请输入" @change="onProxyUpstreamChanged($event)"
							:remote-method="initProxyUpstreamList" clearable filterable remote style="width: 100%;">
					<el-option v-for="item in proxyUpstreamSlt.result.list" :key="item.val" :label="item.label" :value="item.val" />
					<el-pagination layout="total, prev, pager, next" hide-on-single-page @current-change="onProxyUpstreamPageChanged"
						:current-page="proxyUpstreamSlt.pageNo" :page-size="proxyUpstreamSlt.pageSize" :total="proxyUpstreamSlt.result.size"/>
				</el-select>
			</el-form-item>
			<el-form-item label="状态" prop="enabled">
				<el-switch v-model="editForm.enabled" active-text="启用" inactive-text="禁用"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="备注" prop="remarks">
				<el-input type="textarea" :rows="5" placeholder="" v-model="editForm.remarks" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
			<el-button @click.native="formVisible = false">取消</el-button>
			<el-button type="primary" @click.native="editSubmit" :loading="editLoading" v-hasPerm="['server.update']">提交</el-button>
		</div>
	</el-dialog>
</template>

<script>
	export default {
		data() {
			return {
				formVisible: false,//编辑界面是否显示
				editLoading: false,
				formRules: {
					proxyType: [
						{required: true, message: '请选择代理类型', trigger: 'blur'}
					]
				},
				//编辑界面数据
				editForm: {
					id: null,
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
			show: function(index, row) {
				this.formVisible = true;
				//this.editForm = Object.assign({}, row);
				this.editForm = {
					id: row.id,
					proxyType: row.proxyType,
					serverName: row.serverName,
					ifRedirectHttps: row.ifRedirectHttps,
					sslCertPem: row.sslCertPem,
					sslCertKey: row.sslCertKey,
					sslProtocols: row.sslProtocols,
					proxyUpstreamId: row.proxyUpstreamId,
					enabled: row.enabled,
					remarks: row.remarks
				};
                this.resetForm("editForm");
                this.initProxyTypeList();
				if (row.proxyUpstreamId == null) {
                    this.initProxyUpstreamList();
				} else {
                    this.proxyUpstreamSlt.result.size = 1;
                    this.proxyUpstreamSlt.result.list = [
                        {
                            val: row.proxyUpstreamId,
                            label: row.upstreamName
                        }
                    ];
				}
			},

			//编辑
			editSubmit: function() {
				this.$refs.editForm.validate((valid) => {
					if (valid) {
						var params = "_fk=u" + "&id=" + this.editForm.id
								 + (this.editForm.proxyType != null ? "&proxyType=" + this.editForm.proxyType : "")
								 + (this.editForm.serverName != null ? "&serverName=" + this.editForm.serverName : "")
								 + (this.editForm.ifRedirectHttps != null ? "&ifRedirectHttps=" + this.editForm.ifRedirectHttps : "")
								 + (this.editForm.sslCertPem != null ? "&sslCertPem=" + this.editForm.sslCertPem : "")
								 + (this.editForm.sslCertKey != null ? "&sslCertKey=" + this.editForm.sslCertKey : "")
								 + (this.editForm.sslProtocols != null ? "&sslProtocols=" + this.editForm.sslProtocols : "")
								 + (this.editForm.proxyUpstreamId != null ? "&proxyUpstreamId=" + this.editForm.proxyUpstreamId : "")
								 + (this.editForm.enabled != null ? "&enabled=" + this.editForm.enabled : "")
								 + (this.editForm.remarks != null ? "&remarks=" + this.editForm.remarks : "");
						this.editLoading = true;

						this.$axios.post(this.$global.baseUrl + '/server/update', params).then((res) => {
							if (res.data.code === '0000') {
								this.$message({type: "success", message: res.data.msg});
							} else {
								this.$message.error(res.data.msg);
							}
							this.$refs['editForm'].resetFields();
							this.formVisible = false;
							this.editLoading = false;
							this.$emit("success", 1);
						}).catch((err) => {
							this.editLoading = false;
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
                this.editForm.proxyUpstreamId = null;
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
				if (!this.editForm.proxyType) {
                    this.proxyUpstreamSlt.result.size = 0;
                    this.proxyUpstreamSlt.result.list = [];
                    return;
				}
				var params = {
                    proxyType: this.editForm.proxyType,
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

