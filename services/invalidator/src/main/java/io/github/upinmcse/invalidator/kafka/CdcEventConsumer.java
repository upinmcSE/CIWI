package io.github.upinmcse.invalidator.kafka;

import io.github.upinmcse.invalidator.redis.CacheInvalidator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class CdcEventConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(CdcEventConsumer.class);

    private final ObjectMapper mapper;
    private final CacheInvalidator invalidator;
    private final Counter handled;
    private final Counter skippedNoPk;
    private final Counter skippedTombstone;

    public CdcEventConsumer(ObjectMapper mapper,
                            CacheInvalidator invalidator,
                            MeterRegistry registry) {
        this.mapper             = mapper;
        this.invalidator        = invalidator;
        this.handled            = Counter.builder("invalidator.events.handled").register(registry);
        this.skippedNoPk        = Counter.builder("invalidator.events.skipped").tag("reason", "no_pk").register(registry);
        this.skippedTombstone   = Counter.builder("invalidator.events.skipped").tag("reason", "tombstone").register(registry);
    }

    @KafkaListener(
            topics            = "${app.kafka.topic}",
            groupId           = "${app.kafka.group}",
            containerFactory  = "kafkaListenerContainerFactory")
    public void onCdcEvent(ConsumerRecord<String, String> record) {
        if (record.value() == null) {
            skippedTombstone.increment();
            return;
        }

        DebeziumEnvelope.parse(record.value(), mapper).ifPresentOrElse(env -> {
            env.primaryKey("id").ifPresentOrElse(id -> {
                invalidator.invalidate(id);
                handled.increment();
                LOG.debug("op={} id={} partition={} offset={}",
                        env.op(), id, record.partition(), record.offset());
            }, () -> {
                skippedNoPk.increment();
                LOG.warn("CDC event without id pk; partition={} offset={}",
                        record.partition(), record.offset());
            });
        }, () -> {
            skippedNoPk.increment();
            LOG.warn("unparseable CDC envelope; partition={} offset={}",
                    record.partition(), record.offset());
        });
    }
}
