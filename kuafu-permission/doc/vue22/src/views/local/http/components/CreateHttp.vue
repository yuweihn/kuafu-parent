<template>
	<el-dialog title="新增Http参数" :visible.sync="addFormVisible" width="500px" :close-on-click-modal="true" v-drag>
		<el-form :model="addForm" label-width="80px" :rules="addFormRules" ref="addForm">
			<el-form-item label="参数名" prop="name">
				<el-input v-model="addForm.name" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="参数值" prop="value">
				<el-input type="textarea" :rows="5" v-model="addForm.value" clearable autocomplete="off" style="width: 100%;"/>
			</el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
			<el-button @click.native="addFormVisible = false">取消</el-button>
			<el-button type="primary" @click.native="addSubmit" :loading="addLoading" v-hasPerm="['http.create']">提交</el-button>
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
						{required: true, message: '请输入参数名', trigger: 'blur'}
					],
					value: [
						{required: true, message: '请输入参数值', trigger: 'blur'}
					]
				},
				//新增界面数据
				addForm: {
					name: null,
					value: null
				}
			}
		},
		methods: {
			show: function() {
				this.addFormVisible = true;
				this.addForm = {
					name: null,
					value: null
				};
				this.resetForm("addForm");
			},
			//新增
			addSubmit: function() {
				this.$refs.addForm.validate((valid) => {
					if (valid) {
						var params = "_fk=u"
								 + (this.addForm.name ? "&name=" + this.addForm.name : "")
								 + (this.addForm.value ? "&value=" + this.addForm.value : "");
						this.addLoading = true;

						this.$axios.post(this.$global.baseUrl + '/http/create', params).then((res) => {
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
			}
		},
		mounted() {

		}
	}
</script>

<style scoped>

</style>

