package net.microfalx.heimdall.protocol.core.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.resource.MimeType;

import java.time.LocalDateTime;

@Entity
@Table(name = "protocol_parts")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "name")
    @Name
    @Position(1)
    private String name;

    @Column(name = "file_name")
    @Position(5)
    private String fileName;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Position(2)
    private net.microfalx.heimdall.protocol.core.Part.Type type;

    @Column(name = "length", nullable = false)
    @Position(10)
    private int length;

    @Column(name = "mime_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Position(15)
    private MimeType mimeType = MimeType.APPLICATION_OCTET_STREAM;

    @Column(name = "resource", nullable = false)
    @NotBlank
    @Position(100)
    private String resource;

    @Column(name = "created_at", nullable = false)
    @NotNull
    @Position(1000)
    private LocalDateTime createdAt;
}
