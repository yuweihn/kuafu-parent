<template>
	<!--自定义“另存为”：系统 File System Access API 不可用时的兼容方案，外观与功能对齐系统保存对话框-->
	<el-dialog :visible.sync="visible" title="另存为" width="500px" :close-on-click-modal="true"
	           append-to-body v-drag class="save-as-dialog" @opened="onOpened" @close="onClose">
		<div class="save-as-body">
			<div class="save-as-row">
				<span class="save-as-label">保存到：</span>
				<span class="save-as-dir" :class="{placeholder: !dirName}">{{ dirName || '未选择保存目录' }}</span>
				<el-button class="save-as-choose" type="text" @click.native="chooseRoot">浏览...</el-button>
			</div>
			<div class="save-as-row">
				<span class="save-as-label">文件名：</span>
				<el-input ref="nameInput" v-model="fileName" @keyup.enter.native="confirm" />
			</div>
			<div class="save-as-row">
				<span class="save-as-label">保存类型：</span>
				<el-select :value="ext" disabled style="width: 100%;">
					<el-option :label="typeLabel()" :value="ext" />
				</el-select>
			</div>
		</div>
		<div slot="footer">
			<el-button @click.native="close">取消</el-button>
			<el-button type="primary" @click.native="confirm">保存</el-button>
		</div>
		<!-- HTTP / Safari 等不支持 File System Access API 时的目录选择入口（只读，仅记录目录名） -->
		<input ref="dirInput" type="file" webkitdirectory class="save-dir-input" @change="onDirInputChange" />
	</el-dialog>
</template>

<script>
export default {
    name: 'SaveAsDialog',

    data() {
        return {
            visible: false,
            blob: null,
            fileName: '',
            ext: '',
            supportDirectoryPicker: typeof window !== 'undefined' && typeof window.showDirectoryPicker === 'function',
            // 保存目录：只可选择，不可输入；API 环境持有可写句柄，HTTP / Safari 仅记录目录名
            dirName: '',
            dirHandle: null,
            // 会话内记住上次选择的目录，作为之后弹框的默认选择
            lastDir: null  // {handle, name}
        }
    },

    methods: {
        // 保存类型下拉的展示文案，如 “XLSX 文件 (*.xlsx)”
        typeLabel() {
            var e = this.ext;
            return e ? e.substring(1).toUpperCase() + ' 文件 (*' + e + ')' : '所有文件 (*.*)';
        },

        // 浏览保存目录：支持 File System Access API 时可写入；HTTP / Safari 退化为目录浏览（只读，仅记录目录名）
        chooseRoot() {
            if (this.supportDirectoryPicker) {
                this.pickRootWithApi();
                return;
            }
            var inst = this.$refs.dirInput;
            if (!inst) {
                this.$message.warning('当前浏览器或站点环境不支持选择保存目录');
                return;
            }
            inst.click();
        },

        // 支持 API 的环境：系统目录选择器，返回可写句柄
        pickRootWithApi() {
            var this0 = this;
            window.showDirectoryPicker().then((handle) => {
                this0.dirHandle = handle;
                // 浏览器安全限制：FileSystemDirectoryHandle 只暴露目录名，不提供磁盘绝对路径
                this0.dirName = handle.name || '';
                this0.lastDir = {handle: handle, name: this0.dirName};
                this0.$message({type: 'success', message: '已选择保存目录：' + this0.dirName});
            }).catch((err) => {
                if (err && err.name === 'AbortError') {
                    return; // 用户取消
                }
                this0.$message.warning('无法选择目录：' + (err && err.message ? err.message : '未知错误'));
            });
        },

        // 退化环境：webkitdirectory 选择目录，解析目录名（只读，无法写入）
        onDirInputChange(e) {
            var this0 = this;
            var input = e.target;
            var files = input && input.files;
            if (files && files.length) {
                var rel = files[0].webkitRelativePath || '';
                var root = rel ? rel.split('/')[0] : '';
                if (root) {
                    this0.dirHandle = null;
                    this0.dirName = root;
                    this0.lastDir = {handle: null, name: root};
                    this0.$message({type: 'success', message: '已选择保存目录：' + root});
                }
            }
            if (input) {
                input.value = ''; // 允许再次选择同一目录
            }
        },

        // 打开弹框：传入待保存的 blob 与建议文件名（含后缀，来自后端 _filename）
        open(data, suggestedName) {
            var dot = suggestedName.lastIndexOf('.');
            this.ext = dot >= 0 ? suggestedName.substring(dot).toLowerCase() : '';
            this.blob = data;
            // 目录只可选择：默认恢复会话内上次选择的目录；首次使用为空
            var last = this.lastDir;
            if (last) {
                this.dirHandle = last.handle;
                this.dirName = last.name;
            } else {
                this.dirHandle = null;
                this.dirName = '';
            }
            // 与系统“另存为”一致：文件名框显示完整文件名（含后缀）
            this.fileName = suggestedName;
            this.visible = true;
        },

        // 弹框打开后聚焦，并默认只选中主文件名（不含后缀），与系统另存为行为一致
        onOpened() {
            var this0 = this;
            this0.$nextTick(() => {
                var inst = this0.$refs.nameInput;
                var input = inst && inst.$el ? inst.$el.querySelector('input') : null;
                if (input) {
                    input.focus();
                    var e = this0.ext;
                    var name = this0.fileName || '';
                    // 默认只选中主文件名，后缀保留（用户直接输入即可改名且不会丢格式）
                    var end = e && name.toLowerCase().endsWith(e) ? name.length - e.length : name.length;
                    if (input.setSelectionRange) {
                        input.setSelectionRange(0, end);
                    } else {
                        input.select();
                    }
                }
            });
        },

        close() {
            this.visible = false;
            this.blob = null;
        },

        // 点击弹框外（遮罩）/ ESC / 右上角关闭时，清理暂存资源；lastDir 保留作为默认选择
        onClose() {
            this.blob = null;
            this.dirHandle = null;
            this.dirName = '';
        },

        // 确认保存：锁定后缀（不可改格式），优先写入所选目录，否则用浏览器默认下载
        confirm() {
            var this0 = this;
            var name = (this0.fileName || '').trim();
            if (!name) {
                this0.$message.warning('请输入文件名');
                return;
            }
            var e = this0.ext;
            if (e && !name.toLowerCase().endsWith(e)) {
                name = name + e;
            }
            var data = this0.blob;
            if (!data) {
                this0.close();
                return;
            }
            // 保存目标：API 环境下写入所选目录；否则用默认下载
            var handle = this0.dirHandle;
            if (!handle) {
                this0.$fileDownload(data, name);
                this0.$message({type: 'success', message: '下载成功'});
                this0.close();
                return;
            }
            var savePromise;
            if (typeof handle.getFileHandle !== 'function') {
                savePromise = Promise.reject(new Error('当前浏览器不支持写入所选目录'));
            } else {
                savePromise = handle.getFileHandle(name, {create: true}).then((fileHandle) => {
                    if (typeof fileHandle.createWritable !== 'function') {
                        throw new Error('当前浏览器不支持写入所选目录');
                    }
                    return fileHandle.createWritable().then((writable) => {
                        return writable.write(data).then(() => writable.close());
                    });
                });
            }
            savePromise.then(() => {
                this0.$message({type: 'success', message: '下载成功'});
                this0.close();
            }).catch((err) => {
                this0.$message.error('保存失败：' + (err && err.message ? err.message : '未知错误'));
                // 保留弹框，便于用户重新选择目录或改用默认下载位置
            });
        }
    }
}
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
