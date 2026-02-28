package com.upskill.hooks;

import com.upskill.db.DatabaseUtil;
import com.upskill.utils.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("======== STARTING: {} [Tags: {}] ========", scenario.getName(), scenario.getSourceTagNames());
        TestContext.clear();
    }

    @After
    public void afterScenario(Scenario scenario) {
        log.info("======== FINISHED: {} [Status: {}] ========", scenario.getName(), scenario.getStatus());
        TestContext.remove();
    }

    @After("@db")
    public void afterDbScenario() {
        DatabaseUtil.closeConnection();
    }
}
