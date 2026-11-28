<template>
	<!--自定义“另存为”：系统 File System Access API 不可用时的兼容方案，外观与功能对齐系统保存对话框-->
	<el-dialog v-model="visible" title="另存为" width="500px" :close-on-click-modal="true"
	           append-to-body draggable class="save-as-dialog" @opened="onOpened" @close="onClose">
		<div class="save-as-body">
			<div class="save-as-row">
				<span class="save-as-label">保存到：</span>
				<span class="save-as-dir" :class="{placeholder: !dirName}">{{ dirName || '未选择保存目录' }}</span>
				<el-button class="save-as-choose" link type="primary" @click="chooseRoot">浏览...</el-button>
			</div>
			<div class="save-as-row">
				<span class="save-as-label">文件名：</span>
				<el-input ref="nameInput" v-model="fileName" @keyup.enter="confirm" />
			</div>
			<div class="save-as-row">
				<span class="save-as-label">保存类型：</span>
				<el-select :model-value="ext" disabled style="width: 100%;">
					<el-option :label="typeLabel()" :value="ext" />
				</el-select>
			</div>
		</div>
		<template #footer>
			<el-button @click="close">取消</el-button>
			<el-button type="primary" @click="confirm">保存</el-button>
		</template>
		<!-- HTTP / Safari 等不支持 File System Access API 时的目录选择入口（只读，仅记录目录名） -->
		<input ref="dirInput" type="file" webkitdirectory class="save-dir-input" @change="onDirInputChange" />
	</el-dialog>
</template>

<script setup>
const {proxy} = getCurrentInstance();

const visible = ref(false);
const blob = ref(null);
const fileName = ref('');
const ext = ref('');
const nameInput = ref(null);
const dirInput = ref(null);
const supportDirectoryPicker = typeof window !== 'undefined' && typeof window.showDirectoryPicker === 'function';
// 保存目录：只可选择，不可输入；API 环境持有可写句柄，HTTP / Safari 仅记录目录名
const dirName = ref('');
const dirHandle = ref(null);
// 会话内记住上次选择的目录，作为之后弹框的默认选择
const lastDir = ref(null);  // {handle, name}

// 保存类型下拉的展示文案，如 “XLSX 文件 (*.xlsx)”
function typeLabel() {
	const e = ext.value;
	return e ? e.substring(1).toUpperCase() + ' 文件 (*' + e + ')' : '所有文件 (*.*)';
}

// 浏览保存目录：支持 File System Access API 时可写入；HTTP / Safari 退化为目录浏览（只读，仅记录目录名）
function chooseRoot() {
	if (supportDirectoryPicker) {
		pickRootWithApi();
		return;
	}
	const inst = dirInput.value;
	if (!inst) {
		proxy.$modal.msgWarning('当前浏览器或站点环境不支持选择保存目录');
		return;
	}
	inst.click();
}

// 支持 API 的环境：系统目录选择器，返回可写句柄
async function pickRootWithApi() {
	try {
		const handle = await window.showDirectoryPicker();
		dirHandle.value = handle;
		// 浏览器安全限制：FileSystemDirectoryHandle 只暴露目录名，不提供磁盘绝对路径
		dirName.value = handle.name || '';
		lastDir.value = {handle: handle, name: dirName.value};
		proxy.$modal.msgSuccess('已选择保存目录：' + dirName.value);
	} catch (err) {
		if (err && err.name === 'AbortError') {
			return; // 用户取消
		}
		proxy.$modal.msgWarning('无法选择目录：' + (err && err.message ? err.message : '未知错误'));
	}
}

// 退化环境：webkitdirectory 选择目录，解析目录名（只读，无法写入）
function onDirInputChange(e) {
	const input = e.target;
	const files = input && input.files;
	if (files && files.length) {
		const rel = files[0].webkitRelativePath || '';
		const root = rel ? rel.split('/')[0] : '';
		if (root) {
			dirHandle.value = null;
			dirName.value = root;
			lastDir.value = {handle: null, name: root};
			proxy.$modal.msgSuccess('已选择保存目录：' + root);
		}
	}
	if (input) {
		input.value = ''; // 允许再次选择同一目录
	}
}

// 打开弹框：传入待保存的 blob 与建议文件名（含后缀，来自后端 _filename）
function open(data, suggestedName) {
	const dot = suggestedName.lastIndexOf('.');
	ext.value = dot >= 0 ? suggestedName.substring(dot).toLowerCase() : '';
	blob.value = data;
	// 目录只可选择：默认恢复会话内上次选择的目录；首次使用为空
	const last = lastDir.value;
	if (last) {
		dirHandle.value = last.handle;
		dirName.value = last.name;
	} else {
		dirHandle.value = null;
		dirName.value = '';
	}
	// 与系统“另存为”一致：文件名框显示完整文件名（含后缀）
	fileName.value = suggestedName;
	visible.value = true;
}

// 弹框打开后聚焦，并默认只选中主文件名（不含后缀），与系统另存为行为一致
function onOpened() {
	nextTick(() => {
		const inst = nameInput.value;
		const input = inst && inst.$el ? inst.$el.querySelector('input') : null;
		if (input) {
			input.focus();
			const e = ext.value;
			const name = fileName.value || '';
			// 默认只选中主文件名，后缀保留（用户直接输入即可改名且不会丢格式）
			const end = e && name.toLowerCase().endsWith(e) ? name.length - e.length : name.length;
			if (input.setSelectionRange) {
				input.setSelectionRange(0, end);
			} else {
				input.select();
			}
		}
	});
}

function close() {
	visible.value = false;
	blob.value = null;
}

// 点击弹框外（遮罩）/ ESC / 右上角关闭时，清理暂存资源；lastDir 保留作为默认选择
function onClose() {
	blob.value = null;
	dirHandle.value = null;
	dirName.value = '';
}

// 确认保存：锁定后缀（不可改格式），优先写入所选目录，否则用浏览器默认下载
async function confirm() {
	let name = (fileName.value || '').trim();
	if (!name) {
		proxy.$modal.msgWarning('请输入文件名');
		return;
	}
	const e = ext.value;
	if (e && !name.toLowerCase().endsWith(e)) {
		name = name + e;
	}
	const data = blob.value;
	if (!data) {
		close();
		return;
	}
	// 保存目标：API 环境下写入所选目录；否则用默认下载
	const handle = dirHandle.value;
	if (handle) {
		try {
			if (typeof handle.getFileHandle !== 'function') {
				throw new Error('当前浏览器不支持写入所选目录');
			}
			const fileHandle = await handle.getFileHandle(name, {create: true});
			if (typeof fileHandle.createWritable !== 'function') {
				throw new Error('当前浏览器不支持写入所选目录');
			}
			const writable = await fileHandle.createWritable();
			await writable.write(data);
			await writable.close();
			proxy.$modal.msgSuccess('下载成功');
			close();
		} catch (err) {
			proxy.$modal.msgError('保存失败：' + (err && err.message ? err.message : '未知错误'));
			// 保留弹框，便于用户重新选择目录或改用默认下载位置
		}
		return;
	}
	proxy.fileDownload(data, name);
	proxy.$modal.msgSuccess('下载成功');
	close();
}

defineExpose({
	open,
	close
})
</script>

<style scoped>
/* 自定义“另存为”弹框：对齐系统保存对话框的紧凑表单布局 */
.save-as-body {
	padding: 4px 8px 8px 0;
}
.save-as-row {
	display: flex;
	align-items: center;
	margin-bottom: 16px;
}
.save-as-row:last-child {
	margin-bottom: 0;
}
.save-as-label {
	flex: 0 0 92px;
	text-align: right;
	padding-right: 12px;
	font-size: 14px;
	color: #606266;
	white-space: nowrap;
}
/* 保存目录名：只可选择，不可输入 */
.save-as-dir {
	flex: 1;
	min-width: 0;
	font-size: 13px;
	color: #606266;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.save-as-dir.placeholder {
	color: #c0c4cc;
}
.save-as-choose {
	flex: 0 0 auto;
	margin-left: 12px;
	font-size: 13px;
	padding: 0;
	height: auto;
}
.save-dir-input {
	display: none;
}
</style>
