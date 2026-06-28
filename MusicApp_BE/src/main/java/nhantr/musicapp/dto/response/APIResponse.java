package nhantr.musicapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> APIResponse<T> success(T data) {
        return APIResponse.<T>builder()
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> APIResponse<T> error(int code, String message) {
        return APIResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
