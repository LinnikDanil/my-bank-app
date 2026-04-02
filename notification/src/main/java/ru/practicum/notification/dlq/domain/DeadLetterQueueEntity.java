package ru.practicum.notification.dlq.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "dead_letter_queue")
@Getter
@Setter
public class DeadLetterQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "msg_topic", nullable = false, length = 255)
    private String msgTopic;

    @Column(name = "msg_partition", nullable = false)
    private Integer msgPartition;

    @Column(name = "msg_offset", nullable = false)
    private Long msgOffset;

    @Column(name = "msg_key")
    private String msgKey;

    @Column(name = "msg_value", columnDefinition = "text")
    private String msgValue;

    @Column(name = "error_message", nullable = false, columnDefinition = "text")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
