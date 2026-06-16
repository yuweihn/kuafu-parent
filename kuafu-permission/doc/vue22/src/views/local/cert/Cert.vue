<template>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true">
				<el-form-item>
					<el-button type="primary" v-on:click="getCertList()" class="el-icon-refresh" v-hasPerm="['cert.list']"> 刷新</el-button>
				</el-form-item>
			</el-form>
		</el-col>

        <div class="tree-container" v-loading="loading">
            <el-tree :data="certFileList" node-key="path" style="width: 100%;" :default-expanded-keys="expandedKeys"
                        :expand-on-click-node="false" @node-expand="handleCertNodeExpand" @node-collapse="handleCertNodeCollapse">
                <span class="tree-node" slot-scope="{node, data}">
                    <span>{{node.label}}</span>
                    <span class="last-update-time">{{data.lastUpdateTime}}</span>
                    <span>
                        <el-button type="text" @click="() => addSubDir(node)" class="el-icon-plus" v-if="data.isDir === true" v-hasPerm="['cert.dir.create']"> 增加子目录</el-button>
                        <el-button type="text" @click="() => toUploadFile(node)" class="el-icon-upload" v-if="data.isDir === true" v-hasPerm="['cert.upload']"> 上传文件</el-button>
                        <el-button type="text" v-if="data.isDir === false" @click="() => downloadFile(node)" class="el-icon-download" v-hasPerm="['cert.download']"> 下载</el-button>
                        <el-button type="text" @click="() => remove(node)" class="el-icon-delete" v-hasPerm="['cert.delete']"> 删除</el-button>
                    </span>
                </span>
            </el-tree>
        </div>

		<create-sub-dir ref="createSubDir" v-on:success="afterCreateSubDir"/>
        <file-upload ref="uploadCert" :title="'上传证书'" :fileLabel="'文件'" :fileTips="'请选择证书，文件不要超过2MB'"
                  :accept="''" :maxSize="2097152" :fileType="'text'"
                  :actionUrl="this.$global.baseUrl + '/cert/upload'" v-on:change="onUploadChanged" />
	</section>
</template>

<script>
import CreateSubDir from './components/CreateSubDir';
import FileUpload from '@/views/components/SingleFileUpload';

export default {
    components: {
        'create-sub-dir': CreateSubDir,
        'file-upload': FileUpload
    },

    data() {
        return {
            certFileList: [],
            expandedKeys: [],
            loading: false
        }
    },
    methods: {
        getCertList() {
            this.certFileList = [];
            this.loading = true;
            var this0 = this;

            this0.$axios.get(this0.$global.baseUrl + '/cert/list', {}).then((res) => {
                if (res.data.code === '0000') {
                    this0.certFileList = res.data.data;
                } else {
                    this0.$message.error(res.data.msg);
                }
                this0.loading = false;
            }).catch((err) => {
                this0.loading = false;
                this0.$message.error(err.message);
            });
        },

        handleCertNodeExpand(data, node) {
            this.expandedKeys = [node.key];
        },
        handleCertNodeCollapse(data, node) {
            var parentNode = node.parent;
            if (parentNode && parentNode.key) {
                this.expandedKeys = [parentNode.key];
            } else {
                this.expandedKeys = [];
            }
        },
        addSubDir(node) {
            this.$refs.createSubDir.show(node.key);
        },
        afterCreateSubDir(code, curDir) {
            this.getCertList();
            this.expandedKeys = curDir ? [curDir] : null;
        },

        toUploadFile(node) {
            var params = {"fullDir": node.key};
            this.$refs.uploadCert.show(node.key, params);
        },
        onUploadChanged(key, res) {
            if (res.data.code === '0000') {
                this.getCertList();
                this.expandedKeys = key ? [key] : null;
            } else {
                this.$message.error(res.data.msg);
            }
        },

        //下载
        downloadFile: function(node) {
            var params = "path=" + node.key;
            var this0 = this;
            this0.loading = true;
            this0.$axios.post(this0.$global.baseUrl + '/cert/download', params, {responseType: 'blob'}).then((res) => {
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
                this0.loading = false;
            }).catch((err) => {
                this0.loading = false;
                this0.$message.error(err.message);
            });
        },

        remove(node) {
            var paths = node.key;
            this.$confirm('确定删除“' + paths + '”吗？', '提示', {type: 'warning'}).then(() => {
                var params = {paths: paths};
                this.loading = true;

                this.$axios.delete(this.$global.baseUrl + '/cert/delete', {params: params}).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.loading = false;
                    this.getCertList();
                    this.expandedKeys = node.parent ? [node.parent.key] : null;
                }).catch((err) => {
                    this.loading = false;
                    this.$message.error(err.message);
                });
            }).catch(() => {

            });
        }
    },
    mounted() {
        this.getCertList();
    }
}
</script>

<style scoped>
.tree-container {
    display: flex;
    padding-left: 1px;
}
.tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-right: 30px;
}
.last-update-time {
    color: #aaaaaa;
    margin-left: auto;
    margin-right: 20px;
    text-align: right;
    flex: 1;
}
</style>

