<template>
	<el-dialog title="新增子目录" :visible.sync="formVisible" width="500px" :close-on-click-modal="true" v-drag>
		<el-form :model="aForm" label-width="85px" :rules="formRules" ref="aForm" style="padding-right: 10px;">
			<el-form-item label="当前目录" prop="curDir">
				<el-input v-model="aForm.curDir" clearable readonly autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="子目录" prop="subDir">
				<el-input v-model="aForm.subDir" clearable autocomplete="off" placeholder="" style="width: 100%;"/>
			</el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
			<el-button @click.native="formVisible = false">取消</el-button>
			<el-button type="primary" @click.native="doSubmit" :loading="loading" v-hasPerm="['cert.dir.create']">提交</el-button>
		</div>
	</el-dialog>
</template>

<script>
	export default {
		data() {
			return {
				formVisible: false,
				loading: false,
				formRules: {
					subDir: [
						{required: true, message: '请输入子目录', trigger: 'blur'}
					]
				},
				//页面数据
				aForm: {
					curDir: null,
					subDir: null
				}
			}
		},
		methods: {
			show: function(curPath) {
				this.formVisible = true;
				this.aForm = {
					curDir: curPath,
					subDir: null
				};
				this.resetForm("aForm");
			},
			//新增
			doSubmit: function() {
				this.$refs.aForm.validate((valid) => {
					if (valid) {
					  var _curDir = this.aForm.curDir;
						var params = "_fk=u"
								 + (_curDir ? "&curDir=" + _curDir : "")
								 + (this.aForm.subDir ? "&subDir=" + this.aForm.subDir : "");
						this.loading = true;

						this.$axios.post(this.$global.baseUrl + '/cert/dir/create', params).then((res) => {
							if (res.data.code === '0000') {
								this.$message({type: "success", message: res.data.msg});
							} else {
								this.$message.error(res.data.msg);
							}
							this.$refs['aForm'].resetFields();
							this.formVisible = false;
							this.loading = false;
							this.$emit("success", 1, _curDir);
						}).catch((err) => {
							this.loading = false;
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

