<template>
<el-dialog title="设置Nginx执行文件" :visible.sync="formVisible" width="500px" :close-on-click-modal="true"
		 append-to-body v-drag>
    <el-form :model="form" label-width="120px" :rules="formRules" ref="form">
        <el-form-item label="Nginx执行文件" prop="nginxExe">
            <el-input v-model="form.nginxExe" clearable autocomplete="off" style="width: 100%;" />
        </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
        <el-button @click.native="formVisible = false">取消</el-button>
        <el-button type="primary" @click.native="editSubmit" :loading="loading" v-hasPerm="['conf.setting.nginxexe']">提交</el-button>
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
					nginxExe: [
						{required: true, message: '请输入Nginx执行文件', trigger: 'blur'}
					]
				},
				form: {
					nginxExe: null
				}
			}
		},
		methods: {
			show: function(nginxExe) {
				this.resetForm("form");
				this.formVisible = true;
				this.loading = false;
				this.form.nginxExe = nginxExe;
			},

            editSubmit: function() {
				this.$refs.form.validate((valid) => {
					if (valid) {
                        var params = "_fk=u&value=" + this.form.nginxExe;
                        this.loading = true;

                        this.$axios.post(this.$global.baseUrl + '/conf/setting/nginxexe', params).then((res) => {
                            if (res.data.code != '0000') {
                                this.$message.error(res.data.msg);
                            }
                            this.$refs['form'].resetFields();
                            this.formVisible = false;
                            this.loading = false;
                            this.$emit("callback", 1);
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

