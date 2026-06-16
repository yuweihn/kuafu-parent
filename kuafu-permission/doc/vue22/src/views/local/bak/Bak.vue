<template>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true">
				<el-form-item v-hasPerm="['back.list']">
					<el-button type="primary" v-on:click="getBakConfList()" class="el-icon-refresh"> 刷新</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['back.do']">
					<el-button type="success" v-on:click="doBak()" class="el-icon-coin"> 备份</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['back.restart']">
					<el-button type="warning" v-on:click="restart()" class="el-icon-sunny"> 重启应用</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['back.import.db']">
					<el-button type="danger" v-on:click="importDB()" class="el-icon-circle-plus-outline"> 导入数据库</el-button>
				</el-form-item>
				<el-form-item v-hasPerm="['back.resume.setting']">
					<el-button type="danger" v-on:click="resumeSetting()" class="el-icon-d-arrow-left"> 恢复出厂设置</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="bakList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="路径：">
							<span>{{scope.row.path}}</span>
						</el-form-item>
						<el-form-item label="创建时间：">
							<span>{{scope.row.createTime}}</span>
						</el-form-item>
					</el-form>
				</template>
			</el-table-column>
			<el-table-column type="selection" width="30px" />
			<el-table-column prop="path" label="路径" width="350px" :show-overflow-tooltip="true" />
			<el-table-column prop="createTime" label="创建时间" width="150px" />
			<el-table-column label="操作" width="100">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">查看内容</div>
						<el-button type="text" circle class="el-icon-view" @click="$refs.viewConf.show(scope.row.path)" v-hasPerm="['back.content']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['back.restore', 'back.download', 'back.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="restoreConf(scope.row)" v-hasPerm="['back.restore']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">还原</div>
									<el-button type="text" circle class="el-icon-refresh-left"> 还原</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="downloadConf(scope.row)" v-hasPerm="['back.download']">
							<el-dropdown-item>
								<el-tooltip placement="top">
                                    <div slot="content">下载</div>
                                    <el-button type="text" circle class="el-icon-download"> 下载</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['back.delete']">
							<el-dropdown-item divided>
								<el-tooltip placement="top">
									<div slot="content">删除</div>
									<el-button type="text" circle class="el-icon-delete"> 删除</el-button>
								</el-tooltip>
							</el-dropdown-item>
							</div>
						</el-dropdown-menu>
					</el-dropdown>
				</template>
			</el-table-column>
			<el-table-column label="" />
		</el-table>

		<!--工具条-->
		<el-col :span="24" class="toolbar2">
			<el-button type="warning" @click="batchRemove" :disabled="this.sels.length === 0" class="el-icon-delete" v-hasPerm="['back.delete']"> 批量删除</el-button>
			<el-button type="danger" @click="removeAll" class="el-icon-delete" v-hasPerm="['back.all.delete']"> 全部删除</el-button>
		</el-col>

		<!--查看内容-->
		<view-conf ref="viewConf"/>

        <file-upload ref="importDb" :title="'导入数据库'" :fileLabel="'文件'" :fileTips="'只接受*.db格式，最大不要超过2MB'"
                :accept="'.db'" :maxSize="2097152"
                :actionUrl="this.$global.baseUrl + '/back/import'" v-on:change="onImportPost" />
	</section>
</template>

<script>
import ViewConf from "./ViewConf";
import FileUpload from '@/views/components/SingleFileUpload';
export default {
    components: {
        'view-conf': ViewConf,
        'file-upload': FileUpload
    },

    data() {
        return {
            bakList: [],
            listLoading: false,
            sels: []    //列表选中列
        }
    },
    methods: {
        onSelsChanged: function(sels) {
            this.sels = sels;
        },
        getBakConfList() {
            this.listLoading = true;
            this.$axios.get(this.$global.baseUrl + '/back/list', {}).then((res) => {
                if (res.data.code === '0000') {
                    this.bakList = res.data.data;
                } else {
                    this.bakList = [];
                    this.$message.error(res.data.msg);
                }
                this.listLoading = false;
            }).catch((err) => {
                this.listLoading = false;
                this.$message.error(err.message);
            });
        },

        doBak: function() {
            this.$confirm('备份确认！', '提示', {type: 'warning'}).then(() => {
                this.listLoading = true;
                this.$axios.post(this.$global.baseUrl + '/back/do', null).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getBakConfList();
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
            }).catch(() => {

            });
        },

        restart: function() {
            this.$confirm('确定重启应用吗？', '提示', {type: 'warning'}).then(() => {
                this.listLoading = true;
                this.$axios.post(this.$global.baseUrl + '/back/restart', null).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
            }).catch(() => {

            });
        },

        importDB: function() {
            /**
            this.$confirm('确定导入数据库吗？', '提示', {type: 'warning'}).then(() => {
                this.$refs.importDb.show("hehe");
            }).catch(() => {

            });
            **/
            this.$refs.importDb.show("hehe");
        },
        onImportPost(key, res) {
            if (res.data.code === '0000') {
                this.$message({type: "success", message: res.data.msg});
            } else {
                this.$message.error(res.data.msg);
            }
        },

        resumeSetting: function() {
            this.$confirm('确定恢复出厂设置吗？', '提示', {type: 'warning'}).then(() => {
                this.listLoading = true;
                this.$axios.post(this.$global.baseUrl + '/back/resume', null).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
            }).catch(() => {

            });
        },

        //还原
        restoreConf: function(row) {
            this.$confirm('当前配置会被覆盖，确定还原吗？', '提示', {type: 'warning'}).then(() => {
                var params = "path=" + row.path;
                this.listLoading = true;
                this.$axios.post(this.$global.baseUrl + '/back/restore', params).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getBakConfList();
                    this.$emit("callback", 1);
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
            }).catch(() => {

            });
        },

        //下载
        downloadConf: function(row) {
            var params = "path=" + row.path;
            var this0 = this;
            this0.listLoading = true;
            this0.$axios.post(this0.$global.baseUrl + '/back/download', params, {responseType: 'blob'}).then((res) => {
                if (res.data.type === 'application/octet-stream') {
                    const fileName = res.headers["_filename"];
                    if (fileName) {
                        this0.$fileDownload(res.data, decodeURIComponent(fileName));
                    } else {
                        this0.$message.error("Error");
                    }
                } else if (res.data.type === 'application/json') {
                    const reader = new FileReader();
                    reader.onload = function() {
                        var dt = JSON.parse(reader.result);
                        if (dt.code === '0000') {
                            this0.$message({type: "success", message: dt.msg});
                        } else {
                            this0.$message.error(dt.msg);
                        }
                    }
                    reader.readAsText(res.data, 'utf-8');
                } else {
                    this0.$message.error("Invalid Content Type!!");
                }
                this0.listLoading = false;
            }).catch((err) => {
                this0.listLoading = false;
                this0.$message.error(err.message);
            });
        },

        //删除
        handleDel: function(index, row) {
            this.sels = [];
            this.sels.push(row);
            this.batchRemove();
        },
        //批量删除
        batchRemove: function() {
            var paths = this.sels.map(item => item.path).toString();
            this.$confirm('确定删除选中记录吗？', '提示', {type: 'warning'}).then(() => {
                var params = {paths: paths};
                this.listLoading = true;
                this.$axios.delete(this.$global.baseUrl + '/back/delete', {params: params}).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getBakConfList();
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
            }).catch(() => {

            });
            this.sels = [];
        },
        //全部删除
        removeAll: function() {
            this.$confirm('确定删除全部记录吗？', '提示', {type: 'warning'}).then(() => {
                this.listLoading = true;
                this.$axios.delete(this.$global.baseUrl + '/back/all/delete', {}).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.listLoading = false;
                    this.getBakConfList();
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
            }).catch(() => {

            });
            this.sels = [];
        }
    },
    mounted() {
  this.getBakConfList();
    }
}
</script>

<style scoped>
.table-expand {
    font-size: 0;
}
.table-expand >>> label {
    color: #99a9bf;
}
.table-expand .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 90%;
}
</style>

