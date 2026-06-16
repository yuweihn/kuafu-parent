<template>
    <el-dialog title="编辑代理目标" :visible.sync="editFormVisible" width="550px" :close-on-click-modal="true"
				append-to-body v-drag>
        <el-form :model="editForm" label-width="120px" :rules="editFormRules" ref="editForm">
            <el-form-item label="目标路径" prop="path">
				<el-input v-model="editForm.path" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
            <el-form-item label="目标类型" prop="locType">
                <el-select v-model="editForm.locType" placeholder="请选择" @change="onLocTypeChanged($event)"
                            clearable style="width: 100%;">
                    <el-option v-for="item in locTypeList" :key="item.val" :label="item.label" :value="item.val" />
                </el-select>
            </el-form-item>
			<el-form-item label="负载均衡" prop="proxyUpstreamId">
				<el-select v-model="editForm.proxyUpstreamId" placeholder="请输入" @change="onProxyUpstreamChanged($event)"
							:remote-method="initProxyUpstreamList" clearable filterable remote style="width: 100%;">
					<el-option v-for="item in proxyUpstreamSlt.result.list" :key="item.val" :label="item.label" :value="item.val" />
					<el-pagination layout="total, prev, pager, next" hide-on-single-page @current-change="onProxyUpstreamPageChanged"
						:current-page="proxyUpstreamSlt.pageNo" :page-size="proxyUpstreamSlt.pageSize" :total="proxyUpstreamSlt.result.size"/>
				</el-select>
			</el-form-item>
            <el-form-item label="目标地址" prop="proxyTarget">
                <el-input v-model="editForm.proxyTarget" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
            <el-form-item label="静态代理模式" prop="proxyStaticType">
                <el-select v-model="editForm.proxyStaticType" placeholder="请选择" @change="onStaticTypeChanged($event)"
                                    clearable style="width: 100%;">
                    <el-option v-for="item in staticTypeList" :key="item.val" :label="item.label" :value="item.val" />
                </el-select>
            </el-form-item>
            <el-form-item label="默认页" prop="proxyIndexPage">
                <el-input v-model="editForm.proxyIndexPage" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
            <el-form-item label="扩展代理路径" prop="proxyExtPath">
                <el-input v-model="editForm.proxyExtPath" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click.native="editFormVisible = false">取消</el-button>
            <el-button type="primary" @click.native="editSubmit" :loading="editLoading" v-hasPerm="['server.location.update']">提交</el-button>
        </div>
    </el-dialog>
</template>

<script>
	export default {
		data() {
			return {
				serverId: null,
				proxyType: null,

                editFormVisible: false,//编辑界面是否显示
                editLoading: false,
                editFormRules: {
                    path: [
                        {required: true, message: '请输入监控路径', trigger: 'blur'}
                    ],
                    locType: [
                        {required: true, message: '请选择目标类型', trigger: 'blur'}
                    ]
                },
                //编辑界面数据
                editForm: {
                    id: null,
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
			show: function(serverId, proxyType, index, row) {
                this.serverId = serverId;
                this.proxyType = proxyType;
                this.editFormVisible = true;
                //this.editForm = Object.assign({}, row);
                this.editForm = {
                    id: row.id,
                    path: row.path,
                    locType: row.locType,
                    proxyUpstreamId: row.proxyUpstreamId,
                    proxyTarget: row.proxyTarget,
                    proxyStaticType: row.proxyStaticType,
                    proxyIndexPage: row.proxyIndexPage,
                    proxyExtPath: row.proxyExtPath
                };
                this.resetForm("editForm");
                this.initLocTypeList();
                this.initStaticTypeList();
                if (row.proxyUpstreamId) {
                    this.proxyUpstreamSlt.result.size = 1;
                    this.proxyUpstreamSlt.result.list = [
                        {
                            val: row.proxyUpstreamId,
                            label: row.proxyUpstreamName
                        }
                    ];
				} else {
                    this.initProxyUpstreamList();
				}
			},

            //编辑
            editSubmit: function () {
                this.$refs.editForm.validate((valid) => {
                    if (valid) {
                        var params = "id=" + this.editForm.id + "&serverId=" + this.serverId
										 + (this.editForm.path != null ? "&path=" + this.editForm.path : "")
										 + (this.editForm.locType != null ? "&locType=" + this.editForm.locType : "")
										 + (this.editForm.proxyUpstreamId != null ? "&proxyUpstreamId=" + this.editForm.proxyUpstreamId : "")
										 + (this.editForm.proxyTarget != null ? "&proxyTarget=" + this.editForm.proxyTarget : "")
										 + (this.editForm.proxyStaticType != null ? "&proxyStaticType=" + this.editForm.proxyStaticType : "")
										 + (this.editForm.proxyIndexPage != null ? "&proxyIndexPage=" + this.editForm.proxyIndexPage : "")
										 + (this.editForm.proxyExtPath != null ? "&proxyExtPath=" + this.editForm.proxyExtPath : "");
                        this.editLoading = true;

                        this.$axios.post(this.$global.baseUrl + '/server/location/update', params).then((res) => {
                            if (res.data.code === '0000') {
                                this.$message({type: "success", message: res.data.msg});
                            } else {
                                this.$message.error(res.data.msg);
                            }
                            this.$refs['editForm'].resetFields();
                            this.editFormVisible = false;
                            this.editLoading = false;
                            this.$emit("success", 1);
                        }).catch((err) => {
                            this.editLoading = false;
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

