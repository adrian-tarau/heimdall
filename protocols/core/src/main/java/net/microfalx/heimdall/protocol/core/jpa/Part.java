package net.microfalx.heimdall.protocol.core.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.microfalx.heimdall.protocol.core.MimeType;
import net.microfalx.lang.annotation.Name;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "protocol_parts")
public class Part {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @Name
    private String name;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private net.microfalx.heimdall.protocol.core.Part.Type type;

    @Column(name = "length", nullable = false)
    private int length;

    @Column(name = "mime_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MimeType mimeType = MimeType.APPLICATION_OCTET_STREAM;

    @Column(name = "resource", nullable = false)
    @NotBlank
    private String resource;

    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public net.microfalx.heimdall.protocol.core.Part.Type getType() {
        return type;
    }

    public void setType(net.microfalx.heimdall.protocol.core.Part.Type type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Part that = (Part) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", resource='" + resource + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
