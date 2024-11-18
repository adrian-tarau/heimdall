package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.lang.UriUtils;
import net.microfalx.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.util.AntPathMatcher;

import java.util.Collections;

import static net.microfalx.lang.NamedAndTaggedIdentifyAware.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestProjectManagerTest {

    @Mock
    private RestServiceImpl restService;

    @Mock
    private Resource projectResource;


    @InjectMocks
    private RestProjectManager restProjectManager;

    @BeforeEach
    void setUp() {
        Project project = (Project) Project.create(UriUtils.parseUri("https://github.com/adrian-tarau/heimdall.git")).type(Project.Type.GIT)
                .libraryPath("**/test/resource/rest/library/*.js")
                .simulationPath("**/test/resource/rest/library/*.js")
                .tag(SELF_TAG).tag(AUTO_TAG).tag(LOCAL_TAG)
                .name("Heimdall").description("A testing/monitoring tool for developers")
                .build();
        when(restService.getProjectResource()).thenReturn(Resource.temporary());
        when(restService.getProjects()).thenReturn(Collections.singletonList(project));
    }


    @Test
    void gitClone() {
        restProjectManager.initialize(restService);
    }

    @Test
    void gitUpdate(){
        restProjectManager.initialize(restService);
        restProjectManager.reload();
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void antMatcher() {
        AntPathMatcher matcher = new AntPathMatcher();
        assertTrue(matcher.match("/**/test/resources/rest/library/*.js", "/rest/k6/src/test/resources/rest/library/utils.js"));
        assertTrue(matcher.match("/**/test/resources/rest/simulation/*.js", "/rest/k6/src/test/resources/rest/simulation/home.js"));
    }
}