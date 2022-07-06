package org.matsim.run.drt;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.contrib.cba.CbaConfigGroup;
import org.matsim.contrib.cba.CbaModule;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.speedup.DrtSpeedUpParams;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.run.utils.ResumeSimulationFromLastPlans;
import org.matsim.core.config.CommandLine;

import java.util.ArrayList;
import java.util.List;


public class RunDrtOpenBerlinScenarioWithCba {

    /**
     * @param args : Expects at least a config-path argument. Also boolean arguments drt-speed-up and resume-simulation
     * @throws CommandLine.ConfigurationException
     */
    public static void main(String[] args) throws CommandLine.ConfigurationException {
        CommandLine cmd = new CommandLine.Builder(args)
                .requireOptions("config-path")
                .allowOptions("drt-speed-up", "resume-simulation", "no-private-cars")
                .build();
        String configPath = cmd.getOptionStrict("config-path");
        boolean resumeSimulation = cmd.hasOption("resume-simulation") && Boolean.parseBoolean(cmd.getOptionStrict("resume-simulation"));
        boolean drtSpeedUp = cmd.hasOption("drt-speed-up") && Boolean.parseBoolean(cmd.getOptionStrict("drt-speed-up"));
        boolean drtOnly = cmd.hasOption("no-private-cars") && Boolean.parseBoolean(cmd.getOptionStrict("no-private-cars"));
        Config config = prepareConfig(new String[]{configPath}, drtSpeedUp, resumeSimulation);
        cmd.applyConfiguration(config);
        Scenario scenario = prepareScenario(config, drtSpeedUp, drtOnly);
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

    public static Scenario prepareScenario(Config config, boolean drtSpeedUp, boolean drtOnly) {
        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);
        if(drtSpeedUp) {
            for (Person person : scenario.getPopulation().getPersons().values()) {
                person.getPlans().removeIf((plan) -> plan != person.getSelectedPlan());
            }
        }
        if(drtOnly) {
            for(Person person: scenario.getPopulation().getPersons().values()) {
                for(Plan plan: person.getPlans()) {
                    for(PlanElement planElement: plan.getPlanElements()) {
                        if(planElement instanceof Activity) {
                            Activity activity = (Activity) planElement;
                            if(activity.getType().equals("ride interaction")) {
                                activity.setType("car interaction");
                            }
                        }
                        if(planElement instanceof Leg) {
                            Leg leg = (Leg) planElement;
                            if(leg.getMode().equals("ride")) {
                                leg.setMode("car");
                                leg.getAttributes().putAttribute("routingMode", "car");
                            }
                            if("ride".equals(leg.getAttributes().getAttribute("routingMode"))) {
                                leg.getAttributes().putAttribute("routingMode", "car");
                            }
                        }
                    }
                }
            }
            for(Person person: scenario.getPopulation().getPersons().values()) {
                for (Plan plan: person.getPlans()) {
                    for(int i=0; i<plan.getPlanElements().size();i++) {
                        PlanElement planElement = plan.getPlanElements().get(i);
                        if (planElement instanceof Activity) {
                            Activity activity = (Activity) planElement;
                            if(activity.getType().equals("car interaction")) {
                                plan.getPlanElements().remove(i);
                                i--;
                            }
                        }
                        else if(planElement instanceof Leg) {
                            Leg leg = (Leg) planElement;
                            if("car".equals(leg.getAttributes().getAttribute("routingMode"))) {
                                if(leg.getMode().equals("car")){
                                    leg.setMode("drt");
                                    leg.setRoute(null);
                                } else {
                                    plan.getPlanElements().remove(i);
                                    i--;
                                }
                            }
                        }
                    }
                }
            }
            scenario.getConfig().qsim().setEndTime(72*3600);
            String[] oldModes = scenario.getConfig().subtourModeChoice().getModes();
            String[] modes = new String[oldModes.length-1];
            for(int i=0, j=0; i<oldModes.length; i++) {
                if(!oldModes[i].equals("car")){
                    modes[j]=oldModes[i];
                    j++;
                }
            }
            scenario.getConfig().subtourModeChoice().setModes(modes);
        }
        return scenario;
    }

    public static Controler prepareControler(Scenario scenario) {
        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new CbaModule());
        return controler;
    }
}
