<template>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true">
				<el-form-item label="关键字：">
					<el-input v-model="filters.keywords" clearable placeholder="" style="width: 255px;" />
				</el-form-item>
                <el-form-item label="状态：">
                    <el-select v-model="filters.visible" placeholder="" clearable>
                        <el-option v-for="item in visibleList" :key="item.val" :label="item.label" :value="item.val" />
                    </el-select>
                </el-form-item>
				<el-form-item>
					<el-button type="primary" v-on:click="getPermissionList()" class="el-icon-search" v-hasPerm="['sys.permission.list']"> 查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="$refs.createPermission.show()" class="el-icon-edit" v-hasPerm="['sys.permission.create']"> 新增</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="permissionList" row-key="id" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged"
		            style="width: 100%;" :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
			<el-table-column type="selection" width="30" />
			<el-table-column prop="id" label="ID" width="120" :show-overflow-tooltip="true" />
			<el-table-column prop="permNo" label="权限编码" width="150" :show-overflow-tooltip="true" />
			<el-table-column prop="title" label="标题" width="150" :show-overflow-tooltip="true" />
            <el-table-column prop="icon" label="图标" align="center" width="100">
                <template slot-scope="scope">
                    <svg-icon v-if="scope.row.icon" :icon-class="scope.row.icon" />
                </template>
            </el-table-column>
            <el-table-column prop="orderNum" label="排序" width="60" />
            <el-table-column prop="path" label="路由地址" width="180" :show-overflow-tooltip="true" />
            <el-table-column prop="component" label="组件路径" width="180" :show-overflow-tooltip="true" />
			<el-table-column prop="visible" label="状态" width="80">
				<template slot-scope="scope">
                    <el-switch v-model="scope.row.visible" :active-value="true" :inactive-value="false"
                            active-color="#13ce66" inactive-color="#ff4949" disabled/>
				</template>
			</el-table-column>
			<el-table-column prop="creator" label="创建人" width="90" />
			<el-table-column prop="createTime" label="创建时间" width="145" />
			<el-table-column prop="modifier" label="更新人" width="90" />
			<el-table-column prop="modifyTime" label="更新时间" width="145" />

			<el-table-column label="操作" width="100" fixed="right">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit" @click="$refs.editPermission.show(scope.$index, scope.row)" v-hasPerm="['sys.permission.update']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['sys.permission.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['sys.permission.delete']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">删除</div>
									<el-button type="text" circle class="el-icon-delete" v-hasPerm="['sys.permission.delete']"> 删除</el-button>
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
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length === 0" class="el-icon-delete" v-hasPerm="['sys.permission.delete']"> 批量删除</el-button>
            <el-button type="success" v-on:click="doExport" class="el-icon-coin" v-hasPerm="['sys.permission.export']"> 导出</el-button>
            <el-button type="danger" v-on:click="doImport" class="el-icon-circle-plus-outline" v-hasPerm="['sys.permission.import']"> 导入</el-button>
		</el-col>

		<!--新增界面-->
		<create-permission ref="createPermission" v-on:success="getPermissionList"/>

		<!--编辑界面-->
		<edit-permission ref="editPermission" v-on:success="getPermissionList"/>

        <file-upload ref="importData" :title="'导入外部数据'" :fileLabel="'文件'" :fileTips="'只接受*.json格式，最大不要超过2MB'"
                  :accept="'.json'" :maxSize="2097152" :fileType="'text'"
                  :actionUrl="this.$global.baseUrl + '/sys/permission/import'" v-on:change="onImportPost" />

        <!--自定义“另存为”弹框（通用组件）：系统另存为不可用时的兼容方案-->
        <save-as-dialog ref="saveAsDialog" />

	</section>
</template>

<script>
import CreatePermission from './components/CreatePermission';
import EditPermission from './components/EditPermission';
import FileUpload from '@/views/components/SingleFileUpload';

export default {
    components: {
        'create-permission': CreatePermission,
        'edit-permission': EditPermission,
        'file-upload': FileUpload
    },

    data() {
        return {
            filters: {
                keywords: null,
                visible: null
            },

            visibleList: [
                {
                    "val": true,
                    "label": "显示"
                },
                {
                    "val": false,
                    "label": "隐藏"
                }
            ],

            permissionList: [],
            listLoading: false,
            sels: [],    //列表选中列
            supportSavePicker: typeof window !== 'undefined' && typeof window.showSaveFilePicker === 'function',
            pendingSave: null,
            // Safari（含 iOS）没有 File System Access API，且自带下载授权/保存弹框；
            // 若再弹自定义弹框会出现“自定义框 → 系统框”两段式，故 Safari 直接走默认下载
            isSafari: typeof navigator !== 'undefined' && /^((?!chrome|crios|fxios|edg|android).)*safari/i.test(navigator.userAgent),
            // Firefox 桌面版默认开启“每次下载都询问保存位置”，同样会自带系统保存弹框，与 Safari 同理直接走默认下载
            isFirefoxDesktop: typeof navigator !== 'undefined' && /firefox/i.test(navigator.userAgent) && !/android|mobile/i.test(navigator.userAgent)
        }
    },
    methods: {
        onSelsChanged: function(sels) {
            this.sels = sels;
        },
        getPermissionList() {
            var params = {
                keywords: this.filters.keywords ? this.filters.keywords : null,
                visible: this.filters.visible !== '' ? this.filters.visible : null
            };
            this.listLoading = true;
            this.$axios.get(this.$global.baseUrl + '/sys/permission/list', {params: params}).then((res) => {
                if (res.data.code === '0000') {
                    this.permissionList = res.data.data;
                } else {
                    this.permissionList = [];
                    this.$message.error(res.data.msg);
                }
                this.listLoading = false;
            }).catch((err) => {
                this.listLoading = false;
                this.$message.error(err.message);
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
            var ids = this.sels.map(item => item.id).toString();
            this.$confirm('确定删除选中记录吗？', '提示', {type: 'warning'}).then(() => {
                var params = {ids: ids};
                this.listLoading = true;
                this.$axios.delete(this.$global.baseUrl + '/sys/permission/delete', {params: params}).then((res) => {
                    if (res.data.code === '0000') {
                        this.$message({type: "success", message: res.data.msg});
                    } else {
                        this.$message.error(res.data.msg);
                    }
                    this.removeDynamicLoaded();
                    this.listLoading = false;
                    this.getPermissionList();
                }).catch((err) => {
                    this.listLoading = false;
                    this.$message.error(err.message);
                });
            }).catch(() => {

            });
            this.sels = [];
        },

        // 把 blob 写入系统“另存为”对话框选中的文件（File System Access API）
        writeBlobToFileHandle(fileHandle, blob) {
            return fileHandle.createWritable().then((writable) => {
                return writable.write(blob).then(() => writable.close());
            }).then(() => {
                this.$message({type: 'success', message: '下载成功'});
            }).catch((err) => {
                this.$message.error('保存失败：' + (err && err.message ? err.message : '未知错误'));
            });
        },

        // 在用户手势内同步调用系统“另存为”，保证弹框不受请求耗时影响
        openSavePicker(blob, suggestedName) {
            var options = {suggestedName: suggestedName};
            var dot = suggestedName.lastIndexOf('.');
            var ext = dot >= 0 ? suggestedName.substring(dot).toLowerCase() : '';
            if (ext && ext.length > 1) {
                // 系统层锁定文件格式：只允许同名后缀
                options.types = [{description: ext + ' 文件', accept: {'application/octet-stream': [ext]}}];
            }
            var this0 = this;
            window.showSaveFilePicker(options).then((fileHandle) => {
                // 代码层兜底：用户若强行改了后缀则中止
                if (ext && fileHandle.name.toLowerCase().endsWith(ext) === false) {
                    this0.$message.error('不能修改文件格式，请保留 ' + ext + ' 后缀');
                    return;
                }
                return this0.writeBlobToFileHandle(fileHandle, blob);
            }).catch((err) => {
                if (err && err.name === 'AbortError') {
                    return; // 用户取消
                }
                if (err && err.name === 'SecurityError') {
                    this0.pendingSave = {blob: blob, name: suggestedName};
                    this0.$message.warning('文件已准备好，请再点一次下载以选择保存位置');
                    return;
                }
                this0.$refs.saveAsDialog.open(blob, suggestedName);
            });
        },

        // 请求返回后调用：激活仍有效则直接弹；已过期则暂存并提示，不再静默下载
        saveBlobWithPicker(blob, suggestedName) {
            if (typeof navigator !== 'undefined' && navigator.userActivation && !navigator.userActivation.isActive) {
                this.pendingSave = {blob: blob, name: suggestedName};
                this.$message.warning('文件已准备好，请再点一次下载以选择保存位置');
                return;
            }
            this.openSavePicker(blob, suggestedName);
        },

        // 二次点击时调用：若存在暂存结果，在此次手势内同步弹 picker 并返回 true
        consumePendingSave() {
            if (!this.pendingSave) {
                return false;
            }
            var p = this.pendingSave;
            this.pendingSave = null;
            this.openSavePicker(p.blob, p.name);
            return true;
        },

        doExport() {
            if (this.consumePendingSave()) {
                return;
            }
            this.listLoading = true;
            this.$axios.post(this.$global.baseUrl + '/sys/permission/export', null, {responseType: 'blob'}).then((res) => {
                if (res.headers["_filename"] || (res.headers["content-type"] && res.headers["content-type"].indexOf("application/octet-stream") === 0)) {
                    const fileName = res.headers["_filename"];
                    if (fileName) {
                        var name = decodeURIComponent(fileName);
                        if (this.supportSavePicker) {
                            // 拿到真实文件名后立即弹系统“另存为”，一步完成改名+选目录
                            this.saveBlobWithPicker(res.data, name);
                        } else if (this.isSafari || this.isFirefoxDesktop) {
                            // Safari / 桌面 Firefox 自带下载保存弹框，直接默认下载避免双弹框
                            this.$fileDownload(res.data, name);
                        } else {
                            // 其余（HTTP + Chrome/Firefox 移动端等）用自定义兼容弹框
                            this.$refs.saveAsDialog.open(res.data, name);
                        }
                    } else {
                        this.$message.error("Error");
                    }
                } else if (res.headers["content-type"] && res.headers["content-type"].indexOf("application/json") === 0) {
                    const reader = new FileReader();
                    reader.onload = () => {
                        var dt = JSON.parse(reader.result);
                        if (dt.code === '0000') {
                            this.$message({type: "success", message: dt.msg});
                        } else {
                            this.$message.error(dt.msg);
                        }
                    }
                    reader.readAsText(res.data, 'utf-8');
                } else {
                    this.$message.error("Invalid Content Type!!");
                }
                this.listLoading = false;
            }).catch((err) => {
                this.listLoading = false;
                this.$message.error(err.message);
            });
        },

        doImport: function() {
            this.$refs.importData.show("hehe");
        },
        onImportPost(key, res) {
            if (res.data.code === '0000') {
                this.removeDynamicLoaded();
                this.$message({type: "success", message: res.data.msg});
                this.getPermissionList();
            } else {
                this.$message.error(res.data.msg);
            }
        }
    },
    mounted() {
        this.getPermissionList();
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

