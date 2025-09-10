package com.example.harbor_total.feature.Notice.dto.response;

import com.example.harbor_total.feature.Notice.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeListRes {

    private int noticeId;
    private String title;
    private String contents;
    private LocalDate createdAt;
    private String filePath;

    public static NoticeListRes mapToNotice(Notice notice) {
        return NoticeListRes.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .contents(notice.getContents())
                .createdAt(LocalDate.from(notice.getCreatedAt()))
                .filePath(notice.getFilePath())
                .build();
    }
}
