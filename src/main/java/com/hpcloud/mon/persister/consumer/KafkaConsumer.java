package com.hpcloud.mon.persister.consumer;

import com.google.inject.Inject;
import com.hpcloud.mon.persister.configuration.MonPersisterConfiguration;
import kafka.consumer.KafkaStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class KafkaConsumer {

    private static final String KAFKA_CONFIGURATION = "Kafka configuration:";
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    private final Integer numThreads;
    private ExecutorService executorService;
    @Inject
    private KafkaStreams kafkaStreams;

    protected abstract Runnable createRunnable(KafkaStream stream, int threadNumber);
    protected abstract String getStreamName();

    @Inject
    public KafkaConsumer(MonPersisterConfiguration configuration) {

        this.numThreads = configuration.getKafkaConfiguration().getNumThreads();
        logger.info(KAFKA_CONFIGURATION + " numThreads = " + numThreads);
    }

    public void run() {
        List<KafkaStream<byte[], byte[]>> streams = kafkaStreams.getStreams().get(getStreamName());
        executorService = Executors.newFixedThreadPool(numThreads);

        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executorService.submit(createRunnable(stream, threadNumber));
            threadNumber++;
        }
    }

    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
