<template>
<el-dialog title="编辑负载节点" :visible.sync="editFormVisible" width="700px" :close-on-click-modal="true"
				append-to-body v-drag>
	<el-form :model="editForm" label-width="60px" :rules="editFormRules" ref="editForm">
        <el-row>
            <el-col :span="15">
                <el-form-item label="IP" prop="ip">
                    <el-input v-model="editForm.ip" autocomplete="off" clearable style="width: 100%;" />
                </el-form-item>
            </el-col>
            <el-col :span="9">
                <el-form-item label="端口" prop="port">
                    <el-input v-model="editForm.port" autocomplete="off" clearable style="width: 100%;" />
                </el-form-item>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="8">
                <el-form-item label="权重" prop="weight">
                    <el-input v-model="editForm.weight" autocomplete="off" clearable style="width: 100%;" />
                </el-form-item>
            </el-col>
            <el-col :span="8">
                <el-form-item label="失败等待时间" label-width="110px" prop="failTimeout">
                    <el-input v-model="editForm.failTimeout" autocomplete="off" clearable placeholder="单位：秒" style="width: 100%;" />
                </el-form-item>
            </el-col>
            <el-col :span="8">
                <el-form-item label="最大失败次数" label-width="110px" prop="maxFails">
                    <el-input v-model="editForm.maxFails" autocomplete="off" clearable style="width: 100%;" />
                </el-form-item>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="24">
                <el-form-item label="状态" prop="enabled">
                    <el-switch v-model="editForm.enabled" active-text="启用" inactive-text="禁用"
                                :active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
                </el-form-item>
            </el-col>
        </el-row>
    </el-form>
	<div slot="footer" class="dialog-footer">
		<el-button @click.native="editFormVisible = false">取消</el-button>
		<el-button type="primary" @click.native="editSubmit" :loading="editLoading" v-hasPerm="['upstream.server.update']">提交</el-button>
	</div>
</el-dialog>
</template>

<script>
	import {isInteger, isIntegerNotMust} from '@/components/js/validate';
	export default {
		data() {
			return {
				upstreamId: null,

				editFormVisible: false,//编辑界面是否显示
				editLoading: false,
				editFormRules: {
					ip: [
						{required: true, message: '请输入IP', trigger: 'blur'}
					],
					port: [
						{required: true, message: '请输入端口号', trigger: 'blur'}
						, {validator: isInteger, trigger: 'blur'}
					],
					weight: [
						{validator: isIntegerNotMust, trigger: 'blur'}
					],
					failTimeout: [
						{validator: isIntegerNotMust, trigger: 'blur'}
					],
					maxFails: [
						{validator: isIntegerNotMust, trigger: 'blur'}
					]
				},
				//编辑界面数据
				editForm: {
					id: null,
					ip: null,
					port: null,
					weight: null,
					failTimeout: null,
					maxFails: null,
					enabled: true
				}
			}
		},
		methods: {
			show: function(upstreamId, index, row) {
				this.upstreamId = upstreamId;
				this.editFormVisible = true;
				//this.editForm = Object.assign({}, row);
				this.editForm = {
					id: row.id,
					ip: row.ip,
					port: row.port,
					weight: row.weight,
					failTimeout: row.failTimeout,
					maxFails: row.maxFails,
					enabled: row.enabled
				};
				this.resetForm("editForm");
			},

			//编辑
			editSubmit: function () {
				this.$refs.editForm.validate((valid) => {
					if (valid) {
						var params = "id=" + this.editForm.id + "&upstreamId=" + this.upstreamId
										 + (this.editForm.ip != null ? "&ip=" + this.editForm.ip : "")
										 + (this.editForm.port != null ? "&port=" + this.editForm.port : "")
										 + (this.editForm.weight != null ? "&weight=" + this.editForm.weight : "")
										 + (this.editForm.failTimeout != null ? "&failTimeout=" + this.editForm.failTimeout : "")
										 + (this.editForm.maxFails != null ? "&maxFails=" + this.editForm.maxFails : "")
										 + "&enabled=" + this.editForm.enabled;
						this.editLoading = true;

						this.$axios.post(this.$global.baseUrl + '/upstream/server/update', params).then((res) => {
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
			}
		},
		mounted() {

		}
	}
</script>

<style scoped>

</style>

