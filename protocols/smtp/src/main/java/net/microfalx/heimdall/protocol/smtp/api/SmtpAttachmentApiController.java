package net.microfalx.heimdall.protocol.smtp.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.restapi.RestApiDataSetController;
import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/smtp_attachments")
@DataSet(model = SmtpEvent.class, timeFilter = false)
@Tag(name = "SMTP Attachments", description = "SMTP Attachment Management API")
public class SmtpAttachmentApiController extends RestApiDataSetController<SmtpAttachment, SmtpAttachmentDTO, Long> {

    @Operation(summary = "List smtp attachment events", description = "Returns a list of smtp attachment events with search and paging.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SmtpAttachmentDTO.class)))
    @GetMapping
    public List<SmtpAttachmentDTO> list(
            @Parameter(description = "The query used to filter by various model fields", name = "query", example = "username")
            @RequestParam(name = "query", required = false) String query,

            @Parameter(description = "The sorting desired for the result set", name = "sort", example = "modifiedAt=desc")
            @RequestParam(name = "sort", required = false) String sort,

            @Parameter(description = "The page to return for the result set", name = "page", example = "0")
            @RequestParam(name = "page", required = false) int page,

            @Parameter(description = "The page size for the result set", name = "page size", example = "20")
            @RequestParam(name = "page-size", required = false) int pageSize
    ) {
        return doList(null, query, sort, page, pageSize);
    }

    @Operation(summary = "Get smtp attachment event", description = "Returns a single smpt attachment event by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SmtpAttachmentDTO.class)))
    @GetMapping("/{id}")
    public SmtpAttachmentDTO get(@Parameter(description = "The user identifier", example = "42") @PathVariable Long id) {
        return doFind(id);
    }

    @Override
    protected Class<? extends RestApiMapper<SmtpAttachment, SmtpAttachmentDTO>> getMapperClass() {
        return SmtpAttachmentMapper.class;
    }
}
