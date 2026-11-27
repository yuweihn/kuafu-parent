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
					<el-button type="primary" v-on:click="getPermissionList()" :icon="Search" v-if="hasPerm(['sys.permission.list'])"> 查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="$refs.createPermission.show()" :icon="EditPen" v-if="hasPerm(['sys.permission.create'])"> 新增</el-button>
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
                <template #default="{row, $index}">
                    <svg-icon v-if="row.icon" :icon-class="row.icon" />
                </template>
            </el-table-column>
            <el-table-column prop="orderNum" label="排序" width="60" />
            <el-table-column prop="path" label="路由地址" width="180" :show-overflow-tooltip="true" />
            <el-table-column prop="component" label="组件路径" width="180" :show-overflow-tooltip="true" />
			<el-table-column prop="visible" label="状态" width="80">
                <template #default="{row, $index}">
                    <el-switch v-model="row.visible" :active-value="true" :inactive-value="false"
                            active-color="#13ce66" inactive-color="#ff4949" disabled/>
				</template>
			</el-table-column>
			<el-table-column prop="creator" label="创建人" width="90" />
			<el-table-column prop="createTime" label="创建时间" width="145" />
			<el-table-column prop="modifier" label="更新人" width="90" />
			<el-table-column prop="modifyTime" label="更新时间" width="145" />

			<el-table-column label="操作" width="100" fixed="right">
                <template #default="{row, $index}">
					<el-tooltip content="编辑" placement="top">
						<el-button text circle :icon="EditPen" @click="$refs.editPermission.show($index, row)" v-if="hasPerm(['sys.permission.update'])" />
					</el-tooltip>
                    <el-tooltip content="更多" placement="top">
                        <el-dropdown trigger="click" v-if="hasPerm(['sys.permission.delete'])">
                            <el-button text circle :icon="MoreFilled" />
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="handleDel($index, row)" v-if="hasPerm(['sys.permission.delete'])">
                                        <el-icon><Delete /></el-icon>
                                        <span>删除</span>
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </el-tooltip>
				</template>
			</el-table-column>
			<el-table-column label="" />
		</el-table>

		<!--工具条-->
		<el-col :span="24" class="toolbar2">
			<el-button type="danger" @click="batchRemove" :disabled="sels.length === 0" :icon="Delete" v-if="hasPerm(['sys.permission.delete'])"> 批量删除</el-button>
            <el-button type="success" v-on:click="doExport" :icon="Coin" v-if="hasPerm(['sys.permission.export'])"> 导出</el-button>
            <el-button type="danger" v-on:click="doImport" :icon="CirclePlus" v-if="hasPerm(['sys.permission.import'])"> 导入</el-button>
		</el-col>

		<!--新增界面-->
		<create-permission ref="createPermission" v-on:success="getPermissionList"/>

		<!--编辑界面-->
		<edit-permission ref="editPermission" v-on:success="getPermissionList"/>

        <file-upload ref="importData" :title="'导入外部数据'" :fileLabel="'文件'" :fileTips="'只接受*.json格式，最大不要超过2MB'"
                  :accept="'.json'" :maxSize="2097152" :fileType="'text'"
                  :actionUrl="'/sys/permission/import'" v-on:change="onImportPost" />

	</section>
</template>

<script setup>
import CreatePermission from './components/CreatePermission';
import EditPermission from './components/EditPermission';
import {Search, EditPen, MoreFilled, Coin, CirclePlus, Delete} from '@element-plus/icons-vue';

const {proxy} = getCurrentInstance();
const filters = ref({
    keywords: null,
    visible: null
});
const visibleList = ref([
    {
        "val": true,
        "label": "显示"
    },
    {
        "val": false,
        "label": "隐藏"
    }
]);
const permissionList = ref([]);
const listLoading = ref(false);
const sels = ref([]);
const supportSavePicker = typeof window !== 'undefined' && typeof window.showSaveFilePicker === 'function';

function onSelsChanged(v) {
    sels.value = v;
}
function getPermissionList() {
    let params = {
        keywords: filters.value.keywords ? filters.value.keywords : null,
        visible: filters.value.visible != null && filters.value.visible !== '' ? filters.value.visible : null
    };
    listLoading.value = true;
    proxy.request.get('/sys/permission/list', params).then((res) => {
        permissionList.value = res.data.data;
        listLoading.value = false;
    }).catch((err) => {
        permissionList.value = [];
        listLoading.value = false;
    });
}
function handleDel(index, row) {
    sels.value = [];
    sels.value.push(row);
    batchRemove();
}
function batchRemove() {
    var ids = sels.value.map(item => item.id).toString();
    proxy.$modal.confirm('确定删除选中记录吗?', '提示', {type: 'warning'}).then(function() {
        listLoading.value = true;
        proxy.request.delete('/sys/permission/delete', {ids: ids}).then((res) => {
            proxy.$modal.msgSuccess(res.data.msg);
            proxy.removeDynamicLoaded();
            listLoading.value = false;
            getPermissionList();
        }).catch((err) => {
            listLoading.value = false;
        });
    }).catch((err) => {});
    sels.value = [];
}
// 把 blob 写入系统“另存为”对话框选中的文件（File System Access API）
async function writeBlobToFileHandle(fileHandle, blob) {
    try {
        const writable = await fileHandle.createWritable();
        await writable.write(blob);
        await writable.close();
        proxy.$modal.msgSuccess('下载成功');
    } catch (err) {
        proxy.$modal.msgError('保存失败：' + (err && err.message ? err.message : '未知错误'));
    }
}

// “激活已过期”时暂存下载结果，等用户再次点击时在手势内同步弹系统另存为
const pendingSave = ref(null);

// 在用户手势内同步调用系统“另存为”，保证弹框不受请求耗时影响
function openSavePicker(blob, suggestedName) {
    const options = {suggestedName: suggestedName};
    const dot = suggestedName.lastIndexOf('.');
    const ext = dot >= 0 ? suggestedName.substring(dot).toLowerCase() : '';
    if (ext && ext.length > 1) {
        // 系统层锁定文件格式：只允许同名后缀
        options.types = [{description: ext + ' 文件', accept: {'application/octet-stream': [ext]}}];
    }
    window.showSaveFilePicker(options).then((fileHandle) => {
        // 代码层兜底：用户若强行改了后缀则中止
        if (ext && !fileHandle.name.toLowerCase().endsWith(ext)) {
            proxy.$modal.msgError('不能修改文件格式，请保留 ' + ext + ' 后缀');
            return;
        }
        writeBlobToFileHandle(fileHandle, blob);
    }).catch((err) => {
        if (err && err.name === 'AbortError') {
            return; // 用户取消
        }
        if (err && err.name === 'SecurityError') {
            pendingSave.value = {blob: blob, name: suggestedName};
            proxy.$modal.msgWarning('文件已准备好，请再点一次下载以选择保存位置');
            return;
        }
        proxy.fileDownload(blob, suggestedName);
    });
}

// 请求返回后调用：激活仍有效则直接弹；已过期则暂存并提示，不再静默下载
function saveBlobWithPicker(blob, suggestedName) {
    if (typeof navigator !== 'undefined' && navigator.userActivation && !navigator.userActivation.isActive) {
        pendingSave.value = {blob: blob, name: suggestedName};
        proxy.$modal.msgWarning('文件已准备好，请再点一次下载以选择保存位置');
        return;
    }
    openSavePicker(blob, suggestedName);
}

// 二次点击时调用：若存在暂存结果，在此次手势内同步弹 picker 并返回 true
function consumePendingSave() {
    if (!pendingSave.value) {
        return false;
    }
    const p = pendingSave.value;
    pendingSave.value = null;
    openSavePicker(p.blob, p.name);
    return true;
}


function doExport() {
    if (consumePendingSave()) {
        return;
    }
    requestExport();
}

function requestExport() {
    listLoading.value = true;
    proxy.request.post('/sys/permission/export', null, {responseType: 'blob'}).then((res) => {
        if (res.headers["_filename"] || (res.headers["content-type"] && res.headers["content-type"].indexOf("application/octet-stream") === 0)) {
            const blob = res.data;
            const fileName = res.headers["_filename"];
            if (fileName) {
                const name = decodeURIComponent(fileName);
                if (supportSavePicker) {
                    // 拿到真实文件名后立即弹系统“另存为”，一步完成改名+选目录
                    saveBlobWithPicker(blob, name);
                } else {
                    proxy.fileDownload(blob, name);
                }
            } else {
                proxy.$modal.msgError("Error");
            }
        } else if (res.headers["content-type"] && res.headers["content-type"].indexOf("application/json") === 0) {
            const reader = new FileReader();
            reader.onload = function() {
                var dt = JSON.parse(reader.result);
                if (dt.code === proxy.errorCode.success.code) {
                    proxy.$modal.msgSuccess(dt.msg);
                } else {
                    proxy.$modal.msgError(dt.msg);
                }
            }
            reader.readAsText(res.data, 'utf-8');
        } else {
            proxy.$modal.msgError("Invalid Content Type!!");
        }
        listLoading.value = false;
    }).catch((err) => {
        listLoading.value = false;
    });
}
function doImport() {
    proxy.$refs.importData.show("hehe");
}
function onImportPost(key, res) {
    proxy.removeDynamicLoaded();
    proxy.$modal.msgSuccess(res.data.msg);
    getPermissionList();
}

onMounted(() => {
    getPermissionList();
})
</script>

<style scoped>

</style>

