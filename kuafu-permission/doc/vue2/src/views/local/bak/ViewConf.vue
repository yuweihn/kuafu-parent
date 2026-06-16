<template>
<el-dialog title="查看内容" :visible.sync="formVisible" width="700px" :close-on-click-modal="true"
				append-to-body v-drag>
	<section v-loading="loading">
		<el-input type="textarea" v-model="confInfo.content" :rows="12" clearable readonly autocomplete="off" wrap="off"/>
        <div class="sub-conf" v-if="confInfo.subConfList != null && confInfo.subConfList.length > 0">
            <el-table :data="confInfo.subConfList">
                <el-table-column type="expand" width="18">
                    <template slot-scope="scope">
                        <el-form label-position="left" inline class="table-expand">
                            <el-form-item style="width: 100%;">
                                <el-input type="textarea" v-model="scope.row.content" :rows="8" clearable readonly autocomplete="off" wrap="off"/>
                            </el-form-item>
                        </el-form>
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot-scope="scope2">{{scope2.row.name}}</template>
                </el-table-column>
            </el-table>
        </div>
	</section>
	<div slot="footer" class="dialog-footer">
		<el-button type="primary" @click.native="formVisible = false" class="el-icon-close"> 关闭</el-button>
	</div>
</el-dialog>
</template>

<script>
	export default {
		data() {
			return {
				formVisible: false,
				path: null,
				confInfo: {},
				loading: false
			}
		},
		methods: {
			show: function(path) {
                this.path = path;
                this.confInfo = {};
                this.getBakConfContent();
                this.formVisible = true;
			},
			getBakConfContent() {
				var params = {path: this.path};
				this.loading = true;

				this.$axios.get(this.$global.baseUrl + '/back/content', {params: params}).then((res) => {
					if (res.data.code === '0000') {
						this.confInfo = res.data.data;
					} else {
						this.confInfo = {};
						this.$message.error(res.data.msg);
					}
					this.loading = false;
				}).catch((err) => {
					this.loading = false;
					this.$message.error(err.message);
				});
			}
		},
		mounted() {

		}
	}
</script>

<style scoped>
    .sub-conf >>> .el-form-item__content {
        width: 100%;
    }
</style>

