<template>
    <el-dialog title="新增代理目标" :visible.sync="addFormVisible" width="550px" :close-on-click-modal="true"
				append-to-body v-drag>
        <el-form :model="addForm" label-width="120px" :rules="addFormRules" ref="addForm">
            <el-form-item label="目标路径" prop="path">
                <el-input v-model="addForm.path" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
			<el-form-item label="目标类型" prop="locType">
                <el-select v-model="addForm.locType" placeholder="请选择" @change="onLocTypeChanged($event)"
                            clearable style="width: 100%;">
                    <el-option v-for="item in locTypeList" :key="item.val" :label="item.label" :value="item.val" />
                </el-select>
			</el-form-item>
			<el-form-item label="负载均衡" prop="proxyUpstreamId">
				<el-select v-model="addForm.proxyUpstreamId" placeholder="请输入" @change="onProxyUpstreamChanged($event)"
							:remote-method="initProxyUpstreamList" clearable filterable remote style="width: 100%;">
					<el-option v-for="item in proxyUpstreamSlt.result.list" :key="item.val" :label="item.label" :value="item.val" />
					<el-pagination layout="total, prev, pager, next" hide-on-single-page @current-change="onProxyUpstreamPageChanged"
						:current-page="proxyUpstreamSlt.pageNo" :page-size="proxyUpstreamSlt.pageSize" :total="proxyUpstreamSlt.result.size"/>
				</el-select>
			</el-form-item>
            <el-form-item label="目标地址" prop="proxyTarget">
                <el-input v-model="addForm.proxyTarget" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
			<el-form-item label="静态代理模式" prop="proxyStaticType">
                <el-select v-model="addForm.proxyStaticType" placeholder="请选择" @change="onStaticTypeChanged($event)"
                            clearable style="width: 100%;">
                    <el-option v-for="item in staticTypeList" :key="item.val" :label="item.label" :value="item.val" />
                </el-select>
			</el-form-item>
            <el-form-item label="默认页" prop="proxyIndexPage">
                <el-input v-model="addForm.proxyIndexPage" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
            <el-form-item label="扩展代理路径" prop="proxyExtPath">
                <el-input v-model="addForm.proxyExtPath" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click.native="addFormVisible = false">取消</el-button>
            <el-button type="primary" @click.native="addSubmit" :loading="addLoading" v-hasPerm="['server.location.create']">提交</el-button>
        </div>
    </el-dialog>
</template>

<script>
	export default {
		data() {
			return {
				serverId: null,
				proxyType: null,

                addFormVisible: false,//新增界面是否显示
                addLoading: false,
                addFormRules: {
                    path: [
                        {required: true, message: '请输入监控路径', trigger: 'blur'}
                    ],
                    locType: [
                        {required: true, message: '请选择目标类型', trigger: 'blur'}
                    ]
                },
                //新增界面数据
                addForm: {
                    path: null,
                    locType: null,
                    proxyUpstreamId: null,
                    proxyTarget: null,
                    proxyStaticType: null,
                    proxyIndexPage: null,
                    proxyExtPath: null
                },

                locTypeList: [],
                staticTypeList: [],
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
            show: function(serverId, proxyType) {
                this.serverId = serverId;
                this.proxyType = proxyType;
                this.addFormVisible = true;
                this.addForm = {
                    path: null,
                    locType: null,
                    proxyUpstreamId: null,
                    proxyTarget: null,
                    proxyStaticType: null,
                    proxyIndexPage: null,
                    proxyExtPath: null
                };
                this.resetForm("addForm");
                this.initLocTypeList();
                this.initStaticTypeList();
                this.initProxyUpstreamList();
			},
			//新增
            addSubmit: function () {
                this.$refs.addForm.validate((valid) => {
                    if (valid) {
						var params = "_fk=u"
                                         + "&serverId=" + this.serverId
										 + (this.addForm.path != null ? "&path=" + this.addForm.path : "")
										 + (this.addForm.locType != null ? "&locType=" + this.addForm.locType : "")
										 + (this.addForm.proxyUpstreamId != null ? "&proxyUpstreamId=" + this.addForm.proxyUpstreamId : "")
										 + (this.addForm.proxyTarget != null ? "&proxyTarget=" + this.addForm.proxyTarget : "")
										 + (this.addForm.proxyStaticType != null ? "&proxyStaticType=" + this.addForm.proxyStaticType : "")
										 + (this.addForm.proxyIndexPage != null ? "&proxyIndexPage=" + this.addForm.proxyIndexPage : "")
										 + (this.addForm.proxyExtPath != null ? "&proxyExtPath=" + this.addForm.proxyExtPath : "");
                        this.addLoading = true;

                        this.$axios.post(this.$global.baseUrl + '/server/location/create', params).then((res) => {
                            if (res.data.code === '0000') {
                                this.$message({type: "success", message: res.data.msg});
                            } else {
                                this.$message.error(res.data.msg);
                            }
                            this.$refs['addForm'].resetFields();
                            this.addFormVisible = false;
                            this.addLoading = false;
                            this.$emit("success", 1);
                        }).catch((err) => {
                            this.addLoading = false;
                            this.$message.error(err.message);
                        });
                    }
                });
            },

            initLocTypeList() {
                let me = this;
                this.$axios.get(this.$global.baseUrl + '/server/location/type/drop-down-list', {}).then((res) => {
                    if (res.data.code === '0000') {
                        me.locTypeList = res.data.data.map(item => {
                            return {val: item.id, label: item.name};
                        });
                    } else {
                        me.locTypeList = [];
                        me.$message.error(res.data.msg);
                    }
                }).catch((err) => {
                    me.$message.error(err.message);
                });
            },
            onLocTypeChanged(locType) {
                if (locType) {
                    return;
                }
                this.initLocTypeList();
            },

            initStaticTypeList() {
                let me = this;
                this.$axios.get(this.$global.baseUrl + '/server/location/static-type/drop-down-list', {}).then((res) => {
                    if (res.data.code === '0000') {
                        me.staticTypeList = res.data.data.map(item => {
                            return {val: item.id, label: item.name};
                        });
                    } else {
                        me.staticTypeList = [];
                        me.$message.error(res.data.msg);
                    }
                }).catch((err) => {
                    me.$message.error(err.message);
                });
            },
            onStaticTypeChanged(staticType) {
                if (staticType) {
                    return;
                }
                this.initStaticTypeList();
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
				if (!this.proxyType) {
                    this.proxyUpstreamSlt.result.size = 0;
                    this.proxyUpstreamSlt.result.list = [];
                    return;
				}
				var params = {
                    proxyType: this.proxyType,
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

