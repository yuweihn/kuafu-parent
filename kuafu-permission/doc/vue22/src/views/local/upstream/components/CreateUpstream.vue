<template>
	<el-dialog title="新增负载" :visible.sync="addFormVisible" width="500px" :close-on-click-modal="true" v-drag>
		<el-form :model="addForm" label-width="90px" :rules="addFormRules" ref="addForm">
			<el-form-item label="名称" prop="name">
				<el-input v-model="addForm.name" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="代理类型" prop="proxyType">
				<el-select v-model="addForm.proxyType" placeholder="请选择" @change="onProxyTypeChanged($event)"
						 clearable style="width: 100%;">
					<el-option v-for="item in proxyTypeList" :key="item.val" :label="item.label" :value="item.val" />
				</el-select>
			</el-form-item>
			<el-form-item label="负载策略" prop="loadPolicy">
				<el-select v-model="addForm.loadPolicy" placeholder="请选择" @change="onLoadPolicyChanged($event)"
						 clearable style="width: 100%;">
					<el-option v-for="item in loadPolicyList" :key="item.val" :label="item.label" :value="item.val">
						<span style="float:left">{{item.label}}</span>
						<span style="float:right; color:#8492a6; font-size:13px; margin-left:20px;">{{$util.slice(item.desc, 100)}}</span>
					</el-option>
				</el-select>
			</el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
			<el-button @click.native="addFormVisible = false">取消</el-button>
			<el-button type="primary" @click.native="addSubmit" :loading="addLoading" v-hasPerm="['upstream.create']">提交</el-button>
		</div>
	</el-dialog>
</template>

<script>
	export default {
		data() {
			return {
				addFormVisible: false,//新增界面是否显示
				addLoading: false,
				addFormRules: {
					name: [
						{required: true, message: '请输入负载均衡名称', trigger: 'blur'}
					],
					proxyType: [
						{required: true, message: '请选择代理类型', trigger: 'blur'}
					]
				},
				//新增界面数据
				addForm: {
					name: null,
					proxyType: null,
					loadPolicy: null
				},

				proxyTypeList: [],
				loadPolicyList: []
			}
		},
		methods: {
			show: function() {
				this.addFormVisible = true;
				this.addForm = {
					name: null,
					proxyType: null,
					loadPolicy: null
				};
				this.resetForm("addForm");
				this.initProxyTypeList();
				this.initLoadPolicyList();
			},
			//新增
			addSubmit: function() {
				this.$refs.addForm.validate((valid) => {
					if (valid) {
						var params = "_fk=u"
								 + (this.addForm.name ? "&name=" + this.addForm.name : "")
								 + (this.addForm.proxyType ? "&proxyType=" + this.addForm.proxyType : "")
								 + (this.addForm.loadPolicy ? "&loadPolicy=" + this.addForm.loadPolicy : "");
						this.addLoading = true;

						this.$axios.post(this.$global.baseUrl + '/upstream/create', params).then((res) => {
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

