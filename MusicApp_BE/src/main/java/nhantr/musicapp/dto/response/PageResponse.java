package nhantr.musicapp.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public static <T> PageResponse<T> fromPage(Page<T> pageData) {
        return PageResponse.<T>builder()
                .content(pageData.getContent())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .page(pageData.getNumber())
                .size(pageData.getSize())
                .build();
    }
}
