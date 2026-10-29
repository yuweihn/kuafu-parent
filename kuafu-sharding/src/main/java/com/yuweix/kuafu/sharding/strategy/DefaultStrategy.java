package com.yuweix.kuafu.sharding.strategy;


import com.yuweix.kuafu.sharding.context.DatabaseSetting;
import com.yuweix.kuafu.sharding.context.ShardingContext;
import com.yuweix.kuafu.sharding.context.TableSetting;


/**
 * 全局槽位路由：将分片空间视为 databaseSize × tableSize 的网格，
 * 槽位 slot = hash % (databaseSize * tableSize)，库下标 = slot / tableSize，表下标 = slot % tableSize。
 * 只要 hash 均匀分布，任意物理库、物理表均可达，不要求 tableSize 是 databaseSize 的整数倍。
 * 分片下标由 slot 统一计算，保证同一 shardingVal 的库/表坐标一致；
 * 库名的 split/suffixLength 取自 {@link DatabaseSetting}，表名的取自 {@link TableSetting}。
 * @author yuwei
 */
public class DefaultStrategy implements Strategy {
    @Override
    public <T>String getPhysicalDatabaseName(String dbName, String tableName, T shardingVal) {
        DatabaseSetting dsetting = ShardingContext.getShardDatabaseSetting(dbName);
        if (dsetting == null) {
            throw new RuntimeException("[" + dbName + "]'s sharding-conf is required.");
        }
        TableSetting tsetting = ShardingContext.getShardSetting(tableName);
        if (tsetting == null) {
            throw new RuntimeException("[" + tableName + "]'s sharding-conf is required.");
        }
        int slot = slot(tsetting.getDatabaseSize(), tsetting.getTableSize(), shardingVal);
        return dbName + dsetting.getSplit() + format(slot / tsetting.getTableSize(), dsetting.getSuffixLength());
    }

    @Override
    public <T>String getPhysicalTableName(String tableName, T shardingVal) {
        TableSetting setting = ShardingContext.getShardSetting(tableName);
        if (setting == null) {
            throw new RuntimeException("[" + tableName + "]'s sharding-conf is required.");
        }
        int slot = slot(setting.getDatabaseSize(), setting.getTableSize(), shardingVal);
        return tableName + setting.getSplit() + format(slot % setting.getTableSize(), setting.getSuffixLength());
    }

    /**
     * 计算全局槽位，均匀落在 [0, databaseSize * tableSize) 的每一个值。
     */
    private <T>int slot(int databaseSize, int tableSize, T shardingVal) {
        if (databaseSize <= 0 || tableSize <= 0) {
            throw new RuntimeException("database-size and table-size must be greater than 0.");
        }
        return hash(shardingVal) % (databaseSize * tableSize);
    }

    private String format(int index, int suffixLength) {
        return String.format("%0" + suffixLength + "d", index);
    }

    private int hash(Object str) {
        if (str == null) {
            return 0;
        }
        int h = str.hashCode();
        h = h ^ (h >>> 16);
        return h & Integer.MAX_VALUE;
    }
}
