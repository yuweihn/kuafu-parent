<template>
	<el-dialog title="编辑负载" :visible.sync="editFormVisible" width="500px" :close-on-click-modal="true" v-drag>
		<el-form :model="editForm" label-width="90px" :rules="editFormRules" ref="editForm">
			<el-form-item label="名称" prop="name">
				<el-input v-model="editForm.name" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="代理类型" prop="proxyType">
				<el-select v-model="editForm.proxyType" placeholder="请选择" @change="onProxyTypeChanged($event)"
						 clearable style="width: 100%;">
					<el-option v-for="item in proxyTypeList" :key="item.val" :label="item.label" :value="item.val" />
				</el-select>
			</el-form-item>
			<el-form-item label="负载策略" prop="loadPolicy">
				<el-select v-model="editForm.loadPolicy" placeholder="请选择" @change="onLoadPolicyChanged($event)"
						 clearable style="width: 100%;">
					<el-option v-for="item in loadPolicyList" :key="item.val" :label="item.label" :value="item.val">
						<span style="float:left">{{item.label}}</span>
						<span style="float:right; color:#8492a6; font-size:13px; margin-left:20px;">{{$util.slice(item.desc, 100)}}</span>
					</el-option>
				</el-select>
			</el-form-item>
            <el-form-item label="顺序" prop="orderBy">
                <el-input-number v-model="editForm.orderBy" autocomplete="off" clearable placeholder="" controls-position="right" :min="1" />
            </el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
			<el-button @click.native="editFormVisible = false">取消</el-button>
			<el-button type="primary" @click.native="editSubmit" :loading="editLoading" v-hasPerm="['upstream.update']">提交</el-button>
		</div>
	</el-dialog>
</template>

<script>
	import {isInteger} from '@/components/js/validate';
	export default {
		data() {
			return {
				editFormVisible: false,//编辑界面是否显示
				editLoading: false,
				editFormRules: {
					name: [
						{required: true, message: '请输入负载均衡名称', trigger: 'blur'}
					],
					proxyType: [
						{required: true, message: '请选择代理类型', trigger: 'blur'}
					],
                    orderBy: [
                        {required: true, message: '请输入序号', trigger: 'blur'}
                            , {validator: isInteger, trigger: 'blur'}
                    ]
				},
				//编辑界面数据
				editForm: {
					id: null,
					name: null,
					proxyType: null,
					loadPolicy: null,
					orderBy: null
				},

				proxyTypeList: [],
				loadPolicyList: []
			}
		},
		methods: {
			show: function(index, row) {
				this.editFormVisible = true;
				//this.editForm = Object.assign({}, row);
				this.editForm = {
					id: row.id,
					name: row.name,
					proxyType: row.proxyType,
                    loadPolicy: row.loadPolicy,
                    orderBy: row.orderBy
				};
				this.resetForm("editForm");
				this.initProxyTypeList();
				this.initLoadPolicyList();
			},

			//编辑
			editSubmit: function() {
				this.$refs.editForm.validate((valid) => {
					if (valid) {
						var params = "_fk=u" + "&id=" + this.editForm.id
								 + (this.editForm.name ? "&name=" + this.editForm.name : "")
								 + (this.editForm.proxyType ? "&proxyType=" + this.editForm.proxyType : "")
                                 + (this.editForm.loadPolicy ? "&loadPolicy=" + this.editForm.loadPolicy : "")
                                 + "&orderBy=" + this.editForm.orderBy;
						this.editLoading = true;

						this.$axios.post(this.$global.baseUrl + '/upstream/update', params).then((res) => {
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

		}
	}
</script>

<style scoped>

</style>

