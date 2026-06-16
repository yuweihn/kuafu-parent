<template>
	<section>
		<!--工具条-->
		<el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
			<el-form :inline="true">
				<el-form-item>
					<el-button type="primary" v-on:click="getHttpList()" class="el-icon-refresh" v-hasPerm="['http.list']"> 刷新</el-button>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="$refs.createHttp.show()" class="el-icon-edit" v-hasPerm="['http.create']"> 新增</el-button>
				</el-form-item>
			</el-form>
		</el-col>

		<!--列表-->
		<el-table :data="httpList" highlight-current-row v-loading="listLoading" @selection-change="onSelsChanged" style="width: 100%;">
			<el-table-column type="expand" width="18">
				<template slot-scope="scope">
					<el-form label-position="left" inline class="table-expand">
						<el-form-item label="ID：">
							<span>{{scope.row.id}}</span>
						</el-form-item>
						<el-form-item label="参数名：">
							<span>{{scope.row.name}}</span>
						</el-form-item>
						<el-form-item label="参数值：">
							<el-input type="textarea" :rows="5" style="width: 550px;" readonly placeholder="" v-model="scope.row.value"></el-input>
						</el-form-item>
						<el-form-item label="顺序：">
							<span>{{scope.row.orderBy}}</span>
						</el-form-item>
						<el-form-item label="创建人：">
							<span>{{scope.row.creator}}</span>
						</el-form-item>
						<el-form-item label="创建时间：">
							<span>{{scope.row.createTime}}</span>
						</el-form-item>
						<el-form-item label="修改人：">
							<span>{{scope.row.modifier}}</span>
						</el-form-item>
						<el-form-item label="修改时间：">
							<span>{{scope.row.modifyTime}}</span>
						</el-form-item>
					</el-form>
				</template>
			</el-table-column>
			<el-table-column type="selection" width="30" />
			<el-table-column prop="name" label="参数名" width="180" :show-overflow-tooltip="true" />
			<el-table-column prop="value" label="参数值" width="350" :show-overflow-tooltip="true" />
			<el-table-column prop="orderBy" label="顺序" width="80" />
			<el-table-column label="操作" width="200">
				<template slot-scope="scope">
					<el-tooltip placement="top">
						<div slot="content">编辑</div>
						<el-button type="text" circle class="el-icon-edit" @click="$refs.editHttp.show(scope.$index, scope.row)" v-hasPerm="['http.update']" />
					</el-tooltip>
					<el-tooltip placement="top">
						<div slot="content">上移</div>
						<el-button type="text" circle class="el-icon-arrow-up" @click="handleUp(scope.$index, scope.row)" v-hasPerm="['http.move.up']" />
					</el-tooltip>
					<el-tooltip placement="top">
						<div slot="content">下移</div>
						<el-button type="text" circle class="el-icon-arrow-down" @click="handleDown(scope.$index, scope.row)" v-hasPerm="['http.move.down']" />
					</el-tooltip>
					<el-dropdown trigger="click" v-hasPerm="['http.delete']">
						<el-tooltip placement="top">
							<div slot="content">更多</div>
							<el-button type="text" circle class="el-icon-more" />
						</el-tooltip>
						<el-dropdown-menu slot="dropdown">
							<div @click="handleDel(scope.$index, scope.row)" v-hasPerm="['http.delete']">
							<el-dropdown-item>
								<el-tooltip placement="top">
									<div slot="content">删除</div>
									<el-button type="text" circle class="el-icon-delete" v-hasPerm="['http.delete']"> 删除</el-button>
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
			<el-button type="danger" @click="batchRemove" :disabled="this.sels.length === 0" class="el-icon-delete" v-hasPerm="['http.delete']"> 批量删除</el-button>
		</el-col>

		<!--新增界面-->
        <create-http ref="createHttp" v-on:success="getHttpList"/>

        <!--编辑界面-->
        <edit-http ref="editHttp" v-on:success="getHttpList"/>
	</section>
</template>

<script>
	import CreateHttp from './components/CreateHttp';
	import EditHttp from './components/EditHttp';
	export default {
		components: {
			'create-http': CreateHttp,
			'edit-http': EditHttp
		},

		data() {
			return {
				httpList: [],
				listLoading: false,
				sels: []    //列表选中列
			}
		},
		methods: {
			onSelsChanged: function(sels) {
				this.sels = sels;
			},
			getHttpList() {
				this.listLoading = true;

				this.$axios.get(this.$global.baseUrl + '/http/list', {}).then((res) => {
					if (res.data.code === '0000') {
						this.httpList = res.data.data;
					} else {
						this.$message.error(res.data.msg);
					}
					this.listLoading = false;
				}).catch((err) => {
					this.listLoading = false;
					this.$message.error(err.message);
				});
			},

			handleUp: function(index, row) {
				var params = "id=" + row.id;
				this.listLoading = true;
				this.$axios.post(this.$global.baseUrl + '/http/move-up', params).then((res) => {
					if (res.data.code === '0000') {
						//DO NOTHING
					} else {
						this.$message.error(res.data.msg);
					}
					this.listLoading = false;
					this.getHttpList();
				}).catch((err) => {
					this.listLoading = false;
					this.$message.error(err.message);
				});
			},

			handleDown: function(index, row) {
				var params = "id=" + row.id;
				this.listLoading = true;
				this.$axios.post(this.$global.baseUrl + '/http/move-down', params).then((res) => {
					if (res.data.code === '0000') {
						//DO NOTHING
					} else {
						this.$message.error(res.data.msg);
					}
					this.listLoading = false;
					this.getHttpList();
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

					this.$axios.delete(this.$global.baseUrl + '/http/delete', {params: params}).then((res) => {
						if (res.data.code === '0000') {
							this.$message({type: "success", message: res.data.msg});
						} else {
							this.$message.error(res.data.msg);
						}
						this.listLoading = false;
						this.getHttpList();
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
			this.getHttpList();
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

