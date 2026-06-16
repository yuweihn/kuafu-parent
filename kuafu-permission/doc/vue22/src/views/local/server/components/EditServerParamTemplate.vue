<template>
	<el-dialog title="编辑反向代理参数模板" :visible.sync="editFormVisible" width="500px" :close-on-click-modal="true"
	             append-to-body v-drag>
		<el-form :model="editForm" label-width="90px" :rules="editFormRules" ref="editForm">
            <el-form-item label="模板名称" prop="templateId">
                <el-select v-model="editForm.templateId" placeholder="请输入" @change="onTemplateChanged($event)"
                                :remote-method="initTemplateList" clearable filterable remote style="width: 100%;">
                    <el-option v-for="item in templateSlt.result.list" :key="item.val" :label="item.label" :value="item.val" />
                    <el-pagination layout="total, prev, pager, next" hide-on-single-page @current-change="onTemplatePageChanged"
                                :current-page="templateSlt.pageNo" :page-size="templateSlt.pageSize" :total="templateSlt.result.size"/>
                </el-select>
            </el-form-item>
            <el-form-item label="顺序" prop="orderBy">
                <el-input-number v-model="editForm.orderBy" autocomplete="off" clearable placeholder="" controls-position="right" :min="1" />
            </el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
			<el-button @click.native="editFormVisible = false">取消</el-button>
			<el-button type="primary" @click.native="editSubmit" :loading="editLoading" v-hasPerm="['server.param.template.update']">提交</el-button>
		</div>
	</el-dialog>
</template>

<script>
	import {isInteger} from '@/components/js/validate';
	export default {
		data() {
			return {
				editFormVisible: false,//编辑界面是否显示
				serverId: null,
				editLoading: false,
				editFormRules: {
					templateId: [
						{required: true, message: '请选择模板', trigger: 'blur'}
					],
                    orderBy: [
                    {required: true, message: '请输入序号', trigger: 'blur'}
						, {validator: isInteger, trigger: 'blur'}
                    ]
				},
				//编辑界面数据
				editForm: {
					templateId: null,
					orderBy: null
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
			show: function(serverId, index, row) {
				this.editFormVisible = true;
				this.serverId = serverId;
				//this.editForm = Object.assign({}, row);
				this.editForm = {
					id: row.id,
                    templateId: row.templateId,
                    orderBy: row.orderBy
				};
				if (row.templateId != null) {
                    this.templateSlt.result.size = 1;
                    this.templateSlt.result.list = [
                        {
                            val: row.templateId,
                            label: row.templateName
                        }
                    ];
				} else {
				    this.initTemplateList();
				}
				this.resetForm("editForm");
			},

			//编辑
			editSubmit: function() {
				this.$refs.editForm.validate((valid) => {
					if (valid) {
						var params = "_fk=u" + "&id=" + this.editForm.id + "&serverId=" + this.serverId
								 + (this.editForm.templateId ? "&templateId=" + this.editForm.templateId : "")
                                 + "&orderBy=" + this.editForm.orderBy;
						this.editLoading = true;

						this.$axios.post(this.$global.baseUrl + '/server/param/template/update', params).then((res) => {
							if (res.data.code === '0000') {
								this.$message({type: "success", message: res.data.msg});
							} else {
								this.$message.error(res.data.msg);
							}
							this.$refs['editForm'].resetFields();
							this.editFormVisible = false;
							this.editLoading = false;
							this.$emit("success", 1);
						}).catch((err) => {
							this.editLoading = false;
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

