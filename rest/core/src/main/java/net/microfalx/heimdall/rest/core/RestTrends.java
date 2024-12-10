package net.microfalx.heimdall.rest.core;

public class RestTrends {

    private static final String SIMULATION_SQL = """
            select ie.natural_id environment, rs.natural_id simulation, rr.started_at, rr.duration, rr.apdex from rest_result rr
            	inner join infrastructure_environment ie ON ie.id = rr.environment_id
            	inner join rest_simulation rs ON rs.id = rr.simulation_id where rr.started_at between (?,?)""";
    private static final String SCENARIO_SQL = """
            select ie.natural_id environment, rsi.natural_id simulation, rsc.natural_id scenario, ro.started_at, ro.duration, ro.apdex from rest_output ro
            	inner join infrastructure_environment ie ON ie.id = ro.environment_id
            	inner join rest_simulation rsi ON rsi.id = ro.simulation_id
            	inner join rest_scenario rsc ON rsc.id = ro.scenario_id
            where rr.started_at between (?,?)""";
}
