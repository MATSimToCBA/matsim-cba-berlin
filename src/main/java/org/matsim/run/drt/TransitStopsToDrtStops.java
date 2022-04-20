package org.matsim.run.drt;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.run.BerlinExperimentalConfigGroup;

import java.util.*;

public class TransitStopsToDrtStops {

    private final static Logger logger = Logger.getLogger(TransitStopsToDrtStops.class);

    public static void main(String[] args) throws CommandLine.ConfigurationException {
        CommandLine cmd = new CommandLine.Builder(args) //
                .requireOptions("config-path", "output-path") //
                .build();
        String configPath = cmd.getOptionStrict("config-path");
        Config config = RunDrtOpenBerlinScenario.prepareConfig(new String[]{configPath});
        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);
        BerlinExperimentalConfigGroup berlinCfg = ConfigUtils.addOrGetModule(config, BerlinExperimentalConfigGroup.class);
        TransitSchedule schedule = scenario.getTransitSchedule();
        for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
            String drtServiceAreaShapeFile = drtCfg.getDrtServiceAreaShapeFile();
            BerlinShpUtils shpUtils = new BerlinShpUtils( drtServiceAreaShapeFile);

            Network drtNetwork = NetworkUtils.createNetwork();
            new TransportModeNetworkFilter(scenario.getNetwork()).filter(drtNetwork, Collections.singleton(drtCfg.getMode()));
            new NetworkCleaner().run(drtNetwork);
            List<TransitStopFacility> transitStopFacilities = new ArrayList<>(schedule.getFacilities().values());
            for(TransitStopFacility transitStopFacility : transitStopFacilities) {
                if(shpUtils.isCoordInDrtServiceAreaWithBuffer(transitStopFacility.getCoord(), berlinCfg.getTagDrtLinksBufferAroundServiceAreaShp())) {
                    Id<Link> linkId = NetworkUtils.getNearestLink(drtNetwork, transitStopFacility.getCoord()).getId();
                    transitStopFacility.setLinkId(linkId);
                }
                else {
                    schedule.removeStopFacility(transitStopFacility);
                }
            }
        }

        Set<Id<TransitLine>> transitLinesIds = new HashSet<>(schedule.getTransitLines().keySet());
        for(Id<TransitLine> transitLineId : transitLinesIds) {
            schedule.removeTransitLine(schedule.getTransitLines().get(transitLineId));
        }
        new TransitScheduleWriter(schedule).writeFile(cmd.getOptionStrict("output-path"));
    }
}
