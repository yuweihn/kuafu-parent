<template>
    <el-dialog title="编辑监听端口" :visible.sync="editFormVisible" width="500px" :close-on-click-modal="true"
				append-to-body v-drag>
        <el-form :model="editForm" label-width="145px" :rules="editFormRules" ref="editForm">
            <el-form-item label="端口" prop="port">
				<el-input v-model="editForm.port" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
            <el-form-item label="默认Server" prop="ifDefault">
				<el-switch v-model="editForm.ifDefault" active-text="是" inactive-text="否"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="开启Proxy Protocol" prop="ifProxyProtocol">
				<el-switch v-model="editForm.ifProxyProtocol" active-text="是" inactive-text="否"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="开启SSL" prop="ifSsl">
				<el-switch v-model="editForm.ifSsl" active-text="是" inactive-text="否"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="开启Http2" prop="ifHttp2">
				<el-switch v-model="editForm.ifHttp2" active-text="是" inactive-text="否"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click.native="editFormVisible = false">取消</el-button>
            <el-button type="primary" @click.native="editSubmit" :loading="editLoading" v-hasPerm="['server.listen.update']">提交</el-button>
        </div>
    </el-dialog>
</template>

<script>
	import {isInteger} from '@/components/js/validate';
	export default {
		data() {
			return {
				serverId: null,

                editFormVisible: false,//编辑界面是否显示
                editLoading: false,
                editFormRules: {
                    port: [
                        {required: true, message: '请输入端口号', trigger: 'blur'}
                        , {validator: isInteger, trigger: 'blur'}
                    ]
                },
                //编辑界面数据
                editForm: {
                    id: null,
                    port: null,
                    ifDefault: false,
                    ifProxyProtocol: false,
                    ifSsl: false,
                    ifHttp2: false
                }
            }
		},
		methods: {
            show: function(serverId, index, row) {
                this.serverId = serverId;
                this.editFormVisible = true;
                //this.editForm = Object.assign({}, row);
                this.editForm = {
                    id: row.id,
                    port: row.port,
                    ifDefault: row.ifDefault,
                    ifProxyProtocol: row.ifProxyProtocol,
                    ifSsl: row.ifSsl,
                    ifHttp2: row.ifHttp2
                };
                this.resetForm("editForm");
			},

            //编辑
            editSubmit: function () {
                this.$refs.editForm.validate((valid) => {
                    if (valid) {
                        var params = "id=" + this.editForm.id + "&serverId=" + this.serverId
										 + (this.editForm.port != null ? "&port=" + this.editForm.port : "")
										 + (this.editForm.ifDefault != null ? "&ifDefault=" + this.editForm.ifDefault : "")
										 + (this.editForm.ifProxyProtocol != null ? "&ifProxyProtocol=" + this.editForm.ifProxyProtocol : "")
										 + (this.editForm.ifSsl != null ? "&ifSsl=" + this.editForm.ifSsl : "")
										 + (this.editForm.ifHttp2 != null ? "&ifHttp2=" + this.editForm.ifHttp2 : "");
                        this.editLoading = true;

                        this.$axios.post(this.$global.baseUrl + '/server/listen/update', params).then((res) => {
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

