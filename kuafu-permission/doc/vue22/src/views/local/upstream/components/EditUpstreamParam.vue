<template>
	<el-dialog title="编辑负载均衡参数" :visible.sync="editFormVisible" width="500px" :close-on-click-modal="true"
	      append-to-body v-drag>
		<el-form :model="editForm" label-width="80px" :rules="editFormRules" ref="editForm">
			<el-form-item label="参数名" prop="name">
				<el-input v-model="editForm.name" clearable autocomplete="off" style="width: 100%;" />
			</el-form-item>
			<el-form-item label="参数值" prop="value">
				<el-input type="textarea" :rows="5" v-model="editForm.value" clearable autocomplete="off" style="width: 100%;"/>
			</el-form-item>
            <el-form-item label="顺序" prop="orderBy">
                <el-input-number v-model="editForm.orderBy" autocomplete="off" clearable placeholder="" controls-position="right" :min="1" />
            </el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
			<el-button @click.native="editFormVisible = false">取消</el-button>
			<el-button type="primary" @click.native="editSubmit" :loading="editLoading" v-hasPerm="['upstream.param.update']">提交</el-button>
		</div>
	</el-dialog>
</template>

<script>
	import {isInteger} from '@/components/js/validate';
	export default {
		data() {
			return {
				editFormVisible: false,//编辑界面是否显示
				upstreamId: null,
				editLoading: false,
				editFormRules: {
					name: [
						{required: true, message: '请输入参数名', trigger: 'blur'}
					],
					value: [
						{required: true, message: '请输入参数值', trigger: 'blur'}
					],
                    orderBy: [
                        {required: true, message: '请输入序号', trigger: 'blur'}
                            , {validator: isInteger, trigger: 'blur'}
                    ]
				},
				//编辑界面数据
				editForm: {
					id: null,
					name: null,
					value: null,
					orderBy: null
				}
			}
		},
		methods: {
			show: function(upstreamId, index, row) {
				this.editFormVisible = true;
				this.upstreamId = upstreamId;
				//this.editForm = Object.assign({}, row);
				this.editForm = {
					id: row.id,
                    name: row.name,
                    value: row.value,
                    orderBy: row.orderBy
				};
				this.resetForm("editForm");
			},

			//编辑
			editSubmit: function() {
				this.$refs.editForm.validate((valid) => {
					if (valid) {
						var params = "_fk=u" + "&id=" + this.editForm.id + "&upstreamId=" + this.upstreamId
								 + (this.editForm.name ? "&name=" + this.editForm.name : "")
                                 + (this.editForm.value ? "&value=" + this.editForm.value : "")
                                 + "&orderBy=" + this.editForm.orderBy;
						this.editLoading = true;

						this.$axios.post(this.$global.baseUrl + '/upstream/param/update', params).then((res) => {
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
			}
		},
		mounted() {

		}
	}
</script>

<style scoped>

</style>

