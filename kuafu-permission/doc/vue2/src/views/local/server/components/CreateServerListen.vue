<template>
    <el-dialog title="新增监听端口" :visible.sync="addFormVisible" width="500px" :close-on-click-modal="true"
				append-to-body v-drag>
        <el-form :model="addForm" label-width="145px" :rules="addFormRules" ref="addForm">
            <el-form-item label="端口" prop="port">
                <el-input v-model="addForm.port" autocomplete="off" clearable style="width: 100%;" />
            </el-form-item>
			<el-form-item label="默认Server" prop="ifDefault">
				<el-switch v-model="addForm.ifDefault" active-text="是" inactive-text="否"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="开启Proxy Protocol" prop="ifProxyProtocol">
				<el-switch v-model="addForm.ifProxyProtocol" active-text="是" inactive-text="否"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="开启SSL" prop="ifSsl">
				<el-switch v-model="addForm.ifSsl" active-text="是" inactive-text="否"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
			<el-form-item label="开启Http2" prop="ifHttp2">
				<el-switch v-model="addForm.ifHttp2" active-text="是" inactive-text="否"
							:active-value="true" :inactive-value="false" active-color="#13ce66" inactive-color="#ff4949"/>
			</el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click.native="addFormVisible = false">取消</el-button>
            <el-button type="primary" @click.native="addSubmit" :loading="addLoading" v-hasPerm="['server.listen.create']">提交</el-button>
        </div>
    </el-dialog>
</template>

<script>
	import {isInteger} from '@/components/js/validate';
	export default {
		data() {
			return {
				serverId: null,

                addFormVisible: false,//新增界面是否显示
                addLoading: false,
                addFormRules: {
                    port: [
                        {required: true, message: '请输入端口号', trigger: 'blur'}
                        , {validator: isInteger, trigger: 'blur'}
                    ]
                },
                //新增界面数据
                addForm: {
                    port: null,
                    ifDefault: false,
                    ifProxyProtocol: false,
                    ifSsl: false,
                    ifHttp2: false
                }
			}
		},
		methods: {
            show: function(serverId) {
                this.serverId = serverId;
                this.addFormVisible = true;
                this.addForm = {
                    port: null,
                    ifDefault: false,
                    ifProxyProtocol: false,
                    ifSsl: false,
                    ifHttp2: false
                };
                this.resetForm("addForm");
			},
            //新增
            addSubmit: function () {
                this.$refs.addForm.validate((valid) => {
                    if (valid) {
                        var params = "_fk=u"
                                         + "&serverId=" + this.serverId
										 + (this.addForm.port != null ? "&port=" + this.addForm.port : "")
										 + (this.addForm.ifDefault != null ? "&ifDefault=" + this.addForm.ifDefault : "")
										 + (this.addForm.ifProxyProtocol != null ? "&ifProxyProtocol=" + this.addForm.ifProxyProtocol : "")
										 + (this.addForm.ifSsl != null ? "&ifSsl=" + this.addForm.ifSsl : "")
										 + (this.addForm.ifHttp2 != null ? "&ifHttp2=" + this.addForm.ifHttp2 : "");
                        this.addLoading = true;

                        this.$axios.post(this.$global.baseUrl + '/server/listen/create', params).then((res) => {
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

