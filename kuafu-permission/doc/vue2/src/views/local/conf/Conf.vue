<template>
<section v-loading="loading">
	<el-col :span="24" class="toolbar" style="padding-bottom: 0px;" v-loading="settingLoading">
		<el-form :inline="true">
			<el-form-item label="Nginx执行文件：">
				<el-input v-model="nginxExe" readonly placeholder="" style="width: 210px;" />
                <el-link :underline="false" v-hasPerm="['conf.setting.nginxexe']">
                    <font class="el-icon-edit green" style="padding-left: 3px;" @click="toChangeNginxExe()"></font>
                </el-link>
			</el-form-item>
			<el-form-item label="Nginx目录：">
				<el-input v-model="nginxDir" readonly placeholder="" />
			</el-form-item>
			<el-form-item label="目标配置文件：">
				<el-input v-model="nginxPath" readonly placeholder="" />
			</el-form-item>
			<br/>
			<el-form-item>
				<el-button type="primary" v-on:click="refreshConf()" class="el-icon-refresh"> 刷新配置</el-button>
			</el-form-item>
			<el-form-item v-hasPerm="['conf.replace']">
				<el-button type="primary" v-on:click="replaceConf()" class="el-icon-download"> 替换文件</el-button>
			</el-form-item>
			<el-form-item v-hasPerm="['conf.check']">
				<el-button type="primary" v-on:click="checkConf()" class="el-icon-circle-check"> 校验文件</el-button>
			</el-form-item>
			<el-form-item v-hasPerm="['conf.reload']">
				<el-button type="primary" v-on:click="reloadConf()" class="el-icon-s-cooperation"> 重新载入</el-button>
			</el-form-item>
			<el-form-item style="width: 40px; text-align: right;"/>
			<el-form-item>
                <span>分解配置</span>
                <el-switch v-model="decompose" active-text="" inactive-text="" :disabled="!hasPerm(['conf.setting.decompose'])"
        							:active-value="true" :inactive-value="false"
        							@change="saveDecompose" active-color="#13ce66" inactive-color="#cccccc"/>
			</el-form-item>
			<el-form-item>
				<el-button type="success" v-on:click="toExecute()" class="el-icon-d-arrow-right" v-hasPerm="['conf.cmd.execute']"> 启动/停止</el-button>
			</el-form-item>
			<el-form-item>
				<div>
					Nginx状态：
					<span v-if="isNginxRun != null && isNginxRun == true">
                        <el-link :underline="false">
                            <font class="el-icon-success green" @click="checkNginxStatus()" v-loading="statusLoading"></font>
                        </el-link>
                        <span> 运行中</span>
					</span>
					<span v-else>
                        <el-link :underline="false">
                            <font class="el-icon-error red" @click="checkNginxStatus()" v-loading="statusLoading"></font>
                        </el-link>
                        <span> 未运行</span>
					</span>
				</div>
			</el-form-item>
		</el-form>
	</el-col>

    <div class='source-conf conf' v-loading="sourceLoading">
        <div class="ct">源配置：</div>
        <el-input type="textarea" v-model="sourceConf.content" :rows="25" class="ta" clearable readonly autocomplete="off" wrap="off"/>
        <div class="sub-conf" v-if="sourceConf.subConfList != null && sourceConf.subConfList.length > 0">
            <el-table :data="sourceConf.subConfList">
                <el-table-column type="expand" width="18">
                    <template slot-scope="scope">
                        <el-input type="textarea" v-model="scope.row.content" :rows="8" clearable readonly autocomplete="off" wrap="off"/>
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot-scope="scope2">{{scope2.row.name}}</template>
                </el-table-column>
            </el-table>
        </div>
    </div>
    <div class='target-conf conf' v-loading="targetLoading">
        <div class="ct">目标配置：</div>
        <el-input type="textarea" v-model="targetConf.content" :rows="25" class="ta" clearable readonly autocomplete="off" wrap="off"/>
        <div class="sub-conf" v-if="targetConf.subConfList != null && targetConf.subConfList.length > 0">
            <el-table :data="targetConf.subConfList">
                <el-table-column type="expand" width="18">
                    <template slot-scope="scope">
                        <el-input type="textarea" v-model="scope.row.content" :rows="8" clearable readonly autocomplete="off" wrap="off"/>
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot-scope="scope2">{{scope2.row.name}}</template>
                </el-table-column>
            </el-table>
        </div>
    </div>

    <message ref="message"/>
    <exe-nginx ref="exeNginx" v-on:callback="executeNginxCallback"/>
    <change-nginx-exe ref="changeNginxExe" v-on:callback="changeNginxExeCallback"/>
</section>
</template>

<script>
import Message from "@/views/components/Message";
import ChangeNginxExe from "@/views/components/ChangeNginxExe";
import ExeNginx from "./ExeNginx";
export default {
	components: {
        'exe-nginx': ExeNginx,
        'message': Message,
        'change-nginx-exe': ChangeNginxExe
	},

	data () {
		return {
			loading: false,
			settingLoading: false,
			sourceLoading: false,
			targetLoading: false,
			isNginxRun: false,
			statusLoading: false,

			nginxExe: null,
			nginxDir: null,
			nginxPath: null,
			sourceConf: {},
			targetConf: {},
			decompose: false
		}
	},
	methods: {
		getConfSetting() {
			this.settingLoading = true;

			this.$axios.get(this.$global.baseUrl + '/conf/setting', {}).then((res) => {
				if (res.data.code === '0000') {
					var confInfo = res.data.data;
					if (confInfo != null) {
						this.nginxExe = confInfo.nginxExe == null ? "" : confInfo.nginxExe;
						this.nginxDir = confInfo.nginxDir == null ? "" : confInfo.nginxDir;
						this.nginxPath = confInfo.nginxPath == null ? "" : confInfo.nginxPath;
						this.decompose = confInfo.decompose == null ? false : confInfo.decompose;
					}
				} else {
					this.$message.error(res.data.msg);
				}
				this.settingLoading = false;
			}).catch((err) => {
				this.settingLoading = false;
				this.$message.error(err.message);
			});
		},
		getSourceConfContent() {
			this.sourceLoading = true;
			this.sourceConf = {};

			this.$axios.get(this.$global.baseUrl + '/conf/content/source', {}).then((res) => {
				if (res.data.code === '0000') {
					this.sourceConf = res.data.data;
				} else {
					this.$message.error(res.data.msg);
				}
				this.sourceLoading = false;
			}).catch((err) => {
				this.sourceLoading = false;
				this.$message.error(err.message);
			});
		},
		getTargetConfContent() {
			this.targetLoading = true;
			this.targetConf = {};

			this.$axios.get(this.$global.baseUrl + '/conf/content/target', {}).then((res) => {
				if (res.data.code === '0000') {
					this.targetConf = res.data.data;
				} else {
					this.$message.error(res.data.msg);
				}
				this.targetLoading = false;
			}).catch((err) => {
				this.targetLoading = false;
				this.$message.error(err.message);
			});
		},

		saveDecompose: function() {
			var params = "_fk=u&value=" + this.decompose;
			this.settingLoading = true;

			this.$axios.post(this.$global.baseUrl + '/conf/setting/decompose', params).then((res) => {
				if (res.data.code != '0000') {
					this.$message.error(res.data.msg);
				}
				this.settingLoading = false;
				this.refreshConf();
			}).catch((err) => {
				this.settingLoading = false;
				this.$message.error(err.message);
			});
		},

		checkNginxStatus() {
            this.statusLoading = true;
            this.$axios.get(this.$global.baseUrl + '/conf/nginx/status', {}).then((res) => {
                if (res.data.code === '0000') {
                    this.isNginxRun = true;
                } else {
                    this.isNginxRun = false;
                    this.$message.error(res.data.msg);
                }
                this.statusLoading = false;
            }).catch((err) => {
                this.statusLoading = false;
                this.$message.error(err.message);
            });
		},

		refreshConf: function() {
			this.getConfSetting();
			this.getSourceConfContent();
			this.getTargetConfContent();
			this.checkNginxStatus();
		},
		replaceConf: function() {
			this.$confirm('确定替换吗？', '提示', {type: 'warning'}).then(() => {
				this.loading = true;

				this.$axios.post(this.$global.baseUrl + '/conf/replace', "").then((res) => {
					if (res.data.code === '0000') {
						this.$message({type: "success", message: res.data.msg});
						this.getTargetConfContent();
					} else {
						this.$message.error(res.data.msg);
					}
					this.loading = false;
				}).catch((err) => {
					this.loading = false;
					this.$message.error(err.message);
				});
			}).catch(() => {

			});
		},
		restoreCallback: function(flag) {
			this.refreshConf();
		},
		checkConf: function() {
			this.loading = true;

			this.$axios.post(this.$global.baseUrl + '/conf/check').then((res) => {
				if (res.data.code === '0000') {
					this.$refs.message.show(res.data.data);
				} else {
					//this.$message.error(res.data.msg);
					this.$refs.message.show(res.data.msg);
				}
				this.loading = false;
			}).catch((err) => {
				this.loading = false;
				this.$message.error(err.message);
			});
		},
		reloadConf: function() {
			this.loading = true;

			this.$axios.post(this.$global.baseUrl + '/conf/reload').then((res) => {
				if (res.data.code === '0000') {
					this.$refs.message.show(res.data.data);
				} else {
					//this.$message.error(res.data.msg);
					this.$refs.message.show(res.data.msg);
				}
				this.loading = false;
			}).catch((err) => {
				this.loading = false;
				this.$message.error(err.message);
			});
		},
		toExecute: function() {
			this.$refs.exeNginx.show();
		},
		executeNginxCallback: function() {
			this.checkNginxStatus();
		},

		toChangeNginxExe: function() {
			this.$refs.changeNginxExe.show(this.nginxExe);
		},
		changeNginxExeCallback: function() {
			this.getConfSetting();
		}
	},
	mounted() {
		this.refreshConf();
	},
	destroyed() {

	}
}
</script>

<style lang='scss' scoped>
	.conf {
		margin: 10px 15px 100px 0;
		display: inline-block;
		vertical-align: top;
		width: 48%;
	}
	.conf .ct {
		padding-bottom: 5px;
	}
	.green {
		color: green;
	}
	.red {
		color: red;
	}
</style>
<style scoped>
	.source-conf >>> .el-textarea__inner {
		background-color: #ffffff;
	}
	.target-conf >>> .el-textarea__inner {
		background-color: #efefef;
	}
</style>
