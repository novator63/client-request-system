package com.example.app.report.controller;

import com.example.app.report.dto.response.CategoryReportItemResponse;
import com.example.app.report.dto.response.StatusReportItemResponse;
import com.example.app.report.dto.response.SummaryReportResponse;
import com.example.app.report.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	@GetMapping("/summary")
	@PreAuthorize("hasRole('ADMIN')")
	public SummaryReportResponse getSummary() {
		return reportService.getSummary();
	}

	@GetMapping("/by-status")
	@PreAuthorize("hasRole('ADMIN')")
	public List<StatusReportItemResponse> getByStatus() {
		return reportService.getByStatus();
	}

	@GetMapping("/by-category")
	@PreAuthorize("hasRole('ADMIN')")
	public List<CategoryReportItemResponse> getByCategory() {
		return reportService.getByCategory();
	}
}
