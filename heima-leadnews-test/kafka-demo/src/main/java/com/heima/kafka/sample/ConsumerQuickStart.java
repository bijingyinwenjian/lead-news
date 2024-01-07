package com.heima.kafka.sample;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ConsumerQuickStart {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerQuickStart.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"111.230.204.58:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"group2");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        Consumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Collections.singletonList("itheima-topic"));
        while (true){
            try {
                ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    System.out.println(consumerRecord.key());
                    System.out.println(consumerRecord.value());
                }
                consumer.commitAsync(new OffsetCommitCallback() {
                    @Override
                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
                        if (e != null){
                            log.error("提交偏移量错误{}",e);
                            logger.error("提交偏移量错误{}",e);
                        }
                    }
                });
                int i = 1/0;
            } catch (Exception e) {
                log.error("记录错误信息:{}",e);
            }finally {
                try {
                    consumer.commitSync();
                } finally {
                    consumer.close();
                }
            }
        }
    }
}
