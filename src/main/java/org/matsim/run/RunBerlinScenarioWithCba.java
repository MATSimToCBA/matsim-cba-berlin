package org.matsim.run;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.contrib.cba.CbaModule;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.run.utils.ResumeSimulationFromLastPlans;



public class RunBerlinScenarioWithCba {

    private static final Logger log = Logger.getLogger(RunBerlinScenarioWithCba.class);

    public static void main(String[] args) {
        for (String arg : args) {
            log.info( arg );
        }
        String configPath = "scenarios/berlin-v5.5-1pct/input/berlin-v5.5-cba-pt-ag-pv-fr1-1pct.config.xml";
        boolean resumeSimulation = false;
        if (args.length>=1 ) {
            configPath = args[0];
        }
        if(args.length>=2) {
            resumeSimulation = Boolean.parseBoolean(args[1]);
        }
        Config config = prepareConfig(configPath);
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
