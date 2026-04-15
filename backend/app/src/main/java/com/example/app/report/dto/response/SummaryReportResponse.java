package com.example.app.report.dto.response;

public record SummaryReportResponse(
	long total,
	long open,
	long inProgress,
	long closed,
	long overdue
) {
}
