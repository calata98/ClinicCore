package com.cliniccore.analytics.api;

import com.cliniccore.analytics.application.DashboardService;
import com.cliniccore.analytics.application.DashboardService.ClinicDashboard;
import com.cliniccore.shared.application.CurrentUserProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;
	private final CurrentUserProvider currentUserProvider;

	public DashboardController(DashboardService dashboardService, CurrentUserProvider currentUserProvider) {
		this.dashboardService = dashboardService;
		this.currentUserProvider = currentUserProvider;
	}

	@GetMapping("/clinic")
	ClinicDashboard clinicDashboard() {
		return dashboardService.clinicDashboard(currentUserProvider.require());
	}
}
