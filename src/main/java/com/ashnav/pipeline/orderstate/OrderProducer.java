package com.ashnav.pipeline.orderstate;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class OrderProducer {

	private final static String TOPIC = "CreatedOrders";
	private final static String BOOTSTRAP_SERVERS = "localhost:9092,localhost:9093,localhost:9094";
	    
	private static Producer<Long, String> createProducer() {
		
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "Customer");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		return new KafkaProducer<Long, String>(props);
		
	}
	
	static void runProducer(final int sendMessageCount) throws Exception {
		
		final Producer<Long, String> producer = createProducer();
		long time = System.currentTimeMillis();

		try {
			for (long index = time; index < time + sendMessageCount; index++) {
				final ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(TOPIC, index, "181001123");
	
				RecordMetadata metadata = producer.send(record).get();
	
				long elapsedTime = System.currentTimeMillis() - time;
				System.out.printf("sent record(key=%s value=%s) " +
						"meta(partition=%d, offset=%d) time=%d\n",
						record.key(), record.value(), metadata.partition(),
						metadata.offset(), elapsedTime);

			}
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
		}
			
		 finally {
			producer.flush();
			producer.close();
		}
	}
	
	public static void main(String... args) throws Exception {
	    if (args.length == 0) {
	        runProducer(25);
	    } else {
	        runProducer(Integer.parseInt(args[0]));
	    }
	}
}
