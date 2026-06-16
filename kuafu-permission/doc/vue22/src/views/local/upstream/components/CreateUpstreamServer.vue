<template>
<el-dialog title="新增负载节点" :visible.sync="addFormVisible" width="700px" :close-on-click-modal="true"
				append-to-body v-drag>
	<el-form :model="addForm" label-width="60px" :rules="addFormRules" ref="addForm">
        <el-row>
            <el-col :span="15">
                <el-form-item label="IP" prop="ip">
                    <el-input v-model="addForm.ip" autocomplete="off" clearable style="width: 100%;" />
                </el-form-item>
            </el-col>
            <el-col :span="9">
                <el-form-item label="端口" prop="port">
                    <el-input v-model="addForm.port" autocomplete="off" clearable style="width: 100%;" />
                </el-form-item>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="8">
                <el-form-item label="权重" prop="weight">
                    <el-input v-model="addForm.weight" autocomplete="off" clearable style="width: 100%;" />
                </el-form-item>
            </el-col>
            <el-col :span="8">
                <el-form-item label="失败等待时间" label-width="110px" prop="failTimeout">
                    <el-input v-model="addForm.failTimeout" autocomplete="off" clearable placeholder="单位：秒" style="width: 100%;" />
                </el-form-item>
            </el-col>
            <el-col :span="8">
                <el-form-item label="最大失败次数" label-width="110px" prop="maxFails">
                    <el-input v-model="addForm.maxFails" autocomplete="off" clearable style="width: 100%;" />
                </el-form-item>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="24">
                <el-form-item label="状态" prop="enabled">
                    <el-switch v-model="addForm.enabled" active-text="启用" inactive-text="禁用"
                                :active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
                </el-form-item>
            </el-col>
        </el-row>
	</el-form>
	<div slot="footer" class="dialog-footer">
		<el-button @click.native="addFormVisible = false">取消</el-button>
		<el-button type="primary" @click.native="addSubmit" :loading="addLoading" v-hasPerm="['upstream.server.create']">提交</el-button>
	</div>
</el-dialog>
</template>

<script>
	import {isInteger, isIntegerNotMust} from '@/components/js/validate';
	export default {
		data() {
			return {
				upstreamId: null,

				addFormVisible: false,//新增界面是否显示
				addLoading: false,
				addFormRules: {
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
				//新增界面数据
				addForm: {
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
			show: function(upstreamId) {
				this.upstreamId = upstreamId;
				this.addFormVisible = true;
				this.addForm = {
					ip: null,
					port: null,
					weight: null,
					failTimeout: null,
					maxFails: null,
					enabled: true
				};
				this.resetForm("addForm");
			},
			//新增
			addSubmit: function () {
				this.$refs.addForm.validate((valid) => {
					if (valid) {
						var params = "_fk=u"
										 + "&upstreamId=" + this.upstreamId
										 + (this.addForm.ip != null ? "&ip=" + this.addForm.ip : "")
										 + (this.addForm.port != null ? "&port=" + this.addForm.port : "")
										 + (this.addForm.weight != null ? "&weight=" + this.addForm.weight : "")
										 + (this.addForm.failTimeout != null ? "&failTimeout=" + this.addForm.failTimeout : "")
										 + (this.addForm.maxFails != null ? "&maxFails=" + this.addForm.maxFails : "")
										 + "&enabled=" + this.addForm.enabled;
						this.addLoading = true;

						this.$axios.post(this.$global.baseUrl + '/upstream/server/create', params).then((res) => {
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

