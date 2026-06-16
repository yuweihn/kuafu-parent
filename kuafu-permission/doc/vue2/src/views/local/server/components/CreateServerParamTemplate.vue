<template>
	<el-dialog title="新增反向代理参数模板" :visible.sync="addFormVisible" width="500px" :close-on-click-modal="true"
	            append-to-body v-drag>
		<el-form :model="addForm" label-width="90px" :rules="addFormRules" ref="addForm">
            <el-form-item label="模板名称" prop="templateId">
                <el-select v-model="addForm.templateId" placeholder="请输入" @change="onTemplateChanged($event)"
                                :remote-method="initTemplateList" clearable filterable remote style="width: 100%;">
                    <el-option v-for="item in templateSlt.result.list" :key="item.val" :label="item.label" :value="item.val" />
                    <el-pagination layout="total, prev, pager, next" hide-on-single-page @current-change="onTemplatePageChanged"
                                    :current-page="templateSlt.pageNo" :page-size="templateSlt.pageSize" :total="templateSlt.result.size"/>
                </el-select>
            </el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
			<el-button @click.native="addFormVisible = false">取消</el-button>
			<el-button type="primary" @click.native="addSubmit" :loading="addLoading" v-hasPerm="['server.param.template.create']">提交</el-button>
		</div>
	</el-dialog>
</template>

<script>
	export default {
		data() {
			return {
				addFormVisible: false,//新增界面是否显示
				serverId: null,
				addLoading: false,
				addFormRules: {
					templateId: [
						{required: true, message: '请选择模板', trigger: 'blur'}
					]
				},
				//新增界面数据
				addForm: {
					templateId: null
				},

				templateSlt: {
					result: {
						size: 0,
						list: []
					},
					keywords: "",
					pageNo: 1,
					pageSize: 10
				}
			}
		},
		methods: {
			show: function(serverId) {
				this.addFormVisible = true;
				this.serverId = serverId;
				this.addForm = {
					templateId: null
				};
				this.initTemplateList();
				this.resetForm("addForm");
			},
			//新增
			addSubmit: function() {
				this.$refs.addForm.validate((valid) => {
					if (valid) {
						var params = "_fk=u" + "&serverId=" + this.serverId
								 + (this.addForm.templateId ? "&templateId=" + this.addForm.templateId : "");
						this.addLoading = true;

						this.$axios.post(this.$global.baseUrl + '/server/param/template/create', params).then((res) => {
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
			},

			initTemplateList(keywords) {
				this.templateSlt.keywords = keywords;
				this.onTemplatePageChanged(1, 10);
			},
			onTemplatePageChanged(pno, psize) {
				if (pno != null) {
					this.templateSlt.pageNo = pno;
				}
				if (psize != null) {
					this.templateSlt.pageSize = psize;
				}
				var params = {
                    name: this.templateSlt.keywords,
                    pageNo: this.templateSlt.pageNo,
                    pageSize: this.templateSlt.pageSize
				};
				this.$axios.get(this.$global.baseUrl + '/template/drop-down-list', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.templateSlt.result.size = res.data.data.size;
						this.templateSlt.result.list = res.data.data.list.map(item => {
							return {val: item.id, label: item.name};
						});
					} else {
						this.templateSlt.result.size = 0;
						this.templateSlt.result.list = [];
						this.$message.error(res.data.msg);
					}
				}).catch((err) => {
					this.$message.error(err.message);
				});
			},
			onTemplateChanged(templateId) {
				if (templateId) {
					return;
				}
				this.initTemplateList();
			}
		},
		mounted() {

		}
	}
</script>

<style scoped>

</style>

