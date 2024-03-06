package kr.co.yigil.notice.interfaces.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class NoticeDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeCreateRequest{
        private String title;
        private String content;
    }

    @Getter
    @AllArgsConstructor
    public static class NoticeUpdateRequest{
        private String title;
        private String content;
    }

    @Getter
    @AllArgsConstructor
    public abstract static class BaseMessageResponse {
        private final String message;
    }

    @Getter
    @Builder
    public static class NoticeListResponse{
        List<NoticeItem> noticeList;
        boolean hasNext;
    }
    @Getter
    @Builder
    public static class NoticeItem {
        private Long noticeId;
        private String title;
        private String authorId;
        private String author;
        private LocalDateTime createdAt;
    }

    @Getter
    public static class NoticeCreateResponse extends BaseMessageResponse {
        public NoticeCreateResponse(String message) {
            super(message);
        }
    }

    @Getter
    public static class NoticeUpdateResponse extends BaseMessageResponse {
        public NoticeUpdateResponse(String message) {
            super(message);
        }
    }

    @Getter
    public static class NoticeDeleteResponse extends BaseMessageResponse {
        public NoticeDeleteResponse(String message) {
            super(message);
        }
    }

    @Getter
    @Builder
    public static class NoticeDetailResponse {
        private Long noticeId;
        private String title;
        private String content;
        private Long authorId;
        private String authorNickname;
        private String profileImageUrl;
        private LocalDateTime createdAt;
    }
}
