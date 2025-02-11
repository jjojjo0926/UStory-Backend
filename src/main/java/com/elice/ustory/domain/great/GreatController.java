package com.elice.ustory.domain.great;

import com.elice.ustory.domain.great.dto.GreatCountResponse;
import com.elice.ustory.domain.great.dto.GreatListResponse;
import com.elice.ustory.domain.great.dto.GreatResponse;
import com.elice.ustory.domain.paper.entity.Paper;
import com.elice.ustory.global.exception.dto.ErrorResponse;
import com.elice.ustory.global.jwt.JwtAuthorization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.elice.ustory.global.Validation.PageableValidation.pageValidate;

@Tag(name = "Great API")
@RestController
@RequestMapping("/papers")
@RequiredArgsConstructor
public class GreatController {

    private final GreatService greatService;

    @Operation(summary = "Create Great API", description = "좋아요로 지정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{paperId}/great")
    public ResponseEntity<Void> saveGreat(@PathVariable Long paperId,
                                         @JwtAuthorization Long userId) {

        greatService.saveGreat(userId, paperId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Read Papers Greatd API", description = "좋아요로 지정된 Paper 리스트를 불러온다. <br> 좋아요가 존재하지 않는 경우 빈리스트를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GreatListResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/greats")
    public ResponseEntity<List<GreatListResponse>> getGreatdPapersByUserId(
            @JwtAuthorization Long userId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        pageValidate(page, size);

        List<Paper> papers = greatService.getGreatsByUser(userId, page, size);

        List<GreatListResponse> result = papers.stream()
                .map(GreatListResponse::new)
                .toList();

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Great Check API",
            description = "좋아요가 지정되어있는지 확인한다. <br>" +
                    "isGreatd가 0인 경우 좋아요로 지정되지 않았음을 의미한다. <br>" +
                    "isGreatd가 1인 경우 좋아요로 지정되어 있음을 의미한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GreatResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{paperId}/great")
    public ResponseEntity<GreatResponse> isPaperGreatd(@PathVariable Long paperId,
                                                       @JwtAuthorization Long userId) {

        boolean isGreatd = greatService.isPaperGreatdByUser(userId, paperId);

        return ResponseEntity.ok(new GreatResponse(isGreatd));
    }

    @Operation(summary = "Great Count API",
            description = "해당 페이퍼에서 좋아요의 총 개수를 반환한다. <br>" +
                    "countGreat로 개수를 알 수 있다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GreatCountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{paperId}/count")
    public ResponseEntity<GreatCountResponse> countGreatd(@PathVariable Long paperId) {

        int count = greatService.countGreatdById(paperId);

        return ResponseEntity.ok(new GreatCountResponse(count));
    }

    @Operation(summary = "Delete Great API", description = "좋아요를 해제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{paperId}/great")
    public ResponseEntity<Void> deleteGreat(@PathVariable Long paperId,
                                           @JwtAuthorization Long userId) {

        greatService.deleteGreat(userId, paperId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
