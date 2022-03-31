package org.matsim.run;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.contrib.cba.CbaModule;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.run.utils.ResumeSimulationFromLastPlans;



public class RunBerlinScenarioWithCba {

    private static final Logger log = Logger.getLogger(RunBerlinScenarioWithCba.class);

    public static void main(String[] args) throws CommandLine.ConfigurationException {
        CommandLine cmd = new CommandLine.Builder(args)
                .requireOptions("config-path")
                .allowOptions("resume-simulation")
                .build();
        String configPath = cmd.getOptionStrict("config-path");
        boolean resumeSimulation = cmd.hasOption("resume-simulation") && Boolean.parseBoolean(cmd.getOptionStrict("resume-simulation"));
        Config config = prepareConfig(configPath);
        cmd.applyConfiguration(config);
        if(resumeSimulation) {
            ResumeSimulationFromLastPlans.resumeSimulationFromLastPlans(config);
        }
        Scenario scenario = prepareScenario(config);
        Controler controler = prepareControler(scenario);
        controler.run();
    }
    public static Config prepareConfig(String configPath) {
        return RunBerlinScenario.prepareConfig(new String[]{configPath}, new CbaConfigGroup());
    }

    public static Scenario prepareScenario(Config config) {
        return RunBerlinScenario.prepareScenario(config);
    }

    public static Controler prepareControler(Scenario scenario) {
        Controler controler = RunBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new CbaModule());
        return controler;
    }
}
