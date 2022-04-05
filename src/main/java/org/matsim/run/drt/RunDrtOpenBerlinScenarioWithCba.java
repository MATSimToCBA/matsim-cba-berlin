package org.matsim.run.drt;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.contrib.cba.CbaModule;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.speedup.DrtSpeedUpParams;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.run.utils.ResumeSimulationFromLastPlans;
import org.matsim.core.config.CommandLine;


public class RunDrtOpenBerlinScenarioWithCba {

    /**
     * @param args
     * @throws CommandLine.ConfigurationException
     */
    public static void main(String[] args) throws CommandLine.ConfigurationException {
        CommandLine cmd = new CommandLine.Builder(args)
                .requireOptions("config-path")
                .allowOptions("drt-speed-up", "resume-simulation")
                .build();
        String configPath = cmd.getOptionStrict("config-path");
        boolean resumeSimulation = cmd.hasOption("resume-simulation") && Boolean.parseBoolean(cmd.getOptionStrict("resume-simulation"));
        boolean drtSpeedUp = cmd.hasOption("drt-speed-up") && Boolean.parseBoolean(cmd.getOptionStrict("drt-speed-up"));
        Config config = prepareConfig(new String[]{configPath}, drtSpeedUp, resumeSimulation);
        cmd.applyConfiguration(config);
        Scenario scenario = prepareScenario(config, drtSpeedUp);
        Controler controler = prepareControler(scenario);
        controler.run();
    }

    public static Config prepareConfig(String[] args, boolean drtSpeedUp, boolean resumeSimulation) {
        Config config = RunDrtOpenBerlinScenario.prepareConfig(args, new CbaConfigGroup());
        if(resumeSimulation) {
            ResumeSimulationFromLastPlans.resumeSimulationFromLastPlans(config);
        }
        if(drtSpeedUp) {
            for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
                if (drtCfg.getDrtSpeedUpParams().isEmpty()) {
                    drtCfg.addParameterSet(new DrtSpeedUpParams());
                }
            }
        }
        return config;
    }

    public static Scenario prepareScenario(Config config, boolean drtSpeedUp) {
        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);
        if(drtSpeedUp) {
            for (Person person : scenario.getPopulation().getPersons().values()) {
                person.getPlans().removeIf((plan) -> plan != person.getSelectedPlan());
            }
        }
        return scenario;
    }

    public static Controler prepareControler(Scenario scenario) {
        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new CbaModule());
        return controler;
    }
}
