package com.yuweix.kuafu.core.mq.rabbit;


import java.util.List;


public interface BindingSetting {
    List<Item> getBindings();

    class Item {
        private String queue;
        /**
         * 逗号(,)隔开交换机名称和交换机类型，也有可能没有逗号，那就不含有交换机类型，使用默认类型direct。
         * 交换机类型可取值有三种：direct、fanout和topic
         */
        private String exchange;
        private String routeKey;

        public void setQueue(String queue) {
            this.queue = queue;
        }

        public String getQueue() {
            return queue;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getExchange() {
            return exchange;
        }

        public void setRouteKey(String routeKey) {
            this.routeKey = routeKey;
        }

        public String getRouteKey() {
            return routeKey;
        }
    }
}
