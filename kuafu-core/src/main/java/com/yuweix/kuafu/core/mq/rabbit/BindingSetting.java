package com.yuweix.kuafu.core.mq.rabbit;


import java.util.List;


public interface BindingSetting {
    List<Item> getBindings();

    class Item {
        private String queue;
        private String exchange;
        /**
         * 交换机类型可取值有三种：direct、fanout和topic
         */
        private String exchangeType;
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

        public void setExchangeType(String exchangeType) {
            this.exchangeType = exchangeType;
        }

        public String getExchangeType() {
            return exchangeType;
        }

        public void setRouteKey(String routeKey) {
            this.routeKey = routeKey;
        }

        public String getRouteKey() {
            return routeKey;
        }
    }
}
