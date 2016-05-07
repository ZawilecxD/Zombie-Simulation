package zombieSimulator;

import org.apache.poi.ss.formula.functions.T;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.data2.DataSource;
import repast.simphony.data2.builder.AggregateDataSetBuilder;
import repast.simphony.data2.builder.DataSetBuilder;
import repast.simphony.data2.builder.NonAggregateDataSetBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.DefaultGrid;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.visualization.grid.Grid2DLayout;

public class ZombiesSimulatorBuilder implements ContextBuilder<Object> {

	public static int zombieId = 0;
	public static int maxGroupSize = 5;
	public static int humanBaseDmg = 10;
	
	@Override
	public Context build(Context<Object> context) {
		zombieId = 0;
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

		
		//parameters
		int campsNum = (Integer) params.getValue("campsCount");
		int zombieNum = (Integer)params.getValue ("zombiesCount");
		int peopleNum = (Integer)params.getValue ("humanCount");
		int humanSpeed = (Integer) params.getValue("humanSpeed");
		int humanStartingStamina = (Integer) params.getValue("humanStartingStamina");
		int zombieSpeed = (Integer) params.getValue("zombieSpeed");
		int chanceToForgeWeapon = (Integer) params.getValue("chanceToForgeWeapon");
		maxGroupSize = (Integer) params.getValue("maxGroupSize");
		
		for(int i=0; i<campsNum; i++) {
			context.add(new HumanCamp(space, grid, chanceToForgeWeapon));
		}
		
		for(int i=0;i<zombieNum; i++) {
			context.add(new Zombie(space, grid, zombieSpeed));
		}
		
		for(int i=0;i<peopleNum;i++) {
			context.add(new Human(space, grid, humanStartingStamina, humanSpeed));
		}
		
		for(Object obj : context) {
			NdPoint point = space.getLocation(obj);
			grid.moveTo(obj, (int)point.getX(), (int)point.getY());
		}
		
		return context;
	}

	
}
