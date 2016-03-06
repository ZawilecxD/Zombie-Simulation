package zombieSimulator;

import java.util.List;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class Human {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int stamina;
	private int startingStamina;
	
	public Human(ContinuousSpace<Object> space, Grid<Object> grid, int startingEnergy) {
		this.space = space;
		this.grid = grid;
		this.stamina = startingEnergy;
		this.startingStamina = startingEnergy;
	}
	
	@Watch(watcheeClassName = "zombieSimulator.Zombie", watcheeFieldNames = "moved",
			query = "within_moore 1",
			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void escape() {
		//punkt aktualnej lokacji czlowieka
		GridPoint currentPoint = grid.getLocation(this);
		
		GridCellNgh<Zombie> nghCreator = new GridCellNgh<>(grid, currentPoint, Zombie.class, 1,1);
		List<GridCell<Zombie>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint whereLeastZombiesAre = null;
		int minCount = Integer.MAX_VALUE;
		for(GridCell<Zombie> cell : gridCells) {
			if(cell.size() < minCount) {
				whereLeastZombiesAre = cell.getPoint();
				minCount = cell.size();
			}
		}
		
		if(stamina > 0) {
			moveTo(whereLeastZombiesAre);
		} else {
			stamina = startingStamina;
		}
		
	}
	
	public void moveTo(GridPoint targetPoint) {
		if(targetPoint.equals(grid.getLocation(this))) {
			return;
		} else {
			NdPoint currentPoint = space.getLocation(this);
			NdPoint toPoint = new NdPoint(targetPoint.getX(), targetPoint.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, currentPoint, toPoint);
			space.moveByVector(this, 2, angle, 0); //2 to dystans ruchu (ile unitow pokona)
			currentPoint = space.getLocation(this);
			grid.moveTo(this, (int)currentPoint.getX(), (int)currentPoint.getY());
			stamina--;
		}
	}
	
}
