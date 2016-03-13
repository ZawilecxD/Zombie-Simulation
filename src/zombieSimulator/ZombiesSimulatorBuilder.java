package zombieSimulator;

import org.apache.poi.ss.formula.functions.T;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class ZombiesSimulatorBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		
		context.setId("ZombieSimulator");
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("infection network", context, true);
		netBuilder.buildNetwork();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, 
				new RandomCartesianAdder < Object >(), 
				new repast . simphony . space . continuous . WrapAroundBorders ()
				, 50 , 50);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
				new SimpleGridAdder<Object>() ,
				true , 50 , 50));

		Parameters params = RunEnvironment.getInstance().getParameters();

		
		int zombieNum = (Integer)params.getValue ("zombiesCount");
		int peopleNum = (Integer)params.getValue ("humanCount");
		for(int i=0;i<zombieNum; i++) {
			context.add(new Zombie(space, grid));
		}
		
		for(int i=0;i<peopleNum;i++) {
			int energy = RandomHelper.nextIntFromTo(4, 10);
			context.add(new Human(space, grid, energy));
		}
		
		for(Object obj : context) {
			NdPoint point = space.getLocation(obj);
			grid.moveTo(obj, (int)point.getX(), (int)point.getY());
		}
		
		return context;
	}

}
