package com.projectanalytics.portfolio;

import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.portfolio.persistence.PortfolioProjectEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PortfolioApiIntegrationTest {

    @Autowired
    private RecommendationRepository recommendationRepository;


    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PortfolioProjectRepository portfolioProjectRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WorkPackageRepository workPackageRepository;

    private PortfolioEntity portfolio;

    @Autowired
    private com.projectanalytics.analytics.persistence.AnalyticsRepository analyticsRepository;

    @Autowired
    private com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository analyticsSnapshotRepository;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        analyticsSnapshotRepository.deleteAll();
        analyticsRepository.deleteAll();
        workPackageRepository.deleteAll();
        portfolioProjectRepository.deleteAll();
        projectRepository.deleteAll();
        portfolioRepository.deleteAll();
        workspaceRepository.deleteAll();

        WorkspaceEntity workspace = workspaceRepository.save(
                new WorkspaceEntity("API WS", "https://op-api-portfolio.test")
        );
        portfolio = portfolioRepository.save(new PortfolioEntity(workspace, "Core", null));
        ProjectEntity project = new ProjectEntity(workspace, 99L, "P99");
        project.setStatus("ACTIVE");
        projectRepository.save(project);
    }

    @Test
    @DisplayName("authenticated portfolio list/detail/kpis/dashboard endpoints succeed")
    void portfolioEndpoints() {
        String token = login();
        HttpHeaders headers = bearer(token);

        ResponseEntity<Map> list = restTemplate.exchange(
                "/api/v1/portfolios?workspaceId=" + portfolio.getWorkspace().getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> detail = restTemplate.exchange(
                "/api/v1/portfolios/" + portfolio.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(detail.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> detailData = (Map<String, Object>) detail.getBody().get("data");
        assertThat(detailData.get("name")).isEqualTo("Core");

        ResponseEntity<Map> kpis = restTemplate.exchange(
                "/api/v1/portfolios/" + portfolio.getId() + "/kpis",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(kpis.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> dashboard = restTemplate.exchange(
                "/api/v1/portfolios/" + portfolio.getId() + "/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(dashboard.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String login() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "Admin123!"),
                Map.class
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("token");
    }

    private static HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
