<template>
<el-dialog title="启动/停止" :visible.sync="formVisible" width="600px" :close-on-click-modal="true"
		 append-to-body v-drag>
    <el-form :model="form" label-width="75px" :rules="formRules" ref="form">
        <el-form-item label="CMD" prop="cmd">
            <el-select v-model="form.cmd" placeholder="请选择" @change="onCommandChanged($event)" clearable style="width: 100%;">
                <el-option-group v-for="group in cmdSlt" :key="group.label" :label="group.label">
                    <el-option v-for="item in group.list" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-option-group>
            </el-select>
        </el-form-item>
    </el-form>
    <div style="padding-left: 15px;"><pre style="white-space: pre-wrap;">{{result}}</pre></div>
    <div slot="footer" class="dialog-footer">
        <el-button @click.native="formVisible = false" class="el-icon-close"> 关闭</el-button>
        <el-button type="primary" @click="doExecute" :loading="loading" class="el-icon-caret-right" v-hasPerm="['conf.cmd.execute']"> 执行</el-button>
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
					cmd: [
						{required: true, message: '请选择命令', trigger: 'blur'}
					]
				},
				form: {
					cmd: null
				},

				cmdSlt: [],
				result: null
			}
		},
		methods: {
			show: function() {
				this.formVisible = true;
				this.loading = false;
				this.cmdSlt = [];
				this.form.cmd = null;
				this.result = null;
				this.getConfExeCmdList();
			},
			getConfExeCmdList() {
				this.loading = true;
				this.targetConf = "";

				this.$axios.get(this.$global.baseUrl + '/conf/cmd/get_exe_cmd_info_list', {}).then((res) => {
					if (res.data.code === '0000') {
						var _data = res.data.data;
						if (_data != null) {
							this.initCmdSlt(_data.startList, _data.stopList, _data.restartList);
						}
					} else {
						this.$message.error(res.data.msg);
					}
					this.loading = false;
				}).catch((err) => {
					this.loading = false;
					this.$message.error(err.message);
				});
			},
			initCmdSlt: function(startList, stopList, restartList) {
                if (startList) {
                    this.cmdSlt.push({
                        label: '启动',
                        list: startList.map(item => ({value: item, label: item}))
                    });
                }
                if (stopList) {
                    this.cmdSlt.push({
                        label: '停止',
                        list: stopList.map(item => ({value: item, label: item}))
                    });
                }
                if (restartList) {
                    this.cmdSlt.push({
                        label: '重启',
                        list: restartList.map(item => ({value: item, label: item}))
                    });
                }
			},
			onCommandChanged(cmd) {
				this.result = null;
			},
			doExecute: function() {
				this.$refs.form.validate((valid) => {
					if (valid) {
                        this.result = null;
                        if (this.form.cmd == null || this.form.cmd == '') {
                            this.$message.error("请选择命令");
                            return;
                        }
                        var params = "cmd=" + this.form.cmd;
                        this.loading = true;

                        this.$axios.post(this.$global.baseUrl + '/conf/cmd/execute', params).then((res) => {
                            if (res.data.code === '0000') {
                                this.result = res.data.msg;
                            } else {
                                this.result = res.data.msg;
                            }
                            this.loading = false;
                            this.$emit("callback");
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

