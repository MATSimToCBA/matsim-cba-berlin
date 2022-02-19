package org.matsim.run;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.contrib.cba.CbaModule;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;


public class RunBerlinScenarioWithCba {

    private static final Logger log = Logger.getLogger(RunBerlinScenarioWithCba.class);

    public static void main(String[] args) {
        for (String arg : args) {
            log.info( arg );
        }
        if (args.length==0 ) {
            args = new String[] {"scenarios/berlin-v5.5-1pct/input/berlin-v5.5-cba-pt-ag-pv-fr1-1pct.config.xml"}  ;
        }
        Config config = prepareConfig(args);
        Scenario scenario = prepareScenario(config);
        Controler controler = prepareControler(scenario);
        controler.run();
    }
    public static Config prepareConfig(String[] args) {
        return RunBerlinScenario.prepareConfig(args, new CbaConfigGroup());
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
