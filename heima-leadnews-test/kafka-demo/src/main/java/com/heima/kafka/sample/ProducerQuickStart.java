package com.heima.kafka.sample;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class ProducerQuickStart {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"111.230.204.58:9092");
        properties.put(ProducerConfig.RETRIES_CONFIG,5);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,"lz4");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        // 生产者对象
        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);

        // 封装发送的消息
        ProducerRecord<String, String> record = new ProducerRecord<>("itheima-topic", "1000001", "hello kafka");

        // 发送信息
        Future<RecordMetadata> future = producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e != null){
                    log.error(e+"");
                }
                System.out.println(recordMetadata.topic());
            }
        });

        // 关闭通道，必须关闭，否则小心发送不成功
        producer.close();
    }
}
